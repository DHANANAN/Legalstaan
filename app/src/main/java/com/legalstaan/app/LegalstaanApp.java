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
    }
}
