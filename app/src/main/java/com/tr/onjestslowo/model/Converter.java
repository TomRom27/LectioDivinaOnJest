package com.tr.onjestslowo.model;

import android.util.Log;

import com.tr.tools.DateHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by bpl2111 on 2014-05-29.
 */
public class Converter {

    public static Reading Convert(Post post) throws Exception {
        Reading reading = new Reading();

        reading.Content = post.content;
        reading.DateParsed = ConvertDate(post.date);
        reading.Title = post.title;

        return reading;
    }

    private static Date ConvertDate(String dateString) throws ParseException {

        Date date;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = sdf.parse(dateString);
            // we don't need time in the date, so must remove it
            return DateHelper.getDateOnly(date);

        } catch (ParseException ex) {
            Log.e("Converter", "Failed to convert date from string:" + dateString, ex);
            throw ex;
        }

    }
}
