package com.tr.onjestslowo.service;

/**
 * Created by bpl2111 on 2014-05-30.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.tr.onjestslowo.model.Reading;
import com.tr.tools.DateHelper;
import com.tr.tools.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReadingDataSource {

    // Database fields
    private SQLiteDatabase database;
    private ReadingSQLiteHelper dbHelper;
    private String[] allColumns = {
            ReadingSQLiteHelper.COLUMN_ID,
            ReadingSQLiteHelper.COLUMN_TITLE,
            ReadingSQLiteHelper.COLUMN_DATE_PARSED,
            ReadingSQLiteHelper.COLUMN_CONTENT};

    public ReadingDataSource(Context context) {
        dbHelper = new ReadingSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void deleteAllReadings() {

            dbHelper.recreateReadingTable(database);
    }


    public int removeOlderReadings(Date limitDate) {
        String where =  ReadingSQLiteHelper.COLUMN_DATE_PARSED +"< ?";
        String[] whereArgs = new String[1];

        // internally we keep dates as long-s
        whereArgs[0] = Long.toString(DateHelper.toLong(limitDate));

        int count = database.delete(ReadingSQLiteHelper.TABLE_READINGS,where, whereArgs);

        return count;
    }

    public void addReading(Reading reading) {
        addOneReading(reading);
    }


    public void addReadings(List<Reading> readings) {
        for(Reading reading : readings)
            addOneReading(reading);
    }

    public Reading getReadingLastByDate() {

        String orderBy =ReadingSQLiteHelper.COLUMN_DATE_PARSED+" DESC";
        String limit = "1";


        Cursor cursor = database.query(ReadingSQLiteHelper.TABLE_READINGS,
                allColumns, null, null, null, null, orderBy, limit);

        Reading foundReading = null;

        if (cursor.moveToFirst())
            foundReading = cursorToReading(cursor);
        cursor.close();

        return foundReading;
    }

    public ArrayList<Reading> getAllReadingsSortByDate() {
        String orderBy =ReadingSQLiteHelper.COLUMN_DATE_PARSED;
        ArrayList<Reading> Readings = new ArrayList<Reading>();

        Cursor cursor = database.query(ReadingSQLiteHelper.TABLE_READINGS,
                allColumns, null, null, null, null, orderBy);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Reading Reading = cursorToReading(cursor);
            Readings.add(Reading);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return Readings;
    }

    public void deleteReading(Reading reading) {
        long id = reading.Id;
        Logger.debug("X", "Reading deleted with id: " + id);
        database.delete(ReadingSQLiteHelper.TABLE_READINGS, ReadingSQLiteHelper.COLUMN_ID + " = " + id, null);
    }


    private void addOneReading(Reading reading) {
        ContentValues values = new ContentValues();

        values.put(ReadingSQLiteHelper.COLUMN_TITLE, reading.Title);
        values.put(ReadingSQLiteHelper.COLUMN_DATE_PARSED, DateHelper.toLong(reading.DateParsed));
        values.put(ReadingSQLiteHelper.COLUMN_CONTENT, reading.Content);

        reading.Id = database.insert(ReadingSQLiteHelper.TABLE_READINGS, null, values);
    }

    private Reading cursorToReading(Cursor cursor) {
        Reading reading = new Reading();
        reading.Id = cursor.getLong(0);
        reading.Title = cursor.getString(1);
        reading.DateParsed = DateHelper.fromLong(cursor.getLong(2));
        reading.Content = cursor.getString(3);

        return reading;
    }
}
