package com.madfree.popularmovies;


import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.madfree.popularmovies.adapter.TrailerAdapter;
import com.madfree.popularmovies.data.MovieContract;
import com.madfree.popularmovies.model.Trailer;
import com.madfree.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static com.madfree.popularmovies.data.MovieContract.MovieEntry.*;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = DetailActivity.class.getName();

    private TextView mMovieTitle;
    private TextView mReleaseDate;
    private TextView mRating;
    private TextView mDescription;
    private ImageView mMoviePoster;
    private CheckBox mSaveButton;
    private ListView mTrailerListView;
    private TextView mTrailerListViewTitle;

    private String movieId;
    private String movieTitle;
    private String releaseDate;
    private String userRating;
    private String description;
    private String posterUrl;
    private ArrayList<Trailer> trailerList;

    private ShareActionProvider mShareActionProvider;

    private boolean favorite;

    private static final int ID_MOVIE_LOADER = 11;

    // creates the movie details screen with data from the intent and loads the movie poster
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // UI elements of DetailActivity
        mMovieTitle = findViewById(R.id.tv_title);
        mReleaseDate = findViewById(R.id.tv_release_date);
        mRating = findViewById(R.id.tv_rating);
        mDescription = findViewById(R.id.tv_description);
        mMoviePoster = findViewById(R.id.iv_detail_movie_poster);
        mTrailerListView = findViewById(R.id.listview_trailer);
        TextView mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);
        mSaveButton = findViewById(R.id.save_button);
        Button mReviewButton = findViewById(R.id.btn_reviews);
        mTrailerListViewTitle = findViewById(R.id.movie_trailerlist_title);

        // The intent from MainActivity gets read
        Intent intent = getIntent();
        //Log.v(TAG, "This is the intent received:" + intent);

        if (savedInstanceState != null) {
            movieId = savedInstanceState.getString("movieId");
            movieTitle = savedInstanceState.getString("originalTitle");
            releaseDate = savedInstanceState.getString("releaseDate");
            userRating = savedInstanceState.getString("userRating");
            description = savedInstanceState.getString("description");
            posterUrl = savedInstanceState.getString("moviePosterUrl");
            setData();
        } else {
            if (intent != null) {
                movieId = intent.getStringExtra("movieId");
                Log.d(TAG, "This is the intent: " + movieId);
                // if  movieId is found in favorites, use the CursorLoader to load data from DB
                favorite = isFavourite(movieId);
                if (favorite) {
                    Log.v(TAG, "Using loader to get data");
                    getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
                    mSaveButton.setChecked(true);
                } else {
                    // if movieId is not found in favorites, take the data from the intent
                    Log.v(TAG, "Using the data from the intent");
                    movieTitle = intent.getStringExtra("originalTitle");
                    releaseDate = intent.getStringExtra("releaseDate");
                    userRating = intent.getStringExtra("userRating");
                    description = intent.getStringExtra("description");
                    posterUrl = intent.getStringExtra("moviePosterUrl");
                    setData();
                    mSaveButton.setChecked(false);
                }
            } else {
                mErrorMessageDisplay.isShown();
            }
        }
        // when ReviewButton is clicked start ReviewActivity
        mReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(DetailActivity.this, ReviewActivity.class);
                startIntent.putExtra("movieId", movieId);
                startActivity(startIntent);
            }
        });
        // Fetch Trailer Data
        new FetchTrailerData().execute(movieId);
    }

    private void setData() {
        mMovieTitle.setText(movieTitle);
        String releaseDateString = getString(R.string.release_date) + " " + releaseDate;
        mReleaseDate.setText(releaseDateString);
        String userRatingString = getString(R.string.user_rating) + " " + userRating;
        mRating.setText(userRatingString);
        mDescription.setText(description);
        Log.d(TAG, "This is the movieId: " + movieId);

        Picasso.get()
                .load(NetworkUtils.buildThumbString(posterUrl))
                .resize(400, 550)
                .centerCrop()
                .into(mMoviePoster);
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
        if (cursor != null && cursor.moveToFirst()) {
            movieTitle = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_NAME));
            releaseDate = cursor.getString(cursor.getColumnIndex(COLUMN_RELEASE_DATE));
            userRating = cursor.getString(cursor.getColumnIndex(COLUMN_RATING));
            description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
            posterUrl = cursor.getString(cursor.getColumnIndex(COLUMN_MOVIE_POSTER_URL));
        }
        setData();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void toggleFavorite(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                if (!favorite) {
                    saveFavourite(movieId, movieTitle, releaseDate, userRating, description, posterUrl);
                    mSaveButton.setChecked(true);
                } else {
                    removeFavourite(movieId);
                    mSaveButton.setChecked(false);
                }
        }
    }

    private boolean isFavourite(String movieId){
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

    private boolean saveFavourite(String movieId, String title, String releaseDate, String
            userRating, String description, String posterUrl){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_NAME, title);
        contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
        contentValues.put(MovieContract.MovieEntry.COLUMN_RATING, userRating);
        contentValues.put(MovieContract.MovieEntry.COLUMN_DESCRIPTION, description);
        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_URL, posterUrl);

        if (getContentResolver().insert(CONTENT_URI,contentValues) != null) {
            Toast.makeText(getApplicationContext(), "Add movie as favourite!", Toast.LENGTH_SHORT).show();
            favorite = isFavourite(movieId);
            mSaveButton.setChecked(favorite);
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Add Error!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean removeFavourite(String movieId){
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
        outState.putString("movieId", movieId);
        outState.putString("originalTitle", movieTitle);
        outState.putString("releaseDate", releaseDate);
        outState.putString("userRating", userRating);
        outState.putString("description", description);
        outState.putString("moviePosterUrl", posterUrl);
    }

    private void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int numberOflistItems = listAdapter.getCount();
            int totalItemsHeight = 0;
            int totalWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
            for (int itemNum = 0; itemNum < numberOflistItems; itemNum++) {
                View listItem = listAdapter.getView(itemNum, null, listView);
                listItem.measure(totalWidth, View.MeasureSpec.UNSPECIFIED);
                totalItemsHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + (listView.getDividerHeight() * listAdapter.getCount() - 1);
            listView.requestLayout();
        }
    }

    private class FetchTrailerData extends AsyncTask<String, Void, ArrayList<Trailer>> {
        @Override
        protected ArrayList<Trailer> doInBackground(String... params) {
            ArrayList<Trailer> newTrailers = new ArrayList<>();
            try {
                URL trailerUrl = NetworkUtils.buildTrailerUrl(params[0]);
                String jsonResponse = NetworkUtils.getResponseFromHttpUrl(trailerUrl);
                try {
                    JSONObject movieDataObject = new JSONObject(jsonResponse);
                    // Getting JSON Array node
                    JSONArray trailerArray = movieDataObject.getJSONArray("results");
                    Log.d(TAG, "This is the Trailer JsonArray: " + trailerArray);
                    newTrailers = Trailer.fromJson(trailerArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return newTrailers;
        }

        @Override
        protected void onPostExecute(final ArrayList<Trailer> trailers) {
            super.onPostExecute(trailers);
            trailerList = trailers;
            if (trailerList.size() < 1) {
                mTrailerListViewTitle.setVisibility(View.INVISIBLE);
                mTrailerListView.setVisibility(View.INVISIBLE);
            } else {
                TrailerAdapter trailerAdapter = new TrailerAdapter(DetailActivity.this, trailerList);
                mTrailerListView.setAdapter(trailerAdapter);
                setListViewHeight(mTrailerListView);

                Intent shareMovieIntent = new Intent(Intent.ACTION_SEND);
                shareMovieIntent.setType("text/plain");
                shareMovieIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        "Hey, we should watch this movie: "
                                + movieTitle + " "
                                + NetworkUtils.youtubeUrlString(trailerList.get(0).link));
                Log.d(TAG, "This is the shareMovieIntent:" + shareMovieIntent);
                if (mShareActionProvider != null ) {
                    mShareActionProvider.setShareIntent(shareMovieIntent);
                } else {
                    Log.d(TAG, "Kein ShareActionProvider vorhanden!");
                }

                mTrailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                        String trailerKey = trailerList.get(position).link;
                        String youtubeUrlLink = NetworkUtils.youtubeUrlString(trailerKey);
                        Intent startIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrlLink));
                        startActivity(startIntent);
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.share_menu, menu);
        // Locate MenuItem with ShareActionProvider
        final MenuItem shareItem = menu.findItem(R.id.menu_item_share);
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        // Return true to display menu
        return true;
    }
}
