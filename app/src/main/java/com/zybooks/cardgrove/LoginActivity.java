/**
 * SNHU CS-499 Software Capstone Project
 * Author: Yelena Green
 *
 * Purpose: This activity manages user login functionality. It checks
 * user credentials, identifies temporary passwords, and navigates to
 * the appropriate screen (MainActivity or ResetPasswordActivity).
 */

package com.zybooks.cardgrove;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private DatabaseHelper databaseHelper;

    /**
     * Initializes the activity and sets up views and listeners.
     *
     * @param savedInstanceState State of the activity when it was previously closed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.emailEditText);
        editTextPassword = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        Button createAccountButton = findViewById(R.id.createAccountButton);
        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);

        databaseHelper = new DatabaseHelper(this);

        loginButton.setOnClickListener(v -> login());
        createAccountButton.setOnClickListener(v -> createAccount());
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Handles the login process and navigates to the appropriate screen.
     */
    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (validateInput(email, password)) {
            if (databaseHelper.checkUser(email, password)) {
                boolean isTemporary = databaseHelper.isTemporaryPassword(email);
                if (isTemporary) {
                    Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Validates the user's email and password inputs.
     *
     * @param email    The email address entered by the user.
     * @param password The password entered by the user.
     * @return True if the inputs are valid, false otherwise.
     */
    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Creates a new user account if the email does not already exist.
     */
    private void createAccount() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (validateInput(email, password)) {
            if (databaseHelper.checkEmailExists(email)) {
                Toast.makeText(this, "Account already exists.", Toast.LENGTH_SHORT).show();
            } else {
                boolean isInserted = databaseHelper.addUser(email, password);
                if (isInserted) {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error creating account.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



}
