package com.legalstaan.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Browses lectures and study materials for a subject.
 *
 * Two data paths, transparent to the user:
 * 1) Static — config.json declares a {@code videos} array; the items are listed.
 * 2) Live Drive folder — config.json declares a {@code folder_id}; the contents
 *    are fetched from Drive at runtime, so uploading to the folder updates the
 *    app instantly without a release.
 *
 * Sub-folders in a Drive listing open this same activity recursively, so users
 * can drill in and out naturally.
 */
public class SubjectVideosActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID        = "subject_id";
    public static final String EXTRA_SUBJECT_TITLE     = "subject_title";
    public static final String EXTRA_IS_STUDY_MATERIAL = "is_study_material";
    public static final String EXTRA_FOLDER_ID         = "folder_id";

    private final List<VideoItem> items = new ArrayList<>();
    private VideoListAdapter adapter;

    private SwipeRefreshLayout swipe;
    private ProgressBar        progress;
    private View               emptyLayout;
    private TextView           emptyTitle;
    private TextView           emptyMessage;
    private MaterialButton     emptyRetry;

    private boolean isStudyMaterial = false;
    private String  folderId        = null;
    private String  driveApiKey     = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_videos);

        String subjectId    = getIntent().getStringExtra(EXTRA_SUBJECT_ID);
        String subjectTitle = getIntent().getStringExtra(EXTRA_SUBJECT_TITLE);
        isStudyMaterial     = getIntent().getBooleanExtra(EXTRA_IS_STUDY_MATERIAL, false);
        folderId            = getIntent().getStringExtra(EXTRA_FOLDER_ID); // sub-folder drill-down
        driveApiKey         = getString(R.string.drive_api_key);

        Toolbar toolbar = findViewById(R.id.toolbar_subject);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(subjectTitle != null ? subjectTitle : "Lectures");
        }

        swipe        = findViewById(R.id.swipe_subject_videos);
        progress     = findViewById(R.id.pb_subject_loading);
        emptyLayout  = findViewById(R.id.layout_subject_empty);
        emptyTitle   = findViewById(R.id.tv_subject_empty_title);
        emptyMessage = findViewById(R.id.tv_subject_empty_message);
        emptyRetry   = findViewById(R.id.btn_subject_retry);

        swipe.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.primaryColor),
                ContextCompat.getColor(this, R.color.accentColor));
        swipe.setOnRefreshListener(this::reload);
        emptyRetry.setOnClickListener(v -> reload());

        RecyclerView rv = findViewById(R.id.rv_subject_videos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoListAdapter(items, this::openItem);
        rv.setAdapter(adapter);

        // Resolve the folder_id (and any static fallback) for this subject from config.json.
        if (folderId == null) {
            if ("__study_materials__".equals(subjectId)) {
                loadAllStudyMaterials();
            } else {
                resolveSubjectFromConfig(subjectId);
            }
        } else {
            // Drilled into a sub-folder — fetch directly from Drive.
            fetchFromDrive();
        }
    }

    private void reload() {
        items.clear();
        adapter.notifyDataSetChanged();
        if (folderId != null) {
            fetchFromDrive();
        } else {
            // Static config can't really refresh — just hide the spinner.
            swipe.setRefreshing(false);
        }
    }

    private void openItem(VideoItem item) {
        if (item.isFolder()) {
            // Drill into the sub-folder by relaunching the same activity.
            Intent i = new Intent(this, SubjectVideosActivity.class);
            i.putExtra(EXTRA_SUBJECT_TITLE,    item.title);
            i.putExtra(EXTRA_FOLDER_ID,        item.fileId);
            i.putExtra(EXTRA_IS_STUDY_MATERIAL, isStudyMaterial);
            startActivity(i);
            return;
        }
        if (item.isPdf() || isStudyMaterial) {
            // Drive's full PDF viewer in Chrome Custom Tab.
            openInCustomTab("https://drive.google.com/file/d/" + item.fileId + "/view");
            return;
        }
        // Default: video.
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra(VideoActivity.EXTRA_FILE_ID, item.fileId);
        intent.putExtra(VideoActivity.EXTRA_TITLE,   item.title);
        startActivity(intent);
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

    /** Look up the subject in config.json and either: load static videos, or kick off a Drive fetch. */
    private void resolveSubjectFromConfig(String subjectId) {
        if (subjectId == null) {
            showError("Subject not found");
            return;
        }
        try {
            JSONObject root     = readConfig();
            JSONArray  subjects = root.getJSONArray("subjects");
            for (int i = 0; i < subjects.length(); i++) {
                JSONObject subj = subjects.getJSONObject(i);
                if (!subj.getString("id").equals(subjectId)) continue;

                if ("study_material".equals(subj.optString("category", "lecture"))) {
                    isStudyMaterial = true;
                }

                String fid = subj.optString("folder_id", "");
                if (!fid.isEmpty()) {
                    folderId = fid;
                    fetchFromDrive();
                    return;
                }

                // Static videos branch — load synchronously.
                JSONArray videos = subj.optJSONArray("videos");
                if (videos != null) {
                    for (int j = 0; j < videos.length(); j++) {
                        JSONObject v = videos.getJSONObject(j);
                        items.add(new VideoItem(v.getString("title"), v.getString("file_id")));
                    }
                }
                if (items.isEmpty()) {
                    showEmpty(isStudyMaterial ? "No study material yet" : "No lectures yet",
                            "Check back soon — content lands here as faculty upload it.");
                } else {
                    showList();
                }
                return;
            }
            showError("Subject not found");
        } catch (Exception e) {
            showError("Could not load content: " + e.getMessage());
        }
    }

    private void loadAllStudyMaterials() {
        try {
            JSONObject root     = readConfig();
            JSONArray  subjects = root.getJSONArray("subjects");
            for (int i = 0; i < subjects.length(); i++) {
                JSONObject subj = subjects.getJSONObject(i);
                if (!"study_material".equals(subj.optString("category", "lecture"))) continue;
                JSONArray videos = subj.optJSONArray("videos");
                if (videos == null) continue;
                for (int j = 0; j < videos.length(); j++) {
                    JSONObject v = videos.getJSONObject(j);
                    items.add(new VideoItem(v.getString("title"), v.getString("file_id"),
                            VideoItem.MIME_PDF));
                }
            }
            isStudyMaterial = true;
            if (items.isEmpty()) {
                showEmpty("No study material yet",
                        "Check back soon — content lands here as faculty upload it.");
            } else {
                showList();
            }
        } catch (Exception e) {
            showError("Could not load materials.");
        }
    }

    private JSONObject readConfig() throws Exception {
        InputStream is = getAssets().open("config.json");
        byte[] buf = new byte[is.available()];
        is.read(buf);
        is.close();
        return new JSONObject(new String(buf, StandardCharsets.UTF_8));
    }

    /** Live Drive fetch — uses {@link DriveListing} on a background thread. */
    private void fetchFromDrive() {
        showLoading();
        DriveListing.fetchFolder(folderId, driveApiKey, new DriveListing.Callback() {
            @Override
            public void onResult(List<VideoItem> result) {
                items.clear();
                items.addAll(result);
                adapter.notifyDataSetChanged();
                if (items.isEmpty()) {
                    showEmpty("This folder is empty",
                            "Once content is uploaded to Drive, it'll show up here.");
                } else {
                    showList();
                }
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private void showLoading() {
        swipe.setRefreshing(false);
        progress.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        emptyLayout.setVisibility(View.GONE);
    }

    private void showList() {
        swipe.setRefreshing(false);
        progress.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);
    }

    private void showEmpty(String title, String message) {
        swipe.setRefreshing(false);
        progress.setVisibility(View.GONE);
        emptyTitle.setText(title);
        emptyMessage.setText(message);
        emptyRetry.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        swipe.setRefreshing(false);
        progress.setVisibility(View.GONE);
        emptyTitle.setText("Couldn't load");
        emptyMessage.setText(message);
        emptyRetry.setVisibility(folderId != null ? View.VISIBLE : View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
        if (!items.isEmpty()) {
            // Already showing some items — keep them visible and just toast the error.
            emptyLayout.setVisibility(View.GONE);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

        private final List<VideoItem>      items;
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
        public void onBindViewHolder(@NonNull VH h, int position) {
            VideoItem item = items.get(position);
            h.tvTitle.setText(item.title);

            if (item.isFolder()) {
                showTypeIcon(h, android.R.drawable.ic_menu_more);
                h.tvKind.setText("Folder");
                h.tvKind.setVisibility(View.VISIBLE);
                h.chevron.setImageResource(android.R.drawable.ic_media_next);
            } else if (item.isPdf()) {
                showTypeIcon(h, android.R.drawable.ic_menu_agenda);
                h.tvKind.setText("Study Material");
                h.tvKind.setVisibility(View.VISIBLE);
                h.chevron.setImageResource(android.R.drawable.stat_sys_download_done);
            } else if (item.isVideo()) {
                int lectureNum = computeLectureNum(items, position);
                if (lectureNum > 0) {
                    h.tvNum.setVisibility(View.VISIBLE);
                    h.ivType.setVisibility(View.GONE);
                    h.tvNum.setText(String.valueOf(lectureNum));
                } else {
                    showTypeIcon(h, android.R.drawable.ic_media_play);
                }
                h.tvKind.setText("Lecture");
                h.tvKind.setVisibility(View.VISIBLE);
                h.chevron.setImageResource(android.R.drawable.ic_media_play);
            } else {
                showTypeIcon(h, android.R.drawable.ic_menu_view);
                h.tvKind.setVisibility(View.GONE);
                h.chevron.setImageResource(android.R.drawable.ic_media_next);
            }

            h.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        private static void showTypeIcon(VH h, int resId) {
            h.tvNum.setVisibility(View.GONE);
            h.ivType.setVisibility(View.VISIBLE);
            h.ivType.setImageResource(resId);
        }

        /** Number videos sequentially among the videos in the list, ignoring folders / pdfs. */
        private static int computeLectureNum(List<VideoItem> all, int idx) {
            int count = 0;
            for (int i = 0; i <= idx; i++) {
                if (all.get(i).isVideo()) count++;
            }
            return count;
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView  tvTitle, tvNum, tvKind;
            final ImageView ivType, chevron;
            VH(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tv_video_title);
                tvNum   = v.findViewById(R.id.tv_video_num);
                tvKind  = v.findViewById(R.id.tv_video_kind);
                ivType  = v.findViewById(R.id.iv_video_type);
                chevron = v.findViewById(R.id.iv_video_chevron);
            }
        }
    }
}
