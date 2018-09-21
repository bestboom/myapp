package com.app.dlike.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by moses on 8/26/18.
 */

public class Beneficiary {
    @SerializedName("account")
    public String account;
    @SerializedName("weight")
    public int weight;

    public Beneficiary(String account, int weight){
        this.account = account;
        this.weight = weight;
    }
}
