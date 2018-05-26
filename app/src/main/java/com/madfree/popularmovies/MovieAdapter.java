package com.madfree.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.madfree.popularmovies.Helper.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieListAdapterViewHolder> {

    private final String TAG = MainActivity.class.getName();
    private ArrayList<HashMap<String, String>> mMovieData;

    public MovieAdapter() {
    }

    public class MovieListAdapterViewHolder extends RecyclerView.ViewHolder {

        final ImageView mMoviePoster;

        MovieListAdapterViewHolder(View view) {
            super(view);
            mMoviePoster = view.findViewById(R.id.iv_movie_image);
        }
    }

    @NonNull
    @Override
    public MovieListAdapterViewHolder onCreateViewHolder (@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movies_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new MovieListAdapterViewHolder(view);
    }

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
                intent.putExtra("originalTitle", mMovieData.get(position).get("originalTitle"));
                intent.putExtra("moviePosterUrl", urlForThisItem);
                intent.putExtra("description", mMovieData.get(position).get("description"));
                intent.putExtra("userRating", mMovieData.get(position).get("userRating"));
                intent.putExtra("releaseDate", mMovieData.get(position).get("releaseDate"));

                view.getContext().startActivity(intent);
                //Log.v(TAG, "This is the intent sent:" + intent);
            }
        });
    }

    @Override
    public int getItemCount () {
        if (null == mMovieData) return 0;
        return mMovieData.size();
    }

    public void setMovieData(ArrayList<HashMap<String, String>> movieData) {
        mMovieData = movieData;
        notifyDataSetChanged();
    }
}
