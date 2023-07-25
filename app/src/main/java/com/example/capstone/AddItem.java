package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddItem extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemsRef;
    private FirebaseAuth mAuth;

    private EditText nameEditText;
    private EditText quantityEditText;
    private EditText priceEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_item);

        mAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        itemsRef = db.collection("users").document(userId).collection("item");

        nameEditText = findViewById(R.id.item_name);
        quantityEditText = findViewById(R.id.item_quantity);
        priceEditText = findViewById(R.id.item_price);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString().trim();
            String quantityStr = quantityEditText.getText().toString().trim();
            String priceStr = priceEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(quantityStr) || TextUtils.isEmpty(priceStr)) {
                Toast.makeText(AddItem.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert the quantity and price strings to doubles
            double quantity = Double.parseDouble(quantityStr);
            double price = Double.parseDouble(priceStr);

            if (quantity < 0 || price < 0) {
                Toast.makeText(AddItem.this, "Quantity and price cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            // Remove all non-alphanumeric characters from the name for comparison
            String lowercaseName = name.toLowerCase().replaceAll("[^A-Za-z0-9]", "");

            // Perform a query to check for similar items
            itemsRef.get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        boolean similarItemFound = false;
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String itemName = documentSnapshot.getString("name");
                            if (itemName != null) {
                                String lowercaseItemName = itemName.toLowerCase().replaceAll("[^A-Za-z0-9]", "");
                                if (lowercaseItemName.equals(lowercaseName)) {
                                    // Similar item found
                                    similarItemFound = true;
                                    break;
                                }
                            }
                        }

                        if (!similarItemFound) {
                            // No similar item found, proceed with adding the item
                            Map<String, Object> item = new HashMap<>();
                            item.put("name", name);
                            item.put("quantity", quantity);
                            item.put("price", price);

                            itemsRef.add(item)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(AddItem.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(AddItem.this, Inventory.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("AddItem", "Error adding item: ", e);
                                        Toast.makeText(AddItem.this, "Error adding item", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            // Similar item found, display an error message
                            Toast.makeText(AddItem.this, "A similar item already exists", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("AddItem", "Error checking for similar items: ", e);
                        Toast.makeText(AddItem.this, "Error checking for similar items", Toast.LENGTH_SHORT).show();
                    });
        });


    }
}