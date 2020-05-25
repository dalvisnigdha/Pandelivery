package com.example.pandelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegisterActivity extends AppCompatActivity {

    Button regsuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        regsuccess = findViewById(R.id.registernow);

        regsuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                Intent I = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(I);
            }

        });

    }
}
