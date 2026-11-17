package com.example.shelter;


import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener {

    // Colors
    final int DARK_BLUE = Color.rgb(18, 59, 112);
    final int BLUE = Color.rgb(36, 104, 172);
    final int LIGHT_BLUE = Color.rgb(225, 241, 255);
    final int BACKGROUND = Color.rgb(243, 248, 255);
    final int WHITE = Color.WHITE;

    LinearLayout mainLayout, filterLayout;
    EditText searchBar;
    RecyclerView recyclerView;
    TextView emptyMessage, shelterCount;

    Button filterAll, filterOpen, filterMedical, filterFood;

    ArrayList<Shelter> shelterList = new ArrayList<>();
    ArrayList<Shelter> filteredList = new ArrayList<>();

    ShelterAdapter adapter;

    String selectedFilter = "All";

    LocationManager locationManager;

    static final int LOCATION_REQUEST = 101;

    // =========================
    // SHELTER MODEL
    // =========================

    static class Shelter {

        String name, location, contact;

        double latitude, longitude;
        double distance = -1;

        boolean isOpen, medical, food;

        Shelter(String name, String location, String contact,
                double latitude, double longitude,
                boolean isOpen, boolean medical, boolean food) {

            this.name = name;
            this.location = location;
            this.contact = contact;

            this.latitude = latitude;
            this.longitude = longitude;

            this.isOpen = isOpen;
            this.medical = medical;
            this.food = food;
        }
    }

    // =========================
    // MAIN ACTIVITY
    // =========================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        createSampleData();

        createUI();

        setupRecyclerView();

        setupSearch();

        setupFilters();

        filterShelters();

        requestLocationPermission();
    }

    // =========================
    // SAMPLE DATA
    // =========================

    // Replace these entries with verified Firebase/API data later.
    // All entries below are fictional testing data.

    private void createSampleData() {

        shelterList.add(new Shelter(
                "Government Relief Shelter",
                "Puttur, Karnataka",
                "9000000001",
                12.7598, 75.2018,
                true, true, true
        ));

        shelterList.add(new Shelter(
                "Community Safety Centre",
                "Vittal, Karnataka",
                "9000000002",
                12.7650, 75.1930,
                true, false, true
        ));

        shelterList.add(new Shelter(
                "Emergency Care Shelter",
                "Mangalore, Karnataka",
                "9000000003",
                12.9141, 74.8560,
                false, true, true
        ));

        shelterList.add(new Shelter(
                "Town Relief Centre",
                "Bantwal, Karnataka",
                "9000000004",
                12.8900, 75.0340,
                true, true, false
        ));

        shelterList.add(new Shelter(
                "Public Disaster Shelter",
                "Sullia, Karnataka",
                "9000000005",
                12.5610, 75.3870,
                false, false, true
        ));
    }

    // =========================
    // UI CREATION
    // =========================

    private void createUI() {

        ScrollView scrollView = new ScrollView(this);

        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dp(16), dp(18), dp(16), dp(12));
        mainLayout.setBackgroundColor(BACKGROUND);

        scrollView.addView(mainLayout);

        setContentView(scrollView);

        // HEADER

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        mainLayout.addView(header);

        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextSize(36);
        back.setTextColor(DARK_BLUE);
        back.setGravity(Gravity.CENTER);
        back.setLayoutParams(new LinearLayout.LayoutParams(
                dp(42), dp(55)));

        header.addView(back);

        back.setOnClickListener(v -> finish());

        LinearLayout titleLayout = new LinearLayout(this);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        header.addView(titleLayout);

        TextView title = new TextView(this);
        title.setText("Emergency Shelters");
        title.setTextSize(23);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(DARK_BLUE);

        titleLayout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(
                "Find nearby safe shelters during emergencies.");
        subtitle.setTextSize(12);
        subtitle.setTextColor(Color.DKGRAY);

        titleLayout.addView(subtitle);

        // SEARCH BAR

        searchBar = new EditText(this);
        searchBar.setSingleLine(true);
        searchBar.setTextSize(14);
        searchBar.setPadding(dp(14), dp(8), dp(14), dp(8));
        searchBar.setHint("⌕  Search shelters by name or location");
        searchBar.setBackground(searchBackground());

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        -1, dp(52));

        searchParams.setMargins(0, dp(20), 0, dp(18));

        mainLayout.addView(searchBar, searchParams);

        // FILTER HEADING

        addHeading("Filters");

        // FILTER BUTTONS

        HorizontalScrollView horizontalScroll =
                new HorizontalScrollView(this);

        horizontalScroll.setHorizontalScrollBarEnabled(false);

        mainLayout.addView(horizontalScroll);

        filterLayout = new LinearLayout(this);
        filterLayout.setOrientation(LinearLayout.HORIZONTAL);

        horizontalScroll.addView(filterLayout);

        filterAll = createFilterButton("All");
        filterOpen = createFilterButton("Open");
        filterMedical = createFilterButton("Has Medical");
        filterFood = createFilterButton("Has Food");

        filterLayout.addView(filterAll);
        filterLayout.addView(filterOpen);
        filterLayout.addView(filterMedical);
        filterLayout.addView(filterFood);

        // SHELTER HEADING

        addHeading("Available Shelters");

        shelterCount = new TextView(this);
        shelterCount.setTextSize(12);
        shelterCount.setTextColor(Color.GRAY);

        LinearLayout.LayoutParams countParams =
                new LinearLayout.LayoutParams(-1, -2);

        countParams.setMargins(0, 0, 0, dp(10));

        mainLayout.addView(shelterCount, countParams);

        // RECYCLER VIEW

        recyclerView = new RecyclerView(this);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        recyclerView.setNestedScrollingEnabled(false);

        mainLayout.addView(recyclerView,
                new LinearLayout.LayoutParams(-1, -2));

        // EMPTY MESSAGE

        emptyMessage = new TextView(this);

        emptyMessage.setText(
                "No shelters found.\nTry changing your search or filters.");

        emptyMessage.setTextSize(15);
        emptyMessage.setTextColor(Color.GRAY);
        emptyMessage.setGravity(Gravity.CENTER);
        emptyMessage.setPadding(
                dp(15), dp(35), dp(15), dp(35));

        mainLayout.addView(emptyMessage);

    }

    // =========================
    // DESIGN HELPERS
    // =========================

    private int dp(float value) {

        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable roundedBackground(
            int color, int radius, int strokeColor) {

        GradientDrawable drawable = new GradientDrawable();

        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        if (strokeColor != 0) {
            drawable.setStroke(dp(1), strokeColor);
        }

        return drawable;
    }

    private GradientDrawable searchBackground() {

        return roundedBackground(
                WHITE, 14,
                Color.rgb(215, 229, 244));
    }

    private void addHeading(String text) {

        TextView heading = new TextView(this);

        heading.setText(text);
        heading.setTextSize(19);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextColor(DARK_BLUE);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);

        params.setMargins(0, dp(12), 0, dp(10));

        mainLayout.addView(heading, params);
    }

    private Button createFilterButton(String text) {

        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(11);
        button.setAllCaps(false);
        button.setPadding(dp(8), 0, dp(8), 0);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-2, dp(43));

        params.setMargins(0, 0, dp(7), 0);

        button.setLayoutParams(params);

        return button;
    }

    // =========================
    // RECYCLER VIEW
    // =========================

    private void setupRecyclerView() {

        adapter = new ShelterAdapter();

        recyclerView.setAdapter(adapter);
    }

    // =========================
    // SEARCH
    // =========================

    private void setupSearch() {

        searchBar.addTextChangedListener(
                new android.text.TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s, int start, int before, int count) {

                        filterShelters();
                    }

                    @Override
                    public void afterTextChanged(
                            android.text.Editable s) {
                    }
                });
    }

    // =========================
    // FILTERS
    // =========================

    private void setupFilters() {

        filterAll.setOnClickListener(v -> selectFilter("All"));

        filterOpen.setOnClickListener(v -> selectFilter("Open"));

        filterMedical.setOnClickListener(
                v -> selectFilter("Has Medical"));

        filterFood.setOnClickListener(
                v -> selectFilter("Has Food"));

        updateFilterAppearance();
    }

    private void selectFilter(String filter) {

        if (selectedFilter.equals(filter) &&
                !filter.equals("All")) {

            selectedFilter = "All";

        } else {

            selectedFilter = filter;
        }

        updateFilterAppearance();

        filterShelters();
    }

    private void updateFilterAppearance() {

        Button[] buttons = {
                filterAll, filterOpen,
                filterMedical, filterFood
        };

        String[] names = {
                "All", "Open",
                "Has Medical", "Has Food"
        };

        for (int i = 0; i < buttons.length; i++) {

            boolean selected =
                    selectedFilter.equals(names[i]);

            buttons[i].setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            selected ? DARK_BLUE : LIGHT_BLUE));

            buttons[i].setTextColor(
                    selected ? WHITE : DARK_BLUE);
        }
    }

    private void filterShelters() {

        filteredList.clear();

        String query = searchBar.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        for (Shelter shelter : shelterList) {

            boolean searchMatch =
                    shelter.name.toLowerCase(Locale.ROOT)
                            .contains(query)
                            ||
                            shelter.location.toLowerCase(Locale.ROOT)
                                    .contains(query);

            boolean filterMatch = true;

            switch (selectedFilter) {

                case "Open":
                    filterMatch = shelter.isOpen;
                    break;

                case "Has Medical":
                    filterMatch = shelter.medical;
                    break;

                case "Has Food":
                    filterMatch = shelter.food;
                    break;
            }

            if (searchMatch && filterMatch) {
                filteredList.add(shelter);
            }
        }

        adapter.notifyDataSetChanged();

        shelterCount.setText(
                filteredList.size() + " shelters found");

        if (filteredList.isEmpty()) {

            emptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        } else {

            emptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // =========================
    // RECYCLER VIEW ADAPTER
    // =========================

    class ShelterAdapter extends RecyclerView.Adapter<ShelterAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(
                @NonNull ViewGroup parent, int viewType) {

            LinearLayout card = new LinearLayout(MainActivity.this);

            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16), dp(16), dp(16), dp(16));

            card.setBackground(
                    roundedBackground(WHITE, 16,
                            Color.rgb(225, 235, 246)));

            card.setElevation(dp(3));

            RecyclerView.LayoutParams params =
                    new RecyclerView.LayoutParams(
                            -1, -2);

            params.setMargins(0, 0, 0, dp(15));

            card.setLayoutParams(params);

            TextView name = new TextView(MainActivity.this);
            TextView status = new TextView(MainActivity.this);
            TextView location = new TextView(MainActivity.this);
            TextView distance = new TextView(MainActivity.this);
            TextView contact = new TextView(MainActivity.this);
            TextView medical = new TextView(MainActivity.this);
            TextView food = new TextView(MainActivity.this);

            Button callButton = new Button(MainActivity.this);
            Button directionButton = new Button(MainActivity.this);

            name.setTextSize(18);
            name.setTypeface(null, Typeface.BOLD);
            name.setTextColor(DARK_BLUE);

            status.setTextSize(12);
            status.setTypeface(null, Typeface.BOLD);
            status.setPadding(dp(7), dp(5), dp(7), dp(5));

            card.addView(name);
            card.addView(status);

            addCardText(card, location);
            addCardText(card, distance);
            addCardText(card, contact);
            addCardText(card, medical);
            addCardText(card, food);

            callButton.setText("☎  Call");
            directionButton.setText("⌖  Get Directions");

            callButton.setAllCaps(false);
            directionButton.setAllCaps(false);

            callButton.setTextColor(WHITE);
            directionButton.setTextColor(WHITE);

            callButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(DARK_BLUE));

            directionButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(BLUE));

            LinearLayout buttons =
                    new LinearLayout(MainActivity.this);

            buttons.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams buttonParams =
                    new LinearLayout.LayoutParams(0, dp(48), 1);

            buttonParams.setMargins(dp(3), dp(10), dp(3), 0);

            buttons.addView(callButton, buttonParams);

            LinearLayout.LayoutParams directionParams =
                    new LinearLayout.LayoutParams(0, dp(48), 1);

            directionParams.setMargins(dp(3), dp(10), dp(3), 0);

            buttons.addView(directionButton, directionParams);

            card.addView(buttons);

            return new Holder(card, name, status,
                    location, distance, contact,
                    medical, food, callButton, directionButton);
        }

        private void addCardText(
                LinearLayout card, TextView text) {

            text.setTextSize(13);
            text.setTextColor(Color.rgb(75, 90, 110));

            text.setPadding(0, dp(6), 0, dp(6));

            card.addView(text);
        }

        @Override
        public void onBindViewHolder(
                @NonNull Holder holder, int position) {

            Shelter shelter = filteredList.get(position);

            holder.name.setText(shelter.name);

            holder.location.setText(
                    "📍 " + shelter.location);

            holder.distance.setText(
                    shelter.distance >= 0
                            ? String.format(Locale.getDefault(),
                            "Distance: %.2f km", shelter.distance)
                            : "Distance: Unavailable");

            holder.contact.setText(
                    "☎  " + shelter.contact);

            if (shelter.isOpen) {

                holder.status.setText("● OPEN");
                holder.status.setTextColor(
                        Color.rgb(0, 130, 65));

                holder.status.setBackground(
                        roundedBackground(
                                0xFFE0F5E9, 8, 0));

            } else {

                holder.status.setText("● CLOSED");
                holder.status.setTextColor(
                        Color.rgb(190, 35, 35));

                holder.status.setBackground(
                        roundedBackground(
                                0xFFFFE5E5, 8, 0));
            }

            holder.medical.setText(
                    shelter.medical
                            ? "✓ Medical Facility Available"
                            : "✗ Medical Facility Not Available");

            holder.food.setText(
                    shelter.food
                            ? "✓ Food Available"
                            : "✗ Food Not Available");

            holder.callButton.setOnClickListener(v -> {

                Intent intent = new Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse("tel:" + shelter.contact));

                startActivity(intent);
            });

            holder.directionButton.setOnClickListener(v -> {

                String destination =
                        shelter.latitude + "," + shelter.longitude;

                Uri uri = Uri.parse(
                        "https://www.google.com/maps/dir/?api=1"
                                + "&destination="
                                + Uri.encode(destination));

                Intent intent = new Intent(
                        Intent.ACTION_VIEW, uri);

                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        class Holder extends RecyclerView.ViewHolder {

            TextView name, status, location;
            TextView distance, contact, medical, food;

            Button callButton, directionButton;

            Holder(View itemView,
                   TextView name, TextView status,
                   TextView location, TextView distance,
                   TextView contact, TextView medical,
                   TextView food, Button callButton,
                   Button directionButton) {

                super(itemView);

                this.name = name;
                this.status = status;
                this.location = location;
                this.distance = distance;
                this.contact = contact;
                this.medical = medical;
                this.food = food;
                this.callButton = callButton;
                this.directionButton = directionButton;
            }
        }
    }

    // =========================
    // LOCATION PERMISSION
    // =========================

    private void requestLocationPermission() {

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                ||
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            startLocationUpdates();

        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_REQUEST);
        }
    }

    private void startLocationUpdates() {

        locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);

        if (locationManager == null) return;

        try {

            boolean fine = ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;

            String provider = fine
                    ? LocationManager.GPS_PROVIDER
                    : LocationManager.NETWORK_PROVIDER;

            if (!locationManager.isProviderEnabled(provider)) {
                Toast.makeText(this,
                        "Enable location to calculate distances",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Location lastLocation =
                    locationManager.getLastKnownLocation(provider);

            if (lastLocation != null) {
                updateDistances(lastLocation);
            }

            locationManager.requestLocationUpdates(
                    provider, 5000, 10, this);

        } catch (SecurityException e) {

            Toast.makeText(this,
                    "Location permission unavailable",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDistances(Location userLocation) {

        for (Shelter shelter : shelterList) {

            float[] results = new float[1];

            Location.distanceBetween(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    shelter.latitude,
                    shelter.longitude,
                    results);

            shelter.distance = results[0] / 1000.0;
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        updateDistances(location);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST) {

            boolean granted = false;

            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }

            if (granted) {
                startLocationUpdates();
            } else {
                Toast.makeText(this,
                        "Location denied. Distance unavailable.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPause() {

        super.onPause();

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        if (locationManager != null &&
                (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                        ||
                        ActivityCompat.checkSelfPermission(
                                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED)) {

            startLocationUpdates();
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }
}
