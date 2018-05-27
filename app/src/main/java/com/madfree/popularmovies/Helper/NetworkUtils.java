package com.madfree.popularmovies.Helper;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getName();

    // TODO: Add your API key from themoviedb.org here!
    private final static String apiKey = "YourApiKey";

    private final static String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/";
    private final static String CATEGORY = "movie";
    private final static String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private final static String IMAGE_SIZE = "w500";

    // Built the URL for the API call
    public static URL buildUrl(String movieDbUrlString) {
        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                .appendPath(CATEGORY)
                .appendPath(movieDbUrlString)
                .appendQueryParameter("api_key", apiKey)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "buildUrl: " + url);
        return url;
    }

    // build the URL string for the thumb of the movie poster in the grid
    public static String buildThumbString(String thumbUrlString) {
        StringBuilder builtString = new StringBuilder();
        builtString.append(IMAGE_BASE_URL)
                .append(IMAGE_SIZE)
                .append(thumbUrlString);

        //Log.d(TAG, "buildUrl: " + builtString);
        return builtString.toString();
    }

    // Built the network connection to call the API
    public static String getResponseFromHttpUrl(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    // parse the JSON movie data received into an ArrayList<Hashmap>
    public static ArrayList<HashMap<String,String>> parseJsonData(String jsonResponse) {
        ArrayList<HashMap<String, String>> parsedMovieData = new ArrayList<>();

        if (jsonResponse != null) {
            try {
                JSONObject movieDataObject = new JSONObject(jsonResponse);
                // Getting JSON Array node
                JSONArray movieList = movieDataObject.getJSONArray("results");

                // looping through movies
                for (int i = 0; i < movieList.length(); i++) {

                    JSONObject movieEntry = movieList.getJSONObject(i);
                    String originalTitle = movieEntry.getString("original_title");
                    String moviePosterUrl = movieEntry.getString("poster_path");
                    String description = movieEntry.getString("overview");
                    String userRating = movieEntry.getString("vote_average");
                    String releaseDate = movieEntry.getString("release_date");

                    HashMap<String, String> movieDetail = new HashMap<>();
                    movieDetail.put("originalTitle", originalTitle);
                    movieDetail.put("moviePosterUrl", moviePosterUrl);
                    movieDetail.put("description", description);
                    movieDetail.put("userRating", userRating);
                    movieDetail.put("releaseDate", releaseDate);
                    parsedMovieData.add(movieDetail);
                    //Log.v(TAG, "This is the movieDetail: " + movieDetail);
                }
            } catch (final JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JSON parsing error!");
            }
        }
        return parsedMovieData;
    }

}
