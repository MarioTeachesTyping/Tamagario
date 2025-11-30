package com.example.tamagario;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class InteractionAdapter extends RecyclerView.Adapter<InteractionAdapter.InteractionViewHolder> {

    private final List<Interaction> interactions;
    private final Context context;
    private final InteractionDao interactionDao;
    private final PetDao petDao;           // <--- added
    private final AppDatabase db;          // <--- added

    public InteractionAdapter(Context context, List<Interaction> interactions, InteractionDao interactionDao) {
        this.context = context;
        this.interactions = interactions;
        this.interactionDao = interactionDao;

        // Set up Room here so we can update the pet stats on delete
        db = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "tamagario-db"
                )
                .allowMainThreadQueries()
                .build();

        petDao = db.petDao();
    }

    @NonNull
    @Override
    public InteractionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_interaction, parent, false);
        return new InteractionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InteractionViewHolder holder, int position) {
        Interaction interaction = interactions.get(position);

        holder.typeText.setText(interaction.type);

        String timeText = DateFormat.getDateTimeInstance().format(new Date(interaction.timestamp));
        holder.timeText.setText(timeText);

        // Tap = edit
        holder.itemView.setOnClickListener(v -> showEditDialog(interaction, holder.getAdapterPosition()));

        // Long press = delete
        holder.itemView.setOnLongClickListener(v -> {
            showDeleteDialog(interaction, holder.getAdapterPosition());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return interactions.size();
    }

    static class InteractionViewHolder extends RecyclerView.ViewHolder {
        TextView typeText;
        TextView timeText;

        public InteractionViewHolder(@NonNull View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.interaction_type);
            timeText = itemView.findViewById(R.id.interaction_time);
        }
    }

    private void showEditDialog(Interaction interaction, int position) {
        String[] types = {"FEED", "PLAY", "REST", "CLEAN"};

        new AlertDialog.Builder(context)
                .setTitle("Edit Interaction")
                .setItems(types, (dialog, which) -> {
                    String oldType = interaction.type;
                    String newType = types[which];

                    // Only adjust stats if the type actually changed
                    if (!oldType.equals(newType)) {
                        Pet pet = petDao.getSinglePet();
                        if (pet != null) {
                            // 1. Remove +10 from the OLD type
                            switch (oldType) {
                                case "FEED":
                                    pet.hunger = Math.max(pet.hunger - 10, 0);
                                    break;
                                case "PLAY":
                                    pet.happiness = Math.max(pet.happiness - 10, 0);
                                    break;
                                case "REST":
                                    pet.energy = Math.max(pet.energy - 10, 0);
                                    break;
                                case "CLEAN":
                                    pet.hygiene = Math.max(pet.hygiene - 10, 0);
                                    break;
                            }

                            // 2. Add +10 to the NEW type
                            switch (newType) {
                                case "FEED":
                                    pet.hunger = Math.min(pet.hunger + 10, 100);
                                    break;
                                case "PLAY":
                                    pet.happiness = Math.min(pet.happiness + 10, 100);
                                    break;
                                case "REST":
                                    pet.energy = Math.min(pet.energy + 10, 100);
                                    break;
                                case "CLEAN":
                                    pet.hygiene = Math.min(pet.hygiene + 10, 100);
                                    break;
                            }

                            // 3. Save updated pet
                            petDao.updatePet(pet);
                        }

                        // Update interaction type + time
                        interaction.type = newType;
                        interaction.timestamp = System.currentTimeMillis();
                        interactionDao.updateInteraction(interaction);
                        notifyItemChanged(position);

                        Toast.makeText(context,
                                "Interaction Updated and Stats Adjusted!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Type didn't change, just refresh timestamp if you want
                        interaction.timestamp = System.currentTimeMillis();
                        interactionDao.updateInteraction(interaction);
                        notifyItemChanged(position);

                        Toast.makeText(context,
                                "Interaction Updated!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(Interaction interaction, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Interaction")
                .setMessage("Are you sure you want to delete this interaction?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    // 1. Load the single pet
                    Pet pet = petDao.getSinglePet();
                    if (pet != null) {
                        // 2. Subtract 10 from the respective stat, clamped at 0
                        switch (interaction.type) {
                            case "FEED":
                                pet.hunger = Math.max(pet.hunger - 10, 0);
                                break;
                            case "PLAY":
                                pet.happiness = Math.max(pet.happiness - 10, 0);
                                break;
                            case "REST":
                                pet.energy = Math.max(pet.energy - 10, 0);
                                break;
                            case "CLEAN":
                                pet.hygiene = Math.max(pet.hygiene - 10, 0);
                                break;
                        }

                        // 3. Update pet in DB
                        petDao.updatePet(pet);
                    }

                    // 4. Delete the interaction itself
                    interactionDao.deleteInteraction(interaction);
                    interactions.remove(position);
                    notifyItemRemoved(position);

                    Toast.makeText(context, "Interaction Deleted. (-10 from Stat)", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
