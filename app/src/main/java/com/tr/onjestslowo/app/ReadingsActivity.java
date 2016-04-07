package com.tr.onjestslowo.app;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tr.onjestslowo.model.Reading;
import com.tr.onjestslowo.service.ReadingService;
import com.tr.tools.UIHelper;
import com.tr.tools.Logger;


public class ReadingsActivity extends AppCompatActivity
        implements LectioDivinaFragment.OnLectioDivinaFragmentListener {

    public static String LOG_TAG = "ReadingActivity";
    private static String ARG_ZOOM_VISIBLE = "ZoomVisible";
    private static String ARG_IS_THEME_NIGHT = "ThemeNight";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    Boolean mMenuEnabled;
    Boolean mZoomVisible;
    Boolean mIsThemeNight;
    ReadingService mReadingService;
    Menu mMenu;
    // we use this to cancel the tasks (or actually to prevent them to complete)
    // when activity was re-created
    // ('cause in this case we should start everything from the beginning)
    static Boolean mRefreshTaskCancelled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // restore state of the theme flag and then apply theme
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_IS_THEME_NIGHT)))
            mIsThemeNight = savedInstanceState.getBoolean(ARG_IS_THEME_NIGHT);
        else
            mIsThemeNight = false;
        // setting the theme must be done before any view output, also before super.onCreate() !!!
        setAppTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readings);


        // set toolbar as actionbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.readingsToolBar);
        setSupportActionBar(toolbar);

        // customize action bar (app title bar)
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // set to display title and subtitle only
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        // set sub-title
        actionBar.setSubtitle(R.string.app_sub_name);

        mReadingService = new ReadingService(this);

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.mainTabsPager);
        viewPager.setAdapter(new MainTabsPagerAdapter(getSupportFragmentManager(), ReadingsActivity.this));

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.mainTabs);
        tabLayout.setupWithViewPager(viewPager);

        mMenuEnabled = true;

        // this variable must be static as we share it between different instances
        // of this class
        // scenario: refresh task is running when screen rotation occurs
        // then we have two instances activity class which must use the same variable
        mRefreshTaskCancelled = true;

        // set only one of the zoom actions visible
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_ZOOM_VISIBLE)))
            mZoomVisible = savedInstanceState.getBoolean(ARG_ZOOM_VISIBLE);
        else
            mZoomVisible = AppPreferences.getShowZoomOnStart(this);


        // if the app is launched for very first time, we force user to see
        // AboutLectio activity
        if (AppPreferences.getIsAppFirstLaunch(this)) {
            Logger.debug(LOG_TAG, "Launched first time, show AboutLectio");
            showAboutLectio();
            AppPreferences.setIsAppFirstLaunch(this, false);
        } else
            Logger.debug(LOG_TAG, "Another launch, no need to show AboutLectio");
    }

    public ReadingService onGetReadingService() {
        return mReadingService;
    }

    //<editor-fold desc="activity overrides">
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.readings, menu);
        mMenu = menu;

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mMenuEnabled) {
            // we need to show/hide zoom actions
            menu.findItem(R.id.action_zoom_to_hidden).setVisible(mZoomVisible);
            menu.findItem(R.id.action_zoom_to_visible).setVisible(!mZoomVisible);
            // theme actions
            menu.findItem(R.id.action_theme_to_night).setVisible(!mIsThemeNight);
            menu.findItem(R.id.action_theme_to_day).setVisible(mIsThemeNight);
        }
        return mMenuEnabled;// super.onPrepareOptionsMenu(menu); // =false -> menu doesn't show
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
        } else if (id == R.id.action_zoom_to_hidden) {
            disableZoom();
            return true;
        } else if (id == R.id.action_zoom_to_visible) {
            enableZoom();
            return true;
        } else if ((id == R.id.action_theme_to_night) ||
                (id == R.id.action_theme_to_day)) {
            reverseTheme();
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
        savedInstanceState.putBoolean(ARG_ZOOM_VISIBLE, mZoomVisible);
        savedInstanceState.putBoolean(ARG_IS_THEME_NIGHT, mIsThemeNight);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        // mZoomVisible and mIsThemeNight have been restored in OnCreate,
        // so need to restore it here
        //mZoomVisible = savedInstanceState.getBoolean(ARG_ZOOM_VISIBLE);
        //mIsThemeNight = savedInstanceState.getBoolean(ARG_IS_THEME_NIGHT);
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

        displayReadings(new ArrayList<Reading>());
    }

    private void disableZoom() {
        LectioDivinaFragment lectioDivinaFragment = findLectioDivinaFragment();
        if (lectioDivinaFragment != null)
            lectioDivinaFragment.showZoom(false);
        mZoomVisible = false;
        onPrepareOptionsMenu(mMenu);
    }

    private void enableZoom() {
        LectioDivinaFragment lectioDivinaFragment = findLectioDivinaFragment();
        if (lectioDivinaFragment != null)
            lectioDivinaFragment.showZoom(true);
        mZoomVisible = true;
        onPrepareOptionsMenu(mMenu);
    }

    private void reverseTheme() {
        mIsThemeNight = !mIsThemeNight;

        // apply theme
        setAppTheme();
        // udpdate menu
        onPrepareOptionsMenu(mMenu);
        // the theme changes effect
        this.recreate();
    }
    //</editor-fold>

    //<editor-fold desc="private methods">

    private void setAppTheme() {
        int themeId;
        if (mIsThemeNight)
            themeId = AppThemeHelper.GetNightThemeId();
        else
            themeId = AppThemeHelper.GetDayThemeId();
        this.setTheme(themeId);
    }

    private LectioDivinaFragment findLectioDivinaFragment() {
        for (Fragment f : getSupportFragmentManager().getFragments())
            if (f instanceof LectioDivinaFragment)
                return (LectioDivinaFragment) f;

        return null;
    }

    private void displayReadings(List<Reading> loadedReadings) {
        // todo use Lectio Fragment
        LectioDivinaFragment lectioFragment = findLectioDivinaFragment();
        if ((lectioFragment != null) &&
                (lectioFragment.isVisible()))
            lectioFragment.displayReadings((ArrayList<Reading>) loadedReadings);
    }

    private void disableAppMenu() {
        mMenuEnabled = false;
    }

    private void enableAppMenu() {
        mMenuEnabled = true;
    }

    //</editor-fold>


    //<editor-fold desc="refresh status bar notification">

    private void hideRefreshInProgressNotification() {
        Object service = getSystemService(NOTIFICATION_SERVICE);
        NotificationManager nm = (NotificationManager) service;
        nm.cancel(REFRESH_NOTIFICATION_ID);
    }

    static int REFRESH_NOTIFICATION_ID = 100012;

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
        NotificationManager mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNM.notify(REFRESH_NOTIFICATION_ID, notification);
    }


    //<editor-fold> refresh status bar notification

    //<editor-fold desc="MainTabsPagerAdapter implementation">
    public class MainTabsPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private Context context;

        public MainTabsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return LectioDivinaFragment.newInstance();
                case 1:
                    return new ShortContemplationsFragment();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.tab_LectioDivina);
                case 1:
                    return getResources().getString(R.string.tab_ShortContemplation);
                default:
                    return "";
            }
        }
    }
    //</editor-fold>

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

    // we declare this class as static due to it's dependency on activity
    // if outer class is referenced within inner class, then it is strong reference
    // what can result in memory leak if the outer class instance is re-created
    // and this is our case, as an activity is re-created by screen rotation
    // if it happens during Refresh non-static version of RefreshTask could cuase memory leaks
    private static class RefreshAndDisplayReadingsTask extends AsyncTask<Void, Integer, List<Reading>> {

        private final WeakReference<ReadingsActivity> mActivity;
        private int mNewReadingsCount;

        public RefreshAndDisplayReadingsTask(ReadingsActivity activity) {
            mActivity = new WeakReference<>(activity);
            mNewReadingsCount = 0;
        }

        protected List<Reading> doInBackground(Void... params) {
            List<Reading> loadedReadings;

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
                loadedReadings = new ArrayList<>();
                // the task is ended, so hide notification
                mActivity.get().hideRefreshInProgressNotification();
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
            if ((prefs.KeepReadingsHowLong < 7) || (prefs.KeepReadingsHowLong > 300))
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
        public boolean ShowZoomOnStart;
        public boolean DownloadShortContempltion;

    }
    //</editor-fold>

}
