package com.intertech.fyp.mobileapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

//Aka login class
public class MainActivity extends AppCompatActivity {

    TextView phoneNumBox, passwordBox, username, accountID;
    Button loginBtn, registerBtn;

    //Global variable between classes
    public static JSONObject senderDetails;

    String phoneNum;
    String senderAddress;

    //Todo: CHANGE WHEN GOING TO TEST, PRESENTING OR TRADESHOW
    //public static String databaseLink = "http://dbtest290721.duckdns.org/";
    public static String databaseLink = "http://192.168.68.110/";

    //Field or local variable
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Layout that is binded to this class
        setContentView(R.layout.activity_main);

        phoneNumBox = findViewById(R.id.phoneNumText);
        passwordBox = findViewById(R.id.passwordText);

        //Bind this variable to the UI button
        loginBtn = findViewById(R.id.loginButton);

        //Bind setLoginBtn() to this button
        loginBtn.setOnClickListener(this::setLoginBtn);

        registerBtn = findViewById(R.id.registerButton);
        registerBtn.setOnClickListener(this::setRegisterBtn);
    }

    private void setLoginBtn(View view) {

        //variable to store direction or obtain file
        Intent i = new Intent(MainActivity.this, Acc_Sender.class);

        phoneNum = String.valueOf(phoneNumBox.getText()).trim();
        password = String.valueOf(passwordBox.getText()).trim();

        if (phoneNum.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Phone number or password cannot be empty", Toast.LENGTH_LONG).show();
        } else {
            //Login by using google volley to sent POST request
            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

            //Store the detail into a json object
            JSONObject data = new JSONObject();
            try {
                data.put("phoneNum", phoneNum);
                data.put("password", password);

            } catch (JSONException e) {
                Toast.makeText(MainActivity.this, "Login: " + e.toString(), Toast.LENGTH_LONG).show();
            }

            //Setup the requests here, moving it out of ths method will disrupt the async on response method
            //When the end point responded, it will receive a json object, and decode it
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    databaseLink + "login.php",    //URL
                    data,                                               //the json object
                    response ->
                    {
                        String result;
                        try {
                            //Get the state from the message
                            result = response.getString("state");
                            if (result.equals("Success")) {

                                if (response.getString("accType").equals("Sender"))
                                {
                                    senderDetails = response;
                                    startActivity(i);
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this, "The account and password doesn't match any SENDER accounts! Please check with administrators!",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else //Show message when it is not able to login
                                Toast.makeText(MainActivity.this, response.getString("state"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "MainActivity.Request error: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_LONG).show()
            );

            //Calls the request
            requestQueue.add(request);
        }
    }


    private void setRegisterBtn(View view) {
        //Once it is clicked, this will open a
        Intent i = new Intent(MainActivity.this, Registration.class);
        startActivity(i);
    }
}