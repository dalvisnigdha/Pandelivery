package com.example.pandelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.view.Menu;

public class AdminMainActivity extends AppCompatActivity {
    Button computepath;
    Button addstops;
    EditText inp_wh;
    EditText inp_whcap;
    Button savewh;
    Button deletewh;
    EditText inp_whLat;
    EditText inp_whLong;
    int flag = 0;
//    TextView listviewwhtxt;

    // Access a Cloud Firestore instance from your Activity
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AdminMainActivity";

//These variables name can be same or different to the ids
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Admin");

//        listviewwhtxt = findViewById(R.id.listviewwhtxt);
        addstops = findViewById(R.id.addstops);
        inp_wh = findViewById(R.id.inp_wh);
        inp_whcap = findViewById(R.id.inp_whcap);
        savewh = findViewById(R.id.savewh);
        deletewh = findViewById(R.id.deletewh);
        inp_whLat = findViewById(R.id.inp_whLat);
        inp_whLong = findViewById(R.id.inp_whLong);
        computepath = findViewById(R.id.computepath);

        final String wh= inp_wh.getText().toString();
        final String whcap = inp_whcap.getText().toString();
        final String whLat = inp_whLat.getText().toString();
        final String whLong = inp_whLong.getText().toString();


//        listviewwhtxt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick (View view){
//                Intent I = new Intent(AdminMainActivity.this, ListViewActivity.class);
//                startActivity(I);
//            }
//
//        });

        computepath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                flag = 1;
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
                // Add data whLat and whLong to the data base
                final String wh_int= inp_wh.getText().toString();
                final String whcap_int = inp_whcap.getText().toString();
                final String whLat_int = inp_whLat.getText().toString();
                final String whLong_int = inp_whLong.getText().toString();
                if(wh_int.isEmpty()){
                    Toast.makeText(AdminMainActivity.this, "Warehouse name empty!", Toast.LENGTH_SHORT).show();
                }
                else{
                    db.collection("warehouse")
                        .whereEqualTo("warehouse", wh_int)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot query  = task.getResult();
                                if(query.isEmpty()){
                                    Log.d(TAG, "Adding documents: ", task.getException());
                                    addItem(wh_int,whcap_int,whLat_int,whLong_int);
                                }
                                else{
                                    Log.d(TAG, "Updating documents: ", task.getException());
                                    updateItem(wh_int,whcap_int,whLat_int,whLong_int,query.getDocuments());
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                            }
                        });
                }
            }

        });
        deletewh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//            Delete the data in wh and whcap to the database
                // Delete data whLat and whLong to the data base

                Log.d(TAG, "Deleting warehouse");
                final String wh_int= inp_wh.getText().toString();
                final String whcap_int = inp_whcap.getText().toString();
                if(wh_int.isEmpty()){
                    Toast.makeText(AdminMainActivity.this, "Warehouse name empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    db.collection("warehouse")
                        .whereEqualTo("warehouse",wh_int)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot query  = task.getResult();
                                    if(query.isEmpty()){
                                        Toast.makeText(AdminMainActivity.this, "Warehouse not found", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            deleteItem(document);
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                    Toast.makeText(AdminMainActivity.this, "Error getting document", Toast.LENGTH_SHORT).show();// snigdha added
                                }
                            }
                        });
                }
            }
        });
    }
// For Action Bar menu
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
            Intent I = new Intent(AdminMainActivity.this, MainActivity.class);
            startActivity(I);
            return false;
        }
        if(id==R.id.List_View)
        {
            Intent I = new Intent(AdminMainActivity.this, ListViewActivity.class);
            startActivity(I);
            return false;
        }
        return true;
    }


    public void addItem(String wname,String wcap, String wlat, String wlong){
        Map<String, Object> warehouse = new HashMap<>();
        warehouse.put("warehouse", wname);
        if(wcap.isEmpty() || wname.isEmpty() || wlat.isEmpty() || wlong.isEmpty()){
            Toast.makeText(AdminMainActivity.this, "One or more fields empty!", Toast.LENGTH_SHORT).show();
        }
        else{
            inp_wh.setText("");
            inp_whcap.setText("");
            inp_whLat.setText("");
            inp_whLong.setText("");
            warehouse.put("warehouse_cap", wcap);
            warehouse.put("warehouse_lat",wlat);
            warehouse.put("warehouse_long",wlong);
            db.collection("warehouse")
                    .add(warehouse)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            Toast.makeText(AdminMainActivity.this, "Added Successfully", Toast.LENGTH_SHORT).show();// snigdha added
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            Toast.makeText(AdminMainActivity.this, "Error adding document", Toast.LENGTH_SHORT).show();// snigdha added
                        }
                    });
        }

    }

    public void updateItem(String wname, String wcap, String wlat, String wlong, List<DocumentSnapshot> docs){
        for(DocumentSnapshot document : docs){
            DocumentReference docRef = db.collection("warehouse").document(document.getId());
            Map<String,Object> updates = new HashMap<String, Object>();
            if(!wcap.isEmpty()){
                updates.put("warehouse_cap",wcap);
            }
            if(!wlat.isEmpty()){
                updates.put("warehouse_lat",wlat);
            }
            if(!wlong.isEmpty()){
                updates.put("warehouse_long",wlong);
            }
            if(updates.size()!=0){
                inp_wh.setText("");
                inp_whcap.setText("");
                inp_whLat.setText("");
                inp_whLong.setText("");
                docRef.update(updates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                Toast.makeText(AdminMainActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                                Toast.makeText(AdminMainActivity.this, "Error adding document", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else{
                Toast.makeText(AdminMainActivity.this, "Nothing to update", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void deleteItem(QueryDocumentSnapshot document){
        inp_wh.setText("");
        inp_whcap.setText("");
        inp_whLat.setText("");
        inp_whLong.setText("");
        db.collection("warehouse").document(document.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        Toast.makeText(AdminMainActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show(); // snigdha added
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                        Toast.makeText(AdminMainActivity.this, "Error Deleting (Not deleted)", Toast.LENGTH_SHORT).show();// snigdha added
                    }
                });
    }
}
