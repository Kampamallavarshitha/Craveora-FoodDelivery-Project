package com.example.fooddeliveryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    // ================================================================
    // VIEWS
    // ================================================================

    private ImageView imgFood;
    private EditText name;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;

    private Button signUpBtn;
    private Button signInBtn;

    // ================================================================
    // FIREBASE
    // ================================================================

    private FirebaseAuth mAuth;

    // ================================================================
    // ON CREATE
    // ================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        // ============================================================
        // INITIALIZE VIEWS
        // ============================================================

        imgFood = findViewById(R.id.imgFood);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);

        signUpBtn = findViewById(R.id.signUpBtn);
        signInBtn = findViewById(R.id.signInBtn);

        // ============================================================
        // FIREBASE
        // ============================================================

        mAuth = FirebaseAuth.getInstance();

        // ============================================================
        // INPUT TYPES
        // ============================================================

        email.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        password.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        confirmPassword.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        // ============================================================
        // ANIMATION
        // ============================================================

        startAnimations();

        // ============================================================
        // SIGN UP BUTTON
        // ============================================================

        signUpBtn.setOnClickListener(v -> {

            createAccount();
        });

        // ============================================================
        // SIGN IN BUTTON
        // ============================================================

        signInBtn.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            SignUpActivity.this,
                            SignInActivity.class
                    );

            startActivity(intent);

            finish();
        });
    }

    // ================================================================
    // CREATE ACCOUNT
    // ================================================================

    private void createAccount() {

        String nameText =
                name.getText()
                        .toString()
                        .trim();

        String emailText =
                email.getText()
                        .toString()
                        .trim();

        String passwordText =
                password.getText()
                        .toString()
                        .trim();

        String confirmPasswordText =
                confirmPassword.getText()
                        .toString()
                        .trim();

        // ============================================================
        // NAME VALIDATION
        // ============================================================

        if (nameText.isEmpty()) {

            name.setError(
                    "Please enter your name"
            );

            name.requestFocus();

            return;
        }

        if (nameText.length() < 2) {

            name.setError(
                    "Please enter a valid name"
            );

            name.requestFocus();

            return;
        }

        // ============================================================
        // EMAIL VALIDATION
        // ============================================================

        if (emailText.isEmpty()) {

            email.setError(
                    "Please enter your email"
            );

            email.requestFocus();

            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(emailText)
                .matches()) {

            email.setError(
                    "Please enter a valid email address"
            );

            email.requestFocus();

            return;
        }

        // ============================================================
        // PASSWORD VALIDATION
        // ============================================================

        if (passwordText.isEmpty()) {

            password.setError(
                    "Please enter a password"
            );

            password.requestFocus();

            return;
        }

        if (passwordText.length() < 6) {

            password.setError(
                    "Password must contain at least 6 characters"
            );

            password.requestFocus();

            return;
        }

        // ============================================================
        // CONFIRM PASSWORD
        // ============================================================

        if (confirmPasswordText.isEmpty()) {

            confirmPassword.setError(
                    "Please confirm your password"
            );

            confirmPassword.requestFocus();

            return;
        }

        if (!passwordText.equals(confirmPasswordText)) {

            confirmPassword.setError(
                    "Passwords do not match"
            );

            confirmPassword.requestFocus();

            return;
        }

        // ============================================================
        // DISABLE BUTTON
        // ============================================================

        signUpBtn.setEnabled(false);

        signUpBtn.setText("Creating Account...");

        // ============================================================
        // FIREBASE CREATE ACCOUNT
        // ============================================================

        mAuth.createUserWithEmailAndPassword(
                        emailText,
                        passwordText
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            // ====================================================
                            // ENABLE BUTTON
                            // ====================================================

                            signUpBtn.setEnabled(true);

                            signUpBtn.setText("Create Account");

                            // ====================================================
                            // SUCCESS
                            // ====================================================

                            if (task.isSuccessful()) {

                                Log.d(
                                        TAG,
                                        "Firebase account created successfully"
                                );

                                FirebaseUser user =
                                        mAuth.getCurrentUser();

                                // ==================================================
                                // UPDATE DISPLAY NAME
                                // ==================================================

                                if (user != null) {

                                    com.google.firebase.auth.UserProfileChangeRequest profile =
                                            new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                    .setDisplayName(nameText)
                                                    .build();

                                    user.updateProfile(profile)
                                            .addOnCompleteListener(
                                                    profileTask -> {

                                                        Log.d(
                                                                TAG,
                                                                "User profile updated"
                                                        );
                                                    }
                                            );
                                }

                                Toast.makeText(
                                        SignUpActivity.this,
                                        "Account created successfully!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                // ==================================================
                                // OPEN HOME
                                // ==================================================

                                openMainActivity();

                                return;
                            }

                            // ====================================================
                            // ACCOUNT CREATION FAILED
                            // ====================================================

                            Exception exception =
                                    task.getException();

                            Log.e(
                                    TAG,
                                    "Firebase account creation failed",
                                    exception
                            );

                            String errorMessage =
                                    "Unable to create account";

                            if (exception != null
                                    && exception.getMessage() != null) {

                                errorMessage =
                                        exception.getMessage();
                            }

                            // ====================================================
                            // EMAIL ALREADY EXISTS
                            // ====================================================

                            if (errorMessage
                                    .toLowerCase()
                                    .contains("already in use")) {

                                Toast.makeText(
                                        SignUpActivity.this,
                                        "This email is already registered. Please Sign In.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // ====================================================
                            // INVALID EMAIL
                            // ====================================================

                            if (errorMessage
                                    .toLowerCase()
                                    .contains("badly formatted")) {

                                Toast.makeText(
                                        SignUpActivity.this,
                                        "Please enter a valid email address.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // ====================================================
                            // WEAK PASSWORD
                            // ====================================================

                            if (errorMessage
                                    .toLowerCase()
                                    .contains("password")) {

                                Toast.makeText(
                                        SignUpActivity.this,
                                        "Password must contain at least 6 characters.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // ====================================================
                            // OTHER ERROR
                            // ====================================================

                            Toast.makeText(
                                    SignUpActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ================================================================
    // ANIMATIONS
    // ================================================================

    private void startAnimations() {

        try {

            Animation imageAnim =
                    AnimationUtils.loadAnimation(
                            this,
                            R.anim.slide_down
                    );

            Animation cardAnim =
                    AnimationUtils.loadAnimation(
                            this,
                            R.anim.slide_up
                    );

            imgFood.startAnimation(imageAnim);

            findViewById(R.id.signupCard)
                    .startAnimation(cardAnim);

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Animation error",
                    e
            );
        }
    }

    // ================================================================
    // OPEN MAIN ACTIVITY
    // ================================================================

    private void openMainActivity() {

        Intent intent =
                new Intent(
                        SignUpActivity.this,
                        MainActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    // ================================================================
    // CHECK EXISTING SESSION
    // ================================================================

    @Override
    protected void onStart() {

        super.onStart();

        /*
         * If the user is already authenticated,
         * don't show Sign Up again.
         */

        if (mAuth != null
                && mAuth.getCurrentUser() != null) {

            Log.d(
                    TAG,
                    "User already authenticated. Opening Home."
            );

            openMainActivity();
        }
    }
}