package com.intertech.fyp.mobileapplication.ui.map;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.intertech.fyp.mobileapplication.MainActivity;
import com.intertech.fyp.mobileapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

public class MapsFragment extends Fragment {

    String coord;
    Button returnBtn;
    TextView transactionID, productID, temperature, humidity, breachStatus ,coordText;
    Marker myMarker;
    GoogleMap mymap;
    RequestQueue queue;

    //Handler is the variable that handle a new thread (a.k.a. Runnable)
    Handler handler;
    final Runnable r = new Runnable() {
        @Override
        public void run() {
            Date currentTime = Calendar.getInstance().getTime();

            Context context = MapsFragment.this.getContext();

            JSONObject data = MainActivity.senderDetails;

            final String jsonBody = data.toString();

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    MainActivity.databaseLink + "deviceCoordGet.php",
                    response ->
                    {
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
                            Toast.makeText(context, "Runnable Error: " + e.toString(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(context, "Runnable Error: " + error.toString(), Toast.LENGTH_LONG).show()) {

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    try {
                        return jsonBody == null ? null : jsonBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        Toast.makeText(context, uee.toString(), Toast.LENGTH_LONG).show();
                        return null;
                    }
                }

            };

            queue.add(request);
            coordText.setText(coord);
            transactionID.setText(currentTime.toString());

            //Repeat the process until this fragment ends
            handler.postDelayed(this, 1000);
        }
    };

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        //Note : IF you wanna use the map, please create a new map Fragment/Activity to do so
        /**
         It will add a resource file named google_maps_api.xml to refer to the API key (thanks to Aaron)
         It will add a xml component (meta data) to refer to above document
         The rest is just the Google Map object and some zoom functions
         */
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {

            LatLng position = stringToLatLng(coord);

            mymap = googleMap;

            myMarker = googleMap.addMarker(new MarkerOptions().position(position).title("Current Marker Location"));
            mymap.moveCamera(CameraUpdateFactory.newLatLng(position));
            mymap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 20.0f));

            handler = new Handler();
            handler.postDelayed(r, 1000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        returnBtn = view.findViewById(R.id.returnButton);
        returnBtn.setOnClickListener(this::setReturnBtn);

        //Since I am using a fragment of fragment, this fragment is instantiated using navigation controller
        //The argument passed below can be referred to mobile_navigation.xml under the id of "nav_maps"
        Bundle bundle = getArguments();
        coord = bundle.getString("coord");

        transactionID = view.findViewById(R.id.mapsTransactionIDBox);
        temperature = view.findViewById(R.id.mapsTemperatureBox);
        humidity = view.findViewById(R.id.mapsHumidityBox);
        breachStatus = view.findViewById(R.id.mapsBreachStatusBox);
        coordText = view.findViewById(R.id.mapsCoordinatesBox);
        queue = Volley.newRequestQueue(MapsFragment.this.getContext());

        return view;
    }

    //This method is autogenerated
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

    //Ends current fragment and return to previous activity/fragment
    //Not really working..
    private void setReturnBtn(View view) {
        getActivity().onBackPressed();
    }

    public void onDetach()
    {
        handler.removeCallbacks(r);
        super.onDetach();
    }
}