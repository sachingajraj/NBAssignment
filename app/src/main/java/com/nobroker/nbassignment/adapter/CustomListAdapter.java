package com.nobroker.nbassignment.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.nobroker.nbassignment.R;
import com.nobroker.nbassignment.app.AppController;
import com.nobroker.nbassignment.model.PropertyListItem;

import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<PropertyListItem> propertyItems;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public CustomListAdapter(Activity activity, List<PropertyListItem> propertyItems) {
        this.activity = activity;
        this.propertyItems = propertyItems;
    }



    @Override
    public int getCount() {
        return propertyItems.size();
    }

    @Override
    public Object getItem(int location) {
        return propertyItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.thumbnail);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView propertyRent = (TextView) convertView.findViewById(R.id.propertyRent);
        TextView propertySize = (TextView) convertView.findViewById(R.id.propertySize);

        // getting movie data for the row
        PropertyListItem m = propertyItems.get(position);

        // thumbnail image
        thumbNail.setImageUrl(m.getThumbnailUrl(), imageLoader);

        // title
        title.setText(m.getTitle());

        // rating
        propertyRent.setText("INR " + String.valueOf(m.getRent()));

        // release year
        propertySize.setText(String.valueOf(m.getSize()) + "SQ. FT.");

        return convertView;
    }

}