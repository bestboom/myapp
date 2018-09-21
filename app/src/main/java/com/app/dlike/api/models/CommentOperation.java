package com.app.dlike.api.models;

import android.annotation.SuppressLint;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by moses on 8/26/18.
 */

public class CommentOperation {
    @SerializedName("operations")
    public List operations = new ArrayList<>();

    @SuppressLint("SimpleDateFormat")
    public CommentOperation(Discussion discussion, String username, String commentText) {
        Comment comment = new Comment();
        comment.parentAuthor = discussion.author;
        comment.parentPermLink = discussion.permLink;
        comment.author = username;
        comment.permLink = username + '-' + new SimpleDateFormat("M-d-yyyy-hh-mm-SSS").format(Calendar.getInstance().getTime());
        comment.title = "";
        comment.body = commentText;
        comment.jsonMetaData = "{\"community\": \"dlike\"}";


        operations.add(Arrays.asList("comment", comment));


        CommentOptions commentOptions = new CommentOptions();
        commentOptions.author = username;
        commentOptions.permLink = comment.permLink;
        commentOptions.maxAcceptedPayout = "0.000 SBD";
        commentOptions.beneficiaries = Arrays.asList(new Beneficiary("dlike", 9), new Beneficiary("dlike.fund", 1));
        commentOptions.extensions = new Extensions(commentOptions.beneficiaries);

        operations.add(Arrays.asList("comment_options", commentOptions));
    }

    public static class CommentOptions {
        @SerializedName("author")
        public String author;
        @SerializedName("permlink")
        public String permLink;
        @SerializedName("max_accepted_payout")
        public String maxAcceptedPayout;
        @SerializedName("percent_steem_dollars")
        public int percentSteemDollars = 10000;
        @SerializedName("allow_votes")
        public boolean allowVotes = true;
        @SerializedName("allow_curation_rewards")
        public boolean allowCurationRewards = true;
        @SerializedName("beneficiaries")
        public List<Beneficiary> beneficiaries;
        @SerializedName("extensions")
        public List extensions;
    }

    public static class Extensions extends ArrayList {
        public Extensions(List<Beneficiary> beneficiaries) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(0);
            arrayList.add(new Beneficiaries(beneficiaries));

            add(arrayList);
        }

        public static class Beneficiaries {
            @SerializedName("beneficiaries")
            public List<Beneficiary> beneficiaries;

            public Beneficiaries(List<Beneficiary> beneficiaries) {
                this.beneficiaries = beneficiaries;
            }
        }
    }
}
