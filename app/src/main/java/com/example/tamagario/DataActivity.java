package com.example.tamagario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class DataActivity extends AppCompatActivity {

    private AppDatabase db;
    private Pet currentPet;

    private ProgressBar happinessBar;
    private ProgressBar hungerBar;
    private ProgressBar hygieneBar;
    private ProgressBar energyBar;
    private TextView dataDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // --- Room setup ---
        db = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "tamagario-db"
                )
                .allowMainThreadQueries()
                .build();

        PetDao petDao = db.petDao();
        currentPet = petDao.getSinglePet();

        // If somehow no pet exists yet, create a default one
        if (currentPet == null) {
            currentPet = new Pet();
            currentPet.name = "Tamagario";
            currentPet.hunger = 50;
            currentPet.energy = 50;
            currentPet.happiness = 50;
            currentPet.hygiene = 50;
            long id = petDao.insertPet(currentPet);
            currentPet.id = id;
        }

        // --- Views ---
        happinessBar = findViewById(R.id.happiness_bar);
        hungerBar = findViewById(R.id.hunger_bar);
        hygieneBar = findViewById(R.id.hygiene_bar);
        energyBar = findViewById(R.id.energy_bar);
        dataDescription = findViewById(R.id.data_description);
        ImageView backButton = findViewById(R.id.back_button);

        // All stats are 0–100
        happinessBar.setMax(100);
        hungerBar.setMax(100);
        hygieneBar.setMax(100);
        energyBar.setMax(100);

        // Set current values
        happinessBar.setProgress(currentPet.happiness);
        hungerBar.setProgress(currentPet.hunger);
        hygieneBar.setProgress(currentPet.hygiene);
        energyBar.setProgress(currentPet.energy);

        // Simple summary text
        String summary = "Happiness: " + currentPet.happiness + "/100\n"
                + "Hunger: " + currentPet.hunger + "/100\n"
                + "Hygiene: " + currentPet.hygiene + "/100\n"
                + "Energy: " + currentPet.energy + "/100";
        dataDescription.setText(summary);

        // Back button -> return to main screen
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(DataActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
