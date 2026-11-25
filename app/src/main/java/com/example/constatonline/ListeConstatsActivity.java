package com.example.constatonline;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ListeConstatsActivity extends AppCompatActivity {

    ListView lvConstats;
    ConstatDBHelper dbHelper;
    ArrayList<Constat> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_constats);

        lvConstats = findViewById(R.id.listConstats);
        dbHelper = new ConstatDBHelper(this);

        list = dbHelper.getAllConstats();

        ArrayAdapter<Constat> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, list);
        lvConstats.setAdapter(adapter);

        lvConstats.setOnItemClickListener((parent, view, position, id) -> {
            int constatId = list.get(position).getId();
            Intent intent = new Intent(this, DetailConstatActivity.class);
            intent.putExtra("constatId", constatId);
            startActivity(intent);
        });
    }
}
