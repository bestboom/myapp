package com.app.dlike;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;

/**
 * Created by moses on 8/22/18.
 */

public class AuthCallback extends AppCompatActivity {

    private Toolbar toolbar;


    public static final String EXTRA_CODE = "code";
    public static final String URL = "https://v2.steemconnect.com/oauth2/authorize?client_id=dlike.android&response_type=code&redirect_uri=https://dlike.io/callback-android.php&scope=login%2Cvote%2Ccomment%2Ccomment_options%2Ccustom_json%2Coffline";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_callback);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(webViewClient);


        webView.loadUrl(URL);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (url.startsWith("https://dlike.io/callback-android.php")) {
                Uri uri = Uri.parse(url);
                String code = uri.getQueryParameter("code");

                Intent intent = new Intent();
                intent.putExtra(EXTRA_CODE, code);

                Log.d("Token: ", code);

                setResult(RESULT_OK, intent);
                finish();
            }
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return false;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(final WebView view, WebResourceRequest request) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toolbar.setTitle(view.getTitle());
                    toolbar.setSubtitle(view.getUrl());
                }
            });
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    };
}
