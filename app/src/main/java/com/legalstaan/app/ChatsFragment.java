package com.legalstaan.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatsFragment extends Fragment {

    private static final String TAG = "RutuAI";
    private static final String GEMINI_KEY = "AIzaSyCF2XJu2E68Tiuifh6sGBnsQMIrZSNPxF0";
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_KEY;

    private static final String SYSTEM_PROMPT =
            "You are an intelligent educational assistant. " +
            "You are designed to help students understand complex academic topics, summarize legal and general knowledge, and provide accurate, unbiased explanations. " +
            "Always be concise, professional, and directly answer the user's queries. " +
            "You have no specific affiliation.";

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etInput;
    private TextView tvStatus;
    private boolean isOnline = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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
        etInput      = view.findViewById(R.id.et_chat_input);
        tvStatus     = view.findViewById(R.id.tv_ai_status);
        ImageButton btnSend       = view.findViewById(R.id.btn_send);
        SwitchCompat swInternet   = view.findViewById(R.id.switch_internet);

        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        adapter = new ChatAdapter(messages);
        recyclerView.setAdapter(adapter);

        addAiMessage("Hi! I'm Rutu AI ⚖️\nAsk me about courses, legal topics, or anything about Legalstaan!\n\nTip: Toggle \"AI+Web\" for smarter answers powered by Gemini.");

        swInternet.setOnCheckedChangeListener((btn, checked) -> {
            isOnline = checked;
            if (checked) {
                tvStatus.setText("Gemini mode · AI+Web active");
                addAiMessage("Connected to Gemini AI! I can now answer much smarter questions about law, cases, African legal systems, and more.");
            } else {
                tvStatus.setText("Local mode · offline");
                addAiMessage("Switched to offline mode. I'll use my built-in knowledge to help you.");
            }
        });

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;
        etInput.setText("");
        addUserMessage(text);

        if (isOnline) {
            tvStatus.setText("Rutu is thinking...");
            callGeminiApi(text);
        } else {
            mainHandler.postDelayed(() -> {
                if (isAdded()) addAiMessage(getLocalResponse(text));
            }, 600);
        }
    }

    private void callGeminiApi(String userMessage) {
        executor.execute(() -> {
            try {
                URL url = new URL(GEMINI_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                String escapedSystem = escapeJson(SYSTEM_PROMPT);
                String escapedMsg    = escapeJson(userMessage);

                String body = "{" +
                        "\"system_instruction\":{\"parts\":[{\"text\":\"" + escapedSystem + "\"}]}," +
                        "\"contents\":[{\"parts\":[{\"text\":\"" + escapedMsg + "\"}]}]" +
                        "}";

                byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bodyBytes);
                }

                int code = conn.getResponseCode();
                if (code == 200) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                    }
                    JSONObject resp = new JSONObject(sb.toString());
                    String aiText = resp.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    mainHandler.post(() -> {
                        if (isAdded()) {
                            tvStatus.setText("Gemini mode · AI+Web active");
                            addAiMessage(aiText);
                        }
                    });
                } else {
                    // Read error body for better diagnostics
                    StringBuilder errSb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) errSb.append(line);
                    } catch (Exception ignored) {}
                    throw new Exception("HTTP " + code + ": " + errSb.toString());
                }
                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Gemini call failed", e);
                String raw = e.getMessage() != null ? e.getMessage() : e.toString();
                final String friendly = explainGeminiError(raw);
                mainHandler.post(() -> {
                    if (isAdded()) {
                        tvStatus.setText("Gemini mode · AI+Web active");
                        addAiMessage(getLocalResponse(userMessage) +
                                "\n\n⚠️ Gemini unavailable — using local knowledge.\n" + friendly);
                    }
                });
            }
        });
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

    // ── Local knowledge brain (offline fallback) ──────────────────────────────

    private String getLocalResponse(String input) {
        String q = input.toLowerCase().trim();

        if (has(q, "hello", "hi", "hey", "namaste", "hola", "salaam", "jambo"))
            return "Hello! Great to have you here 😊 Ask me about Legalstaan courses, legal topics, or anything to help your studies!";

        if (has(q, "who are you", "what are you", "rutu", "introduce yourself"))
            return "I'm Rutu AI, your Legalstaan legal-education assistant!\n\nI can help with:\n• Navigating the app (courses, live classes, mock tests)\n• Legal concepts (IPR, Constitutional, Criminal Law, etc.)\n• International law\n• Study tips & exam prep\n\nToggle \"AI+Web\" for Gemini-powered deep answers!";

        if (has(q, "what can you", "help me with", "capabilities", "features"))
            return "I can help you with:\n• App navigation and features\n• IPR topics: Trademark, Patent, Copyright, Design, Plant Variety\n• Constitutional Law, Criminal Law (IPC/BNS), Administrative Law\n• CPC, Contract, Torts\n• International legal frameworks\n• Exam strategy and study tips\n\nFor deeper research, turn on AI+Web!";

        if (has(q, "trademark", "brand", "mark"))
            return "Trademark Law has 17 lectures on Legalstaan!\n\nKey topics:\n• Definition & types of marks\n• Registration process (TM-A, TM-O forms)\n• Passing off vs. infringement\n• Distinctiveness & absolute grounds for refusal\n• International registration (Madrid Protocol)\n\nGo to Courses → Trademark Law to start.";

        if (has(q, "patent", "invention", "prior art", "novelty"))
            return "Patent Law has 35 lectures — our biggest subject!\n\nKey topics:\n• Patentability criteria: novelty, inventive step, industrial application\n• Types of patents & patent drafting\n• Section 3(d) controversy (Novartis case)\n• PCT & international filing\n• Compulsory licensing\n\nCourses → Patent Law to begin.";

        if (has(q, "copyright", "author", "literary", "artistic"))
            return "Copyright Law has 10 lectures covering:\n• Works protected under Copyright Act 1957\n• Rights of authors & performers\n• Fair use / fair dealing\n• Moral rights & economic rights\n• Digital copyright (DMCA parallels)\n\nFind it under Courses!";

        if (has(q, "design", "industrial design", "registered design"))
            return "Design Act 2000 has 8 lectures covering:\n• What qualifies as a registrable design\n• Novelty requirement\n• Registration procedure & term\n• Design infringement\n• Differences from Copyright & Trademark";

        if (has(q, "ipr", "intellectual property", "ip law"))
            return "Legalstaan covers IPR comprehensively!\n\nSubjects:\n• Trademark Law (17 lectures)\n• Patent Law (35 lectures)\n• Copyright Law (10 lectures)\n• Design Act 2000 (8 lectures)\n• Plant Variety & Farmers Rights Act\n• International Conventions & Treaties (7 lectures)\n\nAll in the Courses tab!";

        if (has(q, "constitution", "constitutional", "fundamental right", "dpsp", "article"))
            return "Constitutional Law covers:\n• Preamble & basic structure doctrine\n• Fundamental Rights (Articles 12-35)\n• Directive Principles of State Policy\n• Separation of powers\n• Emergency provisions\n• Landmark cases: Kesavananda Bharati, Maneka Gandhi, etc.\n\nCheck the Constitution subject in Courses!";

        if (has(q, "admin law", "administrative", "natural justice", "writ", "judicial review"))
            return "Administrative Law has 4 lectures on:\n• Principles of natural justice (audi alteram partem, nemo judex)\n• Delegated legislation\n• Writs: Mandamus, Certiorari, Habeas Corpus, Prohibition, Quo Warranto\n• Judicial review scope\n• Administrative discretion & its limits";

        if (has(q, "international", "treaty", "convention", "trips", "wipo", "paris convention", "berne"))
            return "International Conventions & Treaties (7 lectures) covers:\n• Paris Convention for Industrial Property (1883)\n• Berne Convention for Copyright\n• TRIPS Agreement (WTO)\n• WIPO treaties (WCT, WPPT)\n• Budapest Treaty (microorganisms)\n• CBD & Nagoya Protocol";

        if (has(q, "plant variety", "farmer", "pvp", "seed", "breeder"))
            return "Plant Variety & Farmers Rights Act:\n• Rights of plant breeders vs. farmers' traditional rights\n• UPOV Convention & India's sui generis system\n• Protection of existing varieties\n• Essential Deposit requirement\n\nAvailable in Courses!";


        if (has(q, "ipc", "criminal", "bns", "section 302", "murder", "theft", "offence"))
            return "Criminal Law (IPC/BNS) fundamentals:\n• General exceptions: mistake, necessity, private defence\n• Attempt vs. abetment\n• Offences against the person (murder, culpable homicide)\n• Property offences (theft, extortion, dacoity)\n• BNS 2023 reforms and key changes from IPC\n\nCheck Courses for full lectures!";

        if (has(q, "crpc", "bnss", "bail", "fir", "arrest", "procedure"))
            return "CrPC / BNSS procedure key points:\n• Cognizable vs. non-cognizable offences\n• FIR registration & investigation powers\n• Bail: regular, anticipatory, default bail\n• Trial procedure: warrant, summons, sessions cases\n• BNSS 2023 key changes from CrPC";

        if (has(q, "civil", "cpc", "plaint", "suit", "execution"))
            return "Civil Procedure Code (CPC) basics:\n• Jurisdiction: territorial, pecuniary, subject-matter\n• Plaint & written statement requirements\n• Order 1 (parties), Order 7 (plaint), Order 8 (defence)\n• Res judicata & res sub judice\n• Execution of decrees";

        if (has(q, "contract", "offer", "acceptance", "consideration", "agreement", "breach"))
            return "Contract Law essentials:\n• Valid contract elements: offer, acceptance, consideration, capacity, legality\n• Types: void, voidable, unenforceable\n• Discharge: performance, frustration, breach\n• Remedies: damages (Hadley v Baxendale), specific performance\n• Standard form contracts & unfair terms";

        if (has(q, "tort", "negligence", "defamation", "nuisance", "liability"))
            return "Law of Torts key areas:\n• Negligence: duty of care, breach, causation, damages\n• Strict & absolute liability (Rylands v Fletcher, MC Mehta)\n• Defamation: libel vs. slander, defences\n• Nuisance: private vs. public\n• Vicarious liability principles";

        if (has(q, "mock test", "quiz", "mcq", "practice test", "test series"))
            return "Mock tests are ready for you! 📝\n\nTap the Mock Tests card on the Home screen or the Test Series in Courses tab.\n\nTips for MCQs:\n• Read all options before choosing\n• Eliminate obviously wrong answers\n• For statute-based questions, recall the section numbers\n• Attempt all questions — no negative marking!";

        if (has(q, "live", "live class", "schedule", "join class", "streaming"))
            return "Live classes are streamed by our faculty!\n\nPlatforms used:\n• Jitsi Meet (in-app, no account needed)\n• YouTube Live (in-app viewer)\n• Google Meet (as backup)\n\nWhen a class is live, you'll see a red LIVE banner on the Home screen. Tap the Live tab to see all active sessions.";

        if (has(q, "study tip", "how to study", "exam tip", "preparation", "strategy"))
            return "Study tips from Legalstaan:\n\n1. 📚 Watch lectures in order — concepts build on each other\n2. ✍ Take notes while watching, don't just watch\n3. 🔁 After each lecture, do 10 MCQs on the topic\n4. 📱 Use the app daily — even 30 min is powerful\n5. 🗣 Discuss cases with friends\n6. 🏆 Take mock tests under timed conditions\n\nConsistency beats cramming every time!";

        if (has(q, "free material", "notes", "pdf", "study material", "download"))
            return "Free Study Materials are available on Legalstaan!\n\nTap the Free Materials card on the Home screen to access PDFs, mock exam papers, and additional notes.\n\nYou can also find them in the Courses tab under the Study Materials section.";

        if (has(q, "contact", "email", "reach", "support", "help", "team"))
            return "Reach Legalstaan at:\n📧 contactlegalstaan@gmail.com\n📸 Instagram: @legalstaan\n▶️ YouTube: @legalstaanofficial\n🌐 legalstaan.com\n\nAll social links are on the Home screen!";

        if (has(q, "faculty", "teacher", "sir", "professor", "instructor"))
            return "Legalstaan faculty:\n• Abhishek Sir\n• Aryaa Anuj Sir\n• Arya Verma Sir\n• Rohit Sir\n• Nikhil Sir\n• Alfaz Mushriff Sir\n• Susen Kamble Sir\n• Ajay Jatav Sir\n• Gautam Sir\n• Eshan Sir\n\nAll experts in their legal domains. Check their live sessions!";

        if (has(q, "how many", "total", "count", "subjects", "videos", "lectures"))
            return "Legalstaan currently offers:\n• 8 subjects\n• 100+ video lectures\n• Free study materials & PDFs\n• Mock tests & practice MCQs\n• Live classes from 10 faculty\n\nAll accessible from the Courses tab!";

        if (has(q, "dark mode", "theme", "night mode", "light mode"))
            return "Toggle Dark Mode in your Profile tab using the switch.\n\nLight mode = daytime studying\nDark mode = late-night revision without eye strain\n\nThe app will immediately update — no restart needed!";

        if (has(q, "sign out", "logout", "log out"))
            return "To sign out, go to 👤 Profile tab → scroll down → tap Sign Out.\n\nYou can sign back in with Google or email/password anytime.";

        if (has(q, "price", "cost", "fee", "free", "paid"))
            return "Legalstaan is completely FREE! 🎉\n\nAll lectures, mock tests, and study materials are open to every student.\nFor premium batch enrollment, contact us at contactlegalstaan@gmail.com.";

        if (has(q, "profile", "account", "photo", "picture", "pic", "avatar"))
            return "To update your profile:\n1. Go to Profile tab\n2. Tap your avatar or 'Change Photo'\n3. Select an image from your gallery\n\nYour photo is saved locally on your device.";

        if (has(q, "thank", "thanks", "thank you", "tysm", "appreciate"))
            return "You're very welcome! Keep studying — great lawyers are built one concept at a time. All the best on your journey! ⚖️";

        if (has(q, "bye", "goodbye", "good night", "cya", "see you"))
            return "Goodbye! Come back anytime. Happy studying and keep going strong! 📚";

        if (has(q, "motivation", "inspire", "encourage", "tired", "give up"))
            return "Don't give up! 💪\n\nEvery great lawyer, judge, and legal scholar started exactly where you are — learning concepts one by one.\n\nLaw is not just about memory; it's about understanding the why behind every rule. Legalstaan is here every step of the way. You've got this!";

        return "That's a great question! For a detailed answer, toggle AI+Web to connect to Gemini — I'll give you a much smarter response.\n\nOr reach us at contactlegalstaan@gmail.com 😊";
    }

    private boolean has(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.contains(kw)) return true;
        }
        return false;
    }

    /** Translate cryptic Gemini errors into actionable guidance. */
    private String explainGeminiError(String raw) {
        if (raw == null) return "Reason: unknown.";
        String low = raw.toLowerCase();
        if (low.contains("http 429") || low.contains("resource_exhausted") || low.contains("quota"))
            return "Reason: Free-tier quota exhausted (15 req/min, 1500/day on gemini-2.0-flash).\nFix: wait 1 min, or upgrade your Google AI Studio billing.";
        if (low.contains("http 400") && low.contains("api key not valid"))
            return "Reason: API key rejected.\nFix: regenerate at aistudio.google.com/apikey and update GEMINI_KEY in ChatsFragment.java.";
        if (low.contains("http 403") || low.contains("permission_denied"))
            return "Reason: Key blocked or referrer restriction.\nFix: in Google Cloud Console → Credentials, ensure the key has no application restriction (or allow Android package com.legalstaan.app).";
        if (low.contains("http 404") || low.contains("model"))
            return "Reason: Model name invalid (gemini-2.0-flash may have moved).\nFix: try gemini-1.5-flash or gemini-flash-latest in GEMINI_URL.";
        if (low.contains("unable to resolve host") || low.contains("failed to connect")
                || low.contains("timeout") || low.contains("etimedout"))
            return "Reason: No network or DNS failure.\nFix: check Wi-Fi / mobile data.";
        return "Reason: " + raw;
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdownNow();
    }
}
