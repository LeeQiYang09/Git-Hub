package com.intertech.fyp.mobileapplicationcourier;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Base64;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class ScanQR extends AppCompatActivity {

    CodeScanner codeScanner;

    TextView scannedText;
    Button confirmBtn, returnBtn;

    //Todo change add product to receive instead
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        setContentView(R.layout.activity_scan_qr);

        //Buttons
        confirmBtn = findViewById(R.id.scanQRConfirmButton);
        confirmBtn.setOnClickListener(this::setConfirmBtn);

        returnBtn = findViewById(R.id.scanQRReturnButton);
        returnBtn.setOnClickListener(this::setReturnBtn);

        //TextView
        scannedText = findViewById(R.id.scannerText);

        //Scanner object, runs on other thread once activated
        CodeScannerView scanner = findViewById(R.id.scannerCamera);
        codeScanner = new CodeScanner(this, scanner);
        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            scannedText.setText(result.getText());
        }));
        scanner.setOnClickListener(view -> codeScanner.startPreview());
    }

    @Override
    protected void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }

    private String BitmapToString(Bitmap image) {
        final int COMPRESSION_QUALITY = 100;
        String img;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, stream);
        byte[] b = stream.toByteArray();
        img = Base64.encodeToString(b, Base64.DEFAULT);

        return img;
    }

    //To setup http POST request to enter details
    protected void setConfirmBtn(View view) {
        RequestQueue queue = Volley.newRequestQueue(ScanQR.this);
        //Toast.makeText(ScanQR.this, "Submitting", Toast.LENGTH_LONG).show();

        JSONObject data = new JSONObject();
        //need to verify if the product is repeated...

        String scan = scannedText.getText().toString();

        try {
            if (!scan.equals("Scan something....")) {
                data.put("deviceID", scan);
                data.put("accountID", MainActivity.courierDetails.getString("accountID"));
            }
        } catch (Exception e) {
            Toast.makeText(ScanQR.this, "Fail to identify: " + e.toString(), Toast.LENGTH_LONG).show();
        }


        final String jsonBody = data.toString();
        Toast.makeText(ScanQR.this, "Body: " + jsonBody, Toast.LENGTH_SHORT).show();

        //This is the http request
        StringRequest request = new StringRequest(
                Request.Method.POST,
                MainActivity.databaseLink + "couriersScanQR.php",
                response ->
                {
                    //Toast.makeText(ScanQR.this, "Response: " + response, Toast.LENGTH_LONG).show();
                    try {
                        JSONObject obj = new JSONObject(response);
                        String result = obj.getString("state");

                        if (result.equals("True"))
                            StateDialog(ScanQR.this, "Pick Up Status", result);

                    } catch (JSONException e) {
                        Toast.makeText(ScanQR.this, "ScanQR submitData() Exception Error: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error ->
                {
                    Toast.makeText(ScanQR.this, "ScanQR submitData() Request Error: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        ){

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return jsonBody == null ? null : jsonBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Toast.makeText(view.getContext(), uee.toString(), Toast.LENGTH_LONG).show();
                    return null;
                }
            }

        };;


        queue.add(request);
    }

    protected void setReturnBtn(View view) {
        finish();
    }

    private void StateDialog(Context context,String title, String state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(state)
                .setNeutralButton("OK", ((dialog, which) -> {
                    finish();
                })).show();
    }
}