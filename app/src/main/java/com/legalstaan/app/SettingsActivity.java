package com.legalstaan.app;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.apply(this);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Dark mode
        SwitchCompat darkSwitch = findViewById(R.id.sw_dark_mode);
        darkSwitch.setChecked(ThemeHelper.isDark(this));
        darkSwitch.setOnCheckedChangeListener((b, checked) -> {
            ThemeHelper.setDark(this, checked);
            recreate();
        });

        // Version
        TextView tvVersion = findViewById(R.id.tv_version_value);
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText("v" + pi.versionName);
        } catch (Exception e) {
            tvVersion.setText("v1.23.0");
        }

        findViewById(R.id.row_contact).setOnClickListener(v ->
                openMail("contactlegalstaan@gmail.com",
                        "Legalstaan App Support",
                        "Hi Legalstaan team,\n\n"));

        findViewById(R.id.row_privacy).setOnClickListener(v ->
                openUrl("https://legalstaan.com/privacy"));

        findViewById(R.id.row_terms).setOnClickListener(v ->
                openUrl("https://legalstaan.com/terms"));

        findViewById(R.id.row_share).setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, "Legalstaan App");
            share.putExtra(Intent.EXTRA_TEXT,
                    "Learn law on the go with Legalstaan — 100+ lectures, mock tests, "
                            + "and live classes.\nhttps://legalstaan.com/");
            startActivity(Intent.createChooser(share, "Share Legalstaan"));
        });

        findViewById(R.id.row_rate).setOnClickListener(v ->
                openUrl("https://play.google.com/store/apps/details?id=" + getPackageName()));

        findViewById(R.id.row_clear_cache).setOnClickListener(v -> {
            try {
                deleteDir(getCacheDir());
                Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Could not clear cache", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.row_about).setOnClickListener(v -> openUrl("https://legalstaan.com/"));
    }

    private void openMail(String to, String subject, String body) {
        // Prefer the Gmail app explicitly so the user lands directly in compose.
        Intent gmail = new Intent(Intent.ACTION_SENDTO);
        gmail.setData(Uri.parse("mailto:"));
        gmail.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        gmail.putExtra(Intent.EXTRA_SUBJECT, subject);
        gmail.putExtra(Intent.EXTRA_TEXT, body);
        try {
            startActivity(Intent.createChooser(gmail, "Send email"));
        } catch (Exception e) {
            Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void openUrl(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception ignored) {}
    }

    private static boolean deleteDir(java.io.File dir) {
        if (dir == null) return false;
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String c : children) deleteDir(new java.io.File(dir, c));
            }
        }
        return dir.delete();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
