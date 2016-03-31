package com.tr.onjestslowo.app;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tr.onjestslowo.app.R;
import com.tr.onjestslowo.model.Reading;
import com.tr.onjestslowo.service.ReadingService;
import com.tr.tools.DateHelper;
import com.tr.tools.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Use the {@link LectioDivinaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LectioDivinaFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_ZOOM = "lectiodivina_zoom";
    private static final String ARG_READINGS = "lectiodivina_readings";
    private static final String ARG_SELECTED_DATE = "SelectedDate";

    private static String LOG_TAG = "LectioDivinaFragment";

    ReadingsPagerAdapter mReadingsPagerAdapter;
    ViewPager mViewPager;
    Date mSelectedDate;
    ArrayList<Reading> mLoadedReadings;
    int mZoom;
    ReadingService mReadingService;

    private OnLectioDivinaFragmentListener mListener;

    public LectioDivinaFragment() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment LectioDivinaFragment.
     *
     */
    public static LectioDivinaFragment newInstance() {
        LectioDivinaFragment fragment = new LectioDivinaFragment();



        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLoadedReadings = getArguments().getParcelableArrayList(ARG_READINGS);
            mSelectedDate = DateHelper.fromInternalString(getArguments().getString(ARG_SELECTED_DATE));
            mZoom = getArguments().getInt(ARG_ZOOM);
        }
        else
        {
            mSelectedDate = DateHelper.getToday();
            mZoom = getInitialZoom();
            // mLoadedReadings ???
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_lectio_divina, container, false);

        // Create the adapter that will return a fragment for each of the loaded reading
        mReadingsPagerAdapter = new ReadingsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.readingPager);

        // load from db and display all readings - async
        new LoadAndDisplayReadingsTask().execute();

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle args) {
        // todo
        args.putParcelableArrayList(ARG_READINGS, mLoadedReadings);
        args.putString(ARG_SELECTED_DATE, DateHelper.toInternalString(mSelectedDate));
        args.putInt(ARG_ZOOM, mZoom);
    }

    // todo onRestoreInstanceState
//    @Override
//    public void onRestoreInstanceState(Bundle args) {
//
//        // todo
//        // Restore UI state from the savedInstanceState.
//        // This bundle has also been passed to onCreate.
//
////        if (savedInstanceState.containsKey(ARG_SELECTED_DATE))
////            mSelectedDate = DateHelper.fromInternalString(savedInstanceState.getString(ARG_SELECTED_DATE));
////        else
////            mSelectedDate = DateHelper.getToday();
////
////        if (savedInstanceState.containsKey(ARG_READING_ZOOM))
////            mZoom = savedInstanceState.getInt(ARG_READING_ZOOM);
////        else
//            mZoom = getInitialZoom();
//    }

    private int getInitialZoom() {
        return 100; // todo??? to get from Preferences
    }
    private ReadingService getReadingService() {
        if (mListener != null) {
            return mListener.onGetReadingService();
        }
        else
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
        getView().findViewById(R.id.emptyReadingContainer).setVisibility(View.VISIBLE);
    }

    private void showReadingPager() {
        getView().findViewById(R.id.emptyReadingContainer).setVisibility(View.GONE);
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
