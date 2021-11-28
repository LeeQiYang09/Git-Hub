package com.intertech.fyp.mobileapplicationreceiver;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

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
        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> scannedText.setText(result.getText())));
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

    //To setup http POST request to enter details
    protected void setConfirmBtn(View view) {
        RequestQueue queue = Volley.newRequestQueue(ScanQR.this);

        JSONObject data = new JSONObject();

        String scan = scannedText.getText().toString();

        try {
            if (!scan.equals("Scan something....")) {
                data.put("deviceID", scan);
                data.put("accountID", MainActivity.receiverDetails.getString("accountID"));
            }
        } catch (Exception e) {
            Toast.makeText(ScanQR.this, "Fail to identify: " + e.toString(), Toast.LENGTH_LONG).show();
        }


        final String jsonBody = data.toString();
        Toast.makeText(ScanQR.this, "Body: " + jsonBody, Toast.LENGTH_SHORT).show();

        //This is the http request
        StringRequest request = new StringRequest(
                Request.Method.POST,
                MainActivity.databaseLink + "receiversScanQR.php",
                response ->
                {
                    Toast.makeText(ScanQR.this, "Response: " + response, Toast.LENGTH_LONG).show();
                    try {
                        JSONObject obj = new JSONObject(response);
                        String result = obj.getString("state");

                        if (result.equals("True"))
                            StateDialog(ScanQR.this,"Congratulations", "You have received the product!");
                        else
                            StateDialog(ScanQR.this, "UwU","Transaction not matched, please contact administrator!");
                    } catch (JSONException e) {
                        Toast.makeText(ScanQR.this, "ScanQR submitData() Exception Error: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error ->
                        Toast.makeText(ScanQR.this, "ScanQR submitData() Request Error: " + error.toString(), Toast.LENGTH_LONG).show()
        ) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return jsonBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Toast.makeText(view.getContext(), uee.toString(), Toast.LENGTH_LONG).show();
                    return null;
                }
            }

        };


        queue.add(request);
    }

    protected void setReturnBtn(View view) {
        finish();
    }

    private void StateDialog(Context context, String title, String state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(state)
                .setNeutralButton("OK", ((dialog, which) -> finish())).show();
    }
}