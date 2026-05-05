package com.legalstaan.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QuestionBank {

    public static class TestSet {
        public final String id;
        public final String title;
        public final String description;
        public final int durationMinutes;
        public final List<Question> questions;
        public TestSet(String id, String title, String description,
                       int durationMinutes, List<Question> questions) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.durationMinutes = durationMinutes;
            this.questions = questions;
        }
    }

    private static List<TestSet> CACHE;

    public static synchronized List<TestSet> all() {
        if (CACHE == null) CACHE = build();
        return CACHE;
    }

    public static TestSet byId(String id) {
        for (TestSet t : all()) if (t.id.equals(id)) return t;
        return null;
    }

    private static List<TestSet> build() {
        List<TestSet> sets = new ArrayList<>();

        sets.add(new TestSet(
                "clat_legal_1",
                "CLAT Legal Reasoning – Set 1",
                "20 questions • 25 minutes • Constitutional & Contract Law focus",
                25,
                clatLegalSet1()));

        sets.add(new TestSet(
                "ipr_mixed_1",
                "IPR Practice – Trademark + Copyright",
                "15 questions • 20 minutes • mixed IPR concepts",
                20,
                iprSet1()));

        sets.add(new TestSet(
                "constitution_1",
                "Constitutional Law – Fundamental Rights",
                "12 questions • 15 minutes • Articles 14–32",
                15,
                constitutionSet1()));

        sets.add(new TestSet(
                "legal_aptitude_1",
                "Legal Aptitude – Principles & Facts",
                "10 questions • 15 minutes • principle-application style",
                15,
                aptitudeSet1()));

        // ── Mock Examination Series (1000-Q reference bank) ──
        sets.add(new TestSet(
                "mes_constitution",
                "Constitutional Law – Articles & Cases",
                "30 questions • 35 minutes • Articles 14–32 + landmark judgments",
                35,
                mesConstitutionSet()));

        sets.add(new TestSet(
                "mes_ipr",
                "IPR – Trademarks & Copyright",
                "30 questions • 35 minutes • full IPR coverage",
                35,
                mesIprSet()));

        sets.add(new TestSet(
                "mes_doctrines",
                "Latin Maxims & Doctrines",
                "30 questions • 35 minutes • doctrines, maxims, principles",
                35,
                mesDoctrinesSet()));

        sets.add(new TestSet(
                "mes_torts",
                "Tort, Negligence & Procedure",
                "20 questions • 25 minutes • principle-application + tort cases",
                25,
                mesTortsSet()));

        return Collections.unmodifiableList(sets);
    }

    private static List<Question> clatLegalSet1() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("clat1_1", "Constitutional Law",
                "Which Article of the Indian Constitution guarantees the right to constitutional remedies?",
                Question.opts("Article 19", "Article 21", "Article 32", "Article 14"),
                2,
                "Article 32 is itself a fundamental right and is called the 'heart and soul of the Constitution' by Dr. B.R. Ambedkar."));

        q.add(new Question("clat1_2", "Constitutional Law",
                "The Doctrine of Basic Structure was propounded in:",
                Question.opts("Golak Nath v. State of Punjab", "Kesavananda Bharati v. State of Kerala",
                        "Minerva Mills v. Union of India", "Indira Gandhi v. Raj Narain"),
                1,
                "Kesavananda Bharati (1973) — 13-judge bench held that Parliament cannot amend the basic structure of the Constitution."));

        q.add(new Question("clat1_3", "Contract Law",
                "An agreement enforceable by law is a:",
                Question.opts("Promise", "Contract", "Offer", "Acceptance"),
                1,
                "Section 2(h) of the Indian Contract Act, 1872 defines a contract as an agreement enforceable by law."));

        q.add(new Question("clat1_4", "Contract Law",
                "Consideration must move at the desire of:",
                Question.opts("The promisee", "The promisor", "Either party", "A third party"),
                1,
                "Section 2(d): consideration must be at the desire of the promisor — not voluntary acts."));

        q.add(new Question("clat1_5", "Tort Law",
                "The maxim 'volenti non fit injuria' means:",
                Question.opts("Wrong without injury", "He who consents cannot complain",
                        "Damage without legal injury", "Let the buyer beware"),
                1,
                "Voluntary assumption of risk — a person who consents cannot later sue for the resulting harm."));

        q.add(new Question("clat1_6", "Constitutional Law",
                "The right to privacy was declared a fundamental right in:",
                Question.opts("M.P. Sharma case", "Kharak Singh case",
                        "K.S. Puttaswamy v. Union of India", "Maneka Gandhi case"),
                2,
                "Puttaswamy (2017) — 9-judge bench unanimously recognized privacy as part of Article 21."));

        q.add(new Question("clat1_7", "Criminal Law",
                "Mens rea means:",
                Question.opts("Guilty act", "Guilty mind", "Strict liability", "Vicarious liability"),
                1,
                "Mens rea — the guilty mental state required to constitute most criminal offences."));

        q.add(new Question("clat1_8", "Constitutional Law",
                "Which Schedule of the Constitution deals with the anti-defection law?",
                Question.opts("Eighth Schedule", "Ninth Schedule",
                        "Tenth Schedule", "Eleventh Schedule"),
                2,
                "The 52nd Amendment (1985) added the Tenth Schedule, dealing with disqualification on the ground of defection."));

        q.add(new Question("clat1_9", "Contract Law",
                "An offer lapses on:",
                Question.opts("Death of the offeror only",
                        "Reasonable time elapsing without acceptance",
                        "Express revocation only",
                        "Counter-offer only"),
                1,
                "Section 6 — an offer lapses on the elapse of a reasonable time, among other grounds. All listed grounds individually may also cause lapse, but the cleanest single answer is reasonable time."));

        q.add(new Question("clat1_10", "Tort Law",
                "Strict liability was first laid down in:",
                Question.opts("Donoghue v. Stevenson", "Rylands v. Fletcher",
                        "M.C. Mehta v. Union of India", "Bhopal Gas Tragedy case"),
                1,
                "Rylands v. Fletcher (1868) — non-natural use of land that escapes and causes harm gives rise to strict liability."));

        q.add(new Question("clat1_11", "Criminal Law",
                "The Indian Penal Code, 1860 was drafted under the chairmanship of:",
                Question.opts("Lord Macaulay", "Lord Curzon",
                        "Lord Bentinck", "Sir William Jones"),
                0,
                "Lord Macaulay chaired the First Law Commission that drafted the IPC."));

        q.add(new Question("clat1_12", "Constitutional Law",
                "The Preamble was amended by the:",
                Question.opts("24th Amendment", "42nd Amendment",
                        "44th Amendment", "73rd Amendment"),
                1,
                "The 42nd Amendment (1976) inserted 'Socialist', 'Secular' and 'Integrity' into the Preamble."));

        q.add(new Question("clat1_13", "Contract Law",
                "A contract by a minor is:",
                Question.opts("Voidable", "Void ab initio",
                        "Valid", "Unenforceable but ratifiable on majority"),
                1,
                "Mohori Bibee v. Dharmodas Ghose — a minor's contract is void ab initio."));

        q.add(new Question("clat1_14", "Tort Law",
                "'Res ipsa loquitur' applies when:",
                Question.opts("There is direct evidence of negligence",
                        "The thing speaks for itself",
                        "There is no duty of care",
                        "Damage is too remote"),
                1,
                "Where the accident is such that it would not occur without negligence, the burden shifts to the defendant."));

        q.add(new Question("clat1_15", "Constitutional Law",
                "Directive Principles of State Policy are contained in:",
                Question.opts("Part III", "Part IV", "Part IVA", "Part V"),
                1,
                "Part IV (Articles 36–51) — DPSPs guide the State in policy-making but are non-justiciable."));

        q.add(new Question("clat1_16", "Criminal Law",
                "Section 302 of the IPC deals with:",
                Question.opts("Culpable homicide", "Murder",
                        "Attempt to murder", "Causing death by negligence"),
                1,
                "Section 302 prescribes punishment for murder — death or imprisonment for life with fine."));

        q.add(new Question("clat1_17", "Contract Law",
                "Quasi-contracts are dealt with under which sections of the Indian Contract Act?",
                Question.opts("Sections 56–58", "Sections 68–72",
                        "Sections 73–75", "Sections 124–129"),
                1,
                "Sections 68–72 cover certain relations resembling those created by contract — i.e., quasi-contracts."));

        q.add(new Question("clat1_18", "Tort Law",
                "Vicarious liability applies in the relationship of:",
                Question.opts("Friend and friend", "Master and servant",
                        "Buyer and seller", "Landlord and stranger"),
                1,
                "A master is liable for torts committed by their servant in the course of employment."));

        q.add(new Question("clat1_19", "Constitutional Law",
                "Article 14 guarantees:",
                Question.opts("Equality before law and equal protection of laws",
                        "Right to freedom of speech",
                        "Right to life and personal liberty",
                        "Right against exploitation"),
                0,
                "Article 14 — equality before the law and equal protection of the laws within the territory of India."));

        q.add(new Question("clat1_20", "Criminal Law",
                "The principle of double jeopardy is found in:",
                Question.opts("Article 20(1)", "Article 20(2)",
                        "Article 20(3)", "Article 21"),
                1,
                "Article 20(2) — no person shall be prosecuted and punished for the same offence more than once."));

        return q;
    }

    private static List<Question> iprSet1() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("ipr1_1", "Trademark",
                "The Trade Marks Act, 1999 came into force on:",
                Question.opts("15 September 2003", "15 September 1999",
                        "26 January 2000", "1 April 2003"),
                0,
                "The 1999 Act came into force on 15 September 2003, replacing the Trade and Merchandise Marks Act, 1958."));

        q.add(new Question("ipr1_2", "Trademark",
                "A trademark in India is initially registered for a period of:",
                Question.opts("5 years", "7 years", "10 years", "20 years"),
                2,
                "Section 25 — registration is for 10 years, renewable indefinitely in 10-year blocks."));

        q.add(new Question("ipr1_3", "Trademark",
                "'Passing off' is a remedy available for:",
                Question.opts("Registered trademarks only",
                        "Unregistered trademarks",
                        "Patents", "Copyrights"),
                1,
                "Passing off is a common-law tort protecting the goodwill of an unregistered mark."));

        q.add(new Question("ipr1_4", "Copyright",
                "Copyright in India subsists for the author's lifetime plus:",
                Question.opts("50 years", "60 years", "70 years", "100 years"),
                1,
                "Section 22 — lifetime + 60 years from the year of the author's death (literary, dramatic, musical, artistic works)."));

        q.add(new Question("ipr1_5", "Copyright",
                "Which of the following is NOT protected by copyright in India?",
                Question.opts("Computer programs", "Cinematograph films",
                        "Ideas", "Sound recordings"),
                2,
                "Copyright protects the expression of ideas — not the ideas themselves (idea-expression dichotomy)."));

        q.add(new Question("ipr1_6", "Trademark",
                "An absolute ground for refusal of trademark registration is:",
                Question.opts("Similarity to an earlier mark",
                        "Mark devoid of distinctive character",
                        "Likelihood of confusion",
                        "Bad faith of opponent"),
                1,
                "Section 9 — absolute grounds include lack of distinctive character. Similarity falls under relative grounds (Section 11)."));

        q.add(new Question("ipr1_7", "Copyright",
                "Fair dealing in India is governed by:",
                Question.opts("Section 51", "Section 52",
                        "Section 53", "Section 54"),
                1,
                "Section 52 lists acts that do not constitute infringement — i.e., fair dealing exceptions."));

        q.add(new Question("ipr1_8", "Trademark",
                "A 'well-known trademark' is defined under:",
                Question.opts("Section 2(1)(zg)", "Section 11(2)",
                        "Section 29(4)", "All of the above are relevant"),
                3,
                "2(1)(zg) defines it; 11(2) gives it cross-class protection; 29(4) provides infringement remedy across goods/services."));

        q.add(new Question("ipr1_9", "Copyright",
                "The 'work for hire' doctrine in Indian copyright generally vests ownership in:",
                Question.opts("The employee/author",
                        "The employer (in absence of contrary agreement)",
                        "The Government", "Joint ownership by default"),
                1,
                "Section 17(c) — for works made in the course of employment, the employer is the first owner unless otherwise agreed."));

        q.add(new Question("ipr1_10", "Trademark",
                "Infringement of trademark requires:",
                Question.opts("Use in the course of trade",
                        "Personal use only",
                        "Use in a foreign country",
                        "Educational use"),
                0,
                "Section 29 — infringement is triggered by use 'in the course of trade'."));

        q.add(new Question("ipr1_11", "Copyright",
                "Moral rights of an author include:",
                Question.opts("Right of paternity and integrity",
                        "Right to royalties only",
                        "Right of distribution only",
                        "Right of import"),
                0,
                "Section 57 — special rights of paternity (attribution) and integrity (objection to distortion)."));

        q.add(new Question("ipr1_12", "Trademark",
                "The Madrid Protocol relates to:",
                Question.opts("International registration of trademarks",
                        "International patent filing",
                        "Copyright reciprocity",
                        "Plant variety protection"),
                0,
                "Madrid Protocol — single international application for trademark protection in multiple jurisdictions."));

        q.add(new Question("ipr1_13", "Copyright",
                "First Indian copyright legislation was enacted in:",
                Question.opts("1847", "1914", "1957", "2012"),
                2,
                "The Copyright Act, 1957 is the current principal statute (heavily amended in 2012)."));

        q.add(new Question("ipr1_14", "Trademark",
                "A device mark consists primarily of:",
                Question.opts("Words", "Logos / graphical elements",
                        "Sounds", "Smells"),
                1,
                "Device marks are graphical/figurative — distinguished from word marks."));

        q.add(new Question("ipr1_15", "Copyright",
                "Statutory licensing for cover versions is provided in:",
                Question.opts("Section 30", "Section 31C",
                        "Section 31D", "Section 32"),
                1,
                "Section 31C (added 2012) — statutory licence for making cover versions of sound recordings."));

        return q;
    }

    private static List<Question> constitutionSet1() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("c1_1", "Fundamental Rights",
                "Article 14 forbids:",
                Question.opts("All classification", "Class legislation",
                        "Reasonable classification", "Affirmative action"),
                1,
                "Article 14 forbids class legislation but permits reasonable classification with intelligible differentia."));

        q.add(new Question("c1_2", "Fundamental Rights",
                "Article 15(3) permits the State to make special provisions for:",
                Question.opts("Religious minorities",
                        "Women and children",
                        "Scheduled Tribes only",
                        "Linguistic minorities"),
                1,
                "Article 15(3) — State may make special provisions for women and children."));

        q.add(new Question("c1_3", "Fundamental Rights",
                "Freedom of the press in India is:",
                Question.opts("Expressly guaranteed under Article 19",
                        "Implied within Article 19(1)(a)",
                        "Guaranteed under Article 21",
                        "Not a fundamental right"),
                1,
                "There is no express clause; the SC has read it into 19(1)(a) (Romesh Thappar, Sakal Papers)."));

        q.add(new Question("c1_4", "Fundamental Rights",
                "Reasonable restrictions on Article 19 freedoms are listed in:",
                Question.opts("Article 19(1)", "Article 19(2) to 19(6)",
                        "Article 20", "Article 22"),
                1,
                "Clauses (2) to (6) of Article 19 enumerate the permissible restrictions."));

        q.add(new Question("c1_5", "Fundamental Rights",
                "The right to die was rejected as part of Article 21 in:",
                Question.opts("P. Rathinam case", "Gian Kaur case",
                        "Aruna Shanbaug case", "Common Cause case"),
                1,
                "Gian Kaur v. State of Punjab (1996) — overruled P. Rathinam; right to life does not include right to die."));

        q.add(new Question("c1_6", "Fundamental Rights",
                "Article 21A provides for:",
                Question.opts("Right to information",
                        "Right to education for children 6–14",
                        "Right to food",
                        "Right to housing"),
                1,
                "86th Amendment (2002) — free and compulsory education for children aged 6–14."));

        q.add(new Question("c1_7", "Fundamental Rights",
                "Preventive detention is dealt with under:",
                Question.opts("Article 20", "Article 21",
                        "Article 22", "Article 23"),
                2,
                "Article 22 contains both rights of arrested persons and the preventive-detention framework."));

        q.add(new Question("c1_8", "Fundamental Rights",
                "Article 25 guarantees:",
                Question.opts("Right to manage religious affairs",
                        "Freedom of conscience and free profession of religion",
                        "Right to cultural autonomy",
                        "Right against religious tax"),
                1,
                "Article 25 — freedom of conscience and free profession, practice and propagation of religion."));

        q.add(new Question("c1_9", "Fundamental Rights",
                "Untouchability is abolished by:",
                Question.opts("Article 14", "Article 15",
                        "Article 17", "Article 23"),
                2,
                "Article 17 — abolishes untouchability and forbids its practice in any form."));

        q.add(new Question("c1_10", "Fundamental Rights",
                "Article 23 prohibits:",
                Question.opts("Untouchability",
                        "Traffic in human beings and forced labour",
                        "Child labour in factories",
                        "Religious discrimination"),
                1,
                "Article 23 — prohibition of traffic in human beings and forced labour (begar)."));

        q.add(new Question("c1_11", "Fundamental Rights",
                "Writ of habeas corpus translates to:",
                Question.opts("'You may have the body'",
                        "'We command'",
                        "'To be certified'",
                        "'What warrant'"),
                0,
                "Habeas corpus — 'you may have the body'; produces a detained person before the court."));

        q.add(new Question("c1_12", "Fundamental Rights",
                "A writ of mandamus cannot be issued against:",
                Question.opts("A public authority",
                        "The President or Governor",
                        "A statutory body",
                        "A subordinate court"),
                1,
                "Mandamus cannot lie against the President or Governor in exercise of constitutional functions."));

        return q;
    }

    private static List<Question> aptitudeSet1() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("la1_1", "Principle Application",
                "Principle: A master is liable for the wrongful acts of a servant done in the course of employment. " +
                        "Facts: A driver employed by X, while driving X's car on a personal errand, knocks down a pedestrian. Is X liable?",
                Question.opts("Yes — master is always liable",
                        "No — the act was outside the course of employment",
                        "Yes — the driver was employed",
                        "No — the pedestrian was negligent"),
                1,
                "Personal errand falls outside the course of employment, so vicarious liability does not attach."));

        q.add(new Question("la1_2", "Principle Application",
                "Principle: Consideration must be lawful. Facts: A promises to pay B Rs 1,000 if B refrains from filing a criminal complaint against A. Is the agreement valid?",
                Question.opts("Yes — there is consideration",
                        "No — consideration is unlawful (stifling prosecution)",
                        "Yes — both parties benefit",
                        "Voidable at B's option"),
                1,
                "Stifling prosecution is opposed to public policy — Section 23 makes the consideration unlawful."));

        q.add(new Question("la1_3", "Principle Application",
                "Principle: Acceptance must be communicated to the offeror. Facts: A writes an offer to B. B writes acceptance and posts the letter, which is lost in transit. Is there a contract?",
                Question.opts("Yes — postal rule applies",
                        "No — acceptance never reached A",
                        "Voidable", "Quasi-contract"),
                0,
                "Adams v. Lindsell — postal rule: acceptance is complete when the letter is posted, even if lost."));

        q.add(new Question("la1_4", "Principle Application",
                "Principle: Volenti non fit injuria — voluntary risk bars action. Facts: A spectator at a cricket match is hit by a ball. Can he sue?",
                Question.opts("Yes — duty of care", "No — assumed risk of the game",
                        "Yes — strict liability", "Depends on stadium design"),
                1,
                "Assumption of inherent risk in attending a cricket match defeats the claim."));

        q.add(new Question("la1_5", "Principle Application",
                "Principle: Necessity is a defence in tort. Facts: A pulls down B's wall to stop a fire from spreading to neighbouring houses. Is A liable?",
                Question.opts("Yes — trespass to property",
                        "No — defence of necessity",
                        "Liable for nominal damages only",
                        "Liable to neighbours, not to B"),
                1,
                "Public/private necessity is a complete defence where action is reasonable to avert greater harm."));

        q.add(new Question("la1_6", "Principle Application",
                "Principle: Mistake of fact may negate mens rea. Facts: A shoots a bush, honestly believing it to be a wild animal, and kills B hidden behind it. Liability for murder?",
                Question.opts("Yes — strict liability for death",
                        "No — honest mistake negates intent for murder",
                        "Yes — knowledge is presumed",
                        "Liable for attempt only"),
                1,
                "Genuine mistake of fact removes mens rea for murder; lesser charge of negligence may still apply."));

        q.add(new Question("la1_7", "Principle Application",
                "Principle: An agreement in restraint of trade is void. Facts: On selling his bakery, A agrees not to open a competing bakery within 2 km for 3 years. Valid?",
                Question.opts("Void — restraint of trade",
                        "Valid — reasonable goodwill clause exception",
                        "Void only after 1 year",
                        "Valid only if registered"),
                1,
                "Section 27 exception — sale of goodwill permits reasonable restraint."));

        q.add(new Question("la1_8", "Principle Application",
                "Principle: Defamation requires publication to a third person. Facts: A writes a defamatory letter about B and shows it only to B. Defamation?",
                Question.opts("Yes", "No — no third-party publication",
                        "Yes if intent existed", "Yes — written form is enough"),
                1,
                "Without communication to a third party, no defamation lies."));

        q.add(new Question("la1_9", "Principle Application",
                "Principle: Ignorantia juris non excusat. Facts: A foreign tourist is unaware that carrying a knife above 6 inches is illegal in India and is caught with one. Defence?",
                Question.opts("Yes — foreigner exception",
                        "No — ignorance of law is no excuse",
                        "Yes if first offence",
                        "Yes — no mens rea"),
                1,
                "Ignorance of law is no excuse — Section 76 IPC covers mistake of fact, not of law."));

        q.add(new Question("la1_10", "Principle Application",
                "Principle: A minor's contract is void ab initio. Facts: A 16-year-old buys a luxury watch on credit; the seller sues for the price. Result?",
                Question.opts("Decree against the minor",
                        "Suit fails — contract void",
                        "Decree against the parent",
                        "Recovery from minor's estate only"),
                1,
                "Mohori Bibee — minor's agreement is wholly void; no recovery in contract."));

        return q;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Mock Examination Series — extracted from the 1000-Q Legalstaan PDF.
    // Duplicates from the source were collapsed; ~110 unique items grouped
    // into four themed sets feed the rotation pool.
    // ─────────────────────────────────────────────────────────────────────

    private static List<Question> mesConstitutionSet() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("mc_1", "Fundamental Rights",
                "Which Article of the Indian Constitution guarantees the Right to Equality?",
                Question.opts("Article 12", "Article 14", "Article 19", "Article 21"), 1,
                "Article 14 — equality before law and equal protection of the laws."));
        q.add(new Question("mc_2", "Fundamental Rights",
                "Which Article provides Right to Freedom of Speech?",
                Question.opts("Article 15", "Article 16", "Article 19(1)(a)", "Article 21"), 2,
                "Article 19(1)(a) — freedom of speech and expression."));
        q.add(new Question("mc_3", "Fundamental Rights",
                "Which Article provides Right to Life and Personal Liberty?",
                Question.opts("Article 19", "Article 20", "Article 21", "Article 22"), 2,
                "Article 21 — no person shall be deprived of life or personal liberty except by procedure established by law."));
        q.add(new Question("mc_4", "Fundamental Rights",
                "Which Article empowers the Supreme Court to issue writs?",
                Question.opts("Article 32", "Article 226", "Article 14", "Article 21"), 0,
                "Article 32 — heart and soul of the Constitution per Dr. Ambedkar."));
        q.add(new Question("mc_5", "Fundamental Rights",
                "Which Article guarantees Freedom of Religion?",
                Question.opts("Article 19", "Article 21", "Article 25", "Article 32"), 2,
                "Article 25 — freedom of conscience and free profession of religion."));
        q.add(new Question("mc_6", "Fundamental Rights",
                "Which Article abolishes Untouchability?",
                Question.opts("Article 15", "Article 16", "Article 17", "Article 18"), 2,
                "Article 17 — abolishes untouchability and forbids its practice."));
        q.add(new Question("mc_7", "Fundamental Rights",
                "Which Article provides Right against Exploitation?",
                Question.opts("Articles 23 & 24", "Article 19", "Article 21", "Article 32"), 0,
                "Articles 23 (forced labour) and 24 (child labour) together."));
        q.add(new Question("mc_8", "Fundamental Rights",
                "Which Article provides Right to Education?",
                Question.opts("Article 19", "Article 21A", "Article 22", "Article 32"), 1,
                "Article 21A added by 86th Amendment, 2002 — free and compulsory education for ages 6–14."));
        q.add(new Question("mc_9", "Fundamental Rights",
                "Which Article provides Right to Freedom of Assembly?",
                Question.opts("Article 19(1)(b)", "Article 21", "Article 32", "Article 25"), 0,
                "Article 19(1)(b) — assembly peaceably and without arms."));
        q.add(new Question("mc_10", "Fundamental Rights",
                "Which Article provides Right to Freedom of Profession?",
                Question.opts("Article 19(1)(g)", "Article 21", "Article 32", "Article 25"), 0,
                "Article 19(1)(g) — profession, occupation, trade or business."));
        q.add(new Question("mc_11", "Fundamental Rights",
                "Which Article provides Right to Freedom of Movement throughout India?",
                Question.opts("Article 19(1)(d)", "Article 21", "Article 32", "Article 25"), 0,
                "Article 19(1)(d) guarantees free movement throughout India."));
        q.add(new Question("mc_12", "Fundamental Rights",
                "Which Article provides Right to Freedom of Residence?",
                Question.opts("Article 19(1)(e)", "Article 21", "Article 32", "Article 25"), 0,
                "Article 19(1)(e) — reside and settle in any part of India."));
        q.add(new Question("mc_13", "Fundamental Rights",
                "Which Article provides Right to Freedom of Association?",
                Question.opts("Article 19(1)(c)", "Article 21", "Article 32", "Article 25"), 0,
                "Article 19(1)(c) — form associations or unions."));
        q.add(new Question("mc_14", "Fundamental Rights",
                "Which Article prohibits discrimination on grounds of religion, race, caste, sex, or place of birth?",
                Question.opts("Article 14", "Article 15", "Article 16", "Article 17"), 1,
                "Article 15 prohibits discrimination on these specific grounds."));
        q.add(new Question("mc_15", "Fundamental Rights",
                "Which Article provides Right to Equality of Opportunity in Public Employment?",
                Question.opts("Article 16", "Article 14", "Article 15", "Article 17"), 0,
                "Article 16 — equality in matters of public employment."));
        q.add(new Question("mc_16", "Fundamental Rights",
                "Which Article provides Cultural and Educational Rights?",
                Question.opts("Articles 29 & 30", "Article 19", "Article 21", "Article 32"), 0,
                "Articles 29–30 protect rights of minorities to conserve culture and run educational institutions."));
        q.add(new Question("mc_17", "Fundamental Rights",
                "Which of the following is NOT a Fundamental Right (after the 44th Amendment)?",
                Question.opts("Right to Equality", "Right to Property",
                        "Right to Freedom", "Right against Exploitation"), 1,
                "Right to Property was removed from FRs by the 44th Amendment (1978) and is now a constitutional right under Article 300A."));
        q.add(new Question("mc_18", "Landmark Cases",
                "Which case established the doctrine of basic structure?",
                Question.opts("Golaknath v. State of Punjab",
                        "Kesavananda Bharati v. State of Kerala",
                        "Maneka Gandhi v. Union of India",
                        "Indira Gandhi v. Raj Narain"), 1,
                "Kesavananda Bharati (1973) — 13-judge bench, basic structure cannot be amended away."));
        q.add(new Question("mc_19", "Landmark Cases",
                "Which case recognized Right to Privacy as part of Article 21?",
                Question.opts("A.K. Gopalan", "Maneka Gandhi",
                        "K.S. Puttaswamy v. Union of India", "Shreya Singhal"), 2,
                "Puttaswamy (2017) — 9-judge bench unanimously recognized privacy."));
        q.add(new Question("mc_20", "Landmark Cases",
                "Which case recognized Right to Internet as part of Article 19?",
                Question.opts("Shreya Singhal", "Puttaswamy",
                        "Anuradha Bhasin v. Union of India", "Maneka Gandhi"), 2,
                "Anuradha Bhasin (2020) — internet shutdowns must satisfy proportionality."));
        q.add(new Question("mc_21", "Landmark Cases",
                "Which case recognized Right to Information under Article 19?",
                Question.opts("State of UP v. Raj Narain", "Kesavananda Bharati",
                        "Indira Gandhi v. Raj Narain", "Shreya Singhal"), 0,
                "Raj Narain (1975) read RTI into freedom of speech."));
        q.add(new Question("mc_22", "Landmark Cases",
                "Which case recognized Right to Livelihood under Article 21?",
                Question.opts("Olga Tellis v. Bombay Municipal Corporation", "Maneka Gandhi",
                        "Puttaswamy", "Shreya Singhal"), 0,
                "Olga Tellis (1985) — pavement dwellers; livelihood is integral to life."));
        q.add(new Question("mc_23", "Landmark Cases",
                "Which case recognized Right to Shelter under Article 21?",
                Question.opts("Chameli Singh v. State of Uttar Pradesh", "Maneka Gandhi",
                        "Puttaswamy", "Shreya Singhal"), 0,
                "Chameli Singh — shelter as a basic human right."));
        q.add(new Question("mc_24", "Landmark Cases",
                "Which case recognized Right to Education under Article 21A?",
                Question.opts("Unnikrishnan v. State of Andhra Pradesh", "Maneka Gandhi",
                        "Puttaswamy", "Shreya Singhal"), 0,
                "Unnikrishnan (1993) — education flows from Article 21."));
        q.add(new Question("mc_25", "Landmark Cases",
                "Which case recognized Right to Clean Environment under Article 21?",
                Question.opts("M.C. Mehta v. Union of India", "Maneka Gandhi",
                        "Puttaswamy", "Shreya Singhal"), 0,
                "M.C. Mehta — clean environment, oleum gas leak / Ganga pollution PILs."));
        q.add(new Question("mc_26", "Landmark Cases",
                "Which case recognized Right to Clean Water under Article 21?",
                Question.opts("Subhash Kumar v. State of Bihar", "Maneka Gandhi",
                        "Puttaswamy", "Shreya Singhal"), 0,
                "Subhash Kumar — water pollution PIL; clean water under Article 21."));
        q.add(new Question("mc_27", "Landmark Cases",
                "Which case recognized Right to Health under Article 21?",
                Question.opts("Paschim Banga Khet Mazdoor Samity v. State of West Bengal",
                        "Maneka Gandhi", "Puttaswamy", "Shreya Singhal"), 0,
                "Paschim Banga (1996) — emergency medical care is part of right to life."));
        q.add(new Question("mc_28", "Landmark Cases",
                "The principle of judicial review was established in:",
                Question.opts("Marbury v. Madison", "Kesavananda Bharati",
                        "Golaknath", "Indira Gandhi v. Raj Narain"), 0,
                "Marbury v. Madison (1803) — Chief Justice John Marshall."));
        q.add(new Question("mc_29", "Landmark Cases",
                "Which case introduced the doctrine of prospective overruling?",
                Question.opts("Golaknath v. State of Punjab", "Kesavananda Bharati",
                        "Maneka Gandhi", "Indira Gandhi v. Raj Narain"), 0,
                "Golaknath (1967) — Chief Justice Subba Rao introduced the technique."));
        q.add(new Question("mc_30", "Landmark Cases",
                "The basic structure doctrine was reaffirmed by striking down the 42nd Amendment in:",
                Question.opts("Kesavananda Bharati", "Minerva Mills v. Union of India",
                        "Golaknath", "Maneka Gandhi"), 1,
                "Minerva Mills (1980) struck down Sections 4 and 55 of the 42nd Amendment."));
        return q;
    }

    private static List<Question> mesIprSet() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("mi_1", "Trademark",
                "Which symbol indicates a registered trademark?",
                Question.opts("™", "©", "®", "℗"), 2,
                "® denotes a registered mark; ™ is for unregistered marks claimed in trade."));
        q.add(new Question("mi_2", "Trademark",
                "'Passing off' in trademark law refers to:",
                Question.opts("Selling counterfeit goods as genuine", "Registering a mark",
                        "Licensing a mark", "Abandoning a mark"), 0,
                "Common-law tort protecting the goodwill of an unregistered mark."));
        q.add(new Question("mi_3", "Trademark",
                "Trademark dilution refers to:",
                Question.opts("Weakening of a famous mark's distinctiveness",
                        "Passing off goods", "Registering similar marks", "Licensing marks"), 0,
                "Dilution by blurring or tarnishment — Section 29(4) TM Act."));
        q.add(new Question("mi_4", "Trademark",
                "Trademark infringement occurs when:",
                Question.opts("Unauthorized use of identical or similar mark",
                        "Registration of new mark", "Licensing of mark", "Abandonment of mark"), 0,
                "Section 29 — use 'in the course of trade'."));
        q.add(new Question("mi_5", "Trademark",
                "Deceptive similarity in trademark law means:",
                Question.opts("Marks likely to confuse consumers", "Marks that are identical",
                        "Marks that are descriptive", "Marks that are generic"), 0,
                "Likelihood of consumer confusion is the test for deceptive similarity."));
        q.add(new Question("mi_6", "Trademark",
                "Trademark law protects:",
                Question.opts("Literary works", "Distinctive signs and marks",
                        "Musical compositions", "Industrial designs"), 1,
                "TM protects source-identifying signs that distinguish goods/services."));
        q.add(new Question("mi_7", "Trademark",
                "The Madrid Protocol relates to:",
                Question.opts("Patents", "Trademarks", "Copyright", "Trade secrets"), 1,
                "Single international application for trademark protection in multiple jurisdictions."));
        q.add(new Question("mi_8", "Trademark",
                "The Paris Convention relates to:",
                Question.opts("Human rights", "Intellectual property",
                        "Environmental law", "Trade agreements"), 1,
                "Paris Convention 1883 — industrial property (patents, trademarks, designs)."));
        q.add(new Question("mi_9", "Trademark",
                "Collective marks are used by:",
                Question.opts("Individual businesses", "Associations or groups",
                        "Government agencies", "Private persons"), 1,
                "Section 61 TM Act — marks of associations for member-distinguishing purposes."));
        q.add(new Question("mi_10", "Trademark",
                "Certification marks indicate:",
                Question.opts("Quality or standard certified by authority", "Ownership of mark",
                        "Registration of mark", "Abandonment of mark"), 0,
                "Section 2(1)(e) — guarantees a particular characteristic (origin, quality, etc.)."));
        q.add(new Question("mi_11", "Trademark",
                "Generic marks are:",
                Question.opts("Not registrable", "Easily registrable",
                        "Protected automatically", "Licensed freely"), 0,
                "Section 9 absolute grounds — generic terms cannot identify a single source."));
        q.add(new Question("mi_12", "Trademark",
                "Well-known marks are protected even:",
                Question.opts("Without registration", "Only with registration",
                        "Only with licensing", "Only with assignment"), 0,
                "Section 11(6) TM Act 1999 — cross-class statutory protection."));
        q.add(new Question("mi_13", "Trademark",
                "Honest concurrent use allows:",
                Question.opts("Registration of similar marks used honestly by different parties",
                        "Exclusive ownership of identical marks",
                        "Cancellation of prior registration",
                        "Transfer of mark to government"), 0,
                "Section 12 TM Act permits this on grounds of honest concurrent use."));
        q.add(new Question("mi_14", "Trademark",
                "Genericide occurs when:",
                Question.opts("Trademark becomes generic due to public use",
                        "Trademark is abandoned voluntarily",
                        "Trademark is transferred", "Trademark is licensed excessively"), 0,
                "Loss of distinctiveness through ubiquitous descriptive use (e.g., escalator, aspirin)."));
        q.add(new Question("mi_15", "Trademark",
                "Reverse passing off occurs when:",
                Question.opts("One sells another's goods as his own", "One sells his goods under another's mark",
                        "One copies packaging only", "One uses identical slogans"), 0,
                "Misrepresenting the origin of someone else's goods as one's own."));
        q.add(new Question("mi_16", "Trademark",
                "Transborder reputation in trademark law was recognized in:",
                Question.opts("N.R. Dongre v. Whirlpool Corporation", "Cadbury v. ITC",
                        "Bata v. Relaxo", "Dabur v. Colgate"), 0,
                "Whirlpool case established that reputation can spillover even without local sales."));
        q.add(new Question("mi_17", "Trademark",
                "Non-use cancellation of a registered mark occurs when:",
                Question.opts("Registered mark is not used for five years",
                        "Registered mark is used excessively",
                        "Registered mark is transferred", "Registered mark is licensed abroad"), 0,
                "Section 47 TM Act — five years and three months of non-use is grounds for removal."));
        q.add(new Question("mi_18", "Copyright",
                "Copyright protects which of the following?",
                Question.opts("Ideas", "Expressions", "Patents", "Trademarks"), 1,
                "Idea-expression dichotomy — only fixed creative expression is protected."));
        q.add(new Question("mi_19", "Copyright",
                "The Berne Convention relates to:",
                Question.opts("Patents", "Copyright", "Trademarks", "Trade secrets"), 1,
                "Berne Convention 1886 — international copyright reciprocity."));
        q.add(new Question("mi_20", "Copyright",
                "Copyright in literary works in India lasts for:",
                Question.opts("20 years", "50 years", "Lifetime + 60 years", "70 years"), 2,
                "Section 22 — author's lifetime + 60 years from year of death."));
        q.add(new Question("mi_21", "Copyright",
                "Copyright in cinematograph films lasts for:",
                Question.opts("20 years from publication", "50 years",
                        "60 years from publication", "70 years"), 2,
                "Section 26 — 60 years from year of publication."));
        q.add(new Question("mi_22", "Copyright",
                "In copyright law, fair dealing allows:",
                Question.opts("Limited use without permission", "Unlimited use without permission",
                        "Licensing of work", "Transfer of rights"), 0,
                "Section 52 — limited use for research, criticism, news reporting, etc."));
        q.add(new Question("mi_23", "Copyright",
                "Copyright infringement occurs when:",
                Question.opts("Unauthorized copying or distribution", "Licensing of work",
                        "Registration of work", "Transfer of rights"), 0,
                "Section 51 — exclusive rights are violated by unauthorized acts."));
        q.add(new Question("mi_24", "Copyright",
                "Moral rights in copyright protect:",
                Question.opts("Author's reputation and integrity of work", "Economic rights",
                        "Transfer rights", "Patent rights"), 0,
                "Section 57 — special rights of paternity (attribution) and integrity (against distortion)."));
        q.add(new Question("mi_25", "Copyright",
                "Adaptation in copyright law refers to:",
                Question.opts("Creating a new work based on existing work", "Licensing work",
                        "Transferring rights", "Registering work"), 0,
                "Right to make derivative works (translations, dramatizations, etc.)."));
        q.add(new Question("mi_26", "Copyright",
                "Derivative work in copyright law means:",
                Question.opts("Work based on existing work", "Original creation",
                        "Licensed work", "Registered work"), 0,
                "Adaptations, translations, abridgments — needs authorization from original copyright owner."));
        q.add(new Question("mi_27", "Copyright",
                "Performer's rights in copyright law protect:",
                Question.opts("Performers against unauthorized recording or broadcast",
                        "Authors against plagiarism", "Publishers against piracy",
                        "Producers against duplication"), 0,
                "Section 38 — exclusive rights to performers over their performances."));
        q.add(new Question("mi_28", "Copyright",
                "Compulsory licensing in copyright law allows:",
                Question.opts("Use of work without owner's consent under statutory conditions",
                        "Free public use of all works", "Exclusive rights to government", "Transfer of ownership"), 0,
                "Sections 31, 31A — for withheld works or unpublished/unknown authors."));
        q.add(new Question("mi_29", "Copyright",
                "'Work for hire' doctrine vests ownership in:",
                Question.opts("Employer (in absence of contrary agreement)", "Employee/author",
                        "Government", "Joint ownership by default"), 0,
                "Section 17(c) — employer is the first owner of works made in the course of employment."));
        q.add(new Question("mi_30", "Copyright",
                "Idea-expression dichotomy means:",
                Question.opts("Ideas cannot be copyrighted, only expressions can",
                        "Both ideas and expressions are protected", "Ideas are public domain",
                        "Expressions are not protected"), 0,
                "Foundational copyright principle — RG Anand v. Delux Films."));
        return q;
    }

    private static List<Question> mesDoctrinesSet() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("md_1", "Latin Maxim",
                "'Ignorantia juris non excusat' means:",
                Question.opts("Ignorance of fact is excusable", "Ignorance of law is no excuse",
                        "Law must be obeyed", "Justice delayed is justice denied"), 1,
                "Knowing the law is presumed; only mistake of fact (Section 76 IPC) excuses."));
        q.add(new Question("md_2", "Latin Maxim",
                "'Audi alteram partem' means:",
                Question.opts("No man shall be punished twice", "Hear the other side",
                        "Justice delayed is justice denied", "Ignorance of law is no excuse"), 1,
                "Core principle of natural justice — both sides must be heard."));
        q.add(new Question("md_3", "Latin Maxim",
                "'Nemo judex in causa sua' means:",
                Question.opts("No one should be judge in his own cause", "Ignorance of law is no excuse",
                        "Justice delayed is justice denied", "No one should be punished twice"), 0,
                "Rule against bias — second pillar of natural justice."));
        q.add(new Question("md_4", "Latin Maxim",
                "'Res ipsa loquitur' means:",
                Question.opts("Wrong without injury", "He who consents cannot complain",
                        "The thing speaks for itself", "Let the buyer beware"), 2,
                "Negligence inferred where the accident itself would not occur without negligence."));
        q.add(new Question("md_5", "Latin Maxim",
                "'Res judicata' means:",
                Question.opts("Matter already decided cannot be reopened", "Ignorance of law is no excuse",
                        "Justice delayed is justice denied", "No one should be punished twice"), 0,
                "Section 11 CPC — bars re-litigation of issues already decided between same parties."));
        q.add(new Question("md_6", "Latin Maxim",
                "'Volenti non fit injuria' applies when:",
                Question.opts("Consent is given", "Consent is absent",
                        "Negligence occurs", "Strict liability applies"), 0,
                "Voluntary assumption of risk — consent negates liability."));
        q.add(new Question("md_7", "Latin Maxim",
                "'Damnum sine injuria' means:",
                Question.opts("Damage without legal injury", "Injury without damage",
                        "Both damage and injury", "No damage, no injury"), 0,
                "Real loss but no violation of legal right — no remedy (Gloucester Grammar School)."));
        q.add(new Question("md_8", "Latin Maxim",
                "'Injuria sine damno' means:",
                Question.opts("Legal injury without damage", "Damage without injury",
                        "Both damage and injury", "No damage, no injury"), 0,
                "Violation of legal right with no actual loss — still actionable (Ashby v. White)."));
        q.add(new Question("md_9", "Latin Maxim",
                "'Ubi jus ibi remedium' means:",
                Question.opts("Where there is a right, there is a remedy", "Ignorance of law is no excuse",
                        "Justice delayed is justice denied", "No one should be punished twice"), 0,
                "Foundational principle — legal right must have a remedy."));
        q.add(new Question("md_10", "Latin Maxim",
                "'Actus non facit reum nisi mens sit rea' means:",
                Question.opts("Act alone does not make a person guilty unless mind is also guilty",
                        "Ignorance of law is no excuse", "Justice delayed is justice denied",
                        "No one should be punished twice"), 0,
                "Foundation of mens rea — both guilty act AND guilty mind needed."));
        q.add(new Question("md_11", "Latin Maxim",
                "'Salus populi suprema lex' means:",
                Question.opts("Welfare of the people is the supreme law", "Ignorance of law is no excuse",
                        "Justice delayed is justice denied", "No one should be punished twice"), 0,
                "Public welfare overrides individual interests in emergencies."));
        q.add(new Question("md_12", "Latin Maxim",
                "'Lex non cogit ad impossibilia' means:",
                Question.opts("Law does not compel the impossible", "Law compels all duties",
                        "Law ignores impossibility", "Law applies universally"), 0,
                "Parties are not bound to perform something genuinely impossible."));
        q.add(new Question("md_13", "Latin Maxim",
                "'Lex posterior derogat priori' means:",
                Question.opts("Later law repeals earlier law", "Earlier law overrides later law",
                        "Law applies equally to all", "Law ignores contradictions"), 0,
                "Implied repeal — statutes in conflict; the later prevails."));
        q.add(new Question("md_14", "Latin Maxim",
                "'Lex specialis derogat legi generali' means:",
                Question.opts("Special law overrides general law", "General law overrides special law",
                        "Law applies equally to all", "Law ignores contradictions"), 0,
                "When statutes conflict, specific provisions trump general ones."));
        q.add(new Question("md_15", "Latin Maxim",
                "'Lex loci delicti' refers to:",
                Question.opts("Law of the place where tort occurred", "Law of the place of contract",
                        "Law of the place of residence", "Law of the place of trial"), 0,
                "Choice-of-law rule for cross-border torts."));
        q.add(new Question("md_16", "Latin Maxim",
                "'Actus curiae neminem gravabit' means:",
                Question.opts("An act of the court shall prejudice no one", "Law favors diligent parties",
                        "Ignorance of law is no excuse", "Justice delayed is justice denied"), 0,
                "Courts will correct their own errors so no party is prejudiced."));
        q.add(new Question("md_17", "Doctrine",
                "The doctrine of 'pith and substance' is used in:",
                Question.opts("Criminal law", "Constitutional law",
                        "Contract law", "Tort law"), 1,
                "Used to determine true subject of legislation when subject overlaps lists."));
        q.add(new Question("md_18", "Doctrine",
                "The doctrine of 'colorable legislation' applies when:",
                Question.opts("Legislature exceeds its power indirectly", "Judiciary interferes with legislature",
                        "Executive overrides judiciary", "Parliament amends Constitution"), 0,
                "What cannot be done directly cannot be done indirectly."));
        q.add(new Question("md_19", "Doctrine",
                "The doctrine of 'severability' applies when:",
                Question.opts("Invalid part of law can be separated from valid part",
                        "Entire law is struck down", "Judiciary overrides legislature",
                        "Parliament amends Constitution"), 0,
                "Article 13(1)(2) — only the unconstitutional portion is struck down."));
        q.add(new Question("md_20", "Doctrine",
                "The doctrine of 'harmonious construction' applies when:",
                Question.opts("Conflicting provisions are interpreted to give effect to both",
                        "Entire law is struck down", "Judiciary overrides legislature",
                        "Parliament amends Constitution"), 0,
                "Two clauses are read together so neither is rendered nugatory."));
        q.add(new Question("md_21", "Doctrine",
                "The doctrine of 'eminent domain' allows:",
                Question.opts("State to acquire private property for public purpose",
                        "Citizens to acquire public property", "Judiciary to override legislature",
                        "Parliament to amend Constitution"), 0,
                "Sovereign power, with compensation, for public purpose (LARR Act 2013)."));
        q.add(new Question("md_22", "Doctrine",
                "The doctrine of 'locus standi' refers to:",
                Question.opts("Right to approach court", "Right to property",
                        "Right to privacy", "Right to equality"), 0,
                "Standing to sue — relaxed in PILs since S.P. Gupta v. Union of India."));
        q.add(new Question("md_23", "Doctrine",
                "The doctrine of 'ultra vires' applies when:",
                Question.opts("Authority acts beyond its powers", "Judiciary overrides legislature",
                        "Parliament amends Constitution", "Citizens violate law"), 0,
                "Acts beyond statutory or constitutional authority are invalid."));
        q.add(new Question("md_24", "Doctrine",
                "The doctrine of 'legitimate expectation' applies when:",
                Question.opts("Public authority fails to fulfill promised benefit",
                        "Judiciary overrides legislature", "Parliament amends Constitution",
                        "Citizens violate law"), 0,
                "Procedural protection where past conduct created reasonable expectation."));
        q.add(new Question("md_25", "Doctrine",
                "The principle of 'public trust doctrine' was laid down in:",
                Question.opts("M.C. Mehta v. Kamal Nath", "Rylands v. Fletcher",
                        "Donoghue v. Stevenson", "Carlill v. Carbolic"), 0,
                "M.C. Mehta v. Kamal Nath — natural resources held in trust by the State."));
        q.add(new Question("md_26", "Doctrine",
                "The principle of 'public interest litigation' was recognized in:",
                Question.opts("S.P. Gupta v. Union of India", "Kesavananda Bharati",
                        "Golaknath", "Indira Gandhi v. Raj Narain"), 0,
                "S.P. Gupta (1981) — Justice Bhagwati relaxed locus standi for PILs."));
        q.add(new Question("md_27", "Doctrine",
                "The doctrine of 'manifest arbitrariness' was recognized in:",
                Question.opts("Shayara Bano v. Union of India", "Kesavananda Bharati",
                        "Golaknath", "Maneka Gandhi"), 0,
                "Shayara Bano (2017) — triple talaq struck down as manifestly arbitrary."));
        q.add(new Question("md_28", "Doctrine",
                "The doctrine of 'constitutional morality' was emphasized in:",
                Question.opts("Navtej Singh Johar v. Union of India", "Kesavananda Bharati",
                        "Golaknath", "Maneka Gandhi"), 0,
                "Navtej (2018) — read down Section 377 IPC; constitutional morality > popular morality."));
        q.add(new Question("md_29", "Doctrine",
                "The principle of 'natural justice' includes:",
                Question.opts("Right to fair hearing", "Right to property",
                        "Right to privacy", "Right to equality"), 0,
                "Audi alteram partem + nemo judex in causa sua — twin pillars."));
        q.add(new Question("md_30", "Doctrine",
                "The doctrine of 'separation of powers' was recognized in:",
                Question.opts("Ram Jawaya Kapur v. State of Punjab", "Kesavananda Bharati",
                        "Golaknath", "Maneka Gandhi"), 0,
                "Ram Jawaya Kapur — broad separation, but Indian Constitution doesn't follow rigid US-style separation."));
        return q;
    }

    private static List<Question> mesTortsSet() {
        List<Question> q = new ArrayList<>();
        q.add(new Question("mt_1", "Tort",
                "The principle of 'strict liability' was laid down in:",
                Question.opts("Donoghue v. Stevenson", "Rylands v. Fletcher",
                        "Carlill v. Carbolic Smoke Ball Co.", "Ashby v. White"), 1,
                "Rylands v. Fletcher (1868) — non-natural use of land that escapes."));
        q.add(new Question("mt_2", "Tort",
                "The principle of 'absolute liability' was laid down in:",
                Question.opts("Rylands v. Fletcher", "M.C. Mehta v. Union of India",
                        "Donoghue v. Stevenson", "Carlill v. Carbolic"), 1,
                "M.C. Mehta (Oleum gas leak, 1986) — no exceptions to liability for hazardous activity."));
        q.add(new Question("mt_3", "Tort",
                "Principle: A person who causes harm by negligence is liable. " +
                        "Fact: A drives carelessly and injures B.",
                Question.opts("A is liable", "A is not liable",
                        "B is liable", "None"), 0,
                "Direct causation by negligent driving — A is liable in tort."));
        q.add(new Question("mt_4", "Tort",
                "Principle: A person who voluntarily consents cannot claim damages. " +
                        "Fact: A joins a boxing match and gets injured.",
                Question.opts("Can claim damages", "Cannot claim damages",
                        "Contract is void", "Contract is binding"), 1,
                "Volenti non fit injuria — voluntary participation bars the claim."));
        q.add(new Question("mt_5", "Constitutional",
                "Principle: No person shall be deprived of personal liberty except according to procedure established by law. " +
                        "Fact: A is detained without trial.",
                Question.opts("Valid detention", "Invalid detention",
                        "Enforceable detention", "Binding detention"), 1,
                "Article 21 violation — detention without procedure is illegal."));
        q.add(new Question("mt_6", "Constitutional",
                "Principle: No person shall be deprived of property except by authority of law. " +
                        "Fact: A's property is seized without compensation.",
                Question.opts("Valid seizure", "Invalid seizure",
                        "Enforceable seizure", "Binding seizure"), 1,
                "Article 300A — property protected against arbitrary state action."));
        q.add(new Question("mt_7", "Constitutional",
                "Principle: No person shall be compelled to self-incriminate. " +
                        "Fact: A is forced to confess.",
                Question.opts("Valid confession", "Invalid confession",
                        "Enforceable confession", "Binding confession"), 1,
                "Article 20(3) — coerced confessions are inadmissible."));
        q.add(new Question("mt_8", "Constitutional",
                "Principle: No person shall be punished for the same offence twice. " +
                        "Fact: A is tried again for the same offence.",
                Question.opts("Valid trial", "Invalid trial",
                        "Enforceable trial", "Binding trial"), 1,
                "Article 20(2) — double jeopardy bars second prosecution for same offence."));
        q.add(new Question("mt_9", "Constitutional",
                "Principle: No taxation without authority of law. " +
                        "Fact: A is taxed by executive order alone.",
                Question.opts("Valid taxation", "Invalid taxation",
                        "Enforceable taxation", "Binding taxation"), 1,
                "Article 265 — no tax shall be levied except by authority of law."));
        q.add(new Question("mt_10", "Contract",
                "Principle: A contract without consideration is void. " +
                        "Fact: A promises to give B ₹10,000 without expecting anything in return.",
                Question.opts("Valid contract", "Void contract",
                        "Enforceable contract", "Binding contract"), 1,
                "Section 25 Contract Act — gratuitous promises are void unless covered by exceptions."));
        q.add(new Question("mt_11", "Contract",
                "Which principle applies when a minor enters into a contract?",
                Question.opts("Contract is void", "Contract is valid",
                        "Contract is enforceable", "Contract is binding"), 0,
                "Mohori Bibee v. Dharmodas Ghose — minor's contract is void ab initio."));
        q.add(new Question("mt_12", "Criminal",
                "Mens rea means:",
                Question.opts("Guilty act", "Guilty mind",
                        "Strict liability", "Vicarious liability"), 1,
                "Mens rea — the mental element in crime."));
        q.add(new Question("mt_13", "Criminal",
                "Actus reus refers to:",
                Question.opts("Guilty act", "Guilty mind",
                        "Legal injury", "Negligence"), 0,
                "The physical / external element of the offence."));
        q.add(new Question("mt_14", "Criminal",
                "A person is accused of theft. Which principle applies in legal reasoning?",
                Question.opts("Presumption of innocence", "Strict liability",
                        "Vicarious liability", "Absolute liability"), 0,
                "Until proven guilty, the accused is presumed innocent."));
        q.add(new Question("mt_15", "Defamation",
                "Principle: No person shall defame another. " +
                        "Fact: Publishing false statements harming reputation.",
                Question.opts("Yes — defamation",
                        "No — public criticism",
                        "No — opinion only",
                        "No — satire"), 0,
                "Defamation requires publication of false statement that injures reputation."));
        q.add(new Question("mt_16", "Trespass",
                "Principle: No person shall trespass private property. " +
                        "Fact: Entering a neighbour's garden without consent.",
                Question.opts("Walking on public road", "Trespass — entering neighbour's garden without consent",
                        "Visiting a government office", "Attending a public rally"), 1,
                "Unauthorized physical entry on another's land = trespass."));
        q.add(new Question("mt_17", "Tort",
                "Principle: Negligence requires duty of care. " +
                        "Fact: A doctor fails to attend patient causing harm.",
                Question.opts("Liable for negligence", "Not liable",
                        "Liable for strict liability", "None"), 0,
                "Doctor owes duty to patient; breach causing harm = negligence (Bolam test)."));
        q.add(new Question("mt_18", "Tort",
                "Principle: Vicarious liability arises from master-servant relationship. " +
                        "Fact: A factory owner ignores safety norms and a worker is injured.",
                Question.opts("Liable for negligence", "Not liable",
                        "Liable for strict liability", "None"), 0,
                "Direct negligence by employer; vicarious liability for employee acts in scope of employment."));
        q.add(new Question("mt_19", "Tort",
                "Principle: Duty of care extends to foreseeable plaintiffs. " +
                        "Fact: A shopkeeper leaves wet floor unmarked; B slips and falls.",
                Question.opts("Liable for negligence", "Not liable",
                        "Liable for strict liability", "None"), 0,
                "Occupier's liability — duty to warn of known hazards."));
        q.add(new Question("mt_20", "Procedure",
                "Speedy trial under Article 21 was recognized in:",
                Question.opts("Hussainara Khatoon v. State of Bihar", "Maneka Gandhi",
                        "Puttaswamy", "A.K. Gopalan"), 0,
                "Hussainara Khatoon — undertrial prisoners; speedy trial part of right to life."));
        return q;
    }
}
