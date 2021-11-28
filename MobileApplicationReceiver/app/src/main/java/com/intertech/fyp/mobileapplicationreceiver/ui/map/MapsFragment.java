package com.intertech.fyp.mobileapplicationreceiver.ui.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.intertech.fyp.mobileapplicationreceiver.MainActivity;
import com.intertech.fyp.mobileapplicationreceiver.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

public class MapsFragment extends Fragment {

    Button returnBtn;
    TextView transactionID, productID, temperature, humidity, breachStatus ,coordText;
    Marker myMarker;
    GoogleMap mymap;
    RequestQueue queue;
    String transactionDetails;
    Handler handler;

    final Runnable r = new Runnable() {
        @Override
        public void run() {
            if (transactionDetails != null)
            {
                final String jsonBody = MainActivity.receiverDetails.toString();

                StringRequest request = new StringRequest(Request.Method.POST,
                        MainActivity.databaseLink + "deviceCoordGet.php",
                        response -> {
                            try {
                                JSONArray arr = new JSONArray(response);
                                JSONObject obj = (JSONObject) arr.get(0);
                                temperature.setText(obj.getString("dhtTempData"));
                                humidity.setText(obj.getString("dhtHumidityData"));

                                int lightValue = obj.getInt("breachStatus");

                                if (lightValue < 50)
                                    breachStatus.setText("Safe");
                                else
                                    breachStatus.setText("Danger");

                                coordText.setText(obj.getString("gpsData"));

                                LatLng position = stringToLatLng(obj.getString("gpsData"));
                                myMarker.setPosition(position);
                                mymap.moveCamera(CameraUpdateFactory.newLatLng(position));
                                mymap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 20.0f));

                            } catch (JSONException e) {
                                Toast.makeText(MapsFragment.this.getContext(), "Maps runnable response: " + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        },
                        error -> Toast.makeText(MapsFragment.this.getContext(), "Maps runnable: " + error.toString(), Toast.LENGTH_LONG).show()
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
                            Toast.makeText(MapsFragment.this.getContext(), uee.toString(), Toast.LENGTH_LONG).show();
                            return null;
                        }
                    }
                };

                queue.add(request);
            }
            Date currentTime = Calendar.getInstance().getTime();
            transactionID.setText(currentTime.toString());
            handler.postDelayed(this, 2000);
        }
    };

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng startingPoint = new LatLng(3.139, 101.6869);
            mymap = googleMap;
            myMarker = googleMap.addMarker(new MarkerOptions().position(startingPoint).title("Current Marker Location"));

            mymap.moveCamera(CameraUpdateFactory.newLatLng(startingPoint));
            mymap.animateCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, 20.0f));

            handler = new Handler();
            handler.postDelayed(r, 1000);

            /*
            Geofence geofence = new Geofence()
                    .getRequestId();
            */
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        transactionDetails = getArguments().getString("transactionInfo");

        queue = Volley.newRequestQueue(MapsFragment.this.getContext());

        transactionID = view.findViewById(R.id.mapsTransactionIDBox);
        productID = view.findViewById(R.id.mapsProductIDBox);
        temperature = view.findViewById(R.id.mapsTemperatureBox);
        humidity = view.findViewById(R.id.mapsHumidityBox);
        breachStatus = view.findViewById(R.id.mapsBreachStatusBox);
        coordText = view.findViewById(R.id.mapsCoordinatesBox);

        try {
            JSONObject obj = new JSONObject(transactionDetails);
            transactionID.setText(obj.getString("transactionID"));
            productID.setText(obj.getString("productID"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        returnBtn = view.findViewById(R.id.returnButton);
        returnBtn.setOnClickListener(this::setReturnBtn);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    //Changes string to LatLng object for googleMap to feed on
    private LatLng stringToLatLng(String coordinate) {
        String[] post = coordinate.split(",");
        double lat = Double.parseDouble(post[0]);
        double lng = Double.parseDouble(post[1]);

        return new LatLng(lat, lng);
    }


    private void setReturnBtn(View view)
    {
        getActivity().onBackPressed();
    }

    public void onDetach()
    {
        handler.removeCallbacks(r);
        super.onDetach();
    }
}