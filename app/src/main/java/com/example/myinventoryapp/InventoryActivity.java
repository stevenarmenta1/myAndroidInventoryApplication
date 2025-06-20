package com.example.myinventoryapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class InventoryActivity extends AppCompatActivity {
    // UI components
    private TableLayout inventoryTable;
    private Button addItemButton, btnOpenNotification;

    // Database helper
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        inventoryTable = findViewById(R.id.inventoryTable);
        addItemButton = findViewById(R.id.addItemButton);
        btnOpenNotification = findViewById(R.id.btnOpenNotification);

        // Load inventory data
        loadInventoryData();

        // Set up add item button click listener
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItemDialog();
            }
        });

        // Set up notification button click listener
        btnOpenNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InventoryActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh inventory data when activity resumes
        loadInventoryData();
    }

    /**
     * Loads inventory data from database and displays it in table
     */
    private void loadInventoryData() {
        // Clear existing table rows except the header
        if (inventoryTable.getChildCount() > 1) {
            inventoryTable.removeViews(1, inventoryTable.getChildCount() - 1);
        }

        // Get all inventory items from database
        Cursor cursor = dbHelper.getAllInventoryItems();

        if (cursor.moveToFirst()) {
            do {
                // Get column indices safely
                int idColumnIndex = cursor.getColumnIndex("id");
                int nameColumnIndex = cursor.getColumnIndex("item_name");
                int quantityColumnIndex = cursor.getColumnIndex("quantity");
                int thresholdColumnIndex = cursor.getColumnIndex("threshold");

                // Check if columns exist before accessing them
                if (idColumnIndex == -1 || nameColumnIndex == -1 ||
                        quantityColumnIndex == -1 || thresholdColumnIndex == -1) {
                    // Handle the case where one or more columns are missing
                    Toast.makeText(this, "Database schema error", Toast.LENGTH_SHORT).show();
                    continue;
                }

                final long id = cursor.getLong(idColumnIndex);
                String itemName = cursor.getString(nameColumnIndex);
                final int quantity = cursor.getInt(quantityColumnIndex);
                int threshold = cursor.getInt(thresholdColumnIndex);

                // Create a new table row
                TableRow row = new TableRow(this);

                // Create and add item name text view
                TextView nameTextView = new TextView(this);
                nameTextView.setText(itemName);
                nameTextView.setPadding(10, 10, 10, 10);
                row.addView(nameTextView);

                // Create and add quantity text view
                final TextView quantityTextView = new TextView(this);
                quantityTextView.setText(String.valueOf(quantity));
                quantityTextView.setPadding(10, 10, 10, 10);
                row.addView(quantityTextView);

                // Create and add update button
                Button updateButton = new Button(this);
                updateButton.setText("Edit");
                updateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateItemDialog(id, itemName, quantity, threshold);
                    }
                });
                row.addView(updateButton);

                // Create and add delete button
                Button deleteButton = new Button(this);
                deleteButton.setText("Delete");
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Delete item from database
                        dbHelper.deleteInventoryItem(id);
                        // Refresh inventory data
                        loadInventoryData();
                        Toast.makeText(InventoryActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                    }
                });
                row.addView(deleteButton);

                // Add row to table
                inventoryTable.addView(row);

            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    /**
     * Shows dialog for adding a new inventory item
     */
    private void showAddItemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Item");

        // Set up the input fields
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        final EditText itemNameInput = view.findViewById(R.id.itemNameInput);
        final EditText quantityInput = view.findViewById(R.id.quantityInput);
        final EditText thresholdInput = view.findViewById(R.id.thresholdInput);

        builder.setView(view);

        // Set up the buttons
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemName = itemNameInput.getText().toString().trim();
                String quantityStr = quantityInput.getText().toString().trim();
                String thresholdStr = thresholdInput.getText().toString().trim();

                // Validate input
                if (itemName.isEmpty() || quantityStr.isEmpty()) {
                    Toast.makeText(InventoryActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int quantity = Integer.parseInt(quantityStr);
                int threshold = thresholdStr.isEmpty() ? 5 : Integer.parseInt(thresholdStr);

                // Add item to database
                long id = dbHelper.addInventoryItem(itemName, quantity, threshold);

                if (id != -1) {
                    // Item added successfully
                    Toast.makeText(InventoryActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                    // Refresh inventory data
                    loadInventoryData();
                } else {
                    // Item addition failed
                    Toast.makeText(InventoryActivity.this, "Failed to add item", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Shows dialog for updating an existing inventory item
     */
    private void showUpdateItemDialog(final long id, String currentName, int currentQuantity, int currentThreshold) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Item");

        // Set up the input fields
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null);
        final EditText itemNameInput = view.findViewById(R.id.itemNameInput);
        final EditText quantityInput = view.findViewById(R.id.quantityInput);
        final EditText thresholdInput = view.findViewById(R.id.thresholdInput);

        // Pre-fill fields with current values
        itemNameInput.setText(currentName);
        quantityInput.setText(String.valueOf(currentQuantity));
        thresholdInput.setText(String.valueOf(currentThreshold));

        builder.setView(view);

        // Set up the buttons
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemName = itemNameInput.getText().toString().trim();
                String quantityStr = quantityInput.getText().toString().trim();
                String thresholdStr = thresholdInput.getText().toString().trim();

                // Validate input
                if (itemName.isEmpty() || quantityStr.isEmpty() || thresholdStr.isEmpty()) {
                    Toast.makeText(InventoryActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int quantity = Integer.parseInt(quantityStr);
                int threshold = Integer.parseInt(thresholdStr);

                // Update item in database
                int result = dbHelper.updateInventoryItem(id, itemName, quantity, threshold);

                if (result > 0) {
                    // Item updated successfully
                    Toast.makeText(InventoryActivity.this, "Item updated", Toast.LENGTH_SHORT).show();
                    // Refresh inventory data
                    loadInventoryData();
                } else {
                    // Item update failed
                    Toast.makeText(InventoryActivity.this, "Failed to update item", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}