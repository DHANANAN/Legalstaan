package com.legalstaan.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Receives FCM pushes and posts a notification.
 * Devices subscribe to the "live_classes" topic at app start (see LegalstaanApp).
 * Faculty trigger pushes via Firebase console / Cloud Functions when they go live.
 */
public class LegalstaanFcmService extends FirebaseMessagingService {

    private static final String TAG = "LegalstaanFCM";
    private static final String CHANNEL_ID = "live_classes";
    private static final int NOTIF_ID = 1001;

    @Override
    public void onMessageReceived(RemoteMessage msg) {
        super.onMessageReceived(msg);
        Log.d(TAG, "FCM payload: " + msg.getData());

        String title = "Live class starting";
        String body  = "Tap to join the session";

        if (msg.getNotification() != null) {
            if (msg.getNotification().getTitle() != null) title = msg.getNotification().getTitle();
            if (msg.getNotification().getBody() != null)  body  = msg.getNotification().getBody();
        }
        Map<String, String> data = msg.getData();
        if (data.containsKey("title")) title = data.get("title");
        if (data.containsKey("body"))  body  = data.get("body");

        showNotification(title, body);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed FCM token: " + token);
        // Token is auto-mapped to subscribed topics; no per-user storage needed for topic broadcasts.
    }

    private void showNotification(String title, String body) {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Live Classes", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Notifies when a faculty goes live");
            nm.createNotificationChannel(ch);
        }

        Intent open = new Intent(this, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, flags);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        nm.notify(NOTIF_ID, b.build());
    }

    /** Called from LegalstaanApp.onCreate() */
    public static void subscribeToTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic("live_classes");
    }
}
