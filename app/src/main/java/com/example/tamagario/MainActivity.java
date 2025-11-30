package com.example.tamagario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

public class MainActivity extends AppCompatActivity
{
    private AppDatabase db;
    private Pet currentPet;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ---------------------------
        // Set up Room database
        // ---------------------------
        db = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "tamagario-db"
                )
                // for simplicity in a school project; normally you'd use background threads
                .allowMainThreadQueries()
                .build();

        PetDao petDao = db.petDao();
        InteractionDao interactionDao = db.interactionDao();

        // Get existing pet or create a default one
        currentPet = petDao.getSinglePet();
        if (currentPet == null) {
            currentPet = new Pet();
            currentPet.name = "Tamagario";
            currentPet.hunger = 50;
            currentPet.energy = 50;
            currentPet.happiness = 50;
            currentPet.hygiene = 50;

            long newId = petDao.insertPet(currentPet);
            currentPet.id = newId;
        }

        // ---------------------------
        // Navigation buttons
        // ---------------------------
        ImageView dataBtn = findViewById(R.id.data_button);
        ImageView settingsBtn = findViewById(R.id.settings_button);

        dataBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DataActivity.class);
            startActivity(intent);
        });

        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // ---------------------------
        // Interaction buttons
        // ---------------------------
        ImageView playBtn = findViewById(R.id.play_button);
        ImageView feedBtn = findViewById(R.id.feed_button);
        ImageView cleanBtn = findViewById(R.id.clean_button);
        ImageView restBtn = findViewById(R.id.rest_button);

        // Play -> happiness up
        playBtn.setOnClickListener(v -> {
            currentPet.happiness = Math.min(currentPet.happiness + 10, 100);
            petDao.updatePet(currentPet);

            Interaction interaction = new Interaction();
            interaction.petId = currentPet.id;
            interaction.type = "PLAY";
            interaction.timestamp = System.currentTimeMillis();
            interactionDao.insertInteraction(interaction);
        });

        // Feed -> hunger up
        feedBtn.setOnClickListener(v -> {
            currentPet.hunger = Math.min(currentPet.hunger + 10, 100);
            petDao.updatePet(currentPet);

            Interaction interaction = new Interaction();
            interaction.petId = currentPet.id;
            interaction.type = "FEED";
            interaction.timestamp = System.currentTimeMillis();
            interactionDao.insertInteraction(interaction);
        });

        // Clean -> hygiene up
        cleanBtn.setOnClickListener(v -> {
            currentPet.hygiene = Math.min(currentPet.hygiene + 10, 100);
            petDao.updatePet(currentPet);

            Interaction interaction = new Interaction();
            interaction.petId = currentPet.id;
            interaction.type = "CLEAN";
            interaction.timestamp = System.currentTimeMillis();
            interactionDao.insertInteraction(interaction);
        });

        // Rest -> energy up
        restBtn.setOnClickListener(v -> {
            currentPet.energy = Math.min(currentPet.energy + 10, 100);
            petDao.updatePet(currentPet);

            Interaction interaction = new Interaction();
            interaction.petId = currentPet.id;
            interaction.type = "REST";
            interaction.timestamp = System.currentTimeMillis();
            interactionDao.insertInteraction(interaction);
        });
    }
}
