package com.legalstaan.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ProfileFragment extends Fragment {

    private static final String PREFS = "legalstaan_prefs";
    private static final String KEY_PIC = "profile_pic_path";

    private ImageView ivAvatar;
    private ActivityResultLauncher<String> imagePicker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) saveAndLoadProfilePic(uri);
                });
    }

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

        ivAvatar = view.findViewById(R.id.iv_avatar);

        // Load saved profile pic
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS, 0);
        String savedPath = prefs.getString(KEY_PIC, null);
        if (savedPath != null) {
            File f = new File(savedPath);
            if (f.exists()) {
                Glide.with(this).load(f).circleCrop().into(ivAvatar);
            }
        }

        // Tap avatar to change photo
        ivAvatar.setOnClickListener(v -> imagePicker.launch("image/*"));
        view.findViewById(R.id.tv_change_photo).setOnClickListener(v -> imagePicker.launch("image/*"));

        // User info
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView nameTv = view.findViewById(R.id.tv_profile_name);
            String name = user.getDisplayName();
            nameTv.setText((name != null && !name.isEmpty()) ? name : "Legalstaan Student");

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

        // Row labels
        setRowLabel(view, R.id.row_certificates, "Course Certificates");
        setRowLabel(view, R.id.row_downloads,    "Offline Downloads");
        setRowLabel(view, R.id.row_free_material,"Free Material");
        setRowLabel(view, R.id.row_settings,     "Settings");
        setRowLabel(view, R.id.row_how_to,       "How to Use the App");
        setRowLabel(view, R.id.row_privacy,      "Privacy Policy");
        setRowLabel(view, R.id.row_sign_out,     "Sign Out");

        // Row click handlers
        view.findViewById(R.id.row_certificates).setOnClickListener(v ->
                showInfo("Course Certificates", "Certificates are awarded on completing a full course. Stay consistent with your lectures to earn yours!"));

        view.findViewById(R.id.row_downloads).setOnClickListener(v ->
                showInfo("Offline Downloads", "Offline download support is coming in the next update. Make sure you have a good internet connection for now."));

        view.findViewById(R.id.row_free_material).setOnClickListener(v ->
                openUrl("https://legalstaan.com/"));

        view.findViewById(R.id.row_settings).setOnClickListener(v -> showSettings(view));

        view.findViewById(R.id.row_how_to).setOnClickListener(v -> showHowToUse());

        view.findViewById(R.id.row_privacy).setOnClickListener(v ->
                openUrl("https://legalstaan.com/"));

        view.findViewById(R.id.row_sign_out).setOnClickListener(v -> signOut());
    }

    private void showSettings(View root) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Settings")
                .setMessage("• Dark Mode: use the toggle in your profile\n\n• Notifications: coming soon\n\n• App Version: 1.2\n\n• Contact: contactlegalstaan@gmail.com")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showHowToUse() {
        new AlertDialog.Builder(requireContext())
                .setTitle("How to Use Legalstaan")
                .setMessage(
                        "1. Home — Quick access to all features and social links.\n\n" +
                        "2. Courses — Browse 10 subjects, 105 video lectures. Tap any subject to see its lectures.\n\n" +
                        "3. Community — Chat with Rutu AI for app guidance and legal topic help.\n\n" +
                        "4. Profile — Update your picture, toggle dark mode, and access settings.\n\n" +
                        "5. Quiz — Tap 'Test Series' on the home screen to take practice MCQs.\n\n" +
                        "Tip: Use dark mode for comfortable night-time studying!")
                .setPositiveButton("Got it!", null)
                .show();
    }

    private void showInfo(String title, String msg) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void openUrl(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    private void saveAndLoadProfilePic(Uri uri) {
        try {
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            File outFile = new File(requireContext().getFilesDir(), "profile_pic.jpg");
            OutputStream out = new FileOutputStream(outFile);
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            in.close();
            out.close();

            requireContext().getSharedPreferences(PREFS, 0)
                    .edit().putString(KEY_PIC, outFile.getAbsolutePath()).apply();

            Glide.with(this).load(outFile).circleCrop().into(ivAvatar);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Could not set photo", Toast.LENGTH_SHORT).show();
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(requireActivity(), gso);
        client.signOut().addOnCompleteListener(task -> {
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
