package io.github.teccheck.fastlyrics.api.storage

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import io.github.teccheck.fastlyrics.model.SongWithLyrics

@Database(
    entities = [SongWithLyrics::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class LyricsDatabase : RoomDatabase() {
    abstract fun songsDao(): SongsDao
}