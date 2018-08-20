package com.madfree.popularmovies.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Review {

    private static final String TAG = Review.class.getName();

    public String name;
    public String content;

    private Review(JSONObject object) {
        try {
            this.name = object.getString("author");
            this.content = object.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Review> fromJson(JSONArray jsonObjects) {
        ArrayList<Review> reviews = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                Review review = new Review(jsonObjects.getJSONObject(i));
                reviews.add(review);
                Log.d(TAG, "Added Review: " + review);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return reviews;
    }
}