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
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;
import java.util.Collections;

public class LiveStreamActivity extends AppCompatActivity {

    private static final int REQ_PERMISSIONS = 100;

    // Full desktop Chrome UA — bypasses Google's "disallowed_useragent" 403 for OAuth
    private static final String DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

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

        // All live sessions render inside the in-app WebView (Chromium engine —
        // same renderer Chrome uses). The desktop UA + X-Requested-With strip
        // below is what stops Google's "browser not secure" detector. Meet
        // sign-in participants who still hit the wall can long-press the
        // Live card → Open in browser, which falls back to Chrome Custom Tab.
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

        // Escape hatch — if Google's "browser not secure" still trips and
        // sign-in is required, this button hands the live URL off to real Chrome.
        android.widget.ImageButton btnOpenBrowser = findViewById(R.id.btn_live_open_browser);
        if (btnOpenBrowser != null) {
            btnOpenBrowser.setOnClickListener(v -> {
                String url = currentLiveUrl();
                if (url != null) openInCustomTab(url);
            });
        }

        webView = findViewById(R.id.webview_live);
        setupWebView();

        if ("jitsi".equals(pendingPlatform)) {
            loadJitsi();
        } else if ("meet".equals(pendingPlatform)) {
            webView.loadUrl(pendingMeetUrl != null ? pendingMeetUrl : "https://meet.google.com");
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

        // Full desktop Chrome UA — Google checks both the UA string AND the
        // X-Requested-With header to detect WebViews. Just removing "; wv" is
        // no longer enough; we need a complete desktop UA.
        ws.setUserAgentString(DESKTOP_UA);

        // Strip X-Requested-With header — the other signal Google uses to detect WebViews
        if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
            WebSettingsCompat.setRequestedWithHeaderOriginAllowList(ws, Collections.emptySet());
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                // Keep Google OAuth and Meet navigation inside the WebView
                if (url.contains("google.com") || url.contains("gstatic.com")
                        || url.contains("youtube.com") || url.contains("jitsi")) {
                    return false;
                }
                return false;
            }
        });

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
        webView.loadUrl(jitsiUrl());
    }

    private String jitsiUrl() {
        String cfg = "#config.prejoinPageEnabled=false"
                + "&config.enableWelcomePage=false"
                + "&config.disableDeepLinking=true"
                + "&config.disableInviteFunctions=true";
        if (!pendingIsFaculty) {
            cfg += "&config.startWithVideoMuted=true"
                    + "&config.startWithAudioMuted=true";
        }
        return "https://meet.jit.si/" + pendingRoomId + cfg;
    }

    /** Returns the current live URL — used by the "open in browser" escape hatch. */
    private String currentLiveUrl() {
        if ("jitsi".equals(pendingPlatform)) return jitsiUrl();
        if ("meet".equals(pendingPlatform)) {
            return pendingMeetUrl != null ? pendingMeetUrl : "https://meet.google.com";
        }
        // YouTube — open the watch URL in Chrome (user's full YouTube app/site experience)
        return pendingYtUrl;
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
