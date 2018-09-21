package com.app.dlike.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlike.activities.LoginActivity;
import com.app.dlike.activities.MainActivity;
import com.app.dlike.R;
import com.app.dlike.Tools;
import com.app.dlike.activities.ViewPostActivity;
import com.app.dlike.api.Steem;
import com.app.dlike.api.models.Comment;
import com.app.dlike.api.models.Discussion;
import com.app.dlike.api.models.VoteOperation;
import com.app.dlike.widgets.VotingDialog;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by moses on 8/18/18.
 */

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    private ArrayList<Discussion> discussions = new ArrayList<>();


    public PostsAdapter() {
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());

        return new PostViewHolder(layoutInflater.inflate(R.layout.item_post, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder postViewHolder, int i) {
        postViewHolder.setDiscussion(discussions.get(i));
    }

    @Override
    public int getItemCount() {
        return discussions.size();
    }

    private boolean exists(Discussion discussion){
        for(Discussion d: discussions){
            if(discussion.permLink.equalsIgnoreCase(d.permLink) &&
                    discussion.author.equalsIgnoreCase(d.author)){
                return true;
            }
        }
        return false;
    }

    public void addDiscussions(ArrayList<Discussion> discussions) {
        for (Discussion discussion : discussions) {
            if (discussion.isDlikeDiscussion() && !exists(discussion)) {
                this.discussions.add(discussion);
                notifyItemInserted(this.discussions.size() - 1);
            }
        }
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements VotingDialog.VotingCompletionListener {

        private ImageView authorImageView;
        private TextView author;
        private TextView timeAgo;
        private ImageView imageView;
        private TextView title;
        private TextView numOfLikes;
        private TextView numComments;
        private TextView income;
        private FrameLayout like;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorImageView = itemView.findViewById(R.id.userProfileImage);
            author = itemView.findViewById(R.id.author);
            timeAgo = itemView.findViewById(R.id.timeAgo);
            imageView = itemView.findViewById(R.id.postImage);
            title = itemView.findViewById(R.id.postTitle);
            numOfLikes = itemView.findViewById(R.id.numberOfLikes);
            numComments = itemView.findViewById(R.id.comments);
            income = itemView.findViewById(R.id.income);
            like = itemView.findViewById(R.id.likeButton);
        }


        public void refresh() {
            notifyItemChanged(getAdapterPosition());
        }

        @SuppressLint("SetTextI18n")
        public void setDiscussion(final Discussion discussion) {
            author.setText(discussion.author);
            timeAgo.setText(TimeAgo.using(convertDate(discussion.created)));


            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    MainActivity activity = (MainActivity) view.getContext();
                    if (Tools.isLoggedIn(view.getContext())) {

                        new VotingDialog().show(activity.getSupportFragmentManager(), view, PostViewHolder.this, discussion);

                    } else {
                        activity.startActivityForResult(new Intent(view.getContext(), LoginActivity.class), MainActivity.LOGIN_REQUEST_CODE);
                    }
                }
            });
            String image;
            try {
                if (discussion.getJSONMetaData().get("image") == null) {
                    imageView.setVisibility(View.GONE);
                } else {
                    if (discussion.getJSONMetaData().get("image") instanceof JSONArray) {
                        image = discussion.getJSONMetaData().getJSONArray("image").getString(0);
                    } else {
                        image = discussion.getJSONMetaData().getString("image");
                    }
                    if (image != null && !image.isEmpty()) {
                        Picasso.with(imageView.getContext())
                                .load(image)
                                .into(imageView);
                    } else {
                        imageView.setVisibility(View.GONE);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Picasso.with(itemView.getContext())
                    .load("https://steemitimages.com/u/" + discussion.author + "/avatar")
                    .placeholder(R.drawable.profile)
                    .into(authorImageView);
            Tools.showProfile(authorImageView, discussion.author);

            title.setText(discussion.title);
            numComments.setText(String.valueOf(0));
            numOfLikes.setText(discussion.netVotes + " likes");
            Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.steemjs.com")
                    .build();

            Steem steem = retrofit.create(Steem.class);
            steem.getComments(discussion.author, discussion.permLink)
                    .enqueue(new Callback<List<Comment>>() {
                        @Override
                        public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                            int commentCount = 0;
                            for (Comment comment : response.body()) {
                                if (comment.isDlikeDiscussion()) {
                                    commentCount++;
                                }
                            }

                            numComments.setText(String.valueOf(commentCount));
                        }

                        @Override
                        public void onFailure(Call<List<Comment>> call, Throwable t) {
                        }
                    });
            income.setText("$ " + discussion.pendingPayoutValue.substring(0, 4));


            for (VoteOperation.Vote vote : discussion.activeVotes) {
                if (vote.voter.equals(Tools.getUsername(imageView.getContext()))) {
                    ImageView imageView = like.findViewById(R.id.likeImage);
                    imageView.setColorFilter(itemView.getResources().getColor(R.color.colorAccent));
                }
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ViewPostActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(ViewPostActivity.BUNDLE_DISCUSSION, discussion);

                    intent.putExtras(bundle);
                    view.getContext().startActivity(intent);
                }
            });
        }

        @Override
        public void onSuccess(Discussion discussion) {
            refresh();
        }

        @Override
        public void onFailure(Discussion discussion) {

        }
    }

    public ArrayList<Discussion> getDiscussions() {
        return discussions;
    }

    public void setDiscussions(ArrayList<Discussion> discussions) {
        this.discussions.clear();
        for (Discussion discussion : discussions) {
            if (discussion.isDlikeDiscussion()) {
                this.discussions.add(discussion);
            }
        }
        notifyDataSetChanged();
    }

    public static long convertDate(String date) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return simpleDateFormat.parse(date).getTime();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }
}
