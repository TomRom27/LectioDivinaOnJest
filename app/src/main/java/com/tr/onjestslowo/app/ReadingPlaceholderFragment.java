package com.tr.onjestslowo.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.Fragment;

import com.tr.tools.DateHelper;
import com.tr.tools.UIHelper;

import java.util.Date;

/**
 * Created by bpl2111 on 2016-03-29.
 * code to support fragment's zoom logic
 */
public class ReadingPlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_READING_TITLE = "fragment_reading_title";
    private static final String ARG_READING_DATE = "fragment_reading_date";
    private static final String ARG_READING_CONTENT = "fragment_reading_content";
    private static final String ARG_READING_ZOOM = "fragment_reading_zoom";

    private static final String SAVED_READING_ZOOM = "fragment_saved_reading_zoom";

    int mZoom;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ReadingPlaceholderFragment newInstance(String title, Date date, String content, int zoom) {

        ReadingPlaceholderFragment fragment = new ReadingPlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_READING_TITLE, title);
        args.putString(ARG_READING_DATE, DateHelper.toInternalString(date));
        args.putString(ARG_READING_CONTENT, content);
        args.putInt(ARG_READING_ZOOM, zoom);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_readings, container, false);

        TextView titleView = (TextView) rootView.findViewById(R.id.titleView);
        WebView contentView = (WebView) rootView.findViewById(R.id.readingWebView);

        String title;
        String content;
        int zoom;

        Bundle args = this.getArguments();
        if (args != null) {
            title = this.getArguments().getString(ARG_READING_TITLE);
            content = this.getArguments().getString(ARG_READING_CONTENT);
            zoom = this.getArguments().getInt(ARG_READING_ZOOM, 100);
        } else {
            title = rootView.getContext().getResources().getString(R.string.text_no_readings);
            content = rootView.getContext().getResources().getString(R.string.html_empty_reading_content);
            zoom = 100; // default zoom = 100%
        }
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(SAVED_READING_ZOOM)))
            zoom = savedInstanceState.getInt(SAVED_READING_ZOOM);

        titleView.setText(title);

        setWebViewZoom(rootView, zoom);

        // we must use some trick in order to display content in wanted (not black" color
//        String color = "white";
//        if (rootView.getContext() != null) {
//            // here we retrieve a color, defined in the app them by custom attr. webView_textColor
//            Context context = rootView.getContext();
//            TypedValue typedValue = new TypedValue();
//
//            Resources.Theme theme = context.getTheme();
//            theme.resolveAttribute(R.attr.webView_textColor, typedValue, true);
//            int webviewTextColor = typedValue.data;
//
//            //now convert the int color to hex string
//            color = String.format("#%06X", (0xFFFFFF & webviewTextColor));
//        }
//        String coloredContent = "<font color=\"" +
//                color +
//                "\">" + content + "</font>";

        String coloredContent = UIHelper.setThemeColorForHtml(rootView, R.attr.webView_textColor, content);
        // we use loadDataWithBaseURL rather then loadData as the latter doesn't display
        // national characters correctly
        contentView.loadDataWithBaseURL(null, coloredContent, "text/html", "UTF-8", null);

        // the below makes the web view transparent !!!
        contentView.setBackgroundColor(0x00000000);

        return rootView;
    }

    public void onSaveInstanceState(Bundle args) {
        args.putInt(SAVED_READING_ZOOM, mZoom);
    }

    public void setWebViewZoom(int textZoom) {
        View view = getView();

        if (view != null)
            setWebViewZoom(view, textZoom);
    }

    public void setWebViewZoom(View view, int textZoom) {
        WebView contentView = (WebView) view.findViewById(R.id.readingWebView);

        if (contentView != null)
            contentView.getSettings().setTextZoom(textZoom);
        mZoom = textZoom;
    }

}