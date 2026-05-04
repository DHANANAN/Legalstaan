package com.legalstaan.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * Schedules a one-shot AlarmManager wake-up at the next test-rotation
 * boundary (06:00 or 18:00 local). When it fires, the receiver posts a
 * "new test is ready" notification and re-arms itself for the slot after.
 *
 * Idempotent: calling {@link #scheduleNext(Context)} repeatedly just
 * overwrites the existing alarm, so it's safe to call from any activity's
 * onCreate / onResume.
 */
public class TestRefreshScheduler {

    private static final String CHANNEL_ID = "test_refresh";
    private static final int    REQ_CODE   = 0x715771; // arbitrary stable code
    private static final int    NOTIF_ID   = 1101;

    public static void scheduleNext(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, RefreshReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getBroadcast(
                context.getApplicationContext(), REQ_CODE, intent, flags);

        long triggerAt = TestRotation.nextSlotEpochMillis();
        // setWindow gives the OS a 5-minute window to batch the alarm — easier
        // on battery than setExact, fine for a "your test is refreshed" cue.
        long window = 5 * 60_000L;
        try {
            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, window, pi);
        } catch (Exception ignored) {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    /** Fired by AlarmManager at the slot boundary. Posts a notification + reschedules. */
    public static class RefreshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            postNotification(ctx);
            scheduleNext(ctx); // arm the *next* slot
        }
    }

    private static void postNotification(Context ctx) {
        NotificationManager nm = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Daily Test Refresh", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Twice a day, when a fresh test set is ready.");
            nm.createNotificationChannel(ch);
        }

        TestRotation.Slot slot = TestRotation.currentSlot();
        String title = "Fresh " + slot.label.toLowerCase() + " test is ready";
        String body  = "A new Combined Daily Test plus rotated subject sets — tap to start.";

        Intent open = new Intent(ctx, QuestionBankActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int piFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) piFlags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, open, piFlags);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pi);

        nm.notify(NOTIF_ID, b.build());
    }
}
