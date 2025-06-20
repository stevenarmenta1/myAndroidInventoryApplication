package com.example.myinventoryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    // UI components
    private EditText usernameEditText, passwordEditText;
    private Button loginButton, createAccountButton;

    // Database helper
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        createAccountButton = findViewById(R.id.createAccountButton);

        // Set up login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set up create account button click listener
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    /**
     * Attempts to login the user with the provided credentials
     */
    private void loginUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check credentials against database
        if (dbHelper.checkUser(username, password)) {
            // Login successful, proceed to inventory screen
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
            startActivity(intent);
            finish(); // Close login activity
        } else {
            // Login failed
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Creates a new user account with the provided credentials
     */
    private void createAccount() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user to database
        long userId = dbHelper.addUser(username, password);

        if (userId != -1) {
            // Account creation successful
            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
            // Proceed to inventory screen
            Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
            startActivity(intent);
            finish(); // Close login activity
        } else {
            // Account creation failed
            Toast.makeText(this, "Failed to create account. Username may already exist.", Toast.LENGTH_SHORT).show();
        }
    }
}