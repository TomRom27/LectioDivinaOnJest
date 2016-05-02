package com.tr.onjestslowo.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tr.onjestslowo.model.ShortContemplationsFile;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by bpl2111 on 2016-05-02.
 */
public class ShortContemplationsAdapter extends ArrayAdapter<ShortContemplationsFile> {

    Context mContext;
    ArrayList<ShortContemplationsFile> mObjectList;
    //int mLayoutId;

    public ShortContemplationsAdapter(Context context, ArrayList<ShortContemplationsFile> objectList)
    {
        super(context,0);
        mContext=context;
        mObjectList = objectList;
        //mLayoutId = layoutId;
    }

    public int getCount()
    {
        // return the number of records in cursor
        return mObjectList.size();
    }

    // getView method is called for each item of ListView
    public View getView(int position, View view, ViewGroup parent)
    {
        // inflate the layout for each item of listView
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.list_app_color, null);

        // move the cursor to required position
        ShortContemplationsFile fileObject = mObjectList.get(position);

        // get the reference of textViews
        TextView titleView=(TextView)view.findViewById(R.id.list_item_title);
        TextView subtitleView=(TextView)view.findViewById(R.id.list_item_subtitle);

        // Set the Sender number and smsBody to respective TextViews
        titleView.setText(getStartEndDateString(fileObject));
        subtitleView.setText(getStartEndDateString(fileObject));


        return view;
    }

    private String getStartEndDateString(ShortContemplationsFile fileObject) {
        return fileObject.FileName; // todo
    }

    private String getStartDateString(ShortContemplationsFile fileObject) {
        return fileObject.FileName; // todo
    }

    public ShortContemplationsFile getItem(int position) {

        return mObjectList.get(position);
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
}