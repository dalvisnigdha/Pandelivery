package com.example.pandelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AdminMainActivity extends AppCompatActivity {
    Button signout;
    Button addstops;
    EditText inp_wh;
    EditText inp_whcap;
    Button savewh;
    Button deletewh;

    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AdminMainActivity";

//These variables name can be same or different to the ids
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        signout = findViewById(R.id.signout);
        addstops = findViewById(R.id.addstops);
        inp_wh = findViewById(R.id.inp_wh);
        inp_whcap = findViewById(R.id.inp_whcap);
        savewh = findViewById(R.id.savewh);
        deletewh = findViewById(R.id.deletewh);

        final String wh= inp_wh.getText().toString();
        final String whcap = inp_whcap.getText().toString();

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                Intent I = new Intent(AdminMainActivity.this, MainActivity.class);
                startActivity(I);
            }

        });
        addstops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                Intent I = new Intent(AdminMainActivity.this, AdminStopsActivity.class);
                startActivity(I);
            }

        });

        savewh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
//            Save the data in wh and whcap to the database
                final String wh_int= inp_wh.getText().toString();
                final String whcap_int = inp_whcap.getText().toString();
                Map<String, Object> warehouse = new HashMap<>();
                warehouse.put("warehouse", wh_int);
                warehouse.put("warehouse_cap", whcap_int);
                db.collection("warehouse")
                        .add(warehouse)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
                inp_wh.setText("");
                inp_whcap.setText("");
            }

        });
        deletewh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//            Delete the data in wh and whcap to the database

                Log.d(TAG, "Deleting warehouse");
                final String wh_int= inp_wh.getText().toString();
                final String whcap_int = inp_whcap.getText().toString();
                db.collection("warehouse")
                        .whereEqualTo("warehouse",wh_int)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        db.collection("warehouse").document(document.getId())
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error deleting document", e);
                                                    }
                                                });
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                inp_wh.setText("");
                inp_whcap.setText("");
            }
        });
    }
}
