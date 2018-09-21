package com.app.dlike.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.app.dlike.R;
import com.app.dlike.api.models.Discussion;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by moses on 8/29/18.
 */

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagViewHolder> {

    private final Discussion discussion;

    private ArrayList<String> tags = new ArrayList<>();

    public TagsAdapter(Discussion discussion) {
        this.discussion = discussion;
        loadTags();
    }

    private void loadTags() {
        try {
            JSONArray jsonArray = discussion.getJSONMetaData().getJSONArray("tags");
            for (int i = 0; i < jsonArray.length(); i++) {
                tags.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TagViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        holder.setText(tags.get(position));
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public class TagViewHolder extends RecyclerView.ViewHolder {

        public TagViewHolder(View itemView) {
            super(itemView);
        }

        public void setText(String text) {
            ((Button) itemView).setText(text);
        }
    }
}
