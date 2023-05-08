package com.example.capstone;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

public class Purchase extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TableLayout tableLayout;
    private String[][] data = {{"John", "Doe", "25"}, {"Jane", "Doe", "28"}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purchase);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        tableLayout = findViewById(R.id.tableLayout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        Button btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                // Handle menu item clicks here
                int id = menuItem.getItemId();
                if (id == R.id.home) {
                    // Start the HomeActivity
                    Intent intent = new Intent(Purchase.this, Home.class);
                    startActivity(intent);
                } else if (id == R.id.inventory) {
                    // Start the InventoryActivity
                    Intent intent = new Intent(Purchase.this, Inventory.class);
                    startActivity(intent);
                } else if (id == R.id.sales) {
                    // Start the SalesChartActivity
                    Intent intent = new Intent(Purchase.this, Sales.class);
                    startActivity(intent);
                } else if (id == R.id.purchase) {
                    // Start the PurchaseListActivity
                    Intent intent = new Intent(Purchase.this, Purchase.class);
                    startActivity(intent);
                } else if (id == R.id.logout) {
                    // Start the PurchaseListActivity
                    Intent intent = new Intent(Purchase.this, SignIn.class);
                    startActivity(intent);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //table
        // Create header row
        TableRow headerRow = new TableRow(this);
        String[] headerText = {"First Name", "Last Name", "Age"};
        for (String text : headerText) {
            TextView textView = new TextView(this);
            textView.setText(text);
            textView.setBackgroundColor(ContextCompat.getColor(this, R.color.purple));
            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
            textView.setTextSize(23);
            textView.setPadding(55, 20, 20, 20);
            headerRow.addView(textView);
        }
        tableLayout.addView(headerRow);

        // Add initial data rows
        for (String[] row : data) {
            TableRow dataRow = new TableRow(this);
            for (String text : row) {
                TextView textView = new TextView(this);
                textView.setText(text);
                textView.setTextColor(ContextCompat.getColor(this, R.color.purple));
                textView.setTextSize(21);
                textView.setPadding(55, 20, 20, 20);
                dataRow.addView(textView);
            }
            tableLayout.addView(dataRow);
        }

        // Add new data dynamically
        addDataToTable("Alice", "Smith", "30");
        addDataToTable("Bob", "Johnson", "35");
        addDataToTable("Joerel", "Belen", "21");

    }

    //Nav Bar
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //table
    private void addDataToTable(String firstName, String lastName, String age) {
        TableRow dataRow = new TableRow(this);
        String[] rowData = {firstName, lastName, age};
        for (String text : rowData) {
            TextView textView = new TextView(this);
            textView.setText(text);
            textView.setTextColor(ContextCompat.getColor(this, R.color.purple));
            textView.setTextSize(21);
            textView.setPadding(55, 20, 20, 20);
            dataRow.addView(textView);
        }
        tableLayout.addView(dataRow);
    }
}