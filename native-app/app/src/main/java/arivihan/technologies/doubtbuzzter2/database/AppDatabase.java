package arivihan.technologies.doubtbuzzter2.database;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arivihan.technologies.doubtbuzzter2.converters.Converters;
import arivihan.technologies.doubtbuzzter2.database.dao.MediaDao;
import arivihan.technologies.doubtbuzzter2.database.dao.MicroLectureDao;
import arivihan.technologies.doubtbuzzter2.database.dao.NotesDao;
import arivihan.technologies.doubtbuzzter2.model.MediaEntity;
import arivihan.technologies.doubtbuzzter2.model.MicroLectureEntity;
import arivihan.technologies.doubtbuzzter2.model.offline.notes.NoteEntity;
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys;

@Database(
        version = 12,
        entities = {
                MediaEntity.class,
                MicroLectureEntity.class,
                NoteEntity.class
        }
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MediaDao mediaDao();

    public abstract NotesDao notesDao();

    public abstract MicroLectureDao microLectureDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "media_database"
                            )
                            .addMigrations(
                                    MIGRATION_8_9,
                                    MIGRATION_9_10,
                                    MIGRATION_10_11,
                                    MIGRATION_11_12
                            )
                            .fallbackToDestructiveMigration()
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Migration 8 → 9
    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE micro_lecture_new_table ADD COLUMN typeSelected TEXT");
        }
    };

    // Migration 9 → 10
    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE media_table ADD COLUMN locked INTEGER NOT NULL DEFAULT 0");
        }
    };

    // Migration 10 → 11 (adds notes_table safely if missing)
    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Check if notes_table already exists
//            boolean tableExists = false;
//            Cursor cursor = database.query(
//                    "SELECT name FROM sqlite_master WHERE type='table' AND name='notes_table'"
//            );
//            try {
//                if (cursor.moveToFirst()) {
//                    tableExists = true;
//                }
//            } finally {
//                cursor.close();
//            }
//
//            if (!tableExists) {
//                database.execSQL(
//                        "CREATE TABLE IF NOT EXISTS notes_table (" +
//                                "id TEXT NOT NULL PRIMARY KEY, " +
//                                "subjectId TEXT, " +
//                                "subjectName TEXT, " +
//                                "chapterTitle TEXT, " +
//                                "notesBy TEXT, " +
//                                "notesUrl TEXT, " +
//                                "locked INTEGER NOT NULL, " +
//                                "pageCount INTEGER NOT NULL, " +
//                                "fileSize INTEGER NOT NULL, " +
//                                "fileType TEXT, " +
//                                "status TEXT, " +
//                                "progress INTEGER NOT NULL, " +
//                                "userId TEXT, " +
//                                "thumbnailUrl TEXT" +
//                                ")"
//                );
//            }
        }
    };

    static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS notes_table (\n" +
                            "    id TEXT NOT NULL PRIMARY KEY,\n" +
                            "    subjectId TEXT,\n" +
                            "    subjectName TEXT,\n" +
                            "    chapterTitle TEXT,\n" +
                            "    notesBy TEXT,\n" +
                            "    notesUrl TEXT,\n" +
                            "    locked INTEGER NOT NULL,\n" +
                            "    pageCount INTEGER NOT NULL,\n" +
                            "    fileSize INTEGER NOT NULL,\n" +
                            "    fileType TEXT,\n" +
                            "    status TEXT,\n" +
                            "    progress INTEGER NOT NULL,\n" +
                            "    userId TEXT,\n" +
                            "    localPath TEXT,\n" +
                            "    noteType TEXT,\n" +
                            "    language TEXT,\n" +
                            "    selected INTEGER NOT NULL DEFAULT 0\n" +
                            ");"
            );
        }
    };

    // Clear all data from tables
    public void clearDbData() {
        this.clearAllTables();
    }

    // 🔹 Helper: add column safely (future-proof migrations)
    public static void addColumnIfNotExists(SupportSQLiteDatabase db, String tableName,
                                            String columnName, String columnDefinition) {
        boolean columnExists = false;
        Cursor cursor = db.query("PRAGMA table_info(" + tableName + ")");
        try {
            int nameIndex = cursor.getColumnIndex(PreferenceKeys.NAME.getKey());
            while (cursor.moveToNext()) {
                String currentColumn = cursor.getString(nameIndex);
                if (currentColumn != null && currentColumn.equalsIgnoreCase(columnName)) {
                    columnExists = true;
                    break;
                }
            }
        } finally {
            cursor.close();
        }

        if (!columnExists) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }
}
