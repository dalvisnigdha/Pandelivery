package com.example.pandelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class UserMainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener,CheckDialog.CheckDialogListener {

    Button listbtn;
    private GoogleMap mMap;
    boolean[] checkeditems;
    ArrayList <Integer> useritem = new ArrayList<>(); // names of stops
    Button Donebtn;
    Button newtaskbtn;
    boolean working = false;
    Polyline polyline = null;
    ArrayList route;
    String[] listitems = {"gurgaon","cp","faridabad","indiagate"};//hard coded
    String warehouse = "iit_delhi";
    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    int warehousecap;
    ArrayList<Integer> maplocationcaplist = new ArrayList<Integer>();
    ArrayList<String> maplocationnamelist = new ArrayList<String>();

    ArrayList<RPoint> routeList;

    // Locations to be added from Latitude and Longitude added in array list at the bootom
    ArrayList<LatLng> maplocationList = new ArrayList<LatLng>();

//    LatLng iit_delhi = new LatLng(28.5450, 77.1926);
//    LatLng gurgaon = new LatLng(28.4595, 77.0266);
//    LatLng cp = new LatLng(28.6304, 77.2177);
//    LatLng faridabad = new LatLng(28.4089, 77.3178);
//    LatLng indiagate = new LatLng(28.6129, 77.2295);
    // Map Objects
    UiSettings mUiSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        listbtn = findViewById(R.id.listbtn);
        Donebtn = findViewById(R.id.Donebtn);
        newtaskbtn = findViewById(R.id.newtaskbtn);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        user = firebaseAuth.getCurrentUser();
        addRouteListener();

        // Check Permission
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("User");

        // ArrayList updated :
//        maplocationList.add(iit_delhi);
//        maplocationList.add(gurgaon);
//        maplocationList.add(cp);
//        maplocationList.add(faridabad);
//        maplocationList.add(indiagate);

        // Array list of checkbox
//            listitems = getResources().getStringArray(R.array.stopslist);
        checkeditems = new boolean[listitems.length];
        newtaskbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                working = true;
                if(polyline != null) polyline.remove();
                PolylineOptions polylineOptions = new PolylineOptions().addAll(maplocationList).clickable(true);
                polyline = mMap.addPolyline(polylineOptions);
                polyline.setColor(Color.rgb(102,178,255));
                Log.d("route tag", "route path "+maplocationList);

            }
        });

        listbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (working) {
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(UserMainActivity.this);
                    mBuilder.setTitle("Warehouse-"+warehouse);
                    mBuilder.setMultiChoiceItems(listitems, checkeditems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                            if (isChecked) {
                                if (!useritem.contains(position)) {
                                    useritem.add(position);
                                    Log.d("my tag", "add " + useritem.toString());
                                }
                            } else if (useritem.contains(position)) {
                                useritem.remove(new Integer(position));
                            }

                        }
                    });
                    mBuilder.setCancelable(false);
                    mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String item = "";
                            for (int i = 0; i < useritem.size(); i++) {
                                item = item + listitems[useritem.get(i)];
                                if (i != useritem.size() - 1) {
                                    item = item + ',';
                                }

                            }
                        }
                    });
                    mBuilder.setNeutralButton("Clear all", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (int i = 0; i < checkeditems.length; i++) {
                                checkeditems[i] = false;
                                useritem.clear();

                            }
                        }
                    });
                    AlertDialog mDialog = mBuilder.create();
                    mDialog.show();


                }

            }
        });

        Donebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(working){
                if (listitems.length == useritem.size()) {
                    Dialogdone();
                    working = false;

                } else {
                    Dialogcheck();
                }
            }
            }
        });

    }

    public void addRouteListener(){
        String uid = user.getUid();
        final DocumentReference docRef = db.collection("users").document(uid);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Firestore Route", "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Map user_data = snapshot.getData();
                    Log.d("Firestore Route", source + " data: " + user_data);
                    if ( ((Long)user_data.get("assigned")).intValue() == 1){
                        Log.d("Firestore Route", "Route assigned and fetched");
                        // Populate routeList
                        ArrayList stopsList = (ArrayList)user_data.get("stops");
                        ArrayList routeIndex = (ArrayList)user_data.get("route");
                        routeList = new ArrayList<RPoint>();
                        for (Object index : routeIndex){
                            int ind = ((Long)index).intValue();
                            String entry = (String)stopsList.get(ind);
                            String[] vals = entry.split("#");
                            RPoint obj = new RPoint(vals[0], Integer.parseInt(vals[1]), new LatLng(Double.parseDouble(vals[2]), Double.parseDouble(vals[3])));
                            routeList.add(obj);
                        }
                        // User routeList to draw on Map - SNIGDHA
                        // SNIGDHA OLD CODE
//                        route = (ArrayList)user_data.get("route");    // Array having routes
//                        GeoPoint pt;       // Access array element
//                        LatLng mappt;// Convert to latlng for display on map
//                        for(int i =0;i<route.size();i++)
//                        {
//                            pt = (GeoPoint)route.get(i);
//                            mappt = new LatLng(pt.getLatitude(),pt.getLongitude());
//                            maplocationList.add(mappt);
//
//                        }
                    }else{
                        // DO NOTHING
                        Log.d("Firestore Route", "Route not assigned");
                    }
                } else {
                    Log.d("Firestore Route", source + " data: null");
                }
            }
        });
    }

    class RPoint {
        String name;
        int capacity;
        LatLng location;

        public RPoint(String nm, int cap, LatLng loc){
            name = nm;
            capacity = cap;
            location = loc;
        }
    };
    public void Dialogdone(){
      DonDialog dialogdone = new DonDialog();
      dialogdone.show(getSupportFragmentManager(),"completed dialog");
    }

    public void Dialogcheck()
    {
        CheckDialog dialogcheck = new CheckDialog();
        dialogcheck.show(getSupportFragmentManager(),"check dialog");
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

            for (int i = 0; i < maplocationList.size(); i++) {
                if (i == 0) {
                    mMap.addMarker(new MarkerOptions().position(maplocationList.get(i)).title("Warehouse-" + warehouse));
                } else {
                    mMap.addMarker(new MarkerOptions().position(maplocationList.get(i)).title("Stop " + i));
                }

                mMap.animateCamera(CameraUpdateFactory.zoomTo(21));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(maplocationList.get(i)));
            }


        // Map UI Settings
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
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
                FirebaseAuth.getInstance().signOut();
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
            if(id==R.id.help)
            {
                Intent I = new Intent(UserMainActivity.this, HelpActivity.class);
                startActivity(I);
                return false;
            }

            return true;
        }

    @Override
    public void onYesClicked() {
        working = false;
    }
}




