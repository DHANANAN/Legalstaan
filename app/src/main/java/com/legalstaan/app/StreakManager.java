package com.legalstaan.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class StreakManager {

    private static final String PREFS = "streak_prefs";
    private static final String K_XP = "xp";
    private static final String K_STREAK = "streak";
    private static final String K_LAST_DAY = "last_day";
    private static final String K_TESTS = "tests_taken";

    private static StreakManager INSTANCE;
    public static synchronized StreakManager get(Context c) {
        if (INSTANCE == null) INSTANCE = new StreakManager(c.getApplicationContext());
        return INSTANCE;
    }

    private final SharedPreferences sp;
    private StreakManager(Context c) {
        sp = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int getXp()           { return sp.getInt(K_XP, 0); }
    public int getStreak()       { return sp.getInt(K_STREAK, 0); }
    public int getTestsTaken()   { return sp.getInt(K_TESTS, 0); }
    public int getLevel()        { return 1 + getXp() / 200; }
    public int getXpInLevel()    { return getXp() % 200; }
    public int getXpForNextLvl() { return 200; }

    public void onTestCompleted(int xpEarned) {
        int today = todayKey();
        int lastDay = sp.getInt(K_LAST_DAY, 0);
        int streak = sp.getInt(K_STREAK, 0);

        if (lastDay == today) {
            // already counted today
        } else if (lastDay == today - 1) {
            streak += 1;
        } else {
            streak = 1;
        }

        int newXp = sp.getInt(K_XP, 0) + xpEarned;
        int newTests = sp.getInt(K_TESTS, 0) + 1;

        sp.edit()
                .putInt(K_XP, newXp)
                .putInt(K_STREAK, streak)
                .putInt(K_LAST_DAY, today)
                .putInt(K_TESTS, newTests)
                .apply();

        mirrorToFirestore(newXp, streak, newTests);
    }

    private void mirrorToFirestore(int xp, int streak, int tests) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("xp", xp);
        data.put("streak", streak);
        data.put("tests_taken", tests);
        data.put("display_name", user.getDisplayName() != null ? user.getDisplayName() : "Aspirant");
        data.put("photo_url", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
        data.put("uid", user.getUid());
        FirebaseFirestore.getInstance()
                .collection("leaderboard").document(user.getUid())
                .set(data, SetOptions.merge());
    }

    private static int todayKey() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) * 10000 + c.get(Calendar.MONTH) * 100 + c.get(Calendar.DAY_OF_MONTH);
    }
}
