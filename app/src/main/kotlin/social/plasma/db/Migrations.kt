package social.plasma.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.delete("contacts", null, null)
    }
}

val Migration_6_7 = object : Migration(6, 7) {
    val tempTableName = "temp_table"

    override fun migrate(db: SupportSQLiteDatabase) {
        addEventRefFk(db)
        addPubKeyRefFk(db)
        addHashTagRefFk(db)
    }

    private fun addHashTagRefFk(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $tempTableName")
        db.execSQL("CREATE TABLE IF NOT EXISTS $tempTableName AS SELECT * FROM hashtag_ref")
        db.execSQL("DELETE FROM hashtag_ref")
        db.execSQL("DROP TABLE IF EXISTS hashtag_ref")
        db.execSQL("CREATE TABLE IF NOT EXISTS hashtag_ref (`source_event` TEXT NOT NULL, `hashtag` TEXT NOT NULL, `pubkey` TEXT NOT NULL, PRIMARY KEY(`source_event`, `hashtag`), FOREIGN KEY(`source_event`) REFERENCES `events`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_hashtag_ref_hashtag` ON hashtag_ref (`hashtag`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_hashtag_ref_hashtag_source_event` ON hashtag_ref (`hashtag`, `source_event`)")
        db.execSQL("INSERT INTO hashtag_ref SELECT * FROM $tempTableName")
        db.execSQL("DROP TABLE IF EXISTS $tempTableName")
    }

    private fun addPubKeyRefFk(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $tempTableName")
        db.execSQL("CREATE TABLE IF NOT EXISTS $tempTableName AS SELECT * FROM pubkey_ref")
        db.execSQL("DELETE FROM pubkey_ref")
        db.execSQL("DROP TABLE IF EXISTS pubkey_ref")
        db.execSQL("CREATE TABLE IF NOT EXISTS pubkey_ref (`source_event` TEXT NOT NULL, `pubkey` TEXT NOT NULL, `relay_url` TEXT, PRIMARY KEY(`source_event`, `pubkey`), FOREIGN KEY(`source_event`) REFERENCES `events`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pubkey_ref_source_event` ON pubkey_ref (`source_event`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pubkey_ref_pubkey` ON pubkey_ref (`pubkey`)")
        db.execSQL("INSERT INTO pubkey_ref SELECT * FROM $tempTableName")
        db.execSQL("DROP TABLE IF EXISTS $tempTableName")
    }

    private fun addEventRefFk(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS $tempTableName")
        db.execSQL("CREATE TABLE IF NOT EXISTS $tempTableName AS SELECT * FROM event_ref")
        db.execSQL("DELETE FROM event_ref")
        db.execSQL("DROP TABLE IF EXISTS event_ref")
        db.execSQL("CREATE TABLE IF NOT EXISTS event_ref (`source_event` TEXT NOT NULL, `target_event` TEXT NOT NULL, `relay_url` TEXT, `marker` TEXT, PRIMARY KEY(`source_event`, `target_event`), FOREIGN KEY(`source_event`) REFERENCES `events`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_event_ref_target_event` ON event_ref (`target_event`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_event_ref_source_event` ON event_ref (`source_event`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pubkey_ref_pubkey` ON event_ref (`pubkey`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pubkey_ref_source_event` ON event_ref (`source_event`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS event_ref (`source_event` TEXT NOT NULL, `pubkey` TEXT NOT NULL, `relay_url` TEXT, PRIMARY KEY(`source_event`, `pubkey`), FOREIGN KEY(`source_event`) REFERENCES `events`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("INSERT INTO event_ref SELECT * FROM $tempTableName")
        db.execSQL("DROP TABLE IF EXISTS $tempTableName")
    }
}
