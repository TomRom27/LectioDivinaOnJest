package com.tr.onjestslowo.app;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.tr.tools.UIHelper;


public class AboutUsActivity extends AppCompatActivity {
    private static String ARG_IS_THEME_NIGHT = "ThemeNight";

    Boolean mIsThemeNight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // restore state of the theme flag and then apply theme
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(ARG_IS_THEME_NIGHT)))
            mIsThemeNight = savedInstanceState.getBoolean(ARG_IS_THEME_NIGHT);
        else {
            // we assume, nothing restorable
            mIsThemeNight = false;

            // check for passed params
            Bundle bundle = getIntent().getExtras();
            if ((bundle != null) && bundle.containsKey("params")) {
                Bundle params = bundle.getBundle("params");
                if ((params != null) && params.containsKey(ARG_IS_THEME_NIGHT))
                    mIsThemeNight = params.getBoolean(ARG_IS_THEME_NIGHT);
            }

        }
        // setting the theme must be done before any view output, also before super.onCreate() !!!
        setAppTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // set toolbar as actionbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.aboutUsToolBar);
        setSupportActionBar(toolbar);

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        // set to display title and subtitle only
        actionBar.setDisplayOptions(android.app.ActionBar.DISPLAY_SHOW_TITLE);

        actionBar.setDisplayHomeAsUpEnabled(true);

        WebView contentView = (WebView) findViewById(R.id.aboutUsWebView);

        String content  = getResources().getString(R.string.html_about_us);

        // we must use some trick in order to display content in wanted (not black" color
        content = UIHelper.setThemeColorForHtml(toolbar.getRootView(), R.attr.webView_textColor, content);

        contentView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);

        // the below makes the web view transparent !!!
        contentView.setBackgroundColor(0x00000000);

        setVersionInfoText();
    }

    private void setVersionInfoText() {
        String versionNumber;
        int version;

        try {
            String pkg = this.getPackageName();
            version = this.getPackageManager().getPackageInfo(pkg, 0).versionCode;
            versionNumber = this.getPackageManager().getPackageInfo(pkg, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionNumber = "?";
            version = -1;
        }

        TextView versionView = (TextView) findViewById(R.id.versionView);
        String versionFormat = getResources().getString(R.string.versionTextFormat);
        String versionText = String.format(versionFormat, versionNumber, version);
        versionView.setText(versionText);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putBoolean(ARG_IS_THEME_NIGHT, mIsThemeNight);
    }


    private void setAppTheme() {
        int themeId;
        if (mIsThemeNight)
            themeId = AppThemeHelper.GetNightThemeId();
        else
            themeId = AppThemeHelper.GetDayThemeId();
        this.setTheme(themeId);
    }

    public void onCloseClick(View view) {
        ((Activity)view.getContext()).finish();
    }
}
