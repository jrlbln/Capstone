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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Purchase extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TableLayout tableLayout;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference itemsRef;


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
                    // Start the SignInActivity
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(Purchase.this, SignIn.class);
                    intent.putExtra("logout", true);
                    startActivity(intent);
                    finish();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //table

        // Create header row
        TableRow headerRow = new TableRow(this);
        String[] headerText = {"Item Name", "Quantity", "Price"};
        for (String text : headerText) {
            TextView textView = new TextView(this);
            textView.setText(text);
            textView.setBackgroundColor(ContextCompat.getColor(this, R.color.purple));

            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
            textView.setTextSize(23);
            textView.setPadding(55, 18, 18, 18);
            headerRow.addView(textView);
        }
        tableLayout.addView(headerRow);

        // Read data from Firestore
        mAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        itemsRef = db.collection("users").document(userId).collection("item");

        itemsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            Double quantity = document.getDouble("quantity");
                            Double price = document.getDouble("price");


                            TableRow row = new TableRow(this);
                            row.setPadding(0, 4, 0, 4); // Set row padding
                            row.setBackgroundColor(Color.LTGRAY); // Set row background color

                            if (quantity < 5.0) {
                                row = new TableRow(this);

                                TextView nameTextView = new TextView(this);
                                nameTextView.setText(name);
                                nameTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
                                nameTextView.setTextSize(19);
                                nameTextView.setPadding(120, 8, 8, 8);

                                TextView quantityTextView = new TextView(this);
                                quantityTextView.setText(String.valueOf(quantity));
                                quantityTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
                                quantityTextView.setTextSize(19);
                                quantityTextView.setPadding(150, 8, 8, 8);

                                TextView priceTextView = new TextView(this);
                                priceTextView.setText(String.valueOf(price));
                                priceTextView.setTextColor(ContextCompat.getColor(this, R.color.black));
                                priceTextView.setTextSize(19);
                                priceTextView.setPadding(120, 8, 8, 8);

                                row.addView(nameTextView);
                                row.addView(quantityTextView);
                                row.addView(priceTextView);

                                tableLayout.addView(row, new TableLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            }
                        }
                    } else {
                        Log.d("ViewItems", "Error getting items", task.getException());
                    }
                });

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