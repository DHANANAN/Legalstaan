package com.legalstaan.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;

/**
 * Smooth in-app video player for public Drive files.
 *
 * Streams via Drive's public download endpoint (uc?export=download), which
 * for "Anyone with the link" folders 302-redirects to the actual mp4 served
 * from googleusercontent.com. ExoPlayer follows the redirect and plays it
 * natively with proper seek + buffering UI — no WebView, no Drive's broken
 * embedded player.
 *
 * On any error (restricted file, network blip, codec mismatch) we silently
 * fall back to {@link VideoActivity}'s WebView preview so the student never
 * sees a dead screen.
 */
@OptIn(markerClass = UnstableApi.class)
public class ExoPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_FILE_ID = "file_id";
    public static final String EXTRA_TITLE   = "title";

    private static final String BROWSER_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private PlayerView   playerView;
    private ExoPlayer    player;
    private View         loadingOverlay;
    private TextView     tvTitle;
    private ImageButton  btnRotate, btnBack;

    private boolean fellBack       = false;
    private boolean userIsLandscape = false;
    private final Handler ui = new Handler(Looper.getMainLooper());

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.apply(this);
        setContentView(R.layout.activity_exo_player);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        playerView     = findViewById(R.id.exo_player_view);
        loadingOverlay = findViewById(R.id.exo_loading_overlay);
        tvTitle        = findViewById(R.id.tv_player_title);
        btnRotate      = findViewById(R.id.btn_player_rotate);
        btnBack        = findViewById(R.id.btn_player_back);

        String title  = getIntent().getStringExtra(EXTRA_TITLE);
        String fileId = getIntent().getStringExtra(EXTRA_FILE_ID);
        tvTitle.setText(title != null ? title : "Video Lecture");

        btnBack.setOnClickListener(v -> finish());
        btnRotate.setOnClickListener(v -> toggleOrientation());

        if (fileId == null || fileId.isEmpty()) {
            finish();
            return;
        }
        initPlayer(fileId, title);
    }

    private void initPlayer(String fileId, String title) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        playerView.setControllerHideOnTouch(true);
        playerView.setControllerAutoShow(true);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);

        String url = "https://drive.google.com/uc?export=download&id=" + fileId + "&confirm=t";

        DefaultHttpDataSource.Factory httpFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent(BROWSER_UA)
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(20000);

        MediaItem item = new MediaItem.Builder()
                .setUri(Uri.parse(url))
                .setMimeType(MimeTypes.VIDEO_MP4)
                .build();

        MediaSource source = new ProgressiveMediaSource.Factory(httpFactory)
                .createMediaSource(item);

        player.setMediaSource(source);
        player.setPlayWhenReady(true);
        player.prepare();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                loadingOverlay.setVisibility(
                        state == Player.STATE_BUFFERING ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                if (fellBack) return;
                fellBack = true;
                ui.post(() -> {
                    Intent fb = new Intent(ExoPlayerActivity.this, VideoActivity.class);
                    fb.putExtra(VideoActivity.EXTRA_FILE_ID, fileId);
                    fb.putExtra(VideoActivity.EXTRA_TITLE,   title);
                    startActivity(fb);
                    finish();
                });
            }
        });
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
        applySystemUi(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    private void applySystemUi(boolean landscape) {
        WindowInsetsControllerCompat ctrl =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (ctrl == null) return;
        if (landscape) {
            ctrl.hide(WindowInsetsCompat.Type.systemBars());
            ctrl.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            ctrl.show(WindowInsetsCompat.Type.systemBars());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        applySystemUi(getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        super.onDestroy();
    }
}
