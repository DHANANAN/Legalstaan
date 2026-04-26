package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SubjectVideosActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID    = "subject_id";
    public static final String EXTRA_SUBJECT_TITLE = "subject_title";

    private final List<VideoItem> videoItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_videos);

        String subjectId    = getIntent().getStringExtra(EXTRA_SUBJECT_ID);
        String subjectTitle = getIntent().getStringExtra(EXTRA_SUBJECT_TITLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(subjectTitle != null ? subjectTitle : "Lectures");
        }

        loadVideos(subjectId);

        RecyclerView rv = findViewById(R.id.rv_subject_videos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new VideoListAdapter(videoItems, item -> {
            Intent intent = new Intent(this, VideoActivity.class);
            intent.putExtra(VideoActivity.EXTRA_FILE_ID, item.fileId);
            intent.putExtra(VideoActivity.EXTRA_TITLE, item.title);
            startActivity(intent);
        }));
    }

    private void loadVideos(String subjectId) {
        if (subjectId == null) return;
        try {
            InputStream is = getAssets().open("config.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONObject root = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
            JSONArray subjects = root.getJSONArray("subjects");

            for (int i = 0; i < subjects.length(); i++) {
                JSONObject subj = subjects.getJSONObject(i);
                if (subj.getString("id").equals(subjectId)) {
                    JSONArray videos = subj.getJSONArray("videos");
                    for (int j = 0; j < videos.length(); j++) {
                        JSONObject v = videos.getJSONObject(j);
                        videoItems.add(new VideoItem(
                                v.getString("title"),
                                v.getString("file_id")));
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not load videos.", Toast.LENGTH_SHORT).show();
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

    // ── Adapter ──────────────────────────────────────────────────────────────

    interface OnVideoClickListener {
        void onClick(VideoItem item);
    }

    static class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VH> {

        private final List<VideoItem> items;
        private final OnVideoClickListener listener;

        VideoListAdapter(List<VideoItem> items, OnVideoClickListener listener) {
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
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvTitle;

            VH(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_video_title);
            }
        }
    }
}
