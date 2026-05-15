package com.legalstaan.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Local-only payment state for courses. RBI rules make programmatic UPI
 * confirmation infeasible from a self-published Android app, so paid status
 * is recorded on-device via one of three demo unlock paths:
 *
 *   - simulated success after the user returns from a UPI app
 *   - admin/faculty "Mark as Paid" toggle for testers
 *   - the last 6 digits of a UPI transaction reference entered by the user
 *
 * Each unlock writes a {@link Receipt} (course id, amount, txn ref, source,
 * timestamp) into a JSON-encoded history that the Payments section reads.
 */
public final class PaymentManager {

    public static final String COURSE_APP_CONTENT = "app_content_course";

    public static final String SOURCE_SIMULATED  = "simulated";
    public static final String SOURCE_ADMIN      = "admin_toggle";
    public static final String SOURCE_TXN_REF    = "txn_ref";

    private static final String PREFS         = "legalstaan_payments";
    private static final String KEY_PAID_PREFIX = "paid_";
    private static final String KEY_HISTORY     = "receipt_history";

    private PaymentManager() {}

    public static boolean isPaid(Context ctx, String courseId) {
        return prefs(ctx).getBoolean(KEY_PAID_PREFIX + courseId, false);
    }

    /** Mark a course paid AND append a receipt. Same call is used by all 3 unlock paths. */
    public static Receipt markPaid(Context ctx,
                                   String courseId,
                                   String courseTitle,
                                   int amountRupees,
                                   String couponCode,
                                   String txnRef,
                                   String source) {
        Receipt r = new Receipt(
                courseId,
                courseTitle,
                amountRupees,
                couponCode == null ? "" : couponCode,
                txnRef == null ? "" : txnRef,
                source,
                System.currentTimeMillis());

        List<Receipt> all = getReceipts(ctx);
        all.add(0, r); // newest first
        SharedPreferences.Editor e = prefs(ctx).edit();
        e.putBoolean(KEY_PAID_PREFIX + courseId, true);
        e.putString(KEY_HISTORY, serialise(all));
        e.apply();
        return r;
    }

    /** Returns newest-first. Empty if user has never paid. */
    public static List<Receipt> getReceipts(Context ctx) {
        String raw = prefs(ctx).getString(KEY_HISTORY, null);
        if (raw == null || raw.isEmpty()) return new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(raw);
            List<Receipt> out = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                out.add(new Receipt(
                        o.optString("courseId"),
                        o.optString("courseTitle"),
                        o.optInt("amountRupees", 0),
                        o.optString("coupon"),
                        o.optString("txnRef"),
                        o.optString("source"),
                        o.optLong("ts", 0L)));
            }
            return out;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /** For testing — clears paid flag + receipt history. Not currently wired into UI. */
    public static void clearAll(Context ctx) {
        prefs(ctx).edit().clear().apply();
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String serialise(List<Receipt> all) {
        JSONArray arr = new JSONArray();
        for (Receipt r : all) {
            try {
                JSONObject o = new JSONObject();
                o.put("courseId",     r.courseId);
                o.put("courseTitle",  r.courseTitle);
                o.put("amountRupees", r.amountRupees);
                o.put("coupon",       r.coupon);
                o.put("txnRef",       r.txnRef);
                o.put("source",       r.source);
                o.put("ts",           r.timestamp);
                arr.put(o);
            } catch (Exception ignored) {}
        }
        return arr.toString();
    }

    public static final class Receipt {
        public final String courseId;
        public final String courseTitle;
        public final int    amountRupees;
        public final String coupon;
        public final String txnRef;
        public final String source;
        public final long   timestamp;

        Receipt(String courseId, String courseTitle, int amountRupees,
                String coupon, String txnRef, String source, long timestamp) {
            this.courseId     = courseId;
            this.courseTitle  = courseTitle;
            this.amountRupees = amountRupees;
            this.coupon       = coupon;
            this.txnRef       = txnRef;
            this.source       = source;
            this.timestamp    = timestamp;
        }

        public String formattedDate() {
            return new SimpleDateFormat("d MMM yyyy, h:mm a", Locale.ENGLISH)
                    .format(new Date(timestamp));
        }

        public String sourceLabel() {
            switch (source) {
                case SOURCE_ADMIN:     return "Admin override";
                case SOURCE_TXN_REF:   return "Txn ref verified";
                case SOURCE_SIMULATED: return "UPI return (demo)";
                default:               return source;
            }
        }
    }
}
