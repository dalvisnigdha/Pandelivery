package com.example.pandelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
//import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AdminStopsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Button savestop;
    Button deletestop;
    EditText inp_stop;
    EditText inp_stopqty;
    Spinner dropdown;
    EditText inp_stopLat;
    EditText inp_stopLong;
    Button computepath;
    int flag = 0;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AdminStopsActivity";
    private static String warehouse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stops);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Admin");
        savestop = findViewById(R.id.savestop);
        deletestop = findViewById(R.id.deletestop);
        inp_stop = findViewById(R.id.inp_stop);
        inp_stopqty = findViewById(R.id.inp_stopqty);
        dropdown = findViewById(R.id.spinnerwh);
        inp_stopLat = findViewById(R.id.inp_stopLat);
        inp_stopLong = findViewById(R.id.inp_stopLong);
        computepath = findViewById(R.id.computepath);
        dropdown.setOnItemSelectedListener(this);

        String stop= inp_stop.getText().toString();
        String stopqty = inp_stopqty.getText().toString();
        final String stopLat = inp_stopLat.getText().toString();
        final String stopLong = inp_stopLong.getText().toString();

        // Add list of warehouses here
        //        String[] items = new String[]{"Choose a warehouse","1","2","3","4"};
        final List<String> items = new ArrayList<String>();
        items.add("Choose a warehouse");
        db.collection("warehouse")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String warehouse_name = document.getString("warehouse");
                                items.add(warehouse_name);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

// in this block
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);


        computepath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                flag = 1;
                db.collection("warehouse")
                .whereEqualTo("warehouse", warehouse)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot query  = task.getResult();
                            if(query.isEmpty()){
                                Log.d(TAG, "Warehouse not found: ", task.getException());
                                Toast.makeText(AdminStopsActivity.this, "Warehouse not found", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Log.d(TAG, "Updating documents: ", task.getException());
                                for(DocumentSnapshot document : query.getDocuments()) {
                                    DocumentReference docRef = db.collection("warehouse").document(document.getId());
//                                    Map<String,Object> data = document.getData(); // not used
                                    docRef.update("runVRP", 1)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "DocumentSnapshot successfully updated in update!");
                                                    Toast.makeText(AdminStopsActivity.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error updating document in update", e);
                                                    Toast.makeText(AdminStopsActivity.this, "Request Failed!", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
            }

        });
        savestop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
//            Save the data in stop and stopqty to the database
                // Add stops Latitude and Longitude Here
                final String stop_int= inp_stop.getText().toString();
                final String stopqty_int = inp_stopqty.getText().toString();
                final String stopLat_int = inp_stopLat.getText().toString();
                final String stopLong_int = inp_stopLong.getText().toString();

                if(warehouse.equals("Choose a warehouse")){
                    Toast.makeText(AdminStopsActivity.this, "Select a warehouse first", Toast.LENGTH_SHORT).show();
                }
                else{
                    db.collection("warehouse")
                        .whereEqualTo("warehouse", warehouse)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot query  = task.getResult();
                                if(query.isEmpty()){
                                    Log.d(TAG, "Warehouse not found: ", task.getException());
                                    Toast.makeText(AdminStopsActivity.this, "Warehouse not found", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Log.d(TAG, "Updating documents: ", task.getException());
                                    for(DocumentSnapshot document : query.getDocuments()) {
                                        DocumentReference docRef = db.collection("warehouse").document(document.getId());
                                        Map<String,Object> data = document.getData();
                                        if(data.containsKey("stops")){
                                            updateItem(stop_int,stopqty_int,stopLat_int,stopLong_int,data,document.getId());
                                        }
                                        else{
                                            addItem(stop_int,stopqty_int,stopLat_int,stopLong_int,document.getId());
                                        }
                                    }
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                            }
                        });
                }
            }

        });
        deletestop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String stop_int= inp_stop.getText().toString();
                final String stopqty_int = inp_stopqty.getText().toString();
                final String stopLat_int = inp_stopLat.getText().toString();
                final String stopLong_int = inp_stopLong.getText().toString();
//            Delete the Latitude and Longitude of stops from database
                if(warehouse.equals("Choose a warehouse")){
                    Toast.makeText(AdminStopsActivity.this, "Select a warehouse first", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.collection("warehouse")
                        .whereEqualTo("warehouse",warehouse)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if(document.getData().containsKey("stops")){
                                            deleteItem(stop_int,stopqty_int, document);
                                        }
                                        else{
                                            Toast.makeText(AdminStopsActivity.this, "Stops not present", Toast.LENGTH_SHORT).show();
                                        }
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                    Toast.makeText(AdminStopsActivity.this, "Error getting document", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });



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
            Intent I = new Intent(AdminStopsActivity.this, MainActivity.class);
            startActivity(I);
            finish();
            return false;
        }else if(id==R.id.List_View)
        {
            Intent I = new Intent(AdminStopsActivity.this, ListViewActivity.class);
            startActivity(I);
            return false;
        }else if (id == android.R.id.home)      // IMPORTANT: Needed for back button if using action bar at the top
        {
            onBackPressed();
            return true;
        }
        if(id==R.id.help)
        {
            Intent I = new Intent(AdminStopsActivity.this, HelpActivity.class);
            startActivity(I);
            return false;
        }
        return true;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
         warehouse = (String) parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void addItem(String sname,String scap,String slat, String slong,String docId){
        List<String> stops = new ArrayList<String>();
        if(sname.isEmpty() || scap.isEmpty() || slat.isEmpty() || slong.isEmpty()){
            Toast.makeText(AdminStopsActivity.this, "One or more fields empty!", Toast.LENGTH_SHORT).show();
        }
        else{
            inp_stop.setText("");
            inp_stopqty.setText("");
            inp_stopLat.setText("");
            inp_stopLong.setText("");
            stops.add(sname+"#"+scap+"#"+slat+"#"+slong);
            DocumentReference docRef = db.collection("warehouse").document(docId);
            docRef.update("stops", stops)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated in add!");
                        Toast.makeText(AdminStopsActivity.this, "Stop Added Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document in add", e);
                        Toast.makeText(AdminStopsActivity.this, "Error adding stop", Toast.LENGTH_SHORT).show();
                    }
                });
        }

    }

    public void updateItem(String sname, String scap,String slat, String slong, Map<String,Object> data, String docId){
        if(sname.isEmpty()){
            Toast.makeText(AdminStopsActivity.this, "Stop name empty", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference docRef = db.collection("warehouse").document(docId);
        ArrayList<String> stops = (ArrayList<String>) data.get("stops");
        int index = -1;
        for(int i =0;i<stops.size();i++){
            if(stops.get(i).split("#")[0].equals(sname)){
                index = i;
                break;
            }
        }
        if(index==-1){
            if(sname.isEmpty() || scap.isEmpty() || slat.isEmpty() || slong.isEmpty()){
                Toast.makeText(AdminStopsActivity.this, "One or more fields empty", Toast.LENGTH_SHORT).show();
            }
            else{
                stops.add(sname+"#"+scap+"#"+slat+"#"+slong);
            }
        }
        else{
            String[] prevStop = stops.get(index).split("#");
            String updateStop = prevStop[0];
            if(!scap.isEmpty()){
                updateStop += ("#"+scap);
            }
            else{
                updateStop += ("#"+prevStop[1]);
            }
            if(!slat.isEmpty()){
                updateStop += ("#"+slat);
            }
            else{
                updateStop += ("#"+prevStop[2]);
            }
            if(!slong.isEmpty()){
                updateStop += ("#"+slong);
            }
            else{
                updateStop += ("#"+prevStop[3]);
            }
            stops.set(index,updateStop);
        }
        inp_stop.setText("");
        inp_stopqty.setText("");
        inp_stopLat.setText("");
        inp_stopLong.setText("");
        docRef.update("stops", stops)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot successfully updated in update!");
                    Toast.makeText(AdminStopsActivity.this, "Stop Updated Successfully", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error updating document in update", e);
                    Toast.makeText(AdminStopsActivity.this, "Error adding stop", Toast.LENGTH_SHORT).show();
                }
            });
    }

    public void deleteItem(String sname, String scap,QueryDocumentSnapshot document){
        if(sname.isEmpty()){
            Toast.makeText(AdminStopsActivity.this, "Stop name empty", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference docRef = db.collection("warehouse").document(document.getId());
        ArrayList<String> stops = (ArrayList<String>) document.getData().get("stops");
        int index = -1;
        for(int i =0;i<stops.size();i++){
            if(stops.get(i).split("#")[0].equals(sname)){
                index = i;
                break;
            }
        }
        if(index==-1){
            Toast.makeText(AdminStopsActivity.this, "Stop not found", Toast.LENGTH_SHORT).show();
        }
        else{
            inp_stop.setText("");
            inp_stopqty.setText("");
            inp_stopLat.setText("");
            inp_stopLong.setText("");
            stops.remove(index);
            docRef.update("stops", stops)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated in delete!");
                        Toast.makeText(AdminStopsActivity.this, "Stop deleted Successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        Toast.makeText(AdminStopsActivity.this, "Error deleting stop", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

}
