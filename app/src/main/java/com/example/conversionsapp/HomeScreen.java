package com.example.conversionsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import android.database.Cursor;
import android.annotation.SuppressLint;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeScreen extends AppCompatActivity {

    private Spinner fromUnitSpinner, toUnitSpinner, conversionTypeSpinner;
    private EditText inputValue;
    private TextView resultTextView,  formulaTextView;;
    private Button convertButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView; // Make this a class-level variable
    private UserDatabaseHelper dbHelper;
    private TextView formulaTextCaption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);  // Ensure this matches your layout file

        dbHelper = new UserDatabaseHelper(this);

        inputValue = findViewById(R.id.inputValue);
        conversionTypeSpinner = findViewById(R.id.conversionTypeSpinner);
        fromUnitSpinner = findViewById(R.id.fromUnitSpinner);
        toUnitSpinner = findViewById(R.id.toUnitSpinner);
        resultTextView = findViewById(R.id.resultTextView);
        formulaTextView = findViewById(R.id.formulaTextView);
        formulaTextCaption = findViewById(R.id.formulaTextCaption);
        convertButton = findViewById(R.id.convertButton);

        RecyclerView historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadConversionHistory(historyRecyclerView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);


        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(androidx.constraintlayout.widget.R.drawable.abc_btn_radio_material_anim);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        updateDrawerMenu(navigationView);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_view_conversion_history) {
                    Toast.makeText(HomeScreen.this, "View Conversion History clicked", Toast.LENGTH_SHORT).show();
                } else {
                    return false;
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.conversion_types_array,
                android.R.layout.simple_spinner_item
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conversionTypeSpinner.setAdapter(typeAdapter);

        // Listener for conversion type spinner
        conversionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = conversionTypeSpinner.getSelectedItem().toString();
                updateUnitSpinners(selectedType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Listener for Convert Button
        convertButton.setOnClickListener(v -> performConversion());
    }

    public void updateUnitSpinners(String selectedType) {
        int unitArrayId;
        switch (selectedType){
            case "Length":
                unitArrayId = R.array.length_units_array;
                break;

            case "Weight":
                unitArrayId = R.array.weight_units_array;
                break;

            case "Volume":
                unitArrayId = R.array.volume_units_array;
                break;

            case "Temperature":
                unitArrayId = R.array.temperature_units_array;
                break;

            default:
                unitArrayId = R.array.units_array;
                break;
        }
        // Set up the unit spinners
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this,
                unitArrayId,
                android.R.layout.simple_spinner_item
        );
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromUnitSpinner.setAdapter(unitAdapter);
        toUnitSpinner.setAdapter(unitAdapter);
    }


    private void updateDrawerMenu(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        menu.clear(); // Clear the existing menu to avoid duplication

        // Access the SearchView from the header layout
        View headerView = navigationView.getHeaderView(0);
        SearchView searchView = headerView.findViewById(R.id.drawer_search_view);

        // List to store dynamic menu items for filtering
        List<MenuItem> menuItems = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        // Regular expression to allow only valid alphanumeric and space characters
        String validSearchPattern = "^[a-zA-Z0-9 ]*$";

        // Populate the drawer menu with conversion history
        if (username != null) {
            Cursor cursor = dbHelper.getConversionHistory(username);

            if (cursor != null && cursor.moveToFirst()) { // Start from the most recent entry
                do {
                    @SuppressLint("Range")
                    String label = cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.COLUMN_LABEL));

                    // Dynamically add menu items
                    MenuItem item = menu.add(label);
                    menuItems.add(item);

                    // Set click listener for each item
                    item.setOnMenuItemClickListener(menuItem -> {
                        Toast.makeText(this, "Selected: " + label, Toast.LENGTH_SHORT).show();
                        return true;
                    });
                } while (cursor.moveToNext()); // Iterate over the rest of the entries

                cursor.close();
            } else {
                Toast.makeText(this, "No conversion history available.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not logged in. Cannot display history.", Toast.LENGTH_SHORT).show();
        }

        // Handle SearchView query changes for filtering
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Optionally handle query submission here
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Check if the input contains invalid characters
                if (!newText.matches(validSearchPattern)) {
                    Toast.makeText(getApplicationContext(), "Invalid characters entered. Please use letters, numbers, and spaces only.", Toast.LENGTH_SHORT).show();
                    return true; // Prevent further filtering
                }

                // Filter menu items based on search input
                boolean hasResults = false;
                for (MenuItem item : menuItems) {
                    if (item.getTitle() != null) {
                        boolean matches = item.getTitle().toString().toLowerCase().contains(newText.toLowerCase());
                        item.setVisible(matches);
                        if (matches) {
                            hasResults = true; // At least one result found
                        }
                    }
                }

                // Show a message if no results are found
                if (!hasResults && !newText.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No matching results found.", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        // Set up default behavior if the menu is empty
        if (menuItems.isEmpty()) {
            MenuItem emptyItem = menu.add("No history available");
            emptyItem.setEnabled(false); // Disable the item to indicate no interactions are allowed
        }
    }




    private void performConversion() {
        String fromUnit = fromUnitSpinner.getSelectedItem().toString();
        String toUnit = toUnitSpinner.getSelectedItem().toString();
        String input = inputValue.getText().toString();

        if (input.isEmpty()) {
            resultTextView.setVisibility(View.GONE);
            formulaTextView.setVisibility(View.GONE);
            formulaTextCaption.setVisibility(View.GONE);
            resultTextView.setText("Please enter a value.");
            return;
        }

        double inputValue = Double.parseDouble(input);
        double result = convertUnits(inputValue, fromUnit, toUnit);

        if (result != 0) {
            resultTextView.setText("Result: " + result);
            resultTextView.setVisibility(View.VISIBLE);
            formulaTextView.setVisibility(View.VISIBLE);
            formulaTextCaption.setVisibility(View.VISIBLE);
        } else {
            resultTextView.setVisibility(View.GONE);
            formulaTextView.setVisibility(View.GONE);
            formulaTextCaption.setVisibility(View.GONE);
        }

        // Save conversion to history
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username != null) {
            boolean isSaved = dbHelper.addConversionHistory(username, fromUnit, inputValue, toUnit, result);

            if (isSaved) {
                Toast.makeText(this, "Conversion saved to history", Toast.LENGTH_SHORT).show();

                // Refresh the history RecyclerView
                RecyclerView historyRecyclerView = findViewById(R.id.historyRecyclerView);
                loadConversionHistory(historyRecyclerView);

                // Update the drawer menu to reload with the latest data
                updateDrawerMenu(navigationView);
            } else {
                Toast.makeText(this, "Failed to save conversion", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not logged in. Conversion not saved.", Toast.LENGTH_SHORT).show();
        }
    }



    private String getConversionFormula(String fromUnit, String toUnit, double inputValue, double result) {
        // Round values to 3 decimal places
        String inputValueStr = String.format("%.5f", inputValue);
        String resultStr = String.format("%.5f", result);

        // Length Conversions
        if (fromUnit.equals("Meters") && toUnit.equals("Kilometers")) {
            return "Formula: " + inputValueStr + " M ÷ 1000 = " + resultStr + " Km";
        } else if (fromUnit.equals("Kilometers") && toUnit.equals("Meters")) {
            return "Formula: " + inputValueStr + " Km × 1000 = " + resultStr + " M";
        } else if (fromUnit.equals("Meters") && toUnit.equals("Centimeters")) {
            return "Formula: " + inputValueStr + " M × 100 = " + resultStr + " Cm";
        } else if (fromUnit.equals("Centimeters") && toUnit.equals("Meters")) {
            return "Formula: " + inputValueStr + " Cm ÷ 100 = " + resultStr + " M";
        } else if (fromUnit.equals("Meters") && toUnit.equals("Millimeters")) {
            return "Formula: " + inputValueStr + " M × 1000 = " + resultStr + " Mm";
        } else if (fromUnit.equals("Millimeters") && toUnit.equals("Meters")) {
            return "Formula: " + inputValueStr + " Mm ÷ 1000 = " + resultStr + " M";
        } else if (fromUnit.equals("Meters") && toUnit.equals("Inches")) {
            return "Formula: " + inputValueStr + " M × 39.3701 = " + resultStr + " Inches";
        } else if (fromUnit.equals("Inches") && toUnit.equals("Meters")) {
            return "Formula: " + inputValueStr + " Inches ÷ 39.3701 = " + resultStr + " M";
        } else if (fromUnit.equals("Meters") && toUnit.equals("Feet")) {
            return "Formula: " + inputValueStr + " M × 3.28084 = " + resultStr + " Feet";
        } else if (fromUnit.equals("Feet") && toUnit.equals("Meters")) {
            return "Formula: " + inputValueStr + " Feet ÷ 3.28084 = " + resultStr + " M";
        } else if (fromUnit.equals("Kilometers") && toUnit.equals("Miles")) {
            return "Formula: " + inputValueStr + " Km × 0.621371 = " + resultStr + " Miles";
        } else if (fromUnit.equals("Miles") && toUnit.equals("Kilometers")) {
            return "Formula: " + inputValueStr + " Miles ÷ 0.621371 = " + resultStr + " Km";
        } else if (fromUnit.equals("Inches") && toUnit.equals("Centimeters")) {
            return "Formula: " + inputValueStr + " Inches × 2.54 = " + resultStr + " Cm";
        } else if (fromUnit.equals("Centimeters") && toUnit.equals("Inches")) {
            return "Formula: " + inputValueStr + " Cm ÷ 2.54 = " + resultStr + " Inches";
        } else if (fromUnit.equals("Feet") && toUnit.equals("Inches")) {
            return "Formula: " + inputValueStr + " Feet × 12 = " + resultStr + " Inches";
        } else if (fromUnit.equals("Inches") && toUnit.equals("Feet")) {
            return "Formula: " + inputValueStr + " Inches ÷ 12 = " + resultStr + " Feet";
        }

        // Weight Conversions
        else if (fromUnit.equals("Grams") && toUnit.equals("Kilograms")) {
            return "Formula: " + inputValueStr + " G ÷ 1000 = " + resultStr + " Kg";
        } else if (fromUnit.equals("Kilograms") && toUnit.equals("Grams")) {
            return "Formula: " + inputValueStr + " Kg × 1000 = " + resultStr + " G";
        } else if (fromUnit.equals("Grams") && toUnit.equals("Milligrams")) {
            return "Formula: " + inputValueStr + " G × 1000 = " + resultStr + " Mg";
        } else if (fromUnit.equals("Milligrams") && toUnit.equals("Grams")) {
            return "Formula: " + inputValueStr + " Mg ÷ 1000 = " + resultStr + " G";
        } else if (fromUnit.equals("Kilograms") && toUnit.equals("Pounds")) {
            return "Formula: " + inputValueStr + " Kg × 2.20462 = " + resultStr + " Lbs";
        } else if (fromUnit.equals("Pounds") && toUnit.equals("Kilograms")) {
            return "Formula: " + inputValueStr + " Lbs ÷ 2.20462 = " + resultStr + " Kg";
        } else if (fromUnit.equals("Pounds") && toUnit.equals("Ounces")) {
            return "Formula: " + inputValueStr + " Lbs × 16 = " + resultStr + " Oz";
        } else if (fromUnit.equals("Ounces") && toUnit.equals("Pounds")) {
            return "Formula: " + inputValueStr + " Oz ÷ 16 = " + resultStr + " Lbs";
        }

        // Volume Conversions
        else if (fromUnit.equals("Liters") && toUnit.equals("Milliliters")) {
            return "Formula: " + inputValueStr + " L × 1000 = " + resultStr + " Ml";
        } else if (fromUnit.equals("Milliliters") && toUnit.equals("Liters")) {
            return "Formula: " + inputValueStr + " Ml ÷ 1000 = " + resultStr + " L";
        } else if (fromUnit.equals("Liters") && toUnit.equals("Cubic Meters")) {
            return "Formula: " + inputValueStr + " L ÷ 1000 = " + resultStr + " m³";
        } else if (fromUnit.equals("Cubic Meters") && toUnit.equals("Liters")) {
            return "Formula: " + inputValueStr + " m³ × 1000 = " + resultStr + " L";
        } else if (fromUnit.equals("Gallons") && toUnit.equals("Liters")) {
            return "Formula: " + inputValueStr + " Gal × 3.78541 = " + resultStr + " L";
        } else if (fromUnit.equals("Liters") && toUnit.equals("Gallons")) {
            return "Formula: " + inputValueStr + " L ÷ 3.78541 = " + resultStr + " Gal";
        }

        // Time Conversions
        else if (fromUnit.equals("Seconds") && toUnit.equals("Minutes")) {
            return "Formula: " + inputValueStr + " Sec ÷ 60 = " + resultStr + " Min";
        } else if (fromUnit.equals("Minutes") && toUnit.equals("Seconds")) {
            return "Formula: " + inputValueStr + " Min × 60 = " + resultStr + " Sec";
        } else if (fromUnit.equals("Minutes") && toUnit.equals("Hours")) {
            return "Formula: " + inputValueStr + " Min ÷ 60 = " + resultStr + " Hr";
        } else if (fromUnit.equals("Hours") && toUnit.equals("Minutes")) {
            return "Formula: " + inputValueStr + " Hr × 60 = " + resultStr + " Min";
        } else if (fromUnit.equals("Hours") && toUnit.equals("Days")) {
            return "Formula: " + inputValueStr + " Hr ÷ 24 = " + resultStr + " Day";
        } else if (fromUnit.equals("Days") && toUnit.equals("Hours")) {
            return "Formula: " + inputValueStr + " Day × 24 = " + resultStr + " Hr";
        }

        // Temperature Conversions
        else if (fromUnit.equals("Celsius") && toUnit.equals("Fahrenheit")) {
            return "Formula: (" + inputValueStr + " °C × 9 ÷ 5) + 32 = " + resultStr + " °F";
        } else if (fromUnit.equals("Fahrenheit") && toUnit.equals("Celsius")) {
            return "Formula: (" + inputValueStr + " °F - 32) × 5 ÷ 9 = " + resultStr + " °C";
        } else if (fromUnit.equals("Celsius") && toUnit.equals("Kelvin")) {
            return "Formula: " + inputValueStr + " °C + 273.15 = " + resultStr + " K";
        } else if (fromUnit.equals("Kelvin") && toUnit.equals("Celsius")) {
            return "Formula: " + inputValueStr + " K - 273.15 = " + resultStr + " °C";
        } else if (fromUnit.equals("Fahrenheit") && toUnit.equals("Kelvin")) {
            return "Formula: ((" + inputValueStr + " °F - 32) × 5 ÷ 9) + 273.15 = " + resultStr + " K";
        } else if (fromUnit.equals("Kelvin") && toUnit.equals("Fahrenheit")) {
            return "Formula: ((" + inputValueStr + " K - 273.15) × 9 ÷ 5) + 32 = " + resultStr + " °F";
        }

        // Unsupported Conversion
        return "Unsupported conversion from " + fromUnit + " to " + toUnit;
    }




    private double convertUnits(double inputValue, String fromUnit, String toUnit) {
        double result;

        // Length Conversions
        if (fromUnit.equals("Meters") && toUnit.equals("Kilometers")) {
            result = inputValue / 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Kilometers") && toUnit.equals("Meters")) {
            result = inputValue * 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Meters") && toUnit.equals("Centimeters")) {
            result = inputValue * 100;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Centimeters") && toUnit.equals("Meters")) {
            result = inputValue / 100;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Meters") && toUnit.equals("Millimeters")) {
            result = inputValue * 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Millimeters") && toUnit.equals("Meters")) {
            result = inputValue / 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Meters") && toUnit.equals("Inches")) {
            result = inputValue * 39.3701;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Inches") && toUnit.equals("Meters")) {
            result = inputValue / 39.3701;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Meters") && toUnit.equals("Feet")) {
            result = inputValue * 3.28084;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Feet") && toUnit.equals("Meters")) {
            result = inputValue / 3.28084;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Kilometers") && toUnit.equals("Miles")) {
            result = inputValue * 0.621371;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Miles") && toUnit.equals("Kilometers")) {
            result = inputValue / 0.621371;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Inches") && toUnit.equals("Centimeters")) {
            result = inputValue * 2.54;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Centimeters") && toUnit.equals("Inches")) {
            result = inputValue / 2.54;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Feet") && toUnit.equals("Inches")) {
            result = inputValue * 12;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Inches") && toUnit.equals("Feet")) {
            result = inputValue / 12;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        }

        // Weight Conversions
        else if (fromUnit.equals("Grams") && toUnit.equals("Kilograms")) {
            result = inputValue / 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Kilograms") && toUnit.equals("Grams")) {
            result = inputValue * 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Grams") && toUnit.equals("Milligrams")) {
            result = inputValue * 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Milligrams") && toUnit.equals("Grams")) {
            result = inputValue / 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Kilograms") && toUnit.equals("Pounds")) {
            result = inputValue * 2.20462;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Pounds") && toUnit.equals("Kilograms")) {
            result = inputValue / 2.20462;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Pounds") && toUnit.equals("Ounces")) {
            result = inputValue * 16;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Ounces") && toUnit.equals("Pounds")) {
            result = inputValue / 16;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        }

        // Volume Conversions
        else if (fromUnit.equals("Liters") && toUnit.equals("Milliliters")) {
            result = inputValue * 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Milliliters") && toUnit.equals("Liters")) {
            result = inputValue / 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Liters") && toUnit.equals("Cubic Meters")) {
            result = inputValue / 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Cubic Meters") && toUnit.equals("Liters")) {
            result = inputValue * 1000;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Gallons") && toUnit.equals("Liters")) {
            result = inputValue * 3.78541;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Liters") && toUnit.equals("Gallons")) {
            result = inputValue / 3.78541;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        }

        // Time Conversions
        else if (fromUnit.equals("Seconds") && toUnit.equals("Minutes")) {
            result = inputValue / 60;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Minutes") && toUnit.equals("Seconds")) {
            result = inputValue * 60;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Minutes") && toUnit.equals("Hours")) {
            result = inputValue / 60;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Hours") && toUnit.equals("Minutes")) {
            result = inputValue * 60;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Hours") && toUnit.equals("Days")) {
            result = inputValue / 24;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Days") && toUnit.equals("Hours")) {
            result = inputValue * 24;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        }

        // Temperature Conversions
        else if (fromUnit.equals("Celsius") && toUnit.equals("Fahrenheit")) {
            result = (inputValue * 9 / 5) + 32;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Fahrenheit") && toUnit.equals("Celsius")) {
            result = (inputValue - 32) * 5 / 9;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Celsius") && toUnit.equals("Kelvin")) {
            result = inputValue + 273.15;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Kelvin") && toUnit.equals("Celsius")) {
            result = inputValue - 273.15;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Fahrenheit") && toUnit.equals("Kelvin")) {
            result = (inputValue - 32) * 5 / 9 + 273.15;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        } else if (fromUnit.equals("Kelvin") && toUnit.equals("Fahrenheit")) {
            result = (inputValue - 273.15) * 9 / 5 + 32;
            formulaTextView.setText(getConversionFormula(fromUnit, toUnit, inputValue, result));
            return result;
        }

        // Unsupported Conversion
        else {
            showErrorToast("Unsupported conversion from " + fromUnit + " to " + toUnit);
            return 0; // Return 0 if conversion fails
        }
    }


    // Method to show error message (For unsupported conversions)
    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu); // Ensure this file name matches your XML
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_user) {
            View anchorView = findViewById(R.id.action_user); // Find the user icon view
            showUserOptionsMenu(anchorView);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showUserOptionsMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_user_options, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_logout) {
                logoutUser();
                return true;
            } else if (itemId == R.id.action_delete_user) {
                confirmDeleteUser();
                return true;
            } else if (itemId == R.id.action_reset_password) {
                resetPassword();
                return true;
            } else {
                return false;
            }
        });

        popup.show();
    }

    private void confirmDeleteUser() {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user? This action cannot be undone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteUser();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "Error: No logged-in user found", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isDeleted = dbHelper.deleteUser(username);

        if (isDeleted) {
            Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("username");
            editor.apply();

            logoutUser();
        } else {
            Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutUser() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(HomeScreen.this, LoginUser.class);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
    }
    private void loadConversionHistory(RecyclerView historyRecyclerView) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        if (username != null) {
            Cursor cursor = dbHelper.getConversionHistory(username);
            List<String> historyList = new ArrayList<>();

            if (cursor != null && cursor.moveToFirst()) {
                int count = 0; // Limit the number of items to 5
                do {
                    @SuppressLint("Range")
                    String label = cursor.getString(cursor.getColumnIndex(UserDatabaseHelper.COLUMN_LABEL));
                    historyList.add(label);
                    count++;
                } while (cursor.moveToNext() && count < 5); // Stop after adding 5 items
                cursor.close();
            }

            ConversionHistoryAdapter adapter = new ConversionHistoryAdapter(
                    this,
                    historyList,
                    historyLabel -> {
                        boolean isDeleted = dbHelper.deleteHistoryItem(username, historyLabel);
                        if (isDeleted) {
                            Toast.makeText(this, "History item deleted", Toast.LENGTH_SHORT).show();
                            loadConversionHistory(historyRecyclerView); // Refresh the RecyclerView
                        } else {
                            Toast.makeText(this, "Failed to delete history item", Toast.LENGTH_SHORT).show();
                        }
                    },
                    () -> updateDrawerMenu(navigationView) // Pass the updateDrawerMenu method as a Runnable
            );

            historyRecyclerView.setAdapter(adapter);
            if (historyList.isEmpty()) {
                Toast.makeText(this, "No history available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }




    private void resetPassword() {
        Toast.makeText(this, "Reset Password clicked", Toast.LENGTH_SHORT).show();
    }
}





