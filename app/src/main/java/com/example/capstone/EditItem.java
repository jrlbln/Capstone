package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EditItem extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText editName;
    private EditText editQuantity;
    private EditText editPrice;
    private EditText editAddQuantity;
    private Button saveButton;
    private Button deleteButton;
    private Button returnsAndLossesButton;
    private String documentId;
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item);

        editName = findViewById(R.id.item_name);
        editQuantity = findViewById(R.id.item_quantity);
        editPrice = findViewById(R.id.item_price);
        editAddQuantity = findViewById(R.id.add_quantity);
        saveButton = findViewById(R.id.save_button);
        returnsAndLossesButton = findViewById(R.id.returns_and_losses_button);
        deleteButton = findViewById(R.id.delete_button);

        // Get the data from the Intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        double quantity = intent.getDoubleExtra("quantity", 0);
        double price = intent.getDoubleExtra("price", 0);
        documentId = intent.getStringExtra("documentId");

        // Set the current values
        editName.setText(name);
        editQuantity.setText(String.valueOf(quantity));
        editPrice.setText(String.valueOf(price));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the new values
                String updatedName = editName.getText().toString();
                String updatedQuantityStr = editQuantity.getText().toString();
                String updatedAddQuantityStr = editAddQuantity.getText().toString();
                String updatedPriceStr = editPrice.getText().toString();

                if (updatedQuantityStr.isEmpty() || updatedPriceStr.isEmpty()) {
                    Toast.makeText(EditItem.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                double updatedQuantity = Double.parseDouble(updatedQuantityStr);
                double updatedAddQuantity = updatedAddQuantityStr.isEmpty() ? 0 : Double.parseDouble(updatedAddQuantityStr);
                double updatedPrice = Double.parseDouble(updatedPriceStr);

                if(updatedQuantity < 0 || updatedAddQuantity < 0 || updatedPrice < 0) {
                    Toast.makeText(EditItem.this, "Negative values are not allowed", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update the item in Firestore
                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                DocumentReference itemRef = db.collection("users").document(userId).collection("item").document(documentId);

                // Update the item with prevQuantity field
                itemRef.update(
                                "name", updatedName,
                                "quantity", updatedQuantity + updatedAddQuantity,
                                "price", updatedPrice,
                                "restockedQuantity", updatedAddQuantity,
                                "prevQuantity", quantity
                        )
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditItem.this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                                double finalQuantity = updatedQuantity + updatedAddQuantity;
                                if (finalQuantity > quantity) {
                                    Toast.makeText(EditItem.this, "Cannot consume more items than exist", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (updatedQuantity < quantity) {
                                    double quantityToConsume = quantity - updatedQuantity;
                                    consumeItemsFromBatches(itemRef, quantityToConsume);
                                }

                                // If quantity was increased, add a new batch
                                if (updatedAddQuantity > 0) {
                                    addBatchToItem(itemRef, updatedAddQuantity);
                                }

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

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditItem.this);
                builder.setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                DocumentReference itemRef = db.collection("users").document(userId).collection("item").document(documentId);

                                itemRef.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(EditItem.this, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                                                // Navigate back to Inventory activity
                                                Intent intent = new Intent(EditItem.this, Inventory.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(EditItem.this, "Error deleting item", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog, do nothing
                                dialog.cancel();
                            }
                        });
                // Create the AlertDialog object and display it
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        returnsAndLossesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the ReturnsAndLosses activity
                Intent intent = new Intent(EditItem.this, ReturnsAndLosses.class);
                // Pass the documentId to the ReturnsAndLosses activity
                intent.putExtra("documentId", documentId);
                intent.putExtra("name", name);
                intent.putExtra("quantity", quantity);
                intent.putExtra("price", price);
                startActivity(intent);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence notifname = "LowStockChannel";
            String description = "Channel for Low stock notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyLowStock", notifname, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showLowStockNotification(String itemName) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
                == PackageManager.PERMISSION_GRANTED) {
            // Create a vibration pattern
            long[] pattern = {0, 500, 200, 500}; // Vibrate for 500ms, pause for 200ms, vibrate for 500ms

            // Build the notification with vibration
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notifyLowStock")
                    .setSmallIcon(R.drawable.ic_notification) // replace with your own icon
                    .setContentTitle("Low Stock Alert")
                    .setContentText(itemName + " is running low on stock.")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setVibrate(pattern); // Set the vibration pattern

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } else {
            // Handle the case where the permission is not granted
            // You can show a message to the user or request the permission here
            // For simplicity, I'm showing a Toast message in this example
            Toast.makeText(this, "Vibration permission not granted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void consumeItemsFromBatches(DocumentReference itemRef, double quantity) {
        double[] quantityToConsume = {quantity};  // Create a mutable container for quantityToConsume

        itemRef.collection("batches").orderBy("timestamp").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> batchesList = queryDocumentSnapshots.getDocuments();
                db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        for (DocumentSnapshot document : batchesList) {
                            double batchQuantity = document.getDouble("quantity");
                            DocumentReference batchRef = document.getReference();

                            if (quantityToConsume[0] >= batchQuantity) {
                                // Consume the entire batch and delete it
                                quantityToConsume[0] -= batchQuantity;
                                transaction.delete(batchRef);
                            } else {
                                // Consume part of the batch and update its quantity
                                double newBatchQuantity = batchQuantity - quantityToConsume[0];
                                transaction.update(batchRef, "quantity", newBatchQuantity);
                                break;
                            }
                        }

                        return null;
                    }
                });
            }
        });
    }

    private void addBatchToItem(DocumentReference itemRef, double quantityToAdd) {
        // Create a new batch document for the added items
        Map<String, Object> batch = new HashMap<>();
        batch.put("quantity", quantityToAdd);
        batch.put("timestamp", FieldValue.serverTimestamp());

        // Add the batch to the "batches" subcollection of the item
        itemRef.collection("batches").add(batch)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(EditItem.this, "Batch added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditItem.this, "Error adding batch", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}