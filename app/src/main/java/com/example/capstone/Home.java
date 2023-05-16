package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Home extends AppCompatActivity {

    private TableRow salesHeaderRow;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TableLayout inventoryTableLayout;
    private TableLayout salesTableLayout;
    private TableLayout purchaseTableLayout;
    private String[] inventoryHeaders = {"Item Name", "Quantity", "Price"};
    private String[] salesHeaders = {"Date", "Sales"};
    private String[] purchaseHeaders = {"Item Name"};
    private List<String[]> inventoryData = new ArrayList<>();
    private List<String[]> salesData = new ArrayList<>();
    private List<String[]> purchaseData = new ArrayList<>();

    private LineChart lineChart;
    private final List<Sales.SalesData> salesDataList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        inventoryTableLayout = findViewById(R.id.inventory_table);
        salesTableLayout = findViewById(R.id.sales_list);
        purchaseTableLayout = findViewById(R.id.purchase_list);
        lineChart = findViewById(R.id.line_chart);

        // Additional initialization for sales calculation and chart
        LineDataSet lineDataSet = new LineDataSet(new ArrayList<>(), "Sales");
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DateFormatter()); // Apply the DateFormatter to the X-axis

        loadSalesData();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //Menu Button
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
                    Intent intent = new Intent(Home.this, Home.class);
                    startActivity(intent);
                } else if (id == R.id.inventory) {
                    // Start the InventoryActivity
                    Intent intent = new Intent(Home.this, Inventory.class);
                    startActivity(intent);
                } else if (id == R.id.sales) {
                    // Start the SalesChartActivity
                    Intent intent = new Intent(Home.this, Sales.class);
                    startActivity(intent);
                } else if (id == R.id.purchase) {
                    // Start the PurchaseListActivity
                    Intent intent = new Intent(Home.this, Purchase.class);
                    startActivity(intent);
                } else if (id == R.id.logout) {
                    // Start the SignInActivity
                    SignIn signInInstance = SignIn.getInstance();
                    if (signInInstance != null) {
                        signInInstance.logOut();
                    } else {
                        FirebaseAuth.getInstance().signOut();
                    }
                    Intent intent = new Intent(Home.this, SignIn.class);
                    intent.putExtra("logout", true);
                    startActivity(intent);
                    finish();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Set up table headers for inventoryTableLayout
        TableRow inventoryHeaderRow = new TableRow(this);
        for (String header : inventoryHeaders) {
            TextView headerTextView = new TextView(this);
            headerTextView.setText(header);
            headerTextView.setTextColor(Color.WHITE);
            headerTextView.setTextSize(21);
            headerTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.purple));
            headerTextView.setPadding(50, 18, 18, 18);

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 2); // Add margin at the bottom of each header row

            inventoryHeaderRow.addView(headerTextView, params);
        }
        inventoryTableLayout.addView(inventoryHeaderRow);

        // Set up table headers for purchaseTableLayout
        TableRow purchaseHeaderRow = new TableRow(this);
        for (String header : purchaseHeaders) {
            TextView headerTextView = new TextView(this);
            headerTextView.setText(header);
            headerTextView.setTextColor(Color.WHITE);
            headerTextView.setTextSize(21);
            headerTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.purple));
            headerTextView.setPadding(50, 18, 18, 18);

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 2); // Add margin at the bottom of each header row

            purchaseHeaderRow.addView(headerTextView, params);
        }
        purchaseTableLayout.addView(purchaseHeaderRow);

        // Set up table headers for salesTableLayout
        salesHeaderRow = new TableRow(this);
        for (String header : salesHeaders) {
            TextView headerTextView = new TextView(this);
            headerTextView.setText(header);
            headerTextView.setTextColor(Color.WHITE);
            headerTextView.setTextSize(21);
            headerTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.purple));
            headerTextView.setPadding(50, 18, 18, 18);

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 2); // Add margin at the bottom of each header row

            salesHeaderRow.addView(headerTextView, params);
        }
        salesTableLayout.addView(salesHeaderRow);

        // Fetch data from Firestore and populate tables
        fetchDataFromFirestore();
    }

    // Method to fetch data from Firestore and populate tables
    private void fetchDataFromFirestore() {
        mAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        CollectionReference itemsRef = db.collection("users").document(userId).collection("item");

        itemsRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            double quantity = document.getDouble("quantity");
                            double price = document.getDouble("price");

                            // Create and add row to inventoryTableLayout
                            TableRow inventoryRow = new TableRow(this);

                            TextView nameTextView = new TextView(this);
                            nameTextView.setText(name);
                            nameTextView.setText(name);
                            nameTextView.setTextColor(Color.BLACK);
                            nameTextView.setTextSize(19);
                            nameTextView.setPadding(110, 18, 18, 18);

                            TextView quantityTextView = new TextView(this);
                            quantityTextView.setText(String.valueOf(quantity));
                            quantityTextView.setTextColor(Color.BLACK);
                            quantityTextView.setTextSize(19);
                            quantityTextView.setPadding(110, 18, 18, 18);


                            TextView priceTextView = new TextView(this);
                            priceTextView.setText(String.valueOf(price));
                            priceTextView.setTextColor(Color.BLACK);
                            priceTextView.setTextSize(19);
                            priceTextView.setPadding(110, 18, 18, 18);

                            inventoryRow.addView(nameTextView);
                            inventoryRow.addView(quantityTextView);
                            inventoryRow.addView(priceTextView);

                            inventoryTableLayout.addView(inventoryRow);

                            // Check the quantity condition for purchaseTableLayout
                            if (quantity <= 5) {
                                // Create and add row to purchaseTableLayout
                                TableRow purchaseRow = new TableRow(this);

                                TextView purchaseNameTextView = new TextView(this);
                                purchaseNameTextView.setText(name);
                                purchaseNameTextView.setText(name);
                                purchaseNameTextView.setTextColor(Color.BLACK);
                                purchaseNameTextView.setTextSize(19);
                                purchaseNameTextView.setPadding(110, 18, 18, 18);

                                purchaseRow.addView(purchaseNameTextView);

                                purchaseTableLayout.addView(purchaseRow);
                            }
                        }
                    } else {
                        Log.d("ViewItems", "Error getting items", task.getException());
                    }
                });
    }
    //exit drawer
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadSalesData() {
        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference salesDataRef = db.collection("users").document(userId).collection("sales_data");

        salesDataRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                salesDataList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    double sales = document.getDouble("sales");
                    long timestamp = document.getLong("timestamp");

                    // Create a SalesData object with the retrieved data
                    Sales.SalesData salesData = new Sales.SalesData(sales, timestamp);

                    // Add the SalesData object to the salesDataList
                    salesDataList.add(salesData);
                }

                // Update the LineChart with the new data
                updateLineChart();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Sales", "Error loading sales data from Firestore: ", e);
            }
        });
    }

    private void updateLineChart() {
        List<Entry> chartEntries = new ArrayList<>();

        // Sort the salesDataList by timestamp
        Collections.sort(salesDataList, new Comparator<Sales.SalesData>() {
            @Override
            public int compare(Sales.SalesData s1, Sales.SalesData s2) {
                return Long.compare(s1.getTimestamp(), s2.getTimestamp());
            }
        });

        // Clear the table (except headers) and prepare to add new rows
        salesTableLayout.removeViews(1, salesTableLayout.getChildCount() - 1);

        for (int i = 0; i < salesDataList.size(); i++) {
            Sales.SalesData salesData = salesDataList.get(i);
            float xAxisValue = i;
            float yAxisValue = (float) salesData.getSales();
            Entry chartEntry = new Entry(xAxisValue, yAxisValue);
            chartEntries.add(chartEntry);

            // Create new TableRow for each SalesData
            TableRow salesDataRow = new TableRow(this);

            // Format the timestamp
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
            String formattedDate = formatter.format(new Date(salesData.getTimestamp() * 1000));

            TextView dateTextView = new TextView(this);
            dateTextView.setText(formattedDate);
            dateTextView.setTextColor(Color.BLACK);
            dateTextView.setTextSize(19);
            dateTextView.setPadding(110, 18, 18, 18);

            TextView salesTextView = new TextView(this);
            salesTextView.setText(String.valueOf(salesData.getSales()));
            salesTextView.setTextColor(Color.BLACK);
            salesTextView.setTextSize(19);
            salesTextView.setPadding(110, 18, 18, 18);

            // Add TextViews to the TableRow
            salesDataRow.addView(dateTextView);
            salesDataRow.addView(salesTextView);

            // Add TableRow to the TableLayout
            salesTableLayout.addView(salesDataRow);
        }

        LineDataSet lineDataSet = new LineDataSet(chartEntries, "My Data Set");
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setValueTextColor(Color.RED);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);

        YAxis yAxisRight = lineChart.getAxisRight();
        yAxisRight.setEnabled(false);

        lineChart.invalidate(); // Refresh the chart
    }

    public class SalesData {
        private double sales;
        private long timestamp;

        public SalesData() {
        }

        public SalesData(double sales, long timestamp) {
            this.sales = sales;
            this.timestamp = timestamp;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public double getSales() {
            return sales;
        }
    }

    public class DateFormatter extends ValueFormatter implements IAxisValueFormatter {
        private SimpleDateFormat formatter;

        public DateFormatter() {
            formatter = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long timestamp = (long) value;
            return formatter.format(new Date(timestamp * 1000)); // Convert back to milliseconds
        }
    }

}