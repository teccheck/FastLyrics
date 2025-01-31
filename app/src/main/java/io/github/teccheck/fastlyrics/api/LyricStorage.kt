package io.github.teccheck.fastlyrics.api

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import dev.forkhandles.result4k.Result
import io.github.teccheck.fastlyrics.api.storage.LyricsDatabase
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.exceptions.LyricsNotFoundException
import io.github.teccheck.fastlyrics.model.LyricsType
import io.github.teccheck.fastlyrics.model.SongWithLyrics
import io.github.teccheck.fastlyrics.utils.Utils
import java.util.concurrent.Executors

object LyricStorage {
    private const val TAG = "LyricsStorage"

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var database: LyricsDatabase

    fun init(context: Context) {
        database = Room.databaseBuilder(context, LyricsDatabase::class.java, "lyrics")
            .addMigrations(LyricsDatabase.MIGRATION_3_4)
            .build()
    }

    fun deleteAsync(ids: List<Long>) {
        executor.submit { database.songsDao().deleteAll(ids) }
    }

    fun getSongsAsync(liveDataTarget: MutableLiveData<Result<List<SongWithLyrics>, LyricsApiException>>) {
        Log.d(TAG, "fetchSongsAsync")
        executor.submit {
            liveDataTarget.postValue(
                Utils.result(
                    database.songsDao().getAll(),
                    LyricsNotFoundException()
                )
            )
        }
    }

    fun getSongAsync(id: Long, liveDataTarget: MutableLiveData<Result<SongWithLyrics, LyricsApiException>>) {
        executor.submit {
            liveDataTarget.postValue(
                Utils.result(getSong(id), LyricsNotFoundException())
            )
        }
    }

    fun store(song: SongWithLyrics) {
        if (findSong(song.title, song.artist, song.type) == null) {
            database.songsDao().insert(song)
        }
    }

    private fun getSong(id: Long): SongWithLyrics? = database.songsDao().getSong(id)

    fun findSong(title: String, artist: String): SongWithLyrics? = database.songsDao().findSong(title, artist)

    fun findSong(title: String, artist: String, type: LyricsType): SongWithLyrics? =
        database.songsDao().findSong(title, artist, type)
}
