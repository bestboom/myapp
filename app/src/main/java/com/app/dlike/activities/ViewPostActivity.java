package com.app.dlike.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dlike.R;
import com.app.dlike.Tools;
import com.app.dlike.adapters.CommentsAdapter;
import com.app.dlike.adapters.TagsAdapter;
import com.app.dlike.api.Steem;
import com.app.dlike.api.models.Comment;
import com.app.dlike.api.models.CommentOperation;
import com.app.dlike.api.models.Discussion;
import com.app.dlike.api.models.VoteOperation;
import com.app.dlike.widgets.VotingDialog;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.app.dlike.adapters.PostsAdapter.convertDate;

/**
 * Created by moses on 8/25/18.
 */

public class ViewPostActivity extends LoginRequestActivity implements SwipeRefreshLayout.OnRefreshListener, VotingDialog.VotingCompletionListener {

    private Toolbar toolbar;
    private ImageView imageView;

    public static final String BUNDLE_DISCUSSION = "discussion";

    private static final int REQUEST_CODE_WRITE_COMMENT = 1133;
    private Discussion discussion;
    private ImageView authorImageView;
    private TextView author;
    private TextView timeAgo;
    private TextView numOfLikes;
    private TextView numComments;
    private TextView income;
    private FrameLayout like;
    private WebView discussionBody;
    private TextView postTitle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView comments;
    private CommentsAdapter commentsAdapter;
    private WebView commentTextBox;
    private ImageView commentButton;
    private ProgressBar commentingProgress;
    private View commentingLayout;
    private ImageView openLink;
    private ImageView shareButton;
    private RecyclerView tagsRecyclerView;
    private TagsAdapter tagsAdapter;
    private TextView commentHintTextView;

    private String comment = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        discussion = getIntent().getParcelableExtra(BUNDLE_DISCUSSION);
        if (discussion == null) {
            finish();
        }

        setContentView(R.layout.activity_view_post);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        authorImageView = findViewById(R.id.userProfileImage);
        author = findViewById(R.id.author);
        timeAgo = findViewById(R.id.timeAgo);
        numOfLikes = findViewById(R.id.numberOfLikes);
        numComments = findViewById(R.id.comments);
        postTitle = findViewById(R.id.postTitle);
        imageView = findViewById(R.id.postImage);
        income = findViewById(R.id.income);
        like = findViewById(R.id.likeButton);
        discussionBody = findViewById(R.id.discussionBody);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        comments = findViewById(R.id.commentsList);
        commentTextBox = findViewById(R.id.commentEditText);
        commentButton = findViewById(R.id.commentButton);
        commentingProgress = findViewById(R.id.commentingProgress);
        commentingLayout = findViewById(R.id.commentingLayout);
        comments.setAdapter(commentsAdapter = new CommentsAdapter());
        openLink = findViewById(R.id.openLink);
        commentHintTextView = findViewById(R.id.commentHint);
        shareButton = findViewById(R.id.shareButton);
        tagsRecyclerView = findViewById(R.id.tagsRecyclerView);
        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        commentsAdapter.setOnReplyListener(new CommentsAdapter.OnReplyListener() {
            public void onReplyClicked(String username) {
                comment += " @" + username + " ";
                updateComment();
            }
        });

        comments.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this);

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentBody = comment;
                if (Tools.isLoggedIn(ViewPostActivity.this)) {

                    if (!commentBody.isEmpty()) {
                        commentingLayout.setVisibility(View.INVISIBLE);
                        commentingProgress.setVisibility(View.VISIBLE);

                        CommentOperation commentOperation = new CommentOperation(discussion, Tools.getUsername(ViewPostActivity.this), commentBody);

                        Steem steem = Tools.getSteem(ViewPostActivity.this);
                        Log.d("Moses", new Gson().toJson(commentOperation));
                        steem.comment(commentOperation).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                commentingLayout.setVisibility(View.VISIBLE);
                                commentingProgress.setVisibility(View.INVISIBLE);
                                ResponseBody responseBody = response.body();
                                if (responseBody != null) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(responseBody.string());
                                        if (jsonObject.has("error")) {
                                            commentingError();
                                        } else {
                                            Toast.makeText(ViewPostActivity.this, R.string.commenting_success, Toast.LENGTH_SHORT).show();
                                            loadDiscussion();
                                            comment = "";
                                            updateComment();
                                        }
                                    } catch (JSONException | IOException ignore) {
                                        commentingError();
                                    }
                                } else {
                                    commentingError();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                commentingLayout.setVisibility(View.VISIBLE);
                                commentingProgress.setVisibility(View.INVISIBLE);
                                commentingError();
                            }
                        });
                    }
                } else {
                    startActivityForResult(new Intent(ViewPostActivity.this, LoginActivity.class), MainActivity.LOGIN_REQUEST_CODE);
                }
            }
        });
        reload();
    }

    private void commentingError() {
        Toast.makeText(ViewPostActivity.this, R.string.commenting_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateComment() {
        commentTextBox.loadData(comment, "text/html; charset=utf-8", "UTF-8");
        commentHintTextView.setVisibility(comment == null || comment.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void reload() {
        loadDiscussion();
    }

    @SuppressLint("SetTextI18n")
    private void loadDiscussion() {
        swipeRefreshLayout.setRefreshing(true);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.steemjs.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Steem steem = retrofit.create(Steem.class);
        steem.getDiscussion(discussion.author, discussion.permLink)
                .enqueue(new Callback<Discussion>() {
                    @Override
                    public void onResponse(Call<Discussion> call, Response<Discussion> response) {
                        ViewPostActivity.this.discussion = response.body();
                        inflateUI();
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onFailure(Call<Discussion> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        Snackbar.make(swipeRefreshLayout, R.string.connection_error_please_try_again, Snackbar.LENGTH_INDEFINITE).show();
                    }
                });
    }

    public void startWriteCommentActivity(View view) {
        Intent intent = new Intent(this, WritePostActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, comment);
        intent.putExtra(WritePostActivity.EXTRA_TITLE, "Write Comment");

        startActivityForResult(intent, REQUEST_CODE_WRITE_COMMENT);
    }

    private void inflateUI() {
        setTitle(discussion.title);
        tagsRecyclerView.setAdapter(tagsAdapter = new TagsAdapter(discussion));

        postTitle.setText(discussion.title);

        author.setText(discussion.author);
        timeAgo.setText(TimeAgo.using(convertDate(discussion.created)));

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String externalLink = discussion.getJSONMetaData().getString("url");
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, externalLink);
                    startActivity(Intent.createChooser(intent, "Share"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        openLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String externalLink = discussion.getJSONMetaData().getString("url");
                    if (!externalLink.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(externalLink));
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppCompatActivity activity = (AppCompatActivity) view.getContext();
                if (Tools.isLoggedIn(view.getContext())) {
                    new VotingDialog().show(activity.getSupportFragmentManager(), view, ViewPostActivity.this, discussion);
                } else {
                    activity.startActivityForResult(new Intent(view.getContext(), LoginActivity.class), MainActivity.LOGIN_REQUEST_CODE);
                }
            }
        });
        // load picture
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

                        commentsAdapter.setComments(response.body());
                        numComments.setText(String.valueOf(commentCount));
                    }

                    @Override
                    public void onFailure(Call<List<Comment>> call, Throwable t) {
                    }
                });
        income.setText("$ " + discussion.pendingPayoutValue.substring(0, 4));
        Picasso.with(this)
                .load("https://steemitimages.com/u/" + discussion.author + "/avatar")
                .placeholder(R.drawable.profile)
                .into(authorImageView);

        for (VoteOperation.Vote vote : discussion.activeVotes) {
            if (vote.voter.equals(Tools.getUsername(imageView.getContext()))) {
                ImageView imageView = like.findViewById(R.id.likeImage);
                imageView.setColorFilter(getResources().getColor(R.color.colorAccent));
                like.setClickable(false);
            }
        }

        discussionBody.getSettings().setJavaScriptEnabled(true);
        discussionBody.loadData(getBody(), "text/html; charset=utf-8", "UTF-8");
    }

    private String getBody() {
        Pattern pattern = Pattern.compile("^([.\\s\\S]*)#####([\\s\\S.]*)#####([.\\s\\S.]*)$");
        Matcher matcher = pattern.matcher(discussion.body);

        String body;
        if (matcher.find()) {
            body = matcher.group(2).trim();
        } else {
            body = discussion.body;
        }
        return addReadMore(body.replace("<html>", "").replace("</html>", ""));
    }

    private String addReadMore(String body) {
        return "<html>\n" +
                "<meta charset=\"utf-8\">\n" +
                "      <head>\n" +
                "            <script src=\"http://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js\"></script>\n" +
                "            <script src=\"https://fastcdn.org/Readmore.js/2.1.0/readmore.min.js\"></script>\n" +
                "            <style>" +
                "               .less{" +
                "                   color: red" +
                "               }" +
                "            </style>" +
                "      </head>\n" +
                "<body>\n" +
                "<div id=\"main\">\n" + body +
                "</div>\n" +
                "<script type=\"text/javascript\">\n" +
                "      $('#main').readmore({\n" +
                "            collapsedHeight: 100\n," +
                "            lessLink: '<a class=\'less\' href=\"#\">Show Less</a>'\n," +
                "            moreLink: '<a class=\'less\' href=\"#\">Read more</a>\n" +
                "      });\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }

    @Override
    public void loginSuccessful() {

    }

    @Override
    public void onRefresh() {
        loadDiscussion();
    }

    @Override
    public void onSuccess(Discussion discussion) {
        loadDiscussion();
    }

    @Override
    public void onFailure(Discussion discussion) {
        Toast.makeText(this, R.string.voting_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_WRITE_COMMENT) {
            if (resultCode == RESULT_OK) {
                comment = data.getStringExtra(Intent.EXTRA_TEXT);
            } else {
                comment = "";
            }
            updateComment();
        }
    }
}
