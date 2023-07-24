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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddItem extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference itemsRef;
    private DocumentReference itemNumberRef;
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
        itemNumberRef = db.collection("users").document(userId).collection("meta").document("itemNumber");

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
                Toast.makeText(AddItem.this, "Negative values are not allowed", Toast.LENGTH_SHORT).show();
                return;
            }

            itemNumberRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long itemNumber = document.getLong("nextItemNumber");
                        // Create a new item document with the item number
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", name);
                        item.put("quantity", quantity);
                        item.put("price", price);
                        item.put("itemNumber", itemNumber);

                        itemsRef.add(item)
                                .addOnSuccessListener(documentReference -> {
                                    // Increment the item number
                                    itemNumberRef.update("nextItemNumber", itemNumber + 1);
                                    // Create a new batch document for the added items
                                    Map<String, Object> batch = new HashMap<>();
                                    batch.put("quantity", quantity);
                                    batch.put("timestamp", FieldValue.serverTimestamp());

                                    // Add the batch to the "batches" subcollection of the item
                                    documentReference.collection("batches").add(batch)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(AddItem.this, "Item added successfully", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(AddItem.this, Inventory.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.d("AddItem", "Error adding batch: ", e);
                                                Toast.makeText(AddItem.this, "Error adding batch", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.d("AddItem", "Error adding item: ", e);
                                    Toast.makeText(AddItem.this, "Error adding item", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.d("AddItem", "No such document");
                    }
                } else {
                    Log.d("AddItem", "get failed with ", task.getException());
                }
            });
        });
    }
}

