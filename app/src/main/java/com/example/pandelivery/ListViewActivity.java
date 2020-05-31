package com.example.pandelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListViewActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "ListViewActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("List View");
        final List<String> items = new ArrayList<String>();
        db.collection("warehouse")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String warehouse_name = (String) document.get("warehouse");
                                items.add(warehouse_name);
                                if(document.getData().containsKey("stops")){
                                    List<String> stops = (ArrayList<String>) document.get("stops");
                                    for(int i=0;i<stops.size();i++){
                                        String[] loc = stops.get(i).split("#");
                                        items.add("Stop "+"i "+loc[0]);
                                    }
                                }
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_list_view, items);

        ListView listView = (ListView) findViewById(R.id.list_View);
        listView.setAdapter(adapter);
    }
}
