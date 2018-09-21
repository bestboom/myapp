package com.app.dlike.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moses on 8/18/18.
 */

public class GetDiscussionsResult {
    @SerializedName("jsonrpc")
    public String jsonrpc;
    @SerializedName("result")
    public Result result;

    public static class Result {
        @SerializedName("discussions")
        public List<Discussion> discussions = new ArrayList<>();
    }
}
