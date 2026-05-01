package com.legalstaan.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_messages);
        etInput = view.findViewById(R.id.et_chat_input);
        ImageButton btnSend = view.findViewById(R.id.btn_send);

        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        adapter = new ChatAdapter(messages);
        recyclerView.setAdapter(adapter);

        // Welcome message from Rutu AI
        addAiMessage("Hi! I'm Rutu AI, your Legalstaan assistant ⚖️\nAsk me about courses, subjects, quizzes, or anything about the app!");

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;
        etInput.setText("");

        addUserMessage(text);

        // Simulate AI thinking delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAdded()) addAiMessage(getRutuResponse(text));
        }, 600);
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, true));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private void addAiMessage(String text) {
        messages.add(new ChatMessage(text, false));
        adapter.notifyItemInserted(messages.size() - 1);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

    private String getRutuResponse(String input) {
        String q = input.toLowerCase().trim();

        if (contains(q, "hello", "hi", "hey", "namaste", "hola"))
            return "Hello! Great to see you here. Ask me anything about Legalstaan courses or legal topics!";

        if (contains(q, "who are you", "what are you", "rutu", "introduce"))
            return "I'm Rutu AI, the Legalstaan learning assistant. I help you navigate courses, find subjects, and guide your legal studies!";

        if (contains(q, "course", "subject", "lecture", "video", "topic"))
            return "Legalstaan has 10 subjects with 105 video lectures!\n\nGo to the Courses tab to explore:\n• Constitutional Law\n• Criminal Law & IPC\n• CrPC & Evidence\n• Civil Procedure\n• Contract & Torts\n• and more!";

        if (contains(q, "quiz", "test", "mcq", "mock", "exam", "practice"))
            return "Ready to test yourself? 📝\n\nTap the Test Series card on the Home screen, or find the quiz button in the Courses tab. Good luck!";

        if (contains(q, "certificate", "completion"))
            return "Course certificates will be issued upon completing full courses. Keep watching lectures and stay consistent!";

        if (contains(q, "profile", "account", "photo", "picture", "pic"))
            return "Go to the Profile tab to:\n• Change your profile picture\n• Toggle Dark Mode\n• View Settings\n• Access Free Material";

        if (contains(q, "dark mode", "theme", "night mode", "light mode"))
            return "You can toggle Dark Mode in the Profile tab using the switch. Makes night studying much easier on your eyes!";

        if (contains(q, "download", "offline", "save"))
            return "Offline downloads are coming soon! For now, make sure you have a stable internet connection for the best streaming experience.";

        if (contains(q, "free material", "notes", "pdf", "study material"))
            return "Free study material is available in the Profile tab under 'Free Material'. Check it out for supplementary resources!";

        if (contains(q, "constitution", "constitutional", "article", "fundamental right"))
            return "Constitutional Law is one of our most popular subjects! It covers Articles, Fundamental Rights, DPSP, and more. Find it in the Courses tab.";

        if (contains(q, "ipc", "criminal", "section 302", "murder", "bns"))
            return "Criminal Law (IPC/BNS) is fully covered in Courses. It includes key offences, defences, and landmark judgements. Check it out!";

        if (contains(q, "crpc", "bnss", "procedure", "arrest", "bail", "fir"))
            return "CrPC / BNSS covers the criminal procedure from FIR to trial. All lectures are in the Courses section!";

        if (contains(q, "civil", "cpc", "suit", "plaint"))
            return "Civil Procedure Code (CPC) and related subjects are available in the Courses tab. Great for litigation-focused students!";

        if (contains(q, "contract", "consideration", "offer", "agreement"))
            return "Contract Law basics — offer, acceptance, consideration, breach — are all covered in our video lectures. Go to Courses!";

        if (contains(q, "price", "cost", "fee", "free", "paid", "enroll", "enrolment"))
            return "The app is open to all learners! For premium batch enrollment, contact us:\n📧 contactlegalstaan@gmail.com\n📸 @legalstaan on Instagram";

        if (contains(q, "contact", "email", "reach", "support", "help"))
            return "You can reach the Legalstaan team at:\n📧 contactlegalstaan@gmail.com\n📸 Instagram: @legalstaan\n▶ YouTube: @legalstaanofficial";

        if (contains(q, "social", "instagram", "youtube", "whatsapp", "website"))
            return "Find us on:\n📸 Instagram: @legalstaan\n▶ YouTube: @legalstaanofficial\n🌐 legalstaan.com\nAll links are on the Home screen!";

        if (contains(q, "schedule", "live", "batch", "class", "timing"))
            return "Live class schedules are announced on our Instagram and WhatsApp channel. Check the Home screen for links!";

        if (contains(q, "how many", "total", "count", "subjects", "videos"))
            return "Legalstaan has 10 subjects and 105 total video lectures — all accessible from the Courses tab!";

        if (contains(q, "settings"))
            return "Settings are in the Profile tab. You can change your profile picture, toggle dark mode, and manage app preferences there.";

        if (contains(q, "sign out", "logout", "log out"))
            return "To sign out, go to the Profile tab and scroll down to the Sign Out option.";

        if (contains(q, "thank", "thanks", "thank you", "tysm"))
            return "You're welcome! Keep studying hard — great lawyers are made through consistent effort. All the best! ⚖️";

        if (contains(q, "bye", "goodbye", "good night", "cya"))
            return "Goodbye! Come back anytime you need help. Happy studying! 📚";

        if (contains(q, "good morning", "good afternoon", "good evening"))
            return "Good day! Hope your studies are going well. What can I help you with today?";

        if (contains(q, "motivation", "inspire", "study tips", "tips"))
            return "Stay consistent! Even 1 hour of focused study each day compounds into mastery. Legal careers are built on clarity of concepts — Legalstaan has you covered! 💪";

        return "That's a good question! For detailed legal topics, check the relevant course in the Courses tab. For other help, reach us at contactlegalstaan@gmail.com 😊";
    }

    private boolean contains(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.contains(kw)) return true;
        }
        return false;
    }
}
