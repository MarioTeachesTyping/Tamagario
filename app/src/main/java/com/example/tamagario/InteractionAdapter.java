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

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class InteractionAdapter extends RecyclerView.Adapter<InteractionAdapter.InteractionViewHolder> {

    private final List<Interaction> interactions;
    private final Context context;
    private final InteractionDao interactionDao;

    public InteractionAdapter(Context context, List<Interaction> interactions, InteractionDao interactionDao) {
        this.context = context;
        this.interactions = interactions;
        this.interactionDao = interactionDao;
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
                    interaction.type = types[which];
                    interaction.timestamp = System.currentTimeMillis();
                    interactionDao.updateInteraction(interaction);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Interaction updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(Interaction interaction, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Interaction")
                .setMessage("Are you sure you want to delete this interaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    interactionDao.deleteInteraction(interaction);
                    interactions.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Interaction deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
