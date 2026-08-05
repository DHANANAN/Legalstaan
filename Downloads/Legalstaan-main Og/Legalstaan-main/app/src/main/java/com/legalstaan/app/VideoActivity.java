package com.legalstaan.app;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;

public class VideoActivity extends AppCompatActivity {
    public static final String EXTRA_FILE_ID = "file_id";
    public static final String EXTRA_TITLE = "title";

    private PlayerView playerView;
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        
        playerView = findViewById(R.id.player_view);
        playerView.setKeepScreenOn(true);
        initializePlayer();
        loadVideo();
    }

    private void initializePlayer() {
        DefaultHttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setUserAgent("Legalstaan/1.34");

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(httpDataSourceFactory))
                .build();
        player.setRepeatMode(Player.REPEAT_MODE_OFF);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(VideoActivity.this, "Playback failed. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
        playerView.setPlayer(player);
    }

    private void loadVideo() {
        String driveId = getIntent().getStringExtra(EXTRA_FILE_ID);
        String title = getIntent().getStringExtra(EXTRA_TITLE);

        if (getSupportActionBar() != null && title != null) {
            getSupportActionBar().setTitle(title);
        }

        if (driveId == null || driveId.isEmpty()) {
            Toast.makeText(this, "No video ID provided.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri videoUri = new Uri.Builder()
                    .scheme("https")
                    .authority("www.googleapis.com")
                    .appendPath("drive")
                    .appendPath("v3")
                    .appendPath("files")
                    .appendPath(driveId)
                    .appendQueryParameter("alt", "media")
                    .appendQueryParameter("key", "AIzaSyD5ik_V30tAhvEEuS7aXPkGrOCZnrxzNck")
                    .build();

            MediaItem mediaItem = MediaItem.fromUri(videoUri);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to load video.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            playerView.setPlayer(null);
            player.release();
        }
    }
}