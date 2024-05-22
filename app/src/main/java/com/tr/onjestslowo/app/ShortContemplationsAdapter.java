package com.tr.onjestslowo.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.appcompat.view.ContextThemeWrapper;

import com.tr.onjestslowo.model.ShortContemplationsFile;
import com.tr.onjestslowo.service.ShortContemplationDataSource;
import com.tr.tools.DateHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ShortContemplationsAdapter extends ArrayAdapter<ShortContemplationsFile> {

    Context mContext;
    ArrayList<ShortContemplationsFile> mObjectList;
    ShortContemplationDataSource mShortContemplationDS;

    public ShortContemplationsAdapter(Context context, ArrayList<ShortContemplationsFile> objectList)
    {
        super(context,0);
        mContext=context;
        mObjectList = objectList;
        mShortContemplationDS = new ShortContemplationDataSource(context);
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
        view = inflater.inflate(R.layout.short_cont_list_item, null);

        // move the cursor to required position
        ShortContemplationsFile fileObject = mObjectList.get(position);

        // get the reference of textViews
        TextView titleView=(TextView)view.findViewById(R.id.list_item_title);
        TextView subtitleView=(TextView)view.findViewById(R.id.list_item_subtitle);
        TextView isCurrentView=(TextView)view.findViewById(R.id.list_item_is_current);

        titleView.setText(getStartEndDateString(fileObject));
        subtitleView.setText(getStartDateString(fileObject));
        if (DateHelper.isInRange(DateHelper.getToday(), fileObject.FirstDate, fileObject.LastDate))
            isCurrentView.setVisibility(View.VISIBLE);
        else
            isCurrentView.setVisibility(View.GONE);

        Button menuButton = view.findViewById(R.id.menu_button);

        menuButton.setOnClickListener(v ->  showPopupMenu(menuButton, fileObject));

        return view;
    }

    private void showPopupMenu(View view, ShortContemplationsFile fileObject) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(mContext, R.style.ActionBarMenu);
        // see we use wrapper, not the mContext - in order to style the menu
        PopupMenu popupMenu = new PopupMenu(wrapper, view);

        popupMenu.getMenuInflater().inflate(R.menu.short_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_short) {
                try {
                    mShortContemplationDS.delete(fileObject.FileName);
                    mObjectList.remove(fileObject);
                } catch (IOException ignore) {
                }
                notifyDataSetChanged();
            }
            return true;
        });
        popupMenu.show();
    }
    private String getStartEndDateString(ShortContemplationsFile fileObject) {

        return "Rozważania "+DateHelper.periodToShortestString(fileObject.FirstDate, fileObject.LastDate);
    }

    private String getStartDateString(ShortContemplationsFile fileObject) {
        // Sunday, 24 April 2016
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy");

        return dateFormat.format(fileObject.FirstDate);
    }

    public ShortContemplationsFile getItem(int position) {

        return mObjectList.get(position);
    }

    public long getItemId(int position) {
        // Auto-generated method stub
        return position;
    }
}