package com.intertech.fyp.mobileapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class Registration extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Spinner roles; //a.k.a Dropdown list
    Button cancelButton, registerButton;
    int count = 0;
    String accountID;

    TextView afterRoleText, afterRoleText1;

    //Edit text are textboxes
    EditText username, name, phoneNumber, password,
            confirmPassword, email, address, afterRoleBox, afterRoleBox1;

    boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //Buttons
        cancelButton = findViewById(R.id.regCancelButton);
        cancelButton.setOnClickListener(this::setCancelButton);

        registerButton = findViewById(R.id.regRegisterButton);
        registerButton.setOnClickListener(this::setRegisterButton);

        //Text view
        afterRoleText   = findViewById(R.id.regAfterRoleText);
        afterRoleText1  = findViewById(R.id.regAfterRoleText1);

        //EditTexts
        username        = findViewById(R.id.regUsernameBox);
        phoneNumber     = findViewById(R.id.regPhoneNumberBox);
        name            = findViewById(R.id.regNameBox);
        password        = findViewById(R.id.regPasswordBox);
        confirmPassword = findViewById(R.id.regConfirmPasswordBox);
        email           = findViewById(R.id.regEmailBox);
        address         = findViewById(R.id.regAddressBox);
        afterRoleBox    = findViewById(R.id.regAfterRoleBox);
        afterRoleBox1   = findViewById(R.id.regAfterRoleBox1);

        //For dropdown list a.k.a. spinner
        roles = findViewById(R.id.regRoles);
        ArrayAdapter<CharSequence> rolesAdapter = ArrayAdapter.createFromResource(this,
                R.array.user_roles, android.R.layout.simple_spinner_item);
        rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roles.setAdapter(rolesAdapter);
        roles.setOnItemSelectedListener(this);

    }

    private boolean fieldValidation()
    {
        Boolean isValid = true;

        if (username.getText().toString().trim().isEmpty() || phoneNumber.getText().toString().trim().isEmpty() ||
                name.getText().toString().trim().isEmpty() || email.getText().toString().trim().isEmpty() || address.getText().toString().trim().isEmpty())
        {
            Toast.makeText(Registration.this, "All of the fields must not be empty", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        if (!password.getText().toString().equals(confirmPassword.getText().toString()))
        {
            Toast.makeText(Registration.this, "Password and Confirm Password do not match!", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        String choice = roles.getSelectedItem().toString();

        if (!choice.equals("Receiver"))
        {
            if (afterRoleBox.getText().toString().trim().isEmpty())
                isValid =false;
        }
        else
        {
            if (afterRoleBox.getText().toString().trim().isEmpty() || afterRoleBox1.getText().toString().trim().isEmpty())
                isValid = false;
        }


        return  isValid;
    }

    private void setRegisterButton(View view)
    {
        if (!fieldValidation())
        {
            return;
        }

        int index = roles.getSelectedItemPosition();
        JSONObject data = new JSONObject();

        try{
            data.put("accountID", accountID);
            data.put("username", username.getText());
            data.put("phoneNum", phoneNumber.getText());
            data.put("password", password.getText());
            data.put("name", name.getText());
            data.put("email", email.getText());
            data.put("address", address.getText());
            data.put("accType", roles.getSelectedItem().toString());

            if (index == 1)
            {
                data.put("company", afterRoleBox.getText());
            }
            else if (index == 2)
            {
                data.put("company", afterRoleBox.getText());
                data.put("empID", afterRoleBox1.getText());
            }
            else
            {
                data.put("altContactPerson", afterRoleBox.getText());
                data.put("altPhoneNum", afterRoleBox1.getText());
            }

        } catch (JSONException ex)
        {
            Toast.makeText(Registration.this, "Registration Input data error: " + ex.toString(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        final String jsonBody = data.toString();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                MainActivity.databaseLink + "registration.php",
                response -> {
                    StateDialog(Registration.this, response);
                    success = true;
                },
                error -> {
                    Toast.makeText(Registration.this, "Registration JSON error: " + error.toString(),
                            Toast.LENGTH_LONG).show();
                    success = false;
                }
        )
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return jsonBody == null ? null : jsonBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Toast.makeText(Registration.this, uee.toString(), Toast.LENGTH_LONG).show();
                    return null;
                }
            }
        };

        RequestQueue queue = Volley.newRequestQueue(Registration.this);
        queue.add(request);

        //Require validation
        //finish();
    }

    private void setCancelButton (View view)
    {
        finish();
    }


    //Debug purposes for the spinner
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String fromSpinner = adapterView.getItemAtPosition(i).toString();

        StringRequest accCount = new StringRequest(
                Request.Method.GET,
                MainActivity.databaseLink + "accountsCount.php",
                response -> {
                    count = Integer.parseInt(response) + 1;
                },
                error -> {
                    Toast.makeText(Registration.this, "Accounts Count :" + error.toString(), Toast.LENGTH_LONG).show();
                }
        );
        RequestQueue queue = Volley.newRequestQueue(Registration.this);
        queue.add(accCount);

        switch (fromSpinner)
        {
            case "Sender":
                accountID = "S" + count;
                afterRoleText.setText("Company: ");
                afterRoleText.setVisibility(View.VISIBLE);
                afterRoleBox.setVisibility(View.VISIBLE);
                afterRoleText1.setVisibility(View.INVISIBLE);
                afterRoleBox1.setVisibility(View.INVISIBLE);
                break;

            case "Courier":
                accountID = "C" + count;
                afterRoleText.setText("Company: ");
                afterRoleText.setVisibility(View.VISIBLE);
                afterRoleBox.setVisibility(View.VISIBLE);

                afterRoleText1.setText("Employee ID (if any): ");
                afterRoleText1.setVisibility(View.VISIBLE);
                afterRoleBox1.setVisibility(View.VISIBLE);;

                break;

            case "Receiver":
                accountID = "R" + count;
                afterRoleText.setText("Alternate Contact Person:");
                afterRoleText.setVisibility(View.VISIBLE);

                afterRoleText1.setText("Alternate Phone Number:");
                afterRoleText1.setVisibility(View.VISIBLE);

                afterRoleBox.setVisibility(View.VISIBLE);
                afterRoleBox1.setVisibility(View.VISIBLE);
                break;

            default:
                afterRoleText.setVisibility(View.INVISIBLE);
                afterRoleText1.setVisibility(View.INVISIBLE);
                afterRoleBox.setVisibility(View.INVISIBLE);
                afterRoleBox1.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void StateDialog(Context context, String state)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Registration")
                .setMessage(state)
                .setNeutralButton("OK", ((dialog, which) -> {
                    if (success)
                        finish();
                })).show();
    }
}