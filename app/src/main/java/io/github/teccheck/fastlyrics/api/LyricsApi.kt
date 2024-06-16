package io.github.teccheck.fastlyrics.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.github.teccheck.fastlyrics.api.provider.Deezer
import io.github.teccheck.fastlyrics.api.provider.Genius
import io.github.teccheck.fastlyrics.api.provider.LrcLib
import io.github.teccheck.fastlyrics.api.provider.LyricsProvider
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SearchResult
import io.github.teccheck.fastlyrics.model.SongMeta
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import java.util.concurrent.Executors

object LyricsApi {

    private const val TAG = "LyricsApi"

    private val executor = Executors.newFixedThreadPool(2)

    private var providers: Array<LyricsProvider> = arrayOf(Genius, LrcLib)
    private var providers_synced: Array<LyricsProvider> = arrayOf(Deezer, LrcLib)

    private val provider: LyricsProvider
        get() = providers.first()

    private val provider_synced: LyricsProvider
        get() = providers_synced.first()

    fun setProviderOrder(order: Array<String>) {
        val all = LyricsProvider.getAllProviders()
        providers = order.mapNotNull { name -> all.find { provider -> provider.getName() == name } }
            .toTypedArray()
    }

    fun getLyricsAsync(
        songMeta: SongMeta,
        liveDataTarget: MutableLiveData<Result<SongWithLyrics, LyricsApiException>>,
        synced: Boolean = false
    ) {
        executor.submit {
            Log.d(TAG, "getLyricsAsync($synced)")

            val type = if (synced) LyricsType.LRC else LyricsType.RAW_TEXT
            val song = songMeta.artist?.let { LyricStorage.findSong(songMeta.title, it, type) }
            if (song != null) {
                liveDataTarget.postValue(Success(song))
                return@submit
            }

            val result = fetchLyrics(songMeta, synced)
            liveDataTarget.postValue(result)

            if (result is Success) {
                LyricStorage.store(result.value)
            }
        }
    }

    fun getLyricsAsync(
        searchResult: SearchResult,
        liveDataTarget: MutableLiveData<Result<SongWithLyrics, LyricsApiException>>
    ) {
        executor.submit {
            val provider = searchResult.provider

            val result = searchResult.id?.let { provider.fetchLyrics(it) } ?: Failure(
                LyricsNotFoundException()
            )
            liveDataTarget.postValue(result)

            if (result is Success) {
                LyricStorage.store(result.value)
            }
        }
    }

    fun search(
        query: String,
        liveDataTarget: MutableLiveData<Result<List<SearchResult>, LyricsApiException>>,
        provider: LyricsProvider = this.provider
    ) {
        executor.submit {
            liveDataTarget.postValue(provider.search(query))
            val results = provider.search(query)

            if (results is Success)
                results.value[0].id?.let { provider.fetchLyrics(it) }
        }
    }

    private fun fetchLyrics(
        songMeta: SongMeta,
        synced: Boolean = false
    ): Result<SongWithLyrics, LyricsApiException> {
        var bestResult: SearchResult? = null
        var bestResultScore = 0.0

        val providers = if (synced) {
            providers_synced
        } else {
            providers
        }

        for (provider in providers) {
            val search = provider.search(songMeta)
            if (search !is Success)
                continue

            val result = search.value.maxByOrNull { getResultScore(songMeta, it) } ?: continue
            val score = getResultScore(songMeta, result)

            Log.d(TAG, "Search with ${provider.getName()}: ${result.title} by ${result.artist}, score: $score")

            if (score > bestResultScore) {
                bestResult = result
                bestResultScore = score
            }

            if (score > 0.8)
                break
        }

        if (bestResult?.id == null) return Failure(LyricsNotFoundException())

        Log.d(TAG, "Best result: ${bestResult.title}, score: $bestResultScore")
        return bestResult.provider.fetchLyrics(bestResult.id!!)
    }

    private fun getResultScore(songMeta: SongMeta, searchResult: SearchResult): Double {
        var score = 0.0

        if (songMeta.title == searchResult.title)
            score += 0.5
        else if (songMeta.title.startsWith(searchResult.title))
            score += 0.4
        else if (searchResult.title.startsWith(songMeta.title))
            score += 0.3

        if (songMeta.artist == null)
            return score
        else if (songMeta.artist == searchResult.artist)
            score += 0.5
        else if (songMeta.artist.startsWith(searchResult.artist))
            score += 0.4
        else if (searchResult.artist.startsWith(songMeta.artist))
            score += 0.3

        return score
    }
}