package com.app.dlike.api.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moses on 8/18/18.
 */

public class Discussion implements Parcelable {
    @SerializedName("id")
    public int id;
    @SerializedName("author")
    public String author;
    @SerializedName("permlink")
    public String permLink;
    @SerializedName("parent_permlink")
    public String parentPermLink;
    @SerializedName("category")
    public String category;
    @SerializedName("parent_author")
    public String parentAuthor;
    @SerializedName("title")
    public String title;
    @SerializedName("body")
    public String body;
    @SerializedName("last_update")
    public String lastUpdate;
    @SerializedName("created")
    public String created;
    @SerializedName("active")
    public String active;
    @SerializedName("last_payout")
    public String lastPayout;
    @SerializedName("pending_payout_value")
    public String pendingPayoutValue;
    @SerializedName("url")
    public String url;
    @SerializedName("root_title")
    public String rootTitle;
    @SerializedName("net_votes")
    public int netVotes;
    @SerializedName("json_metadata")
    public String jsonMetaData;
    @SerializedName("active_votes")
    public List<VoteOperation.Vote> activeVotes = new ArrayList<>();

    public Discussion() {

    }

    protected Discussion(Parcel in) {
        id = in.readInt();
        author = in.readString();
        permLink = in.readString();
        category = in.readString();
        parentAuthor = in.readString();
        title = in.readString();
        body = in.readString();
        lastUpdate = in.readString();
        created = in.readString();
        active = in.readString();
        lastPayout = in.readString();
        pendingPayoutValue = in.readString();
        url = in.readString();
        rootTitle = in.readString();
        netVotes = in.readInt();
        jsonMetaData = in.readString();
    }

    public static final Creator<Discussion> CREATOR = new Creator<Discussion>() {
        @Override
        public Discussion createFromParcel(Parcel in) {
            return new Discussion(in);
        }

        @Override
        public Discussion[] newArray(int size) {
            return new Discussion[size];
        }
    };

    public JSONObject getJSONMetaData() {
        try {
            return new JSONObject(jsonMetaData);
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(author);
        parcel.writeString(permLink);
        parcel.writeString(category);
        parcel.writeString(parentAuthor);
        parcel.writeString(title);
        parcel.writeString(body);
        parcel.writeString(lastUpdate);
        parcel.writeString(created);
        parcel.writeString(active);
        parcel.writeString(lastPayout);
        parcel.writeString(pendingPayoutValue);
        parcel.writeString(url);
        parcel.writeString(rootTitle);
        parcel.writeInt(netVotes);
        parcel.writeString(jsonMetaData);
    }

    public boolean isDlikeDiscussion() {
        try {
            JSONObject jsonObject = new JSONObject(jsonMetaData);
            return jsonObject.getString("community") != null && jsonObject.getString("community").equalsIgnoreCase("dlike");
        } catch (JSONException e) {
            return false;
        }
    }
}
