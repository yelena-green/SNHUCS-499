/**
 * SNHU CS-499 Software Capstone Project
 * Author: Yelena Green
 *
 * Purpose: This class provides helper methods for managing the SQLite database
 * for the Card Grove app. It includes functionality for managing user accounts
 * (e.g., adding users, resetting passwords) and handling card inventory.
 */

package com.zybooks.cardgrove;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database constants
    private static final String DATABASE_NAME = "CardGrove.db";
    private static final int DATABASE_VERSION = 2;

    // Users table constants
    private static final String TABLE_USERS = "Users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_IS_TEMPORARY = "is_temporary";

    // Inventory table constants
    private static final String TABLE_INVENTORY = "Inventory";
    private static final String COLUMN_ITEM_ID = "item_id";
    private static final String COLUMN_ITEM_TYPE = "type";
    private static final String COLUMN_ITEM_NAME = "name";
    private static final String COLUMN_ITEM_QUANTITY = "qty";

    /**
     * Constructor: Initializes the SQLiteOpenHelper with the app's context.
     *
     * @param context Application context for database access.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * Creates the Users and Inventory tables.
     *
     * @param db The database instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_IS_TEMPORARY + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_INVENTORY_TABLE = "CREATE TABLE " + TABLE_INVENTORY + "("
                + COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ITEM_TYPE + " TEXT,"
                + COLUMN_ITEM_NAME + " TEXT,"
                + COLUMN_ITEM_QUANTITY + " INTEGER" + ")";
        db.execSQL(CREATE_INVENTORY_TABLE);

        // Insert default admin user
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, "admin");
        values.put(COLUMN_PASSWORD, "admin");
        db.insert(TABLE_USERS, null, values);
    }

    /**
     * Called when the database needs to be upgraded.
     * Drops the existing tables and recreates them.
     *
     * @param db         The database instance.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        onCreate(db);
    }

    // ============================= User Table Operations =============================

    /**
     * Adds a new user to the Users table.
     *
     * @param email    The user's email.
     * @param password The user's password.
     * @return True if the user was successfully added, false otherwise.
     */
    public boolean addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_IS_TEMPORARY, 0); // Default to permanent password
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        Log.d("DatabaseHelper", "addUser: " + (result != -1 ? "Success" : "Failure") + " for " + email);
        return result != -1;
    }

    /**
     * Verifies if a user exists with the given email and password.
     *
     * @param email    The user's email.
     * @param password The user's password.
     * @return True if the user exists, false otherwise.
     */
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        Log.d("DatabaseHelper", "checkUser: " + (exists ? "Valid" : "Invalid") + " credentials for " + email);
        return exists;
    }

    /**
     * Checks if an email already exists in the Users table.
     *
     * @param email The email to check.
     * @return True if the email exists, false otherwise.
     */
    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        Log.d("DatabaseHelper", "checkEmailExists: " + (exists ? "Found" : "Not Found") + " for " + email);
        return exists;
    }

    /**
     * Stores a temporary password for a user and marks the password as temporary.
     *
     * @param email       The user's email.
     * @param tempPassword The generated temporary password.
     * @return True if the password was successfully updated, false otherwise.
     */
    public boolean storeTempPassword(String email, String tempPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, tempPassword);
        values.put(COLUMN_IS_TEMPORARY, 1);
        int rowsUpdated = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        Log.d("DatabaseHelper", "storeTempPassword: " + rowsUpdated + " rows updated for " + email);
        return rowsUpdated > 0;
    }

    /**
     * Checks if the user's password is marked as temporary.
     *
     * @param email The user's email.
     * @return True if the password is temporary, false otherwise.
     */
    public boolean isTemporaryPassword(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_IS_TEMPORARY},
                COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int isTemporary = cursor.getInt(0);
            cursor.close();
            db.close();
            Log.d("DatabaseHelper", "isTemporaryPassword: " + isTemporary + " for " + email);
            return isTemporary == 1;
        }
        if (cursor != null) cursor.close();
        db.close();
        return false;
    }

    /**
     * Updates a user's password and marks it as permanent.
     *
     * @param email       The user's email.
     * @param newPassword The new password to set.
     * @return True if the password was successfully updated, false otherwise.
     */
    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);
        values.put(COLUMN_IS_TEMPORARY, 0);
        int rowsUpdated = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
        Log.d("DatabaseHelper", "updatePassword: " + rowsUpdated + " rows updated for " + email);
        return rowsUpdated > 0;
    }

    // ============================= Inventory Table Operations =============================

    /**
     * Adds a new inventory item to the Inventory table.
     *
     * @param type The type of the item.
     * @param name The name of the item.
     * @param qty  The quantity of the item.
     * @return True if the item was successfully added, false otherwise.
     */
    public boolean addItem(String type, String name, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_TYPE, type);
        values.put(COLUMN_ITEM_NAME, name);
        values.put(COLUMN_ITEM_QUANTITY, qty);
        long result = db.insert(TABLE_INVENTORY, null, values);
        db.close();
        Log.d("DatabaseHelper", "addItem: " + (result != -1 ? "Success" : "Failure") + " for item: " + name);
        return result != -1;
    }

    /**
     * Updates an existing inventory item in the Inventory table.
     *
     * @param itemId The ID of the item to update.
     * @param type   The new type of the item.
     * @param name   The new name of the item.
     * @param qty    The new quantity of the item.
     * @return True if the item was successfully updated, false otherwise.
     */
    public boolean updateItem(int itemId, String type, String name, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_TYPE, type);
        values.put(COLUMN_ITEM_NAME, name);
        values.put(COLUMN_ITEM_QUANTITY, qty);
        int result = db.update(TABLE_INVENTORY, values, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
        db.close();
        Log.d("DatabaseHelper", "updateItem: " + (result > 0 ? "Success" : "Failure") + " for item ID: " + itemId);
        return result > 0;
    }

    /**
     * Deletes an inventory item from the Inventory table.
     *
     * @param itemId The ID of the item to delete.
     * @return True if the item was successfully deleted, false otherwise.
     */
    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_INVENTORY, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
        db.close();
        Log.d("DatabaseHelper", "deleteItem: " + (result > 0 ? "Success" : "Failure") + " for item ID: " + itemId);
        return result > 0;
    }

    /**
     * Retrieves all inventory items from the Inventory table.
     *
     * @return A Cursor containing all inventory items.
     */
    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INVENTORY, null);
        Log.d("DatabaseHelper", "getAllItems: Retrieved " + (cursor != null ? cursor.getCount() : 0) + " items");
        return cursor;
    }
}
