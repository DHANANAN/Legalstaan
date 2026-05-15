package com.legalstaan.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

/**
 * Test-run payment portal for the "Legalstaan Course".
 *
 * Two-step gated flow so the QR isn't exposed up front:
 *   1. Pricing card with coupon input. FLASHSALE drops ₹51 → ₹11.
 *   2. "Continue to Payment" CTA reveals the UPI QR + post-payment contact.
 *
 * Free content (lectures, materials, Question Bank) is unaffected.
 */
public class JoinBatchActivity extends AppCompatActivity {

    private static final String CONTACT_NAME  = "Krishna Sir";
    private static final String CONTACT_EMAIL = "krishnashelkeintern@gmail.com";

    private static final String COUPON_CODE      = "FLASHSALE";
    private static final int    PRICE_BASE       = 51;
    private static final int    PRICE_DISCOUNTED = 11;
    private static final int    COUPON_DISCOUNT  = PRICE_BASE - PRICE_DISCOUNTED; // 40

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
    private FrameLayout       glitterOverlay;
    private View     pricingCard;

    private boolean couponApplied = false;

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
        glitterOverlay       = findViewById(R.id.glitter_overlay);
        pricingCard          = findViewById(R.id.card_pricing);

        btnApplyCoupon.setOnClickListener(v -> tryApplyCoupon());
        btnContinueToPayment.setOnClickListener(v -> revealPaymentSection());
        findViewById(R.id.tv_coupon_remove).setOnClickListener(v -> removeCoupon());
        findViewById(R.id.btn_email_proof).setOnClickListener(v -> emailPaymentProof());
    }

    /**
     * Smoothly reveal the QR + post-payment contact card. Keeping the QR
     * hidden until this point gives the screen a "payment portal" feel —
     * the user actively chooses to proceed to the gateway rather than
     * having the UPI details exposed up front.
     */
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
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(420)
                    .setInterpolator(new OvershootInterpolator(1.1f))
                    .start();

            btnContinueToPayment.setText("Payment gateway opened ↓");
            btnContinueToPayment.setEnabled(true);
        }, 650);
    }

    /** Validate the entered code; on success run the discount + glitter sequence. */
    private void tryApplyCoupon() {
        if (etCoupon == null || etCoupon.getText() == null) return;
        String entered = etCoupon.getText().toString().trim().toUpperCase();
        if (entered.isEmpty()) {
            etCoupon.setError("Enter a coupon code");
            return;
        }
        if (!COUPON_CODE.equals(entered)) {
            etCoupon.setError("That code isn't valid");
            Toast.makeText(this,
                    "Coupon not recognised. Try FLASHSALE.",
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

        // Strike through the MRP so the saving reads at a glance
        tvMrp.setPaintFlags(tvMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Animate the new final price in: dip, scale-up bounce, settle
        tvFinalPrice.setText(rupees(PRICE_DISCOUNTED));
        AnimatorSet priceAnim = new AnimatorSet();
        priceAnim.playTogether(
                ObjectAnimator.ofFloat(tvFinalPrice, View.SCALE_X, 0.6f, 1.25f, 1f),
                ObjectAnimator.ofFloat(tvFinalPrice, View.SCALE_Y, 0.6f, 1.25f, 1f),
                ObjectAnimator.ofFloat(tvFinalPrice, View.ALPHA,   0f,   1f,    1f));
        priceAnim.setDuration(620);
        priceAnim.setInterpolator(new OvershootInterpolator(2.2f));
        priceAnim.start();

        // Subtle bump on the whole pricing card so the eye catches the change
        pricingCard.animate()
                .scaleX(1.02f).scaleY(1.02f).setDuration(180)
                .withEndAction(() -> pricingCard.animate()
                        .scaleX(1f).scaleY(1f).setDuration(220).start())
                .start();

        tvCouponHint.setText("Discount unlocked. Tap Continue to Payment to pay ₹11.");
        playGlitter();
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

    /**
     * Spawn ~12 sparkle TextViews ("✨") at random points on the pricing card,
     * each animating up + fading out. Lightweight, no third-party lib.
     */
    private void playGlitter() {
        if (glitterOverlay == null || pricingCard == null) return;
        // Anchor sparkles around the pricing card position
        int[] anchor = new int[2];
        pricingCard.getLocationOnScreen(anchor);
        int[] overlayLoc = new int[2];
        glitterOverlay.getLocationOnScreen(overlayLoc);
        int baseX = anchor[0] - overlayLoc[0];
        int baseY = anchor[1] - overlayLoc[1];
        int width  = pricingCard.getWidth();
        int height = pricingCard.getHeight();

        Random rnd = new Random();
        String[] glyphs = {"✨", "★", "✦", "✶"};
        Handler ui = new Handler(Looper.getMainLooper());

        for (int i = 0; i < 14; i++) {
            final int idx = i;
            ui.postDelayed(() -> spawnSparkle(
                    glyphs[rnd.nextInt(glyphs.length)],
                    baseX + rnd.nextInt(Math.max(width, 1)),
                    baseY + rnd.nextInt(Math.max(height, 1)),
                    rnd), idx * 50L);
        }
    }

    private void spawnSparkle(String glyph, int x, int y, Random rnd) {
        TextView t = new TextView(this);
        t.setText(glyph);
        t.setTextSize(18f + rnd.nextInt(10));
        // Warm gold + occasional cool variant for some sparkle variety
        int[] colors = {0xFFE8C66A, 0xFFFFD54F, 0xFFFFE082, 0xFFFFFFFF};
        t.setTextColor(colors[rnd.nextInt(colors.length)]);

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Math.max(0, x - 12);
        lp.topMargin  = Math.max(0, y - 12);
        glitterOverlay.addView(t, lp);

        float driftX = (rnd.nextFloat() - 0.5f) * 120f;
        float driftY = -80f - rnd.nextInt(140);
        long  dur    = 700L + rnd.nextInt(500);

        t.setAlpha(0f);
        t.setScaleX(0.4f);
        t.setScaleY(0.4f);
        t.animate()
                .translationXBy(driftX)
                .translationYBy(driftY)
                .alpha(1f).scaleX(1f).scaleY(1f)
                .rotation(rnd.nextInt(360) - 180f)
                .setDuration(dur / 2)
                .setInterpolator(new OvershootInterpolator(2f))
                .withEndAction(() -> t.animate()
                        .alpha(0f).scaleX(0.6f).scaleY(0.6f)
                        .setDuration(dur / 2)
                        .setInterpolator(new AccelerateInterpolator())
                        .withEndAction(() -> glitterOverlay.removeView(t))
                        .start())
                .start();
    }

    /** Hand off to Gmail (or any mail app) with subject + body pre-filled. */
    private void emailPaymentProof() {
        String subject = "Legalstaan Course — payment proof";
        String priceLine = couponApplied
                ? "₹" + PRICE_DISCOUNTED + " (with coupon FLASHSALE)"
                : "₹" + PRICE_BASE;
        String body    = "Hello " + CONTACT_NAME + ",\n\n"
                + "I have completed the UPI payment for the Legalstaan Course "
                + "(" + priceLine + ").\n\n"
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

    private static String rupees(int amount) {
        // ₹ + grouping (e.g., 15,000 / 6,500)
        return "₹" + String.format(java.util.Locale.ENGLISH, "%,d", amount);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
