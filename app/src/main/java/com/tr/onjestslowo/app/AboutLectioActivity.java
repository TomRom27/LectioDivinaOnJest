package com.tr.onjestslowo.app;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.webkit.WebView;
import android.widget.Button;


public class AboutLectioActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_lectio);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        // set to display title and subtitle only
        actionBar.setDisplayOptions(android.app.ActionBar.DISPLAY_SHOW_TITLE);
        // set sub-title
        //actionBar.setSubtitle(R.string.app_sub_name);

        WebView contentView = (WebView) findViewById(R.id.aboutLectioWebView);

        String content  = getResources().getString(R.string.html_about_lectio);
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

    public void onCloseClick(View view) {
        ((Activity)view.getContext()).finish();
    }
}
