package com.example.capstone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class LandingPage extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_TIME_KEY = "isFirstTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

        // Check if this is the first time the app is being opened
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(FIRST_TIME_KEY, true);

        if (isFirstTime) {
            // Set the boolean value in SharedPreferences to false
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(FIRST_TIME_KEY, false);
            editor.apply();

            // Show the landing page
            setContentView(R.layout.landing_page);
        } else {
            // Navigate to the sign-in page
            startActivity(new Intent(this, SignIn.class));
            finish();
        }

        //pages
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new Page1Fragment());
        fragments.add(new Page2Fragment());
        fragments.add(new Page3Fragment());
        fragments.add(new Page4Fragment());

        ViewPager viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        viewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), fragments));
        tabLayout.setupWithViewPager(viewPager);

        // Set custom tab layout for each tab
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(R.layout.custom_tab);
                ImageView dot = tab.getCustomView().findViewById(R.id.dot);
                dot.setSelected(i == 0);
            }
        }

        // Add a listener to update the dots when a tab is selected
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ImageView dot = tab.getCustomView().findViewById(R.id.dot);
                dot.setSelected(true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ImageView dot = tab.getCustomView().findViewById(R.id.dot);
                dot.setSelected(false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
}
