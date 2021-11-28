package com.intertech.fyp.mobileapplicationreceiver.ui.products;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.intertech.fyp.mobileapplicationreceiver.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ProductDetailFragment extends Fragment {

    public ProductDetailFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);
        TextView productID = view.findViewById(R.id.productDetailsProductIDBox);
        TextView productName = view.findViewById(R.id.productDetailsProductNameBox);
        TextView manDate = view.findViewById(R.id.productDetailsManDateBox);
        TextView expDate = view.findViewById(R.id.productDetailsExpDateBox);
        TextView company = view.findViewById(R.id.productDetailsCompanyBox);
        TextView serialNum = view.findViewById(R.id.productDetailsSerialNumberBox);
        TextView category = view.findViewById(R.id.productDetailsCategoryBox);
        ImageView qrCode = view.findViewById(R.id.productDetailsQRCode);

        String productInfo =getArguments().getString("productInfo");
        JSONObject data = null;

        try {
            data = new JSONObject(productInfo);

            productID.setText(data.getString("productID"));
            productName.setText(data.getString("productName"));
            manDate.setText(data.getString("manufactureDate"));
            expDate.setText(data.getString("expiryDate"));
            company.setText(data.getString("company"));
            serialNum.setText(data.getString("serialNum"));
            category.setText(data.getString("category"));

            Bitmap bitmap = StringToBitmap(data.getString("qrCode"));
            qrCode.setImageBitmap(bitmap);

        } catch (JSONException e) {
            Toast.makeText(view.getContext(), "Product Details Data is null : " + e.toString(), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    private Bitmap StringToBitmap(String input)
    {
        final byte[] decodedBytes = Base64.decode(input, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}