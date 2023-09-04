package com.tr.onjestslowo.app;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.tr.onjestslowo.model.Reading;
import com.tr.onjestslowo.service.ReadingService;
import com.tr.tools.DateHelper;
import com.tr.tools.Logger;

import java.util.ArrayList;
import java.util.Date;

/**
 * Use the {@link LectioDivinaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LectioDivinaFragment extends Fragment {
    private static final String ARG_ZOOM = "lectiodivina_zoom";
    private static final String ARG_ZOOM_VISIBLE = "lectiodivina_zoom_visible";
    private static final String ARG_READINGS = "lectiodivina_readings";
    private static final String ARG_SELECTED_DATE = "lectiodivina_selectedDate";
    private static final int ZOOM_STEP_PERCENT = 10;

    private static String LOG_TAG = "LectioDivinaFragment";

    ReadingsPagerAdapter mReadingsPagerAdapter;
    ViewPager mViewPager;
    View mEmptyReadingContainer;
    Date mSelectedDate;
    ArrayList<Reading> mLoadedReadings;
    int mZoom;
    boolean mZoomVisible;
    ReadingService mReadingService;

    private OnLectioDivinaFragmentListener mListener;

    public LectioDivinaFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment LectioDivinaFragment.
     */
    public static LectioDivinaFragment newInstance(boolean isZoomVivisble) {
        LectioDivinaFragment fragment = new LectioDivinaFragment();

        Bundle args = new Bundle();
        args.putBoolean(ARG_ZOOM_VISIBLE, isZoomVivisble);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLoadedReadings = savedInstanceState.getParcelableArrayList(ARG_READINGS);
            mSelectedDate = DateHelper.fromInternalString(savedInstanceState.getString(ARG_SELECTED_DATE));
            mZoom = savedInstanceState.getInt(ARG_ZOOM);
            mZoomVisible = savedInstanceState.getBoolean(ARG_ZOOM_VISIBLE);
        } else {
            mSelectedDate = DateHelper.getToday();
            mZoom = getInitialZoom();
            mLoadedReadings = null;
            if (getArguments() != null)
                mZoomVisible = getArguments().getBoolean(ARG_ZOOM_VISIBLE);
            else
                mZoomVisible = true;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_lectio_divina, container, false);

        // Create the adapter that will return a fragment for each of the loaded reading
        mReadingsPagerAdapter = new ReadingsPagerAdapter(getChildFragmentManager());

        mEmptyReadingContainer = rootView.findViewById(R.id.emptyReadingContainer);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.readingPager);

        mReadingService = mListener.onGetReadingService();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                getSelectedDateByReadingIndex(position);
            }

            @Override
            public void onPageSelected(int position) {
                getSelectedDateByReadingIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // assign handlers for zooming buttons
        // zoom in
        LinearLayout zoomButton;
        zoomButton = (LinearLayout) rootView.findViewById(R.id.button_zoomIn);
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseWebViewZoom(rootView, ZOOM_STEP_PERCENT);

            }
        });
        // zoom out
        zoomButton = (LinearLayout) rootView.findViewById(R.id.button_zoomOut);
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseWebViewZoom(rootView, -ZOOM_STEP_PERCENT);
            }
        });

        showZoomForView(rootView);
        // load from db and display all readings - async
        if (mLoadedReadings == null)
            new LoadAndDisplayReadingsTask().execute();
        else
            displayReadings(mLoadedReadings);

        return rootView;
    }

    public void showZoom(boolean zoomEnabled) {
        mZoomVisible = zoomEnabled;
        showZoomForView(getView());
    }

    private void showZoomForView(View rootView) {
        View zoomView = rootView.findViewById(R.id.zoom_buttons);
        if (zoomView != null) {
            if (mZoomVisible)
                zoomView.setVisibility(View.VISIBLE);
            else
                zoomView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle args) {
        args.putParcelableArrayList(ARG_READINGS, mLoadedReadings);
        args.putString(ARG_SELECTED_DATE, DateHelper.toInternalString(mSelectedDate));
        args.putInt(ARG_ZOOM, mZoom);
        args.putBoolean(ARG_ZOOM_VISIBLE, mZoomVisible);
    }

    private int getInitialZoom() {
        return 100; // to do??? to get from Preferences
    }

    private void increaseWebViewZoom(View rootView, int percentIncrease) {
        mZoom = mZoom + Math.round(mZoom * percentIncrease / 100);
        if (mZoom < 0)
            mZoom = 1;
        setZoomForChildren();
        Bundle args = getArguments();
        if (args != null)
            args.putInt(ARG_ZOOM, mZoom);
    }

    private ReadingService getReadingService() {
        if (mListener != null) {
            return mListener.onGetReadingService();
        } else
            return null;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLectioDivinaFragmentListener) {
            mListener = (OnLectioDivinaFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLectioDivinaFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void displayReadings(ArrayList<Reading> readings) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_READINGS, readings);
        mLoadedReadings = readings;
        if (mLoadedReadings.size() > 0) {
            showReadingPager();
        } else {
            showEmptyReading();
        }
    }

    private void showEmptyReading() {
        // hide pager
        mViewPager.setVisibility(View.GONE);
        // show empty reading view
        mEmptyReadingContainer.setVisibility(View.VISIBLE);
    }

    private void showReadingPager() {
        mEmptyReadingContainer.setVisibility(View.GONE);
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

    private void getSelectedDateByReadingIndex(int index) {
        if ((index >= 0) && (index <= mLoadedReadings.size() - 1))
            mSelectedDate = mLoadedReadings.get(index).DateParsed;
    }

    private void setZoomForChildren() {
        for (Fragment f : getChildFragmentManager().getFragments()) {
            ReadingPlaceholderFragment readingPlaceholderFragment = (ReadingPlaceholderFragment) f;
            if (readingPlaceholderFragment != null)
                readingPlaceholderFragment.setWebViewZoom(mZoom);
        }
    }

    private class LoadAndDisplayReadingsTask extends AsyncTask<Void, Integer, ArrayList<Reading>> {

        protected ArrayList<Reading> doInBackground(Void... params) {
            ArrayList<Reading> loadedReadings;

            Logger.debug(LOG_TAG, "Loading readings async - started");
            loadedReadings = mReadingService.loadReadings();

            return loadedReadings;
        }

        protected void onPostExecute(ArrayList<Reading> loadedReadings) {
            Logger.debug(LOG_TAG, "Loading readings async - ended, displaying them now");
            displayReadings(loadedReadings);
        }
    }
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
            return ReadingPlaceholderFragment.newInstance(reading.Title, reading.DateParsed, reading.Content, mZoom);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLectioDivinaFragmentListener {
        ReadingService onGetReadingService();
    }
}
