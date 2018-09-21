package com.app.dlike.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlike.R;
import com.app.dlike.activities.MainActivity;
import com.app.dlike.activities.PostActivity;
import com.app.dlike.api.models.Discussion;
import com.app.dlike.api.models.Draft;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by moses on 9/12/18.
 */

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.DraftViewHolder> {

    private ArrayList<Draft> drafts;

    public DraftsAdapter() {
        this.drafts = new ArrayList<>();
    }

    @NonNull
    @Override
    public DraftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DraftViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_draft, parent, false));
    }

    public void setDrafts(ArrayList<Draft> drafts) {
        this.drafts = new ArrayList<>(drafts);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull DraftViewHolder holder, int position) {
        holder.setDraft(drafts.get(position));
    }

    @Override
    public int getItemCount() {
        return drafts.size();
    }

    public class DraftViewHolder extends ViewHolder {

        private ImageView imageView;
        private TextView titleTextView;
        private TextView timeAgoTextView;
        private WebView descriptionWebView;
        private View clickableArea;

        public DraftViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.draftImage);
            titleTextView = itemView.findViewById(R.id.title);
            timeAgoTextView = itemView.findViewById(R.id.timeAgo);
            descriptionWebView = itemView.findViewById(R.id.description);
            clickableArea = itemView.findViewById(R.id.clickableArea);
        }

        public void setDraft(final Draft draft) {
            if (draft.image.startsWith("http")) {
                Picasso.with(itemView.getContext())
                        .load(draft.image)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(imageView);
            } else {
                File file = new File(draft.image);
                Picasso.with(itemView.getContext())
                        .load(Uri.fromFile(file))
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(imageView);
            }

            titleTextView.setText(draft.title);
            try {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                timeAgoTextView.setText(TimeAgo.using(simpleDateFormat.parse(draft.createdAt).getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            descriptionWebView.loadData(draft.post, "text/html; charset=utf-8", "UTF-8");

            clickableArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), PostActivity.class);
                    intent.putExtra(PostActivity.EXTRA_IMAGE, draft.image);
                    intent.putExtra(PostActivity.EXTRA_EXTERNAL_URL, draft.extUrl);
                    intent.putExtra(PostActivity.EXTRA_UPVOTE, draft.upvote == 1);
                    intent.putExtra(PostActivity.EXTRA_REWARD_WEIGHT, draft.rewardOption);
                    intent.putExtra(PostActivity.EXTRA_TITLE, draft.title);
                    intent.putExtra(PostActivity.EXTRA_TAGS, draft.tags);
                    intent.putExtra(PostActivity.EXTRA_DESCRIPTION, draft.post);
                    intent.putExtra(PostActivity.EXTRA_COMMUNITY, draft.category);

                    Activity activity = (Activity) v.getContext();
                    activity.startActivityForResult(intent, MainActivity.REQUEST_CODE_CREATE_POST);
                }
            });
        }
    }
}
