package com.intertech.fyp.mobileapplicationcourier;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.intertech.fyp.mobileapplicationcourier.databinding.ActivityAccCourierBinding;

public class Acc_Courier extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityAccCourierBinding binding;
    private final int requestCode = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAccCourierBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarAccReceiver.toolbar);
        binding.appBarAccReceiver.fab.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(Acc_Courier.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                //This requests permission to use camera
                //ActivityCompat.requestPermissions(SenderAcc.this, new String[] {Manifest.permission.CAMERA}, requestCode);
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestCode);
            } else {
                Intent i = new Intent(Acc_Courier.this, ScanQR.class);
                startActivity(i);
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_transactions, R.id.nav_profile, R.id.nav_scanQR)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_acc_courier);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.acc_courier, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_acc_courier);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}