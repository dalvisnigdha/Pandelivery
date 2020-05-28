package com.example.pandelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.Menu;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class UserMainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
// Locations to be added from Latitude and Longitude added in array list at the bootom
    ArrayList<LatLng> arrayList = new ArrayList<LatLng>();

    LatLng iit_delhi = new LatLng(28.5450,77.1926);
    LatLng gurgaon = new LatLng(28.4595,77.0266);
    LatLng cp = new LatLng(28.6304,77.2177);
    LatLng faridabad = new LatLng(28.4089,77.3178);
    LatLng indiagate = new LatLng(28.6129,77.2295);




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        ActionBar actionbar = getSupportActionBar();
//        actionbar.setTitle("User");

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    for(int i =0;i<arrayList.size();i++)
        {
            mMap.addMarker(new MarkerOptions().position(arrayList.get(i)).title("Marker"));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(2));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(arrayList.get(i)));
        }
    }
}
