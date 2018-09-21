package com.app.dlike.adapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlike.activities.LoginActivity;
import com.app.dlike.activities.MainActivity;
import com.app.dlike.R;
import com.app.dlike.Tools;
import com.app.dlike.api.Steem;
import com.app.dlike.api.models.Comment;
import com.app.dlike.api.models.Discussion;
import com.app.dlike.api.models.VoteOperation;
import com.app.dlike.widgets.VotingDialog;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.app.dlike.adapters.PostsAdapter.convertDate;

/**
 * Created by moses on 8/26/18.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private ArrayList<Comment> comments;
    private OnReplyListener onReplyListener;

    public CommentsAdapter() {
        comments = new ArrayList<>();
    }

    public void setComments(List<Comment> comments) {
        this.comments.clear();
        for (Comment comment : comments) {
            if (comment.isDlikeDiscussion()) {
                this.comments.add(comment);
            }
        }
        notifyDataSetChanged();
    }

    public void setOnReplyListener(OnReplyListener onReplyListener) {
        this.onReplyListener = onReplyListener;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {
        holder.setComment(comments.get(position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder implements VotingDialog.VotingCompletionListener {
        private Comment comment;
        private CircleImageView authorImageView;
        private TextView author, timeAgo,  numOfLikes, income;
        private TextView replyButton;
        private WebView commentBody;
        private View like;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            authorImageView = itemView.findViewById(R.id.commentUserProfile);
            author = itemView.findViewById(R.id.author);
            timeAgo = itemView.findViewById(R.id.timeAgo);
            commentBody = itemView.findViewById(R.id.commentBody);
            numOfLikes = itemView.findViewById(R.id.numberOfLikes);
            income = itemView.findViewById(R.id.income);
            replyButton = itemView.findViewById(R.id.replyButton);
            like = itemView.findViewById(R.id.likeButton);
        }

        public void setComment(final Comment discussion) {
            this.comment = discussion;

            author.setText(discussion.author);
            timeAgo.setText(TimeAgo.using(convertDate(discussion.created)));
            Picasso.with(itemView.getContext())
                    .load("https://steemitimages.com/u/" + discussion.author + "/avatar")
                    .placeholder(R.drawable.profile)
                    .into(authorImageView);

            numOfLikes.setText(discussion.netVotes + " likes");

            income.setText("$ " + discussion.pendingPayoutValue.substring(0, 4));


            commentBody.loadData(discussion.body, "text/html; charset=utf-8", "UTF-8");

            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    if (Tools.isLoggedIn(view.getContext())) {

                        new VotingDialog().show(activity.getSupportFragmentManager(), view, CommentsViewHolder.this, discussion);

                    } else {
                        activity.startActivityForResult(new Intent(view.getContext(), LoginActivity.class), MainActivity.LOGIN_REQUEST_CODE);
                    }
                }
            });

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.steemjs.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            Steem steem = retrofit.create(Steem.class);
            steem.getActivevotes(discussion.author, discussion.permLink)
                    .enqueue(new Callback<List<VoteOperation.Vote>>() {
                        @Override
                        public void onResponse(Call<List<VoteOperation.Vote>> call, Response<List<VoteOperation.Vote>> response) {
                            discussion.activeVotes = response.body() == null ? new ArrayList<VoteOperation.Vote>() : response.body();
                            for (VoteOperation.Vote vote : discussion.activeVotes) {
                                if (vote.voter.equals(Tools.getUsername(authorImageView.getContext()))) {
                                    ImageView imageView = like.findViewById(R.id.likeImage);
                                    imageView.setColorFilter(itemView.getResources().getColor(R.color.colorAccent));
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<VoteOperation.Vote>> call, Throwable t) {

                        }
                    });
            replyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onReplyListener != null){
                        onReplyListener.onReplyClicked(discussion.author);
                    }
                }
            });
        }

        @Override
        public void onSuccess(Discussion discussion) {
            notifyItemChanged(getAdapterPosition());
        }

        @Override
        public void onFailure(Discussion discussion) {

        }
    }

    public interface OnReplyListener {
        void onReplyClicked(String username);
    }
}
