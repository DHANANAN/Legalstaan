package com.legalstaan.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RutuChatActivity extends AppCompatActivity {

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView recyclerView;
    private EditText etInput;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutu_chat);

        Toolbar toolbar = findViewById(R.id.toolbar_rutu);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Rutu AI");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.rv_messages);
        etInput = findViewById(R.id.et_chat_input);
        ImageButton btnSend = findViewById(R.id.btn_send);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);
        adapter = new ChatAdapter(messages);
        recyclerView.setAdapter(adapter);

        addAiMessage("Hi! I'm Rutu AI — your Legalstaan legal-education assistant!\n\nAsk me about courses, legal topics, faculty, mock tests, or anything about the app!");

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;
        etInput.setText("");
        addUserMessage(text);

        mainHandler.postDelayed(() -> addAiMessage(getLocalResponse(text)), 400);
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

    // ── Expanded local knowledge brain ──────────────────────────────────────────

    private String getLocalResponse(String input) {
        String q = input.toLowerCase().trim();

        if (has(q, "hello", "hi", "hey", "namaste", "hola", "salaam"))
            return "Hello! Great to have you here. Ask me about Legalstaan courses, legal topics, or anything to help your studies!";

        if (has(q, "who are you", "what are you", "rutu", "introduce yourself"))
            return "I'm Rutu AI, your Legalstaan legal-education assistant!\n\nI can help with:\n- Navigating the app (courses, live classes, mock tests)\n- Legal concepts (IPR, Constitutional, Criminal Law, etc.)\n- International law\n- Study tips & exam prep\n- Faculty info & contacts";

        if (has(q, "what can you", "help me with", "capabilities", "features"))
            return "I can help you with:\n- App navigation and features\n- IPR topics: Trademark, Patent, Copyright, Design, Plant Variety\n- Constitutional Law, Criminal Law (IPC/BNS), Administrative Law\n- CPC, Contract, Torts\n- International legal frameworks\n- Exam strategy and study tips\n- Faculty and contact information";

        // ── IPR Subjects ──
        if (has(q, "trademark", "brand", "mark", "tm"))
            return "Trademark Law has 17 lectures on Legalstaan!\n\nKey topics:\n- Definition & types of marks (word, device, composite, shape, sound)\n- Registration process (TM-A, TM-O forms)\n- Passing off vs. infringement\n- Distinctiveness & absolute grounds for refusal (Section 9)\n- Relative grounds for refusal (Section 11)\n- International registration (Madrid Protocol)\n- Well-known trademarks (Section 2(1)(zg))\n- Collective & certification marks\n\nGo to Courses > Trademark Law to start.";

        if (has(q, "patent", "invention", "prior art", "novelty", "section 3"))
            return "Patent Law has 35 lectures — our biggest subject!\n\nKey topics:\n- Patentability criteria: novelty, inventive step, industrial application\n- Types of patents & patent drafting\n- Section 3(d) controversy (Novartis v. Union of India)\n- PCT & international filing under Paris Convention\n- Compulsory licensing (Section 84)\n- Patent opposition: pre-grant (Section 25(1)) & post-grant (Section 25(2))\n- Patent infringement & remedies\n- Biotechnology patents & software patents debate\n- Evergreening & data exclusivity\n\nCourses > Patent Law to begin.";

        if (has(q, "copyright", "author", "literary", "artistic", "fair use"))
            return "Copyright Law has 10 lectures covering:\n- Works protected under Copyright Act 1957\n- Rights of authors & performers\n- Fair use / fair dealing (Section 52)\n- Moral rights (Section 57) & economic rights\n- Digital copyright & intermediary liability\n- Copyright in films, sound recordings, broadcasts\n- Assignment & licensing of copyright\n- Term of protection (60 years after author's death)\n- International: Berne Convention, TRIPS, WCT\n\nFind it under Courses!";

        if (has(q, "design", "industrial design", "registered design", "design act"))
            return "Design Act 2000 has 8 lectures covering:\n- What qualifies as a registrable design (Section 2(d))\n- Novelty & originality requirement\n- Registration procedure & term (15 years)\n- Design infringement & piracy (Section 22)\n- Cancellation of registration (Section 19)\n- Differences from Copyright & Trademark protection\n- Locarno Classification\n- Prior publication as bar to registration";

        if (has(q, "ipr", "intellectual property", "ip law"))
            return "Legalstaan covers IPR comprehensively!\n\nSubjects:\n- Trademark Law (17 lectures)\n- Patent Law (35 lectures)\n- Copyright Law (10 lectures)\n- Design Act 2000 (8 lectures)\n- Plant Variety & Farmers Rights Act (1 lecture)\n- International Conventions & Treaties (7 lectures)\n\nAll in the Courses tab!";

        if (has(q, "plant variety", "farmer", "pvp", "seed", "breeder"))
            return "Plant Variety & Farmers Rights Act:\n- Rights of plant breeders vs. farmers' traditional rights\n- UPOV Convention & India's sui generis system\n- Protection of Existing Varieties & Extant Varieties\n- Essential Deposit requirement\n- Researchers' rights exception\n- Duration: 18 years (trees/vines), 15 years (others)\n\nAvailable in Courses!";

        // ── Other Law Subjects ──
        if (has(q, "constitution", "constitutional", "fundamental right", "dpsp", "article", "basic structure"))
            return "Constitutional Law covers:\n- Preamble & basic structure doctrine (Kesavananda Bharati)\n- Fundamental Rights (Articles 12-35)\n- Right to Equality (Art. 14-18)\n- Freedom of Speech (Art. 19)\n- Right to Life & Personal Liberty (Art. 21)\n- Directive Principles of State Policy (Art. 36-51)\n- Fundamental Duties (Art. 51A)\n- Emergency provisions (Art. 352, 356, 360)\n- Amendment power (Art. 368)\n- Landmark cases: Maneka Gandhi, Golaknath, Minerva Mills\n\nCheck Constitution in Courses!";

        if (has(q, "admin law", "administrative", "natural justice", "writ", "judicial review", "delegated"))
            return "Administrative Law has 4 lectures on:\n- Principles of natural justice (audi alteram partem, nemo judex in causa sua)\n- Delegated legislation & its limits\n- Writs: Mandamus, Certiorari, Habeas Corpus, Prohibition, Quo Warranto\n- Judicial review: scope & grounds\n- Administrative discretion & fettering\n- Doctrine of proportionality\n- Legitimate expectation\n- Ombudsman & Lokpal";

        if (has(q, "international", "treaty", "convention", "trips", "wipo", "paris convention", "berne"))
            return "International Conventions & Treaties (7 lectures) covers:\n- Paris Convention for Industrial Property (1883)\n- Berne Convention for Copyright (1886)\n- TRIPS Agreement (WTO, 1995)\n- WIPO treaties (WCT, WPPT)\n- Budapest Treaty (microorganisms)\n- Patent Cooperation Treaty (PCT)\n- Madrid Protocol (trademarks)\n- Hague Agreement (industrial designs)\n- CBD & Nagoya Protocol (genetic resources)\n- Doha Declaration on TRIPS & Public Health";

        if (has(q, "ipc", "criminal", "bns", "section 302", "murder", "theft", "offence", "crime"))
            return "Criminal Law (IPC/BNS) fundamentals:\n- General exceptions: mistake, necessity, private defence\n- Attempt (Section 511 IPC) vs. abetment (Section 107)\n- Murder (Section 302) vs. Culpable Homicide (Section 304)\n- Theft (Section 378), Robbery, Dacoity\n- Cheating (Section 420) & Criminal Breach of Trust\n- BNS 2023 reforms: key changes from IPC\n  - Sedition replaced with offence against sovereignty\n  - Community service as punishment\n  - Enhanced penalties for mob lynching\n\nCheck Courses for full lectures!";

        if (has(q, "crpc", "bnss", "bail", "fir", "arrest", "procedure", "investigation"))
            return "CrPC / BNSS procedure key points:\n- Cognizable vs. non-cognizable offences\n- FIR registration (Section 154) & Zero FIR\n- Investigation powers of police\n- Bail: regular, anticipatory (Section 438), default bail (Section 167)\n- Trial procedure: warrant, summons, sessions cases\n- BNSS 2023 key changes from CrPC:\n  - Mandatory forensic investigation for 7+ year offences\n  - Electronic FIR & summons\n  - Time-bound trials\n  - Victim's right to be heard";

        if (has(q, "civil", "cpc", "plaint", "suit", "execution", "order", "decree"))
            return "Civil Procedure Code (CPC) basics:\n- Jurisdiction: territorial, pecuniary, subject-matter\n- Plaint (Order 7) & Written Statement (Order 8)\n- Order 1 (Parties), Order 2 (Frames of Suit)\n- Res judicata (Section 11) & Res sub judice (Section 10)\n- Temporary injunctions (Order 39)\n- Execution of decrees (Section 36-74)\n- Appeals, Revision, Review\n- Inherent powers (Section 151)";

        if (has(q, "contract", "offer", "acceptance", "consideration", "agreement", "breach", "indian contract"))
            return "Contract Law essentials:\n- Valid contract: offer, acceptance, consideration, capacity, legality\n- Types: void (Section 2(g)), voidable (Section 2(i)), unenforceable\n- Free consent: coercion, undue influence, fraud, misrepresentation, mistake\n- Performance & discharge: by performance, frustration (Section 56), breach\n- Remedies: damages (Hadley v Baxendale rule), specific performance, injunction\n- Indemnity & Guarantee (Section 124-147)\n- Agency (Section 182-238)\n- Standard form contracts & unfair terms";

        if (has(q, "tort", "negligence", "defamation", "nuisance", "liability", "damage"))
            return "Law of Torts key areas:\n- Negligence: duty of care (Donoghue v Stevenson), breach, causation, remoteness\n- Strict liability (Rylands v Fletcher)\n- Absolute liability (MC Mehta v Union of India)\n- Defamation: libel vs. slander, defences (truth, fair comment, privilege)\n- Nuisance: private vs. public\n- Trespass: to person, land, goods\n- Vicarious liability principles\n- Consumer Protection Act 2019 & tortious remedies";

        // ── App Features ──
        if (has(q, "mock test", "quiz", "mcq", "practice test", "test series"))
            return "Mock tests are ready for you!\n\nTap the Mock Tests card on the Home screen.\n\nAvailable:\n- Legal Reasoning Mocks (8 sessions)\n- Legal Reasoning AIET Pattern (14 sessions)\n\nTips:\n- Read all options before choosing\n- Eliminate obviously wrong answers\n- For statute-based questions, recall section numbers\n- Time yourself to build exam stamina";

        if (has(q, "live", "live class", "schedule", "join class", "streaming"))
            return "Live classes are streamed by our faculty!\n\nPlatforms:\n- Jitsi Meet (in-app, no account needed)\n- YouTube Live (in-app viewer)\n- Google Meet (opens in Chrome for security)\n\nWhen a class is live, you'll see a red LIVE banner on Home. Tap the Live tab to see all active sessions.";

        if (has(q, "study tip", "how to study", "exam tip", "preparation", "strategy"))
            return "Study tips from Legalstaan:\n\n1. Watch lectures in order — concepts build on each other\n2. Take notes while watching, don't just watch\n3. After each lecture, attempt related MCQs\n4. Use the app daily — even 30 min is powerful\n5. Discuss cases with friends in Community\n6. Take mock tests under timed conditions\n7. Revise landmark cases weekly\n8. Make mnemonic devices for sections & articles\n\nConsistency beats cramming!";

        if (has(q, "free material", "notes", "pdf", "study material", "download"))
            return "Free Study Materials are available on Legalstaan!\n\nTap the Free Materials card on the Home screen to access:\n- Legal Reasoning Mock PDFs\n- AIET Pattern practice papers\n- Subject-wise notes\n\nYou can also find them under Courses > Free Study Materials section.";

        if (has(q, "community", "chat", "message", "contact", "people"))
            return "The Community tab lets you:\n- Chat with faculty directly\n- Add friends by their email\n- Ask subject-related queries to experts\n- Connect with fellow law students\n\nTap on any faculty member or contact to start a conversation!";

        if (has(q, "contact", "email", "reach", "support", "help", "team"))
            return "Reach Legalstaan at:\nEmail: contactlegalstaan@gmail.com\nInstagram: @legalstaan\nYouTube: @legalstaanofficial\nWebsite: legalstaan.com\n\nAll social links are on the Home screen!";

        if (has(q, "faculty", "teacher", "sir", "professor", "instructor"))
            return "Legalstaan faculty:\n- Abhishek Sir (IPR)\n- Aryaa Anuj Sir (Constitutional Law)\n- Arya Verma Sir (Patent Law)\n- Rohit Sir (Criminal Law)\n- Nikhil Sir (Administrative Law)\n- Alfaz Mushriff Sir (Contract Law)\n- Susen Kamble Sir (Torts)\n- Ajay Jatav Sir (CPC)\n- Gautam Sir (International Law)\n- Eshan Sir (Copyright)\n\nConnect with them in the Community tab!";

        if (has(q, "how many", "total", "count", "subjects", "videos", "lectures"))
            return "Legalstaan currently offers:\n- 8 subjects with video lectures\n- 83+ video lectures total\n- Free study materials & PDFs\n- Mock tests (22 sessions)\n- Live classes from 10+ faculty\n- Community chat with faculty\n\nAll accessible from the app!";

        if (has(q, "dark mode", "theme", "night mode", "light mode"))
            return "Toggle Dark Mode in your Profile tab using the switch.\n\nLight mode = daytime studying\nDark mode = late-night revision without eye strain\n\nThe app updates immediately — no restart needed!";

        if (has(q, "sign out", "logout", "log out"))
            return "To sign out: Profile tab > scroll down > tap Sign Out.\n\nYou can sign back in with Google or email/password anytime.";

        if (has(q, "price", "cost", "fee", "free", "paid"))
            return "Legalstaan is completely FREE!\n\nAll lectures, mock tests, and study materials are open to every student.\nFor premium batch enrollment, contact contactlegalstaan@gmail.com.";

        if (has(q, "thank", "thanks", "thank you", "tysm", "appreciate"))
            return "You're very welcome! Keep studying — great lawyers are built one concept at a time. All the best on your journey!";

        if (has(q, "bye", "goodbye", "good night", "cya", "see you"))
            return "Goodbye! Come back anytime. Happy studying and keep going strong!";

        if (has(q, "motivation", "inspire", "encourage", "tired", "give up"))
            return "Don't give up!\n\nEvery great lawyer, judge, and scholar started exactly where you are — learning concepts one by one.\n\nLaw is not just about memory; it's about understanding the 'why' behind every rule. Legalstaan is here every step of the way. You've got this!";

        // ── Landmark Cases ──
        if (has(q, "novartis", "glivec", "section 3d", "evergreen"))
            return "Novartis AG v. Union of India (2013):\n- Supreme Court rejected patent for Glivec (imatinib mesylate beta crystalline form)\n- Held: mere new form of known substance doesn't qualify unless enhanced efficacy shown\n- Landmark interpretation of Section 3(d) Patents Act\n- Prevented 'evergreening' of pharmaceutical patents\n- Upheld India's right to affordable medicine";

        if (has(q, "kesavananda", "basic structure"))
            return "Kesavananda Bharati v. State of Kerala (1973):\n- 13-judge bench, decided 7:6\n- Established the Basic Structure Doctrine\n- Parliament can amend Constitution but cannot alter its basic structure\n- Basic structure includes: supremacy of Constitution, republican form, separation of powers, secular character, federal character, judicial review\n- Most important constitutional case in Indian history";

        if (has(q, "maneka gandhi", "article 21", "due process"))
            return "Maneka Gandhi v. Union of India (1978):\n- Expanded scope of Article 21 (Right to Life)\n- Right to life includes right to live with dignity\n- Any law depriving personal liberty must be just, fair, and reasonable\n- Introduced 'due process' reading into Article 21\n- Connected Articles 14, 19, and 21 as a golden triangle";

        if (has(q, "donoghue", "stevenson", "neighbour", "duty of care"))
            return "Donoghue v. Stevenson (1932):\n- 'Snail in the bottle' case\n- Established modern law of negligence\n- Lord Atkin's 'neighbour principle': you owe a duty of care to persons closely and directly affected by your act\n- Manufacturer liable to ultimate consumer even without contract\n- Foundation of product liability law worldwide";

        return "That's an interesting question! I have knowledge about:\n- All IPR subjects (Trademark, Patent, Copyright, Design)\n- Constitutional, Criminal, Administrative Law\n- Contract, Torts, CPC\n- International conventions\n- App features & navigation\n- Study tips & exam strategy\n- Landmark cases\n\nTry asking about any of these topics!";
    }

    private boolean has(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.contains(kw)) return true;
        }
        return false;
    }
}
