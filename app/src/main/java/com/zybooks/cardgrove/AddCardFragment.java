package com.zybooks.cardgrove;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class AddCardFragment extends Fragment {

    private EditText editTextCardName;
    private Spinner spinnerOccasion;
    private EditText editTextQuantity;
    private DatabaseHelper databaseHelper;

    public AddCardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_card, container, false);

        editTextCardName = view.findViewById(R.id.editTextCardName);
        spinnerOccasion = view.findViewById(R.id.spinnerOccasion);
        editTextQuantity = view.findViewById(R.id.editTextQuantity);
        Button buttonAddCard = view.findViewById(R.id.buttonAddCard);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(getContext());

        //Replaced with lambda to make it more concise
        buttonAddCard.setOnClickListener(v -> addCard());

        return view;
    }

    private void addCard() {
        String itemName = editTextCardName.getText().toString().trim();
        String itemType = spinnerOccasion.getSelectedItem().toString();
        String quantityStr = editTextQuantity.getText().toString().trim();

        if (validateInput(itemName, quantityStr)) {
            int quantity = Integer.parseInt(quantityStr);
            boolean isInserted = databaseHelper.addItem(itemType, itemName, quantity);
            if (isInserted) {
                Toast.makeText(getContext(), "Card added successfully", Toast.LENGTH_SHORT).show();

                //Navigate back to the Card Inventory fragment after adding the item
                getParentFragmentManager().beginTransaction().replace(R.id.fragment_container, new CardInventoryFragment()).commit();

            } else {
                Toast.makeText(getContext(), "Error adding card", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInput(String itemName, String quantityStr) {
        if (itemName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter Card name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (itemName.length() > 250) {
            Toast.makeText(getContext(), "Card name cannot exceed 250 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (quantityStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter Card quantity", Toast.LENGTH_SHORT).show();
            return false;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Quantity must be a valid number", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (quantity < 1 || quantity > 100) {
            Toast.makeText(getContext(), "Quantity must be between 1 and 100", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
