package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Sales extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private LineChart lineChart;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final List<SalesData> salesDataList = new ArrayList<>();
    private LineDataSet lineDataSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sales);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        lineChart = findViewById(R.id.line_chart);

        // Additional initialization for sales calculation and chart
        lineDataSet = new LineDataSet(new ArrayList<>(), "Sales");
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        loadSalesData();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        Button btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Handle menu item clicks here
                int id = menuItem.getItemId();
                if (id == R.id.home) {
                    // Start the HomeActivity
                    Intent intent = new Intent(Sales.this, Home.class);
                    startActivity(intent);
                } else if (id == R.id.inventory) {
                    // Start the InventoryActivity
                    Intent intent = new Intent(Sales.this, Inventory.class);
                    startActivity(intent);
                } else if (id == R.id.sales) {
                    // Start the SalesChartActivity
                    Intent intent = new Intent(Sales.this, Sales.class);
                    startActivity(intent);
                } else if (id == R.id.purchase) {
                    // Start the PurchaseListActivity
                    Intent intent = new Intent(Sales.this, Purchase.class);
                    startActivity(intent);
                } else if (id == R.id.logout) {
                    // Start the SignInActivity
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(Sales.this, SignIn.class);
                    intent.putExtra("logout", true);
                    startActivity(intent);
                    finish();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sales_menu, menu);

        MenuItem calculateItem = menu.findItem(R.id.calculate);
        calculateItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                calculateSales();
                return true;
            }
        });

        return true;
    }

    private void calculateSales() {
        String userId = mAuth.getCurrentUser().getUid();
        final DocumentReference userDocRef = db.collection("users").document(userId);
        final CollectionReference userItemsRef = db.collection("users").document(userId).collection("item");

        userItemsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                double totalSales = 0;

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String name = document.getString("name");
                    int quantity = document.getLong("quantity").intValue();
                    int prevQuantity = document.contains("prevQuantity") ? document.getLong("prevQuantity").intValue() : quantity;
                    double price = document.getDouble("price");

                    if (quantity < prevQuantity) {
                        int netChange = prevQuantity - quantity;
                        double sales = netChange * price;
                        totalSales += sales;
                    }

                    document.getReference().update("prevQuantity", quantity);
                }

                // Create a new SalesData object with the total sales and current timestamp
                final SalesData salesData = new SalesData(totalSales, System.currentTimeMillis());

                // Add the SalesData object to the salesDataList
                salesDataList.add(salesData);

                // Update the chart with the new sales data
                updateChart();

                // Add salesData to Firestore after updating the chart
                userDocRef.collection("sales_data").add(salesData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("Sales", "Sales data added to Firestore.");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Sales", "Error adding sales data to Firestore: ", e);
                            }
                        });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Sales", "Error getting documents: ", e);
            }
        });
    }

    private void updateChart() {
        List<Entry> entries = new ArrayList<>();

        for (SalesData data : salesDataList) {
            entries.add(new Entry(data.getTimestamp(), (float) data.getSales()));
        }

        if (lineDataSet != null) {
            lineDataSet.setValues(entries);
            lineDataSet.notifyDataSetChanged();
        } else {
            lineDataSet = new LineDataSet(entries, "Sales");
            lineDataSet.setColor(Color.BLUE);
            lineDataSet.setDrawValues(false);
            lineDataSet.setDrawCircles(true);
            lineDataSet.setCircleColor(Color.BLUE);
            lineDataSet.setCircleRadius(4f);
            lineDataSet.setDrawCircleHole(false);

            LineData lineData = new LineData(lineDataSet);
            lineChart.setData(lineData);
        }

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new TimestampAxisValueFormatter());

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public static class TimestampAxisValueFormatter extends ValueFormatter {

        private final SimpleDateFormat simpleDateFormat;

        public TimestampAxisValueFormatter() {
            simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault());
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return simpleDateFormat.format(new Date((long) value));
        }
    }

    public static class SalesData {
        private double sales;
        private long timestamp;

        public SalesData(double sales, long timestamp) {
            this.sales = sales;
            this.timestamp = timestamp;
        }

        public double getSales() {
            return sales;
        }

        public void setSales(double sales) {
            this.sales = sales;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    private void loadSalesData() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        final DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.collection("sales_data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            salesDataList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                double sales = document.getDouble("sales");
                                long timestamp = document.getLong("timestamp");

                                // Add the retrieved sales data to the salesDataList
                                salesDataList.add(new SalesData(sales, timestamp));
                            }

                            // Update the chart with the loaded sales data
                            updateChart();
                        } else {
                            Log.d("Sales", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}