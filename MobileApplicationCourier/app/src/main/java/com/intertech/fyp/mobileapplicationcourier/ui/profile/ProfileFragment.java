package com.intertech.fyp.mobileapplicationcourier.ui.profile;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.intertech.fyp.mobileapplicationcourier.MainActivity;
import com.intertech.fyp.mobileapplicationcourier.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileFragment extends Fragment {
    EditText accountID, username, phoneNum, password, confirmPassword, name,
            email, address, company;
    TextView confirmPasswordText;
    Button editBtn, returnBtn;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    private void getData()
    {
        RequestQueue queue = Volley.newRequestQueue(this.getContext());

        JSONObject data = MainActivity.courierDetails;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                MainActivity.databaseLink + "accountsGet.php",
                data,
                response -> {
                    try {
                        accountID.setText(response.getString("accountID"));
                        username.setText(response.getString("username"));
                        phoneNum.setText(response.getString("phoneNum"));
                        password.setText(response.getString("password"));
                        confirmPassword.setText(response.getString("password"));
                        name.setText(response.getString("name"));
                        email.setText(response.getString("email"));
                        address.setText(response.getString("address"));
                        company.setText(response.getString("company"));

                    }catch (JSONException ex)
                    {
                        Toast.makeText(this.getContext(), "Profile getData(): " + ex.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                },
                error -> {Toast.makeText(this.getContext(), "Profile getData(): " + error.toString(),
                        Toast.LENGTH_LONG).show();});

        queue.add(request);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        accountID = view.findViewById(R.id.profileAccountIDBox);
        username = view.findViewById(R.id.profileUsernameBox);
        phoneNum = view.findViewById(R.id.profilePhoneNumberBox);
        password = view.findViewById(R.id.profilePasswordBox);

        confirmPasswordText = view.findViewById(R.id.profileConfirmPasswordText);
        confirmPassword = view.findViewById(R.id.profileConfirmPasswordBox);

        name = view.findViewById(R.id.profileNameBox);
        email = view.findViewById(R.id.profileEmailBox);
        address = view.findViewById(R.id.profileAddressBox);
        company = view.findViewById(R.id.profileCompanyBox);

        editBtn = view.findViewById(R.id.profileEditButton);
        editBtn.setOnClickListener(this::setEditBtn);
        returnBtn = view.findViewById(R.id.profileReturnButton);
        returnBtn.setOnClickListener(this::setReturnBtn);

        getData();

        return view;
    }

    private void StateDialog(Context context, String state)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Insertion")
                .setMessage(state)
                .setNeutralButton("OK", ((dialog, which) -> {
                    getParentFragment();
                })).show();
    }

    private void setEditBtn(View view)
    {
        if (!password.getText().toString().equals(confirmPassword.getText().toString()))
        {
            StateDialog(view.getContext(), "Confirm Password doesn't match with password! Please check");
            return;
        }

        JSONObject data = new JSONObject();

        try {
            data.put("accountID", accountID.getText());
            data.put("username", username.getText());
            data.put("phoneNum", phoneNum.getText());
            data.put("password", password.getText());
            data.put("name", name.getText());
            data.put("email", email.getText());
            data.put("address", address.getText());
            data.put("company", company.getText());
        } catch (JSONException e) {
            Toast.makeText(this.getContext(), e.toString(), Toast.LENGTH_SHORT);
        }

        RequestQueue queue = Volley.newRequestQueue(this.getContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                MainActivity.databaseLink + "accountUpdate.php",
                data,
                response -> {

                }, error -> {

        });

        queue.add(request);
    }

    private void setReturnBtn (View view)
    {
        getActivity().onBackPressed();
    }
}