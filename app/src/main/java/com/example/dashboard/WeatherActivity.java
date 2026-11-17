package com.example.dashboard;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WeatherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        ImageButton btnRefreshWeather = findViewById(R.id.btnRefreshWeather);
        if (btnRefreshWeather != null) {
            btnRefreshWeather.setOnClickListener(v -> 
                Toast.makeText(this, "Refreshing weather telemetry (API offline)...", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
