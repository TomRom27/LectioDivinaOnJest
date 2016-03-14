package com.tr.onjestslowo.app;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.tr.onjestslowo.model.Reading;
import com.tr.onjestslowo.service.ReadingService;
import com.tr.tools.DateHelper;
import com.tr.tools.UIHelper;
import com.tr.tools.Logger;


public class ReadingsActivity extends ActionBarActivity {

    private String ARG_SELECTED_DATE = "SelectedDate";
    public static String LOG_TAG = "ReadingActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    ReadingsPagerAdapter mReadingsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    /**
     * indicates the date of currently selected reading
     */
    Date mSelectedDate;

    List<Reading> mLoadedReadings;
    Boolean mMenuEnabled;
    ReadingService mReadingService;
    // we use this to cancel the tasks (or actually to prevent them to complete)
    // when activity was re-created
    // ('cause in this case we should start everything from the beginning)
    static Boolean mRefreshTaskCancelled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readings);

        //

//        boolean hasMenuKey =  ViewConfiguration.get(this).hasPermanentMenuKey();
//        Logger.debug(LOG_TAG, "Device has meny key: "+Boolean.toString(hasMenuKey));

        // customize action bar (app title bar)
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // set to display title and subtitle only
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        // set sub-title
        actionBar.setSubtitle(R.string.app_sub_name);

        mReadingService = new ReadingService(this);
        // Create the adapter that will return a fragment for each of the loaded reading
        mReadingsPagerAdapter = new ReadingsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.readingPager);


        mSelectedDate = DateHelper.getToday();
        // load from db and display all readings - async
        new LoadAndDisplayReadingsTask().execute();

        mMenuEnabled = true;

        // this variable must be static as we share it between different instances
        // of this class
        // scenario: refresh task is running when screen rotation occurs
        // then we have two instances activity class which must use the same variable
        mRefreshTaskCancelled = true;

        // if the app is launched for very first time, we force user to see
        // AboutLectio activity
        if (AppPreferences.getIsAppFirstLaunch(this)) {
            Logger.debug(LOG_TAG,"Launched first time, show AboutLectio");
            showAboutLectio();
            AppPreferences.setIsAppFirstLaunch(this, false);
        }
        else
            Logger.debug(LOG_TAG,"Another launch, no need to show AboutLectio");
    }

    //<editor-fold desc="activity overrides">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.readings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refreshReadingsAsync();
            return true;
        } else if (id == R.id.action_about_lectio) {
            showAboutLectio();
            return true;
        } else if (id == R.id.action_about_us) {
            showAboutUs();
            return true;
        } else if (id == R.id.action_settings) {
            showSettings();
            return true;
        } else if (id == R.id.action_clear) {
            confirmAndClearReadings();
            return true;
        } else if (id == R.id.action_exit) {
            exitApp();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString(ARG_SELECTED_DATE, DateHelper.toInternalString(mSelectedDate));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        if (savedInstanceState.containsKey(ARG_SELECTED_DATE))
            mSelectedDate = DateHelper.fromInternalString(savedInstanceState.getString(ARG_SELECTED_DATE));
        else
            mSelectedDate = DateHelper.getToday();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return mMenuEnabled;// super.onPrepareOptionsMenu(menu); // =false -> menu doesn't show
    }
    //</editor-fold>

    //<editor-fold desc="menu handling methods and reading display">
    private void exitApp() {
        finish();
    }

    private void showAboutLectio() {
        Intent intent = new Intent(this, AboutLectioActivity.class);
        startActivity(intent);
    }


    private void showAboutUs() {
        Intent intent = new Intent(this, AboutUsActivity.class);
        startActivity(intent);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void refreshReadingsAsync() {
        showRefreshInProgressNotification(this);
        // show info
        UIHelper.showToast(this, R.string.text_refreshing_started, Toast.LENGTH_SHORT);
        // disable menu
        disableAppMenu();
        // start the refresh task
        new RefreshAndDisplayReadingsTask(this).execute();
    }


    private void confirmAndClearReadings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.text_are_you_sure_to_delete).setPositiveButton(R.string.text_yes, clearReadingsDialogListener)
                .setNegativeButton(R.string.text_no, clearReadingsDialogListener).show();
    }

    private void removeAllReadings() {
        // remove all readings
        mReadingService.clearReadings();

        // refresh internal list
        mLoadedReadings.clear();

        displayReadings(mLoadedReadings);
    }
    //</editor-fold>

    //<editor-fold desc="private methods">

    private void displayReadings(List<Reading> loadedReadings) {
        mLoadedReadings = loadedReadings;
        if (mLoadedReadings.size() > 0) {
            showReadingPager();
        } else {
            showEmptyReading();
        }
    }

    private void disableAppMenu() {
        mMenuEnabled = false;
    }

    private void enableAppMenu() {
        mMenuEnabled = true;
    }

    private void showEmptyReading() {
        // hide pager
        mViewPager.setVisibility(View.GONE);
        // show empty reading view
        findViewById(R.id.emptyReadingContainer).setVisibility(View.VISIBLE);
    }

    private void showReadingPager() {
        findViewById(R.id.emptyReadingContainer).setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);

        mViewPager.setAdapter(mReadingsPagerAdapter);

        int selectedDateReadingIndex = getReadingIndexByDate(mSelectedDate);
        if (selectedDateReadingIndex < 0) {
            // if reading for current date wasn't found, we get last reading
            selectedDateReadingIndex = mLoadedReadings.size() - 1;
            mSelectedDate = mLoadedReadings.get(selectedDateReadingIndex).DateParsed;
        }

        mViewPager.setCurrentItem(selectedDateReadingIndex);
    }

    private int getReadingIndexByDate(Date date) {
        for (Reading reading : mLoadedReadings)
            if (reading.DateParsed.equals(date))
                return mLoadedReadings.indexOf(reading);

        // if we are here, reading with date was not found
        return -1;
    }
    //</editor-fold>


    //<editor-fold desc="refresh status bar notification">

    private void hideRefreshInProgressNotification() {
        Object service = getSystemService(NOTIFICATION_SERVICE);
        NotificationManager nm = (NotificationManager) service;
        nm.cancel(REFRESH_NOTIFICATION_ID);
    }
    static int REFRESH_NOTIFICATION_ID=100012;
    private static void showRefreshInProgressNotification(Context context) {

        // The PendingIntent to launch our activity if the user selects this notification
        Intent intent = new Intent(context, ReadingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder
                .setSmallIcon(R.drawable.ic_refresh)
                //.setTicker(context.getResources().getString(R.string.text_refreshing_in_progress))
                //.setContentInfo("content info")
                //.setContentTitle("conten title")
                //.setContentText("content text")
                .setContentIntent(contentIntent)
                .build();

        // Set the icon, scrolling text and timestamp
        //notification.flags |= Notification.FLAG_ONGOING_EVENT;
        //notification.flags |= Notification.FLAG_NO_CLEAR;
        // Send the notification.
        NotificationManager mNM = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNM.notify(REFRESH_NOTIFICATION_ID, notification);
    }

    //<editor-fold> refresh status bar notification

    //<editor-fold desc="ReadingsPagerAdapter implementation">

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class ReadingsPagerAdapter extends FragmentStatePagerAdapter {

        public ReadingsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            // for the reading wanted by position
            Reading reading = mLoadedReadings.get(position);
            return ReadingPlaceholderFragment.newInstance(reading.Title, reading.DateParsed, reading.Content);
        }

        @Override
        public int getCount() {
            if (mLoadedReadings != null)
                return mLoadedReadings.size();
            else
                return 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (mLoadedReadings != null)
                return mLoadedReadings.get(position).Title;
            else
                return null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="ReadingPlaceholderFragment implementation">

    /**
     * A placeholder fragment containing a reading view.
     * (must be static if used (referenced) in XML - as we do,
     * see activity_reading and emptyReadingContainer
     */
    public static class ReadingPlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_READING_TITLE = "reading_title";
        private static final String ARG_READING_DATE = "reading_date";
        private static final String ARG_READING_CONTENT = "reading_content";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static ReadingPlaceholderFragment newInstance(String title, Date date, String content) {
            ReadingPlaceholderFragment fragment = new ReadingPlaceholderFragment();
            Bundle args = new Bundle();
            args.putString(ARG_READING_TITLE, title);
            args.putString(ARG_READING_DATE, DateHelper.toInternalString(date));
            args.putString(ARG_READING_CONTENT, content);

            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_readings, container, false);

            TextView titleView = (TextView) rootView.findViewById(R.id.titleView);
            WebView contentView = (WebView) rootView.findViewById(R.id.readingWebView);

            String title = "";
            String content = "";
            Bundle args = this.getArguments();
            if (args != null) {
                title = this.getArguments().getString(ARG_READING_TITLE);
                //String date = this.getArguments().getString(ARG_READING_DATE);
                content = this.getArguments().getString(ARG_READING_CONTENT);

            } else {
                title = rootView.getContext().getResources().getString(R.string.text_no_readings);
                content = rootView.getContext().getResources().getString(R.string.html_empty_reading_content);
            }

            titleView.setText(title);
            // we use loadDataWithBaseURL rather then loadData as the latter doesn't display
            // national characters correctly
            contentView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);

            // the below makes the web view transparent !!!
            contentView.setBackgroundColor(0x00000000);
            // if api-level >=11
            //cView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

            return rootView;
        }
    }
    //</editor-fold> // ReadingPlaceholderFragment

    //<editor-fold desc="Yes/No dialog for readings clear">
    DialogInterface.OnClickListener clearReadingsDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    removeAllReadings();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked do nothing
                    break;
            }
        }
    };
    //</editor-fold>

    //<editor-fold desc="LoadAndDisplayReadingsTask implementation">

    private class LoadAndDisplayReadingsTask extends AsyncTask<Void, Integer, List<Reading>> {

        protected List<Reading> doInBackground(Void... params) {
            List<Reading> loadedReadings;

            Logger.debug(LOG_TAG, "Loading readings async - started");
            loadedReadings = mReadingService.loadReadings();

            return loadedReadings;
        }

        protected void onPostExecute(List<Reading> loadedReadings) {
            Logger.debug(LOG_TAG, "Loading readings async - ended, displaying them now");
            displayReadings(loadedReadings);
        }
    }
    //</editor-fold>

    //<editor-fold desc="LoadAndDisplayReadingsTask implementation">

    // we declare this class as static due to it's dependency on activity
    // if outer class is referenced within inner class, then it is strong reference
    // what can result in memory leak if the outer class instance is re-created
    // and this is our case, as an activity is re-created by screen rotation
    // if it happens during Refresh non-static version of RefreshTask could cuase memory leaks
    private static class RefreshAndDisplayReadingsTask extends AsyncTask<Void, Integer, List<Reading>> {

        private final WeakReference<ReadingsActivity> mActivity;
        private int mNewReadingsCount;

        public RefreshAndDisplayReadingsTask(ReadingsActivity activity) {
            mActivity = new WeakReference<ReadingsActivity>(activity);
            mNewReadingsCount = 0;
        }

        protected List<Reading> doInBackground(Void... params) {
            List<Reading> loadedReadings = null;

            mRefreshTaskCancelled = false;
            try {
                Logger.debug(LOG_TAG, "Getting preferences");
                OnJestPreferences prefs = getPreferences(mActivity.get());

                Logger.debug(LOG_TAG, "Refreshing readings async - started");
                mNewReadingsCount = mActivity.get().mReadingService.refreshReadings(prefs.KeepReadingsHowLong,
                        prefs.UseProxy, prefs.ProxyHost, prefs.ProxyPort);

                Logger.debug(LOG_TAG, "Loading readings after refreshing");
                loadedReadings = mActivity.get().mReadingService.loadReadings();
            } catch (Exception ex) {
                // in case of exception we
                // log it
                Logger.error(ReadingsActivity.LOG_TAG, "Exception when refreshing the readings", ex);
                // return empty list
                loadedReadings = new ArrayList<Reading>();
                // the task is ended, so hide notification
                mActivity.get().hideRefreshInProgressNotification();
            } finally {
            }
            return loadedReadings;
        }

        private OnJestPreferences getPreferences(Context context) {
            OnJestPreferences prefs = new OnJestPreferences();

            SharedPreferences prefStore = PreferenceManager.getDefaultSharedPreferences(context);

            // remark !!!
            // we use EditTexPreference to enter the preferences, so they ALWAYS are String
            // (even if we use inputType=number
            // (that's why we can't use getInt from prefs)
            String key = context.getResources().getString(R.string.pref_reading_store_how_long);
            prefs.KeepReadingsHowLong = Integer.parseInt(prefStore.getString(key, "30"));
            if ((prefs.KeepReadingsHowLong <7) || (prefs.KeepReadingsHowLong >300))
                prefs.KeepReadingsHowLong = 30;

            key = context.getResources().getString(R.string.pref_wifi_proxy_enable);
            prefs.UseProxy = prefStore.getBoolean(key, false);

            key = context.getResources().getString(R.string.pref_wifi_proxy_host);
            prefs.ProxyHost = prefStore.getString(key, "");

            key = context.getResources().getString(R.string.pref_wifi_proxy_port);
            prefs.ProxyPort = Integer.parseInt(prefStore.getString(key, "8080"));

            return prefs;
        }

        protected void onPostExecute(List<Reading> loadedReadings) {

            // the task is ended, so hide notification
            mActivity.get().hideRefreshInProgressNotification();

            if (!mRefreshTaskCancelled) {
                String refreshEnded = mActivity.get().getResources().getString(R.string.text_refreshing_ended);
                UIHelper.showToast(mActivity.get(), String.format(refreshEnded, mNewReadingsCount), Toast.LENGTH_SHORT);

                Logger.debug(LOG_TAG, "Loading readings async - ended, displaying them now");
                mActivity.get().displayReadings(loadedReadings);

                Logger.debug(LOG_TAG, "Readings displayed");

                mActivity.get().enableAppMenu();
            } else
                Logger.debug(LOG_TAG, "Task is cancelled, nothing displayed");

            mRefreshTaskCancelled = true;
        }
    }

    private static class OnJestPreferences {
        public int KeepReadingsHowLong;
        public boolean UseProxy;
        public String ProxyHost;
        public int ProxyPort;

    }
    //</editor-fold>

}
