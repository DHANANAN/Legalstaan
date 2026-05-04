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
}
