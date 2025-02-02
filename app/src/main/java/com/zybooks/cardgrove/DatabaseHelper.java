/**
 * SNHU CS-499 Software Capstone Project
 * Author: Yelena Green
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

/**
 * SQLite database helper for managing users and card inventory.
 */
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
     */
    public boolean addUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_IS_TEMPORARY, 0);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Verifies if a user exists with the given email and password.
     */
    public boolean checkUser(String email, String password) {
        return checkIfExists(TABLE_USERS, COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?", new String[]{email, password});
    }

    /**
     * Checks if an email already exists in the Users table.
     */
    public boolean checkEmailExists(String email) {
        return checkIfExists(TABLE_USERS, COLUMN_EMAIL + " = ?", new String[]{email});
    }

    /**
     * Stores a temporary password for a user.
     */
    public boolean storeTempPassword(String email, String tempPassword) {
        return updatePassword(email, tempPassword, 1);
    }

    /**
     * Updates a user's password to permanent.
     */
    public boolean updatePassword(String email, String newPassword) {
        return updatePassword(email, newPassword, 0);
    }

    /**
     * Updates a user's password with a temporary or permanent status.
     */
    private boolean updatePassword(String email, String newPassword, int isTemporary) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);
        values.put(COLUMN_IS_TEMPORARY, isTemporary);
        int rowsUpdated = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});
        db.close();
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

        boolean isTemporary = false;

        if (cursor != null && cursor.moveToFirst()) {
            isTemporary = cursor.getInt(0) == 1;
            cursor.close();
        }

        db.close();
        return isTemporary;
    }


    // ============================= Inventory Table Operations =============================

    /**
     * Adds a new inventory item to the Inventory table.
     */
    public boolean addItem(String type, String name, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_TYPE, type);
        values.put(COLUMN_ITEM_NAME, name);
        values.put(COLUMN_ITEM_QUANTITY, qty);
        long result = db.insert(TABLE_INVENTORY, null, values);
        db.close();
        return result != -1;
    }

    /**
     * Updates an existing inventory item.
     */
    public boolean updateItem(int itemId, String type, String name, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_TYPE, type);
        values.put(COLUMN_ITEM_NAME, name);
        values.put(COLUMN_ITEM_QUANTITY, qty);
        int result = db.update(TABLE_INVENTORY, values, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
        db.close();
        return result > 0;
    }

    /**
     * Deletes an inventory item.
     */
    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_INVENTORY, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
        db.close();
        return result > 0;
    }

    /**
     * Retrieves all inventory items.
     */
    public Cursor getAllItems() {
        return executeQuery("SELECT * FROM " + TABLE_INVENTORY);
    }

    /**
     * Retrieves sorted inventory items from the database.
     *
     * @param criteria The sorting criteria ("name", "type", "qty").
     * @return Cursor containing the sorted inventory records.
     */
    public Cursor getSortedCards(String criteria) {
        SQLiteDatabase db = this.getReadableDatabase();
        String orderBy;

        // Ensure criteria maps to actual database column names
        switch (criteria.toLowerCase()) {
            case "name":
                orderBy = COLUMN_ITEM_NAME + " ASC";
                break;
            case "type":
                orderBy = COLUMN_ITEM_TYPE + " ASC";  // Sorting by type
                break;
            case "qty":
                orderBy = COLUMN_ITEM_QUANTITY + " DESC";  // Highest quantity first
                break;
            default:
                Log.e("DatabaseHelper", "Invalid sorting criteria: " + criteria);
                orderBy = COLUMN_ITEM_NAME + " ASC";  // Default sort
        }

        String query = "SELECT * FROM " + TABLE_INVENTORY + " ORDER BY " + orderBy;
        Log.d("DatabaseHelper", "Executing SQL: " + query);  // Debugging log

        return db.rawQuery(query, null);
    }


    /**
     * Retrieves inventory items filtered by name.
     */
    public Cursor getFilteredCardsByName(String nameQuery) {
        return executeQuery("SELECT * FROM " + TABLE_INVENTORY + " WHERE " + COLUMN_ITEM_NAME + " LIKE ? ORDER BY " + COLUMN_ITEM_NAME + " ASC", new String[]{"%" + nameQuery + "%"});
    }

    // ============================= Helper Methods =============================

    private Cursor executeQuery(String query, String... args) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, args);
    }

    private boolean checkIfExists(String table, String selection, String[] args) {
        Cursor cursor = executeQuery("SELECT 1 FROM " + table + " WHERE " + selection + " LIMIT 1", args);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

}
