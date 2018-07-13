package com.madfree.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class MovieProvider extends ContentProvider {

    private final String TAG = MovieProvider.class.getName();

    public static final int CODE_FAV_MOVIES = 100;
    public static final int CODE_FAV_MOVIE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildMatcher();

    public static UriMatcher buildMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE, CODE_FAV_MOVIES);

        matcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/#", CODE_FAV_MOVIE_WITH_ID);

        return matcher;
    }

    private MovieDbHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mOpenHelper = new MovieDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);

        Cursor retCursor;

        switch (match) {

            case CODE_FAV_MOVIES: {
                retCursor = db.query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case CODE_FAV_MOVIE_WITH_ID: {
                retCursor = db.query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        throw new RuntimeException("This method will not be implemented!");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case CODE_FAV_MOVIES:
                long id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, contentValues);

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "Movie successfully inserted: " + returnUri);
        return returnUri;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String
            s, @Nullable String[] strings) {
        throw new RuntimeException("This method will not be implemented!");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);

        int movieDeleted;
        String id;

        switch (match) {
            case CODE_FAV_MOVIE_WITH_ID:
                id = uri.getPathSegments().get(1);
                movieDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME,
                        "movieId=?",
                        new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        if (movieDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        Log.d(TAG, "Movie successfully deleted: " + movieDeleted);
        return movieDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
