package com.legalstaan.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LiveFragment extends Fragment {

    private RecyclerView recyclerView;
    private SessionAdapter adapter;
    private View emptyState;
    private FirebaseFirestore db;
    private ListenerRegistration listener;
    private final List<LiveSession> sessions = new ArrayList<>();
    private String currentUserEmail = "";
    private String currentUserName  = "Faculty";
    private boolean isFaculty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_live, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserEmail = user.getEmail() != null ? user.getEmail().toLowerCase().trim() : "";
            currentUserName  = user.getDisplayName() != null ? user.getDisplayName() : "Faculty";
        }
        isFaculty = FacultyManager.isFaculty(currentUserEmail);

        recyclerView = view.findViewById(R.id.rv_live_sessions);
        emptyState   = view.findViewById(R.id.layout_empty_live);
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_go_live);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new SessionAdapter(sessions);
        recyclerView.setAdapter(adapter);

        if (isFaculty) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> showGoLiveDialog());
        } else {
            fab.setVisibility(View.GONE);
        }

        listenForSessions();
    }

    private void listenForSessions() {
        listener = db.collection("live_sessions")
                .whereEqualTo("live", true)
                .addSnapshotListener((snapshots, e) -> {
                    if (!isAdded() || snapshots == null) return;
                    sessions.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            LiveSession s = doc.toObject(LiveSession.class);
                            if (s != null) {
                                s.setSessionId(doc.getId());
                                sessions.add(s);
                            }
                        } catch (Exception ex) {
                            // Malformed document — skip
                        }
                    }
                    adapter.notifyDataSetChanged();
                    boolean empty = sessions.isEmpty();
                    emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
                });
    }

    private void showGoLiveDialog() {
        for (LiveSession s : sessions) {
            if (currentUserEmail.equals(s.getFacultyEmail() != null
                    ? s.getFacultyEmail().toLowerCase() : "")) {
                Toast.makeText(requireContext(),
                        "You already have an active session. End it before starting a new one.",
                        Toast.LENGTH_LONG).show();
                return;
            }
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_go_live, null);

        EditText  etTitle      = dialogView.findViewById(R.id.et_session_title);
        RadioGroup rgPlatform  = dialogView.findViewById(R.id.rg_platform);
        EditText  etYoutubeUrl = dialogView.findViewById(R.id.et_youtube_url);
        EditText  etMeetUrl    = dialogView.findViewById(R.id.et_meet_url);
        View      layoutYt     = dialogView.findViewById(R.id.layout_youtube_url);
        View      layoutMeet   = dialogView.findViewById(R.id.layout_meet_url);

        rgPlatform.setOnCheckedChangeListener((group, checkedId) -> {
            layoutYt.setVisibility(checkedId == R.id.rb_youtube ? View.VISIBLE : View.GONE);
            layoutMeet.setVisibility(checkedId == R.id.rb_google_meet ? View.VISIBLE : View.GONE);
        });

        AlertDialog alert = new AlertDialog.Builder(requireContext())
                .setTitle("Start Live Session")
                .setView(dialogView)
                .setPositiveButton("Go Live", null)
                .setNegativeButton("Cancel", null)
                .create();
        alert.show();

        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                etTitle.setError("Session title required");
                return;
            }

            int checked = rgPlatform.getCheckedRadioButtonId();

            if (checked == R.id.rb_youtube) {
                String ytUrl = etYoutubeUrl.getText().toString().trim();
                if (TextUtils.isEmpty(ytUrl)) {
                    etYoutubeUrl.setError("YouTube URL required");
                    return;
                }
                alert.dismiss();
                startYoutubeSession(title, ytUrl);

            } else if (checked == R.id.rb_google_meet) {
                String meetUrl = etMeetUrl.getText().toString().trim();
                if (TextUtils.isEmpty(meetUrl)) {
                    etMeetUrl.setError("Google Meet link required");
                    return;
                }
                alert.dismiss();
                startGoogleMeetSession(title, meetUrl);

            } else {
                // Jitsi (default)
                alert.dismiss();
                startJitsiSession(title);
            }
        });
    }

    private void startJitsiSession(String title) {
        String roomId = "LegalStaan-" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Map<String, Object> data = buildSessionData(currentUserEmail, currentUserName,
                title, "jitsi", roomId, "", "");
        db.collection("live_sessions").add(data)
                .addOnSuccessListener(ref -> {
                    Intent intent = new Intent(requireContext(), LiveStreamActivity.class);
                    intent.putExtra("platform", "jitsi");
                    intent.putExtra("room_id", roomId);
                    intent.putExtra("title", title);
                    intent.putExtra("session_id", ref.getId());
                    intent.putExtra("is_faculty", true);
                    startActivity(intent);
                })
                .addOnFailureListener(ex ->
                        Toast.makeText(requireContext(),
                                "Could not start session: " + ex.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void startYoutubeSession(String title, String youtubeUrl) {
        Map<String, Object> data = buildSessionData(currentUserEmail, currentUserName,
                title, "youtube", "", youtubeUrl, "");
        db.collection("live_sessions").add(data)
                .addOnSuccessListener(ref -> {
                    Intent intent = new Intent(requireContext(), LiveStreamActivity.class);
                    intent.putExtra("platform", "youtube");
                    intent.putExtra("youtube_url", youtubeUrl);
                    intent.putExtra("title", title);
                    intent.putExtra("session_id", ref.getId());
                    intent.putExtra("is_faculty", true);
                    startActivity(intent);
                })
                .addOnFailureListener(ex ->
                        Toast.makeText(requireContext(),
                                "Could not start session: " + ex.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void startGoogleMeetSession(String title, String meetUrl) {
        Map<String, Object> data = buildSessionData(currentUserEmail, currentUserName,
                title, "meet", "", "", meetUrl);
        db.collection("live_sessions").add(data)
                .addOnSuccessListener(ref -> {
                    Intent intent = new Intent(requireContext(), LiveStreamActivity.class);
                    intent.putExtra("platform", "meet");
                    intent.putExtra("meet_url", meetUrl);
                    intent.putExtra("title", title);
                    intent.putExtra("session_id", ref.getId());
                    intent.putExtra("is_faculty", true);
                    startActivity(intent);
                })
                .addOnFailureListener(ex ->
                        Toast.makeText(requireContext(),
                                "Could not start session: " + ex.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private Map<String, Object> buildSessionData(String email, String name, String title,
            String platform, String roomId, String ytUrl, String meetUrl) {
        Map<String, Object> d = new HashMap<>();
        d.put("facultyEmail", email);
        d.put("facultyName",  name);
        d.put("title",        title);
        d.put("platform",     platform);
        d.put("roomId",       roomId);
        d.put("youtubeUrl",   ytUrl);
        d.put("meetUrl",      meetUrl);
        d.put("live",         true);
        d.put("startedAt",    FieldValue.serverTimestamp());
        return d;
    }

    private void endSession(String sessionId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("End Session")
                .setMessage("End this live session? All viewers will be disconnected.")
                .setPositiveButton("End Session", (d, w) ->
                        db.collection("live_sessions").document(sessionId)
                                .update("live", false)
                                .addOnFailureListener(ex ->
                                        Toast.makeText(requireContext(),
                                                "Failed to end session",
                                                Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void joinSession(LiveSession session) {
        Intent intent = new Intent(requireContext(), LiveStreamActivity.class);
        intent.putExtra("platform",    session.getPlatform());
        intent.putExtra("room_id",     session.getRoomId());
        intent.putExtra("youtube_url", session.getYoutubeUrl());
        intent.putExtra("meet_url",    session.getMeetUrl());
        intent.putExtra("title",       session.getTitle());
        intent.putExtra("session_id",  session.getSessionId());
        boolean isOwner = isFaculty && currentUserEmail.equals(
                session.getFacultyEmail() != null
                        ? session.getFacultyEmail().toLowerCase() : "");
        intent.putExtra("is_faculty", isOwner);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) listener.remove();
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    private class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.VH> {
        private final List<LiveSession> list;

        SessionAdapter(List<LiveSession> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_live_session, parent, false);
            return new VH(v);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            LiveSession s = list.get(position);
            h.tvTitle.setText(s.getTitle() != null ? s.getTitle() : "Live Class");
            h.tvFaculty.setText("by " + (s.getFacultyName() != null ? s.getFacultyName() : "Faculty"));

            String platform = s.getPlatform();
            if ("youtube".equals(platform)) h.tvPlatform.setText("YouTube Live");
            else if ("meet".equals(platform)) h.tvPlatform.setText("Google Meet");
            else h.tvPlatform.setText("Jitsi Meeting");

            boolean isOwner = isFaculty && currentUserEmail.equals(
                    s.getFacultyEmail() != null ? s.getFacultyEmail().toLowerCase() : "");
            h.btnEnd.setVisibility(isOwner ? View.VISIBLE : View.GONE);

            h.btnJoin.setOnClickListener(v -> joinSession(s));
            h.btnEnd.setOnClickListener(v -> endSession(s.getSessionId()));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvFaculty, tvPlatform;
            Button   btnJoin, btnEnd;

            VH(@NonNull View v) {
                super(v);
                tvTitle    = v.findViewById(R.id.tv_session_title);
                tvFaculty  = v.findViewById(R.id.tv_session_faculty);
                tvPlatform = v.findViewById(R.id.tv_session_platform);
                btnJoin    = v.findViewById(R.id.btn_join_session);
                btnEnd     = v.findViewById(R.id.btn_end_session);
            }
        }
    }
}
