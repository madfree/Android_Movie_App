package com.madfree.popularmovies;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.madfree.popularmovies.data.MovieContract;
import com.madfree.popularmovies.data.MovieProvider;
import com.madfree.popularmovies.helper.NetworkUtils;
import com.squareup.picasso.Picasso;

import static com.madfree.popularmovies.data.MovieContract.MovieEntry.COLUMN_DESCRIPTION;
import static com.madfree.popularmovies.data.MovieContract.MovieEntry.COLUMN_MOVIE_NAME;
import static com.madfree.popularmovies.data.MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_URL;
import static com.madfree.popularmovies.data.MovieContract.MovieEntry.COLUMN_RATING;
import static com.madfree.popularmovies.data.MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
import static com.madfree.popularmovies.data.MovieContract.MovieEntry.CONTENT_URI;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = DetailActivity.class.getName();

    private TextView mMovieTitle;
    private TextView mReleaseDate;
    private TextView mRating;
    private TextView mDescription;
    private ImageView mMoviePoster;
    private TextView mErrorMessageDisplay;
    private CheckBox mSaveButton;

    private String movieId;
    private String movieTitle;
    private String releaseDate;
    private String userRating;
    private String description;
    private String posterUrl;

    private boolean favorite;

    private static final int ID_MOVIE_LOADER = 11;

    // creates the movie details screen with data from the intent and loads the movie poster
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mMovieTitle = findViewById(R.id.tv_title);
        mReleaseDate = findViewById(R.id.tv_release_date);
        mRating = findViewById(R.id.tv_rating);
        mDescription = findViewById(R.id.tv_description);
        mMoviePoster = findViewById(R.id.iv_detail_movie_poster);

        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);

        mSaveButton = findViewById(R.id.save_button);

        Intent intent = getIntent();
        //Log.v(TAG, "This is the intent received:" + intent);

        if (intent != null) {
            movieId = intent.getStringExtra("movieId");
            favorite = isFavourite(movieId);
            if (favorite) {
                Log.v(TAG, "Using loader to get data");
                getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
                mSaveButton.setChecked(true);
            } else {
                Log.v(TAG, "Using the data from the intent");
                movieTitle = intent.getStringExtra("originalTitle");
                releaseDate = intent.getStringExtra("releaseDate");
                userRating = intent.getStringExtra("userRating");
                description = intent.getStringExtra("description");
                posterUrl = intent.getStringExtra("moviePosterUrl");

                mMovieTitle.setText(movieTitle);
                mReleaseDate.setText(getString(R.string.release_date) + releaseDate);
                mRating.setText(getString(R.string.user_rating) + userRating);
                mDescription.setText(description);
                Log.d(TAG, "This is the movieId: " + movieId);

                Picasso.get()
                        .load(NetworkUtils.buildThumbString(posterUrl))
                        .resize(400, 550)
                        .centerCrop()
                        .into(mMoviePoster);

                mSaveButton.setChecked(false);
            }
        } else {
                mErrorMessageDisplay.isShown();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case ID_MOVIE_LOADER:
                Log.d(TAG, "Using CursorLoader with id: " + loaderId);
                return new CursorLoader(this,
                        CONTENT_URI,
                        null,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{movieId},
                        null);
            default:
                throw new RuntimeException("Loader not implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();

        movieTitle = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_NAME));
        releaseDate = cursor.getString(cursor.getColumnIndex(COLUMN_RELEASE_DATE));
        userRating = cursor.getString(cursor.getColumnIndex(COLUMN_RATING));
        description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
        posterUrl = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_POSTER_URL));

        mMovieTitle.setText(movieTitle);
        mReleaseDate.setText(releaseDate);
        mRating.setText(userRating);
        mDescription.setText(description);

        Picasso.get()
                .load(NetworkUtils.buildThumbString(posterUrl))
                .resize(400, 550)
                .centerCrop()
                .into(mMoviePoster);

        cursor.close();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void onClickAddFav(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                if (!favorite) {
                    saveFavourite(movieId, movieTitle, releaseDate, userRating, description, posterUrl);
                    mSaveButton.setChecked(true);
                    mSaveButton.setText(R.string.markFavorite);
                } else {
                    removeFavourite(movieId);
                    mSaveButton.setChecked(false);
                    mSaveButton.setText(R.string.unmarkFavorite);
                }
        }
        //getSupportLoaderManager().restartLoader(ID_MOVIE_LOADER, null, DetailActivity.this);
    }

    public boolean isFavourite(String movieId){
        Cursor cursor = getContentResolver()
                .query(CONTENT_URI,
                        new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{movieId},null);
        if (cursor != null) {
            boolean isFavourite = cursor.getCount() > 0;
            cursor.close();
            return isFavourite;
        }
        return false;
    }

    public boolean saveFavourite(String movieId, String title, String releaseDate, String userRating, String description, String posterUrl){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_NAME, title);
        contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
        contentValues.put(MovieContract.MovieEntry.COLUMN_RATING, userRating);
        contentValues.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_URL, posterUrl);

        if (getContentResolver().insert(CONTENT_URI,contentValues)
                != null){
            Toast.makeText(getApplicationContext(), "Add movie as favourite!", Toast.LENGTH_SHORT).show();
            favorite = isFavourite(movieId);
            mSaveButton.setChecked(favorite);
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Add Error!", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    public boolean removeFavourite(String movieId){
        Uri uri = CONTENT_URI.buildUpon().appendPath(movieId).build();
        int deletedRows = getContentResolver().delete(uri, null, null);
        if (deletedRows > 0) {
            Toast.makeText(getApplicationContext(),"Remove movie from favourites!", Toast.LENGTH_SHORT).show();
            favorite = isFavourite(movieId);
            mSaveButton.setChecked(favorite);
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Remove error!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
