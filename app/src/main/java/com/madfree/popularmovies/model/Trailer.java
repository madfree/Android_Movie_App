package com.madfree.popularmovies.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Trailer {

    private static final String TAG = Trailer.class.getName();

    public String name;
    public String link;

    private Trailer(JSONObject object) {
        try {
            this.name = object.getString("name");
            this.link = object.getString("key");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Trailer> fromJson(JSONArray jsonObjects) {
        ArrayList<Trailer> trailers = new ArrayList<>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                Trailer trailer = new Trailer(jsonObjects.getJSONObject(i));
                trailers.add(trailer);
                Log.d(TAG, "Added trailer: " + trailer);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return trailers;
    }
}
