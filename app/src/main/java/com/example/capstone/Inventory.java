package com.example.capstone;

import androidx.annotation.NonNull;
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
    private String[] headers = {"Item", "Stock Amount", "Price", "Edit"};
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference itemsRef = db.collection("items");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        tableLayout = findViewById(R.id.tableLayout);

        //nav drawer
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

        // Add Item button
        Button addItemButton = findViewById(R.id.add_item_button);
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Inventory.this, AddItem.class);
                startActivity(intent);
            }
        });

        //table

        //read Firestore
        mAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        itemsRef = db.collection("users").document(userId).collection("item");

        // Set up "Edit Item" header
        TableRow headerRow = new TableRow(this);
        TextView nameHeader = new TextView(this);
        TextView quantityHeader = new TextView(this);
        TextView priceHeader = new TextView(this);
        TextView editHeader = new TextView(this);

        nameHeader.setText("Name");
        quantityHeader.setText("Quantity");
        priceHeader.setText("Price");
        editHeader.setText("Edit Item");

        headerRow.addView(nameHeader);
        headerRow.addView(quantityHeader);
        headerRow.addView(priceHeader);
        headerRow.addView(editHeader);

        tableLayout.addView(headerRow, new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Load data from Firebase
        itemsRef.whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String itemName = document.getString("name");
                                String quantity = document.getString("quantity");
                                String price = document.getString("price");
                                String documentId = document.getId();

                                // Add data to table
                                TableRow dataRow = new TableRow(Inventory.this);

                                TextView nameView = new TextView(Inventory.this);
                                nameView.setText(itemName);
                                nameView.setTextSize(16);
                                nameView.setBackgroundColor(getResources().getColor(R.color.white));
                                nameView.setTextColor(getResources().getColor(R.color.light_purple));
                                nameView.setPadding(10, 10, 10, 10);
                                dataRow.addView(nameView);

                                TextView quantityView = new TextView(Inventory.this);
                                quantityView.setText(quantity);
                                quantityView.setTextSize(16);
                                quantityView.setBackgroundColor(getResources().getColor(R.color.white));
                                quantityView.setTextColor(getResources().getColor(R.color.light_purple));
                                quantityView.setPadding(10, 10, 10, 10);
                                dataRow.addView(quantityView);

                                TextView priceView = new TextView(Inventory.this);
                                priceView.setText(price);
                                priceView.setTextSize(16);
                                priceView.setBackgroundColor(getResources().getColor(R.color.white));
                                priceView.setTextColor(getResources().getColor(R.color.light_purple));
                                priceView.setPadding(10, 10, 10, 10);
                                dataRow.addView(priceView);

                                ImageButton editButton = new ImageButton(Inventory.this);
                                editButton.setImageResource(R.drawable.ic_edit);
                                editButton.setBackgroundColor(getResources().getColor(R.color.white));
                                editButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Inventory.this, EditItem.class);
                                        intent.putExtra("DOCUMENT_ID", documentId);
                                        startActivity(intent);
                                    }
                                });
                                dataRow.addView(editButton);

                                tableLayout.addView(dataRow);
                            }
                        } else {
                            Log.d("Inventory", "Error getting documents: ", task.getException());
                        }
                    }
                });
        }
    //Nav Bar
    @Override
    public void onBackPressed () {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}