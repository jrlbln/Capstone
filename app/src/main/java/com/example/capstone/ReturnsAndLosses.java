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

        // Get the data from the Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        double quantity = intent.getDoubleExtra("quantity", 0);
        double price = intent.getDoubleExtra("price", 0);
        documentId = intent.getStringExtra("documentId");

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the return and loss values
                String returnsStr = editReturns.getText().toString();
                String lossesStr = editLosses.getText().toString();

                // Set values to 0 if the fields are empty
                int returns = returnsStr.isEmpty() ? 0 : Integer.parseInt(returnsStr);
                int losses = lossesStr.isEmpty() ? 0 : Integer.parseInt(lossesStr);


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
                                Intent intent = new Intent(ReturnsAndLosses.this, EditItem.class);
                                intent.putExtra("name", name);
                                intent.putExtra("quantity", quantity);
                                intent.putExtra("price", price);
                                intent.putExtra("documentId", documentId);
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
