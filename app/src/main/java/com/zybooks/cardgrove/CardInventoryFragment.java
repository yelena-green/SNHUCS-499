/**
 * SNHU CS-499 Software Capstone Project
 * Author: Yelena Green
 * Purpose: This class handles UI and logic for displaying the card inventory.
 * Implements sorting & filtering functionality based on user selection.
 */

package com.zybooks.cardgrove;

import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Fragment that displays the card inventory in a grid layout.
 * Provides functionality for sorting and filtering cards.
 */
public class CardInventoryFragment extends Fragment implements CardAdapter.OnItemClickListener {

    private DatabaseHelper databaseHelper;
    private CardAdapter cardAdapter;
    private Cursor cursor;
    private RecyclerView recyclerView;

    public CardInventoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card_inventory, container, false);

        databaseHelper = new DatabaseHelper(getContext());

        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1)); // 1 column

        // Load inventory data
        cursor = databaseHelper.getAllItems();
        cardAdapter = new CardAdapter(getContext(), cursor, this);
        recyclerView.setAdapter(cardAdapter);

        // Setup sorting dropdown
        setupSortingDropdown(view);

        // Setup search bar for filtering by name
        setupSearch(view);

        return view;
    }

    /**
     * Sets up the search bar for filtering cards by name dynamically.
     *
     * @param view The fragment's root view.
     */
    private void setupSearch(View view) {
        EditText searchBar = view.findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterByName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Filters the RecyclerView to show only cards that match the search query.
     *
     * @param query The name or partial name to filter by.
     */
    private void filterByName(String query) {
        cursor = databaseHelper.getFilteredCardsByName(query);
        cardAdapter.swapCursor(cursor);
    }

    /**
     * Sets up the sorting dropdown (Spinner) to allow users to select sorting criteria.
     *
     * @param view The fragment's root view.
     */
    private void setupSortingDropdown(View view) {
        Spinner sortSpinner = view.findViewById(R.id.sortSpinner);
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View selectedItemView, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString().toLowerCase().replace(" ", "");

                // Map Spinner selection to correct column names in the database
                String criteria;
                switch (selected) {
                    case "inventorycount":
                        criteria = "qty"; // Match the actual database column
                        break;
                    case "name":
                        criteria = "name";
                        break;
                    case "type":
                        criteria = "type";
                        break;
                    default:
                        criteria = "name"; // Default sort
                }

                Log.d("CardInventoryFragment", "Sorting by: " + criteria);
                updateRecyclerView(criteria);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Updates the RecyclerView with sorted card data based on the selected criteria.
     *
     * @param criteria The sorting criteria ("name", "type", "qty").
     */
    private void updateRecyclerView(String criteria) {
        cursor = databaseHelper.getSortedCards(criteria);
        cardAdapter.swapCursor(cursor);
    }

    @Override
    public void onItemClick(int position) {
        // TODO: Handle item click functionality if needed in future updates.
    }

    /**
     * Handles the event when a user reduces the quantity of a card.
     *
     * @param position The position of the item in the RecyclerView.
     */
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

            cursor = databaseHelper.getAllItems();
            cardAdapter.swapCursor(cursor);

            if (cursor.moveToPosition(position) && quantity - 1 == 0) {
                if (getContext() instanceof MainActivity) {
                    ((MainActivity) getContext()).sendSmsAlert("Inventory for " + cursor.getString(nameIndex) + " has reached 0! Please restock.");
                }
            }
        }
    }

    /**
     * Handles the event when a user deletes a card from the inventory.
     *
     * @param position The position of the item in the RecyclerView.
     */
    @Override
    public void onDeleteClick(int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        int itemIdIndex = cursor.getColumnIndex("item_id");

        if (itemIdIndex == -1) {
            Toast.makeText(getContext(), "Error accessing database. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = cursor.getLong(itemIdIndex);
        boolean deleted = databaseHelper.deleteItem((int) id);
        if (deleted) {
            Toast.makeText(getContext(), "Card deleted", Toast.LENGTH_SHORT).show();
            cursor = databaseHelper.getAllItems();
            cardAdapter.swapCursor(cursor);
        } else {
            Toast.makeText(getContext(), "Error deleting card", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the event when a user increases the quantity of a card.
     *
     * @param position The position of the item in the RecyclerView.
     */
    @Override
    public void onIncreaseClick(int position) {
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

        if (quantity < 100) {
            databaseHelper.updateItem((int) id, cursor.getString(typeIndex),
                    cursor.getString(nameIndex), quantity + 1);
            cursor = databaseHelper.getAllItems();
            cardAdapter.swapCursor(cursor);
        } else {
            Toast.makeText(getContext(), "Quantity cannot exceed 100.", Toast.LENGTH_SHORT).show();
        }
    }
}
