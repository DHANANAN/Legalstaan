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
 * Two data paths:
 * 1) Static — config.json declares a {@code videos} array; items listed as-is.
 * 2) Live Drive folder — config.json declares a {@code folder_id}; the entire
 *    Drive tree under that folder is walked breadth-first and the videos +
 *    PDFs are presented in two sections: "Lectures" and "Material".
 *
 * Sub-folders are flattened into the result silently — students never see
 * folder structure, just the content.
 */
public class SubjectVideosActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID        = "subject_id";
    public static final String EXTRA_SUBJECT_TITLE     = "subject_title";
    public static final String EXTRA_IS_STUDY_MATERIAL = "is_study_material";
    public static final String EXTRA_FOLDER_ID         = "folder_id";

    /** Combined header + item list driving the RecyclerView. */
    private final List<Object> rows = new ArrayList<>();
    private SectionAdapter adapter;

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
        folderId            = getIntent().getStringExtra(EXTRA_FOLDER_ID);
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
        adapter = new SectionAdapter(rows, this::openItem);
        rv.setAdapter(adapter);

        if (folderId != null) {
            fetchFromDrive();
        } else if ("__study_materials__".equals(subjectId)) {
            loadAllStudyMaterials();
        } else {
            resolveSubjectFromConfig(subjectId);
        }
    }

    private void reload() {
        rows.clear();
        adapter.notifyDataSetChanged();
        if (folderId != null) {
            fetchFromDrive();
        } else {
            swipe.setRefreshing(false);
        }
    }

    private void openItem(VideoItem item) {
        if (item.isPdf() || (isStudyMaterial && !item.isVideo())) {
            // Drive's full PDF viewer in Chrome Custom Tab.
            openInCustomTab("https://drive.google.com/file/d/" + item.fileId + "/view");
            return;
        }
        // Video — try ExoPlayer first; falls back to WebView VideoActivity on error.
        Intent intent = new Intent(this, ExoPlayerActivity.class);
        intent.putExtra(ExoPlayerActivity.EXTRA_FILE_ID, item.fileId);
        intent.putExtra(ExoPlayerActivity.EXTRA_TITLE,   item.title);
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

    /** Resolve subject from config.json — Drive path takes priority over static videos. */
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

                // Static videos branch.
                List<VideoItem> items = new ArrayList<>();
                JSONArray videos = subj.optJSONArray("videos");
                if (videos != null) {
                    for (int j = 0; j < videos.length(); j++) {
                        JSONObject v = videos.getJSONObject(j);
                        items.add(new VideoItem(v.getString("title"), v.getString("file_id"),
                                isStudyMaterial ? VideoItem.MIME_PDF : "video/mp4"));
                    }
                }
                presentItems(items);
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
            List<VideoItem> items = new ArrayList<>();
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
            presentItems(items);
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

    /** Live Drive fetch — recursive flatten so the user just sees Lectures + Material. */
    private void fetchFromDrive() {
        showLoading();
        DriveListing.fetchFolderRecursive(folderId, driveApiKey, new DriveListing.Callback() {
            @Override
            public void onResult(List<VideoItem> result) {
                presentItems(result);
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    /** Group items into Lectures (videos) + Material (PDFs) sections. */
    private void presentItems(List<VideoItem> items) {
        rows.clear();
        List<VideoItem> lectures = new ArrayList<>();
        List<VideoItem> materials = new ArrayList<>();
        for (VideoItem v : items) {
            if (v.isPdf()) materials.add(v);
            else if (v.isVideo()) lectures.add(v);
            else if (isStudyMaterial) materials.add(v);
            else lectures.add(v);
        }

        if (!lectures.isEmpty()) {
            rows.add("Lectures (" + lectures.size() + ")");
            rows.addAll(lectures);
        }
        if (!materials.isEmpty()) {
            rows.add("Material (" + materials.size() + ")");
            rows.addAll(materials);
        }

        adapter.notifyDataSetChanged();
        if (rows.isEmpty()) {
            showEmpty(isStudyMaterial ? "No material yet" : "No content yet",
                    "Once content is uploaded to Drive, it'll show up here.");
        } else {
            showList();
        }
    }

    private void showLoading() {
        swipe.setRefreshing(false);
        progress.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
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
        emptyRetry.setVisibility(folderId != null ? View.VISIBLE : View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        swipe.setRefreshing(false);
        progress.setVisibility(View.GONE);
        if (!rows.isEmpty()) {
            emptyLayout.setVisibility(View.GONE);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return;
        }
        emptyTitle.setText("Couldn't load");
        emptyMessage.setText(message);
        emptyRetry.setVisibility(folderId != null ? View.VISIBLE : View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Adapter (header rows + item rows) ────────────────────────────────────

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM   = 1;

    interface OnVideoClickListener { void onClick(VideoItem item); }

    static class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<Object>         rows;
        private final OnVideoClickListener listener;

        SectionAdapter(List<Object> rows, OnVideoClickListener listener) {
            this.rows     = rows;
            this.listener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            return rows.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                View v = inf.inflate(R.layout.item_section_header, parent, false);
                return new HeaderVH(v);
            }
            View v = inf.inflate(R.layout.item_video, parent, false);
            return new ItemVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object row = rows.get(position);
            if (row instanceof String) {
                ((HeaderVH) holder).tvHeader.setText((String) row);
                return;
            }
            VideoItem item = (VideoItem) row;
            ItemVH h = (ItemVH) holder;
            h.tvTitle.setText(item.title);

            if (item.isPdf()) {
                h.tvNum.setVisibility(View.GONE);
                h.ivType.setVisibility(View.VISIBLE);
                h.ivType.setImageResource(android.R.drawable.ic_menu_agenda);
                h.tvKind.setText("Material");
                h.tvKind.setVisibility(View.VISIBLE);
                h.chevron.setImageResource(android.R.drawable.stat_sys_download_done);
            } else {
                int n = computeLectureNum(rows, position);
                if (n > 0) {
                    h.tvNum.setVisibility(View.VISIBLE);
                    h.ivType.setVisibility(View.GONE);
                    h.tvNum.setText(String.valueOf(n));
                } else {
                    h.tvNum.setVisibility(View.GONE);
                    h.ivType.setVisibility(View.VISIBLE);
                    h.ivType.setImageResource(android.R.drawable.ic_media_play);
                }
                h.tvKind.setText("Lecture");
                h.tvKind.setVisibility(View.VISIBLE);
                h.chevron.setImageResource(android.R.drawable.ic_media_play);
            }

            h.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        /** Lecture number = position among videos within the same Lectures section. */
        private static int computeLectureNum(List<Object> rows, int idx) {
            int count = 0;
            // Walk back to the header above this item, then forward from there.
            int sectionStart = 0;
            for (int i = idx; i >= 0; i--) {
                if (rows.get(i) instanceof String) { sectionStart = i + 1; break; }
            }
            for (int i = sectionStart; i <= idx; i++) {
                Object r = rows.get(i);
                if (r instanceof VideoItem && ((VideoItem) r).isVideo()) count++;
            }
            return count;
        }

        @Override
        public int getItemCount() { return rows.size(); }

        static class HeaderVH extends RecyclerView.ViewHolder {
            final TextView tvHeader;
            HeaderVH(@NonNull View v) {
                super(v);
                tvHeader = v.findViewById(R.id.tv_section_header);
            }
        }

        static class ItemVH extends RecyclerView.ViewHolder {
            final TextView  tvTitle, tvNum, tvKind;
            final ImageView ivType, chevron;
            ItemVH(@NonNull View v) {
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
