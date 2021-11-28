package com.intertech.fyp.mobileapplication.ui.products;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.intertech.fyp.mobileapplication.Acc_Sender;
import com.intertech.fyp.mobileapplication.MainActivity;
import com.intertech.fyp.mobileapplication.Products_Add;
import com.intertech.fyp.mobileapplication.R;
import com.intertech.fyp.mobileapplication.ScanQR;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ProductsFragment extends Fragment {

    TableLayout productsTable;
    JSONArray arr = null;
    ArrayList<JSONObject> objList = new ArrayList<>();
    private final int requestCode = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_products, container, false);
        Button askBtn = view.findViewById(R.id.addProductsButton);
        askBtn.setOnClickListener(this::hasQRCode);

        productsTable = view.findViewById(R.id.productsTableLayout);
        populateTableString(view, productsTable);

        return view;
    }

    //Populate the table with simple product information with POST StringRequest
    //It will also add JSONobject to objList ArrayList for future use
    private void populateTableString(@NonNull View v, TableLayout tableLayout) {
        RequestQueue queue = Volley.newRequestQueue(v.getContext());

        JSONObject data = MainActivity.senderDetails;

        //String set for the json body to be inserted into getBody()
        final String jsonBody = data.toString();

        //String request parameter (RequestMethod, URL, response, error) {getContentType(), getBody()}
        StringRequest request = new StringRequest
                (
                        Request.Method.POST,
                MainActivity.databaseLink + "productsGet.php",
                response ->
                {
                    try {
                        //JsonArray to accept and filter the json string
                        arr = new JSONArray(response);

                        if (arr != null) {
                            boolean white = false;
                            JSONObject obj;
                            try
                            {
                                //Dynamically add a new row to the table layout
                                for (int i = 0; i < arr.length(); i++) {
                                    obj = arr.getJSONObject(i);
                                    objList.add(obj);
                                    TextView id = new TextView(v.getContext());
                                    TextView name = new TextView(v.getContext());
                                    TextView expDate = new TextView(v.getContext());
                                    TextView company = new TextView(v.getContext());

                                    //Col(1) has longer length
                                    TableRow.LayoutParams params = new TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT, 10f);

                                    //Setting the content of TextView to be inserted into the table row
                                    id.setText(obj.getString("productID"));
                                    id.setGravity(Gravity.CENTER);
                                    name.setText(obj.getString("productName"));
                                    name.setLayoutParams(params);
                                    name.setGravity(Gravity.CENTER);
                                    expDate.setText(obj.getString("expiryDate"));
                                    expDate.setGravity(Gravity.CENTER);
                                    company.setText(obj.getString("company"));
                                    company.setGravity(Gravity.CENTER);

                                    int size = 18   ;
                                    id.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                    name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                    expDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                    company.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);

                                    //Add the textview to table row
                                    TableRow tr = new TableRow(v.getContext());
                                    tr.addView(id);
                                    tr.addView(name);
                                    tr.addView(expDate);
                                    tr.addView(company);

                                    //Set text view color to black
                                    id.setTextColor(Color.parseColor("#000000"));
                                    name.setTextColor(Color.parseColor("#000000"));
                                    expDate.setTextColor(Color.parseColor("#000000"));
                                    company.setTextColor(Color.parseColor("#000000"));

                                    //Change alternate row to different color
                                    if (!white) {
                                        tr.setBackgroundResource(R.color.gray);
                                        white = !white;
                                    } else {
                                        tr.setBackgroundResource(R.color.white);
                                        white = !white;
                                    }
                                    tr.setId(R.id.reservedNamedId + i);
                                    //Set on click listner to show more details in a new fragment (to be implemented)
                                    tr.setOnClickListener(this::rowClick);
                                    tableLayout.addView(tr);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(v.getContext(), "Error: " + error.toString(), Toast.LENGTH_LONG).show())
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
                    Toast.makeText(v.getContext(), uee.toString(), Toast.LENGTH_LONG).show();
                    return null;
                }
            }

        };

        queue.add(request);
    }

    private void rowClick(View view) {

        int indexNum = view.getId() - R.id.reservedNamedId;

        Bundle bundle = new Bundle();
        bundle.putString("productInfo", objList.get(indexNum).toString());
        ListFragment list = new ListFragment();
        list.setArguments(bundle);

        Navigation.findNavController(view).navigate(R.id.action_nav_products_to_nav_productDetails  , bundle);

    }

    //To insert product either by QRCode or by form
    private void hasQRCode(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Preparing to scan")
                .setMessage("Do you have a QR code?")
                .setPositiveButton("No", (dialog, which) -> {
                    Intent i = new Intent(view.getContext(), Products_Add.class);
                    startActivity(i);

                })
                .setNegativeButton("Yes", (dialog, which) -> {
                    if (ContextCompat.checkSelfPermission(view.getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        //This requests permission to use camera
                        //ActivityCompat.requestPermissions(SenderAcc.this, new String[] {Manifest.permission.CAMERA}, requestCode);
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, requestCode);
                    }
                    else
                    {
                        Intent i = new Intent(view.getContext(), ScanQR.class);
                        startActivity(i);
                    }
                })
                .show();
    }

}