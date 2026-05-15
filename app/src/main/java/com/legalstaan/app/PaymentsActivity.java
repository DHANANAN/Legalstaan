package com.legalstaan.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Payments + receipt history screen reached from the Account tab.
 *
 * Simulated email receipts: every unlock through
 * {@link PaymentManager#markPaid} appends a row here with the
 * course title, amount, coupon, txn ref (if entered), source and
 * timestamp. Format mirrors a typical UPI confirmation email.
 */
public class PaymentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.apply(this);
        setContentView(R.layout.activity_payments);

        Toolbar toolbar = findViewById(R.id.toolbar_payments);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        List<PaymentManager.Receipt> receipts = PaymentManager.getReceipts(this);

        View empty = findViewById(R.id.tv_empty);
        RecyclerView rv = findViewById(R.id.rv_receipts);

        if (receipts.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(new Adapter(receipts));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<PaymentManager.Receipt> data;
        Adapter(List<PaymentManager.Receipt> data) { this.data = data; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_payment_receipt, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            PaymentManager.Receipt r = data.get(position);
            h.tvCourse.setText(r.courseTitle);
            h.tvDate.setText(r.formattedDate());
            h.tvAmount.setText("₹" + r.amountRupees);

            StringBuilder line2 = new StringBuilder();
            line2.append("From: payments@legalstaan.app\n")
                 .append("To: ").append(getUserEmail()).append("\n")
                 .append("Subject: Receipt — ").append(r.courseTitle);
            if (!r.coupon.isEmpty()) line2.append("\nCoupon: ").append(r.coupon);
            if (!r.txnRef.isEmpty()) line2.append("\nUPI txn ref: …").append(r.txnRef);
            line2.append("\nUnlocked via: ").append(r.sourceLabel());
            h.tvBody.setText(line2.toString());
        }

        @Override
        public int getItemCount() { return data.size(); }

        /**
         * The simulated email shows whichever email the Firebase user is
         * signed in with — falls back to a placeholder if unauthenticated.
         */
        private String getUserEmail() {
            try {
                com.google.firebase.auth.FirebaseUser u =
                        com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (u != null && u.getEmail() != null) return u.getEmail();
            } catch (Exception ignored) {}
            return "student@legalstaan.app";
        }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvCourse, tvDate, tvAmount, tvBody;
            VH(@NonNull View v) {
                super(v);
                tvCourse = v.findViewById(R.id.tv_receipt_course);
                tvDate   = v.findViewById(R.id.tv_receipt_date);
                tvAmount = v.findViewById(R.id.tv_receipt_amount);
                tvBody   = v.findViewById(R.id.tv_receipt_body);
            }
        }
    }
}
