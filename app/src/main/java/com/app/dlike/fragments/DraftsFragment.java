package com.app.dlike.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.app.dlike.R;
import com.app.dlike.Tools;
import com.app.dlike.adapters.DraftsAdapter;
import com.app.dlike.api.DLike;
import com.app.dlike.api.models.Draft;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by moses on 9/12/18.
 */

public class DraftsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {


    private ViewGroup emptyLayout;
    private RecyclerView recyclerView;
    private DraftsAdapter draftsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.draftsRecyclerView);
        emptyLayout = view.findViewById(R.id.emptyLayout);

        recyclerView.setAdapter(draftsAdapter = new DraftsAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        loadDrafts();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drafts, container, false);
    }

    private void loadDrafts() {
        swipeRefreshLayout.setRefreshing(true);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dlike.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DLike dLike = retrofit.create(DLike.class);
        dLike.getAllDrafts(RequestBody.create(MediaType.parse("text/plain"), Tools.getAccessToken(getContext())))
                .enqueue(new Callback<Draft.DraftsResponse>() {
                    @Override
                    public void onResponse(Call<Draft.DraftsResponse> call, Response<Draft.DraftsResponse> response) {
                        swipeRefreshLayout.setRefreshing(false);
                        ArrayList<Draft> drafts = new ArrayList<>(response.body().drafts);
                        recyclerView.setVisibility(drafts.isEmpty() ? View.GONE : View.VISIBLE);
                        emptyLayout.setVisibility(drafts.isEmpty() ? View.VISIBLE : View.GONE);
                        if (!drafts.isEmpty()) {
                            draftsAdapter.setDrafts(drafts);
                        }
                    }

                    @Override
                    public void onFailure(Call<Draft.DraftsResponse> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), R.string.connection_error_please_try_again, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRefresh() {
        loadDrafts();
    }
}
