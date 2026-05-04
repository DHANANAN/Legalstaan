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

public class QuestionBankActivity extends AppCompatActivity {

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

        RecyclerView rv = findViewById(R.id.rv_test_sets);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new TestSetAdapter(QuestionBank.all()));
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
