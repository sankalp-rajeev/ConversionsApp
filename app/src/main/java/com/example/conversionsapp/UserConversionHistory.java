package com.example.conversionsapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserConversionHistory extends AppCompatActivity {

    private RecyclerView conversionRecyclerView;
    private UserDatabaseHelper dbHelper;
    private String username;
    private List<String> conversionHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_conversion_history);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true); // Enable back button
            actionBar.setTitle("Conversion History"); // Set the title
        }

        // Initialize database helper and RecyclerView
        dbHelper = new UserDatabaseHelper(this);
        conversionRecyclerView = findViewById(R.id.conversionRecyclerView);

        // Retrieve the username passed from the previous activity
        username = getIntent().getStringExtra("username");
        if (username == null) {
            Toast.makeText(this, "No user specified", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load and display conversion history
        loadConversionHistory();
    }

    private void loadConversionHistory() {
        conversionHistory = new ArrayList<>();

        // Fetch conversion history for the user
        Cursor cursor = dbHelper.getConversionHistory(username);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String label = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_LABEL));
                conversionHistory.add(label);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (conversionHistory.isEmpty()) {
            Toast.makeText(this, "No conversion history found for " + username, Toast.LENGTH_SHORT).show();
        } else {
            // Reverse the list to show the newest conversions on top
            Collections.reverse(conversionHistory);
            setupRecyclerView();
        }
    }

    private void setupRecyclerView() {
        // Pass a Runnable to refresh the drawer when a history item is deleted
        ConversionHistoryAdapter adapter = new ConversionHistoryAdapter(
                this,
                conversionHistory,
                historyLabel -> {
                    boolean isDeleted = dbHelper.deleteHistoryItem(username, historyLabel);
                    if (isDeleted) {
                        Toast.makeText(this, "History item deleted", Toast.LENGTH_SHORT).show();
                        loadConversionHistory(); // Refresh the history
                    } else {
                        Toast.makeText(this, "Failed to delete history item", Toast.LENGTH_SHORT).show();
                    }
                },
                null // No drawer updates needed in this activity
        );

        conversionRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        conversionRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Navigate back to Admin Dashboard
        Intent intent = new Intent(this, AdminDashboard.class);
        startActivity(intent);
        finish();
        return true;
    }
}
