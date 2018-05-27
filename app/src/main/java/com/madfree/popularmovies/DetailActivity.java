package com.madfree.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.madfree.popularmovies.Helper.NetworkUtils;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private final String TAG = DetailActivity.class.getName();

    // creates the movie details screen with data from the intent and loads the movie poster
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView mMovieTitle = findViewById(R.id.tv_title);
        TextView mReleaseDate = findViewById(R.id.tv_release_date);
        TextView mRating = findViewById(R.id.tv_rating);
        TextView mDescription = findViewById(R.id.tv_description);
        ImageView mMoviePoster = findViewById(R.id.iv_detail_movie_poster);

        TextView mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);

        Intent intent = getIntent();
        //Log.v(TAG, "This is the intent received:" + intent);

        if (intent != null) {
            String movieTitle = intent.getStringExtra("originalTitle");
            String releaseDate = intent.getStringExtra("releaseDate");
            String userRating = intent.getStringExtra("userRating");
            String description = intent.getStringExtra("description");
            String posterUrl = intent.getStringExtra("moviePosterUrl");

            mMovieTitle.setText(movieTitle);
            mReleaseDate.setText(getString(R.string.release_date) + releaseDate);
            mRating.setText(getString(R.string.user_rating) + userRating);
            mDescription.setText(description);

            Picasso.get()
                    .load(NetworkUtils.buildThumbString(posterUrl))
                    .resize(400, 550)
                    .centerCrop()
                    .into(mMoviePoster);
        } else {
            mErrorMessageDisplay.isShown();
        }
    }
}
