package io.github.teccheck.fastlyrics.api.storage

import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.teccheck.fastlyrics.model.SongWithLyrics

@Database(
    entities = [SongWithLyrics::class],
    version = 5,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)]
)
abstract class LyricsDatabase : RoomDatabase() {
    abstract fun songsDao(): SongsDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d("Migration", "Start")
                db.execSQL("ALTER TABLE songs ADD COLUMN lyricsPlain TEXT")
                db.execSQL("ALTER TABLE songs ADD COLUMN lyricsSynced TEXT")
                db.execSQL("UPDATE songs SET lyricsPlain = lyrics")
                db.execSQL("UPDATE songs SET lyricsSynced = lyricsPlain WHERE type = 'LRC'")
                db.execSQL("UPDATE songs SET lyricsPlain = '' WHERE type = 'LRC'")
                db.execSQL("ALTER TABLE songs DROP COLUMN lyrics")
                Log.d("Migration", "End")
            }
        }
    }
}
