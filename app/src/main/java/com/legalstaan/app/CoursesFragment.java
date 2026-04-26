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

    private final List<Subject> subjects = new ArrayList<>();

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
        loadSubjects();

        RecyclerView rv = view.findViewById(R.id.rv_subjects);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new SubjectAdapter(subjects, subject -> {
            Intent intent = new Intent(requireActivity(), SubjectVideosActivity.class);
            intent.putExtra(SubjectVideosActivity.EXTRA_SUBJECT_ID, subject.id);
            intent.putExtra(SubjectVideosActivity.EXTRA_SUBJECT_TITLE, subject.title);
            startActivity(intent);
        }));
    }

    private void loadSubjects() {
        try {
            InputStream is = requireContext().getAssets().open("config.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            JSONObject root = new JSONObject(new String(buffer, StandardCharsets.UTF_8));
            JSONArray arr = root.getJSONArray("subjects");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject s = arr.getJSONObject(i);
                String id    = s.getString("id");
                String title = s.getString("title");
                String color = s.getString("color");

                JSONArray vArr = s.getJSONArray("videos");
                List<VideoItem> vList = new ArrayList<>();
                for (int j = 0; j < vArr.length(); j++) {
                    JSONObject v = vArr.getJSONObject(j);
                    vList.add(new VideoItem(v.getString("title"), v.getString("file_id")));
                }
                subjects.add(new Subject(id, title, color, vList));
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not load subjects.", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    interface OnSubjectClickListener {
        void onClick(Subject subject);
    }

    static class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.VH> {

        private final List<Subject> items;
        private final OnSubjectClickListener listener;

        SubjectAdapter(List<Subject> items, OnSubjectClickListener listener) {
            this.items    = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subject, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Subject s = items.get(position);
            holder.tvTitle.setText(s.title);
            holder.tvCount.setText(s.getVideoCount() + " lectures");

            // Color the indicator dot
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            try {
                dot.setColor(Color.parseColor(s.color));
            } catch (Exception e) {
                dot.setColor(Color.GRAY);
            }
            holder.colorDot.setBackground(dot);

            holder.itemView.setOnClickListener(v -> listener.onClick(s));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvTitle, tvCount;
            final View colorDot;

            VH(@NonNull View v) {
                super(v);
                tvTitle  = v.findViewById(R.id.tv_subject_title);
                tvCount  = v.findViewById(R.id.tv_video_count);
                colorDot = v.findViewById(R.id.v_color_dot);
            }
        }
    }
}
