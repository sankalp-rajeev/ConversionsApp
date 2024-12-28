package com.example.conversionsapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AdminDashboard extends AppCompatActivity {

    private UserDatabaseHelper dbHelper;
    private ListView userListView;
    private Button viewAdminLogsButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        try {
            dbHelper = new UserDatabaseHelper(this);
            userListView = findViewById(R.id.userListView);
            viewAdminLogsButton = findViewById(R.id.viewAdminLogsButton);
            logoutButton = findViewById(R.id.buttonLogout);

            // Load user list
            loadUsers();

            // View admin logs
            viewAdminLogsButton.setOnClickListener(v -> {
                List<String[]> logs = dbHelper.getLastAdminLogs(5); // Fetch last 5 logs
                if (!logs.isEmpty()) {
                    showLogsInDialog(logs);
                } else {
                    Toast.makeText(AdminDashboard.this, "No logs available", Toast.LENGTH_SHORT).show();
                }
            });

            // Logout button logic
            logoutButton.setOnClickListener(v -> {
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear(); // Clear all session data
                editor.apply();

                Toast.makeText(AdminDashboard.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AdminDashboard.this, LoginUser.class);
                startActivity(intent);
                finish();
            });
        } catch (Exception e) {
            Log.e("AdminDashboard", "Error in AdminDashboard: ", e);
            Toast.makeText(this, "Error loading Admin Dashboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUsers() {
        List<String> users = dbHelper.getAllUsers();
        if (users.isEmpty()) {
            Toast.makeText(this, "No users found", Toast.LENGTH_SHORT).show();
        } else {
            UserAdapter adapter = new UserAdapter(this, users, dbHelper);
            userListView.setAdapter(adapter);
        }
    }

    private void showLogsInDialog(List<String[]> logs) {
        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recent Admin Logs");

        // Create a TableLayout dynamically
        TableLayout tableLayout = new TableLayout(this);

        // Add a header row
        TableRow headerRow = new TableRow(this);

        TextView headerAction = new TextView(this);
        headerAction.setText("Activity");
        headerAction.setPadding(8, 8, 8, 8);
        headerAction.setTextSize(16);
        headerAction.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView headerTimestamp = new TextView(this);
        headerTimestamp.setText("Timestamp");
        headerTimestamp.setPadding(8, 8, 8, 8);
        headerTimestamp.setTextSize(16);
        headerTimestamp.setTypeface(null, android.graphics.Typeface.BOLD);

        headerRow.addView(headerAction);
        headerRow.addView(headerTimestamp);
        tableLayout.addView(headerRow);

        // Add rows for each log
        for (String[] log : logs) {
            TableRow row = new TableRow(this);

            TextView actionText = new TextView(this);
            actionText.setText(log[0]); // Activity text
            actionText.setPadding(8, 8, 8, 8);
            actionText.setTextSize(14);

            TextView timestampText = new TextView(this);
            timestampText.setText(log[1]); // Timestamp
            timestampText.setPadding(8, 8, 8, 8);
            timestampText.setTextSize(14);

            row.addView(actionText);
            row.addView(timestampText);

            tableLayout.addView(row);
        }

        // Set the table layout in the dialog
        builder.setView(tableLayout);
        builder.setPositiveButton("Close", null);
        builder.show();
    }
}
