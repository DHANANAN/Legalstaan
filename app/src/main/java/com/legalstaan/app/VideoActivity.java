package com.legalstaan.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends AppCompatActivity {

    private static final String DRIVE_DOWNLOAD_BASE = "https://drive.google.com/uc?export=download&id=";

    private RecyclerView recyclerView;
    private View playerContainer;
    private PlayerView playerView;
    private ExoPlayer player;

    private final List<VideoItem> videoItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.video_lectures);
        }

        recyclerView    = findViewById(R.id.rv_videos);
        playerContainer = findViewById(R.id.player_container);
        playerView      = findViewById(R.id.player_view);

        loadConfig();
        setupRecyclerView();
        registerBackHandler();
    }

    private void loadConfig() {
        try {
            InputStream is = getAssets().open("config.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONObject root = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
            JSONArray levels = root.getJSONArray("levels");
            for (int i = 0; i < levels.length(); i++) {
                JSONObject obj = levels.getJSONObject(i);
                videoItems.add(new VideoItem(
                        obj.getString("id"),
                        obj.getString("title"),
                        obj.getString("description"),
                        obj.getString("file_id")
                ));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not load video list.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new VideoAdapter(videoItems, this::openPlayer));
    }

    private void openPlayer(VideoItem item) {
        String url = DRIVE_DOWNLOAD_BASE + item.fileId;

        recyclerView.setVisibility(View.GONE);
        playerContainer.setVisibility(View.VISIBLE);

        releasePlayer();
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setMediaItem(MediaItem.fromUri(url));
        player.prepare();
        player.setPlayWhenReady(true);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                Toast.makeText(VideoActivity.this,
                        "Playback error — check your Drive file permissions.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (playerContainer.getVisibility() == View.VISIBLE) {
                    releasePlayer();
                    playerContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    // ── Model ────────────────────────────────────────────────────────────────

    static class VideoItem {
        final String id, title, description, fileId;
        VideoItem(String id, String title, String description, String fileId) {
            this.id          = id;
            this.title       = title;
            this.description = description;
            this.fileId      = fileId;
        }
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    interface OnVideoClickListener {
        void onClick(VideoItem item);
    }

    static class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VH> {

        private final List<VideoItem> items;
        private final OnVideoClickListener listener;

        VideoAdapter(List<VideoItem> items, OnVideoClickListener listener) {
            this.items    = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_video, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            VideoItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDesc.setText(item.description);
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvTitle, tvDesc;
            VH(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_video_title);
                tvDesc  = v.findViewById(R.id.tv_video_desc);
            }
        }
    }
}
