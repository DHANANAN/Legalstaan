package com.legalstaan.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class VideoActivity extends AppCompatActivity {
    public static final String EXTRA_FILE_ID = "file_id";
    public static final String EXTRA_TITLE   = "title";

    private WebView webView;
    private ProgressBar progressBar;
    private FrameLayout fullscreenContainer;
    private View layoutNormal;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View customView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        layoutNormal       = findViewById(R.id.layout_normal);
        fullscreenContainer = findViewById(R.id.fullscreen_container);

        Toolbar toolbar = findViewById(R.id.toolbar_video);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String title = getIntent().getStringExtra(EXTRA_TITLE);
            getSupportActionBar().setTitle(title != null ? title : "Video Lecture");
        }

        webView     = findViewById(R.id.web_view_player);
        progressBar = findViewById(R.id.video_progress);

        setupPlayer();
        loadVideo();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupPlayer() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setSupportZoom(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request != null && request.isForMainFrame()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(VideoActivity.this,
                            "Could not load video. Check your internet connection.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            // Called when Drive video player enters fullscreen (landscape tap)
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView         = view;
                customViewCallback = callback;

                // Hide normal layout, show fullscreen container
                layoutNormal.setVisibility(View.GONE);
                fullscreenContainer.setVisibility(View.VISIBLE);
                fullscreenContainer.addView(view);

                // Go fully immersive
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            // Called when Drive video player exits fullscreen
            @Override
            public void onHideCustomView() {
                if (customView == null) return;

                fullscreenContainer.removeView(customView);
                fullscreenContainer.setVisibility(View.GONE);
                layoutNormal.setVisibility(View.VISIBLE);

                // Restore UI
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                customViewCallback.onCustomViewHidden();
                customView         = null;
                customViewCallback = null;
            }
        });
    }

    private void loadVideo() {
        String fileId = getIntent().getStringExtra(EXTRA_FILE_ID);
        if (fileId == null || fileId.isEmpty()) {
            finish();
            return;
        }
        webView.loadUrl("https://drive.google.com/file/d/" + fileId + "/preview");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Exit fullscreen first if active
        if (customView != null) {
            WebChromeClient.CustomViewCallback cb = customViewCallback;
            // onHideCustomView will be called by WebChromeClient
            if (cb != null) cb.onCustomViewHidden();
            return;
        }
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause JS execution to save CPU when backgrounded
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.destroy();
        }
        super.onDestroy();
    }
}
