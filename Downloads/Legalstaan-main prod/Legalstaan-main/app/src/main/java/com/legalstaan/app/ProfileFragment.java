package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set row labels
        setRowLabel(view, R.id.row_certificates, "Course Certificates");
        setRowLabel(view, R.id.row_downloads,    "Offline Downloads");
        setRowLabel(view, R.id.row_free_material,"Free Material");
        setRowLabel(view, R.id.row_settings,     "Settings");
        setRowLabel(view, R.id.row_how_to,       "How to use the App");
        setRowLabel(view, R.id.row_privacy,      "Privacy Policy");
        setRowLabel(view, R.id.row_sign_out,     "Sign Out");

        // User info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView nameTv = view.findViewById(R.id.tv_profile_name);
            nameTv.setText(user.getDisplayName() != null ? user.getDisplayName() : user.getEmail());

            TextView emailTv = view.findViewById(R.id.tv_profile_email);
            if (emailTv != null) emailTv.setText(user.getEmail());
        }

        // Dark mode toggle
        SwitchCompat darkSwitch = view.findViewById(R.id.switch_dark_mode);
        if (darkSwitch != null) {
            darkSwitch.setChecked(ThemeHelper.isDark(requireContext()));
            darkSwitch.setOnCheckedChangeListener((btn, checked) -> {
                ThemeHelper.setDark(requireContext(), checked);
                requireActivity().recreate();
            });
        }

        // "Coming soon" rows
        int[] comingSoon = {R.id.row_certificates, R.id.row_downloads,
                R.id.row_free_material, R.id.row_settings,
                R.id.row_how_to, R.id.row_privacy};
        for (int id : comingSoon) {
            view.findViewById(id).setOnClickListener(
                    v -> Toast.makeText(requireContext(), "Coming Soon!", Toast.LENGTH_SHORT).show());
        }

        view.findViewById(R.id.row_sign_out).setOnClickListener(v -> signOut());
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(requireActivity(), gso);
        client.signOut().addOnCompleteListener(task -> {
            // Go directly to LoginActivity and clear the back stack
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setRowLabel(View root, int rowId, String label) {
        View row = root.findViewById(rowId);
        if (row != null) {
            TextView tv = row.findViewById(R.id.tv_row_label);
            if (tv != null) tv.setText(label);
        }
    }
}
