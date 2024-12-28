package com.example.conversionsapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "conversionApp.db";
    private static final int DATABASE_VERSION = 11;

    // Users Table
    public static final String TABLE_USERS = "Users";
    public static final String COLUMN_USERNAME = "Username";
    public static final String COLUMN_PASSWORD = "Password";
    public static final String COLUMN_SECURITY_QUESTION = "SecurityQuestion";
    public static final String COLUMN_SECURITY_ANSWER = "SecurityAnswer";

    // ConversionHistory Table
    public static final String TABLE_CONVERSION_HISTORY = "ConversionHistory";
    public static final String COLUMN_FROM_UNIT = "fromUnit";
    public static final String COLUMN_TO_UNIT = "toUnit";
    public static final String COLUMN_FROM_AMOUNT = "fromAmount";
    public static final String COLUMN_TO_AMOUNT = "toAmount";
    public static final String COLUMN_LABEL = "Label";
    public static final String COLUMN_HISTORY_USERNAME = "Username";
    private static final String COLUMN_IS_DELETED = "isDeleted";

    private static final String TABLE_ADMIN_ACTIONS = "AdminActions";
    private static final String COLUMN_ACTION_ID = "ActionId"; // Unique column name
    private static final String COLUMN_ACTION_TEXT = "ActionText";
    private static final String COLUMN_ACTION_TIMESTAMP = "ActionTimestamp";


    // SQL Statements
    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USERNAME + " TEXT PRIMARY KEY, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_SECURITY_QUESTION + " TEXT, " +
                    COLUMN_SECURITY_ANSWER + " TEXT, " +
                    COLUMN_IS_DELETED + " INTEGER DEFAULT 0);";

    private static final String CREATE_CONVERSION_HISTORY_TABLE =
            "CREATE TABLE " + TABLE_CONVERSION_HISTORY + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FROM_UNIT + " TEXT, " +
                    COLUMN_TO_UNIT + " TEXT, " +
                    COLUMN_FROM_AMOUNT + " REAL, " +
                    COLUMN_TO_AMOUNT + " REAL, " +
                    COLUMN_LABEL + " TEXT, " +
                    COLUMN_HISTORY_USERNAME + " TEXT, " +
                    "FOREIGN KEY(" + COLUMN_HISTORY_USERNAME + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USERNAME + "), " +
                    "UNIQUE(" + COLUMN_LABEL + ", " + COLUMN_HISTORY_USERNAME + ") ON CONFLICT REPLACE);";

    private static final String CREATE_ADMIN_ACTIONS_TABLE =
            "CREATE TABLE " + TABLE_ADMIN_ACTIONS + " (" +
                    COLUMN_ACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ACTION_TEXT + " TEXT, " +
                    COLUMN_ACTION_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP);";



    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d("DBHelper", "Creating tables...");
        db.execSQL(CREATE_USERS_TABLE);
        Log.d("DBHelper", "Users table created successfully.");
        db.execSQL(CREATE_CONVERSION_HISTORY_TABLE);
        Log.d("DBHelper", "ConversionHistory table created successfully.");
        db.execSQL(CREATE_ADMIN_ACTIONS_TABLE);
        Log.d("DBHelper", "AdminActions table created successfully");


        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_USERNAME, "admin@unitgenie.com");
        adminValues.put(COLUMN_PASSWORD, encryptPassword("Admin123!")); // Secure admin password
        adminValues.put(COLUMN_SECURITY_QUESTION, "Default");
        adminValues.put(COLUMN_SECURITY_ANSWER, "Admin");

        db.insert(TABLE_USERS, null, adminValues);
        Log.d("DBHelper", "Admin user created successfully");
    }



    public void logAdminAction(String actions) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ACTION_TEXT, actions);
            long result = db.insert(TABLE_ADMIN_ACTIONS, null, values);
            if (result == -1) {
                Log.e("DBHelper", "Error inserting action: " + actions);
            } else {
                Log.d("DBHelper", "Action logged successfully: " + actions);
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Exception in logAdminAction: ", e);
        } finally {
            db.close();
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 10) {
            // Add the 'isDeleted' column if upgrading from a version before 9
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_IS_DELETED + " INTEGER DEFAULT 0;");
            Log.d("DBHelper", "Column 'isDeleted' added to Users table");
        }

    }


    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedPassword = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedPassword) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean registerUser(String username, String password, String securityQuestion, String securityAnswer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, encryptPassword(password));
        values.put(COLUMN_SECURITY_QUESTION, securityQuestion);
        values.put(COLUMN_SECURITY_ANSWER, securityAnswer);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUserCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String encryptedPassword = encryptPassword(password);

        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ? AND " + COLUMN_IS_DELETED + " = 0";
        String[] selectionArgs = {username, encryptedPassword};

        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }


    public boolean addConversionHistory(String username, String fromUnit, double fromAmount, String toUnit, double toAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HISTORY_USERNAME, username);
        values.put(COLUMN_FROM_UNIT, fromUnit);
        values.put(COLUMN_FROM_AMOUNT, fromAmount);
        values.put(COLUMN_TO_UNIT, toUnit);
        values.put(COLUMN_TO_AMOUNT, toAmount);

        // Format label to store conversion as "2 m = 0.002 km"
        String label = String.format("%.5f %s = %.5f %s", fromAmount, fromUnit, toAmount, toUnit);
        values.put(COLUMN_LABEL, label);

        long result = db.insert(TABLE_CONVERSION_HISTORY, null, values);
        db.close();
        return result != -1;
    }


    public Cursor getConversionHistory(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_HISTORY_USERNAME + " = ?";
        String[] selectionArgs = {username};

        // Order by 'id' in descending order to get the most recent conversions first
        return db.query(
                TABLE_CONVERSION_HISTORY,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "id DESC" // Order by id in descending order
        );
    }


    public boolean deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_DELETED, 1);

        int rowsUpdated = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();
        return rowsUpdated > 0;
    }

    @SuppressLint("Range")
    public String getSecurityQuestion(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_SECURITY_QUESTION},
                COLUMN_USERNAME + " = ?", new String[]{username}, null, null, null);

        String question = null;
        if (cursor.moveToFirst()) {
            question = cursor.getString(cursor.getColumnIndex(COLUMN_SECURITY_QUESTION));
        }
        cursor.close();
        db.close();
        return question;
    }

    public boolean checkSecurityAnswer(String username, String answer) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_SECURITY_ANSWER},
                COLUMN_USERNAME + " = ? AND " + COLUMN_SECURITY_ANSWER + " = ?", new String[]{username, answer}, null, null, null);

        boolean result = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return result;
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, encryptPassword(newPassword));

        int rowsUpdated = db.update(TABLE_USERS, values, COLUMN_USERNAME + " = ?", new String[]{username});
        db.close();
        return rowsUpdated > 0;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }
    public boolean deleteHistoryItem(String username, String historyLabel) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(
                TABLE_CONVERSION_HISTORY,
                COLUMN_HISTORY_USERNAME + " = ? AND " + COLUMN_LABEL + " = ?",
                new String[]{username, historyLabel}
        );
        db.close();
        return rowsDeleted > 0;
    }

    public List<String> getAllUsers() {
        List<String> users = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to exclude the admin user
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + " != ?", new String[]{"admin@unitgenie.com"},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                users.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return users;
    }



    public List<String[]> getLastAdminLogs(int limit) {
        List<String[]> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get the last 'limit' admin logs
        String query = "SELECT " + COLUMN_ACTION_TEXT + ", " + COLUMN_ACTION_TIMESTAMP +
                " FROM " + TABLE_ADMIN_ACTIONS +
                " ORDER BY " + COLUMN_ACTION_ID + " DESC LIMIT ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(limit)});

        if (cursor.moveToFirst()) {
            do {
                String actionText = cursor.getString(0);
                String timestamp = cursor.getString(1);
                logs.add(new String[]{actionText, timestamp});
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return logs;
    }


    public boolean isUserDeleted(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_IS_DELETED + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        boolean isDeleted = false;
        if (cursor.moveToFirst()) {
            isDeleted = cursor.getInt(0) == 1; // Check if the flag is 1
        }
        cursor.close();
        db.close();
        return isDeleted;
    }


}
