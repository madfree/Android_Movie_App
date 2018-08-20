package com.madfree.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.madfree.popularmovies.adapter.ReviewAdapter;
import com.madfree.popularmovies.model.Review;
import com.madfree.popularmovies.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    private ListView mReviews;
    private TextView mErrorMessageDisplay;

    private ArrayList<Review> newReviews;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mReviews = findViewById(R.id.listview_review);
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);

        Intent intent = getIntent();
        if (intent != null) {
            String movieId = intent.getStringExtra("movieId");
            new FetchReviews().execute(movieId);
        } else {
            mErrorMessageDisplay.setText(View.VISIBLE);
        }
    }

    class FetchReviews extends AsyncTask<String, Void, ArrayList<Review>> {

        @Override
        protected ArrayList<Review> doInBackground(String... strings) {
            newReviews = new ArrayList<>();
            try {
                URL reviewUrl = NetworkUtils.buildReviewUrl(strings[0]);
                String jsonResponse = NetworkUtils.getResponseFromHttpUrl(reviewUrl);
                try {
                    JSONObject movieDataObject = new JSONObject(jsonResponse);
                    // Getting JSON Array node
                    JSONArray reviewArray = movieDataObject.getJSONArray("results");
                    ReviewActivity.this.newReviews = Review.fromJson(reviewArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newReviews;
        }

        @Override
        protected void onPostExecute(final ArrayList<Review> reviews) {
            super.onPostExecute(reviews);
            if (reviews.size() < 1) {
                mReviews.setVisibility(View.INVISIBLE);
                mErrorMessageDisplay.setVisibility(View.VISIBLE);
            } else {
                ReviewAdapter reviewAdapter = new ReviewAdapter(ReviewActivity.this, reviews);
                mReviews.setAdapter(reviewAdapter);
            }
        }
    }

}
