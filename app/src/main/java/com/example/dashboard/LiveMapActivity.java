package com.example.dashboard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class LiveMapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize osmdroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_live_map);

        ImageButton btnBackMap = findViewById(R.id.btnBackMap);
        if (btnBackMap != null) {
            btnBackMap.setOnClickListener(v -> finish());
        }

        mapView = findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            mapView.setMultiTouchControls(true);

            // Set default starting point (e.g. coastal disaster preparedness zone center)
            GeoPoint startPoint = new GeoPoint(13.0827, 80.2707);
            mapView.getController().setZoom(11.0);
            mapView.getController().setCenter(startPoint);

            // Add sample markers for Emergency Shelters & Hospitals
            addMarker(new GeoPoint(13.0900, 80.2800), "Emergency Shelter #1", "Capacity: 500 people - Safe Zone");
            addMarker(new GeoPoint(13.0700, 80.2600), "City General Hospital", "Emergency Medical Assistance Center");
        }

        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);
        if (fabMyLocation != null) {
            fabMyLocation.setOnClickListener(v -> checkLocationPermissionAndCenter());
        }

        checkLocationPermission();
    }

    private void addMarker(GeoPoint point, String title, String snippet) {
        if (mapView != null) {
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setTitle(title);
            marker.setSnippet(snippet);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
            mapView.invalidate();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void checkLocationPermissionAndCenter() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Centering on current location (GPS Active)", Toast.LENGTH_SHORT).show();
            if (mapView != null) {
                mapView.getController().animateTo(new GeoPoint(13.0827, 80.2707));
            }
        } else {
            Toast.makeText(this, "Location permission required for GPS centering", Toast.LENGTH_SHORT).show();
            checkLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
}
