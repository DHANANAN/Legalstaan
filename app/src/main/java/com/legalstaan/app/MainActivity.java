package com.legalstaan.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    private static final String BANNER_URL = "https://i.ibb.co/fz0BRgQG/GQ4-Ul-NMW.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        ImageView ivBanner = findViewById(R.id.iv_banner);
        Glide.with(this)
                .load(BANNER_URL)
                .placeholder(R.color.primaryColor)
                .error(R.color.primaryColor)
                .centerCrop()
                .into(ivBanner);

        findViewById(R.id.card_videos).setOnClickListener(v ->
                startActivity(new Intent(this, VideoActivity.class)));

        findViewById(R.id.card_quiz).setOnClickListener(v ->
                startActivity(new Intent(this, QuizActivity.class)));
    }
}
