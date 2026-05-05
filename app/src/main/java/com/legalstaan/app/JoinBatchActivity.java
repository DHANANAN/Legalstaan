package com.legalstaan.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Enrollment screen for the CLAT PG 2024 Champions batch.
 *
 * Free content (lectures, materials, Question Bank) is unaffected — this is
 * a separate, optional path for students who want to formally join the
 * mentored batch and support the NGO. Pricing structure:
 *   ₹15,000 base − ₹8,500 NGO supporter coupon = ₹6,500 final.
 *
 * Payment flow: scan the bundled UPI QR (krishnashelkeintern@oksbi), then
 * email the screenshot to Abhishek Yadav for confirmation.
 */
public class JoinBatchActivity extends AppCompatActivity {

    private static final String CONTACT_NAME  = "Abhishek Yadav";
    private static final String CONTACT_EMAIL = "abhi635611@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.apply(this);
        setContentView(R.layout.activity_join_batch);

        Toolbar toolbar = findViewById(R.id.toolbar_join_batch);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Strikethrough the MRP so the discount reads at a glance.
        android.widget.TextView tvMrp = findViewById(R.id.tv_mrp);
        tvMrp.setPaintFlags(tvMrp.getPaintFlags()
                | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

        MaterialButton btnEmail = findViewById(R.id.btn_email_proof);
        btnEmail.setOnClickListener(v -> emailPaymentProof());
    }

    /** Hand off to Gmail (or any mail app) with subject + body pre-filled. */
    private void emailPaymentProof() {
        String subject = "Batch enrollment — payment proof";
        String body    = "Hello " + CONTACT_NAME + ",\n\n"
                + "I have completed the UPI payment for the CLAT PG 2024 Champions "
                + "Preparation Course (₹6,500 after the NGO supporter coupon).\n\n"
                + "Please attach your payment screenshot before sending.\n\n"
                + "Full name: \n"
                + "Phone number: \n"
                + "City / state: \n\n"
                + "Thank you,\n";

        Intent mail = new Intent(Intent.ACTION_SENDTO);
        mail.setData(Uri.parse("mailto:"));
        mail.putExtra(Intent.EXTRA_EMAIL, new String[]{CONTACT_EMAIL});
        mail.putExtra(Intent.EXTRA_SUBJECT, subject);
        mail.putExtra(Intent.EXTRA_TEXT, body);

        try {
            Intent gmail = new Intent(mail);
            gmail.setPackage("com.google.android.gm");
            startActivity(gmail);
        } catch (Exception ignored) {
            try {
                startActivity(Intent.createChooser(mail, "Email " + CONTACT_NAME));
            } catch (Exception e) {
                Toast.makeText(this,
                        "No email app installed on this device", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
