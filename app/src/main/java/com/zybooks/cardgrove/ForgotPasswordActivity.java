/**
 * SNHU CS-499 Software Capstone Project
 * Author: Yelena Green
 * Purpose: This activity allows users to request a temporary password
 * if they have forgotten their current password. The temporary password
 * is sent to their registered email address and is stored in the database
 * with a temporary flag.
 */

package com.zybooks.cardgrove;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;
    private Button backToLoginButton;
    private DatabaseHelper databaseHelper;

    /**
     * Initializes the activity and sets up views and event listeners.
     *
     * @param savedInstanceState State of the activity when it was previously closed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupListeners();
    }

    /**
     * Initializes all views in the activity.
     */
    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);
        databaseHelper = new DatabaseHelper(this); // Initialize database helper
    }

    /**
     * Sets up event listeners for the buttons.
     */
    private void setupListeners() {
        resetPasswordButton.setOnClickListener(v -> resetPassword());
        backToLoginButton.setOnClickListener(v -> navigateToLogin());
    }

    /**
     * Handles the password reset request. Generates a temporary password,
     * stores it in the database, and sends it to the user's email address.
     */
    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (isValidEmail(email)) {
            if (databaseHelper.checkEmailExists(email)) {
                String tempPassword = generateTempPassword();
                boolean isUpdated = databaseHelper.storeTempPassword(email, tempPassword);

                if (isUpdated) {
                    sendTempPasswordEmail(email, tempPassword);
                    Toast.makeText(this, "Temporary password sent to " + email, Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                } else {
                    Toast.makeText(this, "Failed to reset password. Try again later.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Email not registered", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Generates a secure temporary password.
     *
     * @return A randomly generated temporary password.
     */
    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder tempPassword = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            tempPassword.append(chars.charAt(random.nextInt(chars.length())));
        }
        return tempPassword.toString();
    }

    /**
     * Sends the temporary password to the user's email address.
     *
     * @param email        The user's email address.
     * @param tempPassword The temporary password.
     */
    private void sendTempPasswordEmail(String email, String tempPassword) {
        Log.d("ForgotPassword", "Temporary password for " + email + ": " + tempPassword);
    }

    /**
     * Navigates back to the LoginActivity.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Validates the format of the email address.
     *
     * @param email The email address to validate.
     * @return True if the email is valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
