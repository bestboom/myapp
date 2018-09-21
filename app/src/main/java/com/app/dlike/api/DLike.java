package com.app.dlike.api;

import com.app.dlike.api.models.Categories;
import com.app.dlike.api.models.Draft;
import com.app.dlike.api.models.SubmitPostResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

/**
 * Created by moses on 8/31/18.
 */

public interface DLike {

    @Multipart
    @POST("/dataCrowler.php")
    Call<ResponseBody> crawlWeb(@Part("url") RequestBody urlBody);

    @GET("/android_categories.php")
    Call<Categories> getCategories();

    @Multipart
    @POST("/android_submit_post.php")
    Call<SubmitPostResponse> post(@Part("code") RequestBody code,
                                  @Part("category") RequestBody category,
                                  @Part("title") RequestBody title,
                                  @Part("tags") RequestBody tags,
                                  @Part("exturl") RequestBody externalURL,
                                  @Part("image") RequestBody image,
                                  @Part("post") RequestBody post,
                                  @Part("reward_option") RequestBody rewardOption);

    @Multipart
    @POST("/android_submit_post.php")
    Call<SubmitPostResponse> post(@Part("code") RequestBody code,
                                  @Part("category") RequestBody category,
                                  @Part("title") RequestBody title,
                                  @Part("tags") RequestBody tags,
                                  @Part("exturl") RequestBody externalURL,
                                  @Part MultipartBody.Part image,
                                  @Part("post") RequestBody post,
                                  @Part("reward_option") RequestBody rewardOption);


    @Multipart
    @POST("/android_save_draft.php?create=true")
    Call<Draft.DraftResponse> createDraft(@PartMap Map<String,RequestBody> body);

    @Multipart
    @POST("/android_save_draft.php?all=true")
    Call<Draft.DraftsResponse> getAllDrafts(@Part("code") RequestBody code);

    @Multipart
    @POST("/android_save_draft.php")
    Call<Draft.DraftResponse> getDraft(@Query("id") int id, @Part("code") RequestBody code);
}
