package com.example.pandelivery;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminStopsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Button signout;
    Button savestop;
    Button deletestop;
    Button backtowh;
    EditText inp_stop;
    EditText inp_stopqty;
    Spinner dropdown;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AdminStopsActivity";
    private static String warehouse = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_stops);
        savestop = findViewById(R.id.savestop);
        deletestop = findViewById(R.id.deletestop);
        signout = findViewById(R.id.signout);
        backtowh = findViewById(R.id.backtowh);
        inp_stop = findViewById(R.id.inp_stop);
        inp_stopqty = findViewById(R.id.inp_stopqty);
        dropdown = findViewById(R.id.spinnerwh);
        dropdown.setOnItemSelectedListener(this);

        String stop= inp_stop.getText().toString();
        String stopqty = inp_stopqty.getText().toString();
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                Intent I = new Intent(AdminStopsActivity.this, MainActivity.class);
                startActivity(I);
            }

        });

        savestop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
//            Save the data in stop and stopqty to the database
                final String stop_int= inp_stop.getText().toString();
                final String stopqty_int = inp_stopqty.getText().toString();

                db.collection("warehouse")
                        .whereEqualTo("warehouse", warehouse)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot query  = task.getResult();
                                    if(query.isEmpty()){
                                        Log.d(TAG, "Adding stops: ", task.getException());
                                        Toast.makeText(AdminStopsActivity.this, "Warehouse not found", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Log.d(TAG, "Updating documents: ", task.getException());
                                        for(DocumentSnapshot document : query.getDocuments()) {
                                            DocumentReference docRef = db.collection("warehouse").document(document.getId());
                                            Map<String,Object> data = document.getData();
                                            if(data.containsKey("stops")){
                                                updateItem(stop_int,stopqty_int,data,document.getId());
                                            }
                                            else{
                                                addItem(stop_int,stopqty_int,document.getId());
                                            }
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

                inp_stop.setText("");
                inp_stopqty.setText("");
            }

        });
        deletestop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String stop_int= inp_stop.getText().toString();
                final String stopqty_int = inp_stopqty.getText().toString();
//            Delete the data in stop and stopqty to the database
                db.collection("warehouse")
                        .whereEqualTo("warehouse",warehouse)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if(document.getData().containsKey("stops")){
                                            deleteItem(stop_int,stopqty_int,document);
                                        }
                                        else{
                                            Toast.makeText(AdminStopsActivity.this, "Stops not present", Toast.LENGTH_SHORT).show();
                                        }
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                    Toast.makeText(AdminStopsActivity.this, "Error getting document", Toast.LENGTH_SHORT).show();// snigdha added
                                }
                            }
                        });
                inp_stop.setText("");
                inp_stopqty.setText("");
            }
        });

        backtowh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                Intent I = new Intent(AdminStopsActivity.this, AdminMainActivity.class);
                startActivity(I);
            }

        });
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
         warehouse = (String) parent.getItemAtPosition(pos);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void addItem(String sname,String scap,String docId){
        List<String> stops = new ArrayList<String>();
        stops.add(sname+"#"+scap);
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

    public void updateItem(String sname, String scap, Map<String,Object> data, String docId){
        DocumentReference docRef = db.collection("warehouse").document(docId);
        String newStop = sname + "#" + scap;
        ArrayList<String> stops = (ArrayList<String>) data.get("stops");
        int index = -1;
        for(int i =0;i<stops.size();i++){
            if(stops.get(i).split("#")[0].equals(sname)){
                index = i;
                break;
            }
        }
        if(index==-1){
            stops.add(newStop);
        }
        else{
            stops.set(index,newStop);
        }

        docRef.update("stops", stops)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated in update!");
                        Toast.makeText(AdminStopsActivity.this, "Stop Added Successfully", Toast.LENGTH_SHORT).show();
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
