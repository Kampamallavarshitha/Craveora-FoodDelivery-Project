package com.example.fooddeliveryapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    // ================================================================
    // VIEWS
    // ================================================================

    private EditText email;
    private EditText password;
    private Button continueBtn;
    private Button signUpBtn;
    private ImageView imgFood;


    private FirebaseAuth mAuth;

  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

        // ============================================================
        // INITIALIZE VIEWS
        // ============================================================

        imgFood = findViewById(R.id.imgFood);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        continueBtn = findViewById(R.id.continueBtn);
        signUpBtn = findViewById(R.id.signUpBtn);

        // ============================================================
        // INITIALIZE FIREBASE AUTH
        // ============================================================

        mAuth = FirebaseAuth.getInstance();

        // ============================================================
        // EMAIL INPUT
        // ============================================================

        email.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );

        // ============================================================
        // PASSWORD INPUT
        // ============================================================

        password.setInputType(
                InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        // ============================================================
        // ANIMATIONS
        // ============================================================

        startAnimations();

        // ============================================================
        // SIGN IN BUTTON
        // ============================================================

        continueBtn.setOnClickListener(v -> {

            String emailText = email
                    .getText()
                    .toString()
                    .trim();

            String passwordText = password
                    .getText()
                    .toString()
                    .trim();

            // ========================================================
            // VALIDATE EMAIL
            // ========================================================

            if (emailText.isEmpty()) {

                email.setError(
                        "Please enter your email"
                );

                email.requestFocus();

                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(emailText)
                    .matches()) {

                email.setError(
                        "Please enter a valid email address"
                );

                email.requestFocus();

                return;
            }

            // ========================================================
            // VALIDATE PASSWORD
            // ========================================================

            if (passwordText.isEmpty()) {

                password.setError(
                        "Please enter your password"
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

            // ========================================================
            // DISABLE BUTTON
            // ========================================================

            continueBtn.setEnabled(false);

            continueBtn.setText("Signing In...");

            // ========================================================
            // FIREBASE EMAIL SIGN IN
            // ========================================================

            signInUser(
                    emailText,
                    passwordText
            );
        });

        // ============================================================
        // SIGN UP BUTTON
        // ============================================================

        signUpBtn.setOnClickListener(v -> {

            Intent intent = new Intent(
                    SignInActivity.this,
                    SignUpActivity.class
            );

            startActivity(intent);
        });
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

            findViewById(R.id.loginCard)
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
    // SIGN IN USER
    // ================================================================

    private void signInUser(
            String emailText,
            String passwordText
    ) {

        Log.d(
                TAG,
                "Attempting Firebase email login for: "
                        + emailText
        );

        mAuth.signInWithEmailAndPassword(
                        emailText,
                        passwordText
                )
                .addOnCompleteListener(
                        this,
                        task -> {

                            // ====================================================
                            // ENABLE BUTTON
                            // ====================================================

                            continueBtn.setEnabled(true);

                            continueBtn.setText("Continue");

                            // ====================================================
                            // LOGIN SUCCESS
                            // ====================================================

                            if (task.isSuccessful()) {

                                Log.d(
                                        TAG,
                                        "Firebase email login successful"
                                );

                                Toast.makeText(
                                        SignInActivity.this,
                                        "Login Successful",
                                        Toast.LENGTH_SHORT
                                ).show();

                                openMainActivity();

                                return;
                            }

                            // ====================================================
                            // LOGIN FAILED
                            // ====================================================

                            Exception exception =
                                    task.getException();

                            Log.e(
                                    TAG,
                                    "Firebase email login failed",
                                    exception
                            );

                            // ====================================================
                            // INVALID USER
                            // ====================================================

                            if (exception
                                    instanceof FirebaseAuthInvalidUserException) {

                                Toast.makeText(
                                        SignInActivity.this,
                                        "Account not found. Please Sign Up first.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // ====================================================
                            // INVALID PASSWORD / CREDENTIAL
                            // ====================================================

                            if (exception
                                    instanceof FirebaseAuthInvalidCredentialsException) {

                                Toast.makeText(
                                        SignInActivity.this,
                                        "Incorrect email or password.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            // ====================================================
                            // OTHER FIREBASE ERROR
                            // ====================================================

                            String errorMessage =
                                    "Unable to sign in";

                            if (exception != null
                                    && exception.getMessage() != null) {

                                errorMessage =
                                        exception.getMessage();
                            }

                            Toast.makeText(
                                    SignInActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    // ================================================================
    // OPEN MAIN ACTIVITY
    // ================================================================

    private void openMainActivity() {

        Intent intent =
                new Intent(
                        SignInActivity.this,
                        MainActivity.class
                );

        // ============================================================
        // CLEAR LOGIN SCREEN FROM BACK STACK
        // ============================================================

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }

    // ================================================================
    // CHECK FIREBASE SESSION
    // ================================================================

    @Override
    protected void onStart() {

        super.onStart();

        /*
         * Firebase automatically remembers a successfully
         * authenticated user.
         *
         * Therefore:
         *
         * User signs in
         *       ↓
         * Firebase keeps session
         *       ↓
         * App is closed
         *       ↓
         * App opened again
         *       ↓
         * FirebaseAuth.getCurrentUser() != null
         *       ↓
         * MainActivity
         */

        if (mAuth != null
                && mAuth.getCurrentUser() != null) {

            Log.d(
                    TAG,
                    "User already signed in. Opening Home."
            );

            openMainActivity();
        }
    }
}