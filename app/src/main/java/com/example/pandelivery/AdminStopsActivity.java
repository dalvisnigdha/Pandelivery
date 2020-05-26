package com.example.pandelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class AdminStopsActivity extends AppCompatActivity {
    Button signout;
    Button savestop;
    Button deletestop;
    Button backtowh;
    EditText inp_stop;
    EditText inp_stopqty;
    Spinner dropdown;

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

        String[] items = new String[]{"-- Choose warehouse --","1","2","3","4","5","6","7"};
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
