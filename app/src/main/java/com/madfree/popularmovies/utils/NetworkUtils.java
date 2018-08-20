package com.madfree.popularmovies.utils;

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
    private final static String apiKey = "YOUR_API_KEY";

    private final static String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/";
    private final static String CATEGORY = "movie";
    private final static String SUBCATEGORY_TRAILER = "videos";
    private final static String SUBCATEGORY_REVIEW = "reviews";
    private final static String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";
    private final static String IMAGE_SIZE = "w500";
    private static final Uri YOUTUBE_BASE_URL = Uri.parse("https://www.youtube.com/watch");

    // Built the URL for the API call
    public static URL buildMovieUrl(String movieDbUrlString) {
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
        return url;
    }

    public static URL buildTrailerUrl(String movieId) {
        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                .appendPath(CATEGORY)
                .appendPath(movieId)
                .appendPath(SUBCATEGORY_TRAILER)
                .appendQueryParameter("api_key", apiKey)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildReviewUrl(String movieId) {
        Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                .appendPath(CATEGORY)
                .appendPath(movieId)
                .appendPath(SUBCATEGORY_REVIEW)
                .appendQueryParameter("api_key", apiKey)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    // build the URL string for the thumb of the movie poster in the grid
    public static String buildThumbString(String movieId) {
        StringBuilder builtString = new StringBuilder();
        builtString.append(IMAGE_BASE_URL)
                .append(IMAGE_SIZE)
                .append(movieId);
        return builtString.toString();
    }

    public static String youtubeUrlString(String trailerKey) {
        StringBuilder builtString = new StringBuilder();
        builtString.append(YOUTUBE_BASE_URL).append("?v=").append(trailerKey);
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
    public static ArrayList<HashMap<String, String>> parseJsonData(String jsonResponse) {
        ArrayList<HashMap<String, String>> parsedMovieData = new ArrayList<>();

        if (jsonResponse != null) {
            try {
                JSONObject movieDataObject = new JSONObject(jsonResponse);
                // Getting JSON Array node
                JSONArray movieList = movieDataObject.getJSONArray("results");

                // looping through movies
                for (int i = 0; i < movieList.length(); i++) {

                    JSONObject movieEntry = movieList.getJSONObject(i);
                    String movieId = movieEntry.getString("id");
                    String originalTitle = movieEntry.getString("original_title");
                    String releaseDate = movieEntry.getString("release_date");
                    String userRating = movieEntry.getString("vote_average");
                    String description = movieEntry.getString("overview");
                    String moviePosterUrl = movieEntry.getString("poster_path");

                    HashMap<String, String> movieDetail = new HashMap<>();
                    movieDetail.put("id", movieId);
                    movieDetail.put("originalTitle", originalTitle);
                    movieDetail.put("releaseDate", releaseDate);
                    movieDetail.put("userRating", userRating);
                    movieDetail.put("description", description);
                    movieDetail.put("moviePosterUrl", moviePosterUrl);
                    parsedMovieData.add(movieDetail);
                }
            } catch (final JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JSON parsing error!");
            }
        }
        return parsedMovieData;
    }

}
