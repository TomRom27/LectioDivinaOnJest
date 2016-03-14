package com.tr.onjestslowo.service;

/**
 * Created by bpl2111 on 2014-05-30.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tr.tools.Logger;

public class ReadingSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_READINGS = "Reading";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DATE_PARSED = "date_parsed";
    public static final String COLUMN_CONTENT = "content";

    private static final String DATABASE_NAME = "OnJestSlowo.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String READING_TABLE_CREATE = "create table "
            + TABLE_READINGS + "(" +
            COLUMN_ID + " integer primary key autoincrement" +
            ", "+COLUMN_TITLE + " text not null"+
            ", "+COLUMN_DATE_PARSED + " bigint not null"+
            ", "+COLUMN_CONTENT + " text not null"+
            ");";

    private static final String READING_TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_READINGS;

    public ReadingSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        createReadingTable(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.warning(ReadingSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        deleteReadingTable(db);
        onCreate(db);
    }

    public void recreateReadingTable(SQLiteDatabase db) {
        deleteReadingTable(db);
        createReadingTable(db);
    }

    private static void deleteReadingTable(SQLiteDatabase database) {

        Logger.warning(ReadingSQLiteHelper.class.getName(), "Deleting reading table");
        database.execSQL(READING_TABLE_DELETE);
    }

    private static void createReadingTable(SQLiteDatabase database) {

        Logger.info(ReadingSQLiteHelper.class.getName(), "Creating reading table");
        database.execSQL(READING_TABLE_CREATE);
    }
}
