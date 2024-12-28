package com.example.conversionsapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class RegisterUser extends AppCompatActivity {
//    UserDatabaseHelper.openDatabase();
private EditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextSecurityAnswer;
    private Spinner spinnerSecurityQuestion;
    private Button buttonSignUp;
    private TextView textViewBackToLogin;
    private UserDatabaseHelper dbHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize the database helper
        dbHelper = new UserDatabaseHelper(this);
//        dbHelper.openDatabase();
        // Initialize UI components
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextSecurityAnswer = findViewById(R.id.editTextSecurityAnswer);
        spinnerSecurityQuestion = findViewById(R.id.spinnerSecurityQuestion);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewBackToLogin = findViewById(R.id.textViewBackToLogin);

        // Set onClickListener for Sign-Up Button
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input values
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();
                String securityQuestion = spinnerSecurityQuestion.getSelectedItem().toString();
                String securityAnswer = editTextSecurityAnswer.getText().toString().trim();
                // Validate input fields
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || securityAnswer.isEmpty() || securityQuestion.isEmpty() ) {
                    Toast.makeText(RegisterUser.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate email format
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegisterUser.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if passwords match
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterUser.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Encrypt password
                String encryptedPassword = encryptPassword(password);

                // Register the user in the database
                if (dbHelper.registerUser(email, password, securityQuestion, securityAnswer)) {
                    Toast.makeText(RegisterUser.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterUser.this, LoginUser.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterUser.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set onClickListener for Back to Login link
        textViewBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to Login Activity
                Intent intent = new Intent(RegisterUser.this, LoginUser.class);
                startActivity(intent);
                finish(); // Close Register Activity
            }
        });
    }

    // Method to encrypt password using SHA-256
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close(); // Close the database connection when the activity is destroyed
    }
}
