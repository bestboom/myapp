package com.app.dlike.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlike.R;
import com.squareup.picasso.Picasso;

/**
 * Created by moses on 8/29/18.
 */

public class ProfileViewActivity extends LoginRequestActivity {

    public static final String EXTRA_USERNAME = "username";
    private ImageView profileImageView;
    private TextView usernameTextView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileImageView = findViewById(R.id.profileImage);
        usernameTextView = findViewById(R.id.profileUsername);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String username = getIntent().getStringExtra(EXTRA_USERNAME);
        setTitle("Profile");

        Picasso.with(this)
                .load("https://steemitimages.com/u/" + username + "/avatar")
                .placeholder(R.drawable.profile)
                .into(profileImageView);

        usernameTextView.setText(username);
    }

    @Override
    public void loginSuccessful() {

    }
}
