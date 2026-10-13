package com.example.fooddeliveryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private ImageView foodImage;
    private TextView title;
    private TextView description;
    private LinearLayout bottomLayout;
    private Button signInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        foodImage = findViewById(R.id.foodImage);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        bottomLayout = findViewById(R.id.bottomLayout);
        signInBtn = findViewById(R.id.signInBtn);

        if (foodImage == null || title == null || description == null || bottomLayout == null || signInBtn == null) {
            Log.e(TAG, "One or more login screen views are missing");
            return;
        }

        try {
            Animation fadeScale = AnimationUtils.loadAnimation(this, R.anim.fade_scale);
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

            foodImage.startAnimation(fadeScale);
            title.startAnimation(fadeScale);
            description.startAnimation(fadeScale);
            bottomLayout.startAnimation(slideUp);
        } catch (Exception e) {
            Log.e(TAG, "Error applying login animations", e);
        }

        signInBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
            startActivity(intent);
            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        });
    }
}