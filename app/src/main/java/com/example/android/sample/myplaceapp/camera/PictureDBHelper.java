package com.example.android.sample.myplaceapp.camera;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 日付情報を保存するデータベース。
 */
public class PictureDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Picture.db";

    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "PICTURE";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_FILE_PATH = "file_path";

    public static final String COLUMN_DATE_STR = "date_str";

    public static final String COLUMN_REGISTER_TIME = "register_time";

    /**
     * コンストラクタ。
     *
     * @param context
     */
    public PictureDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FILE_PATH + " TEXT NOT NULL,"
                + COLUMN_DATE_STR + " TEXT NOT NULL,"
                + COLUMN_REGISTER_TIME + " TIMESTAMP DEFAULT (DATETIME('now','localtime'))" + ")";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing.
    }
}
