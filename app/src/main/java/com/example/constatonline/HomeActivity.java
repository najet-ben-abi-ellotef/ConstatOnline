package com.example.constatonline;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnRemplir = findViewById(R.id.btnRemplirConstat);
        Button btnListe = findViewById(R.id.btnListeConstats);

        Button btnAnnuler = findViewById(R.id.btnAnnuler);

        // Aller vers remplir constat
        btnRemplir.setOnClickListener(v ->
                startActivity(new Intent(this, ConstatActivity.class)));

        // Aller vers liste des constats
        btnListe.setOnClickListener(v ->
                startActivity(new Intent(this, ListeConstatsActivity.class)));



        // Annuler / Déconnexion → Login
        btnAnnuler.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
