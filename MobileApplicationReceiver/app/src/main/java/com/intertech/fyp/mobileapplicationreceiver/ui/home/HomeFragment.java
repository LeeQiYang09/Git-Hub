package com.intertech.fyp.mobileapplicationreceiver.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.intertech.fyp.mobileapplicationreceiver.MainActivity;
import com.intertech.fyp.mobileapplicationreceiver.R;
import com.intertech.fyp.mobileapplicationreceiver.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private View root;
    private TableLayout productTableLayout, transactionTableLayout;
    private ArrayList<JSONObject> productObjList = new ArrayList<>();
    private ArrayList<JSONObject> transactionObjList = new ArrayList<>();
    JSONArray arr;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        productTableLayout = root.findViewById(R.id.homeProductTableLayout);
        transactionTableLayout = root.findViewById(R.id.homeTransactionTableLayout);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void populateProductTable() {
        RequestQueue queue = Volley.newRequestQueue(root.getContext());

        JSONObject data = MainActivity.receiverDetails;

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
                                    try {
                                        //Dynamically add a new row to the table layout
                                        for (int i = 0; i < arr.length(); i++) {
                                            obj = arr.getJSONObject(i);
                                            productObjList.add(obj);
                                            TextView id = new TextView(root.getContext());
                                            TextView name = new TextView(root.getContext());
                                            TextView expDate = new TextView(root.getContext());
                                            TextView company = new TextView(root.getContext());

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

                                            int size = 18;
                                            id.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                            name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                            expDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                            company.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);

                                            //Add the textview to table row
                                            TableRow tr = new TableRow(root.getContext());
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
                                            tr.setOnClickListener(this::productRowClick);
                                            productTableLayout.addView(tr);
                                        }
                                    } catch (JSONException e) {
                                        Toast.makeText(root.getContext(), "Product table Error: " + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }

                            } catch (JSONException e) {
                                Toast.makeText(root.getContext(), "Product table : " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        },
                        error -> Toast.makeText(root.getContext(), "Product table : " + error.toString(), Toast.LENGTH_LONG).show()) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return jsonBody == null ? null : jsonBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Toast.makeText(root.getContext(), uee.toString(), Toast.LENGTH_LONG).show();
                    return null;
                }
            }

        };

        queue.add(request);
    }

    private void productRowClick(View view) {
        int indexNum = view.getId() - R.id.reservedNamedId;

        Bundle bundle = new Bundle();
        bundle.putString("productInfo", productObjList.get(indexNum).toString());
        ListFragment list = new ListFragment();
        list.setArguments(bundle);

        Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_productDetails, bundle);
    }

    private void populateTransactionTable() {
        RequestQueue queue = Volley.newRequestQueue(root.getContext());

        JSONObject data = new JSONObject();

        try {
            data.put("receiverAcc", MainActivity.receiverDetails.getString("accountID"));
            data.put("accType", MainActivity.receiverDetails.getString("accType"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String jsonBody = data.toString();

        StringRequest request = new StringRequest(Request.Method.POST,
                MainActivity.databaseLink + "transactionsGet.php",
                response ->
                {
                    try {
                        arr = new JSONArray(response);

                        if (arr != null) {
                            boolean white = false;
                            JSONObject obj;
                            try {
                                for (int i = 0; i < arr.length(); i++)
                                {
                                    obj = arr.getJSONObject(i);
                                    transactionObjList.add(obj);

                                    if (obj.getInt("pickedUp") == 1 && obj.getInt("delivered") == 1) {

                                        //Do nothing

                                    } else {
                                        TextView transactionID = new TextView(root.getContext());
                                        TextView receiverID = new TextView(root.getContext());
                                        TextView productID = new TextView(root.getContext());
                                        TextView courierID = new TextView(root.getContext());
                                        TextView pickedUp = new TextView(root.getContext());
                                        TextView delivered = new TextView(root.getContext());

                                        TableRow.LayoutParams params = new TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT, 10f);

                                        transactionID.setText(obj.getString("transactionID"));
                                        receiverID.setText(obj.getString("receiverAcc"));
                                        productID.setText(obj.getString("productID"));
                                        courierID.setText(obj.getString("courierAcc"));

                                        if (obj.getString("pickedUp").equals("1"))
                                            pickedUp.setText("Done");
                                        else
                                            pickedUp.setText("Not Yet");
                                        if (obj.getString("delivered").equals("1"))
                                            delivered.setText("Done");
                                        else
                                            delivered.setText("Not Yet");

                                        transactionID.setGravity(Gravity.CENTER);
                                        receiverID.setGravity(Gravity.CENTER);
                                        productID.setGravity(Gravity.CENTER);
                                        courierID.setGravity(Gravity.CENTER);
                                        pickedUp.setGravity(Gravity.CENTER);
                                        delivered.setGravity(Gravity.CENTER);

                                        transactionID.setTextColor(Color.parseColor("#000000"));
                                        receiverID.setTextColor(Color.parseColor("#000000"));
                                        productID.setTextColor(Color.parseColor("#000000"));
                                        courierID.setTextColor(Color.parseColor("#000000"));
                                        pickedUp.setTextColor(Color.parseColor("#000000"));
                                        delivered.setTextColor(Color.parseColor("#000000"));

                                        //Font Size
                                        int size = 18;
                                        transactionID.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                        receiverID.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                        productID.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                        courierID.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                        pickedUp.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                                        delivered.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);

                                        TableRow tr = new TableRow(root.getContext());
                                        tr.addView(transactionID);
                                        tr.addView(receiverID);
                                        tr.addView(productID);
                                        tr.addView(courierID);
                                        tr.addView(pickedUp);
                                        tr.addView(delivered);

                                        if (!white) {
                                            tr.setBackgroundResource(R.color.gray);
                                            white = !white;
                                        } else {
                                            tr.setBackgroundResource(R.color.white);
                                            white = !white;
                                        }
                                        tr.setId(R.id.TransactionReservedNames + i);
                                        tr.setOnClickListener(this::transactionRowClick);
                                        transactionTableLayout.addView(tr);
                                    }
                                }
                            } catch (JSONException e) {
                                Toast.makeText(root.getContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }

                    } catch (JSONException e) {
                        Toast.makeText(root.getContext(), "Error: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(root.getContext(), "Error: " + error.toString(), Toast.LENGTH_LONG).show()) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return jsonBody == null ? null : jsonBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Toast.makeText(root.getContext(), uee.toString(), Toast.LENGTH_LONG).show();
                    return null;
                }
            }
        };

        queue.add(request);
    }

    private void transactionRowClick(View view) {
//        action_nav_home_to_nav_transactionsDetails

        int indexNum = view.getId() - R.id.TransactionReservedNames;

        Bundle bundle = new Bundle();

        bundle.putString("transactionInfo", transactionObjList.get(indexNum).toString());

        ListFragment list = new ListFragment();
        list.setArguments(bundle);
        Navigation.findNavController(view).navigate(R.id.action_nav_home_to_nav_transactionsDetails, bundle);
    }

    public void onResume()
    {
        super.onResume();
        populateProductTable();
        populateTransactionTable();
    }
}