package com.example.pandelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AdminMainActivity extends AppCompatActivity {
    Button signout;
    Button addstops;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        signout = findViewById(R.id.signout);
        addstops = findViewById(R.id.addstops);
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
    }
}
