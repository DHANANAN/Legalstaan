package com.legalstaan.app;

import android.app.Application;

public class LegalstaanApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.apply(this);
    }
}
