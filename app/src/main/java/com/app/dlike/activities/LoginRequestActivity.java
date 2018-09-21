package com.app.dlike.activities;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.app.dlike.Tools;

/**
 * Created by moses on 8/26/18.
 */

public abstract class LoginRequestActivity extends AppCompatActivity {

    public static final int LOGIN_REQUEST_CODE = 1001;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case LOGIN_REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    String accessToken = data.getStringExtra(LoginActivity.EXTRA_ACCESS_TOKEN);
                    String username = data.getStringExtra(LoginActivity.EXTRA_USERNAME);
                    String refreshToken = data.getStringExtra(LoginActivity.EXTRA_REFRESH_TOKEN);
                    Tools.setAuthentication(this, accessToken, refreshToken, username);
                    Log.d("AccessToken",  accessToken);
                    loginSuccessful();
                } else {
                    Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public abstract void loginSuccessful();
}
