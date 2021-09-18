package com.ol.multiaddressmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed,
            tv_sensor, tv_updates, tv_address, tv_wayPointCounts;
    Button btn_newWayPoint, btn_showWayPointList,  btn_remove , btn_showMap, btn_showAddress, btn_addAddress;
    Switch sw_locationupdates, sw_gps;

    boolean updateOn = false;
    Location currentLocation;
    List<Location> savedLocations;

    //Google's API
    FusedLocationProviderClient fusedLocationProviderClient;

    //Location request is a config file for FusedLocation
    LocationRequest locationRequest;
    LocationCallback locationCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        tv_wayPointCounts = findViewById(R.id.tv_countOfCrumbs);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);
        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWayPointList = findViewById(R.id.btn_showWayPointList);
        btn_remove = findViewById(R.id.btn_remove);
        btn_showMap = findViewById(R.id.btn_showMap);
        btn_showAddress = findViewById(R.id.btn_addAddress);
        btn_addAddress = findViewById(R.id.btn_addAddress);




        //location request
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationCallBack = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                startLocationUpdates();
            }
        };   //end locationcallback

        btn_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              MyApplication myApplication = (MyApplication) getApplicationContext();
              savedLocations = myApplication.getMyLocations();
              savedLocations.add(currentLocation);
                tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));
            }
        });

        btn_showWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ShowSavedLocationList.class);
                startActivity(i);
            }
        });


        btn_addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,ShowAddressForm.class);
                startActivity(i);
            }
        });

btn_showAddress.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent i = new Intent(MainActivity.this,ShowAddressForm.class);
        startActivity(i);
    }
});


        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication myApplication = (MyApplication) getApplicationContext();
                savedLocations = myApplication.getMyLocations();
                savedLocations.clear();
                startLocationUpdates();
            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,MapsActivity.class );
                        startActivity(i);

            }
        });


        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText("Using Towers + WIFI");
                }
            }
        });


        sw_locationupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationupdates.isChecked()) {
                    startLocationUpdates();
                } else {
                    //turn off tracking
                    stopLocationUpdates();
                }
            }
        });

        updateGPS();

    }  //end onCreate Method

    @SuppressLint("MissingPermission")
    private void stopLocationUpdates() {
        tv_updates.setText("lol");
        tv_lat.setText("Not tracking location");
        tv_lon.setText("Not tracking location");
        tv_speed.setText("Not tracking location");
        tv_address.setText("Not tracking location");
        tv_accuracy.setText("Not tracking location");
        tv_altitude.setText("Not tracking location");
        tv_sensor.setText("Not tracking location");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }  //end stopLocationUpdates()


    private void startLocationUpdates() {
        tv_updates.setText("Location is being tracked");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null);
        updateGPS();
    } //end startLoationUpdates()

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    updateGPS();
                }
                else{
                    Toast.makeText(this, "This app requires permission to be granted in settings ", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    ;  //end onCreate method


    private void updateGPS(){
      fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
      if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
       fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
           updateUIValues(location);
           currentLocation = location;

       });
      }
      else{
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
              requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION } , PERMISSION_FINE_LOCATION);

          }
      }
    } //end update GPS

    private void updateUIValues(Location location) {
       tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if (location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else{
            tv_altitude.setText("Not available");
        }

        if (location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else{
            tv_speed.setText("Not available");
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getSubAdminArea() + " " + addresses.get(0).getPostalCode());
        }
        catch(Exception e){
           tv_address.setText("Unable to get street address , etc ");
        }
        MyApplication myApplication = (MyApplication) getApplicationContext();
        savedLocations = myApplication.getMyLocations();
        tv_wayPointCounts.setText(Integer.toString(savedLocations.size()));
    } //end updateUIV


} //end Main Activity class


