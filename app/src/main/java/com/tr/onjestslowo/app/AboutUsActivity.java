package com.tr.onjestslowo.app;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;


public class AboutUsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // set to display title and subtitle only
        actionBar.setDisplayOptions(android.app.ActionBar.DISPLAY_SHOW_TITLE);
        // set sub-title
        //actionBar.setSubtitle(R.string.app_sub_name);

        WebView contentView = (WebView) findViewById(R.id.aboutUsWebView);

        String content  = getResources().getString(R.string.html_about_us);
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

    public void onCloseClick(View view) {
        ((Activity)view.getContext()).finish();
    }
}
