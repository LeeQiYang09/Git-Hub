package com.intertech.fyp.mobileapplication.ui.devices;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.intertech.fyp.mobileapplication.ui.map.MapsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class DevicesFragment extends Fragment {

    TableLayout devicesTable;
    JSONArray arr = null;
    List<JSONObject> objList = new ArrayList<>();
    Button trackAllBtn;

    public DevicesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        trackAllBtn = view.findViewById(R.id.deviceTrackButton);
        devicesTable = view.findViewById(R.id.devicesTable);

        populateTable(view, devicesTable);
        // Inflate the layout for this fragment
        return view;
    }

    //String request to get IoTB data from DB, similar populateTableString() in Products Fragment
    private void populateTable(View v, TableLayout tableLayout)
    {
        RequestQueue queue = Volley.newRequestQueue(v.getContext());

        JSONObject data = MainActivity.senderDetails;

        final String jsonBody = data.toString();

        StringRequest request = new StringRequest(
            Request.Method.POST,
            MainActivity.databaseLink + "deviceCoordGet.php",
            response ->
            {
                try {
                    arr = new JSONArray(response);

                    if (arr != null) {
                        boolean white = false;
                        JSONObject obj;

                        for (int i = 0; i < arr.length(); i++) {
                            obj = arr.getJSONObject(i);
                            objList.add(obj);
                            TextView deviceID = new TextView(v.getContext());
                            TextView dateTime = new TextView(v.getContext());
                            TextView breachStatus = new TextView(v.getContext());
                            TextView dhtTempData = new TextView(v.getContext());
                            TextView dhtHumidityData = new TextView(v.getContext());
                            TextView gpsCoordinate = new TextView(v.getContext());

                            TableRow.LayoutParams params = new TableRow.LayoutParams(150, TableRow.LayoutParams.WRAP_CONTENT, 10f);

                            deviceID.setText(obj.getString("deviceID"));
                            deviceID.setGravity(Gravity.CENTER);

                            dateTime.setText(obj.getString("dateTime"));
                            dateTime.setLayoutParams(params);
                            dateTime.setGravity(Gravity.CENTER);

                            int safe =obj.getInt("breachStatus");

                            if (safe < 50)
                                breachStatus.setText("Safe");
                            else
                                breachStatus.setText("Breached");

                            breachStatus.setGravity(Gravity.CENTER);

                            dhtTempData.setText(obj.getString("dhtTempData"));
                            dhtTempData.setGravity(Gravity.CENTER);

                            dhtHumidityData.setText(obj.getString("dhtHumidityData"));
                            dhtHumidityData.setGravity(Gravity.CENTER);

                            gpsCoordinate.setText(obj.getString("gpsData"));
                            gpsCoordinate.setGravity(Gravity.CENTER);


                            int size = 18   ;
                            deviceID.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                            dateTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                            breachStatus.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                            dhtTempData.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                            dhtHumidityData.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
                            gpsCoordinate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);

                            TableRow tr = new TableRow(v.getContext());
                            tr.addView(deviceID);
                            tr.addView(dateTime);
                            tr.addView(breachStatus);
                            tr.addView(dhtTempData);
                            tr.addView(dhtHumidityData);
                            tr.addView(gpsCoordinate);

                            deviceID.setTextColor(Color.parseColor("#000000"));
                            dateTime.setTextColor(Color.parseColor("#000000"));
                            breachStatus.setTextColor(Color.parseColor("#000000"));
                            dhtTempData.setTextColor(Color.parseColor("#000000"));
                            dhtHumidityData.setTextColor(Color.parseColor("#000000"));
                            gpsCoordinate.setTextColor(Color.parseColor("#000000"));

                            if (!white) {
                                tr.setBackgroundResource(R.color.gray);
                                white = !white;
                            } else {
                                tr.setBackgroundResource(R.color.white);
                                white = !white;
                            }
                            tr.setId(R.id.reservedNamedId + i);
                            tr.setOnClickListener(this::rowClick);


                            tableLayout.addView(tr);
                        }
                    }

                } catch (JSONException e) {
                    Toast.makeText(v.getContext(), "Device table error: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            },
            error -> Toast.makeText(v.getContext(), "Devices Table Error: " + error.toString(), Toast.LENGTH_LONG).show())
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
                    Toast.makeText(v.getContext(), uee.toString(), Toast.LENGTH_LONG).show();
                    return null;
                }
            }

        };

        queue.add(request);
    }

    //Similar to rowClick() in Products Fragment
    //But it can navigate to a new fragment which has google map
    private void rowClick(View view) {

        int indexNum = R.id.reservedNamedId;

        Bundle bundle = new Bundle();
        for (int i = 0; i < objList.size(); i++)
        {
            try{
                if (indexNum == indexNum + i)
                {
                    //Bundle is used to pass coordinate between Fragments
                    bundle.putString("coord", objList.get(i).getString("gpsData"));

                    //Instantiate and navigate to the maps_fragment via link in mobile_navigation.xml
                    Navigation.findNavController(view).navigate(R.id.action_nav_devices_to_nav_map, bundle);
                }
            }catch (JSONException e)
            {
                Toast.makeText(view.getContext(), "DeviceFragment.rowClick(): " + e.toString(),
                        Toast.LENGTH_LONG);
            }
        }

    }
}