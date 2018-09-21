package com.app.dlike.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dlike.R;
import com.app.dlike.Tools;
import com.app.dlike.api.DLike;
import com.app.dlike.api.models.Categories;
import com.app.dlike.api.models.Draft;
import com.app.dlike.api.models.WebCrawlerResponse;
import com.app.dlike.jobs.SchedulePostJob;
import com.app.dlike.widgets.AddPhotoDialogFragment;
import com.app.dlike.widgets.SchedulePostDialogFragment;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PostActivity extends AppCompatActivity implements AddPhotoDialogFragment.ImageChooseListener, SchedulePostDialogFragment.TimeChooseListener {

    public static final String EXTRA_COMMUNITY = "community";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_DESCRIPTION = "description";
    public static final String EXTRA_TAGS = "tags";
    public static final String EXTRA_IMAGE = "image";
    public static final String EXTRA_UPVOTE = "upvote";
    public static final String EXTRA_EXTERNAL_URL = "external_url";
    public static final String EXTRA_REWARD_WEIGHT = "reward_weight";

    private static final int REQUEST_CODE_WRITE_POST = 12;
    private String type = "";
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 100;

    private AppBarLayout appBarLayout;
    private Context mContext;
    private EditText linkEditText, postDescriptionTextView;
    private WebView postDescriptionWebView;
    private ImageView collapsingImageView;
    private File file;
    private LinearLayout bottomLayout;
    private FloatingActionButton fabAddPhoto;
    private Button nextButton;
    private EditText tagEditText;
    private LinearLayout llMain, ll_link;
    private View viewLine;
    private Toolbar toolbar;
    private String description = "";
    private String link = "";
    private ProgressBar btnNextProgress;
    private TextView titleTextView;
    private AutoCompleteTextView communityList;
    private Spinner rewardSpinner;
    private SwitchCompat upvoteSwitch;

    private AddPhotoDialogFragment addPhotoDialogFragment = new AddPhotoDialogFragment();
    private WebCrawlerResponse webCrawlerResponse;
    private MenuItem postMenuItem;
    private SchedulePostDialogFragment schedulePostDialogFragment = new SchedulePostDialogFragment();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        type = getIntent().getStringExtra("type");

        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        init();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post, menu);

        this.postMenuItem = menu.findItem(R.id.menu_post);
        if (type != null) {
            this.postMenuItem.setVisible(!type.equals("link"));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_post:
                sendPost();
                break;
            case R.id.menu_save_draft:
                if (hasChanges()) {
                    saveDraft();
                } else {
                    Toast.makeText(mContext, "Nothing to save", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_schedule_post:
                if (hasChanges()) {
                    schedulePostDialogFragment.show(getSupportFragmentManager(), SchedulePostDialogFragment.class.getSimpleName());
                } else {
                    Toast.makeText(mContext, "Nothing to schedule!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    private void sendPost() {
        String imageLink = webCrawlerResponse == null ? "" : webCrawlerResponse.imgUrl;
        String community = communityList.getText().toString().trim();
        String title = titleTextView.getText().toString().trim();
        String description = this.description;
        String tags = tagEditText.getText().toString().trim().replaceAll(" ", ",");
        String reward = rewardSpinner.getSelectedItem().toString();
        String externalURl = webCrawlerResponse == null ? "https://dlike.io/@" + Tools.getUsername(this) : webCrawlerResponse.url;
        boolean upVote = upvoteSwitch.isChecked();
        String fileLink = file == null ? (imageLink == null || imageLink.isEmpty() ? "" : imageLink) : file.getAbsolutePath();
        int rewardWeight = 0;
        Pattern pattern = Pattern.compile("^\\w+\\s+\\((\\d+)%\\)");
        Matcher matcher = pattern.matcher(reward);
        if (matcher.find()) {
            rewardWeight = Integer.parseInt(matcher.group(1));
        }


        if (community.isEmpty()) {
            Toast.makeText(this, "Please choose a community", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tags.isEmpty()) {
            Toast.makeText(this, "Please enter at least one tag", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_COMMUNITY, community);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_EXTERNAL_URL, externalURl);
        intent.putExtra(EXTRA_DESCRIPTION, description);
        intent.putExtra(EXTRA_TAGS, tags);
        intent.putExtra(EXTRA_REWARD_WEIGHT, rewardWeight);
        intent.putExtra(EXTRA_UPVOTE, upVote);
        intent.putExtra(EXTRA_IMAGE, fileLink);

        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public boolean hasChanges() {
        String community = communityList.getText().toString().trim();
        String title = titleTextView.getText().toString().trim();
        String tags = tagEditText.getText().toString().trim().replaceAll(" ", ",");

        return !(community.isEmpty() && title.isEmpty() && tags.isEmpty() && description.isEmpty());
    }

    @Override
    public void onBackPressed() {
        if (hasChanges()) {
            confirmSaveAsDraft();
        } else {
            super.onBackPressed();
        }
    }

    private void confirmSaveAsDraft() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_title_discard_post)
                .setMessage(R.string.dialog_message_save_as_draft)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // save as draft
                        saveDraft();
                    }
                }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        }).create().show();

    }

    private void saveDraft() {
        saveDraft(0);
    }

    private void saveDraft(final long schedule) {

        final Draft draft = new Draft();
        String imageLink = webCrawlerResponse == null ? "" : webCrawlerResponse.imgUrl;

        draft.category = communityList.getText().toString().trim();
        draft.title = titleTextView.getText().toString().trim();
        draft.post = this.description;
        draft.tags = tagEditText.getText().toString().trim().replaceAll(" ", ",");
        String reward = rewardSpinner.getSelectedItem().toString();
        draft.extUrl = webCrawlerResponse == null ? "https://dlike.io/@" + Tools.getUsername(this) : webCrawlerResponse.url;
        draft.upvote = upvoteSwitch.isChecked() ? 1 : 0;
        draft.image = file == null ? (imageLink == null || imageLink.isEmpty() ? "" : imageLink) : file.getAbsolutePath();
        int rewardWeight = 0;
        Pattern pattern = Pattern.compile("^\\w+\\s+\\((\\d+)%\\)");
        Matcher matcher = pattern.matcher(reward);
        if (matcher.find()) {
            rewardWeight = Integer.parseInt(matcher.group(1));
        }
        draft.rewardOption = rewardWeight;
        draft.authorName = Tools.getUsername(this);
        draft.accessCode = Tools.getAccessToken(this);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        draft.scheduledAt = schedule == 0 ? null : simpleDateFormat.format(new Date(schedule));
        if (schedule > 0) {
            if (draft.category.isEmpty()) {
                Toast.makeText(this, "Please choose a community", Toast.LENGTH_SHORT).show();
                return;
            }
            if (draft.title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }
            if (draft.tags.isEmpty()) {
                Toast.makeText(this, "Please enter at least one tag", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Log.d("Schedule Time", draft.scheduledAt + "");

        Map<String, RequestBody> body = new HashMap<>();
        body.put("code", RequestBody.create(MediaType.parse("text/plain"), draft.accessCode));
        body.put("category", RequestBody.create(MediaType.parse("text/plain"), draft.category));
        body.put("title", RequestBody.create(MediaType.parse("text/plain"), draft.title));
        body.put("exturl", RequestBody.create(MediaType.parse("text/plain"), draft.extUrl));
        body.put("post", RequestBody.create(MediaType.parse("text/plain"), draft.post));
        body.put("image", RequestBody.create(MediaType.parse("text/plain"), draft.image));
        body.put("reward_option", RequestBody.create(MediaType.parse("text/plain"), draft.rewardOption + ""));
        body.put("scheduled_at", RequestBody.create(MediaType.parse("text/plain"), draft.scheduledAt + ""));
        body.put("upvote", RequestBody.create(MediaType.parse("text/plain"), draft.upvote + ""));
        body.put("tags", RequestBody.create(MediaType.parse("text/plain"), draft.tags));

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(schedule == 0 ? "Saving draft..." : "Scheduling Post...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dlike.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofit.create(DLike.class)
                .createDraft(body)
                .enqueue(new Callback<Draft.DraftResponse>() {
                    @Override
                    public void onResponse(Call<Draft.DraftResponse> call, Response<Draft.DraftResponse> response) {
                        progressDialog.dismiss();
                        Draft.DraftResponse draftResponse = response.body();

                        if (draftResponse == null || draftResponse.error) {
                            Toast.makeText(mContext, schedule == 0 ? "Unable to save draft at the moment!" : "Unable to schedule at the moment!", Toast.LENGTH_SHORT).show();
                            Log.d("Moses", draftResponse == null ? "None" : draftResponse.description + "");
                        } else {
                            Toast.makeText(mContext, schedule == 0 ? "Draft saved!" : "Post scheduled successfully!", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_CANCELED);

                            if (schedule > 0) {
                                SchedulePostJob.scheduleJob(draft, schedule);
                            }
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<Draft.DraftResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        Toast.makeText(mContext, schedule == 0 ? "Unable to save draft at the moment!" : "Unable to schedule at the moment!", Toast.LENGTH_SHORT).show();
                        Log.d("Moses", "onFailure", t);
                    }
                });
    }

    public void startWritePostActivity(View view) {
        Intent intent = new Intent(PostActivity.this, WritePostActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, description);
        startActivityForResult(intent, REQUEST_CODE_WRITE_POST);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        mContext = PostActivity.this;
        linkEditText = findViewById(R.id.etLink);
        postDescriptionWebView = findViewById(R.id.postDescriptionWebView);
        collapsingImageView = findViewById(R.id.collapsingImageView);
        fabAddPhoto = findViewById(R.id.fabAddPhoto);
        nextButton = findViewById(R.id.btnNext);
        ll_link = findViewById(R.id.ll_link);
        llMain = findViewById(R.id.mainLayout);
        bottomLayout = findViewById(R.id.llBottom);
        viewLine = findViewById(R.id.viewLine);
        postDescriptionTextView = findViewById(R.id.postDescriptionTextView);
        btnNextProgress = findViewById(R.id.btnNextProgress);
        titleTextView = findViewById(R.id.titleTextView);
        communityList = findViewById(R.id.communityList);
        tagEditText = findViewById(R.id.tagEditText);
        rewardSpinner = findViewById(R.id.rewardSpinner);
        upvoteSwitch = findViewById(R.id.upvoteSwitch);

        schedulePostDialogFragment.setTimeChooseListener(this);

        communityList.setText(getIntent().getStringExtra(EXTRA_COMMUNITY));
        titleTextView.setText(getIntent().getStringExtra(EXTRA_TITLE));
        this.description = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        this.description = this.description == null ? "" : this.description;
        postDescriptionWebView.loadData(description, "text/html; charset=utf-8", "UTF-8");
        postDescriptionTextView.setVisibility(description.trim().isEmpty() ? View.VISIBLE : View.GONE);
        String tagText = getIntent().getStringExtra(EXTRA_TAGS);
        tagEditText.setText(tagText == null ? "" : tagText.replace(",", " "));
        List<String> rewards = Arrays.asList(getResources().getStringArray(R.array.reward));
        int weight = getIntent().getIntExtra(EXTRA_REWARD_WEIGHT, 0);
        for (int i = 0; i < rewards.size(); i++) {
            if (rewards.get(i).matches("\\w+\\s\\(" + weight + "%\\)")) {
                rewardSpinner.setSelection(i);
            }
        }
        String externalURL = getIntent().getStringExtra(EXTRA_EXTERNAL_URL);
        String imageURL = getIntent().getStringExtra(EXTRA_IMAGE);

        if (imageURL != null && imageURL.startsWith("http")) {
            webCrawlerResponse = new WebCrawlerResponse();
            webCrawlerResponse.imgUrl = imageURL;
            Picasso.with(mContext).load(webCrawlerResponse.imgUrl).placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder).into(collapsingImageView);
        }
        if (externalURL != null && !externalURL.isEmpty()) {
            webCrawlerResponse = new WebCrawlerResponse();
            webCrawlerResponse.url = externalURL;
        }
        if (imageURL != null && !imageURL.isEmpty() && !imageURL.startsWith("http")) {
            file = new File(imageURL);
            onImageChosen(file);
        }
        upvoteSwitch.setChecked(getIntent().getBooleanExtra(EXTRA_UPVOTE, false));


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dlike.io/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofit.create(DLike.class)
                .getCategories().enqueue(new Callback<Categories>() {
            @Override
            public void onResponse(Call<Categories> call, Response<Categories> response) {
                communityList.setAdapter(new ArrayAdapter<>(PostActivity.this, android.R.layout.simple_dropdown_item_1line, response.body().categories));
            }

            @Override
            public void onFailure(Call<Categories> call, Throwable t) {

            }
        });

        type = type == null ? "text" : type;
        if (type.equalsIgnoreCase("link")) {
            ll_link.setVisibility(View.VISIBLE);
            llMain.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.GONE);
            setTitle("Link");
            fabAddPhoto.setVisibility(View.GONE);
            linkEditText.setVisibility(View.VISIBLE);
            if (postMenuItem != null) {
                postMenuItem.setVisible(false);
            }
        } else if (type.equalsIgnoreCase("text")) {
            linkEditText.setHint("Description");
            setTitle("Text");
            postDescriptionWebView.setVisibility(View.VISIBLE);
        } else {
            setTitle("Photo");
            addPhotoDialogFragment.show(getSupportFragmentManager(), false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkAndAddPermission();
            }
        }

//        rlCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                rlUpload.setVisibility(View.VISIBLE);
//                collapsingImageView.setVisibility(View.GONE);
//            }
//        });

        fabAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPhotoDialogFragment.show(getSupportFragmentManager(), file != null);
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (postMenuItem != null) {
                    postMenuItem.setVisible(true);
                }
                final String link = linkEditText.getText().toString();
                if (!link.trim().isEmpty()) {
                    nextButton.setVisibility(View.INVISIBLE);
                    btnNextProgress.setVisibility(View.VISIBLE);
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://dlike.io/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    DLike dLike = retrofit.create(DLike.class);
                    dLike.crawlWeb(RequestBody.create(MediaType.parse("text/plain"), link))
                            .enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    ResponseBody responseBody = response.body();
                                    if (responseBody == null) {
                                        linkError();
                                    } else {
                                        PostActivity.this.webCrawlerResponse = new Gson().fromJson(responseBody.charStream(), WebCrawlerResponse.class);
                                        ll_link.setVisibility(View.GONE);
                                        llMain.setVisibility(View.VISIBLE);
                                        bottomLayout.setVisibility(View.VISIBLE);
                                        collapsingImageView.setVisibility(View.VISIBLE);
                                        fabAddPhoto.setVisibility(View.GONE);

                                        description = webCrawlerResponse.description;

                                        postDescriptionWebView.loadData(description, "text/html; charset=utf-8", "UTF-8");
                                        postDescriptionTextView.setVisibility(description.trim().isEmpty() ? View.VISIBLE : View.GONE);

                                        titleTextView.setText(webCrawlerResponse.title);

                                        PostActivity.this.link = webCrawlerResponse.url;

                                        Picasso.with(mContext).load(webCrawlerResponse.imgUrl).placeholder(R.drawable.placeholder)
                                                .error(R.drawable.placeholder).into(collapsingImageView);
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    linkError();
                                }
                            });
                }
            }
        });
        addPhotoDialogFragment.setImageChooseListener(this);
    }

    private void linkError() {
        nextButton.setVisibility(View.VISIBLE);
        btnNextProgress.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Unable to get link information. Kindly try again!", Toast.LENGTH_SHORT).show();
    }

    private void checkAndAddPermission() {
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();

        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("WriteExternalStorage");
        if (!addPermission(permissionsList, Manifest.permission.CAMERA))
            permissionsNeeded.add("Camera");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                ActivityCompat.requestPermissions(PostActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(PostActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        }

    }

    // Ask for permission
    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onImageChosen(File file) {
        this.file = file;
        collapsingImageView.setVisibility(View.VISIBLE);

        Picasso.with(mContext).load(Uri.fromFile(file)).into(collapsingImageView);
        bottomLayout.setVisibility(View.VISIBLE);

        viewLine.setVisibility(View.GONE);
    }

    @Override
    public void onRemoveImage() {
        this.file = null;
        collapsingImageView.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_WRITE_POST:
                if (resultCode == RESULT_OK) {
                    description = data.getStringExtra(Intent.EXTRA_TEXT);
                    postDescriptionTextView.setVisibility(View.GONE);
                } else {
                    description = "";
                }
                postDescriptionWebView.loadData(description, "text/html; charset=utf-8", "UTF-8");
                postDescriptionTextView.setVisibility(description.trim().isEmpty() ? View.VISIBLE : View.GONE);
                break;
        }
    }

    @Override
    public void timeChosen(Calendar time) {
        Log.d("Schedule Time", time.getTime().toGMTString());
        saveDraft(time.getTimeInMillis());
    }
}
