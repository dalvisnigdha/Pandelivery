package com.example.pandelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AdminMainActivity extends AppCompatActivity {
    Button signout;
    Button addstops;
    EditText inp_wh;
    EditText inp_whcap;
    Button savewh;
    Button deletewh;
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

        String wh= inp_wh.getText().toString();
        String whcap = inp_whcap.getText().toString();

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
                inp_wh.setText("");
                inp_whcap.setText("");
            }

        });
        deletewh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//            Delete the data in wh and whcap to the database
                inp_wh.setText("");
                inp_whcap.setText("");
            }
        });
    }
}
