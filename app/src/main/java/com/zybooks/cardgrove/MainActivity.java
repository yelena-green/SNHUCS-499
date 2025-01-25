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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up bottom navigation item selection using the non-deprecated method
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_card_inventory) {
                // Load Card Inventory Fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CardInventoryFragment())
                        .commit();
                return true;
            } else if (id == R.id.navigation_add_card) {
                // Load Add Card Fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AddCardFragment())
                        .commit();
                return true;

            } else if (id == R.id.navigation_settings) {
                // Load SMS Permission Fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SMSPermissionFragment())
                        .commit();
                return true;
            } else if (id == R.id.navigation_logout) {
                // Log out and redirect to LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        // Load default fragment (Card Inventory) on start
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CardInventoryFragment()).commit();
        }

        // Check for SMS permission on app start
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call to super

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "SMS permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "SMS permission denied. SMS notifications won't be sent.", Toast.LENGTH_SHORT).show();
                // App continues to function without SMS notifications
            }
        }
    }

    public void sendSmsAlert(String message) {
        SharedPreferences prefs = getSharedPreferences("CardGrovePrefs", Context.MODE_PRIVATE);
        boolean smsEnabled = prefs.getBoolean("sms_enabled", true); // Default to true

        if (smsEnabled) {
            String phoneNumber = "5121231234"; // My app phone number to send sms messages
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
