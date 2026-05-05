package com.legalstaan.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CoursesFragment extends Fragment {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM   = 1;

    /** Wrapper to hold either a header string or a Subject in the unified list. */
    private static class Row {
        final String  header;  // non-null for header rows
        final Subject subject; // non-null for item rows
        Row(String h)  { header = h; subject = null; }
        Row(Subject s) { header = null; subject = s; }
        boolean isHeader() { return header != null; }
    }

    private final List<Subject> lectures       = new ArrayList<>();
    private final List<Subject> studyMaterials = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Banner at the top of the tab — paid CLAT PG batch enrollment.
        // Free content below remains free; this is a separate optional path.
        View joinBatchCard = view.findViewById(R.id.card_join_batch);
        if (joinBatchCard != null) {
            joinBatchCard.setOnClickListener(v ->
                    startActivity(new Intent(requireActivity(), JoinBatchActivity.class)));
        }

        loadSubjects();

        // Build the flat list: header + items for each section
        List<Row> rows = new ArrayList<>();
        if (!lectures.isEmpty()) {
            rows.add(new Row("Video Lectures"));
            for (Subject s : lectures) rows.add(new Row(s));
        }
        if (!studyMaterials.isEmpty()) {
            rows.add(new Row("Free Study Materials"));
            for (Subject s : studyMaterials) rows.add(new Row(s));
        }

        RecyclerView rv = view.findViewById(R.id.rv_subjects);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new SectionAdapter(rows, this::openSubject));
    }

    private void openSubject(Subject subject) {
        Intent intent = new Intent(requireActivity(), SubjectVideosActivity.class);
        intent.putExtra(SubjectVideosActivity.EXTRA_SUBJECT_ID,    subject.id);
        intent.putExtra(SubjectVideosActivity.EXTRA_SUBJECT_TITLE, subject.title);
        intent.putExtra(SubjectVideosActivity.EXTRA_IS_STUDY_MATERIAL, subject.isStudyMaterial());
        if (subject.isDriveLinked()) {
            intent.putExtra(SubjectVideosActivity.EXTRA_FOLDER_ID, subject.folderId);
        }
        startActivity(intent);
    }

    private void loadSubjects() {
        try {
            InputStream is = requireContext().getAssets().open("config.json");
            byte[] buffer  = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONObject root = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
            JSONArray  arr  = root.getJSONArray("subjects");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject s = arr.getJSONObject(i);
                String id       = s.getString("id");
                String title    = s.getString("title");
                String color    = s.getString("color");
                String category = s.optString("category", "lecture");

                JSONArray     vArr  = s.optJSONArray("videos");
                List<VideoItem> vList = new ArrayList<>();
                if (vArr != null) {
                    for (int j = 0; j < vArr.length(); j++) {
                        JSONObject v = vArr.getJSONObject(j);
                        vList.add(new VideoItem(v.getString("title"), v.getString("file_id")));
                    }
                }
                String folderId = s.optString("folder_id", null);
                Subject subject = new Subject(id, title, color, vList, category, folderId);
                if (subject.isStudyMaterial()) studyMaterials.add(subject);
                else                           lectures.add(subject);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not load subjects.", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Adapter with section headers ──────────────────────────────────────────

    interface OnSubjectClickListener { void onClick(Subject subject); }

    static class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<Row>              rows;
        private final OnSubjectClickListener listener;

        SectionAdapter(List<Row> rows, OnSubjectClickListener listener) {
            this.rows     = rows;
            this.listener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            return rows.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                View v = inf.inflate(R.layout.item_section_header, parent, false);
                return new HeaderVH(v);
            }
            View v = inf.inflate(R.layout.item_subject, parent, false);
            return new SubjectVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Row row = rows.get(position);
            if (row.isHeader()) {
                ((HeaderVH) holder).tvHeader.setText(row.header);
            } else {
                Subject s = row.subject;
                SubjectVH h = (SubjectVH) holder;
                h.tvTitle.setText(s.title);
                String label;
                if (s.isDriveLinked()) {
                    label = s.isStudyMaterial() ? "Live · Drive synced"
                                                : "Tap to load lectures";
                } else if (s.isStudyMaterial()) {
                    label = s.getVideoCount() + " files";
                } else {
                    label = s.getVideoCount() + " lectures";
                }
                h.tvCount.setText(label);

                GradientDrawable dot = new GradientDrawable();
                dot.setShape(GradientDrawable.OVAL);
                try { dot.setColor(Color.parseColor(s.color)); }
                catch (Exception e) { dot.setColor(Color.GRAY); }
                h.colorDot.setBackground(dot);

                h.itemView.setOnClickListener(v -> listener.onClick(s));
            }
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

        static class SubjectVH extends RecyclerView.ViewHolder {
            final TextView tvTitle, tvCount;
            final View     colorDot;
            SubjectVH(@NonNull View v) {
                super(v);
                tvTitle  = v.findViewById(R.id.tv_subject_title);
                tvCount  = v.findViewById(R.id.tv_video_count);
                colorDot = v.findViewById(R.id.v_color_dot);
            }
        }
    }
}
