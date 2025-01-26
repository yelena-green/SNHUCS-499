/**
 * SNHU CS-499 Software Capstone Project
 * Author: Yelena Green
 * Purpose: This activity allows users to reset their password if they logged in
 * using a temporary password. It updates the password in the database and marks 
 * it as permanent.
 */

package com.zybooks.cardgrove;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private Button resetPasswordButton;
    private DatabaseHelper databaseHelper;

    /**
     * Initializes the activity and sets up views and event listeners.
     *
     * @param savedInstanceState State of the activity when it was previously closed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize views
        passwordEditText = findViewById(R.id.passwordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Check if the button is properly initialized
        if (resetPasswordButton != null) {
            resetPasswordButton.setOnClickListener(v -> {
                String email = getIntent().getStringExtra("email");
                String newPassword = passwordEditText.getText().toString().trim();
                resetPassword(email, newPassword);
            });
        } else {
            Log.e("ResetPasswordActivity", "resetPasswordButton is null");
        }
    }

    /**
     * Resets the user's password and updates it in the database.
     *
     * @param email       The user's email address.
     * @param newPassword The new password to set.
     */
    private void resetPassword(String email, String newPassword) {
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isUpdated = databaseHelper.updatePassword(email, newPassword);
        if (isUpdated) {
            Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
            Log.d("ResetPasswordActivity", "Password updated for: " + email);
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to reset password", Toast.LENGTH_SHORT).show();
            Log.e("ResetPasswordActivity", "Failed to update password for: " + email);
        }
    }
}
