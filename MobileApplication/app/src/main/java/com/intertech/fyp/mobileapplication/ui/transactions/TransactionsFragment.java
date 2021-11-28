package com.intertech.fyp.mobileapplication.ui.transactions;

import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.intertech.fyp.mobileapplication.MainActivity;
import com.intertech.fyp.mobileapplication.R;
import com.intertech.fyp.mobileapplication.Transactions_Add;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class TransactionsFragment extends Fragment {

    JSONArray arr;
    ArrayList <JSONObject> objList = new ArrayList<>();
    TableLayout table;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view =inflater.inflate(R.layout.fragment_transactions, container, false);

        Button addButton = view.findViewById(R.id.addTransactionsButton);
        addButton.setOnClickListener(this::setAddButton);

        table = view.findViewById(R.id.transactionsTableLayout);
        populateTableString(view, table);


        return view;
    }

    private void setAddButton(View view)
    {
        Intent i = new Intent(view.getContext(), Transactions_Add.class);
        startActivity(i);
    }

    private void populateTableString(@NonNull View v, TableLayout tableLayout) {
        RequestQueue queue = Volley.newRequestQueue(v.getContext());

        JSONObject data = new JSONObject();

        try {
            data.put("senderAcc", MainActivity.senderDetails.getString("accountID"));
            data.put("accType", MainActivity.senderDetails.getString("accType"));
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

                        if (arr != null)
                        {
                            boolean white = false;
                            JSONObject obj;
                            try
                            {
                                for (int i = 0; i < arr.length(); i++)
                                {
                                    obj = arr.getJSONObject(i);
                                    objList.add(obj);
                                    TextView transactionID = new TextView(v.getContext());
                                    TextView receiverID = new TextView(v.getContext());
                                    TextView productID = new TextView(v.getContext());
                                    TextView courierID = new TextView(v.getContext());
                                    TextView pickedUp = new TextView(v.getContext());
                                    TextView delivered = new TextView(v.getContext());

                                    TableRow.LayoutParams params = new TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT, 10f);

                                    transactionID.setText(obj.getString("transactionID"));
                                    receiverID.setText(obj.getString("receiverAcc"));
                                    productID.setText(obj.getString("productID"));
                                    courierID.setText(obj.getString("courierAcc"));
                                    pickedUp.setText(obj.getString("pickedUp"));
                                    delivered.setText(obj.getString("delivered"));

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

                                    TableRow tr = new TableRow(v.getContext());
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
                error -> Toast.makeText(v.getContext(), "Error: " + error.toString(), Toast.LENGTH_LONG).show()) {

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

        int indexNum = view.getId() - R.id.TransactionReservedNames;

        Bundle bundle = new Bundle();
        bundle.putString("transactionInfo", objList.get(indexNum).toString());

        ListFragment list = new ListFragment();
        list.setArguments(bundle);

        Navigation.findNavController(view).navigate(R.id.action_nav_transactions_to_nav_transactionsDetails, bundle);
    }
}