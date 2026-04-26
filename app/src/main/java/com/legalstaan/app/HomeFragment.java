package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    private static final String BANNER_URL = "https://i.ibb.co/fz0BRgQG/GQ4-Ul-NMW.jpg";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load banner image
        ImageView ivBanner = view.findViewById(R.id.iv_banner);
        Glide.with(this)
                .load(BANNER_URL)
                .placeholder(R.color.primaryColor)
                .error(R.color.primaryColor)
                .centerCrop()
                .into(ivBanner);

        // Recorded Lectures → switch to Courses tab
        view.findViewById(R.id.card_recorded).setOnClickListener(v -> switchToCoursesTab());

        // Live Schedule → coming soon
        view.findViewById(R.id.card_schedule).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Coming Soon!", Toast.LENGTH_SHORT).show());

        // Test Series → QuizActivity
        view.findViewById(R.id.card_test).setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), QuizActivity.class)));

        // Free Material → coming soon
        view.findViewById(R.id.card_free).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Coming Soon!", Toast.LENGTH_SHORT).show());
    }

    private void switchToCoursesTab() {
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_courses);
        }
    }
}
