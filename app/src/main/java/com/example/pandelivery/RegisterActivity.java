package com.example.pandelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {
    EditText input_username;
    EditText input_email;
    EditText input_phone;
    EditText input_pass;
    RadioGroup radioGroup;
    Button regsuccess;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Assign Layout Elements
        input_username = findViewById(R.id.fullname);
        input_email = findViewById(R.id.regemail);
        input_phone = findViewById(R.id.contact);
        input_pass = findViewById(R.id.pwd);
        radioGroup = findViewById(R.id.usertypereg);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        regsuccess = findViewById(R.id.registernow);
        // Firebase Elements
        firebaseAuth = FirebaseAuth.getInstance();

        regsuccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                // Show Progress Bar
                progressBar.setVisibility(View.VISIBLE);
                registerUser();
            }

        });

    }

    private void registerUser(){
        // Get Registration Values
        String email = input_email.getText().toString();
        String password = input_pass.getText().toString();
        final String phone = input_phone.getText().toString();
        final String username = input_username.getText().toString();
        final String usertype;
        if (radioGroup.getCheckedRadioButtonId() == R.id.ADMINreg){
            usertype = "Admin";
        }else if (radioGroup.getCheckedRadioButtonId() == R.id.USERreg){
            usertype = "User";
        }else {
            Toast.makeText(getApplicationContext(), "Please Select User Type", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            return;
        }
        // Register on Firebase
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firebase::", "User Registered");
                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                            if (currentUser != null){
                                Log.d("Firebase::", "Updating Display Name");
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(usertype+","+phone+","+username)
                                        .build();
                                currentUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("Firebase::", "User profile updated.");
                                                    Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                                }else{
                                                    Log.d("Firebase::", "Name not Set");
                                                }
                                                firebaseAuth.signOut();
                                                progressBar.setVisibility(View.GONE);
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        });
                            }else{
                                Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_LONG).show();
                                Log.d("Firebase::", "NO USER AFTER REGISTRATION");
                            }
//                            progressBar.setVisibility(View.GONE);

//                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
//                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
//                            progressBar.setVisibility(View.GONE);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                });
//        Intent I = new Intent(RegisterActivity.this, MainActivity.class);
//        startActivity(I);
    }
}
