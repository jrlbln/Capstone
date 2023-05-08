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

import com.google.android.material.internal.ViewUtils;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class Inventory extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TableLayout tableLayout;
    private String[] headers = {"Name", "Age", "Gender"};
    private List<String[]> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        tableLayout = findViewById(R.id.tableLayout);

        // Add some sample data
        data.add(new String[]{"John Doe", "25", "Male"});
        data.add(new String[]{"Jane Smith", "30", "Female"});
        data.add(new String[]{"Bob Johnson", "40", "Male"});
        data.add(new String[]{"Alice Brown", "22", "Female"});
        data.add(new String[]{"Tom Jones", "50", "Male"});
        data.add(new String[]{"Sue Davis", "28", "Female"});
        data.add(new String[]{"Sam Wilson", "35", "Male"});
        data.add(new String[]{"Eva Lee", "29", "Female"});
        data.add(new String[]{"Mike Smith", "45", "Male"});
        data.add(new String[]{"Lisa Chen", "27", "Female"});

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
                    Intent intent = new Intent(Inventory.this, Home.class);
                    startActivity(intent);
                } else if (id == R.id.inventory) {
                    // Start the InventoryActivity
                    Intent intent = new Intent(Inventory.this, Inventory.class);
                    startActivity(intent);
                } else if (id == R.id.sales) {
                    // Start the SalesChartActivity
                    Intent intent = new Intent(Inventory.this, Sales.class);
                    startActivity(intent);
                } else if (id == R.id.purchase) {
                    // Start the PurchaseListActivity
                    Intent intent = new Intent(Inventory.this, Purchase.class);
                    startActivity(intent);
                } else if (id == R.id.logout) {
                    // Start the PurchaseListActivity
                    Intent intent = new Intent(Inventory.this, SignIn.class);
                    startActivity(intent);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //table
        // Add the table header
        TableRow headerRow = new TableRow(this);
        for (String header : headers) {
            TextView textView = new TextView(this);
            textView.setText(header);
            textView.setTextSize(20);
            textView.setPadding(10, 10, 10, 10);
            textView.setBackgroundColor(getResources().getColor(R.color.white));
            textView.setTextColor(getResources().getColor(R.color.purple));
            headerRow.addView(textView);
        }
        tableLayout.addView(headerRow);

        // Add the table data
        int numRowsToShow = 5;
        for (int i = 0; i < Math.min(data.size(), numRowsToShow); i++) {
            TableRow dataRow = new TableRow(this);
            String[] row = data.get(i);
            for (String rowData : row) {
                TextView textView = new TextView(this);
                textView.setText(rowData);
                textView.setTextSize(16);
                textView.setBackgroundColor(getResources().getColor(R.color.white));
                textView.setTextColor(getResources().getColor(R.color.light_purple));
                textView.setPadding(10, 10, 10, 10);
                dataRow.addView(textView);
            }
            tableLayout.addView(dataRow);
        }

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
}