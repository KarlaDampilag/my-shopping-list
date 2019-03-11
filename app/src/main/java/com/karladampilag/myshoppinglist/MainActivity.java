package com.karladampilag.myshoppinglist;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ArrayList<ShoppingListItem> shoppingList;
    private AutoCompleteTextView input;
    private ArrayAdapter<String> adapter;
    private Type type;
    private Gson gson;
    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;
    private boolean headingCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Work on AutoCompleteView*/
        final String[] ITEMNAMES = getResources().getStringArray(R.array.item_names);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, ITEMNAMES);
        input = (AutoCompleteTextView) findViewById(R.id.inputText);
        input.setAdapter(adapter);

        /*Add action to Item name TextView's DONE keyboard press*/
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Button addBtn = (Button) findViewById(R.id.button);
                    addBtn.performClick();
                }
                return false;
            }
        });

        /*Read from Shared Preferences*/
        initializeMisc();
        String storedData = appSharedPrefs.getString("the_data", "");
        shoppingList = gson.fromJson(storedData, type);
        if(shoppingList != null && !shoppingList.isEmpty()){
            createHeading();
            updateList();
            updateTotal();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*Save to Shared Preferences*/
        //serialize the ArrayList instance
        String json = gson.toJson(shoppingList, type);
        //commit the serialized json to shared pref
        prefsEditor.putString("the_data", json);
        prefsEditor.commit();
    }

    public void initializeMisc(){
        //get type of ShoppingListItem
        type = new TypeToken<List<ShoppingListItem>>(){}.getType();
        //initialize Gson object
        gson = new Gson();
        //initialize shared preferences instance
        appSharedPrefs = getPreferences(MODE_PRIVATE);
        //initialize editor
        prefsEditor =  appSharedPrefs.edit();
    }

    public void createHeading(){
        //Create the Linear Layout that holds the heading of the list
        LinearLayout headingContainer = (LinearLayout) findViewById(R.id.headingContainer);
        headingContainer.setOrientation(LinearLayout.HORIZONTAL);
        headingContainer.removeAllViews();

        //Create the checkbox Layout parameter
        LinearLayout.LayoutParams paramHeadCheck = new LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                0.4f
        );
        //Create the head name Layout parameter
        LinearLayout.LayoutParams paramHeadName = new LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                2.1f
        );
        paramHeadName.setMargins(0,0,8,0);
        //Create the price Layout parameter
        LinearLayout.LayoutParams paramHeadPrice = new LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                0.6f
        );
        paramHeadPrice.setMargins(8, 0, 0, 0);
        //Create the quantity Layout parameter
        LinearLayout.LayoutParams paramHeadQty = new LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                0.4f
        );
        //Create the remove button Layout parameter
        LinearLayout.LayoutParams paramHeadBtn = new LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                0.5f
        );
        paramHeadBtn.setMargins(0, 0, 0, 0);

        //Create the TextView for the checkbox heading (this will be blank text because it will just serve as a space)
        TextView headCheck = new TextView(this);
        headCheck.setLayoutParams(paramHeadCheck);

        //Create the TextView for item name heading
        TextView headName = new TextView(this);
        headName.setText("ITEM NAME");
        headName.setTextColor(Color.parseColor("#000000"));
        headName.setLayoutParams(paramHeadName);

        //Create the TextView for item price heading
        TextView headPrice = new TextView(this);
        headPrice.setText("PRICE");
        headPrice.setTextColor(Color.parseColor("#000000"));
        headPrice.setLayoutParams(paramHeadPrice);

        //Create the TextView for item quantity heading
        TextView headQty = new TextView(this);
        headQty.setText("QTY");
        headQty.setTextColor(Color.parseColor("#000000"));
        headQty.setLayoutParams(paramHeadQty);

        //Create the TextView for the remove button heading (this will be blank text because it will just serve as a space)
        TextView headRemove = new TextView(this);
        headRemove.setLayoutParams(paramHeadBtn);

        headingContainer.addView(headCheck);
        headingContainer.addView(headName);
        headingContainer.addView(headQty);
        headingContainer.addView(headPrice);
        headingContainer.addView(headRemove);

        headingCreated = true;
    }

    public void addItem(View view) {
        //Create heading if not created yet
        if(!headingCreated){
            createHeading();
        }

        //Get the input text which is the item name
        String toAddString = input.getText().toString();

        if(toAddString.isEmpty()){
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Ooops!");
            alertDialog.setMessage("Empty item name is not allowed");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Got It",
                    new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int which){
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }else{
            //Create the Linear Layout that holds the item views
            final LinearLayout itemContainer = new LinearLayout(this);
            itemContainer.setOrientation(LinearLayout.HORIZONTAL);

            //Create a ShoppingListItem instance and add to the ArrayList
            ShoppingListItem shoppingListItem = new ShoppingListItem(toAddString);
            if(shoppingList == null){
                shoppingList = new ArrayList<ShoppingListItem>();
            }
            shoppingList.add(shoppingListItem);

            updateList();

            //Reset the input textbox for item name
            input.setText("");

            Toast toast = Toast.makeText(getApplicationContext(), "'" + toAddString+ "' added to list", Toast.LENGTH_SHORT);
            toast.show();
        }
    }//end of AddItem method

    public void updateList(){
        //get listContainer
        LinearLayout listContainer = (LinearLayout) findViewById(R.id.list_container);
        //clear it first
        listContainer.removeAllViews();

        //Create the checkbox Layout parameter
        LinearLayout.LayoutParams paramCheck = new LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                0.4f
        );

        //Create the name Layout parameter
        LinearLayout.LayoutParams paramName = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                2.1f
        );
        paramName.setMargins(0,0,8,0);

        //Create the price Layout parameter
        LinearLayout.LayoutParams paramPrice = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0.6f
        );

        //Create the quantity Layout parameter
        LinearLayout.LayoutParams paramQty = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                0.4f
        );
        paramQty.setMargins(0, 0, 12, 0);
        //Create the remove button Layout parameter
        LinearLayout.LayoutParams paramBtn = new LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                0.5f
        );
        paramBtn.setMargins(0, 0, 0, 0);

        //Create the itemContainer Layout parameter
        LinearLayout.LayoutParams paramItem = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );

        for(int c = 0; c < shoppingList.size(); c++){
            final int currentCounter = c;
            final ShoppingListItem currentItem = shoppingList.get(currentCounter);
            final String currentName = currentItem.getName();

            //Create a LinearLayout to hold all Views (checkbox, name, price, remove button)
            final LinearLayout itemContainer = new LinearLayout(this);
            itemContainer.setGravity(Gravity.CENTER);

            //Create TextView for the ShoppingListItem instance name
            final AutoCompleteTextView itemNameView = new AutoCompleteTextView(this);
            itemNameView.setText(currentName);
            itemNameView.setTextSize(12);
            itemNameView.setTextColor(Color.parseColor("#000000"));
            itemNameView.setBackgroundColor(Color.TRANSPARENT);
            itemNameView.setPadding(8, 8, 8, 8);
            itemNameView.setLayoutParams(paramName);
            itemNameView.setInputType(InputType.TYPE_CLASS_TEXT);
            itemNameView.setHorizontallyScrolling(false);
            itemNameView.setMaxLines(3);
            itemNameView.setImeOptions(EditorInfo.IME_ACTION_DONE);
            itemNameView.setAdapter(adapter);

            //add text change listener
            itemNameView.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }


                        @Override
                        public void afterTextChanged(Editable s) {
                            String response = itemNameView.getText().toString();
                            if(!response.isEmpty()) {
                                currentItem.setName(response);
                            }else{
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setTitle("Ooops!");
                                alertDialog.setMessage("Empty item name is not allowed");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Got It",
                                        new DialogInterface.OnClickListener(){
                                            public void onClick(DialogInterface dialog, int which){
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }

                        }
                    }
            );

            //add focus change listener
            itemNameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        itemNameView.setBackgroundResource(R.drawable.edittext_bg);
                    }else{
                        itemNameView.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            });
            itemNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        currentItem.setName(itemNameView.getText().toString());
                        updateList();
                    }
                    return false;
                }
            });

            //Create EditView for the ShoppingListItem instance price
            final EditText itemPriceView = new EditText(this);
            itemPriceView.setLayoutParams(paramPrice);
            itemPriceView.setTextSize(12);
            itemPriceView.setBackgroundResource(R.drawable.edittext_bg);
            itemPriceView.setHint("price");
            itemPriceView.setHintTextColor(Color.parseColor("#9E9E9E"));
            itemPriceView.setPadding(12, 8, 8, 8);
            itemPriceView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            itemPriceView.setImeOptions(EditorInfo.IME_ACTION_DONE);

            //if item has a set price in ArrayList, capture it and display
            double itemPrice = currentItem.getPrice();
            if (itemPrice > 0) {
                itemPriceView.setText("" + itemPrice);
            }

            itemPriceView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        String response = itemPriceView.getText().toString();
                        if( (!response.isEmpty()) && (!response.equals(".")) ) {
                            try{
                                double priceValue = Double.parseDouble(itemPriceView.getText().toString());
                                currentItem.setPrice(priceValue);
                            }catch(NumberFormatException e){
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setTitle("Oooops!");
                                alertDialog.setMessage("Please enter a valid price number");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Got It",
                                        new DialogInterface.OnClickListener(){
                                            public void onClick(DialogInterface dialog, int which){
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                itemPriceView.setText("");
                            }
                        }else{
                            currentItem.setPrice(0.00);
                        }
                        updateList();
                        updateTotal();
                    }
                    return false;
                }
            });

            //add text change listener
            itemPriceView.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }


                        @Override
                        public void afterTextChanged(Editable s) {
                            String response = itemPriceView.getText().toString();
                            if( (!response.isEmpty()) && (!response.equals(".")) ) {
                                try{
                                    double priceValue = Double.parseDouble(itemPriceView.getText().toString());
                                    currentItem.setPrice(priceValue);
                                }catch(NumberFormatException e){
                                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                    alertDialog.setTitle("Oooops!");
                                    alertDialog.setMessage("Please enter a valid price number");
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Got It",
                                            new DialogInterface.OnClickListener(){
                                                public void onClick(DialogInterface dialog, int which){
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialog.show();
                                    itemPriceView.setText("");
                                }
                            }else{
                                currentItem.setPrice(0.00);
                            }
                            updateTotal();
                        }
                    }
            );

            //add focus change listener
            itemPriceView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus){
                        itemPriceView.setHint("");
                    }else{
                        String response = itemPriceView.getText().toString();
                        if( (!response.isEmpty()) && (!response.equals(".")) ) {
                            try{
                                double priceValue = Double.parseDouble(itemPriceView.getText().toString());
                                currentItem.setPrice(priceValue);
                            }catch(NumberFormatException e){
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setTitle("Oooops!");
                                alertDialog.setMessage("Please enter a valid price number");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Got It",
                                        new DialogInterface.OnClickListener(){
                                            public void onClick(DialogInterface dialog, int which){
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                itemPriceView.setText("");
                            }
                        }else{
                            itemPriceView.setText("");
                            currentItem.setPrice(0.00);
                        }
                        updateTotal();
                    }
                }
            });

            //Create EditView for the ShoppingListItem instance quantity
            final EditText itemQtyView = new EditText(this);
            itemQtyView.setLayoutParams(paramQty);
            itemQtyView.setTextSize(12);
            itemQtyView.setBackgroundResource(R.drawable.edittext_bg);
            itemQtyView.setHint("qty");
            itemQtyView.setHintTextColor(Color.parseColor("#9E9E9E"));
            itemQtyView.setPadding(12, 8, 8, 8);
            itemQtyView.setInputType(InputType.TYPE_CLASS_NUMBER);
            itemQtyView.setImeOptions(EditorInfo.IME_ACTION_DONE);
            //if item has a set quantity in ArrayList, capture it and display
            int itemQty = currentItem.getQuantity();
            if (itemQty > 0) {
                itemQtyView.setText("" + itemQty);
            }

            itemQtyView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        String response = itemQtyView.getText().toString();
                        if(!response.equals("")) {
                            try{
                                int qtyValue = Integer.parseInt(itemQtyView.getText().toString());
                                currentItem.setQuantity(qtyValue);
                            }catch(NumberFormatException e){
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setTitle("Oooops!");
                                alertDialog.setMessage("Please enter a valid quantity number");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Got It",
                                        new DialogInterface.OnClickListener(){
                                            public void onClick(DialogInterface dialog, int which){
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                itemQtyView.setText("");
                            }
                        }else{
                            currentItem.setQuantity(1);
                        }
                        updateList();
                        updateTotal();

                    }
                    return false;
                }
            });

            //add text change listener
            itemQtyView.addTextChangedListener(
                    new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String response = itemQtyView.getText().toString();
                            if(!response.equals("")) {
                                try{
                                    int qtyValue = Integer.parseInt(itemQtyView.getText().toString());
                                    currentItem.setQuantity(qtyValue);
                                }catch(NumberFormatException e){
                                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                    alertDialog.setTitle("Oooops!");
                                    alertDialog.setMessage("Please enter a valid quantity number");
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Got It",
                                            new DialogInterface.OnClickListener(){
                                                public void onClick(DialogInterface dialog, int which){
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialog.show();
                                    itemQtyView.setText("");
                                }
                            }else{
                                currentItem.setQuantity(1);
                            }
                            updateTotal();
                        }
                    }
            );

            //add focus change listener
            itemQtyView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus){
                        String response = itemQtyView.getText().toString();
                        if(!response.equals("")) {
                            try{
                                int qtyValue = Integer.parseInt(itemQtyView.getText().toString());
                                currentItem.setQuantity(qtyValue);
                            }catch(NumberFormatException e){
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setTitle("Oooops!");
                                alertDialog.setMessage("Please enter a valid quantity number.");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Got It",
                                        new DialogInterface.OnClickListener(){
                                            public void onClick(DialogInterface dialog, int which){
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                                itemQtyView.setText("");
                            }
                        }else{
                            currentItem.setQuantity(1);
                        }
                        updateTotal();
                    }
                }
            });

            //Create CheckBox for the ShoppingListItem instance
            CheckBox itemCheckBox = new CheckBox(this);
            itemCheckBox.setLayoutParams(paramCheck);
            //if item is checked, set checkbox to checked
            if(currentItem.getIsChecked()){
                itemCheckBox.setChecked(true);
            }
            //add change check listener
            itemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        currentItem.setIsChecked(true);
                    }else{
                        currentItem.setIsChecked(false);
                    }
                    String response = itemPriceView.getText().toString();
                    if(!response.equals("")){
                        try{
                            double priceValue = Double.parseDouble(itemPriceView.getText().toString());
                            currentItem.setPrice(priceValue);
                        }catch(NumberFormatException e){
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle("Oooops!");
                            alertDialog.setMessage("Please enter a valid price number");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Got It",
                                    new DialogInterface.OnClickListener(){
                                        public void onClick(DialogInterface dialog, int which){
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                            itemPriceView.setText("");
                        }
                    }
                    updateTotal();
                    updateList();
                }
            });

            //Create remove Button
            Button itemRemoveBtn = new Button(this);
            itemRemoveBtn.setText("X");
            itemRemoveBtn.setTextSize(16);
            itemRemoveBtn.setTextColor(Color.parseColor("#F44336"));
            itemRemoveBtn.setBackgroundColor(Color.TRANSPARENT);
            itemRemoveBtn.setLayoutParams(paramBtn);

            itemRemoveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Confirm Deletion");
                    alertDialog.setMessage("Delete '" + currentName + "'?");
                    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    alertDialog.setNegativeButton(android.R.string.no, null);
                    alertDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Toast toast = Toast.makeText(getApplicationContext(), "'" +currentName + "' deleted from list", Toast.LENGTH_SHORT);
                            toast.show();
                            shoppingList.remove(currentItem);
                            itemContainer.removeAllViews();
                            updateList();
                            updateTotal();
                        }}
                    );
                    alertDialog.show();

                }
            });

            //Populate listItem with the Views and add design
            itemContainer.addView(itemCheckBox);
            itemContainer.addView(itemNameView);
            itemContainer.addView(itemQtyView);
            itemContainer.addView(itemPriceView);
            itemContainer.addView(itemRemoveBtn);
            itemContainer.setBackgroundResource(R.drawable.item_bg);
            itemContainer.setPadding(4, 4, 4, 4);
            itemContainer.setOrientation(LinearLayout.HORIZONTAL);
            itemContainer.setLayoutParams(paramItem);

            //Add the listItem to listContainer
            listContainer.addView(itemContainer);
        }
    }

    public void updateTotal(){
        double totalPrice = 0;

        for(int c = 0; c <= shoppingList.size() - 1; c ++){
            double itemPrice = shoppingList.get(c).getPrice();
            int itemQuantity = shoppingList.get(c).getQuantity();
            if(shoppingList.get(c).getIsChecked()){
                totalPrice += itemPrice * itemQuantity;
            }
        }

        TextView totalView = (TextView) findViewById(R.id.totalView);
        totalView.setText(NumberFormat.getNumberInstance(Locale.US).format(totalPrice));
    }

    public void reset(View v){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Confirm Reset Action");
        alertDialog.setMessage("Remove everything from list?");
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setNegativeButton(android.R.string.no, null);
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                shoppingList.clear();
                Toast toast = Toast.makeText(getApplicationContext(), "Your list is now empty", Toast.LENGTH_SHORT);
                toast.show();
                updateList();
                updateTotal();
            }}
        );
        alertDialog.show();
    }

    /**
     * ShoppingListItem class
     */
    public class ShoppingListItem{
        private String name;
        private double price;
        private int quantity;
        private boolean isChecked;

        /**
         * The constructor for ShoppingListItem wherein all fields are given
         * @param inputName the item name
         * @param inputPrice the item price
         * @param inputQty the item quantity
         * @param inputChecked whether the item is currently checked or not
         */
        public ShoppingListItem(String inputName, double inputPrice, int inputQty, boolean inputChecked){
            name = inputName;
            price = inputPrice;
            quantity = inputQty;
            isChecked = inputChecked;
        }

        /**
         * The constructor for ShoppingListItem wherein only the name, price and quantity are given
         * @param inputName the item name
         * @param inputPrice the item price
         * @param inputQty the item quantity
         */
        public ShoppingListItem(String inputName, double inputPrice, int inputQty){
            name = inputName;
            price = inputPrice;
            quantity = inputQty;
            isChecked = false;
        }

        /**
         * The constructor for ShoppingListItem wherein only the name and price are given
         * @param inputName the item name
         * @param inputPrice the item price
         */
        public ShoppingListItem(String inputName, double inputPrice){
            name = inputName;
            price = inputPrice;
            quantity = 1;
            isChecked = false;
        }

        /**
         * The constructor for ShoppingListItem wherein only the name is given
         * @param inputName the item name
         */
        public ShoppingListItem(String inputName){
            name = inputName;
            price = 0.00;
            quantity = 1;
            isChecked = false;
        }

        public void setName(String inputName){
            name = inputName;
        }

        public void setPrice(double inputPrice){
            price = inputPrice;
        }

        public void setQuantity(int inputQty) { quantity = inputQty; }

        public void setIsChecked(boolean inputChecked){
            isChecked = inputChecked;
        }

        public String getName(){
            return name;
        }

        public double getPrice(){
            return price;
        }

        public int getQuantity() { return quantity; }

        public boolean getIsChecked(){
            return isChecked;
        }
    } //end of ShoppingListItem class
}
