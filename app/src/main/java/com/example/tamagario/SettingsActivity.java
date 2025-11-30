package com.example.tamagario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up Room
        db = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "tamagario-db"
                )
                .allowMainThreadQueries()
                .build();

        PetDao petDao = db.petDao();
        InteractionDao interactionDao = db.interactionDao();

        Pet pet = petDao.getSinglePet();
        List<Interaction> interactions = interactionDao.getInteractionsForPet(
                pet != null ? pet.id : 0
        );

        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.interactions_viewer);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        InteractionAdapter adapter = new InteractionAdapter(this, interactions, interactionDao);
        recyclerView.setAdapter(adapter);

        // Back button
        ImageView backButton = findViewById(R.id.back_button_settings);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
