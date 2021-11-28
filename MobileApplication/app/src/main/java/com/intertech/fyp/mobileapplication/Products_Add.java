package com.intertech.fyp.mobileapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.WriterException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Products_Add extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Button confirmBtn, returnBtn;
    EditText productId, productName, company, serialNum;
    ImageView qrCode;
    DatePicker manDate, expDate;
    Spinner category;
    Boolean success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_add);

        //Matching variables with respective UIs

        //EditText (a.k.a. Textbox)
        productId = findViewById(R.id.apProductIDBox);

        productName = findViewById(R.id.apProductNameBox);
        company = findViewById(R.id.apCompanyBox);
        serialNum = findViewById(R.id.apSerialNumberBox);

        //ImageView
        qrCode = findViewById(R.id.apQRCodeImageView);
        qrCode.setOnClickListener(this::generateQR);

        //Date picker set to spinning mode
        manDate = findViewById(R.id.apManufactureDateBox);
        expDate = findViewById(R.id.apExpiryDateBox);

        //Dropdown list (a.k.a. spinner)
        category = findViewById(R.id.apCategoryBox);

        //Setup mode of the spinner (context, the list, the type of layout)
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.product_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(categoryAdapter);
        category.setOnItemSelectedListener(this);

        //Buttons
        confirmBtn = findViewById(R.id.apConfirmButton);
        confirmBtn.setOnClickListener(this::setConfirmBtn);

        returnBtn = findViewById(R.id.apReturnButton);
        returnBtn.setOnClickListener(this::setCancelBtn);

        setProductID();
    }

    private void generateQR(View view) {
        //Setup the box for the image to be set
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        Point point = new Point();
        display.getSize(point);

        int height = point.x;
        int width = point.y;

        int dimension = width < height ? width :height;
        dimension = dimension * 3 / 4;

        //Prepare a variable to get all the data from thde Products_Add form
        String all = null;
        try {
            all = productId.getText() + ", " +
                    productName.getText() + ", " +
                    getDate(manDate) + ", " +
                    getDate(expDate) + ", " +
                    MainActivity.senderDetails.getString("accountID") + ", " + //Need to pass SenderAcc to here somehow
                    company.getText() + ", " +
                    serialNum.getText() + ", " +
                    category.getSelectedItem();
        } catch (JSONException e) {
            Toast.makeText(view.getContext(), "Products_Add.generateQR():" + e.toString(), Toast.LENGTH_LONG).show();
        }

        //The encoder to change text into QRcode and put it into the ImageView
        QRGEncoder encoder = new QRGEncoder(all, null, QRGContents.Type.TEXT, dimension);
        try {
            Bitmap bitmap = encoder.encodeAsBitmap();
            qrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(view.getContext(), "" + e.toString(), Toast.LENGTH_LONG).show();
        }

    }

    //Form validation
    private boolean fieldValidation()
    {
        Boolean noProblem = true;
        TextView views [] = {productId, productName, company, serialNum};

        for (int i = 0; i < views.length; i++)
        {
            if (views[i].getText().toString().trim().isEmpty())
                noProblem = false;
        }

        return noProblem;
    }

    private void submitData()
    {
        //Request queue act as the "request manager"
        RequestQueue queue = Volley.newRequestQueue(Products_Add.this);

        Toast.makeText(Products_Add.this, "Submitting", Toast.LENGTH_LONG).show();

        if (!fieldValidation())
        {
            Toast.makeText(Products_Add.this, "All fields must not be empty", Toast.LENGTH_LONG).show();
            return;
        }

        //Getting the QR code from ImageView
        BitmapDrawable drawable = (BitmapDrawable) qrCode.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        //This is the json body that is going to be sent out
        JSONObject data = new JSONObject();

        try{
            data.put("productID", productId.getText());
            data.put("productName", productName.getText());
            data.put("manufactureDate", getDate(manDate));
            data.put("expiryDate", getDate(expDate));
            data.put("senderAcc", MainActivity.senderDetails.getString("accountID"));
            data.put("company", company.getText());
            data.put("serialNum", serialNum.getText());
            data.put("category", category.getSelectedItem());
            data.put("qrCode", BitmapToString(bitmap));

        } catch (JSONException e) {
            Toast.makeText(Products_Add.this, "AP submitData() Body Error: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        //Getting the username from MainActivity class
        try {
            String name = MainActivity.senderDetails.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //This is the http request
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                MainActivity.databaseLink + "productsAdd.php",
                data,
                response ->
                {
                    //Once it succeeds in connecting, it will get a json object from the web page
                    String result;

                    try {
                        //Get string from the json object
                        //Will cause exception if the object doesn't has the variable 'state'
                        //Will become error if the response received isn't a jsonObject

                        result = response.getString("state");

                        StateDialog(Products_Add.this, "Product Add", result);
                        //Toast.makeText(Products_Add.this, result, Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        Toast.makeText(Products_Add.this, "AP submitData() Exception Error: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                error ->
                {
                    Toast.makeText(Products_Add.this, "AP submitData() Request Error: " + error.toString(), Toast.LENGTH_LONG).show();
                }
                );

        queue.add(request);
    }

    //Method to change Bitmap into a string using Base64 encoder
    private String BitmapToString(Bitmap image)
    {
        final int COMPRESSION_QUALITY = 100;
        String img;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, stream);
        byte[] b = stream.toByteArray();
        img = Base64.encodeToString(b, Base64.DEFAULT);

        return img;
    }
    private void setProductID()
    {
        RequestQueue queue =Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(
                Request.Method.GET,
                MainActivity.databaseLink + "productsCount.php",
                response -> {
                    int count = Integer.parseInt(response) + 1;

                    productId.setText("P" + count);
                },
                error -> {
                    Toast.makeText(this, "Product Count Error: " + error.toString(), Toast.LENGTH_SHORT)
                            .show();
                }
        );

        queue.add(request);
    }

    //Change Date from date picker into SQL format
    private String getDate(DatePicker date) {

        return date.getYear() + "/" +
                (date.getMonth() + 1) + "/" +
                date.getDayOfMonth();
    }

    private void StateDialog(Context context, String title, String state)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(state)
                .setNeutralButton("OK", ((dialog, which) -> {
                    finish();
                })).show();
    }

    private void setConfirmBtn(View view) {
        submitData();
    }

    private void setCancelBtn(View view)
    {
        finish();
    }

    //This is the onItemSelected Listener for spinner, it is here to for debug purpose
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}