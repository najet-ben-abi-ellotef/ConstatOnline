package com.example.constatonline;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ConstatActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_LOCATION = 101;

    EditText etDate, etVeh1, etVeh2, etLat, etLon, etPlace;
    LinearLayout photoContainer;
    Button btnSave, btnPhoto, btnGPS;
    MapView mapView;
    ConstatDBHelper dbHelper;
    LocationManager locationManager;
    ArrayList<Bitmap> photoList = new ArrayList<>();

    Constat editConstat = null; // Pour savoir si on modifie

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_constat);

        dbHelper = new ConstatDBHelper(this);

        etDate = findViewById(R.id.etDate);
        etVeh1 = findViewById(R.id.etVehicule1);
        etVeh2 = findViewById(R.id.etVehicule2);
        etLat = findViewById(R.id.etLat);
        etLon = findViewById(R.id.etLon);
        etPlace = findViewById(R.id.etPlace);

        photoContainer = findViewById(R.id.photoContainer);
        mapView = findViewById(R.id.map);

        btnSave = findViewById(R.id.btnSave);
        btnPhoto = findViewById(R.id.btnPhoto);
        btnGPS = findViewById(R.id.btnMap);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // ---------------- DatePicker ----------------
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> showDatePicker());

        // ---------------- Ajouter photo ----------------
        btnPhoto.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            } else {
                openCamera();
            }
        });

        // ---------------- Récupérer GPS ----------------
        btnGPS.setOnClickListener(v -> getLocation());

        // ---------------- Initialiser la map ----------------
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // ---------------- Vérifier si on est en mode édition ----------------
        int editId = getIntent().getIntExtra("editId", -1);

        if (editId != -1) {
            editConstat = dbHelper.getConstat(editId);
            if (editConstat != null) {
                etDate.setText(editConstat.getDate());
                etVeh1.setText(editConstat.getVehicule1());
                etVeh2.setText(editConstat.getVehicule1());
                etLat.setText(String.valueOf(editConstat.getLatitude()));
                etLon.setText(String.valueOf(editConstat.getLongitude()));
                etPlace.setText(editConstat.getPlace());

                // Afficher les photos existantes
                ArrayList<byte[]> photos = editConstat.getPhotos();
                if (photos != null) {
                    for (byte[] b : photos) {
                        Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(b, 0, b.length);
                        ImageView iv = new ImageView(this);
                        iv.setImageBitmap(bmp);
                        iv.setAdjustViewBounds(true);
                        iv.setMaxHeight(400);
                        iv.setPadding(0, 8, 0, 8);
                        photoContainer.addView(iv);
                        photoList.add(bmp); // pour conserver et éventuellement remplacer
                    }
                }
            }
        }

        // ---------------- Enregistrer / Modifier constat ----------------
        btnSave.setOnClickListener(v -> saveConstat());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) ->
                        etDate.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1),
                year, month, day);
        dialog.show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            photoList.add(photo);

            ImageView iv = new ImageView(this);
            iv.setImageBitmap(photo);
            iv.setAdjustViewBounds(true);
            iv.setMaxHeight(400);
            iv.setPadding(0, 8, 0, 8);
            photoContainer.addView(iv);
        }
    }

    private void getLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                etLat.setText(String.valueOf(lat));
                etLon.setText(String.valueOf(lon));
                getPlaceName(lat, lon);

                // Afficher sur la map
                GeoPoint point = new GeoPoint(lat, lon);
                IMapController mapController = mapView.getController();
                mapController.setZoom(15.0);
                mapController.setCenter(point);

                Marker marker = new Marker(mapView);
                marker.setPosition(point);
                marker.setTitle(etPlace.getText().toString());
                mapView.getOverlays().clear();
                mapView.getOverlays().add(marker);
                mapView.invalidate();

                locationManager.removeUpdates(this);
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
    }

    private void getPlaceName(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (!addresses.isEmpty()) {
                etPlace.setText(addresses.get(0).getAddressLine(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConstat() {
        if (etDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Date obligatoire", Toast.LENGTH_SHORT).show();
            return;
        }

        String latStr = etLat.getText().toString().trim();
        String lonStr = etLon.getText().toString().trim();
        if (latStr.isEmpty() || lonStr.isEmpty()) {
            Toast.makeText(this, "Veuillez récupérer la localisation avant d'enregistrer", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<byte[]> photosBytes = new ArrayList<>();
        for (Bitmap bmp : photoList) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            photosBytes.add(stream.toByteArray());
        }

        String date = etDate.getText().toString().trim();
        String veh1 = etVeh1.getText().toString().trim();
        String veh2 = etVeh2.getText().toString().trim();
        double lat = Double.parseDouble(latStr);
        double lon = Double.parseDouble(lonStr);
        String place = etPlace.getText().toString().trim();

        if (editConstat != null) {
            // Mode modification
            Constat updated = new Constat(editConstat.getId(), date, veh1, veh2, photosBytes, lat, lon, place);
            dbHelper.updateConstat(updated);
            Toast.makeText(this, "Constat modifié ✔", Toast.LENGTH_SHORT).show();
        } else {
            // Mode ajout
            long id = dbHelper.addConstat(date, veh1, veh2, photosBytes, lat, lon, place);
            if (id != -1) {
                Toast.makeText(this, "Constat ajouté ✔", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
        if (requestCode == REQUEST_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }
}
