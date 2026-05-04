package com.legalstaan.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rv;
    private LeaderboardAdapter adapter;
    private ProgressBar progress;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Leaderboard");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // My stats panel
        StreakManager sm = StreakManager.get(this);
        ((TextView) findViewById(R.id.tv_my_xp)).setText(String.valueOf(sm.getXp()));
        ((TextView) findViewById(R.id.tv_my_streak)).setText(sm.getStreak() + " day streak");
        ((TextView) findViewById(R.id.tv_my_level)).setText("Level " + sm.getLevel());
        ((TextView) findViewById(R.id.tv_my_tests)).setText(sm.getTestsTaken() + " tests taken");

        rv = findViewById(R.id.rv_leaderboard);
        progress = findViewById(R.id.progress);
        tvEmpty = findViewById(R.id.tv_empty);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter();
        rv.setAdapter(adapter);

        loadTop();
    }

    private void loadTop() {
        progress.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance()
                .collection("leaderboard")
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(snap -> {
                    progress.setVisibility(View.GONE);
                    List<Entry> entries = new ArrayList<>();
                    int rank = 1;
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Entry e = new Entry();
                        e.rank = rank++;
                        e.name = d.getString("display_name");
                        e.photo = d.getString("photo_url");
                        Long xp = d.getLong("xp");
                        Long streak = d.getLong("streak");
                        e.xp = xp == null ? 0 : xp.intValue();
                        e.streak = streak == null ? 0 : streak.intValue();
                        entries.add(e);
                    }
                    adapter.submit(entries);
                    tvEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Couldn't load leaderboard.\n" + e.getMessage());
                });
    }

    static class Entry {
        int rank, xp, streak;
        String name, photo;
    }

    private static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.VH> {
        private List<Entry> data = new ArrayList<>();
        void submit(List<Entry> list) { data = list; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_leaderboard, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Entry e = data.get(position);
            h.rank.setText("#" + e.rank);
            h.name.setText(e.name == null ? "Aspirant" : e.name);
            h.xp.setText(e.xp + " XP");
            h.streak.setText("🔥 " + e.streak);
            int color;
            switch (e.rank) {
                case 1: color = 0xFFFFD700; break;
                case 2: color = 0xFFC0C0C0; break;
                case 3: color = 0xFFCD7F32; break;
                default: color = 0xFF607D95;
            }
            h.rank.setTextColor(color);
        }

        @Override public int getItemCount() { return data.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView rank, name, xp, streak;
            VH(@NonNull View v) {
                super(v);
                rank = v.findViewById(R.id.tv_rank);
                name = v.findViewById(R.id.tv_name);
                xp = v.findViewById(R.id.tv_xp);
                streak = v.findViewById(R.id.tv_streak);
            }
        }
    }
}
