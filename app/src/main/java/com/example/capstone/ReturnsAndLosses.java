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

public class ReturnsAndLosses extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText editReturns;
    private EditText editLosses;
    private Button saveButton;
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.returns_and_losses);

        editReturns = findViewById(R.id.item_returns);
        editLosses = findViewById(R.id.item_losses);
        saveButton = findViewById(R.id.save_button);

        // Get the document ID from the Intent
        Intent intent = getIntent();
        documentId = intent.getStringExtra("documentId");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the return and loss values
                String returnsStr = editReturns.getText().toString();
                String lossesStr = editLosses.getText().toString();

                if (returnsStr.isEmpty() || lossesStr.isEmpty()) {
                    Toast.makeText(ReturnsAndLosses.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int returns = Integer.parseInt(returnsStr);
                int losses = Integer.parseInt(lossesStr);

                // Update the item in Firestore
                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                DocumentReference itemRef = db.collection("users").document(userId).collection("item").document(documentId);

                itemRef.update(
                                "returns", returns,
                                "losses", losses
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ReturnsAndLosses.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                                // Navigate back to Inventory activity
                                Intent intent = new Intent(ReturnsAndLosses.this, Inventory.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ReturnsAndLosses.this, "Error updating item", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
