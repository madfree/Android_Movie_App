package com.madfree.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.madfree.popularmovies.adapter.MovieAdapter;
import com.madfree.popularmovies.data.MovieContract;
import com.madfree.popularmovies.utils.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ArrayList<HashMap<String, String>>> {

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private static final String PREF_MOVIE_LIST = "MyMovieList";
    private static final String KEY_POPULAR = "popular";
    private static final String KEY_RATING = "top_rated";
    private static final String KEY_FAVORITE = "favorite";

    private SharedPreferences sharedPref;

    private static final int MOVIE_LOADER_ID = 0;
    private static final String BUNDLE_RECYCLER_LAYOUT = "recyler_layout";
    private Parcelable savedRecyclerLayoutState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rv_movies);
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        final int numColumns = getResources().getInteger(R.integer.gallery_columns);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, numColumns);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mMovieAdapter = new MovieAdapter();
        mRecyclerView.setAdapter(mMovieAdapter);
        sharedPref = getSharedPreferences(PREF_MOVIE_LIST, MODE_PRIVATE);
        LoaderManager.LoaderCallbacks<ArrayList<HashMap<String, String>>> callback = MainActivity
                .this;
        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, callback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    // shows the movie data
    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // shows an error message (in case there is no data to display, see below)
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public Loader<ArrayList<HashMap<String, String>>> onCreateLoader(int loaderId, @Nullable
            Bundle args) {

        return new AsyncTaskLoader<ArrayList<HashMap<String, String>>>(this) {

            ArrayList<HashMap<String, String>> movieDbData = null;

            @Override
            protected void onStartLoading() {
                if (movieDbData != null) {
                    deliverResult(movieDbData);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public ArrayList<HashMap<String, String>> loadInBackground() {

                String prefMovieList = sharedPref.getString(PREF_MOVIE_LIST, KEY_POPULAR);

                ArrayList<HashMap<String, String>> parsedMovieData = new ArrayList<>();
                if (prefMovieList.equals(KEY_FAVORITE)) {

                    Cursor cursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                    if ((cursor != null) && (cursor.getCount() > 0)) {
                        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                            String movieId = cursor.getString(cursor.getColumnIndex(MovieContract
                                    .MovieEntry.COLUMN_MOVIE_ID));
                            String originalTitle = cursor.getString(cursor.getColumnIndex
                                    (MovieContract.MovieEntry.COLUMN_MOVIE_NAME));
                            String releaseDate = cursor.getString(cursor.getColumnIndex
                                    (MovieContract.MovieEntry.COLUMN_RELEASE_DATE));
                            String userRating = cursor.getString(cursor.getColumnIndex
                                    (MovieContract.MovieEntry.COLUMN_RATING));
                            String description = cursor.getString(cursor.getColumnIndex
                                    (MovieContract.MovieEntry.COLUMN_DESCRIPTION));
                            String moviePosterUrl = cursor.getString(cursor.getColumnIndex
                                    (MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_URL));

                            HashMap<String, String> movieDetail = new HashMap<>();
                            movieDetail.put("id", movieId);
                            movieDetail.put("originalTitle", originalTitle);
                            movieDetail.put("releaseDate", releaseDate);
                            movieDetail.put("userRating", userRating);
                            movieDetail.put("description", description);
                            movieDetail.put("moviePosterUrl", moviePosterUrl);
                            parsedMovieData.add(movieDetail);
                        }
                        cursor.close();
                    } else {
                        return null;
                    }
                } else {
                    try {
                        URL movieDbUrl = NetworkUtils.buildMovieUrl(prefMovieList);
                        String jsonResponse = NetworkUtils.getResponseFromHttpUrl(movieDbUrl);
                        parsedMovieData = NetworkUtils.parseJsonData(jsonResponse);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                return parsedMovieData;
            }

            public void deliverResult(ArrayList<HashMap<String, String>> data) {
                movieDbData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<HashMap<String, String>>> loader,
                               ArrayList<HashMap<String, String>> data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (null == data) {
            showErrorMessage();
        } else {
            showMovieDataView();
            mMovieAdapter.setMovieData(data);
            if (savedRecyclerLayoutState != null) {
                mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<HashMap<String, String>>> loader) {

    }

    private void invalidateData() {
        mMovieAdapter.setMovieData(null);
    }

    // the options menu to select which movie list to load and save in sharedPreferences
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = sharedPref.edit();
        int id = item.getItemId();

        if (id == R.id.most_popular_movies) {
            invalidateData();
            editor.putString(PREF_MOVIE_LIST, KEY_POPULAR);
            editor.apply();
            getSupportLoaderManager().restartLoader(id, null, this);
        }
        if (id == R.id.best_rated_movies) {
            invalidateData();
            editor.putString(PREF_MOVIE_LIST, KEY_RATING);
            editor.apply();
            getSupportLoaderManager().restartLoader(id, null, this);
        }
        if (id == R.id.favorite_movies) {
            invalidateData();
            editor.putString(PREF_MOVIE_LIST, KEY_FAVORITE);
            editor.apply();
            getSupportLoaderManager().restartLoader(id, null, this);
        }
        return super.onOptionsItemSelected(item);
    }

    // Solution dereived from here:
    // https://stackoverflow.com/questions/27816217/how-to-save-recyclerviews-scroll-position
    // -using-recyclerview-state
    // also: https://stackoverflow.com/questions/14462456/returning-from-an-activity-using
    // -navigateupfromsametask/16147110#16147110
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager()
                .onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        }
    }
}