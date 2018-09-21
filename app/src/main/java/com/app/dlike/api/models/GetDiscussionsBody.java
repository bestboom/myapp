package com.app.dlike.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by moses on 8/18/18.
 */

public class GetDiscussionsBody {
    @SerializedName("jsonrpc")
    public String jsonrpc = "2.0";
    @SerializedName("method")
    public String method = "tags_api.get_discussions_by_created";
    @SerializedName("params")
    public Params params = new Params();
    @SerializedName("id")
    public int id = 1;

    public static class Params {
        @SerializedName("tag")
        public String tag = "dlike";
        @SerializedName("limit")
        public int limit = 30;
        @SerializedName("start_author")
        public String startAuthor;
        @SerializedName("start_permlink")
        public String startPermlink;
    }
}
