package com.app.dlike.api.models;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by moses on 8/19/18.
 */

public class Comment extends Discussion {

    public Comment(){

    }
    protected Comment(Parcel in) {
        super(in);
    }

}
