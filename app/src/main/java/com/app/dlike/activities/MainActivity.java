package com.app.dlike.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dlike.R;
import com.app.dlike.Tools;
import com.app.dlike.fragments.PostsFragment;
import com.app.dlike.widgets.PostingFragment;
import com.squareup.picasso.Picasso;

public class MainActivity extends LoginRequestActivity {

    public static final int REQUEST_CODE_CREATE_POST = 1121;

    private PostingFragment postingFragment;
    private View postingFragmentLayout;
    private ViewGroup mainLayout;

    private ImageView userProfileImage;
    private TextView userProfileName;
    private MenuItem logoutItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainLayout = findViewById(R.id.mainLayout);
        postingFragmentLayout = findViewById(R.id.postingFragmentLayout);
        postingFragment = (PostingFragment) getSupportFragmentManager().findFragmentById(R.id.postingFragment);
        postingFragment.setLayouts(postingFragmentLayout, mainLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkLoginStatus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.userAccount) {
            if (!Tools.isLoggedIn(this)) {
                requestLogin();
            } else {
                Intent intent = new Intent(this, UserAccountActivity.class);
                startActivity(intent);
            }
        }
        return true;
    }


    public void openDialog(View view) {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this);

        LinearLayout ll = (LinearLayout) getLayoutInflater().inflate(R.layout.custom_dialog_post, null);

        RelativeLayout rlLink = ll.findViewById(R.id.rlLink);
        RelativeLayout rlUpload = ll.findViewById(R.id.mainLayout);
        RelativeLayout rlText = ll.findViewById(R.id.rlText);

        ImageView imgClose = ll.findViewById(R.id.imgClose);

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.cancel();
            }
        });

        rlLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.cancel();

                if (Tools.isLoggedIn(MainActivity.this)) {
                    Intent in = new Intent(MainActivity.this, PostActivity.class);
                    in.putExtra("type", "link");
                    startActivityForResult(in, REQUEST_CODE_CREATE_POST);
                } else {
                    requestLogin();
                }

            }
        });

        rlUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.cancel();
                if (Tools.isLoggedIn(MainActivity.this)) {
                    Intent in = new Intent(MainActivity.this, PostActivity.class);
                    in.putExtra("type", "upload");
                    startActivityForResult(in, REQUEST_CODE_CREATE_POST);
                } else {
                    requestLogin();
                }
            }
        });

        rlText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.cancel();
                if (Tools.isLoggedIn(MainActivity.this)) {
                    Intent in = new Intent(MainActivity.this, PostActivity.class);
                    in.putExtra("type", "text");
                    startActivityForResult(in, REQUEST_CODE_CREATE_POST);
                } else {
                    requestLogin();
                }
            }
        });


        bottomSheetDialog.setContentView(ll);

        bottomSheetDialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Tools.isLoggedIn(this)) {
            refresh();
        }
    }

    private void checkLoginStatus() {
//        boolean loggedIn = Tools.isLoggedIn(this);
//
//        logoutItem.setVisible(loggedIn);
//        if (loggedIn) {
//            Picasso.with(this)
//                    .load("https://steemitimages.com/u/" + Tools.getUsername(this) + "/avatar")
//                    .into(userProfileImage);
//            userProfileName.setText(Tools.getUsername(this));
//        } else {
//            userProfileName.setText("Login or Signup");
//            userProfileImage.setImageDrawable(getResources().getDrawable(R.drawable.profile));
//        }
    }

    private void requestLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST_CODE);
    }

    public void refresh() {
        PostsFragment postsFragment = (PostsFragment) getSupportFragmentManager().findFragmentById(R.id.postsFragment);

        assert postsFragment != null;
        postsFragment.loadDiscussions();
    }

    @Override
    public void loginSuccessful() {
        checkLoginStatus();
        refresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_POST) {
            if (resultCode == Activity.RESULT_OK) {
                postingFragment.show(data);
            }
        }
    }
}
