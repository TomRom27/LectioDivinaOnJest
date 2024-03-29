package com.tr.onjestslowo.service;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tr.onjestslowo.app.R;
import com.tr.onjestslowo.model.Converter;
import com.tr.onjestslowo.model.JSONSerializer;
import com.tr.onjestslowo.model.Post;
import com.tr.onjestslowo.model.Reading;
import com.tr.onjestslowo.model.ReadingListResult;
import com.tr.onjestslowo.model.ShortContemplationsFile;
import com.tr.tools.DateHelper;
import com.tr.tools.HttpConnection;
import com.tr.tools.IOHelper;
import com.tr.tools.NetworkHelper;
import com.tr.tools.Logger;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
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
    private ShortContemplationDataSource mShortContemplationDS;
    private Activity mActivity;

    public ReadingService(Activity activity) {

        mReadingDS = new ReadingDataSource(activity);
        mShortContemplationDS = new ShortContemplationDataSource(activity);
        mActivity = activity;
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

    public ArrayList<ShortContemplationsFile> getShortContemplationsList(String path) {
        Logger.debug(LOG_TAG, "Getting a list of short contemplation files from " + path);
        try {
            ArrayList<ShortContemplationsFile> list = mShortContemplationDS.getAllFrom(path);

            Logger.debug(LOG_TAG, "Contemplation files found: " + Integer.toString(list.size()));
            return list;
        } catch (Exception ex) {
            Logger.debug(LOG_TAG, "Failed to get contemplation files: " + ex.getMessage());
            return new ArrayList<>();
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

    public String downloadCurrentShortContemplations(boolean useProxy, String proxyHost, int proxyPort, String destination) {
        Logger.debug(LOG_TAG, String.format("Starting to download short contemplations, useProxy:%s", Boolean.toString(useProxy)));

        try {

            Date contemplationsDate = determineDateOfContemplations();

            String contemplationsFileName = ShortContemplationsFile.getFileNameFromDate(contemplationsDate);
            Logger.debug(LOG_TAG, String.format("Filename is : %s", contemplationsFileName));

            int year, month;
            year = DateHelper.getYear(contemplationsDate);
            month = DateHelper.getMonth(contemplationsDate);

            // complete logging inside the mothod, no need to do it here
            Boolean ok = downloadShortContemplations(year, month, contemplationsFileName, destination, useProxy, proxyHost, proxyPort);


            if (ok) {
                Logger.debug(LOG_TAG, "Short contemplation downloaded Ok");
            } else {
                Logger.debug(LOG_TAG, "The previous try didn't work, trying previous month");
                year = DateHelper.getYear(DateHelper.addDay(contemplationsDate, -15));
                month = DateHelper.getMonth(DateHelper.addDay(contemplationsDate, -15));

                ok = downloadShortContemplations(year, month, contemplationsFileName, destination, useProxy, proxyHost, proxyPort);
                if (!ok) {
                    Logger.debug(LOG_TAG, "Failed for previous month");
                    contemplationsFileName = ""; // empty filename means file not downloaded
                }
            }

            return contemplationsFileName;
        } catch (Exception ex) {
            Logger.error(LOG_TAG, "Error when trying to download: " + ex.getMessage());
            return "";
        }
    }

    private Boolean downloadShortContemplations(int year, int month, String fileName, String destination,
                                                boolean useProxy, String proxyHost, int proxyPort) {
        boolean isOk = false;
        String fileUrlString = String.format("https://www.onjest.pl/slowo/wp-content/uploads/%d/%02d/%s", year, month, fileName);
        Logger.debug(LOG_TAG, String.format("Trying to get from %d of %d at %s", month, year, fileUrlString));

        InputStream input = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(fileUrlString);

            if (useProxy) {
                Logger.debug(LOG_TAG, "Proxy used for connections");
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                connection = (HttpURLConnection) url.openConnection(proxy);
            } else
                connection = (HttpURLConnection) url.openConnection();

            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Logger.debug(LOG_TAG, String.format("Error, server returned HTTP %s",
                        connection.getResponseCode() + " " + connection.getResponseMessage()));
                connection.disconnect();
                return false;
            }

            // ensure app can save the file, is fo
            IOHelper.verifyStoragePermissions(mActivity);
            // start to download the file
            input = connection.getInputStream();
            // ... and save it
            ensureFolder(destination);
            mShortContemplationDS.saveFromStream(fileName, destination, input);
            isOk = true;

        } catch (Exception ex) {
            Logger.debug(LOG_TAG, "Something went wrong: " + ex.getMessage());
            isOk = false;

        } finally {
            if (connection != null)
                connection.disconnect();
            try {
                if (input != null)
                    input.close();
            } catch (Exception ignored) {
            }

        }
        return isOk;
    }


    //<editor-fold> downloadCurrentShortContemplations private methods>
    private Date determineDateOfContemplations() {
        Date closestSunday = DateHelper.getClosestSunday(DateHelper.getToday());

        return closestSunday;
    }

    private void ensureFolder(String path) {
        File file = new File(path);

        if (!file.exists()) {
            Logger.debug(LOG_TAG, "Folder " + path + " not exists, will be created");
            file.mkdirs();
        } else
            Logger.debug(LOG_TAG, "Folder " + path + " exists already");
    }
    //</editor-fold>

    public int refreshReadings(int keepLastReadingDaysNumber, boolean useProxy, String proxyHost, int proxyPort, boolean useURI2, boolean showErrors ) throws Exception  {
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

            try {
                newReadings = downloadReadingsForRange(firstDate, lastDate, useProxy, proxyHost, proxyPort, useURI2, showErrors);
                if (newReadings.size() > 0) {
                    mReadingDS.addReadings(newReadings);
                }
                count = newReadings.size();
                deleteOutdatedreadings(keepLastReadingDaysNumber);
                return count;

            } catch (Exception ex) {
                if (showErrors)
                    throw ex;
                else
                    return 0;
            }


        } finally {
            mReadingDS.close();
            Logger.debug(LOG_TAG, String.format("Refreshing of readings ended with %d new.", count));
        }
    }


    //<editor-fold> refreshReadings private methods
    private ArrayList<Reading> downloadReadingsForRange(Date firstDate, Date lastDate, boolean useProxy, String proxyHost, int proxyPort,boolean useURI2, boolean showErrors) throws Exception {
        ArrayList<Reading> newReadings = new ArrayList<>();
        HttpConnection httpConnection = new HttpConnection(this.mActivity);

        prepareConnection(useProxy, proxyHost, proxyPort, httpConnection);

        if (httpConnection.checkConnectivity()) {
            HttpConnection.CONNECTION_TIMEOUT = 10000; // increase time to 10 secs.

            try {

                if (useURI2)
                    newReadings = downloadReadingsForDates(firstDate, lastDate, httpConnection);
                else
                    newReadings = downloadReadingsOneByOne(firstDate, lastDate, httpConnection);

            } catch (Exception ex) {
                Logger.error(LOG_TAG, ex.getMessage());
                // in case of error we just silently finish the operation or not
                if (showErrors)
                    throw ex;
            }
        } else {
            Logger.debug(LOG_TAG, "No connection available, download terminated");
            if (showErrors)
                throw new Exception("Brak połączenia z internetem");
        }
        return newReadings;
    }

    private ArrayList<Reading> downloadReadingsOneByOne(Date firstDate, Date lastDate, HttpConnection httpConnection) throws Exception {
        String onJestUrlForOneDay = getOnJestForOneDayUrl();
        ArrayList<Reading> newReadings = new ArrayList<>();

        Logger.debug(LOG_TAG, String.format("Server base url is:%s", onJestUrlForOneDay));

        // we get readings day by day - in the loop until we reach lastDate
        for (Date currentDate = firstDate; !currentDate.after(lastDate); currentDate = DateHelper.addDay(currentDate, 1)) {

            newReadings.addAll(downloadReadingsForOneDate(onJestUrlForOneDay, currentDate, httpConnection));
        }

        return newReadings;
    }

    private ArrayList<Reading> downloadReadingsForOneDate(String onJestUrlForOneDay, Date currentDate, HttpConnection httpConnection) throws Exception {
        ArrayList<Reading> readings = new ArrayList<>();

        // prepare specific url for current date
        String actualOnJestUrl = String.format(onJestUrlForOneDay, formatDateForOnJestServer(currentDate));

        // retrieve JSON
        String response = httpConnection.requestFromService(actualOnJestUrl);

        ReadingListResult result = JSONSerializer.deserializePostListResult(response);
        if (result.status.equals("ok"))
            for (Post post : result.posts)
                readings.add(Converter.Convert(post));

        return readings;
    }

    private ArrayList<Reading> downloadReadingsForDates(Date fromDate, Date toDate, HttpConnection httpConnection) throws Exception {
        String onJestUrlForDates = getOnJestForDatesUrl();
        ArrayList<Reading> newReadings = new ArrayList<>();

        Logger.debug(LOG_TAG, String.format("Server base url is:%s", onJestUrlForDates));
        ArrayList<Reading> readings = new ArrayList<>();
        Date currentToDate;

        while ( !fromDate.after(toDate) ) {
            int maxPosts = DateHelper.days(toDate,fromDate,true);
            maxPosts = Math.min(maxPosts,31); // we never take more then 31 posts in one shot to server
            currentToDate = DateHelper.addDay(fromDate,maxPosts);

            String actualOnJestUrl = String.format(onJestUrlForDates,
                    formatDateForOnJestServer(fromDate),
                    formatDateForOnJestServer(currentToDate), maxPosts);

            // retrieve JSON
            String response = httpConnection.requestFromService(actualOnJestUrl);

            ReadingListResult result = JSONSerializer.deserializePostListResult(response);
            if (result.status.equals("ok")) {
                for (Post post : result.posts)
                    readings.add(Converter.Convert(post));
            }

            fromDate = DateHelper.addDay(currentToDate,1);
        }

        return readings;
    }

    private void deleteOutdatedreadings(int keepLastReadingDaysNumber) {
        // calculate the date of readings to keep
        Date limitDate = DateHelper.addDay(DateHelper.getToday(), -1 * keepLastReadingDaysNumber + 1);
        // remove obsolete readings
        int count = mReadingDS.removeOlderReadings(limitDate);
        Logger.debug(LOG_TAG, Integer.toString(count) + " rows deleted");
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
            while (lastDate.compareTo(DateHelper.getToday()) < 0);

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

    private String getOnJestForOneDayUrl() {
        // https://www.onjest.pl/slowo/?json=get_date_posts&date=201406&count=30&include=date,title,content
        // https://www.onjest.pl/slowo/api/core/get_posts/?count=7&page=1&include=date,title,content
        // https://www.onjest.pl/slowo/api/core/get_posts/?count=7&page=1&include=date,title&post_status=future OR any

        String uri = getPreferenceString(R.string.pref_adv_server_uri,
                "https://www.onjest.pl/slowo/?json=get_date_posts&date=%s&include=title,date,content");
        if (true)
            uri = uri.replace("http:","https:");
        return uri;
    }

    private String getOnJestForDatesUrl() {
         // https://www.onjest.pl/slowo/?json=get_dates_posts&fromdate=2021-10-29&todate=20211103&include=title,date,content&count=30

        String uri = getPreferenceString(R.string.pref_adv_server_uri2,
                "https://www.onjest.pl/slowo/?json=get_dates_posts&fromdate=%s&todate=%s&include=title,date,content&count=%d");
        if (true)
            uri = uri.replace("http:","https:");
        return uri;
    }
    //</editor-fold> refreshReadings private methods


    private void prepareConnection(boolean useProxy, String proxyHost, int proxyPort, HttpConnection httpConnection) {
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

    }

    private String getPreferenceString(int preferenceResourceId, String defValue) {
        String prefKey = mActivity.getResources().getString(preferenceResourceId);
        SharedPreferences prefsStore = PreferenceManager.getDefaultSharedPreferences(mActivity);

        return prefsStore.getString(prefKey, defValue);
    }
}
