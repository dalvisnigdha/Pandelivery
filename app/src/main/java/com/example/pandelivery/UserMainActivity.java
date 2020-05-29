package com.example.pandelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class UserMainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {
//    Button signout;
    private GoogleMap mMap;
    // Locations to be added from Latitude and Longitude added in array list at the bootom
    ArrayList<LatLng> arrayList = new ArrayList<LatLng>();

    LatLng iit_delhi = new LatLng(28.5450, 77.1926);
    LatLng gurgaon = new LatLng(28.4595, 77.0266);
    LatLng cp = new LatLng(28.6304, 77.2177);
    LatLng faridabad = new LatLng(28.4089, 77.3178);
    LatLng indiagate = new LatLng(28.6129, 77.2295);


    // Map Objects

    UiSettings mUiSettings;
        // Map Objects

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        // Check Permission
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Maps

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        signout = findViewById(R.id.signout);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("User");

        // ArrayList updated :
        arrayList.add(iit_delhi);
        arrayList.add(gurgaon);
        arrayList.add(cp);
        arrayList.add(faridabad);
        arrayList.add(indiagate);


        //        signout = findViewById(R.id.signout);


//        signout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick (View view){
//                Intent I = new Intent(UserMainActivity.this, MainActivity.class);
//                startActivity(I);
//            }
//
//        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {        // Check Permission
        mMap = googleMap;
        // Map Settings
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        }

        for (int i = 0; i < arrayList.size(); i++) {
            if (i==0)
            {
                mMap.addMarker(new MarkerOptions().position(arrayList.get(i)).title("Warehouse"));
            }
            else
            {
                mMap.addMarker(new MarkerOptions().position(arrayList.get(i)).title("Stop "+i));
            }

                mMap.animateCamera(CameraUpdateFactory.zoomTo(2));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(arrayList.get(i)));
            }


        // Map UI Settings
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        //        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        //        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
        //        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
        //            @Override
        //            public boolean onMarkerClick(Marker marker) {
        ////                int position = (int)(marker.getTag());
        ////                Toast.makeText(con, ""+marker.getPosition(), Toast.LENGTH_LONG).show();
        //                return false;
        //            }
        //        });


    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    // Check Location Permissions
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
            return false;
        } else {
            return true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.signout)
        {
            Intent I = new Intent(UserMainActivity.this, MainActivity.class);
            startActivity(I);
            return false;
        }
        if(id==R.id.List_View)
        {
            Intent I = new Intent(UserMainActivity.this, ListViewActivity.class);
            startActivity(I);
            return false;
        }
        return true;
    }

}




