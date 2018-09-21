package com.app.dlike.jobs;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.app.dlike.Tools;
import com.app.dlike.activities.PostActivity;
import com.app.dlike.api.DLike;
import com.app.dlike.api.Steem;
import com.app.dlike.api.models.Draft;
import com.app.dlike.api.models.SubmitPostResponse;
import com.app.dlike.api.models.VoteOperation;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import java.io.File;
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
 * Created by moses on 9/18/18.
 */

public class SchedulePostJob extends Job {

    public static final String TAG = "job_schedule_post";
    public static final String EXTRA_DRAFT = "draft";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Draft draft = params.getTransientExtras().getParcelable(EXTRA_DRAFT);
        RequestBody codeBody = RequestBody.create(MediaType.parse("text/plain"), Tools.getAccessToken(getContext()));
        RequestBody communityBody = RequestBody.create(MediaType.parse("text/plain"), draft.category);
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), draft.title);
        RequestBody descriptionBody = RequestBody.create(MediaType.parse("text/plain"), draft.post);
        RequestBody tagsBody = RequestBody.create(MediaType.parse("text/plain"), draft.tags);
        RequestBody externalURLBody = RequestBody.create(MediaType.parse("text/plain"), draft.extUrl);
        RequestBody rewardOption = RequestBody.create(MediaType.parse("text/plain"), draft.rewardOption + "");
        RequestBody imageBody;

        String fileLink = draft.image;
        final boolean upVote = draft.upvote == 1;
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

        Log.d("Job", draft.post + " ");
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
                    // error
                    Log.e("Moses", "Error 1");
                } else {
                    if (upVote) {
                        final VoteOperation.Vote vote = new VoteOperation.Vote();
                        vote.author = Tools.getUsername(getContext());
                        vote.permLink = submitPostResponse.description.toString();
                        vote.voter = Tools.getUsername(getContext());
                        vote.weight = 10000;

                        VoteOperation voteOperation = new VoteOperation();
                        voteOperation.setVote(vote);

                        Steem steem = Tools.getSteem(getContext());
                        steem.vote(voteOperation).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<SubmitPostResponse> call, Throwable t) {
                Log.e("Moses", "Error 2", t);
            }
        });

        return Result.SUCCESS;
    }

    public static void scheduleJob(Draft draft, long time) {
        Bundle extras = new Bundle();
        extras.putParcelable(SchedulePostJob.EXTRA_DRAFT, draft);
        new JobRequest.Builder(SchedulePostJob.TAG)
                .setExact(time - System.currentTimeMillis())
                .setUpdateCurrent(true)
                .setTransientExtras(extras)
                .build()
                .schedule();
    }
}
