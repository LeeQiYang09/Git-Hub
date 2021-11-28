package com.intertech.fyp.mobileapplicationcourier.ui.transactions;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.intertech.fyp.mobileapplicationcourier.R;

import org.json.JSONException;
import org.json.JSONObject;

public class TransactionDetailsFragment extends Fragment {

    TextView transactionID, deviceID, pickupAddress, productID, courierAcc, pickedUp, receiverAcc,
            deliveryAddress, deliveryCoord, delivered, deliveryFunds;

    public TransactionDetailsFragment() {
        // Required empty public constructor
    }

    public static TransactionDetailsFragment newInstance(String param1, String param2) {
        TransactionDetailsFragment fragment = new TransactionDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_transaction_details, container, false);

        transactionID = view.findViewById(R.id.tdTransactionIDBox);
        deviceID = view.findViewById(R.id.tdDeviceIDBox);
        pickupAddress = view.findViewById(R.id.tdPickupAddressBox);
        productID = view.findViewById(R.id.tdProductIDBox);
        courierAcc = view.findViewById(R.id.tdCourierBox);
        pickedUp = view.findViewById(R.id.tdPickedupBox);
        receiverAcc = view.findViewById(R.id.tdReceiverIDBox);
        deliveryAddress = view.findViewById(R.id.tdDeliveryAddressBox);
        deliveryCoord = view.findViewById(R.id.tdGPSBox);
        delivered = view.findViewById(R.id.tdDeliveredBox);
        deliveryFunds = view.findViewById(R.id.tdDeliveryFundsBox);

        String transactionInfo = getArguments().getString("transactionInfo");
        JSONObject data;

        try{
            data = new JSONObject(transactionInfo);

            transactionID.setText(data.getString("transactionID"));
            deviceID.setText(data.getString("deviceID"));
            pickupAddress.setText(data.getString("pickupAddress"));
            productID.setText(data.getString("productID"));
            courierAcc.setText(data.getString("courierAcc"));

            if (data.getInt("pickedUp") == 1)
                pickedUp.setText("Yes");
            else
                pickedUp.setText("Not");

            receiverAcc.setText(data.getString("receiverAcc"));
            deliveryAddress.setText(data.getString("deliveryAddress"));
            deliveryCoord.setText(data.getString("deliveryCoord"));

            if (data.getInt("delivered") == 1)
                delivered.setText("Yes");
            else
                delivered.setText("Not");

            Double funds = data.getDouble("deliveryFunds");
            deliveryFunds.setText(funds.toString());


        }catch (JSONException e)
        {
            Toast.makeText(view.getContext(), "Transaction Details Data is null : " + e.toString(), Toast.LENGTH_LONG).show();
        }

        return view;
    }
}