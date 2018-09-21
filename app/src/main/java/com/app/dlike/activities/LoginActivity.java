package com.app.dlike.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.app.dlike.AuthCallback;
import com.app.dlike.R;
import com.app.dlike.api.Steem;
import com.app.dlike.api.models.LoginRequest;
import com.app.dlike.api.models.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by moses on 8/22/18.
 */

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_AUTHENTICATION = 1001;

    public static final String EXTRA_ACCESS_TOKEN = "access_token";
    public static final String EXTRA_USERNAME = "username";
    public static final String EXTRA_REFRESH_TOKEN = "refresh_token";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void close(View view) {
        finish();
    }

    public void login(View view) {
        Intent loginIntent = new Intent(this, AuthCallback.class);
        startActivityForResult(loginIntent, REQUEST_CODE_AUTHENTICATION);
    }

    public void signUp(View view) {
        Intent signUpIntent = new Intent(Intent.ACTION_VIEW);
        signUpIntent.setData(Uri.parse("https://signup.steemit.com/"));

        startActivity(signUpIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_AUTHENTICATION) {
            if (resultCode == Activity.RESULT_OK) {
                String code = data.getStringExtra(AuthCallback.EXTRA_CODE);

                requestRefreshToken(code);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestRefreshToken(String code) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://v2.steemconnect.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Steem steem = retrofit.create(Steem.class);

        steem.login(new LoginRequest(code))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        progressDialog.dismiss();
                        LoginResponse loginResponse = response.body();
                        if (loginResponse != null) {
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_USERNAME, loginResponse.username);
                            intent.putExtra(EXTRA_ACCESS_TOKEN, loginResponse.accessToken);
                            intent.putExtra(EXTRA_REFRESH_TOKEN, loginResponse.refreshToken);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        } else {
                            unableToLogin();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        unableToLogin();
                    }
                });

    }

    private void unableToLogin() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
