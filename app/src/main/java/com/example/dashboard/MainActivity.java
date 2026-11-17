package com.example.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Header Actions
        ImageButton btnNotification = findViewById(R.id.btnNotification);
        ImageButton btnProfile = findViewById(R.id.btnProfile);

        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> 
                Toast.makeText(this, "Notifications: No new alerts", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> 
                Toast.makeText(this, "Opening Profile & Settings", Toast.LENGTH_SHORT).show()
            );
        }

        // Cyclone Alert View Details -> Opens Weather & Alerts Page
        MaterialButton btnViewAlertDetails = findViewById(R.id.btnViewAlertDetails);
        if (btnViewAlertDetails != null) {
            btnViewAlertDetails.setOnClickListener(v -> {
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
            });
        }

        // Quick Access Cards Navigation
        setupCardNavigation(R.id.cardShelter, LiveMapActivity.class, "Opening Nearest Evacuation Shelters");
        setupCardNavigation(R.id.cardLiveMap, LiveMapActivity.class, "Opening Live Cyclone & Radar Map");
        setupCardNavigation(R.id.cardSafeRoutes, LiveMapActivity.class, "Opening Safe Evacuation Routes");
        setupCardNavigation(R.id.cardMedicalHelp, LiveMapActivity.class, "Opening Emergency Medical Help");

        setupQuickAccessCard(R.id.cardAiAssistance, "Opening CycloneSafe AI Assistant");
        setupQuickAccessCard(R.id.cardFamilySafety, "Opening Family Safety & Check-ins");
        setupQuickAccessCard(R.id.cardEmergencyContacts, "Opening Emergency Contacts Directory");
        setupQuickAccessCard(R.id.cardFirstAidGuide, "Opening First Aid & Survival Guide");

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_alerts) {
                    startActivity(new Intent(this, WeatherActivity.class));
                    return true;
                } else if (id == R.id.nav_map) {
                    startActivity(new Intent(this, LiveMapActivity.class));
                    return true;
                } else if (id == R.id.nav_profile) {
                    Toast.makeText(this, "Profile page (Placeholder)", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }
    }

    private void setupCardNavigation(int cardId, Class<?> targetActivityClass, String message) {
        MaterialCardView card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, targetActivityClass);
                startActivity(intent);
            });
        }
    }

    private void setupQuickAccessCard(int cardId, String message) {
        MaterialCardView card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> 
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            );
        }
    }
}
