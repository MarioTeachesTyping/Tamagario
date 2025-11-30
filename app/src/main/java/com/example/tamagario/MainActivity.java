package com.example.tamagario;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;

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
    private ImageView petImage;

    private final Handler handler = new Handler(Looper.getMainLooper());

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

        // Pet image
        petImage = findViewById(R.id.tamagario);

        // Set up Room database
        db = Room.databaseBuilder(
                        getApplicationContext(),
                        AppDatabase.class,
                        "tamagario-db"
                )
                // For simplicity. works a diff way apparently
                .allowMainThreadQueries()
                .build();

        PetDao petDao = db.petDao();
        InteractionDao interactionDao = db.interactionDao();

        // Get existing pet or create a default one
        currentPet = petDao.getSinglePet();
        if (currentPet == null)
        {
            currentPet = new Pet();
            currentPet.name = "Tamagario";
            currentPet.hunger = 0;
            currentPet.energy = 0;
            currentPet.happiness = 0;
            currentPet.hygiene = 0;

            long newId = petDao.insertPet(currentPet);
            currentPet.id = newId;
        }

        // Set initial image based on stats (perfect vs normal)
        updatePetImage();

        // Navigation buttons
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

        // Interaction buttons
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

            Toast.makeText(MainActivity.this,
                    "Increased Tamagario's Happiness by +10",
                    Toast.LENGTH_SHORT).show();

            showHappyTemporarily();
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

            Toast.makeText(MainActivity.this,
                    "Increased Tamagario's Hunger by +10",
                    Toast.LENGTH_SHORT).show();

            showHappyTemporarily();
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

            Toast.makeText(MainActivity.this,
                    "Increased Tamagario's Hygiene by +10",
                    Toast.LENGTH_SHORT).show();

            showHappyTemporarily();
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

            Toast.makeText(MainActivity.this,
                    "Increased Tamagario's Energy by +10",
                    Toast.LENGTH_SHORT).show();

            showHappyTemporarily();
        });
    }

    // Sets image based on current stats
    private void updatePetImage()
    {
        if (isPetPerfect())
        {
            petImage.setImageResource(R.drawable.tamagario_perfect);
        }
        else
        {
            petImage.setImageResource(R.drawable.tamagario);
        }
    }

    private boolean isPetPerfect()
    {
        return currentPet.hunger == 100 &&
                currentPet.energy == 100 &&
                currentPet.happiness == 100 &&
                currentPet.hygiene == 100;
    }

    // Show happy for 1 second, then revert to normal or perfect
    private void showHappyTemporarily()
    {
        // If pet is perfect after this interaction, just show perfect permanently
        if (isPetPerfect())
        {
            petImage.setImageResource(R.drawable.tamagario_perfect);
            return;
        }

        // Show happy
        petImage.setImageResource(R.drawable.tamagario_happy);

        // After 1 second, revert to normal (or perfect if it somehow became perfect)
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> {
            if (isPetPerfect())
            {
                petImage.setImageResource(R.drawable.tamagario_perfect);
            }
            else
            {
                petImage.setImageResource(R.drawable.tamagario);
            }
        }, 1000); // 1000 ms = 1 second
    }
}