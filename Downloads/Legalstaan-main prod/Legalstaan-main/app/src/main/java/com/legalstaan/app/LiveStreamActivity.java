package com.legalstaan.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LiveStreamActivity extends AppCompatActivity {

    private WebView webView;
    private String pendingUrl;
    private static final int PERM_REQ = 201;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.apply(this);
        setContentView(R.layout.activity_live_stream);

        String platform  = getIntent().getStringExtra("platform");
        String roomId    = getIntent().getStringExtra("room_id");
        String ytUrl     = getIntent().getStringExtra("youtube_url");
        String title     = getIntent().getStringExtra("title");
        boolean isFaculty = getIntent().getBooleanExtra("is_faculty", false);

        Toolbar toolbar = findViewById(R.id.toolbar_live);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title != null ? title : "Live Session");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        webView = findViewById(R.id.webview_live);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                // Grant WebRTC camera/mic access at the WebView level
                request.grant(request.getResources());
            }
        });

        String url;
        if ("jitsi".equals(platform)) {
            StringBuilder cfg = new StringBuilder("#config.prejoinPageEnabled=false");
            cfg.append("&config.enableWelcomePage=false");
            cfg.append("&config.disableDeepLinking=true");
            if (!isFaculty) {
                // Students join muted to save bandwidth
                cfg.append("&config.startWithVideoMuted=true");
                cfg.append("&config.startWithAudioMuted=true");
            }
            url = "https://meet.jit.si/" + roomId + cfg;
        } else {
            // YouTube embed — adaptive quality works down to 240p on poor connections
            String videoId = extractYouTubeId(ytUrl);
            url = "https://www.youtube.com/embed/" + videoId
                    + "?autoplay=1&rel=0&modestbranding=1&playsinline=1";
        }

        if ("jitsi".equals(platform)) {
            // Request Android-level camera/mic permissions before loading Jitsi WebRTC
            pendingUrl = url;
            requestMediaPermissions();
        } else {
            webView.loadUrl(url);
        }
    }

    private void requestMediaPermissions() {
        boolean camOk  = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        boolean micOk  = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
        if (camOk && micOk) {
            webView.loadUrl(pendingUrl);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO},
                    PERM_REQ);
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, @androidx.annotation.NonNull String[] perms,
                                           @androidx.annotation.NonNull int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (req == PERM_REQ && pendingUrl != null) {
            webView.loadUrl(pendingUrl);
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
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroy();
    }
}
