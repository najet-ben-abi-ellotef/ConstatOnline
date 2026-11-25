package com.example.constatonline;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailConstatActivity extends AppCompatActivity {

    TextView tvDate, tvVeh1, tvVeh2, tvLat, tvLon, tvPlace;
    LinearLayout photoContainer;
    ConstatDBHelper dbHelper;
    int constatId;
    Button btnUpdate, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_constat);

        dbHelper = new ConstatDBHelper(this);
        tvDate = findViewById(R.id.tvDate);
        tvVeh1 = findViewById(R.id.tvVeh1);
        tvVeh2 = findViewById(R.id.tvVeh2);
        tvLat = findViewById(R.id.tvLat);
        tvLon = findViewById(R.id.tvLon);
        tvPlace = findViewById(R.id.tvPlace);
        photoContainer = findViewById(R.id.photoContainer);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        // Récupérer l'ID du constat
        constatId = getIntent().getIntExtra("constatId", -1);
        Constat c = dbHelper.getConstat(constatId);

        if (c != null) {
            tvDate.setText(c.getDate());
            tvVeh1.setText(c.getVehicule1());
            tvVeh2.setText(c.getVehicule2());
            tvLat.setText(String.valueOf(c.getLatitude()));
            tvLon.setText(String.valueOf(c.getLongitude()));
            tvPlace.setText(c.getPlace());

            for (byte[] photo : c.getPhotos()) {
                ImageView iv = new ImageView(this);
                iv.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo.length));
                iv.setAdjustViewBounds(true);
                iv.setMaxHeight(400);
                iv.setPadding(0, 8, 0, 8);
                photoContainer.addView(iv);
            }

            // -------- Modifier constat --------
            btnUpdate.setOnClickListener(v -> {
                Intent intent = new Intent(this, ConstatActivity.class);
                intent.putExtra("editId", constatId);
                startActivity(intent);
                finish();
            });

            // -------- Supprimer constat --------
            btnDelete.setOnClickListener(v -> {
                dbHelper.deleteConstat(constatId);
                Toast.makeText(this, "Constat supprimé", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
}
