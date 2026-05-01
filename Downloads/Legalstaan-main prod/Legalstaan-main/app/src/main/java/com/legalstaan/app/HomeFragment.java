package com.legalstaan.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load real banner image via Glide (falls back to vector placeholder)
        Glide.with(this)
                .load("https://i.ibb.co/fz0BRgQG/GQ4-Ul-NMW.jpg")
                .placeholder(R.drawable.banner)
                .error(R.drawable.banner)
                .centerCrop()
                .into((ImageView) view.findViewById(R.id.iv_banner));

        // Personalised welcome
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView tvWelcome = view.findViewById(R.id.tv_welcome);
            String name = user.getDisplayName();
            if (name != null && !name.isEmpty()) {
                tvWelcome.setText("Hello, " + name.split(" ")[0] + "!");
            }
        }

        // Navigation cards
        view.findViewById(R.id.card_recorded).setOnClickListener(
                v -> switchToTab(R.id.nav_courses));
        view.findViewById(R.id.card_test).setOnClickListener(
                v -> startActivity(new Intent(requireActivity(), QuizActivity.class)));

        // Social links
        view.findViewById(R.id.iv_youtube).setOnClickListener(
                v -> openUrl("https://youtube.com/@legalstaanofficial"));
        view.findViewById(R.id.iv_whatsapp).setOnClickListener(
                v -> openUrl("https://whatsapp.com/channel/0029Vb7bj4F65yDKgltFsl2V"));
        view.findViewById(R.id.iv_instagram).setOnClickListener(
                v -> openUrl("https://www.instagram.com/legalstaan"));
        view.findViewById(R.id.iv_website).setOnClickListener(
                v -> openUrl("https://legalstaan.com/"));
    }

    private void switchToTab(int navId) {
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) nav.setSelectedItemId(navId);
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
