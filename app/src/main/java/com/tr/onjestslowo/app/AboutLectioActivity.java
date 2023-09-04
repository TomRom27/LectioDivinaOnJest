package com.tr.onjestslowo.app;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;

import com.tr.tools.UIHelper;


public class AboutLectioActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_about_lectio);

        // set toolbar as actionbar for the activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.aboutLectioToolBar);
        setSupportActionBar(toolbar);

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        // set to display title and subtitle only
        actionBar.setDisplayOptions(android.app.ActionBar.DISPLAY_SHOW_TITLE);

        actionBar.setDisplayHomeAsUpEnabled(true);

        WebView contentView = (WebView) findViewById(R.id.aboutLectioWebView);

        String content = getResources().getString(R.string.html_about_lectio);

        // we must use some trick in order to display content in wanted (not black" color
        content = UIHelper.setThemeColorForHtml(toolbar.getRootView(), R.attr.webView_textColor, content);

        contentView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);

        // the below makes the web view transparent !!!
        contentView.setBackgroundColor(0x00000000);


//        final Button button = (Button) findViewById(R.id.buttonClose);
//        button.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                finish();
//            }
//        });

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
        ((Activity) view.getContext()).finish();
    }
}
