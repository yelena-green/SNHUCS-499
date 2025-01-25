package com.zybooks.cardgrove;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.usernameEditText);
        editTextPassword = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        databaseHelper = new DatabaseHelper(this);

        loginButton.setOnClickListener(v -> login());

        createAccountButton.setOnClickListener(v -> createAccount());
    }

    private void login() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (validateInput(username, password)) {
            if (databaseHelper.checkUser(username, password)) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else if (databaseHelper.checkUsernameExists(username)) {
                Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Username not found. Press Create an account or try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createAccount() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (validateInput(username, password)) {
            if (databaseHelper.checkUsernameExists(username)) {
                Toast.makeText(this, "Username already exists. Please choose a different one.", Toast.LENGTH_SHORT).show();
            } else {
                boolean isInserted = databaseHelper.addUser(username, password);
                if (isInserted) {
                    Toast.makeText(this, "Account created successfully! Press Login", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean validateInput(String username, String password) {
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
