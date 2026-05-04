package com.legalstaan.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.List;

public class HomeFragment extends Fragment {

    private ListenerRegistration liveListener;
    private View cardLive;
    private TextView tvLiveTitle, tvLiveFaculty;
    private LiveSession currentSession;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Banner is now a local FrameLayout with vector background — no network load needed

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView tvWelcome = view.findViewById(R.id.tv_welcome);
            String name = user.getDisplayName();
            if (name != null && !name.isEmpty()) {
                tvWelcome.setText("Hello, " + name.split(" ")[0] + "!");
            }
        }

        // Live session card
        cardLive = view.findViewById(R.id.card_live);
        tvLiveTitle = view.findViewById(R.id.tv_live_title);
        tvLiveFaculty = view.findViewById(R.id.tv_live_faculty);
        view.findViewById(R.id.btn_watch_live).setOnClickListener(v -> joinCurrentSession());
        cardLive.setOnClickListener(v -> joinCurrentSession());

        // 4 navigation cards
        view.findViewById(R.id.card_recorded).setOnClickListener(
                v -> switchToTab(R.id.nav_courses));
        view.findViewById(R.id.card_test).setOnClickListener(
                v -> startActivity(new Intent(requireActivity(), QuizActivity.class)));
        view.findViewById(R.id.card_live_classes).setOnClickListener(
                v -> switchToTab(R.id.nav_live));
        view.findViewById(R.id.card_free_material).setOnClickListener(
                v -> openFreeMaterials());

        // v1.22.0 mega-merge cards
        view.findViewById(R.id.card_qbank).setOnClickListener(
                v -> startActivity(new Intent(requireActivity(), QuestionBankActivity.class)));
        view.findViewById(R.id.card_leaderboard).setOnClickListener(
                v -> startActivity(new Intent(requireActivity(), LeaderboardActivity.class)));
        view.findViewById(R.id.btn_search).setOnClickListener(
                v -> startActivity(new Intent(requireActivity(), SearchActivity.class)));

        StreakManager sm = StreakManager.get(requireContext());
        TextView tvXp = view.findViewById(R.id.tv_xp_chip);
        if (tvXp != null) tvXp.setText(sm.getXp() + " XP • " + sm.getStreak() + " day streak");

        // Social links
        view.findViewById(R.id.iv_youtube).setOnClickListener(
                v -> openUrl("https://youtube.com/@legalstaanofficial"));
        view.findViewById(R.id.iv_whatsapp).setOnClickListener(
                v -> openUrl("https://whatsapp.com/channel/0029Vb7bj4F65yDKgltFsl2V"));
        view.findViewById(R.id.iv_instagram).setOnClickListener(
                v -> openUrl("https://www.instagram.com/legalstaan"));
        view.findViewById(R.id.iv_website).setOnClickListener(
                v -> openUrl("https://legalstaan.com/"));

        listenForLiveSessions();
    }

    private void openFreeMaterials() {
        Intent intent = new Intent(requireActivity(), SubjectVideosActivity.class);
        intent.putExtra(SubjectVideosActivity.EXTRA_SUBJECT_ID, "__study_materials__");
        intent.putExtra(SubjectVideosActivity.EXTRA_SUBJECT_TITLE, "Free Study Materials");
        intent.putExtra(SubjectVideosActivity.EXTRA_IS_STUDY_MATERIAL, true);
        startActivity(intent);
    }

    private void listenForLiveSessions() {
        liveListener = FirebaseFirestore.getInstance()
                .collection("live_sessions")
                .whereEqualTo("live", true)
                .addSnapshotListener((snapshots, e) -> {
                    if (!isAdded() || snapshots == null) return;
                    List<DocumentSnapshot> docs = snapshots.getDocuments();
                    if (!docs.isEmpty()) {
                        try {
                            LiveSession s = docs.get(0).toObject(LiveSession.class);
                            if (s != null) {
                                s.setSessionId(docs.get(0).getId());
                                currentSession = s;
                                tvLiveTitle.setText(s.getTitle() != null ? s.getTitle() : "Live Class");
                                tvLiveFaculty.setText(s.getFacultyName() != null ? s.getFacultyName() : "Faculty");
                                cardLive.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception ex) {
                            cardLive.setVisibility(View.GONE);
                        }
                    } else {
                        currentSession = null;
                        if (cardLive != null) cardLive.setVisibility(View.GONE);
                    }
                });
    }

    private void joinCurrentSession() {
        if (currentSession == null) return;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user != null && user.getEmail() != null
                ? user.getEmail().toLowerCase().trim() : "";
        boolean isOwner = FacultyManager.isFaculty(email) && email.equals(
                currentSession.getFacultyEmail() != null
                        ? currentSession.getFacultyEmail().toLowerCase() : "");

        Intent intent = new Intent(requireContext(), LiveStreamActivity.class);
        intent.putExtra("platform",    currentSession.getPlatform());
        intent.putExtra("room_id",     currentSession.getRoomId());
        intent.putExtra("youtube_url", currentSession.getYoutubeUrl());
        intent.putExtra("meet_url",    currentSession.getMeetUrl());
        intent.putExtra("title",       currentSession.getTitle());
        intent.putExtra("session_id",  currentSession.getSessionId());
        intent.putExtra("is_faculty",  isOwner);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (liveListener != null) liveListener.remove();
    }

    private void switchToTab(int navId) {
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) nav.setSelectedItemId(navId);
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
