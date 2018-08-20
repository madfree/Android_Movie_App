package com.madfree.popularmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.madfree.popularmovies.R;
import com.madfree.popularmovies.model.Review;

import java.util.ArrayList;

public class ReviewAdapter extends ArrayAdapter {

    public ReviewAdapter(Context context, ArrayList<Review> reviewList) {
        super(context, 0, reviewList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Review review = (Review) getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_list_item, parent, false);
        }

        // Lookup view for data population
        TextView reviewName = convertView.findViewById(R.id.tv_review_name);
        TextView reviewContent = convertView.findViewById(R.id.tv_review_content);

        // Populate the data into the template view using the data object
        reviewName.setText(review.name);
        reviewContent.setText(review.content);

        // Return the completed view to render on screen
        return convertView;
    }
}
