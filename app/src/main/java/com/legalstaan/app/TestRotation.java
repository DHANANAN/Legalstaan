package com.legalstaan.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

/**
 * Twice-daily test rotation.
 *
 * The day is split into two slots — Morning (00:00–11:59) and Evening
 * (12:00–23:59). Each slot deterministically picks a fresh subset from the
 * underlying QuestionBank using a seed derived from {@code yyyymmdd-slot},
 * so every device sees the same questions in the same slot, and the next
 * slot rolls the seed forward.
 *
 * The first item in {@link #currentTestSets()} is always a "Combined Daily
 * Test" mixing questions from every subject — this is the headline test
 * pinned at the top.
 */
public class TestRotation {

    public enum Slot {
        MORNING("Morning"), EVENING("Evening");
        public final String label;
        Slot(String label) { this.label = label; }
    }

    /** How many questions per individual rotated test. */
    private static final int QUESTIONS_PER_SET = 15;

    /** How many questions in the combined all-subjects daily test. */
    private static final int COMBINED_QUESTIONS = 50;

    /** Time limit (minutes) for the combined daily test. */
    private static final int COMBINED_DURATION_MIN = 60;

    /** Wall-clock minute at which the morning slot starts (06:00 IST). */
    public static final int MORNING_HOUR = 6;
    /** Wall-clock minute at which the evening slot starts (18:00 IST). */
    public static final int EVENING_HOUR = 18;

    /** Returns the slot the user is currently in, based on local time. */
    public static Slot currentSlot() {
        Calendar c = Calendar.getInstance();
        int h = c.get(Calendar.HOUR_OF_DAY);
        return (h >= MORNING_HOUR && h < EVENING_HOUR) ? Slot.MORNING : Slot.EVENING;
    }

    /** Slot key (e.g. "20260505-MORNING") — stable for the duration of the slot. */
    public static String currentSlotKey() {
        Calendar c = Calendar.getInstance();
        // Use the start-of-slot date so evening rolls correctly across midnight.
        if (currentSlot() == Slot.EVENING && c.get(Calendar.HOUR_OF_DAY) < MORNING_HOUR) {
            c.add(Calendar.DAY_OF_YEAR, -1);
        }
        int yyyy = c.get(Calendar.YEAR);
        int mm   = c.get(Calendar.MONTH) + 1;
        int dd   = c.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d%02d%02d-%s", yyyy, mm, dd, currentSlot().name());
    }

    /** Epoch millis at which the next slot begins (used for the countdown UI). */
    public static long nextSlotEpochMillis() {
        Calendar c = Calendar.getInstance();
        Slot now = currentSlot();
        if (now == Slot.MORNING) {
            c.set(Calendar.HOUR_OF_DAY, EVENING_HOUR);
        } else {
            // Next morning — bump day if we're past evening start
            if (c.get(Calendar.HOUR_OF_DAY) >= EVENING_HOUR) {
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
            c.set(Calendar.HOUR_OF_DAY, MORNING_HOUR);
        }
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /** Friendly "Refreshes in 4h 12m" string for the question bank header. */
    public static String formatTimeUntilNextSlot() {
        long ms = nextSlotEpochMillis() - System.currentTimeMillis();
        if (ms <= 0) return "Refreshing now…";
        long mins = ms / 60_000L;
        long h    = mins / 60;
        long m    = mins % 60;
        if (h <= 0) return "Refreshes in " + m + "m";
        return "Refreshes in " + h + "h " + m + "m";
    }

    /**
     * Returns the rotated list of test sets for the current slot.
     * The first entry is always the Combined Daily Test (all subjects).
     */
    public static List<QuestionBank.TestSet> currentTestSets() {
        List<QuestionBank.TestSet> base = QuestionBank.all();
        long seed = currentSlotKey().hashCode();

        List<QuestionBank.TestSet> rotated = new ArrayList<>();

        // 1) Combined daily test — pinned at top.
        List<Question> all = new ArrayList<>();
        for (QuestionBank.TestSet t : base) all.addAll(t.questions);
        rotated.add(new QuestionBank.TestSet(
                "combined_" + currentSlotKey(),
                "Daily Combined Test · " + currentSlot().label,
                COMBINED_QUESTIONS + " questions • " + COMBINED_DURATION_MIN
                        + " min • all subjects • " + formatTimeUntilNextSlot(),
                COMBINED_DURATION_MIN,
                pickRandom(all, COMBINED_QUESTIONS, seed)));

        // 2) Per-subject rotated sets.
        for (QuestionBank.TestSet t : base) {
            List<Question> picked = pickRandom(t.questions, QUESTIONS_PER_SET, seed ^ t.id.hashCode());
            String desc = picked.size() + " questions • " + t.durationMinutes + " minutes • "
                    + currentSlot().label + " set";
            rotated.add(new QuestionBank.TestSet(
                    t.id + "_" + currentSlotKey(),
                    t.title,
                    desc,
                    t.durationMinutes,
                    picked));
        }
        return Collections.unmodifiableList(rotated);
    }

    /** Look up a rotated set by its id (e.g. for the runner activity). */
    public static QuestionBank.TestSet bySlotId(String id) {
        for (QuestionBank.TestSet t : currentTestSets()) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }

    /** Deterministically pick {@code n} questions from {@code pool} using {@code seed}. */
    private static List<Question> pickRandom(List<Question> pool, int n, long seed) {
        if (pool == null || pool.isEmpty()) return new ArrayList<>();
        List<Question> copy = new ArrayList<>(pool);
        Random r = new Random(seed);
        Collections.shuffle(copy, r);
        if (copy.size() <= n) return copy;
        return new ArrayList<>(copy.subList(0, n));
    }
}
