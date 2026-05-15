package com.legalstaan.app;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * "App Content Course" — paid premium content locked behind a Scan & Pay
 * UPI portal. Three unlock paths cover the fact that RBI rules prevent
 * programmatic UPI confirmation on a self-published app:
 *
 *   1. Simulated success when the user returns from a UPI app (demo-friendly).
 *   2. Admin/faculty "Mark as Paid" toggle for in-house testers.
 *   3. Manual entry of the last 6 digits of a UPI transaction reference.
 *
 * State persists in {@link PaymentManager}. On reopen, the screen jumps
 * straight to the unlocked view with the receipt summary and a button
 * into the bundled course content.
 */
public class AppContentCourseActivity extends AppCompatActivity {

    private static final String COURSE_TITLE = "App Content Course";

    private static final String COUPON_CODE      = "FLASHSALE";
    private static final int    PRICE_BASE       = 51;
    private static final int    PRICE_DISCOUNTED = 11;

    private View sectionLocked;
    private View sectionUnlocked;

    // Locked-state views
    private TextView tvMrp;
    private TextView tvFinalPrice;
    private TextView tvCouponHint;
    private View     rowDiscount;
    private View     rowCouponInput;
    private View     rowCouponApplied;
    private TextInputEditText etCoupon;
    private MaterialButton    btnApplyCoupon;
    private MaterialButton    btnContinueToPayment;
    private View     paymentSection;
    private MaterialButton btnOpenUpiApp;
    private MaterialButton btnSimulatedReturn;
    private MaterialButton btnAdminToggle;
    private MaterialButton btnSubmitTxn;
    private TextInputEditText etTxnRef;
    private TextInputLayout   tilTxnRef;

    // Unlocked-state views
    private TextView tvReceiptSummary;
    private MaterialButton btnOpenCourse;

    private boolean couponApplied  = false;
    private boolean upiAppLaunched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.apply(this);
        setContentView(R.layout.activity_app_content_course);

        Toolbar toolbar = findViewById(R.id.toolbar_app_content_course);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        sectionLocked    = findViewById(R.id.section_locked);
        sectionUnlocked  = findViewById(R.id.section_unlocked);

        bindLockedViews();
        bindUnlockedViews();

        if (PaymentManager.isPaid(this, PaymentManager.COURSE_APP_CONTENT)) {
            renderUnlocked();
        } else {
            renderLocked();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the user just bounced back from a UPI app, surface the
        // "Mark payment as completed" CTA so they have a clear next step.
        if (upiAppLaunched && !PaymentManager.isPaid(this, PaymentManager.COURSE_APP_CONTENT)) {
            upiAppLaunched = false;
            if (btnSimulatedReturn != null) {
                btnSimulatedReturn.setEnabled(true);
                btnSimulatedReturn.setAlpha(1f);
                btnSimulatedReturn.setText("I've completed the payment");
            }
        }
    }

    // ─── locked state ────────────────────────────────────────────────────────

    private void bindLockedViews() {
        tvMrp                = findViewById(R.id.tv_mrp);
        tvFinalPrice         = findViewById(R.id.tv_final_price);
        tvCouponHint         = findViewById(R.id.tv_coupon_hint);
        rowDiscount          = findViewById(R.id.row_discount);
        rowCouponInput       = findViewById(R.id.row_coupon_input);
        rowCouponApplied     = findViewById(R.id.row_coupon_applied);
        etCoupon             = findViewById(R.id.et_coupon);
        btnApplyCoupon       = findViewById(R.id.btn_apply_coupon);
        btnContinueToPayment = findViewById(R.id.btn_continue_payment);
        paymentSection       = findViewById(R.id.section_payment);
        btnOpenUpiApp        = findViewById(R.id.btn_open_upi_app);
        btnSimulatedReturn   = findViewById(R.id.btn_simulated_return);
        btnAdminToggle       = findViewById(R.id.btn_admin_toggle);
        btnSubmitTxn         = findViewById(R.id.btn_submit_txn);
        etTxnRef             = findViewById(R.id.et_txn_ref);
        tilTxnRef            = findViewById(R.id.til_txn_ref);

        btnApplyCoupon.setOnClickListener(v -> tryApplyCoupon());
        findViewById(R.id.tv_coupon_remove).setOnClickListener(v -> removeCoupon());
        btnContinueToPayment.setOnClickListener(v -> revealPaymentSection());
        btnOpenUpiApp.setOnClickListener(v -> openUpiApp());
        btnSimulatedReturn.setOnClickListener(v -> unlockVia(PaymentManager.SOURCE_SIMULATED, ""));
        btnAdminToggle.setOnClickListener(v -> confirmAdminUnlock());
        btnSubmitTxn.setOnClickListener(v -> trySubmitTxn());
    }

    private void renderLocked() {
        sectionLocked.setVisibility(View.VISIBLE);
        sectionUnlocked.setVisibility(View.GONE);
    }

    private void tryApplyCoupon() {
        if (etCoupon == null || etCoupon.getText() == null) return;
        String entered = etCoupon.getText().toString().trim().toUpperCase();
        if (entered.isEmpty()) {
            etCoupon.setError("Enter a coupon code");
            return;
        }
        if (!COUPON_CODE.equals(entered)) {
            etCoupon.setError("That code isn't valid");
            Toast.makeText(this, "Coupon not recognised. Try FLASHSALE.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        applyCoupon();
    }

    private void applyCoupon() {
        couponApplied = true;
        rowCouponInput.setVisibility(View.GONE);
        rowCouponApplied.setVisibility(View.VISIBLE);
        rowDiscount.setVisibility(View.VISIBLE);
        tvMrp.setPaintFlags(tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        tvFinalPrice.setText(rupees(PRICE_DISCOUNTED));
        tvCouponHint.setText("Discount unlocked. Tap Continue to Payment to pay ₹11.");
    }

    private void removeCoupon() {
        couponApplied = false;
        rowCouponInput.setVisibility(View.VISIBLE);
        rowCouponApplied.setVisibility(View.GONE);
        rowDiscount.setVisibility(View.GONE);
        tvMrp.setPaintFlags(tvMrp.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        tvFinalPrice.setText(rupees(PRICE_BASE));
        if (etCoupon != null) {
            etCoupon.setText("");
            etCoupon.setError(null);
        }
        tvCouponHint.setText("Tip: enter coupon code FLASHSALE to unlock the discount");
    }

    private void revealPaymentSection() {
        if (paymentSection.getVisibility() == View.VISIBLE) {
            paymentSection.requestFocus();
            return;
        }
        btnContinueToPayment.setEnabled(false);
        btnContinueToPayment.setText("Loading payment gateway…");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            paymentSection.setAlpha(0f);
            paymentSection.setTranslationY(40f);
            paymentSection.setVisibility(View.VISIBLE);
            paymentSection.animate()
                    .alpha(1f).translationY(0f).setDuration(420).start();
            btnContinueToPayment.setText("Payment gateway opened ↓");
            btnContinueToPayment.setEnabled(true);
        }, 650);
    }

    /**
     * Open a UPI app via the upi:// intent. Most UPI apps accept this and
     * pre-fill the amount + payee. We can't read the result reliably (RBI
     * deep-link callbacks are restricted), so we just remember that the
     * user left for a UPI app so we can prompt them on return.
     */
    private void openUpiApp() {
        int amount = couponApplied ? PRICE_DISCOUNTED : PRICE_BASE;
        String upi = "upi://pay?pa=krishnashelkeintern@oksbi"
                + "&pn=Krishna%20Panditrao%20Shelke"
                + "&am=" + amount
                + "&cu=INR"
                + "&tn=" + Uri.encode(COURSE_TITLE);
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(upi));
            startActivity(Intent.createChooser(i, "Pay with UPI"));
            upiAppLaunched = true;
        } catch (Exception e) {
            Toast.makeText(this, "No UPI app found. Please scan the QR instead.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void confirmAdminUnlock() {
        new AlertDialog.Builder(this)
                .setTitle("Admin override")
                .setMessage("Mark this course as paid without going through UPI? "
                        + "Use only for tester / faculty accounts.")
                .setPositiveButton("Mark as Paid",
                        (d, w) -> unlockVia(PaymentManager.SOURCE_ADMIN, ""))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void trySubmitTxn() {
        if (etTxnRef == null || etTxnRef.getText() == null) return;
        String ref = etTxnRef.getText().toString().trim();
        if (ref.length() != 6 || !ref.matches("\\d{6}")) {
            tilTxnRef.setError("Enter the last 6 digits of your UPI txn ref");
            return;
        }
        tilTxnRef.setError(null);
        unlockVia(PaymentManager.SOURCE_TXN_REF, ref);
    }

    private void unlockVia(String source, String txnRef) {
        int amount     = couponApplied ? PRICE_DISCOUNTED : PRICE_BASE;
        String coupon  = couponApplied ? COUPON_CODE : "";
        PaymentManager.markPaid(this,
                PaymentManager.COURSE_APP_CONTENT,
                COURSE_TITLE,
                amount,
                coupon,
                txnRef,
                source);
        Toast.makeText(this, "Course unlocked. Welcome in!", Toast.LENGTH_LONG).show();
        renderUnlocked();
    }

    // ─── unlocked state ──────────────────────────────────────────────────────

    private void bindUnlockedViews() {
        tvReceiptSummary = findViewById(R.id.tv_receipt_summary);
        btnOpenCourse    = findViewById(R.id.btn_open_course);
        btnOpenCourse.setOnClickListener(v -> openCourseContent());
        findViewById(R.id.btn_view_payments).setOnClickListener(v ->
                startActivity(new Intent(this, PaymentsActivity.class)));
    }

    private void renderUnlocked() {
        sectionLocked.setVisibility(View.GONE);
        sectionUnlocked.setVisibility(View.VISIBLE);

        java.util.List<PaymentManager.Receipt> all = PaymentManager.getReceipts(this);
        PaymentManager.Receipt newest = null;
        for (PaymentManager.Receipt r : all) {
            if (PaymentManager.COURSE_APP_CONTENT.equals(r.courseId)) {
                newest = r;
                break; // history is newest-first
            }
        }
        if (newest != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Paid ").append(rupees(newest.amountRupees));
            if (!newest.coupon.isEmpty()) sb.append(" · coupon ").append(newest.coupon);
            sb.append("\n").append(newest.formattedDate());
            if (!newest.txnRef.isEmpty()) sb.append(" · txn …").append(newest.txnRef);
            sb.append("\nUnlocked via ").append(newest.sourceLabel());
            tvReceiptSummary.setText(sb.toString());
        } else {
            tvReceiptSummary.setText("Course unlocked.");
        }
    }

    /**
     * The bundled course "content" reuses the SubjectVideosActivity Drive
     * viewer that powers the free study materials, so any Drive folder
     * (lectures, notes, slides) the faculty drops in is picked up live.
     * Today this points at the same Drive-synced study-materials bucket
     * the free path uses — when a dedicated App-Content-Course folder is
     * ready, swap the magic ID below for a real folder_id extra.
     */
    private void openCourseContent() {
        Intent i = new Intent(this, SubjectVideosActivity.class);
        i.putExtra(SubjectVideosActivity.EXTRA_SUBJECT_ID, "__study_materials__");
        i.putExtra(SubjectVideosActivity.EXTRA_SUBJECT_TITLE, COURSE_TITLE);
        i.putExtra(SubjectVideosActivity.EXTRA_IS_STUDY_MATERIAL, true);
        startActivity(i);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private static String rupees(int amount) {
        return "₹" + String.format(java.util.Locale.ENGLISH, "%,d", amount);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
