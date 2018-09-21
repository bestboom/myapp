package com.app.dlike.widgets;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dlike.adapters.PostsAdapter;
import com.app.dlike.R;
import com.app.dlike.Tools;
import com.app.dlike.api.Steem;
import com.app.dlike.api.models.Discussion;
import com.app.dlike.api.models.VoteOperation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by moses on 8/25/18.
 */

public class VotingDialog extends BaseDialogFragment {

    private View view;
    private PostsAdapter.PostViewHolder postViewHolder;
    private ProgressBar likeProgress;
    private ImageView likeImage;
    private AppCompatSeekBar tickSeekBar;
    private TextView progress;
    private Button likeButton, cancelButton;
    private Discussion discussion;
    private VotingCompletionListener votingCompletionListener;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progress = view.findViewById(R.id.progress);
        tickSeekBar = view.findViewById(R.id.seekBar);

        cancelButton = view.findViewById(R.id.cancelButton);
        likeButton = view.findViewById(R.id.voteButton);

        tickSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress.setText(seekBar.getProgress() + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                int weight = tickSeekBar.getProgress() * 100;
                likeProgress.setVisibility(View.VISIBLE);
                likeImage.setVisibility(View.GONE);
                dismiss();

                final VoteOperation.Vote vote = new VoteOperation.Vote();
                vote.author = discussion.author;
                vote.permLink = discussion.permLink;
                vote.voter = Tools.getUsername(view.getContext());
                vote.weight = weight;

                VoteOperation voteOperation = new VoteOperation();
                voteOperation.setVote(vote);

                Steem steem = Tools.getSteem(view.getContext());
                steem.vote(voteOperation).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        likeProgress.setVisibility(View.GONE);
                        likeImage.setVisibility(View.VISIBLE);

                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(responseBody.string());
                                if (!jsonObject.has("error")) {
                                    discussion.netVotes++;
                                    discussion.activeVotes.add(vote);
                                }
                                if (votingCompletionListener != null) {
                                    votingCompletionListener.onSuccess(discussion);
                                }
                            } catch (JSONException | IOException ignore) {
                                if (votingCompletionListener != null) {
                                    votingCompletionListener.onFailure(discussion);
                                }
                            }
                        } else {
                            view.setClickable(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        likeProgress.setVisibility(View.GONE);
                        likeImage.setVisibility(View.VISIBLE);
                        view.setClickable(true);
                        if (votingCompletionListener != null) {
                            votingCompletionListener.onFailure(discussion);
                        }
                    }
                });

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_vote, container, false);
    }

    private boolean hasVoted() {
        for (VoteOperation.Vote vote : discussion.activeVotes) {
            if ((vote.author != null && vote.author.equals(Tools.getUsername(view.getContext()))) ||
                    likeImage.getColorFilter() != null) {
                return true;
            }
        }
        return false;
    }

    public void show(FragmentManager fragmentManager, View parent, VotingCompletionListener votingCompletionListener, final Discussion discussion) {
        this.discussion = discussion;
        this.view = parent;
        this.votingCompletionListener = votingCompletionListener;

        likeProgress = view.findViewById(R.id.likeProgress);
        likeImage = view.findViewById(R.id.likeImage);
        if (hasVoted()) {
            Toast.makeText(view.getContext(), R.string.already_voted, Toast.LENGTH_SHORT).show();
        } else {
            show(fragmentManager, VotingDialog.class.getSimpleName());
        }

    }


    public interface VotingCompletionListener {
        void onSuccess(Discussion discussion);

        void onFailure(Discussion discussion);
    }
}
