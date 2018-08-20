package com.madfree.popularmovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.madfree.popularmovies.DetailActivity;
import com.madfree.popularmovies.R;
import com.madfree.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieListAdapterViewHolder> {

    private ArrayList<HashMap<String, String>> mMovieData;

    public MovieAdapter() {
    }

    // the constructor for our adapter
    public class MovieListAdapterViewHolder extends RecyclerView.ViewHolder {

        final ImageView mMoviePoster;

        MovieListAdapterViewHolder(View view) {
            super(view);
            mMoviePoster = view.findViewById(R.id.iv_movie_image);
        }
    }

    // creates and inflates the adapter when called
    @NonNull
    @Override
    public MovieListAdapterViewHolder onCreateViewHolder (@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movies_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new MovieListAdapterViewHolder(view);
    }

    // binds the data for the movies to the grid via the adapter
    @Override
    public void onBindViewHolder(@NonNull final MovieListAdapterViewHolder holder, final int position) {

        final String urlForThisItem = mMovieData.get(position).get("moviePosterUrl");

        Picasso.get()
                .load(NetworkUtils.buildThumbString(urlForThisItem))
                .fit()
                .into(holder.mMoviePoster);

        holder.mMoviePoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra("movieId", mMovieData.get(position).get("id"));
                intent.putExtra("originalTitle", mMovieData.get(position).get("originalTitle"));
                intent.putExtra("releaseDate", mMovieData.get(position).get("releaseDate"));
                intent.putExtra("userRating", mMovieData.get(position).get("userRating"));
                intent.putExtra("description", mMovieData.get(position).get("description"));
                intent.putExtra("moviePosterUrl", urlForThisItem);

                view.getContext().startActivity(intent);
                //Log.v(TAG, "This is the intent sent:" + intent);
            }
        });
    }

    // counts the number of items of the movie data list
    @Override
    public int getItemCount () {
        if (null == mMovieData) return 0;
        return mMovieData.size();
    }

    // fetches the movie data for the adapter
    public void setMovieData(ArrayList<HashMap<String, String>> movieData) {
        mMovieData = movieData;
        notifyDataSetChanged();
    }
}