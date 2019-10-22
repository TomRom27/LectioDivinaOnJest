package com.tr.onjestslowo.app;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tr.onjestslowo.model.OnJestPreferences;
import com.tr.onjestslowo.model.Reading;
import com.tr.onjestslowo.service.ReadingService;
import com.tr.onjestslowo.service.ShortContemplationDataSource;
import com.tr.tools.UIHelper;
import com.tr.tools.Logger;


public class ReadingsActivity extends AppCompatActivity
        implements
        LectioDivinaFragment.OnLectioDivinaFragmentListener,
        ShortContemplationsFragment.OnShortContempationsListener,
        SimpleGestureFilter.SimpleGestureListener {

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

    SimpleGestureFilter mDetector;
    Boolean mDetectorEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // restore state of the theme flag and then apply theme, either from savedInstance
        // or from preferences (after app re-launch)
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_IS_THEME_NIGHT)))
            mIsThemeNight = savedInstanceState.getBoolean(ARG_IS_THEME_NIGHT);
        else
            mIsThemeNight = AppPreferences.getInstance(this).isLastThemeNight();
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
            mZoomVisible = AppPreferences.getInstance(this).get().ShowZoomOnStart;

        // prevent screen turn off
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        enableAppStatsSending(AppPreferences.getInstance(this).get().SendAppStats);

        // if the app is launched for very first time, we force user to see
        // AboutLectio activity
        if (AppPreferences.getInstance(this).isAppFirstLaunch()) {
            Logger.debug(LOG_TAG, "Launched first time, show AboutLectio");
            initiateApp();
        } else
            Logger.debug(LOG_TAG, "Another launch, no need to show AboutLectio");

        // Detect touched area
        mDetector = new SimpleGestureFilter(this,this);
        mDetectorEnabled = true;
    }

    // implementation of OnLectioDivinaFragmentListener interface
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (mDetectorEnabled) {
            mDetector.onTouchEvent(motionEvent);
        }
        return super.dispatchTouchEvent(motionEvent);
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // the code below can result in displaying a popup window, attached to
        // Readings Activity so the latter must have a window
        // that's why the code must be here, not in onCreate (that would be too early)

        // check whether we need to get consent for analytics from user
        // AboutLectio activity
        if (!AppPreferences.getInstance(this).isStatsInfoShown()) {
            Logger.debug(LOG_TAG, "Stats info not shown yet, doing now");
            getAppStatsConfirmation(findViewById(R.id.activityReadings));
        } else
            Logger.debug(LOG_TAG, "Stats info already shown");
    }

    //<editor-fold desc="menu handling methods and reading display">
    private void exitApp() {
        finish();
    }

    private void initiateApp() {
        AppPreferences appPreferences = AppPreferences.getInstance(this);

        // set and save default download path
//        OnJestPreferences prefs = appPreferences.get();
//        ShortContemplationDataSource ds = new ShortContemplationDataSource(this);
//        prefs.ShortContemplationDownloadPath = ds.defaultDestinationFolder();
//
//        Logger.debug(LOG_TAG, "Saving path for short contemplations as: " + prefs.ShortContemplationDownloadPath);
//        appPreferences.setShortContemplationDownloadPath(prefs.ShortContemplationDownloadPath);
        setDefaultShortContemplationDownloadPath(this, appPreferences);

        // show info
        showAboutLectio();
        appPreferences.setAppFirstLaunch(false);
    }


    private String setDefaultShortContemplationDownloadPath(Context context, AppPreferences appPreferences) {
        OnJestPreferences prefs = appPreferences.get();
        ShortContemplationDataSource ds = new ShortContemplationDataSource(context);
        prefs.ShortContemplationDownloadPath = ds.defaultDestinationFolder();

        Logger.debug(LOG_TAG, "Saving path for short contemplations as: " + prefs.ShortContemplationDownloadPath);
        appPreferences.setShortContemplationDownloadPath(prefs.ShortContemplationDownloadPath);

        return prefs.ShortContemplationDownloadPath;
    }

    private void showAboutLectio() {
        Intent intent = new Intent(this, AboutLectioActivity.class);

        Bundle params = new Bundle();
        params.putBoolean(ARG_IS_THEME_NIGHT, mIsThemeNight);
        intent.putExtra("params", params);

        startActivity(intent);
    }


    private void showAboutUs() {
        Intent intent = new Intent(this, AboutUsActivity.class);

        Bundle params = new Bundle();
        params.putBoolean(ARG_IS_THEME_NIGHT, mIsThemeNight);
        intent.putExtra("params", params);

        startActivity(intent);
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    //<editor-fold getAppStatsConfirmation related >
    private void getAppStatsConfirmation(View parentView) {
        // stats info is displayed as pop-up i.e. it is not separate activity
        // so we must prepare current axtivity for it


        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.activity_confirm_analytics, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        popupWindow.showAtLocation(parentView , Gravity.CENTER, 0, 0);

        Button closeButton = (Button) popupView.findViewById(R.id.buttonPopupClose);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setAppStatsSending(((CheckBox)popupView.findViewById(R.id.checkboxPopupAllow)).isChecked());
                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });

    }

    private void setAppStatsSending(boolean enabled) {
        Logger.debug(LOG_TAG,String.format("User has enabled to send app stats = %b",enabled));
        AppPreferences appPreferences =  AppPreferences.getInstance(this);
        appPreferences.setStatsInfoShown(true);

        Logger.debug(LOG_TAG,"Saving current state to settings");
        appPreferences.setStatsInfoEnabled(enabled);

        enableAppStatsSending(enabled);
    }
     //</editor-fold>

    //<editor-fold> app analytics
    private void enableAppStatsSending(boolean enabled) {
        Logger.debug(LOG_TAG,String.format("Setting analytics in Firebase =%b", enabled));
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(enabled);
    }

    private void logRefreshInAnalytics() {
        if (AppPreferences.getInstance(this).get().SendAppStats)
        {
            Logger.debug(LOG_TAG,"Sending analytics to Firebase about Refresh");
            FirebaseAnalytics.getInstance(this).logEvent("Get_Readings", null);
        }
        else
            Logger.debug(LOG_TAG,"Sending analytics is disabled");
    }
    //</editor-fold>

    private void refreshReadingsAsync() {
        showHideProgress(true);
        // show info
        UIHelper.showToast(this, R.string.text_refreshing_started, Toast.LENGTH_SHORT);
        // disable menu
        disableAppMenu();
        // disable custom gesture handling
        mDetectorEnabled = false;

        logRefreshInAnalytics();

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
        // save to
        AppPreferences.getInstance(this).setLastThemeNight(mIsThemeNight);
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

    private ShortContemplationsFragment findShortContemplationsFragment() {
        for (Fragment f : getSupportFragmentManager().getFragments())
            if (f instanceof ShortContemplationsFragment)
                return (ShortContemplationsFragment) f;

        return null;
    }

    private LectioDivinaFragment findLectioDivinaFragment() {
        for (Fragment f : getSupportFragmentManager().getFragments())
            if (f instanceof LectioDivinaFragment)
                return (LectioDivinaFragment) f;

        return null;
    }

    private void displayReadings(List<Reading> loadedReadings) {

        LectioDivinaFragment lectioFragment = findLectioDivinaFragment();
        if ((lectioFragment != null))
            //&& (lectioFragment.isVisible()))
            lectioFragment.displayReadings((ArrayList<Reading>) loadedReadings);
    }

    private void refreshShortContemplations() {
        ShortContemplationsFragment contemplationsFragment = findShortContemplationsFragment();
        if (contemplationsFragment != null)
            contemplationsFragment.refresh();
    }

    private void disableAppMenu() {
        mMenuEnabled = false;
    }

    private void enableAppMenu() {
        mMenuEnabled = true;
    }

    //</editor-fold>

    //<editor-fold desc="SimpleGestureListener interface methods">
    @Override
    public void onSwipe(int direction) {
        if (direction == SimpleGestureFilter.SWIPE_DOWN) {
            refreshReadingsAsync();
        }
    }

    @Override
    public void onDoubleTap() {
        refreshReadingsAsync();
    }
    //</editor-fold>

    //<editor-fold desc="refresh progress bar">
    public void showHideProgress(boolean show) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        if (progressBar != null)
            if (show)
                progressBar.setVisibility(View.VISIBLE);
            else
                progressBar.setVisibility(View.GONE);
    }

    //</editor-fold> refresh progress bar

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
                    return LectioDivinaFragment.newInstance(mZoomVisible);
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
    private static class RefreshAndDisplayReadingsTask extends AsyncTask<Void, Integer, RefreshResult> {

        private final WeakReference<ReadingsActivity> mActivity;
        private int mNewReadingsCount;

        public RefreshAndDisplayReadingsTask(ReadingsActivity activity) {
            mActivity = new WeakReference<>(activity);
            mNewReadingsCount = 0;
        }

        protected RefreshResult doInBackground(Void... params) {

            RefreshResult refreshResult = new RefreshResult();

            mRefreshTaskCancelled = false;
            try {
                Logger.debug(LOG_TAG, "Getting preferences");
                AppPreferences appPreferences = AppPreferences.getInstance(mActivity.get());

                OnJestPreferences prefs = appPreferences.get();

                Logger.debug(LOG_TAG, "Refreshing readings");
                mNewReadingsCount = mActivity.get().mReadingService.refreshReadings(prefs.KeepReadingsHowLong,
                        prefs.UseProxy, prefs.ProxyHost, prefs.ProxyPort, prefs.ShowDownloadErrors);

                if (prefs.DownloadShortContemplation) {
                    ensureShortContemplationDownloadPath(appPreferences, prefs);

                    Logger.debug(LOG_TAG, "Downloading current short contemplations");
                    refreshResult.ShortContemplationsFilename = mActivity.get().
                            mReadingService.downloadCurrentShortContemplations(prefs.UseProxy, prefs.ProxyHost, prefs.ProxyPort, prefs.ShortContemplationDownloadPath);
                } else
                    Logger.debug(LOG_TAG, "Skipped to download short contemplations");

                Logger.debug(LOG_TAG, String.format("Data refreshing ended (%d readings, %b new short contemplations)",
                        mNewReadingsCount, refreshResult.ShortContemplationsFilename));

                refreshResult.Readings = mActivity.get().mReadingService.loadReadings();
            } catch (Exception ex) {
                // in case of exception we
                // log it
                Logger.error(ReadingsActivity.LOG_TAG, "Exception when refreshing the data", ex);

                refreshResult.ErrorMessage = ex.getMessage();

                // exception handling can't interact with UI since it is async task
                // UI operations can be continued in the OnPostExecute only!!!
            }
            return refreshResult;
        }

        private void ensureShortContemplationDownloadPath(AppPreferences appPreferences, OnJestPreferences prefs) {
            if (prefs.ShortContemplationDownloadPath == null || prefs.ShortContemplationDownloadPath.isEmpty()) {
                prefs.ShortContemplationDownloadPath = mActivity.get().setDefaultShortContemplationDownloadPath(mActivity.get(), appPreferences);
            }
        }

        protected void onPostExecute(RefreshResult refreshResult) {

            // the task is ended, so hide notification
            mActivity.get().showHideProgress(false);

            if (!mRefreshTaskCancelled) {
                if (TextUtils.isEmpty (refreshResult.ErrorMessage) ) {
                    // error message emoty, we've got readings

                    String refreshEnded;
                    // display result info text relevant to Lectio only or all
                    if (!refreshResult.ShortContemplationsFilename.isEmpty())
                        refreshEnded = mActivity.get().getResources().getString(R.string.text_refreshing_ended_all);
                    else
                        refreshEnded = mActivity.get().getResources().getString(R.string.text_refreshing_ended_Lectio);
                    UIHelper.showToast(mActivity.get(), String.format(refreshEnded, mNewReadingsCount), Toast.LENGTH_SHORT);

                    Logger.debug(LOG_TAG, "Loading readings async - ended, displaying them now");
                    mActivity.get().displayReadings(refreshResult.Readings);
                    Logger.debug(LOG_TAG, "Readings displayed");

                    if (!refreshResult.ShortContemplationsFilename.isEmpty()) {
                        Logger.debug(LOG_TAG, "Showing short contemplations file");
                        mActivity.get().refreshShortContemplations();
                    } else
                        Logger.debug(LOG_TAG, "Short contemplations filename is empty");
                }
                else
                {
                    UIHelper.showToast(mActivity.get(), "Błąd pobierania rozważań: "+refreshResult.ErrorMessage, Toast.LENGTH_LONG);
                }

                mActivity.get().enableAppMenu();
                mActivity.get().mDetectorEnabled = true;
            } else
                Logger.debug(LOG_TAG, "Task is cancelled, nothing displayed");

            mRefreshTaskCancelled = true;
        }

    }

    private static class RefreshResult {

        public RefreshResult() {
            Readings = new ArrayList<>();
            ShortContemplationsFilename = "";
        }

        public List<Reading> Readings;
        public String ShortContemplationsFilename;
        public String ErrorMessage;
    }

    //</editor-fold>

}
