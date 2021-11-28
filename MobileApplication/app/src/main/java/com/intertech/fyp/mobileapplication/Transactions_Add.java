package com.intertech.fyp.mobileapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class Transactions_Add extends AppCompatActivity{

    //Texts
    TextView gpsText;

    //Text boxes
    EditText transactionID;
    EditText pickupAdd, dropdownAddress, funds;

    //Spinner or dropdown lists
    Spinner devices, receivers, products, courier;

    Button confirmBtn, returnBtn;

    JSONArray arr = null;
    String ID = "";
    int count;

    //ArrayLists to store the objects from PHP query
    ArrayList<JSONObject> receiverList = new ArrayList<>();
    ArrayList<JSONObject> productList = new ArrayList<>();
    ArrayList<JSONObject> courierList = new ArrayList<>();

    //Arrays to setup spinner (a.k.a. Dropdown lists)
    String [] usernameArr, productsArr, devicesArr, courierArr;
    String coord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions_add);

        gpsText = findViewById(R.id.atGPSText);

        //Buttons
        confirmBtn = findViewById(R.id.atConfirmButton);
        confirmBtn.setOnClickListener(this::setConfirm);

        returnBtn = findViewById(R.id.atReturnButton);
        returnBtn.setOnClickListener(this::setReturn);

        //Edit texts (a.k.a. TextBoxes)
        transactionID = findViewById(R.id.atTransactionIDBox);
        pickupAdd = findViewById(R.id.atPickupAddressBox);
        try {
            pickupAdd.setText(MainActivity.senderDetails.getString("address"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dropdownAddress = findViewById(R.id.atReceiverAddressBox);
        dropdownAddress.setOnFocusChangeListener(this::changeAddress);
        funds = findViewById(R.id.atDeliveryFundsBox);

        //Spinners (a.k.a. Dropdown lists)
        devices = findViewById(R.id.atDeviceIDBox);
        courier = findViewById(R.id.atCourierIDBox);
        receivers = findViewById(R.id.atReceiverIDBox);
        products = findViewById(R.id.atProductIDBox);

        //When an item is selected in the spinner, changes text in receiver address
        receivers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                try {
                    dropdownAddress.setText(receiverList.get(position).getString("address"));
                    getCoordinate(dropdownAddress.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do Nothing
            }
        });

        //these functions run in async... which means Requests might run before onCreate()
        //which causes, it cannot set items directly and store them?
        getProducts();
        getCouriers();
        getReceivers();
        setDevicesSpinner();
        setTransactionID();
    }

    //Method to setup transactionID
    private void setTransactionID()
    {
        //A queue to conduct requests
        RequestQueue queue = Volley.newRequestQueue(this);

        //The request to get transactions count from DB and set the text
        //String requests receives String response, but JSONObject requests receive JSONObject
        //Suitable for GET requests, but can be setup to do POST requests also
        StringRequest requests =new StringRequest(Request.Method.POST,
                MainActivity.databaseLink + "transactionCount.php",
                response ->
                {
                    count = Integer.parseInt(response) + 1;
                    ID = "T" + count;
                    transactionID.setText(ID);
                }, error ->
        {
            Toast.makeText(this, "AT getReceivers(): " +error.toString(), Toast.LENGTH_LONG);
        }
        );

        //Execute the requests
        queue.add(requests);
    }

    //Method to populate devices spinner
    private void setDevicesSpinner()
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        //StringRequest parameters (method, url, response, error)
        StringRequest requests =new StringRequest(Request.Method.POST,
                MainActivity.databaseLink + "devicesGet.php",
                response ->
                {
                    try {
                        //If the response is an array, we need to setup an array to get what we want from it
                        arr = new JSONArray(response);

                        //Stores the string we want into an array
                        devicesArr = new String[arr.length()];
                        for (int i = 0; i < arr.length(); i++)
                        {
                            devicesArr[i] = arr.getJSONObject(i).getString("deviceID");
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "AT getReceivers(): " + e.toString(), Toast.LENGTH_LONG);
                    }

                    //This lists serves as the list for the adapter below
                    ArrayList<String> devicesArray = new ArrayList<>(Arrays.asList(devicesArr));

                    //Adapter is needed to setup the spinner
                    ArrayAdapter adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_dropdown_item, devicesArray);
                    devices.setAdapter(adapter);
                }, error ->
        {
            Toast.makeText(this, "AT getReceivers(): " +error.toString(), Toast.LENGTH_LONG);
        }
        );

        queue.add(requests);
    }

    //Similar method to setDevicesSpinner()
    private void getProducts()
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject data = new JSONObject();

        try {
            data.put("accountID", MainActivity.senderDetails.getString("accountID"));
            data.put("accType", MainActivity.senderDetails.getString("accType"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String jsonBody = data.toString();

        StringRequest request = new StringRequest(Request.Method.POST,
                MainActivity.databaseLink + "productsGet.php",
                response ->
                {
                    try {
                        arr = new JSONArray(response);
                        productsArr = new String[arr.length()];

                        assert(productsArr != null);
                        for (int i = 0; i < arr.length(); i++)
                        {
                            productList.add(arr.getJSONObject(i));
                            productsArr[i] = arr.getJSONObject(i).getString("productName");
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "" + e.toString(), Toast.LENGTH_LONG);
                    }

                    if (productsArr == null)
                    {
                        Toast.makeText(this, "productsArr is null: ", Toast.LENGTH_LONG);
                        return;
                    }

                    ArrayList <String> productsArray = new ArrayList<>(Arrays.asList(productsArr));
                    ArrayAdapter adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_dropdown_item, productsArray);
                    products.setAdapter(adapter);
                },
                error -> Toast.makeText(this, "Error: " + error.toString(), Toast.LENGTH_LONG).show()) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return jsonBody == null ? null : jsonBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Toast.makeText(Transactions_Add.this, uee.toString(), Toast.LENGTH_LONG).show();
                    return null;
                }
            }
        };

        queue.add(request);
    }

    //Similar method to setDevicesSpinner()
    private void getCouriers()
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST,
                MainActivity.databaseLink + "couriersGet.php",
                response ->
                {
                    try {
                        arr = new JSONArray(response);
                        courierArr = new String[arr.length()];
                        for (int i = 0; i < arr.length(); i++)
                        {
                            courierArr[i] = arr.getJSONObject(i).getString("name");
                            courierList.add(arr.getJSONObject(i));
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "" + e.toString(), Toast.LENGTH_LONG);
                    }
                    ArrayList <String> couriersArray = new ArrayList<>(Arrays.asList(courierArr));
                    ArrayAdapter adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_dropdown_item, couriersArray);
                    courier.setAdapter(adapter);
                },
                error -> Toast.makeText(this, "Courier Error: " + error.toString(), Toast.LENGTH_LONG).show()) {
        };

        queue.add(request);
    }

    //Similar method to setDevicesSpinner()
    private void getReceivers()
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest requests =new StringRequest(Request.Method.POST,
                MainActivity.databaseLink + "receiversGet.php",
                response ->
                {
                    try {
                        arr = new JSONArray(response);
                        usernameArr = new String[arr.length()];
                        for (int i = 0; i < arr.length(); i++)
                        {
                            receiverList.add(arr.getJSONObject(i));
                            usernameArr[i] = arr.getJSONObject(i).getString("username");
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "AT getReceivers(): " + e.toString(), Toast.LENGTH_LONG).show();
                    }

                    ArrayList<String> usernameArray = new ArrayList<>(Arrays.asList(usernameArr));

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_dropdown_item, usernameArray);
                    receivers.setAdapter(adapter);
                }, error ->
                {
                    Toast.makeText(this, "AT getReceivers(): " +error.toString(), Toast.LENGTH_LONG);
                }
                );

        queue.add(requests);
    }

    //Validation in progress
    private boolean validateFields()
    {
        Boolean pass = true;

        String deliveryFunds = funds.getText().toString().trim();
        String pickupAddress = pickupAdd.getText().toString().trim();
        String receiverAddress = dropdownAddress.getText().toString().trim();

        if (deliveryFunds.equals("") || pickupAddress.equals("") || receiverAddress.equals(""))
            pass = false;

        return pass;
    }

    private void getCoordinate(String address)
    {
        //api key for reverse geocoding
        String apikey = "AIzaSyCw1wPkgtrszhohSGmDH-OSR-IDiFv6CrY";

        RequestQueue queue = Volley.newRequestQueue(Transactions_Add.this);

        StringRequest coordRequest = new StringRequest(
                Request.Method.POST,
                "https://maps.googleapis.com/maps/api/geocode/json?address="
                        +address
                        +",+CA&key=" + apikey,
                response ->
                {
                    try {
                        JSONObject result = new JSONObject(response);
                        JSONArray jsonObject1 = (JSONArray) result.get("results");
                        JSONObject jsonObject2 = (JSONObject)jsonObject1.get(0);
                        JSONObject jsonObject3 = (JSONObject)jsonObject2.get("geometry");
                        JSONObject location = (JSONObject) jsonObject3.get("location");

                        coord = location.getString("lat") + ", " + location.getString("lng");
                        gpsText.setText(coord);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Get coord error: " + e.toString(), Toast.LENGTH_LONG).show();

                        return;
                    }
                },
                error -> Toast.makeText(this, "Error: " + error.toString(), Toast.LENGTH_LONG).show());

        queue.add(coordRequest);
    }

    //onClickListener() to insert transaction to DB
    private void setConfirm(View view)
    {
        //This time, queue will handle 2 requests
        RequestQueue queue = Volley.newRequestQueue(Transactions_Add.this);

        if (!validateFields())
        {
            Toast.makeText(this, "There is an empty field! Please check!", Toast.LENGTH_LONG);
        }

        //setting up the address for processing
        String tempAdd = dropdownAddress.getText().toString().replaceAll(",", "+");
        getCoordinate(tempAdd);

        JSONObject data = new JSONObject();

        try{
            data.put("transactionID", transactionID.getText());
            data.put("deviceID", devices.getSelectedItem());
            data.put("senderAcc", MainActivity.senderDetails.getString("accountID"));
            data.put("pickupAddress", pickupAdd.getText());
            data.put("productID", productList.get(products.getSelectedItemPosition()).getString("productID"));
            data.put("courierAcc", courierList.get(courier.getSelectedItemPosition()).getString("accountID"));
            data.put("receiverAcc", receiverList.get(receivers.getSelectedItemPosition()).getString("accountID"));
            data.put("deliveryAddress", dropdownAddress.getText().toString());
            data.put("deliveryCoord", gpsText.getText());
            data.put("deliveryFunds", Double.parseDouble(funds.getText().toString()));
        } catch (JSONException e) {
            Toast.makeText(this, "AT submitData() Body Error: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        final String jsonBody = data.toString();
        StringRequest request = new StringRequest
                (
                        Request.Method.POST,
                        MainActivity.databaseLink + "transactionsAdd.php",
                        response ->
                        {
                            //Receives a json object in return
                            Toast.makeText(this, "Transaction Insert: " + response, Toast.LENGTH_LONG).show();
                            finish();
                        },
                        error -> Toast.makeText(this, "Error: " + error.toString(), Toast.LENGTH_LONG).show())
        {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return jsonBody == null ? null : jsonBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Toast.makeText(Transactions_Add.this, uee.toString(), Toast.LENGTH_LONG).show();
                    return null;
                }
            }

        };
        queue.add(request);
    }

    private void changeAddress(View view, boolean hasFocus)
    {
        if (!hasFocus)
            getCoordinate(dropdownAddress.getText().toString());
    }

    private void setReturn(View view)
    {
        this.finish();
    }
}