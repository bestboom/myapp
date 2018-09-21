package com.app.dlike.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.app.dlike.R;
import com.app.dlike.Tools;
import com.squareup.picasso.Picasso;
import com.surblime.richtexteditor.Editor;
import com.surblime.richtexteditor.EditorTools;

import jp.wasabeef.richeditor.RichEditor;

/**
 * Created by moses on 8/30/18.
 */

public class WritePostActivity extends LoginRequestActivity implements RichEditor.OnTextChangeListener {

    public static final String EXTRA_TITLE = "title";
    private Toolbar toolbar;
    private Editor editor;
    private EditorTools editorTools;
    private ImageView userProfileImage;
    private MenuItem sendMenuItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        setTitle(title == null || title.isEmpty() ? "Write Post" : title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.close);

        String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);

        editor = findViewById(R.id.postText);
        editor.setOnTextChangeListener(this);
        editorTools = findViewById(R.id.editorTools);
        userProfileImage = findViewById(R.id.userProfileImage);

        editorTools.setEditor(editor);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
            }
        });

        editor.setText(text);

        Picasso.with(this)
                .load("https://steemitimages.com/u/" + Tools.getUsername(this) + "/avatar")
                .placeholder(R.drawable.profile)
                .into(userProfileImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_write_post, menu);
        sendMenuItem = menu.findItem(R.id.menu_post);
        sendMenuItem.setEnabled(false);
        return true;
    }

    private void send() {
        String text = editor.getText();
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_TEXT, text);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editor.getWindowToken(), 0);
    }

    public void clear() {
        if (editor.getText().trim().isEmpty()) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_title_discard_post)
                    .setMessage(R.string.dialog_message_save_as_draft)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                        }
                    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();
        }
    }

    @Override
    public void onBackPressed() {
        send();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_post:
                send();
                break;
        }
        return false;
    }

    @Override
    public void loginSuccessful() {

    }

    @Override
    public void onTextChange(String text) {
        sendMenuItem.setEnabled(!text.isEmpty());
    }
}
