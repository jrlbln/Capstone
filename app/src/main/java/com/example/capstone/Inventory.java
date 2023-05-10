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

        // Set up your navigation drawer code here

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
        quantityHeader.setText("Quantity");
        priceHeader.setText("Price");
        editHeader.setText("Edit");

        headerRow.addView(nameHeader);
        headerRow.addView(quantityHeader);
        headerRow.addView(priceHeader);
        headerRow.addView(editHeader);

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
                            TextView nameTextView = new TextView(this);
                            TextView quantityTextView = new TextView(this);
                            TextView priceTextView = new TextView(this);
                            ImageButton editButton = new ImageButton(this);

                            nameTextView.setText(name);
                            quantityTextView.setText(quantity);
                            priceTextView.setText(price);
                            editButton.setImageResource(R.drawable.ic_edit);
                            editButton.setBackgroundColor(Color.TRANSPARENT); // Optional: remove button background

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