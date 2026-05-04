package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Lists the day's rotated test sets — Combined Daily Test pinned at the
 * top, then per-subject sets. Refreshes twice daily (06:00 and 18:00 local
 * time); see {@link TestRotation} for the rotation logic and
 * {@link TestRefreshScheduler} for the slot-boundary notifications.
 */
public class QuestionBankActivity extends AppCompatActivity {

    private TextView tvSlotTitle, tvSlotSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_bank);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Question Bank");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvSlotTitle    = findViewById(R.id.tv_slot_title);
        tvSlotSubtitle = findViewById(R.id.tv_slot_subtitle);

        RecyclerView rv = findViewById(R.id.rv_test_sets);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new TestSetAdapter(TestRotation.currentTestSets()));

        // Make sure the rotation alarm is registered (idempotent).
        TestRefreshScheduler.scheduleNext(this);

        renderSlotBanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderSlotBanner();
    }

    private void renderSlotBanner() {
        TestRotation.Slot slot = TestRotation.currentSlot();
        tvSlotTitle.setText(slot.label + " Slot");
        tvSlotSubtitle.setText(TestRotation.formatTimeUntilNextSlot()
                + " · 6 AM & 6 PM daily");
    }

    private class TestSetAdapter extends RecyclerView.Adapter<TestSetAdapter.VH> {
        private final List<QuestionBank.TestSet> data;
        TestSetAdapter(List<QuestionBank.TestSet> data) { this.data = data; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_test_set, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            QuestionBank.TestSet t = data.get(position);
            h.title.setText(t.title);
            h.desc.setText(t.description);
            h.qCount.setText(t.questions.size() + " Q");
            h.card.setOnClickListener(v -> {
                Intent i = new Intent(QuestionBankActivity.this, QuestionRunnerActivity.class);
                i.putExtra(QuestionRunnerActivity.EXTRA_TEST_ID, t.id);
                startActivity(i);
            });
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder {
            MaterialCardView card;
            TextView title, desc, qCount;
            VH(@NonNull View v) {
                super(v);
                card = v.findViewById(R.id.card);
                title = v.findViewById(R.id.tv_title);
                desc = v.findViewById(R.id.tv_desc);
                qCount = v.findViewById(R.id.tv_q_count);
            }
        }
    }
}
