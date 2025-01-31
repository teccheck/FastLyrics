package io.github.teccheck.fastlyrics

import android.app.Application
import io.github.teccheck.fastlyrics.api.LyricStorage
import io.github.teccheck.fastlyrics.api.MediaSession
import io.github.teccheck.fastlyrics.utils.ProviderOrder

class FastLyricsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        LyricStorage.init(this)
        MediaSession.init(this)
        ProviderOrder.init(this)
    }
}
