package com.intertech.fyp.mobileapplication;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class ScanQR extends AppCompatActivity {

    /*
    This is a class for scanning QR code
     */

    //Object for the scanner
    CodeScanner codeScanner;

    TextView scannedText;
    Button confirmBtn, returnBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Toast.makeText(ScanQR.this, "Submitting", Toast.LENGTH_LONG).show();

        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        Point point = new Point();
        display.getSize(point);

        int height = 810;
        int width = 810;

        int dimension = width < height ? width : height;
        dimension = dimension * 3 / 4;
        Bitmap bitmap;

        JSONObject jsonBody = new JSONObject();
        //need to verify if the product is repeated...

        String scan = scannedText.getText().toString();

        try {
            if (!scan.equals("Scan something....")) {
                String[] data = scan.split(", ");
                jsonBody.put("productID", data[0]);
                jsonBody.put("productName", data[1]);
                jsonBody.put("manufactureDate", data[2]);
                jsonBody.put("expiryDate", data[3]);
                jsonBody.put("senderAcc", data[4]);
                jsonBody.put("company", data[5]);
                jsonBody.put("serialNum", data[6]);
                jsonBody.put("category", data[7]);

                QRGEncoder encoder = new QRGEncoder(scan, null, QRGContents.Type.TEXT, dimension);
                bitmap = encoder.encodeAsBitmap();

                jsonBody.put("qrCode", BitmapToString(bitmap));
            }
        } catch (Exception e) {
            Toast.makeText(ScanQR.this, "Fail to add: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        //This is the http request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                MainActivity.databaseLink + "productsAdd.php",
                jsonBody,
                response ->
                {
                    //Once it succeeds in connecting, it will get a json object from the web page
                    String result;

                    try {
                        //Get string from the json object
                        //Will cause exception if the object doesn't has the variable 'state'
                        //Will become error if the response received isn't a jsonObject

                        result = response.getString("state");

                        StateDialog(ScanQR.this, result);

                    } catch (JSONException e) {
                        Toast.makeText(ScanQR.this, "ScanQR submitData() Exception Error: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error ->
                {
                    Toast.makeText(ScanQR.this, "ScanQR submitData() Request Error: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        );

        queue.add(request);
    }

    protected void setReturnBtn(View view) {
        finish();
    }

    private void StateDialog(Context context, String state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Insertion")
                .setMessage(state)
                .setNeutralButton("OK", ((dialog, which) -> {
                    finish();
                })).show();
    }

}