package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Inventory extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TableLayout tableLayout;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference itemsRef = db.collection("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        tableLayout = findViewById(R.id.tableLayout);

        //nav button
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

        // Add Item button
        Button addItemButton = findViewById(R.id.add_item_button);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Inventory.this, AddItem.class);
                startActivity(intent);
            }
        });

        // Set up table headers
        TableRow headerRow = new TableRow(this);
        TextView nameHeader = new TextView(this);
        TextView quantityHeader = new TextView(this);
        TextView priceHeader = new TextView(this);
        TextView editHeader = new TextView(this);

        nameHeader.setText("Item Name");
        nameHeader.setTextColor(ContextCompat.getColor(this, R.color.white));
        nameHeader.setTextSize(21);
        nameHeader.setBackgroundColor(ContextCompat.getColor(this, R.color.purple));
        nameHeader.setPadding(50, 18, 18, 18);

        quantityHeader.setText("Quantity");
        quantityHeader.setTextColor(ContextCompat.getColor(this, R.color.white));
        quantityHeader.setTextSize(21);
        quantityHeader.setBackgroundColor(ContextCompat.getColor(this, R.color.purple));
        quantityHeader.setPadding(50, 18, 18, 18);

        priceHeader.setText("Price");
        priceHeader.setTextColor(ContextCompat.getColor(this, R.color.white));
        priceHeader.setTextSize(21);
        priceHeader.setBackgroundColor(ContextCompat.getColor(this, R.color.purple));
        priceHeader.setPadding(50, 18, 18, 18);

        editHeader.setText("Edit");
        editHeader.setTextColor(ContextCompat.getColor(this, R.color.white));
        editHeader.setTextSize(21);
        editHeader.setBackgroundColor(ContextCompat.getColor(this, R.color.purple));
        editHeader.setPadding(50, 18, 18, 18);


        headerRow.addView(nameHeader);
        headerRow.addView(quantityHeader);
        headerRow.addView(priceHeader);


        tableLayout.addView(headerRow, new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Fetch data from Firestore
        mAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        itemsRef = db.collection("users").document(userId).collection("item");

        // Populate table with data from Firestore
        itemsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String quantity = document.getString("quantity");
                            String price = document.getString("price");
                            String documentId = document.getId();

                            TableRow row = new TableRow(this);
                            row.setPadding(0, 4, 0, 4); // Set row padding
                            //row.setBackgroundColor(Color.LTGRAY); // Set row background color

                            TextView nameTextView = new TextView(this);
                            nameTextView.setText(name);
                            nameTextView.setTextColor(Color.BLACK);
                            nameTextView.setTextSize(19);
                            nameTextView.setPadding(110, 18, 18, 18);

                            TextView quantityTextView = new TextView(this);
                            quantityTextView.setText(quantity);
                            quantityTextView.setTextColor(Color.BLACK);
                            quantityTextView.setTextSize(19);
                            quantityTextView.setPadding(110, 18, 18, 18);

                            TextView priceTextView = new TextView(this);
                            priceTextView.setText(price);
                            priceTextView.setTextColor(Color.BLACK);
                            priceTextView.setTextSize(19);
                            priceTextView.setPadding(110, 18, 18, 18);

                            ImageButton editButton = new ImageButton(this);
                            editButton.setImageResource(R.drawable.edit);
                            editButton.setBackgroundColor(Color.TRANSPARENT);
                            editButton.setPadding(10, 18, 18, 18);

                            row.addView(nameTextView);
                            row.addView(quantityTextView);
                            row.addView(priceTextView);
                            row.addView(editButton);

                            editButton.setOnClickListener(v -> {
                                Intent intent = new Intent(Inventory.this, EditItem.class);
                                intent.putExtra("name", name);
                                intent.putExtra("quantity", quantity);
                                intent.putExtra("price", price);
                                intent.putExtra("documentId", documentId);
                                startActivity(intent);
                            });

                            tableLayout.addView(row, new TableLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        }
                    } else {
                        Log.d("ViewItems", "Error getting items", task.getException());
                    }
                });
    }

    // Navigation drawer onBackPressed() method
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}