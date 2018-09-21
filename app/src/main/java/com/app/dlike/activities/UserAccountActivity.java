package com.app.dlike.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlike.R;
import com.app.dlike.Tools;
import com.app.dlike.adapters.UserAccountViewPagerAdapter;
import com.app.dlike.fragments.PostsFragment;
import com.squareup.picasso.Picasso;

/**
 * Created by moses on 9/12/18.
 */

public class UserAccountActivity extends LoginRequestActivity {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private ImageView userAccountImage;
    private TextView userAccountName;
    private TabLayout tabLayout;
    private UserAccountViewPagerAdapter userAccountViewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.viewPager);
        userAccountImage = findViewById(R.id.profileImage);
        userAccountName = findViewById(R.id.profileUsername);
        tabLayout = findViewById(R.id.tabLayout);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("My Profile");

        viewPager.setAdapter(userAccountViewPagerAdapter = new UserAccountViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(userAccountViewPagerAdapter.getCount());

        tabLayout.setupWithViewPager(viewPager);

        String username = Tools.getUsername(this);

        Picasso.with(this)
                .load("https://steemitimages.com/u/" + username + "/avatar")
                .placeholder(R.drawable.profile)
                .into(userAccountImage);

        userAccountName.setText("@"+username);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_logout:
                logout();
                break;
        }
        return true;
    }

    private void logout() {
        new AlertDialog.Builder(UserAccountActivity.this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Tools.clearAuthentication(UserAccountActivity.this);
                        finish();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).create().show();
    }

    @Override
    public void loginSuccessful() {

    }
}
