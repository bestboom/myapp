package com.app.dlike.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moses on 8/31/18.
 */

public class Categories {
    @SerializedName("categories")
    public List<String> categories = new ArrayList<>();
}
