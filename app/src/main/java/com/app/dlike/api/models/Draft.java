package com.app.dlike.api.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by moses on 9/6/18.
 */

public class Draft implements Parcelable {
    public Draft() {

    }

    @SerializedName("id")
    public int id;

    @SerializedName("code")
    public String accessCode;

    @SerializedName("author_name")
    public String authorName;

    @SerializedName("category")
    public String category;

    @SerializedName("title")
    public String title;

    @SerializedName("exturl")
    public String extUrl;

    @SerializedName("post")
    public String post;

    @SerializedName("image")
    public String image;

    @SerializedName("reward_option")
    public int rewardOption;

    @SerializedName("upvote")
    public int upvote;

    @SerializedName("tags")
    public String tags;

    @SerializedName("scheduled_at")
    public String scheduledAt;

    @SerializedName("created_at")
    public String createdAt;

    protected Draft(Parcel in) {
        id = in.readInt();
        accessCode = in.readString();
        authorName = in.readString();
        category = in.readString();
        title = in.readString();
        extUrl = in.readString();
        post = in.readString();
        image = in.readString();
        rewardOption = in.readInt();
        upvote = in.readInt();
        tags = in.readString();
        scheduledAt = in.readString();
        createdAt = in.readString();
    }

    public static final Creator<Draft> CREATOR = new Creator<Draft>() {
        @Override
        public Draft createFromParcel(Parcel in) {
            return new Draft(in);
        }

        @Override
        public Draft[] newArray(int size) {
            return new Draft[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(accessCode);
        dest.writeString(authorName);
        dest.writeString(category);
        dest.writeString(title);
        dest.writeString(extUrl);
        dest.writeString(post);
        dest.writeString(image);
        dest.writeInt(rewardOption);
        dest.writeInt(upvote);
        dest.writeString(tags);
        dest.writeString(scheduledAt);
        dest.writeString(createdAt);
    }

    public static class DraftsResponse {
        @SerializedName("error")
        public boolean error;

        @SerializedName("drafts")
        public List<Draft> drafts;
    }

    public static class DraftResponse {
        @SerializedName("error")
        public boolean error;

        @SerializedName("draft")
        public Draft draft;

        @SerializedName("description")
        public String description;
    }
}
