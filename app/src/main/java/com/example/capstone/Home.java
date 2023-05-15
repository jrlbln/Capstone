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
import android.widget.Button;
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

public class Home extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TableLayout inventoryTableLayout;
    private TableLayout salesTableLayout;
    private TableLayout purchaseTableLayout;
    private String[] inventoryHeaders = {"Name", "Quantity", "Price"};
    private String[] salesHeaders = {"Date", "Sales"};
    private String[] purchaseHeaders = {"Item"};
    private List<String[]> inventoryData = new ArrayList<>();
    private List<String[]> salesData = new ArrayList<>();
    private List<String[]> purchaseData = new ArrayList<>();

    private LineChart lineChart;
    private final List<Sales.SalesData> salesDataList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

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

        // Add some sample data for inventory table
        inventoryData.add(new String[]{"iPhone", "10", "$1000"});
        inventoryData.add(new String[]{"iPad", "20", "$800"});
        inventoryData.add(new String[]{"MacBook Pro", "5", "$2000"});
        inventoryData.add(new String[]{"Apple Watch", "15", "$400"});
        inventoryData.add(new String[]{"AirPods Pro", "30", "$250"});
        inventoryData.add(new String[]{"iPhone", "10", "$1000"});
        inventoryData.add(new String[]{"iPad", "20", "$800"});
        inventoryData.add(new String[]{"MacBook Pro", "5", "$2000"});
        inventoryData.add(new String[]{"Apple Watch", "15", "$400"});
        inventoryData.add(new String[]{"AirPods Pro", "30", "$250"});
        inventoryData.add(new String[]{"iPhone", "10", "$1000"});
        inventoryData.add(new String[]{"iPad", "20", "$800"});
        inventoryData.add(new String[]{"MacBook Pro", "5", "$2000"});
        inventoryData.add(new String[]{"Apple Watch", "15", "$400"});
        inventoryData.add(new String[]{"AirPods Pro", "30", "$250"});

        // Add some sample data for sales table
        salesData.add(new String[]{"January", "$5000"});
        salesData.add(new String[]{"February", "$3000"});
        salesData.add(new String[]{"March", "$10000"});
        salesData.add(new String[]{"April", "$2000"});
        salesData.add(new String[]{"May", "$7500"});
        salesData.add(new String[]{"June", "$5000"});
        salesData.add(new String[]{"July", "$3000"});
        salesData.add(new String[]{"August", "$10000"});
        salesData.add(new String[]{"September", "$2000"});
        salesData.add(new String[]{"October", "$7500"});

        // Add some sample data for purchase table
        purchaseData.add(new String[]{"iPhone"});
        purchaseData.add(new String[]{"iPad"});
        purchaseData.add(new String[]{"MacBook Pro"});
        purchaseData.add(new String[]{"Apple Watch"});
        purchaseData.add(new String[]{"AirPods Pro"});
        purchaseData.add(new String[]{"iPhone"});
        purchaseData.add(new String[]{"iPad"});
        purchaseData.add(new String[]{"MacBook Pro"});
        purchaseData.add(new String[]{"Apple Watch"});
        purchaseData.add(new String[]{"AirPods Pro"});

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

        // Add the table Inventory header
        TableRow headerRow1 = new TableRow(this);
        for (String header : inventoryHeaders) {
            TextView textView = new TextView(this);
            textView.setText(header);
            textView.setTextSize(16);
            textView.setPadding(10, 10, 10, 10);
            textView.setBackgroundColor(getResources().getColor(R.color.white));
            textView.setTextColor(getResources().getColor(R.color.purple));
            headerRow1.addView(textView);
        }
        inventoryTableLayout.addView(headerRow1);

        // Add the table Inventory data
        int numRowsToShow1 = 15;
        for (int i = 0; i < Math.min(inventoryData.size(), numRowsToShow1); i++) {
            TableRow dataRow = new TableRow(this);
            String[] row = inventoryData.get(i);
            for (String rowData : row) {
                TextView textView = new TextView(this);
                textView.setText(rowData);
                textView.setTextSize(16);
                textView.setBackgroundColor(getResources().getColor(R.color.white));
                textView.setTextColor(getResources().getColor(R.color.light_purple));
                textView.setPadding(10, 10, 10, 10);
                dataRow.addView(textView);
            }
            inventoryTableLayout.addView(dataRow);
        }

        // Add the table Sales header
        TableRow headerRow2 = new TableRow(this);
        for (String header : salesHeaders) {
            TextView textView = new TextView(this);
            textView.setText(header);
            textView.setTextSize(14);
            textView.setPadding(10, 10, 10, 10);
            textView.setBackgroundColor(getResources().getColor(R.color.white));
            textView.setTextColor(getResources().getColor(R.color.purple));
            headerRow2.addView(textView);
        }
        salesTableLayout.addView(headerRow2);

        // Add the table Sales data
        int numRowsToShow2 = 15;
        for (int i = 0; i < Math.min(salesData.size(), numRowsToShow2); i++) {
            TableRow dataRow = new TableRow(this);
            String[] row = salesData.get(i);
            for (String rowData : row) {
                TextView textView = new TextView(this);
                textView.setText(rowData);
                textView.setTextSize(11);
                textView.setBackgroundColor(getResources().getColor(R.color.white));
                textView.setTextColor(getResources().getColor(R.color.light_purple));
                textView.setPadding(10, 10, 10, 10);
                dataRow.addView(textView);
            }
            salesTableLayout.addView(dataRow);
        }

        // Add the table Purchase header
        TableRow headerRow3 = new TableRow(this);
        for (String header : purchaseHeaders) {
            TextView textView = new TextView(this);
            textView.setText(header);
            textView.setTextSize(14);
            textView.setPadding(5, 5, 5, 5);
            textView.setBackgroundColor(getResources().getColor(R.color.white));
            textView.setTextColor(getResources().getColor(R.color.purple));
            headerRow3.addView(textView);
        }
        purchaseTableLayout.addView(headerRow3);

        // Add the table Purchase data
        int numRowsToShow = 10;
        for (int i = 0; i < Math.min(purchaseData.size(), numRowsToShow); i++) {
            TableRow dataRow = new TableRow(this);
            String[] row = purchaseData.get(i);
            for (String rowData : row) {
                TextView textView = new TextView(this);
                textView.setText(rowData);
                textView.setTextSize(11);
                textView.setBackgroundColor(getResources().getColor(R.color.white));
                textView.setTextColor(getResources().getColor(R.color.light_purple));
                textView.setPadding(5, 5, 5, 5);
                dataRow.addView(textView);
            }
            purchaseTableLayout.addView(dataRow);
        }


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

        for (int i = 0; i < salesDataList.size(); i++) {
            Sales.SalesData salesData = salesDataList.get(i);
            float xAxisValue = i;
            float yAxisValue = (float) salesData.getSales();
            Entry chartEntry = new Entry(xAxisValue, yAxisValue);
            chartEntries.add(chartEntry);
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
            formatter = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            long timestamp = (long) value;
            return formatter.format(new Date(timestamp * 1000)); // Convert back to milliseconds
        }
    }

}