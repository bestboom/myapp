package com.app.dlike.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by moses on 8/29/18.
 */

public class LoginResponse {
    @SerializedName("refresh_token")
    public String refreshToken;
    @SerializedName("username")
    public String username;
    @SerializedName("access_token")
    public String accessToken;
}
