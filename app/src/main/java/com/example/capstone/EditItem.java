package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class EditItem extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText editName;
    private EditText editQuantity;
    private EditText editPrice;
    private Button saveButton;
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);

        editName = findViewById(R.id.item_name);
        editQuantity = findViewById(R.id.item_quantity);
        editPrice = findViewById(R.id.item_price);
        saveButton = findViewById(R.id.save_button);

        // Get the data from the Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String quantity = intent.getStringExtra("quantity");
        String price = intent.getStringExtra("price");
        documentId = intent.getStringExtra("documentId");

        // Set the current values
        editName.setText(name);
        editQuantity.setText(quantity);
        editPrice.setText(price);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the new values
                String updatedName = editName.getText().toString();
                String updatedQuantity = editQuantity.getText().toString();
                String updatedPrice = editPrice.getText().toString();

                // Update the item in Firestore
                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                DocumentReference itemRef = db.collection("users").document(userId).collection("item").document(documentId);

                itemRef.update("name", updatedName, "quantity", updatedQuantity, "price", updatedPrice)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditItem.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                                // Navigate back to Inventory activity
                                Intent intent = new Intent(EditItem.this, Inventory.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditItem.this, "Error updating item", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}