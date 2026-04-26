package com.legalstaan.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

        setRowLabel(view, R.id.row_certificates, "Course Certificates");
        setRowLabel(view, R.id.row_downloads,    "Offline Downloads");
        setRowLabel(view, R.id.row_free_material,"Free Material");
        setRowLabel(view, R.id.row_settings,     "Settings");
        setRowLabel(view, R.id.row_how_to,       "How to use the App");
        setRowLabel(view, R.id.row_privacy,      "Privacy Policy");

        // Tapping any row shows "Coming Soon" for now
        int[] rowIds = {
            R.id.row_certificates, R.id.row_downloads, R.id.row_free_material,
            R.id.row_settings, R.id.row_how_to, R.id.row_privacy
        };
        for (int id : rowIds) {
            view.findViewById(id).setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Coming Soon!", Toast.LENGTH_SHORT).show());
        }
    }

    private void setRowLabel(View root, int rowId, String label) {
        View row = root.findViewById(rowId);
        if (row != null) {
            TextView tv = row.findViewById(R.id.tv_row_label);
            if (tv != null) tv.setText(label);
        }
    }
}
