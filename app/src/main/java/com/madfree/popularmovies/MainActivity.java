package com.madfree.popularmovies;

import android.content.SharedPreferences;
import android.os.AsyncTask;
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

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getName();

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    private static final String PREF_MOVIE_LIST = "MyMovieList";
    private static final String KEY_POPULAR = "popular";
    private static final String KEY_RATING = "top_rated";

    private SharedPreferences sharedPref;

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

        loadMovieData(sharedPref.getString(PREF_MOVIE_LIST, KEY_POPULAR));

    }

    private void loadMovieData(String selection) {
        showMovieDataView();
        new FetchMovieData().execute(selection);
    }

    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    class FetchMovieData extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

        private final String TAG = FetchMovieData.class.getName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<HashMap<String,String>> doInBackground(String... params) {

            String movieDbData = params[0];
            URL movieDbUrl = NetworkUtils.buildUrl(movieDbData);
            ArrayList<HashMap<String, String>> parsedMovieData = null;

            try {
                String jsonResponse = NetworkUtils.getResponseFromHttpUrl(movieDbUrl);
                //Log.e(TAG, "Response from url" + jsonResponse);
                parsedMovieData = NetworkUtils.parseJsonData(jsonResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return parsedMovieData;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> movieData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieData != null) {
                showMovieDataView();
                mMovieAdapter.setMovieData(movieData);
            } else {
                showErrorMessage();
            }
        }
    }

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
            editor.putString(PREF_MOVIE_LIST, KEY_POPULAR);
            editor.apply();
            loadMovieData(sharedPref.getString(PREF_MOVIE_LIST, null));
        }
        if (id == R.id.best_rated_movies) {
            editor.putString(PREF_MOVIE_LIST, KEY_RATING);
            editor.apply();
            loadMovieData(sharedPref.getString(PREF_MOVIE_LIST, null));
        }
        return super.onOptionsItemSelected(item);
    }
}