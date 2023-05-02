package com.example.capstone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class LandingPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_page);

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
