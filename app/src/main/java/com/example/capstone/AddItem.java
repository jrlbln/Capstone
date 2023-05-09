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
        itemsRef = db.collection("items").document(userId).collection("item");

        // Check if "item" collection exists and create it if it doesn't
        itemsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().size() == 0) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("name", "");
                            item.put("quantity", "");
                            item.put("price", "");

                            itemsRef.add(item)
                                    .addOnSuccessListener(documentReference -> Log.d("AddItem", "Item collection created"))
                                    .addOnFailureListener(e -> Log.d("AddItem", "Error creating item collection"));
                        }
                    } else {
                        Log.d("AddItem", "Error getting item collection", task.getException());
                    }
                });

        nameEditText = findViewById(R.id.item_name);
        quantityEditText = findViewById(R.id.item_quantity);
        priceEditText = findViewById(R.id.item_price);
        saveButton = findViewById(R.id.save_button);

        saveButton.setOnClickListener(view -> {
            String name = nameEditText.getText().toString().trim();
            String quantity = quantityEditText.getText().toString().trim();
            String price = priceEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(quantity) || TextUtils.isEmpty(price)) {
                Toast.makeText(AddItem.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new item document with auto-generated ID
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
        });
    }
}
