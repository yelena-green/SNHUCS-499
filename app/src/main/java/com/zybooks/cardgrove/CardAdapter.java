/**
 * SNHU CS-499 Software Capstone Project
 * Author: Yelena Green
 * Purpose: This class serves as the RecyclerView adapter for displaying the card inventory.
 * It binds data from the SQLite database to the RecyclerView, handles UI interactions,
 * and allows sorting and filtering of card data.
 */

package com.zybooks.cardgrove;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Adapter for displaying card inventory in a RecyclerView.
 * Handles UI updates and user interactions for modifying card inventory.
 */
public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private final Context context;
    private Cursor cursor;
    private final OnItemClickListener listener;

    /**
     * Interface for handling user interactions such as clicking and modifying inventory.
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
        void onReduceClick(int position);
        void onDeleteClick(int position);
        void onIncreaseClick(int position);
    }

    /**
     * Constructor for initializing the adapter.
     *
     * @param context  The application context.
     * @param cursor   The cursor containing card inventory data.
     * @param listener The listener for handling user actions on cards.
     */
    public CardAdapter(Context context, Cursor cursor, OnItemClickListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
    }

    /**
     * Inflates the item layout and returns a new CardViewHolder instance.
     */
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view, listener);
    }

    /**
     * Binds data from the database to the RecyclerView item view.
     */
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return; // Prevents crashes if cursor moves out of bounds
        }

        // Retrieve card details from database
        String cardName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String cardType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
        int cardQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("qty"));
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("item_id"));

        // Set UI elements with retrieved data
        int iconResId = getCardTypeIcon(cardType);
        holder.cardTypeIcon.setImageResource(iconResId);
        holder.cardName.setText(cardName);
        holder.inventoryCount.setText(String.valueOf(cardQuantity));
        holder.itemView.setTag(id);
    }

    /**
     * Returns the number of items in the dataset.
     */
    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    /**
     * Updates the adapter with a new Cursor containing updated card data.
     *
     * @param newCursor The new Cursor with updated inventory data.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close(); // Close the previous cursor to free resources
        }
        cursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged(); // Refresh the RecyclerView with new data
        }
    }

    /**
     * ViewHolder class for holding and binding UI elements for each card item.
     */
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        public final ImageView cardTypeIcon;
        public final TextView cardName;
        public final TextView inventoryCount;
        public final ImageView reduceCountIcon;
        public final ImageView deleteCardIcon;
        public final ImageView increaseCountIcon;

        /**
         * Initializes the UI components and sets up event listeners.
         *
         * @param view     The view representing a single card item.
         * @param listener The listener for handling user actions.
         */
        public CardViewHolder(View view, final OnItemClickListener listener) {
            super(view);
            cardTypeIcon = view.findViewById(R.id.cardTypeIcon);
            cardName = view.findViewById(R.id.cardName);
            inventoryCount = view.findViewById(R.id.inventoryCount);
            reduceCountIcon = view.findViewById(R.id.reduceCountIcon);
            deleteCardIcon = view.findViewById(R.id.deleteCardIcon);
            increaseCountIcon = view.findViewById(R.id.increaseCountIcon);

            // Set click listeners for each action (Reduce, Delete, Increase)
            reduceCountIcon.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onReduceClick(position);
                    }
                }
            });

            deleteCardIcon.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });

            increaseCountIcon.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onIncreaseClick(position);
                    }
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    /**
     * Returns the appropriate icon resource ID based on the card type.
     *
     * @param cardType The type of the card (e.g., "Birthday", "Thank You").
     * @return The corresponding drawable resource ID for the card type.
     */
    private int getCardTypeIcon(String cardType) {
        switch (cardType) {
            case "Birthday":
                return R.drawable.ic_card_birthday;
            case "Thank you":
                return R.drawable.ic_card_thankyou;
            case "Get well":
                return R.drawable.ic_card_getwell;
            case "Anniversary":
                return R.drawable.ic_card_anniversary;
            default:
                return R.drawable.ic_card_other; // Default icon for other card types
        }
    }
}
