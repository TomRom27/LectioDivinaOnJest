package com.tr.onjestslowo.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tr.onjestslowo.app.R;
import com.tr.onjestslowo.model.Converter;
import com.tr.onjestslowo.model.JSONSerializer;
import com.tr.onjestslowo.model.Post;
import com.tr.onjestslowo.model.Reading;
import com.tr.onjestslowo.model.ReadingListResult;
import com.tr.tools.DateHelper;
import com.tr.tools.HttpConnection;
import com.tr.tools.NetworkHelper;
import com.tr.tools.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bpl2111 on 2014-05-29.
 */
// in order to be more efficient, this class is responsible for open/close data source
// for all operations towards db it performs
public class ReadingService {

    public static String LOG_TAG = "ReadingService";

    private ReadingDataSource mReadingDS;
    private Context context;

    public ReadingService(Context context) {

        mReadingDS = new ReadingDataSource(context);
        this.context = context;
    }

    public ArrayList<Reading> loadReadings() {
        Logger.debug(LOG_TAG, "Starting to load readings from db.");

        ArrayList<Reading> list = new ArrayList<>();
        int count = 0;
        mReadingDS.open();

        try {
            list = mReadingDS.getAllReadingsSortByDate();
            count = list.size();
        } finally {
            mReadingDS.close();
            Logger.debug(LOG_TAG, String.format("Loaded %d readings", count));
        }

        return list;
    }

    public String downloadCurrentShortContemplations(boolean useProxy, String proxyHost, int proxyPort) {
        Logger.debug(LOG_TAG, String.format("Starting to download short contemplations, useProxy:%s", Boolean.toString(useProxy)));

        try {

            Date contemplationsDate = determineDateOfContemplations();

            String contemplationsFileName = resolveShortContemplationsFileName(contemplationsDate);
            Logger.debug(LOG_TAG, String.format("Filename is : %s", contemplationsFileName));

            // // TODO: 2016-04-18  
            return contemplationsFileName;
        }
        catch (Exception ex) {
            // todo
            return "";
        }
    }

    private Date determineDateOfContemplations() {
        Date closestSunday = DateHelper.getClosestSunday(DateHelper.getToday());

        return closestSunday;
    }

    private String resolveShortContemplationsFileName(Date sundayDate) {
        return DateHelper.toString("rkyyMMdd_br", sundayDate);
    }

    private String getXOne(int year, int month, String fileName) {
        // http://www.onjest.pl/slowo/wp-content/uploads/2016/04/rk160417_br.pdf
        return String.format("http://www.onjest.pl/slowo/wp-content/uploads/%d/%02d/%s",year,month, fileName);
    }
    public int refreshReadings(int keepLastReadingDaysNumber, boolean useProxy, String proxyHost, int proxyPort) {
        Logger.debug(LOG_TAG, String.format("Starting to refresh readings, keepLastReadingsNumber:%d, useProxy:%s", keepLastReadingDaysNumber, Boolean.toString(useProxy)));
        int count = 0;

        mReadingDS.open();
        try {
            Date[] rangeDates;
            Date firstDate, lastDate;
            List<Reading> newReadings;

            rangeDates = determineDateRangeForReadings(keepLastReadingDaysNumber);
            firstDate = rangeDates[0];
            lastDate = rangeDates[1];

            Logger.debug(LOG_TAG, String.format("Dates are: 1st:%s, last:%s ", firstDate.toString(), lastDate.toString()));

            newReadings = downloadReadingsForRange(firstDate, lastDate, useProxy, proxyHost, proxyPort);
            if (newReadings.size() > 0) {
                mReadingDS.addReadings(newReadings);
            }
            count = newReadings.size();
            deleteOutdatedreadings(keepLastReadingDaysNumber);

            return count;
        } finally {
            mReadingDS.close();
            Logger.debug(LOG_TAG, String.format("Refreshing of readings ended with %d new.", count));
        }
    }


    public void clearReadings() {
        mReadingDS.open();
        try {
            mReadingDS.deleteAllReadings();
        } finally {
            mReadingDS.close();
        }
    }

    private ArrayList<Reading> downloadReadingsForRange(Date firstDate, Date lastDate, boolean useProxy, String proxyHost, int proxyPort) {
        ArrayList<Reading> newReadings = new ArrayList<>();
        HttpConnection httpConnection = new HttpConnection(this.context);

        // set proxy, if needed - only for WiFi connections
        if (NetworkHelper.isWiFiConnection()) {
            if (useProxy) {
                Logger.debug(LOG_TAG, String.format("Proxy:%s, %d", proxyHost, proxyPort));
                httpConnection.setProxyServer(proxyHost, proxyPort);
            } else {
                Logger.debug(LOG_TAG, "No proxy used");
                httpConnection.resetProxyServer();
            }
        }
        if (httpConnection.checkConnectivity()) {
            HttpConnection.CONNECTION_TIMEOUT = 10000; // increase time to 10 secs.

            String onJestUrlForOneDay = getOnJestForOneDayUrl();
            Logger.debug(LOG_TAG, String.format("Server base url is:%s", onJestUrlForOneDay));

            try {
                // we get readings day by day - in the loop until we reach lastDate
                for (Date currentDate = firstDate; !currentDate.after(lastDate); currentDate = DateHelper.addDay(currentDate, 1)) {

                    newReadings.addAll(downloadReadingsForOneDate(onJestUrlForOneDay, currentDate, httpConnection));
                }
            } catch (Exception ex) {
                // in case of error we just silently finish the operation
                Logger.error(LOG_TAG, ex.getMessage());
            }
        } else
            Logger.debug(LOG_TAG, "No connection available, download terminated");


        return newReadings;
    }

    private ArrayList<Reading> downloadReadingsForOneDate(String onJestUrlForOneDay, Date currentDate, HttpConnection httpConnection) {
        ArrayList<Reading> readings = new ArrayList<>();

        // prepare specific url for current date
        String actualOnJestUrl = String.format(onJestUrlForOneDay, formatDateForOnJestServer(currentDate));
        try {
            // retrieve JSON
            String response = httpConnection.requestFromService(actualOnJestUrl);

            ReadingListResult result = JSONSerializer.deserializePostListResult(response);
            if (result.status.equals("ok"))
                for (Post post : result.posts)
                    readings.add(Converter.Convert(post));
        } catch (Exception ex) {
            // in case of error we just silently finish the operation
            Logger.error(LOG_TAG, ex.getMessage());
        }
        return readings;
    }


    private void deleteOutdatedreadings(int keepLastReadingDaysNumber) {
        // calculate the date of readings to keep
        Date limitDate = DateHelper.addDay(DateHelper.getToday(), -1 * keepLastReadingDaysNumber + 1);
        // remove obsolete readings
        int count = mReadingDS.removeOlderReadings(limitDate);
        Logger.debug(LOG_TAG, Integer.toString(count)+" rows deleted");
    }


    private Date[] determineDateRangeForReadings(int keepLastReadingDaysNumber) {
        Date[] rangeDates = new Date[2];
        Reading lastReading;
        Date firstDate, lastDate;

        // determine the period to get readings for
        lastReading = mReadingDS.getReadingLastByDate();

        if (lastReading != null) {
            // if we already have some readings - take next date
            firstDate = DateHelper.addDay(lastReading.DateParsed, 1);
            // we want to download readings for current date, no matter how old are the existing ones
            // so we ensure, that lastDate is not earlier then today
            lastDate = firstDate;
            do
                lastDate = DateHelper.getNextSaturday(lastDate);
            while (lastDate.compareTo(DateHelper.getToday())<0);

            Logger.debug(LOG_TAG, "Last reading is from " + lastReading.DateParsed.toString());
        } else {
            // if we don't have readings then we must take as many of them
            // as needed to match the KeepLastReadings number (more or less :-)
            // BUT
            // we take them always until Saturday that's why we deduct from next Saturday
            // rather then today

            lastDate = DateHelper.getNextSaturday(DateHelper.getToday());
            firstDate = DateHelper.addDay(lastDate, -1 * (keepLastReadingDaysNumber - 1));
        }
        rangeDates[0] = firstDate;
        rangeDates[1] = lastDate;

        return rangeDates;
    }

    private String formatDateForOnJestServer(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        return sdf.format(d);
    }

    private int getKeepLastReadingsNumber() {

        return 10; // todo
    }

    private String getOnJestForOneDayUrl() {
        // http://www.onjest.pl/slowo/?json=get_date_posts&date=201406&count=30&include=date,title,content
        // http://www.onjest.pl/slowo/api/core/get_posts/?count=7&page=1&include=date,title,content
        // http://www.onjest.pl/slowo/api/core/get_posts/?count=7&page=1&include=date,title&post_status=future OR any

        String uri = getPreferenceString(R.string.pref_adv_server_uri,
                "http://www.onjest.pl/slowo/?json=get_date_posts&date=%s&include=title,date,content");
        return uri;
    }

    private String getPreferenceString(int preferenceResourceId, String defValue) {
        String prefKey = context.getResources().getString(preferenceResourceId);
        SharedPreferences prefsStore = PreferenceManager.getDefaultSharedPreferences(context);

        return prefsStore.getString(prefKey, defValue);
    }
}
