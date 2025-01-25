package com.zybooks.cardgrove;

import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class CardInventoryFragment extends Fragment implements CardAdapter.OnItemClickListener {

    private DatabaseHelper databaseHelper;
    private CardAdapter cardAdapter;
    private Cursor cursor; // Keep a reference to the Cursor

    public CardInventoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card_inventory, container, false);

        databaseHelper = new DatabaseHelper(getContext());

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1)); // 1 column

        // Load inventory data
        cursor = databaseHelper.getAllItems();
        cardAdapter = new CardAdapter(getContext(), cursor, this);
        recyclerView.setAdapter(cardAdapter);

        return view;
    }

    @Override
    public void onItemClick(int position) {
        // TODO: For future functionality, handle item click if needed
    }

    @Override
    public void onReduceClick(int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        int itemIdIndex = cursor.getColumnIndex("item_id");
        int qtyIndex = cursor.getColumnIndex("qty");
        int typeIndex = cursor.getColumnIndex("type");
        int nameIndex = cursor.getColumnIndex("name");

        if (itemIdIndex == -1 || qtyIndex == -1 || typeIndex == -1 || nameIndex == -1) {
            Toast.makeText(getContext(), "Error accessing database. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = cursor.getLong(itemIdIndex);
        int quantity = cursor.getInt(qtyIndex);

        if (quantity > 0) {
            databaseHelper.updateItem((int) id, cursor.getString(typeIndex),
                    cursor.getString(nameIndex), quantity - 1);

            // Refresh the cursor after updating
            cursor = databaseHelper.getAllItems();
            cardAdapter.swapCursor(cursor);

            // Re-check if the cursor can move to the correct position
            if (cursor.moveToPosition(position)) {
                // Send alert if the quantity reaches zero
                if (quantity - 1 == 0) {
                    if (getContext() instanceof MainActivity) {
                        ((MainActivity) getContext()).sendSmsAlert("Inventory for " + cursor.getString(nameIndex) + " has reached 0! Please restock.");
                    }
                }
            } else {
                Toast.makeText(getContext(), "Error: Cursor out of bounds.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onDeleteClick(int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        int itemIdIndex = cursor.getColumnIndex("item_id");

        // Check if the column exists in the Cursor
        if (itemIdIndex == -1) {
            Toast.makeText(getContext(), "Error accessing database. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = cursor.getLong(itemIdIndex);
        boolean deleted = databaseHelper.deleteItem((int) id);
        if (deleted) {
            Toast.makeText(getContext(), "Card deleted", Toast.LENGTH_SHORT).show();
            cursor = databaseHelper.getAllItems(); // Refresh cursor after deletion
            cardAdapter.swapCursor(cursor);
        } else {
            Toast.makeText(getContext(), "Error deleting card", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onIncreaseClick(int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        int itemIdIndex = cursor.getColumnIndex("item_id");
        int qtyIndex = cursor.getColumnIndex("qty");
        int typeIndex = cursor.getColumnIndex("type");
        int nameIndex = cursor.getColumnIndex("name");

        // Check if columns exist in the Cursor
        if (itemIdIndex == -1 || qtyIndex == -1 || typeIndex == -1 || nameIndex == -1) {
            Toast.makeText(getContext(), "Error accessing database. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = cursor.getLong(itemIdIndex);
        int quantity = cursor.getInt(qtyIndex);

        // Increase the quantity by 1 only if it's less than 100
        if (quantity < 100) {
            databaseHelper.updateItem((int) id, cursor.getString(typeIndex),
                    cursor.getString(nameIndex), quantity + 1);
            cursor = databaseHelper.getAllItems(); // Refresh cursor after update
            cardAdapter.swapCursor(cursor);
        } else {
            Toast.makeText(getContext(), "Quantity cannot exceed 100.", Toast.LENGTH_SHORT).show();
        }
    }
}
