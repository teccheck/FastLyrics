package io.github.teccheck.fastlyrics.ui.saved

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.forkhandles.result4k.Result
import io.github.teccheck.fastlyrics.api.LyricStorage
import io.github.teccheck.fastlyrics.exceptions.LyricsApiException
import io.github.teccheck.fastlyrics.model.SongWithLyrics

class SavedViewModel : ViewModel() {
    private val _songs = MutableLiveData<Result<List<SongWithLyrics>, LyricsApiException>>()
    val songs: LiveData<Result<List<SongWithLyrics>, LyricsApiException>> = _songs

    fun fetchSongs() {
        LyricStorage.getSongsAsync(_songs)
    }
}
