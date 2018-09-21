package com.app.dlike.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by moses on 8/31/18.
 */

public class WebCrawlerResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("url")
    public String url;

    @SerializedName("title")
    public String title;

    @SerializedName("imgUrl")
    public String imgUrl;

    @SerializedName("des")
    public String description;
}
