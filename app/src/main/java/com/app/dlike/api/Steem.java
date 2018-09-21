package com.app.dlike.api;

import com.app.dlike.api.models.Comment;
import com.app.dlike.api.models.CommentOperation;
import com.app.dlike.api.models.Discussion;
import com.app.dlike.api.models.GetDiscussionsBody;
import com.app.dlike.api.models.LoginRequest;
import com.app.dlike.api.models.LoginResponse;
import com.app.dlike.api.models.RefreshTokenRequest;
import com.app.dlike.api.models.VoteOperation;

import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by moses on 8/18/18.
 */

public interface Steem {

    @POST("/api/oauth2/token")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("/api/oauth2/token")
    Call<LoginResponse> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);

    @GET("/get_discussions_by_created")
    Call<List<Discussion>> getDiscussions(@Query("query") String body);

    @GET("/get_content_replies")
    Call<List<Comment>> getComments(@Query("author") String author, @Query("permlink") String permLink);

    @POST("broadcast")
    Call<ResponseBody> vote(@Body VoteOperation body);

    @POST("broadcast")
    Call<ResponseBody> comment(@Body CommentOperation body);

    @GET("/get_content")
    Call<Discussion> getDiscussion(@Query("author") String author, @Query("permlink") String permLink);

    @GET("/get_active_votes")
    Call<List<VoteOperation.Vote>> getActivevotes(@Query("author") String author, @Query("permlink") String permlink);
}
