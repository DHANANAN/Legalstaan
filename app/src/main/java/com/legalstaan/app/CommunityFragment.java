package com.legalstaan.app;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityFragment extends Fragment {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CONTACT = 1;

    private final List<Object> rows = new ArrayList<>();
    private ContactsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Faculty with their subject expertise
    private static final String[][] FACULTY = {
            {"Abhishek Sir", "abhisheknls56789@gmail.com", "IPR & Trademark Law", "#EF5350"},
            {"Aryaa Anuj Sir", "singhpunni592@gmail.com", "Constitutional Law", "#5C6BC0"},
            {"Arya Verma Sir", "aryaverma7355@gmail.com", "Patent Law", "#42A5F5"},
            {"Rohit Sir", "rs11336singh@gmail.com", "Criminal Law", "#FF7043"},
            {"Nikhil Sir", "nikhilanand2367@gmail.com", "Administrative Law", "#EC407A"},
            {"Alfaz Mushriff Sir", "alfajsmmushrif@gmail.com", "Contract Law", "#AB47BC"},
            {"Susen Kamble Sir", "susenk20@gmail.com", "Torts", "#26C6DA"},
            {"Ajay Jatav Sir", "iamajayjatav@gmail.com", "CPC & Procedure", "#FFA726"},
            {"Gautam Sir", "gautam2367@gmail.com", "International Law", "#66BB6A"},
            {"Eshan Sir", "eshan.sharma333@gmail.com", "Copyright Law", "#26A69A"},
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        EditText etSearch = view.findViewById(R.id.et_search_email);
        ImageButton btnAdd = view.findViewById(R.id.btn_add_contact);
        RecyclerView rv = view.findViewById(R.id.rv_community);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ContactsAdapter(rows, this::onItemClick);
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String email = etSearch.getText().toString().trim();
            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(requireContext(), "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            addContactByEmail(email);
            etSearch.setText("");
        });

        buildList();
    }

    private void buildList() {
        rows.clear();

        // Rutu AI pinned at top
        rows.add("Rutu AI");
        rows.add(new Contact("Rutu AI", "rutu@legalstaan.app", "Legal AI Assistant — tap to chat", "#4CAF50", true));

        // Faculty section
        rows.add("Faculty");
        for (String[] f : FACULTY) {
            rows.add(new Contact(f[0], f[1], f[2], f[3], false));
        }

        // My Contacts section — load from Firestore
        rows.add("My Contacts");
        loadMyContacts();

        adapter.notifyDataSetChanged();
    }

    private void loadMyContacts() {
        if (currentUser == null) return;
        db.collection("users").document(currentUser.getUid())
                .collection("contacts")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;
                    int insertPos = rows.size();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        if (name == null) name = email;
                        rows.add(new Contact(name, email, email, "#78909C", false));
                    }
                    if (rows.size() > insertPos) {
                        adapter.notifyItemRangeInserted(insertPos, rows.size() - insertPos);
                    }
                });
    }

    private void addContactByEmail(String email) {
        if (currentUser == null) return;
        // Check if this is self
        if (email.equalsIgnoreCase(currentUser.getEmail())) {
            Toast.makeText(requireContext(), "That's your own email!", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> contact = new HashMap<>();
        contact.put("email", email);
        contact.put("name", email.split("@")[0]); // default name from email
        contact.put("addedAt", System.currentTimeMillis());

        db.collection("users").document(currentUser.getUid())
                .collection("contacts")
                .document(email)
                .set(contact)
                .addOnSuccessListener(v -> {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Contact added!", Toast.LENGTH_SHORT).show();
                    // Add to local list
                    rows.add(new Contact(email.split("@")[0], email, email, "#78909C", false));
                    adapter.notifyItemInserted(rows.size() - 1);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to add contact", Toast.LENGTH_SHORT).show());
    }

    private void onItemClick(Object item) {
        if (!(item instanceof Contact)) return;
        Contact c = (Contact) item;

        if (c.isBot) {
            startActivity(new Intent(requireActivity(), RutuChatActivity.class));
            return;
        }

        // Faculty contacts → email via Gmail (the messaging story for this release).
        // Other custom contacts → existing in-app direct chat.
        if (FacultyManager.isFaculty(c.email)) {
            sendEmailToFaculty(c);
        } else {
            Intent intent = new Intent(requireActivity(), DirectChatActivity.class);
            intent.putExtra("contact_name", c.name);
            intent.putExtra("contact_email", c.email);
            startActivity(intent);
        }
    }

    /** Open Gmail (or any mail app) pre-filled to the chosen faculty member. */
    private void sendEmailToFaculty(Contact c) {
        String studentName = "a Legalstaan student";
        if (currentUser != null && currentUser.getDisplayName() != null
                && !currentUser.getDisplayName().isEmpty()) {
            studentName = currentUser.getDisplayName();
        }
        String subject = "Doubt for " + c.name + " — Legalstaan";
        String body    = "Hello " + c.name + ",\n\n"
                + "I'm " + studentName + " from the Legalstaan app and I had a question about "
                + (c.subtitle != null ? c.subtitle : "your subject") + ".\n\n"
                + "[Type your question here]\n\n"
                + "Thank you,\n" + studentName;

        Intent mail = new Intent(Intent.ACTION_SENDTO);
        mail.setData(Uri.parse("mailto:"));
        mail.putExtra(Intent.EXTRA_EMAIL, new String[]{c.email});
        mail.putExtra(Intent.EXTRA_SUBJECT, subject);
        mail.putExtra(Intent.EXTRA_TEXT, body);

        try {
            // Prefer Gmail directly if installed — drops the user straight into compose.
            Intent gmail = new Intent(mail);
            gmail.setPackage("com.google.android.gm");
            startActivity(gmail);
        } catch (Exception ignored) {
            try {
                startActivity(Intent.createChooser(mail, "Email " + c.name));
            } catch (Exception e) {
                Toast.makeText(requireContext(),
                        "No email app installed on this device", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ── Models ──

    static class Contact {
        final String name, email, subtitle, color;
        final boolean isBot;
        Contact(String name, String email, String subtitle, String color, boolean isBot) {
            this.name = name;
            this.email = email;
            this.subtitle = subtitle;
            this.color = color;
            this.isBot = isBot;
        }
    }

    // ── Adapter ──

    interface OnItemClickListener { void onClick(Object item); }

    static class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<Object> items;
        private final OnItemClickListener listener;

        ContactsAdapter(List<Object> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof String ? TYPE_HEADER : TYPE_CONTACT;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_HEADER) {
                View v = inf.inflate(R.layout.item_section_header, parent, false);
                return new HeaderVH(v);
            }
            View v = inf.inflate(R.layout.item_contact, parent, false);
            return new ContactVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object item = items.get(position);
            if (item instanceof String) {
                ((HeaderVH) holder).tvHeader.setText((String) item);
            } else {
                Contact c = (Contact) item;
                ContactVH h = (ContactVH) holder;
                h.tvName.setText(c.name);
                h.tvSubtitle.setText(c.subtitle);

                // Avatar with initial and color
                String initial = c.name.substring(0, 1).toUpperCase();
                h.tvAvatar.setText(initial);
                GradientDrawable bg = new GradientDrawable();
                bg.setShape(GradientDrawable.OVAL);
                try { bg.setColor(Color.parseColor(c.color)); }
                catch (Exception e) { bg.setColor(Color.GRAY); }
                h.tvAvatar.setBackground(bg);

                // Show the mail-glyph hint on faculty rows so users know what tapping does.
                if (h.ivActionHint != null) {
                    boolean showMail = !c.isBot && FacultyManager.isFaculty(c.email);
                    h.ivActionHint.setVisibility(showMail ? View.VISIBLE : View.GONE);
                }

                h.itemView.setOnClickListener(v -> listener.onClick(c));
            }
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class HeaderVH extends RecyclerView.ViewHolder {
            final TextView tvHeader;
            HeaderVH(View v) { super(v); tvHeader = v.findViewById(R.id.tv_section_header); }
        }

        static class ContactVH extends RecyclerView.ViewHolder {
            final TextView tvAvatar, tvName, tvSubtitle;
            final ImageView ivActionHint;
            ContactVH(View v) {
                super(v);
                tvAvatar = v.findViewById(R.id.tv_avatar);
                tvName = v.findViewById(R.id.tv_contact_name);
                tvSubtitle = v.findViewById(R.id.tv_contact_subtitle);
                ivActionHint = v.findViewById(R.id.iv_action_hint);
            }
        }
    }
}
