package com.legalstaan.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

public class LiveStreamActivity extends AppCompatActivity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.apply(this);

        String platform   = getIntent().getStringExtra("platform");
        String roomId     = getIntent().getStringExtra("room_id");
        String ytUrl      = getIntent().getStringExtra("youtube_url");
        String title      = getIntent().getStringExtra("title");
        boolean isFaculty = getIntent().getBooleanExtra("is_faculty", false);

        if ("jitsi".equals(platform)) {
            // Android WebView sandboxes both WebRTC camera/mic and Google OAuth —
            // Chrome Custom Tabs uses the real Chrome engine so both work correctly.
            StringBuilder cfg = new StringBuilder("#config.prejoinPageEnabled=false");
            cfg.append("&config.enableWelcomePage=false");
            cfg.append("&config.disableDeepLinking=true");
            if (!isFaculty) {
                cfg.append("&config.startWithVideoMuted=true");
                cfg.append("&config.startWithAudioMuted=true");
            }
            openInCustomTab("https://meet.jit.si/" + roomId + cfg);
            finish();
            return;
        }

        // YouTube Live — WebView works fine for read-only embed playback
        setContentView(R.layout.activity_live_stream);

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
                request.grant(request.getResources());
            }
        });

        String videoId = extractYouTubeId(ytUrl);
        webView.loadUrl("https://www.youtube.com/embed/" + videoId
                + "?autoplay=1&rel=0&modestbranding=1&playsinline=1");
    }

    private void openInCustomTab(String url) {
        try {
            int color = ContextCompat.getColor(this, R.color.primaryColor);
            CustomTabColorSchemeParams params = new CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(color)
                    .build();
            CustomTabsIntent intent = new CustomTabsIntent.Builder()
                    .setDefaultColorSchemeParams(params)
                    .setShowTitle(true)
                    .build();
            intent.launchUrl(this, Uri.parse(url));
        } catch (Exception e) {
            // Fallback: open in whatever browser the device has
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception ignored) {}
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
