package com.legalstaan.app;

import android.app.Application;

public class LegalstaanApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.apply(this);
        try {
            LegalstaanFcmService.subscribeToTopics();
        } catch (Throwable ignore) {
            // Firebase not configured in some build variants — don't crash the app.
        }
        // Arm the test-rotation alarm so the 6 AM / 6 PM "fresh test ready"
        // notification fires even if the user never opens the question bank.
        try {
            TestRefreshScheduler.scheduleNext(this);
        } catch (Throwable ignore) {}
    }
}
