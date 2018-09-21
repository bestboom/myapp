package com.app.dlike.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.app.dlike.R;
import com.app.dlike.Tools;
import com.app.dlike.api.Steem;
import com.app.dlike.api.models.LoginResponse;
import com.app.dlike.api.models.RefreshTokenRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler hnd = new Handler();
        hnd.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Tools.isLoggedIn(SplashActivity.this) && Tools.tokenExpired(SplashActivity.this)){
                    refreshToken();
                }else{
                    startMainActivity();
                }
            }
        },2000);

    }

    private void refreshToken() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://v2.steemconnect.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Steem steem = retrofit.create(Steem.class);
        steem.refreshToken(new RefreshTokenRequest(Tools.getRefreshToken(this)))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        LoginResponse loginResponse = response.body();
                        if(loginResponse == null){
                            unableToRefreshToken();
                        }else{
                            Tools.setAuthentication(SplashActivity.this, loginResponse.accessToken, loginResponse.refreshToken, loginResponse.username);
                            startMainActivity();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        unableToRefreshToken();
                    }
                });
    }

    private void unableToRefreshToken(){
        Tools.clearAuthentication(this);
        startMainActivity();
    }

    private void startMainActivity(){
        Intent in = new Intent(SplashActivity.this,MainActivity.class);
        startActivity(in);
        finish();
    }
}
