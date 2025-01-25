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

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private final Context context;
    private Cursor cursor;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onReduceClick(int position);
        void onDeleteClick(int position);
        void onIncreaseClick(int position);
    }

    public CardAdapter(Context context, Cursor cursor, OnItemClickListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        String cardName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String cardType = cursor.getString(cursor.getColumnIndexOrThrow("type"));
        int cardQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("qty"));
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("item_id"));

        int iconResId = getCardTypeIcon(cardType);
        holder.cardTypeIcon.setImageResource(iconResId);
        holder.cardName.setText(cardName);
        holder.inventoryCount.setText(String.valueOf(cardQuantity));
        holder.itemView.setTag(id);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        if (newCursor != null) {
            notifyDataSetChanged(); // Replace with more specific change events if possible
        }
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        public final ImageView cardTypeIcon;
        public final TextView cardName;
        public final TextView inventoryCount;
        public final ImageView reduceCountIcon;
        public final ImageView deleteCardIcon;
        public final ImageView increaseCountIcon;

        public CardViewHolder(View view, final OnItemClickListener listener) {
            super(view);
            cardTypeIcon = view.findViewById(R.id.cardTypeIcon);
            cardName = view.findViewById(R.id.cardName);
            inventoryCount = view.findViewById(R.id.inventoryCount);
            reduceCountIcon = view.findViewById(R.id.reduceCountIcon);
            deleteCardIcon = view.findViewById(R.id.deleteCardIcon);
            increaseCountIcon = view.findViewById(R.id.increaseCountIcon);

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

    //Function to display different icons in the inventory grid based on the card type/occasion
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
                return R.drawable.ic_card_other; //Other icon used as default
        }
    }
}
