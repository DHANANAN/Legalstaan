package com.legalstaan.app;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;
import java.util.Collections;

public class VideoActivity extends AppCompatActivity {
    public static final String EXTRA_FILE_ID = "file_id";
    public static final String EXTRA_TITLE   = "title";

    private WebView webView;
    private ProgressBar progressBar;
    private FrameLayout fullscreenContainer;
    private View layoutNormal;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View customView;
    private boolean userIsLandscape = false;

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

        // Manual rotate button on the toolbar — gives users control over orientation.
        ImageButton btnRotate = findViewById(R.id.btn_video_rotate);
        if (btnRotate != null) btnRotate.setOnClickListener(v -> toggleOrientation());

        webView     = findViewById(R.id.web_view_player);
        progressBar = findViewById(R.id.video_progress);

        // Remove X-Requested-With header so Google doesn't detect WebView
        if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
            WebSettingsCompat.setRequestedWithHeaderOriginAllowList(
                    webView.getSettings(), Collections.emptySet());
        }

        setupPlayer();
        loadVideo();
        applyImmersive(getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE);
    }

    // Desktop Chrome UA — tricks Google Drive into serving video without "virus scan" wall
    private static final String DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

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
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString(DESKTOP_UA);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                view.evaluateJavascript(
                        "(function(){var b=document.getElementById('uc-download-link');" +
                                "if(b)b.click();})()", null);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
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

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView         = view;
                customViewCallback = callback;

                layoutNormal.setVisibility(View.GONE);
                fullscreenContainer.setVisibility(View.VISIBLE);
                fullscreenContainer.addView(view);

                // Force landscape when Drive's player goes fullscreen.
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                applyImmersive(true);
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;

                fullscreenContainer.removeView(customView);
                fullscreenContainer.setVisibility(View.GONE);
                layoutNormal.setVisibility(View.VISIBLE);

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                applyImmersive(false);

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

    private void toggleOrientation() {
        userIsLandscape = !userIsLandscape;
        setRequestedOrientation(userIsLandscape
                ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        applyImmersive(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    /** Hide system bars + toolbar in landscape so the video gets the whole screen. */
    private void applyImmersive(boolean landscape) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), !landscape);
        WindowInsetsControllerCompat ctrl =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (ctrl != null) {
            if (landscape) {
                ctrl.hide(WindowInsetsCompat.Type.systemBars());
                ctrl.setSystemBarsBehavior(
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            } else {
                ctrl.show(WindowInsetsCompat.Type.systemBars());
            }
        }
        Toolbar tb = findViewById(R.id.toolbar_video);
        if (tb != null) tb.setVisibility(landscape ? View.GONE : View.VISIBLE);
        if (landscape) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
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
        if (customView != null) {
            WebChromeClient.CustomViewCallback cb = customViewCallback;
            if (cb != null) cb.onCustomViewHidden();
            return;
        }
        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE) {
            // First tap rotates back to portrait — second tap exits.
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            userIsLandscape = false;
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
