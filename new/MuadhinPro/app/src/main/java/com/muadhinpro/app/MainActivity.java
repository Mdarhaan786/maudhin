package com.muadhinpro.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private TextView locationText;
    private TextView currentTimeText;
    private TextView nextPrayerText;
    private RecyclerView prayerTimesRecyclerView;
    private PrayerTimesAdapter prayerTimesAdapter;
    private AdhanManager adhanManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupBottomNavigation();
        requestLocationPermission();
        setupPrayerTimesRecyclerView();
        
        adhanManager = new AdhanManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        getCurrentLocation();
        startTimeUpdates();
    }

    private void initializeViews() {
        locationText = findViewById(R.id.locationText);
        currentTimeText = findViewById(R.id.currentTimeText);
        nextPrayerText = findViewById(R.id.nextPrayerText);
        prayerTimesRecyclerView = findViewById(R.id.prayerTimesRecyclerView);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            // Handle navigation item clicks
            switch (item.getItemId()) {
                case R.id.nav_prayers:
                    // Already on prayers screen
                    return true;
                case R.id.nav_qibla:
                    // Launch qibla activity
                    return true;
                case R.id.nav_settings:
                    // Launch settings activity
                    return true;
            }
            return false;
        });
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updatePrayerTimes(location);
                    }
                });
        }
    }

    private void updatePrayerTimes(Location location) {
        adhanManager.calculatePrayerTimes(location.getLatitude(), location.getLongitude());
        List<PrayerTime> prayerTimes = adhanManager.getTodayPrayerTimes();
        prayerTimesAdapter.updatePrayerTimes(prayerTimes);
        updateNextPrayer();
    }

    private void setupPrayerTimesRecyclerView() {
        prayerTimesAdapter = new PrayerTimesAdapter();
        prayerTimesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        prayerTimesRecyclerView.setAdapter(prayerTimesAdapter);
    }

    private void startTimeUpdates() {
        // Update current time every minute
        new Thread(() -> {
            while (!isFinishing()) {
                runOnUiThread(this::updateCurrentTime);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        String currentTime = String.format("%02d:%02d", 
            calendar.get(Calendar.HOUR_OF_DAY), 
            calendar.get(Calendar.MINUTE));
        currentTimeText.setText(currentTime);
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
}
