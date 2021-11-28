package com.intertech.fyp.mobileapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.intertech.fyp.mobileapplication.databinding.ActivityAccSenderBinding;

import org.json.JSONException;

public class Acc_Sender extends AppCompatActivity {

    /*
    This is an activity with drawer
    Drawer is made of several components, appbar, header, navigator and etc

    Layouts under this class are:
    1. activity_acc_sender
    2. app_bar_acc_sender
    3. content_bar_acc_sender
    4. nav_header_acc_sender
    5. acc_sender
    6. activity_acc_sender_drawer
    7. mobile_navigation

    Classes involved directly under this class are:
    1. HomeFragment
    2. Products Fragment
    3. TransactionsFragment
    4. Acc_Sender
    */

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityAccSenderBinding binding;
    private final int requestCode = 101;

    TextView username, accountID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAccSenderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarAccSender.toolbar);

        //Attaches the floating button to a function
        binding.appBarAccSender.fab.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(Acc_Sender.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                //This requests permission to use camera
                //ActivityCompat.requestPermissions(SenderAcc.this, new String[] {Manifest.permission.CAMERA}, requestCode);
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestCode);
            } else {
                Intent i = new Intent(Acc_Sender.this, ScanQR.class);
                startActivity(i);
            }
        });

        //Binding the drawer to this class
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_products, R.id.nav_transactions, R.id.nav_devices, R.id.nav_profile)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_acc_sender);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);



    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.acc__sender, menu);


        NavigationView headerView = (NavigationView) findViewById(R.id.nav_view);

        try{
            username = headerView.findViewById(R.id.headerUsernameText);
            accountID = headerView.findViewById(R.id.headerAccountIDText);
            username.setText(MainActivity.senderDetails.getString("username"));
            accountID.setText(MainActivity.senderDetails.getString("accountID"));
        }catch (NullPointerException | JSONException e)
        {
            Toast.makeText(this, "acc_Sender.senderDetail error: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        //Navigates to the other pages by clicking the button
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_acc_sender);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //Todo Check if the handlers would pause or will not cause a memory leak
    @Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}