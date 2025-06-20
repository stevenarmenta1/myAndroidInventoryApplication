package com.example.myinventoryapp;

import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {

    private Button sendTestButton, saveButton;
    private EditText phoneNumberEditText;
    private Switch enableNotificationsSwitch;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        dbHelper = new DatabaseHelper(this);

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        enableNotificationsSwitch = findViewById(R.id.enableNotificationsSwitch);
        sendTestButton = findViewById(R.id.sendTestButton);
        saveButton = findViewById(R.id.saveButton);

        // Enable all controls (removed the disabled state)
        phoneNumberEditText.setEnabled(true);
        enableNotificationsSwitch.setEnabled(true);
        sendTestButton.setEnabled(true);

        sendTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTestNotification();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NotificationActivity.this, "Notification settings saved", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void sendTestNotification() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null,
                    "This is a test notification from MyInventoryApp", null, null);
            Toast.makeText(this, "Test notification sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send test notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void checkAndSendLowInventoryNotifications(String phoneNumber) {
        if (phoneNumber.isEmpty()) return;

        Cursor cursor = dbHelper.getLowInventoryItems();

        if (cursor.moveToFirst()) {
            StringBuilder messageBuilder = new StringBuilder("Low inventory alert: ");

            do {
                int itemNameColumnIndex = cursor.getColumnIndex("item_name");
                int quantityColumnIndex = cursor.getColumnIndex("quantity");

                if (itemNameColumnIndex >= 0 && quantityColumnIndex >= 0) {
                    String itemName = cursor.getString(itemNameColumnIndex);
                    int quantity = cursor.getInt(quantityColumnIndex);

                    messageBuilder.append(itemName).append(" (").append(quantity).append("), ");
                }
            } while (cursor.moveToNext());

            if (messageBuilder.length() > "Low inventory alert: ".length()) {
                String message = messageBuilder.substring(0, messageBuilder.length() - 2);

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();
    }
}