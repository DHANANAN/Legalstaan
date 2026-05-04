package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Arrays;

public class QuestionRunnerActivity extends AppCompatActivity {

    public static final String EXTRA_TEST_ID = "test_id";

    private QuestionBank.TestSet testSet;
    private int[] answers;          // -1 unanswered
    private int currentIndex = 0;
    private CountDownTimer timer;
    private long remainingMs;

    private TextView tvProgress, tvSection, tvPrompt, tvTimer;
    private RadioGroup rg;
    private Button btnPrev, btnNext, btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_runner);

        String id = getIntent().getStringExtra(EXTRA_TEST_ID);
        // Slot-rotated IDs (e.g. "ipr_mixed_1_20260505-MORNING") aren't in the
        // static bank — check the current rotation first, then fall back.
        testSet = TestRotation.bySlotId(id);
        if (testSet == null) testSet = QuestionBank.byId(id);
        if (testSet == null) {
            Toast.makeText(this, "Test set not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(testSet.title);
        }
        toolbar.setNavigationOnClickListener(v -> confirmExit());

        tvProgress = findViewById(R.id.tv_progress);
        tvSection  = findViewById(R.id.tv_section);
        tvPrompt   = findViewById(R.id.tv_prompt);
        tvTimer    = findViewById(R.id.tv_timer);
        rg         = findViewById(R.id.rg_options);
        btnPrev    = findViewById(R.id.btn_prev);
        btnNext    = findViewById(R.id.btn_next);
        btnSubmit  = findViewById(R.id.btn_submit);

        answers = new int[testSet.questions.size()];
        Arrays.fill(answers, -1);

        btnPrev.setOnClickListener(v -> { saveCurrent(); if (currentIndex > 0) { currentIndex--; render(); } });
        btnNext.setOnClickListener(v -> { saveCurrent(); if (currentIndex < testSet.questions.size() - 1) { currentIndex++; render(); } });
        btnSubmit.setOnClickListener(v -> { saveCurrent(); confirmSubmit(); });

        remainingMs = testSet.durationMinutes * 60_000L;
        startTimer();
        render();
    }

    private void render() {
        Question q = testSet.questions.get(currentIndex);
        tvProgress.setText("Q " + (currentIndex + 1) + " / " + testSet.questions.size());
        tvSection.setText(q.section);
        tvPrompt.setText(q.prompt);

        rg.setOnCheckedChangeListener(null);
        rg.removeAllViews();
        for (int i = 0; i < q.options.size(); i++) {
            RadioButton rb = new RadioButton(this);
            rb.setId(i + 1);
            rb.setText(Html.fromHtml("<b>" + (char)('A' + i) + ".</b>  " + escapeHtml(q.options.get(i))));
            rb.setTextSize(15f);
            rb.setPadding(16, 18, 16, 18);
            rg.addView(rb);
        }
        if (answers[currentIndex] >= 0) rg.check(answers[currentIndex] + 1);

        btnPrev.setEnabled(currentIndex > 0);
        boolean isLast = currentIndex == testSet.questions.size() - 1;
        btnNext.setVisibility(isLast ? View.GONE : View.VISIBLE);
        btnSubmit.setVisibility(isLast ? View.VISIBLE : View.GONE);
    }

    private String escapeHtml(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void saveCurrent() {
        int checked = rg.getCheckedRadioButtonId();
        answers[currentIndex] = checked == -1 ? -1 : checked - 1;
    }

    private void startTimer() {
        timer = new CountDownTimer(remainingMs, 1000) {
            @Override public void onTick(long ms) {
                remainingMs = ms;
                long min = ms / 60_000L;
                long sec = (ms / 1000L) % 60L;
                tvTimer.setText(String.format("%02d:%02d", min, sec));
                if (ms < 60_000L) tvTimer.setTextColor(0xFFCC0000);
            }
            @Override public void onFinish() {
                Toast.makeText(QuestionRunnerActivity.this, "Time up — auto-submitting", Toast.LENGTH_SHORT).show();
                saveCurrent();
                submit();
            }
        }.start();
    }

    private void confirmSubmit() {
        int unanswered = 0;
        for (int a : answers) if (a < 0) unanswered++;
        new AlertDialog.Builder(this)
                .setTitle("Submit test?")
                .setMessage(unanswered == 0
                        ? "All " + answers.length + " questions answered."
                        : unanswered + " question(s) unanswered. Submit anyway?")
                .setPositiveButton("Submit", (d, w) -> submit())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submit() {
        if (timer != null) timer.cancel();
        Intent i = new Intent(this, QuestionResultActivity.class);
        i.putExtra(QuestionResultActivity.EXTRA_TEST_ID, testSet.id);
        i.putExtra(QuestionResultActivity.EXTRA_ANSWERS, answers);
        startActivity(i);
        finish();
    }

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle("Exit test?")
                .setMessage("Your progress will be lost.")
                .setPositiveButton("Exit", (d, w) -> finish())
                .setNegativeButton("Stay", null)
                .show();
    }

    @Override public void onBackPressed() { confirmExit(); }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }
}
