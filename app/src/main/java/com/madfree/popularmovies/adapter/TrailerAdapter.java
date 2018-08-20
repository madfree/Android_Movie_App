package com.madfree.popularmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.madfree.popularmovies.R;
import com.madfree.popularmovies.model.Trailer;

import java.util.ArrayList;

public class TrailerAdapter extends ArrayAdapter {

    public TrailerAdapter(Context context, ArrayList<Trailer> trailerList) {
        super(context, 0, trailerList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Trailer trailer = (Trailer) getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_list_item, parent, false);
        }

        // Lookup view for data population
        TextView trailerName = convertView.findViewById(R.id.tv_trailer_name);

        // Populate the data into the template view using the data object
        trailerName.setText(trailer.name);

        // Return the completed view to render on screen
        return convertView;
    }
}