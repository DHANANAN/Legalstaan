package com.legalstaan.app;

import java.util.ArrayList;
import java.util.List;

public class Question {
    public final String id;
    public final String section;
    public final String prompt;
    public final List<String> options;
    public final int correctIndex;
    public final String explanation;

    public Question(String id, String section, String prompt,
                    List<String> options, int correctIndex, String explanation) {
        this.id = id;
        this.section = section;
        this.prompt = prompt;
        this.options = options;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
    }

    public static List<String> opts(String... arr) {
        List<String> list = new ArrayList<>(arr.length);
        for (String s : arr) list.add(s);
        return list;
    }
}
