package com.app.dlike.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.dlike.adapters.PostsAdapter;
import com.app.dlike.R;
import com.app.dlike.api.Steem;
import com.app.dlike.api.models.Discussion;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by moses on 8/18/18.
 */

public class PostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View errorView;
    private PostsAdapter postsAdapter;

    private static final int LIMIT_PER_FETCH = 30;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        errorView = view.findViewById(R.id.errorView);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDiscussions();
            }
        });
        recyclerView.setAdapter(postsAdapter = new PostsAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemViewCacheSize(20);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)) {
                    loadDiscussions(true);
                }
            }
        });
        loadDiscussions();
    }

    public void clear() {
        postsAdapter = new PostsAdapter();
        recyclerView.setAdapter(postsAdapter);
        postsAdapter.notifyDataSetChanged();
    }

    public void loadDiscussions() {
        loadDiscussions(false);
    }

    public void loadDiscussions(final boolean more) {
        swipeRefreshLayout.setRefreshing(true);
        errorView.setVisibility(View.GONE);

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.steemjs.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Steem steem = retrofit.create(Steem.class);

        String body = "{\"tag\": \"dlike\", \"limit\": " + LIMIT_PER_FETCH + "}";
        Log.e("PFFF", body);

        if (more && !postsAdapter.getDiscussions().isEmpty()) {
            Discussion discussion = postsAdapter.getDiscussions().get(postsAdapter.getDiscussions().size() - 1);
            body = "{\"tag\": \"dlike\", \"limit\": " + LIMIT_PER_FETCH + ", \"start_author\": \"" + discussion.author + "\", \"start_permlink\": \"" + discussion.permLink + "\"}";
        }

        Call<List<Discussion>> call = steem.getDiscussions(body);
        call.enqueue(new Callback<List<Discussion>>() {
            @Override
            public void onResponse(Call<List<Discussion>> call, Response<List<Discussion>> response) {
                recyclerView.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);

                if (more && !postsAdapter.getDiscussions().isEmpty()) {
                    ArrayList<Discussion> items = new ArrayList(response.body());
                    postsAdapter.addDiscussions(new ArrayList<>(items.subList(1, items.size())));
                } else {
                    postsAdapter.setDiscussions(new ArrayList<>(response.body()));
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Discussion>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                errorView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Log.e("PostsFragment", "onFailure", t);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_posts, container, false);
    }
}
