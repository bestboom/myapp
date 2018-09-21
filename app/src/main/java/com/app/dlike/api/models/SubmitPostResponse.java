package com.app.dlike.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by moses on 8/31/18.
 */

public class SubmitPostResponse {
    @SerializedName("error")
    public boolean error;
    @SerializedName("description")
    public Object description;
}
