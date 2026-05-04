package com.legalstaan.app;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FacultyManager {
    private static final Set<String> EMAILS = new HashSet<>(Arrays.asList(
        "dhanan2838@gmail.com",
        "abhisheknls56789@gmail.com",
        "singhpunni592@gmail.com",
        "aryaverma7355@gmail.com",
        "rs11336singh@gmail.com",
        "nikhilanand2367@gmail.com",
        "alfajsmmushrif@gmail.com",
        "susenk20@gmail.com",
        "iamajayjatav@gmail.com",
        "gautam2367@gmail.com",
        "eshan.sharma333@gmail.com"
    ));

    public static boolean isFaculty(String email) {
        return email != null && EMAILS.contains(email.trim().toLowerCase());
    }

    public static Set<String> allFacultyEmails() {
        return java.util.Collections.unmodifiableSet(EMAILS);
    }
}
