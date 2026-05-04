package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Generic full-screen info / "coming soon" / "how to use" page.
 *
 * Replaces the cramped AlertDialogs that some profile rows used to throw up.
 * Pass {@link #EXTRA_TITLE} and {@link #EXTRA_BODY} (Markdown-ish plain text
 * with \n\n paragraph breaks) and the activity renders them with proper
 * spacing, scrolling, and a back arrow.
 */
public class InfoActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "info_title";
    public static final String EXTRA_BODY  = "info_body";

    public static Intent newIntent(android.content.Context ctx, String title, String body) {
        return new Intent(ctx, InfoActivity.class)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_BODY, body);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.apply(this);
        setContentView(R.layout.activity_info);

        Toolbar toolbar = findViewById(R.id.toolbar_info);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String title = getIntent().getStringExtra(EXTRA_TITLE);
            getSupportActionBar().setTitle(title != null ? title : "Information");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView body = findViewById(R.id.tv_info_body);
        String content = getIntent().getStringExtra(EXTRA_BODY);
        body.setText(content != null ? content : "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
