package com.zybooks.cardgrove;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.content.SharedPreferences;

public class SMSPermissionFragment extends Fragment {

    private static final int REQUEST_SMS_PERMISSION = 123;
    private static final String PREFS_NAME = "CardGrovePrefs";
    private static final String SMS_ENABLED_KEY = "sms_enabled";

    public SMSPermissionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sms_permission, container, false);

        Button buttonRequestPermission = view.findViewById(R.id.buttonRequestPermission);
        Button buttonRevokePermission = view.findViewById(R.id.buttonRevokePermission);

        buttonRequestPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSMSPermission();
            }
        });

        buttonRevokePermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revokeSMSPermission();
            }
        });

        return view;
    }

    private void requestSMSPermission() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean smsEnabled = prefs.getBoolean(SMS_ENABLED_KEY, true);

        if (!smsEnabled) {
            // Re-enable SMS functionality within the app
            enableSMS();
            Toast.makeText(getContext(), "SMS notifications re-enabled.", Toast.LENGTH_SHORT).show();
        } else if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request system SMS permission if not already granted
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS_PERMISSION);
        } else {
            Toast.makeText(getContext(), "SMS permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void revokeSMSPermission() {
        disableSMS();
        Toast.makeText(getContext(), "SMS notifications disabled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableSMS();
                Toast.makeText(getContext(), "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                disableSMS();
                Toast.makeText(getContext(), "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enableSMS() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SMS_ENABLED_KEY, true);
        editor.apply();
    }

    private void disableSMS() {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(SMS_ENABLED_KEY, false);
        editor.apply();
    }

    public void sendSMS(String phoneNumber, String message) {
        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean smsEnabled = prefs.getBoolean(SMS_ENABLED_KEY, true); // Default to true

        if (smsEnabled && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(getContext(), "Low Inventory SMS sent", Toast.LENGTH_SHORT).show();
            Log.d("SMS", " Sent alert as SMS: " + message);
        } else {
            Toast.makeText(getContext(), "SMS permission not granted or SMS is disabled", Toast.LENGTH_SHORT).show();
            Log.d("SMS", "Was trying to send SMS but no permissions granted: " + message);
        }
    }
}
