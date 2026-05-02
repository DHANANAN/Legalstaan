package com.legalstaan.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LiveStreamActivity extends AppCompatActivity {

    private static final int REQ_PERMISSIONS = 100;

    private WebView webView;
    private FrameLayout fullscreenContainer;
    private View layoutNormal;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View customView;

    // Saved for after permission grant
    private String pendingPlatform;
    private String pendingRoomId;
    private String pendingYtUrl;
    private String pendingMeetUrl;
    private String pendingTitle;
    private boolean pendingIsFaculty;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.apply(this);

        pendingPlatform  = getIntent().getStringExtra("platform");
        pendingRoomId    = getIntent().getStringExtra("room_id");
        pendingYtUrl     = getIntent().getStringExtra("youtube_url");
        pendingMeetUrl   = getIntent().getStringExtra("meet_url");
        pendingTitle     = getIntent().getStringExtra("title");
        pendingIsFaculty = getIntent().getBooleanExtra("is_faculty", false);

        if ("meet".equals(pendingPlatform)) {
            // Google Meet requires Google auth → open in Custom Tab (user stays signed in)
            openInCustomTab(pendingMeetUrl != null ? pendingMeetUrl : "https://meet.google.com");
            finish();
            return;
        }

        // For both Jitsi and YouTube, request camera+mic permissions first
        if (!permissionsGranted()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    REQ_PERMISSIONS);
        } else {
            launchStream();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSIONS) launchStream();
    }

    private boolean permissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void launchStream() {
        setContentView(R.layout.activity_live_stream_full);

        layoutNormal        = findViewById(R.id.layout_live_normal);
        fullscreenContainer = findViewById(R.id.fullscreen_live_container);

        Toolbar toolbar = findViewById(R.id.toolbar_live);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(pendingTitle != null ? pendingTitle : "Live Session");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.webview_live);
        setupWebView();

        if ("jitsi".equals(pendingPlatform)) {
            loadJitsi();
        } else {
            loadYoutube();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setAllowFileAccess(true);
        // WebRTC getUserMedia support
        ws.setDatabaseEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        webView.setWebChromeClient(new WebChromeClient() {

            // Grant camera + mic to the web page (Jitsi Meet needs this)
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) { callback.onCustomViewHidden(); return; }
                customView         = view;
                customViewCallback = callback;
                layoutNormal.setVisibility(View.GONE);
                fullscreenContainer.setVisibility(View.VISIBLE);
                fullscreenContainer.addView(view);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;
                fullscreenContainer.removeView(customView);
                fullscreenContainer.setVisibility(View.GONE);
                layoutNormal.setVisibility(View.VISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                customViewCallback.onCustomViewHidden();
                customView = null; customViewCallback = null;
            }
        });
    }

    private void loadJitsi() {
        String cfg = "#config.prejoinPageEnabled=false"
                + "&config.enableWelcomePage=false"
                + "&config.disableDeepLinking=true"
                + "&config.disableInviteFunctions=true";
        if (!pendingIsFaculty) {
            cfg += "&config.startWithVideoMuted=true"
                    + "&config.startWithAudioMuted=true";
        }
        // Show a help button in case WebRTC audio/video doesn't work for the user
        Toast.makeText(this,
                "If mic/camera doesn't work, tap ⋮ → Open in browser",
                Toast.LENGTH_LONG).show();
        webView.loadUrl("https://meet.jit.si/" + pendingRoomId + cfg);
    }

    private void loadYoutube() {
        String videoId = extractYouTubeId(pendingYtUrl);
        webView.loadUrl("https://www.youtube.com/embed/" + videoId
                + "?autoplay=1&rel=0&modestbranding=1&playsinline=1");
    }

    private void openInCustomTab(String url) {
        try {
            int color = ContextCompat.getColor(this, R.color.primaryColor);
            CustomTabColorSchemeParams params = new CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(color).build();
            CustomTabsIntent intent = new CustomTabsIntent.Builder()
                    .setDefaultColorSchemeParams(params)
                    .setShowTitle(false)
                    .build();
            // Make Custom Tab start as full screen
            intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.launchUrl(this, Uri.parse(url));
        } catch (Exception e) {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
            catch (Exception ignored) {}
        }
    }

    private String extractYouTubeId(String url) {
        if (url == null || url.isEmpty()) return "";
        if (url.contains("youtu.be/")) {
            int s = url.indexOf("youtu.be/") + 9;
            int e = url.indexOf("?", s);
            return e == -1 ? url.substring(s) : url.substring(s, e);
        }
        if (url.contains("watch?v=")) {
            int s = url.indexOf("watch?v=") + 8;
            int e = url.indexOf("&", s);
            return e == -1 ? url.substring(s) : url.substring(s, e);
        }
        if (url.contains("/live/")) {
            int s = url.indexOf("/live/") + 6;
            int e = url.indexOf("?", s);
            return e == -1 ? url.substring(s) : url.substring(s, e);
        }
        if (url.contains("/embed/")) {
            int s = url.indexOf("/embed/") + 7;
            int e = url.indexOf("?", s);
            return e == -1 ? url.substring(s) : url.substring(s, e);
        }
        return url;
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            if (customViewCallback != null) customViewCallback.onCustomViewHidden();
            return;
        }
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) { webView.stopLoading(); webView.destroy(); }
        super.onDestroy();
    }
}
