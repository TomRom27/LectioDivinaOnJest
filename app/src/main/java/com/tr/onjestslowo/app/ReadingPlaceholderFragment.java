package com.tr.onjestslowo.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.Fragment;

import com.tr.tools.DateHelper;

import java.util.Date;

/**
 * Created by bpl2111 on 2016-03-29.
 */
public class ReadingPlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_READING_TITLE = "reading_title";
    private static final String ARG_READING_DATE = "reading_date";
    private static final String ARG_READING_CONTENT = "reading_content";
    private static final String ARG_READING_ZOOM = "reading_zoom";
    private static final int ZOOM_STEP_PERCENT = 10;

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
        final View rootView = inflater.inflate(R.layout.fragment_readings, container, false);

        TextView titleView = (TextView) rootView.findViewById(R.id.titleView);
        WebView contentView = (WebView) rootView.findViewById(R.id.readingWebView);

        String title = "";
        String content = "";
        Bundle args = this.getArguments();
        if (args != null) {
            title = this.getArguments().getString(ARG_READING_TITLE);
            //String date = this.getArguments().getString(ARG_READING_DATE);
            content = this.getArguments().getString(ARG_READING_CONTENT);
            setWebViewZoom(rootView, this.getArguments().getInt(ARG_READING_ZOOM,100));
        } else {
            title = rootView.getContext().getResources().getString(R.string.text_no_readings);
            content = rootView.getContext().getResources().getString(R.string.html_empty_reading_content);
            setWebViewZoom(rootView, 100); // default zoom = 100%
        }

        titleView.setText(title);
        // we use loadDataWithBaseURL rather then loadData as the latter doesn't display
        // national characters correctly
        contentView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null);

        // the below makes the web view transparent !!!
        contentView.setBackgroundColor(0x00000000);

        // assign handlers for zooming buttons
        // zoom in
        LinearLayout zoomButton;
        zoomButton = (LinearLayout) rootView.findViewById(R.id.button_zoomIn);
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newZoom = increaseWebViewZoom(rootView, ZOOM_STEP_PERCENT);

            }
        });
        // zoom out
        zoomButton = (LinearLayout) rootView.findViewById(R.id.button_zoomOut);
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newZoom = increaseWebViewZoom(rootView,-ZOOM_STEP_PERCENT);;
            }
        });

        return rootView;
    }

    public static WebView findWebView(View rootView) {
        return (WebView) rootView.findViewById(R.id.readingWebView);
    }
    public static int increaseWebViewZoom(View rootView, int percentIncrease) {
        int textZoom = 100;
        WebView contentView = findWebView(rootView);
        if (contentView != null) {
            WebSettings settings = contentView.getSettings();
            textZoom = settings.getTextZoom();
            textZoom = textZoom +Math.round(textZoom*percentIncrease/100);
            if (textZoom<0)
                textZoom=1;
            settings.setTextZoom(textZoom);
        }
        return textZoom;
    }
    public static void setWebViewZoom(View rootView, int textZoom) {
        WebView contentView = (WebView) rootView.findViewById(R.id.readingWebView);
        contentView.getSettings().setTextZoom(textZoom);
    }
}