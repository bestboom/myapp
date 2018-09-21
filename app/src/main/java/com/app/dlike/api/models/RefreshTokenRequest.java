package com.app.dlike.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by moses on 8/29/18.
 */

public class RefreshTokenRequest {

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @SerializedName("refresh_token")
    public String refreshToken;
    @SerializedName("client_id")
    public String clientId = "dlike.android";
    @SerializedName("client_secret")
    public String clientSecret = "bdaddf356fcf46342c92f3ffecfb1c035c7b9dfe6124e3fb";
    @SerializedName("scope")
    public String scope = "vote,comment,comment_options,custom_json,offline";
}
