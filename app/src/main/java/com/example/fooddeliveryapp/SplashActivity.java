package com.example.fooddeliveryapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    ImageView imgLogo;
    TextView txtAppName, txtTagLine;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgLogo = findViewById(R.id.imgLogo);
        txtAppName = findViewById(R.id.txtAppName);
        txtTagLine = findViewById(R.id.txtTagLine);
        progressBar = findViewById(R.id.progressBar);

        // Animations
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_zoom);
        Animation textAnim = AnimationUtils.loadAnimation(this, R.anim.text_slide);
        Animation fadeAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        imgLogo.startAnimation(logoAnim);

        txtAppName.startAnimation(textAnim);
        txtTagLine.startAnimation(textAnim);

        progressBar.startAnimation(fadeAnim);

        new Handler().postDelayed(() -> {

            Intent intent = new Intent(SplashActivity.this, SignInActivity.class);
            startActivity(intent);

            overridePendingTransition(android.R.anim.fade_in,
                    android.R.anim.fade_out);

            finish();

        }, 2500);

    }
}