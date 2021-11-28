package com.intertech.fyp.mobileapplicationreceiver.ui.transactions;

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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.intertech.fyp.mobileapplicationreceiver.MainActivity;
import com.intertech.fyp.mobileapplicationreceiver.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TransactionsFragment extends Fragment {

    TableLayout table;
    JSONArray arr;
    ArrayList<JSONObject> objList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        table = view.findViewById(R.id.transactionsTableLayout);
        populateTable(view, table);

        return view;
    }

    private void populateTable(View view, TableLayout tableLayout) {
        RequestQueue queue = Volley.newRequestQueue(view.getContext());

        JSONObject data = new JSONObject();

        try {
            data.put("receiverAcc", MainActivity.receiverDetails.getString("accountID"));
            data.put("accType", MainActivity.receiverDetails.getString("accType"));
            //Toast.makeText(view.getContext(), "receiverAcc: " + MainActivity.receiverDetails.getString("accountID"), Toast.LENGTH_SHORT).show();
        } catch (JSONException ex) {
            Toast.makeText(view.getContext(), "ReceiverDetails Error: " + ex.toString(), Toast.LENGTH_SHORT).show();
        }

        final String jsonBody = data.toString();

        StringRequest request = new StringRequest(Request.Method.POST,
                MainActivity.databaseLink + "transactionsGet.php",
                response -> {
                    try {
                        arr = new JSONArray(response);
                        boolean white = false;
                        JSONObject obj;

                        for (int i = 0; i < arr.length(); i++) {
                            obj = arr.getJSONObject(i);
                            objList.add(obj);

                            TextView transactionID = new TextView(view.getContext());
                            TextView senderID = new TextView(view.getContext());
                            TextView productID = new TextView(view.getContext());
                            TextView courierID = new TextView(view.getContext());
                            TextView pickedUp = new TextView(view.getContext());
                            TextView delivered = new TextView(view.getContext());

                            transactionID.setText(obj.getString("transactionID"));
                            senderID.setText(obj.getString("senderAcc"));
                            productID.setText(obj.getString("productID"));
                            courierID.setText(obj.getString("courierAcc"));
                            pickedUp.setText(obj.getString("pickedUp"));
                            delivered.setText(obj.getString("delivered"));

                            transactionID.setGravity(Gravity.CENTER);
                            senderID.setGravity(Gravity.CENTER);
                            productID.setGravity(Gravity.CENTER);
                            courierID.setGravity(Gravity.CENTER);
                            pickedUp.setGravity(Gravity.CENTER);
                            delivered.setGravity(Gravity.CENTER);

                            transactionID.setTextColor(Color.parseColor("#000000"));
                            senderID.setTextColor(Color.parseColor("#000000"));
                            productID.setTextColor(Color.parseColor("#000000"));
                            courierID.setTextColor(Color.parseColor("#000000"));
                            pickedUp.setTextColor(Color.parseColor("#000000"));
                            delivered.setTextColor(Color.parseColor("#000000"));

                            //Font Size
                            int size = 18;
                            transactionID.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                            senderID.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                            productID.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                            courierID.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                            pickedUp.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                            delivered.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);

                            TableRow tr = new TableRow(view.getContext());
                            tr.addView(transactionID);
                            tr.addView(senderID);
                            tr.addView(productID);
                            tr.addView(courierID);
                            tr.addView(pickedUp);
                            tr.addView(delivered);

                            if (!white) {
                                tr.setBackgroundResource(R.color.gray);
                            } else {
                                tr.setBackgroundResource(R.color.white);
                            }
                            white = !white;
                            tr.setId(R.id.TransactionReservedNames + i);
                            tr.setOnClickListener(this::rowClick);
                            tableLayout.addView(tr);
                        }
                    } catch (JSONException ex) {
                        Toast.makeText(view.getContext(), "Request error: " + ex.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(view.getContext(), "Request error: " + error.toString(),
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return jsonBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        queue.add(request);

    }

    private void rowClick(View view) {
        int indexNum = view.getId() - R.id.TransactionReservedNames;

        Bundle bundle = new Bundle();

        bundle.putString("transactionInfo", objList.get(indexNum).toString());

        ListFragment list = new ListFragment();
        list.setArguments(bundle);
        Navigation.findNavController(view).navigate(R.id.action_nav_transactions_to_nav_maps, bundle);

        //Navigation.findNavController(view).navigate(R.id.action_nav_transactions_to_nav_transactionDetails, bundle);
    }

    public void onDetach() {
        super.onDetach();
    }
}