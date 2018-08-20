package com.madfree.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.madfree.popularmovies";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movies";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE)
                .build();

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_MOVIE_NAME = "movieName";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_RATING = "movieRating";
        public static final String COLUMN_DESCRIPTION = "movieDescription";
        public static final String COLUMN_MOVIE_POSTER_URL = "moviePosterUrl";
    }
}
