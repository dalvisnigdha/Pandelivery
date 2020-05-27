package com.example.pandelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminStopsActivity extends AppCompatActivity {
    Button signout;
    Button savestop;
    Button deletestop;
    Button backtowh;
    EditText inp_stop;
    EditText inp_stopqty;
    Spinner dropdown;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AdminStopsActivity";
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

        String stop= inp_stop.getText().toString();
        String stopqty = inp_stopqty.getText().toString();

// Add list of warehouses here
        final List<String> items = new ArrayList<String>();
        final int counter = 1;
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

                inp_stop.setText("");
                inp_stopqty.setText("");
            }

        });
        deletestop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//            Delete the data in stop and stopqty to the database
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
}
