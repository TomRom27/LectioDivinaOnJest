package com.tr.onjestslowo.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.tr.onjestslowo.model.OnJestPreferences;
import com.tr.onjestslowo.model.ShortContemplationsFile;
import com.tr.onjestslowo.service.PdfViewer;
import com.tr.onjestslowo.service.ReadingService;
import com.tr.tools.DateHelper;
import com.tr.tools.Logger;
import com.tr.tools.UIHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShortContemplationsFragment.OnShortContempationsListener} interface
 * to handle interaction events.
 * Use the {@link ShortContemplationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShortContemplationsFragment extends Fragment
        implements AdapterView.OnItemClickListener {

    private static String LOG_TAG = "ShortContemplationsFragment";
    private OnShortContempationsListener mListener;

    public ShortContemplationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShortContemplationsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShortContemplationsFragment newInstance(String param1, String param2) {
        ShortContemplationsFragment fragment = new ShortContemplationsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_short_contemplations, container, false);

        Activity activity = getActivity();
        if (activity != null)
            populateContent(rootView, activity);

        return rootView;
    }

    private void populateContent(View rootView, Context context) {
        View noPdf = rootView.findViewById(R.id.noPdfView);
        ListView pdfList = (ListView) rootView.findViewById(R.id.shortContemplationsList);

        if (PdfViewer.getInstance((Activity) context).canDisplayPdf()) {
            noPdf.setVisibility(View.GONE);
            pdfList.setVisibility(View.VISIBLE);
            loadFilenamesAndShow(pdfList, (Activity) context);
        } else {
            noPdf.setVisibility(View.VISIBLE);
            pdfList.setVisibility(View.GONE);
        }
    }

    private void loadFilenamesAndShow(ListView pdfListView, Activity activity) {
        OnJestPreferences prefs = AppPreferences.getInstance(activity).get();

        ArrayList<ShortContemplationsFile> files = mListener.onGetReadingService().getShortContemplationsList(prefs.ShortContemplationDownloadPath);

        // sort
        Collections.sort(files, new Comparator<ShortContemplationsFile>() {
            @Override
            public int compare(ShortContemplationsFile o1, ShortContemplationsFile o2) {
                return DateHelper.compareDates(o1.FirstDate,o2.FirstDate);
            }
        });

        ShortContemplationsAdapter adapter = new ShortContemplationsAdapter(activity, files);

        pdfListView.setOnItemClickListener(this);

        pdfListView.setAdapter(adapter);
    }

    public void onItemClick(AdapterView<?> listView, View v, int position, long arg3) {
        ShortContemplationsFile fileObject = (ShortContemplationsFile) listView.getAdapter().getItem(position);
        displayPdf(fileObject);
    }

    private void displayPdf(ShortContemplationsFile fileObject) {
        try {

            PdfViewer.getInstance(getActivity()).showFileIfExists(fileObject.FilePath);
        }
        catch (Exception ex) {
            Logger.debug(LOG_TAG, "Failed to show PDS: "+ex.getMessage());
            UIHelper.showToast(getActivity(), R.string.pdf_failed, Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnShortContempationsListener) {
            mListener = (OnShortContempationsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnShortContempationsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnShortContempationsListener {
        ReadingService onGetReadingService();
    }
}
