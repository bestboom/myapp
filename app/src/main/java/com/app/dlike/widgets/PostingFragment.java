package com.app.dlike.widgets;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.dlike.R;
import com.app.dlike.Tools;
import com.app.dlike.activities.MainActivity;
import com.app.dlike.activities.PostActivity;
import com.app.dlike.api.DLike;
import com.app.dlike.api.Steem;
import com.app.dlike.api.models.SubmitPostResponse;
import com.app.dlike.api.models.VoteOperation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by moses on 8/31/18.
 */

public class PostingFragment extends Fragment {

    private View parentLayout, postingLayout, successLayout;
    private TextView titleTextView;

    private View view;
    private boolean showing = false;
    private View postingFragmentLayout;
    private ViewGroup mainLayout;
    private String community;
    private String title;
    private String description;
    private String tags;
    private String externalURL;
    private int rewardWeight;
    private boolean upVote;
    private String fileLink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        parentLayout = view.findViewById(R.id.parentLayout);
        postingLayout = view.findViewById(R.id.postingLayout);
        successLayout = view.findViewById(R.id.successLayout);
        titleTextView = view.findViewById(R.id.titleTextView);
    }

    public void show(Intent intent) {
        showing = true;
        postingFragmentLayout.setVisibility(View.VISIBLE);
        TransitionManager.beginDelayedTransition(mainLayout);

        community = intent.getStringExtra(PostActivity.EXTRA_COMMUNITY);
        title = intent.getStringExtra(PostActivity.EXTRA_TITLE);
        description = intent.getStringExtra(PostActivity.EXTRA_DESCRIPTION);
        tags = intent.getStringExtra(PostActivity.EXTRA_TAGS);
        externalURL = intent.getStringExtra(PostActivity.EXTRA_EXTERNAL_URL);
        rewardWeight = intent.getIntExtra(PostActivity.EXTRA_REWARD_WEIGHT, 0);
        upVote = intent.getBooleanExtra(PostActivity.EXTRA_UPVOTE, false);
        fileLink = intent.getStringExtra(PostActivity.EXTRA_IMAGE);

        RequestBody codeBody = RequestBody.create(MediaType.parse("text/plain"), Tools.getAccessToken(getContext()));
        RequestBody communityBody = RequestBody.create(MediaType.parse("text/plain"), community);
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody tagsBody = RequestBody.create(MediaType.parse("text/plain"), tags);
        RequestBody externalURLBody = RequestBody.create(MediaType.parse("text/plain"), externalURL);
        RequestBody rewardOption = RequestBody.create(MediaType.parse("text/plain"), rewardWeight + "");
        RequestBody imageBody;
        if (fileLink.startsWith("http")) {
            imageBody = RequestBody.create(MediaType.parse("text/plain"), fileLink);
        } else if (fileLink.isEmpty()) {
            imageBody = RequestBody.create(MediaType.parse("text/plain"), "https://dlike.io/@" + Tools.getUsername(getContext()));
        } else {
            imageBody = RequestBody.create(MediaType.parse("image/jpeg"), new File(fileLink));
        }

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dlike.io/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Call<SubmitPostResponse> call;
        if (fileLink.startsWith("http")) {
            call = retrofit.create(DLike.class).post(codeBody, communityBody, titleBody, tagsBody, externalURLBody, imageBody, descriptionBody, rewardOption);
        } else {
            File file = new File(fileLink);
            MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), imageBody);
            call = retrofit.create(DLike.class).post(codeBody, communityBody, titleBody, tagsBody, externalURLBody, part, descriptionBody, rewardOption);
        }
        call.enqueue(new Callback<SubmitPostResponse>() {
            @Override
            public void onResponse(Call<SubmitPostResponse> call, Response<SubmitPostResponse> response) {

                SubmitPostResponse submitPostResponse = response.body();
                if (submitPostResponse == null) {
                    errorPosting();
                } else {

                    if (submitPostResponse.error) {
                        errorPosting();
                    } else {
                        postingLayout.setVisibility(View.GONE);
                        successLayout.setVisibility(View.VISIBLE);
                        if (upVote) {
                            final VoteOperation.Vote vote = new VoteOperation.Vote();
                            vote.author = Tools.getUsername(getContext());
                            vote.permLink = submitPostResponse.description.toString();
                            vote.voter = Tools.getUsername(view.getContext());
                            vote.weight = 10000;

                            VoteOperation voteOperation = new VoteOperation();
                            voteOperation.setVote(vote);

                            Steem steem = Tools.getSteem(view.getContext());
                            steem.vote(voteOperation).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    successPosting();
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    successPosting();
                                }
                            });
                        } else {
                            new Handler()
                                    .postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            successPosting();
                                        }
                                    }, 2000);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<SubmitPostResponse> call, Throwable t) {
                errorPosting();
                Log.e("Moses", "Three", t);
            }
        });

        titleTextView.setText(title);
    }

    private void successPosting() {
        hide();
        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        mainActivity.refresh();
    }

    private void errorPosting() {
        Snackbar snackbar = Snackbar.make(view, "Error submitting post. Please try again!", Snackbar.LENGTH_LONG);
        snackbar.show();
        hide();


        Intent intent = new Intent(getContext(), PostActivity.class);
        intent.putExtra(PostActivity.EXTRA_COMMUNITY, community);
        intent.putExtra(PostActivity.EXTRA_TITLE, title);
        intent.putExtra(PostActivity.EXTRA_EXTERNAL_URL, externalURL);
        intent.putExtra(PostActivity.EXTRA_DESCRIPTION, description);
        intent.putExtra(PostActivity.EXTRA_TAGS, tags);
        intent.putExtra(PostActivity.EXTRA_REWARD_WEIGHT, rewardWeight);
        intent.putExtra(PostActivity.EXTRA_UPVOTE, upVote);
        intent.putExtra(PostActivity.EXTRA_IMAGE, fileLink);

        getActivity().startActivityForResult(intent, MainActivity.REQUEST_CODE_CREATE_POST);

    }

    public void hide() {
        showing = false;
        postingFragmentLayout.setVisibility(View.GONE);
        TransitionManager.beginDelayedTransition(mainLayout);
    }

    public void setLayouts(View postingFragmentLayout, ViewGroup mainLayout) {
        this.postingFragmentLayout = postingFragmentLayout;
        this.mainLayout = mainLayout;
    }
}
