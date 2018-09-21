package com.app.dlike.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moses on 8/18/18.
 */

public class VoteOperation {

    @SerializedName("operations")
    public ArrayList list = new ArrayList();

    public void setVote(Vote vote) {
        List list2 = new ArrayList();
        list.add(list2);

        list2.add("vote");
        list2.add(vote);
    }

    public static class Vote {
        @SerializedName("voter")
        public String voter;
        @SerializedName("weight")
        public int weight;
        @SerializedName("author")
        public String author;
        @SerializedName("permlink")
        public String permLink;
    }
}

