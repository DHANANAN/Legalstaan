package com.legalstaan.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SubjectVideosActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID       = "subject_id";
    public static final String EXTRA_SUBJECT_TITLE    = "subject_title";
    public static final String EXTRA_IS_STUDY_MATERIAL = "is_study_material";

    private final List<VideoItem> videoItems = new ArrayList<>();
    private boolean isStudyMaterial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_videos);

        String subjectId    = getIntent().getStringExtra(EXTRA_SUBJECT_ID);
        String subjectTitle = getIntent().getStringExtra(EXTRA_SUBJECT_TITLE);
        isStudyMaterial     = getIntent().getBooleanExtra(EXTRA_IS_STUDY_MATERIAL, false);

        Toolbar toolbar = findViewById(R.id.toolbar_subject);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(subjectTitle != null ? subjectTitle : "Lectures");
        }

        if ("__study_materials__".equals(subjectId)) {
            // Show all study-material subjects as a flat list
            loadAllStudyMaterials();
        } else {
            loadVideosForSubject(subjectId);
        }

        RecyclerView rv = findViewById(R.id.rv_subject_videos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new VideoListAdapter(videoItems, this::openItem));
    }

    private void openItem(VideoItem item) {
        if (isStudyMaterial) {
            // Open PDF / study file in the Drive full viewer via Chrome Custom Tab
            openInCustomTab("https://drive.google.com/file/d/" + item.fileId + "/view");
        } else {
            Intent intent = new Intent(this, VideoActivity.class);
            intent.putExtra(VideoActivity.EXTRA_FILE_ID, item.fileId);
            intent.putExtra(VideoActivity.EXTRA_TITLE,   item.title);
            startActivity(intent);
        }
    }

    private void openInCustomTab(String url) {
        try {
            int color = ContextCompat.getColor(this, R.color.primaryColor);
            CustomTabColorSchemeParams params = new CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(color).build();
            new CustomTabsIntent.Builder()
                    .setDefaultColorSchemeParams(params)
                    .setShowTitle(true)
                    .build()
                    .launchUrl(this, Uri.parse(url));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

    private void loadVideosForSubject(String subjectId) {
        if (subjectId == null) return;
        try {
            InputStream is  = getAssets().open("config.json");
            byte[]      buf = new byte[is.available()];
            is.read(buf);
            is.close();

            JSONObject root     = new JSONObject(new String(buf, StandardCharsets.UTF_8));
            JSONArray  subjects = root.getJSONArray("subjects");
            for (int i = 0; i < subjects.length(); i++) {
                JSONObject subj = subjects.getJSONObject(i);
                if (subj.getString("id").equals(subjectId)) {
                    JSONArray videos = subj.getJSONArray("videos");
                    for (int j = 0; j < videos.length(); j++) {
                        JSONObject v = videos.getJSONObject(j);
                        videoItems.add(new VideoItem(v.getString("title"), v.getString("file_id")));
                    }
                    // If this subject itself is study material, flip the open behaviour
                    if ("study_material".equals(subj.optString("category", "lecture"))) {
                        isStudyMaterial = true;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not load content.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAllStudyMaterials() {
        // Aggregate items from every study_material subject into one flat list
        try {
            InputStream is  = getAssets().open("config.json");
            byte[]      buf = new byte[is.available()];
            is.read(buf);
            is.close();

            JSONObject root     = new JSONObject(new String(buf, StandardCharsets.UTF_8));
            JSONArray  subjects = root.getJSONArray("subjects");
            for (int i = 0; i < subjects.length(); i++) {
                JSONObject subj = subjects.getJSONObject(i);
                if (!"study_material".equals(subj.optString("category", "lecture"))) continue;
                JSONArray videos = subj.getJSONArray("videos");
                for (int j = 0; j < videos.length(); j++) {
                    JSONObject v = videos.getJSONObject(j);
                    videoItems.add(new VideoItem(v.getString("title"), v.getString("file_id")));
                }
            }
            isStudyMaterial = true;
        } catch (Exception e) {
            Toast.makeText(this, "Could not load materials.", Toast.LENGTH_SHORT).show();
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

    interface OnVideoClickListener { void onClick(VideoItem item); }

    static class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VH> {

        private final List<VideoItem>    items;
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
            holder.tvNum.setText(String.valueOf(position + 1));
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvTitle, tvNum;
            VH(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_video_title);
                tvNum   = v.findViewById(R.id.tv_video_num);
            }
        }
    }
}
