package com.madfree.popularmovies;

import android.content.SharedPreferences;
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

import com.madfree.popularmovies.Helper.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<ArrayList<HashMap<String, String>>> {

    private final String TAG = MainActivity.class.getName();

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private static final String PREF_MOVIE_LIST = "MyMovieList";
    private static final String KEY_POPULAR = "popular";
    private static final String KEY_RATING = "top_rated";

    private SharedPreferences sharedPref;

    private static final int MOVIE_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.rv_movies);

        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mMovieAdapter = new MovieAdapter();
        mRecyclerView.setAdapter(mMovieAdapter);

        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        sharedPref = getSharedPreferences(PREF_MOVIE_LIST, MODE_PRIVATE);

        LoaderManager.LoaderCallbacks<ArrayList<HashMap<String, String>>> callback = MainActivity.this;

        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, callback);
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
    public Loader<ArrayList<HashMap<String, String>>> onCreateLoader(int id, @Nullable Bundle args) {

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
                URL movieDbUrl = NetworkUtils.buildUrl(prefMovieList);

                try {
                    String jsonResponse = NetworkUtils.getResponseFromHttpUrl(movieDbUrl);
                    //Log.e(TAG, "Response from url" + jsonResponse);
                    ArrayList<HashMap<String, String>> parsedMovieData = NetworkUtils.parseJsonData(jsonResponse);
                    return parsedMovieData;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
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
        return super.onOptionsItemSelected(item);
    }
}