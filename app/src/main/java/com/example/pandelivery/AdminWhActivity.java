package com.example.pandelivery;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminWhActivity extends AppCompatActivity {

    EditText inp_wh;
    EditText inp_whcap;
    Button addstops;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "AdminMainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_wh);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Admin");

        inp_wh = findViewById(R.id.inp_wh);
        inp_whcap = findViewById(R.id.inp_whcap);
        addstops = findViewById(R.id.addstops);
        addstops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                Intent I = new Intent(AdminWhActivity.this, AdminStopsActivity.class);
                startActivity(I);
            }

        });
    }
}
