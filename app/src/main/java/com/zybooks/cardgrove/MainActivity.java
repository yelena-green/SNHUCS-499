/**
 * SNHU CS-499 Software Capstone Project
 * Author: Yelena Green
 * Purpose: This activity serves as the main entry point for the app after login.
 * It manages navigation between different fragments (e.g., Card Inventory, Add Card, Settings).
 * It also handles logout functionality and permissions for SMS notifications.
 */

package com.zybooks.cardgrove;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 100;

    /**
     * Initializes the MainActivity and sets up toolbar, bottom navigation, and default fragment.
     *
     * @param savedInstanceState State of the activity when it was previously closed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation(bottomNavigationView);

        if (savedInstanceState == null) {
            loadDefaultFragment();
        }

        checkSmsPermission();
    }

    /**
     * Sets up the bottom navigation menu to handle fragment switching.
     *
     * @param bottomNavigationView The bottom navigation view in the layout.
     */
    private void setupBottomNavigation(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_card_inventory) {
                // Load Card Inventory Fragment
                loadFragment(new CardInventoryFragment());
                return true;
            } else if (id == R.id.navigation_add_card) {
                // Load Add Card Fragment
                loadFragment(new AddCardFragment());
                return true;
            } else if (id == R.id.navigation_settings) {
                // Load SMS Permission Fragment
                loadFragment(new SMSPermissionFragment());
                return true;
            } else if (id == R.id.navigation_logout) {
                // Log out and redirect to LoginActivity
                logout();
                return true;
            }

            return false;
        });
    }


    /**
     * Loads the default fragment (Card Inventory) when the activity starts.
     */
    private void loadDefaultFragment() {
        loadFragment(new CardInventoryFragment());
    }

    /**
     * Replaces the current fragment with the given fragment.
     *
     * @param fragment The fragment to display.
     */
    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        Log.d("MainActivity", "Loaded fragment: " + fragment.getClass().getSimpleName());
    }

    /**
     * Logs out the user and redirects them to the LoginActivity.
     */
    private void logout() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        Log.d("MainActivity", "User logged out");
    }

    /**
     * Checks if SMS permissions are granted. Requests permission if not already granted.
     */
    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Handles the result of the SMS permission request.
     *
     * @param requestCode  The request code for SMS permissions.
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Sends an SMS alert for low inventory.
     *
     * @param message The message to send via SMS.
     */
    public void sendSmsAlert(String message) {
        SharedPreferences prefs = getSharedPreferences("CardGrovePrefs", Context.MODE_PRIVATE);
        boolean smsEnabled = prefs.getBoolean("sms_enabled", true); // Default to true

        if (smsEnabled) {
            String phoneNumber = "5121231234"; // Replace with your desired phone number
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, "Low Inventory SMS sent.", Toast.LENGTH_SHORT).show();
                Log.d("SMS", "Sending SMS: " + message);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to send SMS alert.", Toast.LENGTH_SHORT).show();
                Log.e("SMS", "Failed to send SMS: " + message, e); // Log the error with the stack trace
            }
        } else {
            Toast.makeText(this, "SMS notifications are disabled.", Toast.LENGTH_SHORT).show();
            Log.d("SMS", "SMS notifications are disabled. Message: " + message);
        }
    }

}
