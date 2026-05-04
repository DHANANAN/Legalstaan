package com.legalstaan.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class QuestionResultActivity extends AppCompatActivity {

    public static final String EXTRA_TEST_ID = "test_id";
    public static final String EXTRA_ANSWERS = "answers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_result);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Result");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        String testId = getIntent().getStringExtra(EXTRA_TEST_ID);
        int[] answers = getIntent().getIntArrayExtra(EXTRA_ANSWERS);
        QuestionBank.TestSet t = QuestionBank.byId(testId);
        if (t == null || answers == null) { finish(); return; }

        int correct = 0, attempted = 0;
        for (int i = 0; i < t.questions.size(); i++) {
            if (answers[i] >= 0) {
                attempted++;
                if (answers[i] == t.questions.get(i).correctIndex) correct++;
            }
        }
        int total = t.questions.size();
        int wrong = attempted - correct;
        float pct = total == 0 ? 0 : (correct * 100f) / total;

        ((TextView) findViewById(R.id.tv_score)).setText(correct + " / " + total);
        ((TextView) findViewById(R.id.tv_pct)).setText(String.format("%.0f%%", pct));
        ((TextView) findViewById(R.id.tv_breakdown))
                .setText("Correct: " + correct + "    Wrong: " + wrong + "    Skipped: " + (total - attempted));
        ((TextView) findViewById(R.id.tv_test_title)).setText(t.title);

        // Award XP & streak
        int xp = correct * 10 + (correct == total ? 25 : 0);
        StreakManager.get(this).onTestCompleted(xp);
        ((TextView) findViewById(R.id.tv_xp_earned)).setText("+" + xp + " XP earned");

        RecyclerView rv = findViewById(R.id.rv_review);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new ReviewAdapter(t, answers));
    }

    private static class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.VH> {
        private final QuestionBank.TestSet t;
        private final int[] answers;
        ReviewAdapter(QuestionBank.TestSet t, int[] answers) {
            this.t = t; this.answers = answers;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Question q = t.questions.get(position);
            int chosen = answers[position];
            boolean correct = chosen == q.correctIndex;

            h.tvIndex.setText("Q" + (position + 1));
            h.tvSection.setText(q.section);
            h.tvPrompt.setText(q.prompt);
            h.tvVerdict.setText(chosen < 0 ? "Skipped" : (correct ? "✔ Correct" : "✘ Wrong"));
            h.tvVerdict.setTextColor(chosen < 0 ? 0xFF607D95
                    : (correct ? 0xFF1B5E20 : 0xFFB71C1C));

            h.optionsContainer.removeAllViews();
            for (int i = 0; i < q.options.size(); i++) {
                TextView tv = new TextView(h.itemView.getContext());
                tv.setTextSize(14f);
                tv.setPadding(20, 12, 20, 12);
                String prefix = (char)('A' + i) + ".  ";
                String text = q.options.get(i);
                if (i == q.correctIndex) {
                    tv.setText(Html.fromHtml("<b>" + prefix + "</b>" + escapeHtml(text) + "  ✔"));
                    tv.setBackgroundColor(0x331B5E20);
                    tv.setTypeface(null, Typeface.BOLD);
                } else if (i == chosen) {
                    tv.setText(Html.fromHtml("<b>" + prefix + "</b>" + escapeHtml(text) + "  (your answer)"));
                    tv.setBackgroundColor(0x33B71C1C);
                } else {
                    tv.setText(prefix + text);
                }
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, 6);
                tv.setLayoutParams(lp);
                h.optionsContainer.addView(tv);
            }

            h.tvExplanation.setText(q.explanation);
        }

        private String escapeHtml(String s) {
            return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        @Override public int getItemCount() { return t.questions.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvIndex, tvSection, tvPrompt, tvVerdict, tvExplanation;
            LinearLayout optionsContainer;
            VH(@NonNull View v) {
                super(v);
                tvIndex = v.findViewById(R.id.tv_q_index);
                tvSection = v.findViewById(R.id.tv_q_section);
                tvPrompt = v.findViewById(R.id.tv_q_prompt);
                tvVerdict = v.findViewById(R.id.tv_q_verdict);
                tvExplanation = v.findViewById(R.id.tv_q_explanation);
                optionsContainer = v.findViewById(R.id.ll_options);
            }
        }
    }
}
