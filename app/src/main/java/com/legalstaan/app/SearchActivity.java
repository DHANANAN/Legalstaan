package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    enum Type { SUBJECT, VIDEO, TEST_SET, FACULTY }

    static class Result {
        Type type;
        String title;
        String subtitle;
        String subjectId;       // for SUBJECT / VIDEO
        String videoFileId;     // for VIDEO
        String testId;          // for TEST_SET
        boolean isStudyMaterial;
    }

    private final List<Result> all = new ArrayList<>();
    private ResultAdapter adapter;
    private EditText etQuery;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etQuery = findViewById(R.id.et_query);
        tvEmpty = findViewById(R.id.tv_empty);
        RecyclerView rv = findViewById(R.id.rv_results);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResultAdapter();
        rv.setAdapter(adapter);

        buildIndex();
        etQuery.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                runQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etQuery.requestFocus();
        runQuery("");
    }

    private void buildIndex() {
        // Subjects + videos from assets/config.json
        try (InputStream in = getAssets().open("config.json")) {
            byte[] buf = new byte[in.available()];
            int read = in.read(buf);
            if (read > 0) {
                JSONObject root = new JSONObject(new String(buf, StandardCharsets.UTF_8));
                JSONArray subjects = root.optJSONArray("subjects");
                if (subjects != null) {
                    for (int i = 0; i < subjects.length(); i++) {
                        JSONObject s = subjects.getJSONObject(i);
                        String id = s.optString("id");
                        String title = s.optString("title");
                        String category = s.optString("category", "lecture");
                        boolean study = "study_material".equals(category);

                        Result subjResult = new Result();
                        subjResult.type = Type.SUBJECT;
                        subjResult.title = title;
                        subjResult.subtitle = study ? "Study material set" : "Lecture series";
                        subjResult.subjectId = id;
                        subjResult.isStudyMaterial = study;
                        all.add(subjResult);

                        JSONArray videos = s.optJSONArray("videos");
                        if (videos != null) {
                            for (int j = 0; j < videos.length(); j++) {
                                JSONObject v = videos.getJSONObject(j);
                                Result vr = new Result();
                                vr.type = Type.VIDEO;
                                vr.title = v.optString("title");
                                vr.subtitle = title;
                                vr.subjectId = id;
                                vr.videoFileId = v.optString("file_id");
                                vr.isStudyMaterial = study;
                                all.add(vr);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignore) {}

        // Test sets from QuestionBank
        for (QuestionBank.TestSet t : QuestionBank.all()) {
            Result r = new Result();
            r.type = Type.TEST_SET;
            r.title = t.title;
            r.subtitle = t.description;
            r.testId = t.id;
            all.add(r);
        }

        // Faculty (handled by FacultyManager in app)
        for (String email : FacultyManager.allFacultyEmails()) {
            Result r = new Result();
            r.type = Type.FACULTY;
            r.title = email.substring(0, email.indexOf('@'));
            r.subtitle = email + "  •  Faculty";
            all.add(r);
        }
    }

    private void runQuery(String q) {
        String needle = q.trim().toLowerCase(Locale.ROOT);
        List<Result> filtered = new ArrayList<>();
        if (needle.isEmpty()) {
            filtered.addAll(all);
        } else {
            for (Result r : all) {
                if ((r.title != null && r.title.toLowerCase(Locale.ROOT).contains(needle))
                        || (r.subtitle != null && r.subtitle.toLowerCase(Locale.ROOT).contains(needle))) {
                    filtered.add(r);
                }
            }
        }
        adapter.submit(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void open(Result r) {
        switch (r.type) {
            case SUBJECT: {
                Intent i = new Intent(this, SubjectVideosActivity.class);
                i.putExtra(SubjectVideosActivity.EXTRA_SUBJECT_ID, r.subjectId);
                i.putExtra(SubjectVideosActivity.EXTRA_SUBJECT_TITLE, r.title);
                i.putExtra(SubjectVideosActivity.EXTRA_IS_STUDY_MATERIAL, r.isStudyMaterial);
                startActivity(i);
                break;
            }
            case VIDEO: {
                if (r.isStudyMaterial && r.videoFileId != null) {
                    Intent i = new Intent(this, PdfNotesActivity.class);
                    i.putExtra(PdfNotesActivity.EXTRA_TITLE, r.title);
                    i.putExtra(PdfNotesActivity.EXTRA_DRIVE_ID, r.videoFileId);
                    startActivity(i);
                } else if (r.videoFileId != null) {
                    Intent i = new Intent(this, VideoActivity.class);
                    i.putExtra(VideoActivity.EXTRA_TITLE, r.title);
                    i.putExtra(VideoActivity.EXTRA_FILE_ID, r.videoFileId);
                    startActivity(i);
                }
                break;
            }
            case TEST_SET: {
                Intent i = new Intent(this, QuestionRunnerActivity.class);
                i.putExtra(QuestionRunnerActivity.EXTRA_TEST_ID, r.testId);
                startActivity(i);
                break;
            }
            case FACULTY:
                // No-op for now; could deep-link DM
                break;
        }
    }

    private class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.VH> {
        private List<Result> data = new ArrayList<>();
        void submit(List<Result> list) { data = list; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_result, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Result r = data.get(position);
            h.title.setText(r.title);
            h.subtitle.setText(r.subtitle);
            String tag;
            switch (r.type) {
                case SUBJECT:  tag = "📚 Subject"; break;
                case VIDEO:    tag = r.isStudyMaterial ? "📄 Notes" : "🎬 Lecture"; break;
                case TEST_SET: tag = "📝 Mock Test"; break;
                case FACULTY:  tag = "👤 Faculty"; break;
                default:       tag = "";
            }
            h.tag.setText(tag);
            h.itemView.setOnClickListener(v -> open(r));
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView title, subtitle, tag;
            VH(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.tv_title);
                subtitle = v.findViewById(R.id.tv_subtitle);
                tag = v.findViewById(R.id.tv_tag);
            }
        }
    }
}
