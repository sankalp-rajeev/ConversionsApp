
package com.example.conversionsapp;
import com.example.conversionsapp.HomeScreen;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginUser extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private CheckBox checkBoxRememberMe;
    private TextView textViewRegister, textViewForgotPassword;
    private UserDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new UserDatabaseHelper(this);

        editTextUsername = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe);
        textViewRegister = findViewById(R.id.textViewRegister);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        // Check if the user is already logged in
        checkLoginStatus();

        // Login Button Click Listener
        buttonLogin.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginUser.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.isUserDeleted(username)) {
                Toast.makeText(LoginUser.this, "Your account has been deleted. Please contact support.", Toast.LENGTH_LONG).show();
                return; // Stop further execution
            }
           if (dbHelper.checkUserCredentials(username, password)) {
                if (username.equals("admin@unitgenie.com")) {
                    // Admin Login
                    Toast.makeText(LoginUser.this, "Admin login successful", Toast.LENGTH_SHORT).show();

                    // Redirect to Admin Dashboard
                    Intent intent = new Intent(LoginUser.this,AdminDashboard.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Normal User Login
                    Toast.makeText(LoginUser.this, "Login successful", Toast.LENGTH_SHORT).show();

                    // Save the login session if "Remember Me" is checked
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.putBoolean("isLoggedIn", true);
                    editor.putBoolean("rememberMe", checkBoxRememberMe.isChecked());
                    editor.apply();

                    // Redirect to HomeScreen
                    Intent intent = new Intent(LoginUser.this, HomeScreen.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(LoginUser.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Register TextView Click Listener
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginUser.this, RegisterUser.class);  // Assuming you have a RegisterUser activity
            startActivity(intent);
        });

        // Forgot Password TextView Click Listener
        textViewForgotPassword.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            showResetPasswordDialog(username);
        });
    }

    private void checkLoginStatus() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        boolean rememberMe = sharedPreferences.getBoolean("rememberMe", false);

        if (isLoggedIn && rememberMe) {
            // If already logged in and "Remember Me" is checked
            Intent intent = new Intent(LoginUser.this, HomeScreen.class);
            startActivity(intent);
            finish();
        }
    }

    private void showResetPasswordDialog(String username) {
        // Fetch the security question for the user
        String securityQuestion = dbHelper.getSecurityQuestion(username);
        if (securityQuestion == null) {
            Toast.makeText(this, "User not found or security question is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        // Show the security question in a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Security Question");
        final EditText inputAnswer = new EditText(this);
        inputAnswer.setHint("Answer the question");
        builder.setMessage(securityQuestion);
        builder.setView(inputAnswer);
        builder.setPositiveButton("Submit", (dialog, which) -> {
            String answer = inputAnswer.getText().toString().trim();
            if (dbHelper.checkSecurityAnswer(username, answer)) {
                showNewPasswordDialog(username);
            } else {
                Toast.makeText(LoginUser.this, "Incorrect answer", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
    // Show dialog to reset the password after security question is correct
    private void showNewPasswordDialog(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set New Password");
        final EditText inputPassword = new EditText(this);
        inputPassword.setHint("New Password");
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(inputPassword);
        builder.setPositiveButton("Reset", (dialog, which) -> {
            String newPassword = inputPassword.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                dbHelper.updatePassword(username, newPassword);
                Toast.makeText(LoginUser.this, "Password reset successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginUser.this, "Please enter a valid password", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
