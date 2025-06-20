package com.example.myinventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "inventory_manager";

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_INVENTORY = "inventory";

    // Common column names
    private static final String KEY_ID = "id";

    // USERS Table - column names
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    // INVENTORY Table - column names
    private static final String KEY_ITEM_NAME = "item_name";
    private static final String KEY_QUANTITY = "quantity";
    private static final String KEY_THRESHOLD = "threshold";

    // Table Create Statements
    // Users table create statement
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USERNAME + " TEXT UNIQUE NOT NULL,"
            + KEY_PASSWORD + " TEXT NOT NULL" + ")";

    // Inventory table create statement
    private static final String CREATE_TABLE_INVENTORY = "CREATE TABLE " + TABLE_INVENTORY + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_ITEM_NAME + " TEXT UNIQUE NOT NULL,"
            + KEY_QUANTITY + " INTEGER NOT NULL,"
            + KEY_THRESHOLD + " INTEGER NOT NULL DEFAULT 5" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_INVENTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);

        // Create new tables
        onCreate(db);
    }

    // ========== USER METHODS ==========

    // Check if user exists
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = KEY_USERNAME + " = ? AND " + KEY_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();

        cursor.close();
        return count > 0;
    }

    // Add new user
    public long addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username);
        values.put(KEY_PASSWORD, password);

        // Insert row
        long id = db.insert(TABLE_USERS, null, values);

        return id;
    }

    // ========== INVENTORY METHODS ==========

    // Add new inventory item
    public long addInventoryItem(String itemName, int quantity, int threshold) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ITEM_NAME, itemName);
        values.put(KEY_QUANTITY, quantity);
        values.put(KEY_THRESHOLD, threshold);

        // Insert row
        long id = db.insert(TABLE_INVENTORY, null, values);

        return id;
    }

    // Update inventory item quantity
    public int updateInventoryItem(long id, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_QUANTITY, quantity);

        // Updating row
        return db.update(TABLE_INVENTORY, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Update inventory item completely
    public int updateInventoryItem(long id, String itemName, int quantity, int threshold) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ITEM_NAME, itemName);
        values.put(KEY_QUANTITY, quantity);
        values.put(KEY_THRESHOLD, threshold);

        // Updating row
        return db.update(TABLE_INVENTORY, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Delete inventory item
    public void deleteInventoryItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INVENTORY, KEY_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // Get all inventory items
    public Cursor getAllInventoryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_INVENTORY;

        return db.rawQuery(selectQuery, null);
    }

    // Get inventory items below threshold for notifications
    public Cursor getLowInventoryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_INVENTORY + " WHERE " + KEY_QUANTITY + " <= " + KEY_THRESHOLD;

        return db.rawQuery(selectQuery, null);
    }
}