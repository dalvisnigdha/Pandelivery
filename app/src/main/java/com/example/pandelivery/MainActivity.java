package com.example.pandelivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    // Defining Variables
    public EditText inp_user, inp_password;
    Button b_login;
    TextView b_register;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        inp_user = findViewById(R.id.email);
        inp_password = findViewById(R.id.password);
        b_login = findViewById(R.id.submit);
        b_register = findViewById(R.id.register);
        radioGroup = findViewById(R.id.usertype);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    String displayName = user.getDisplayName();
                    String[] parts = displayName.split("|");
                    String user_type = parts[0];
                    if ((user_type.equals("Admin")) && (radioGroup.getCheckedRadioButtonId() == R.id.ADMIN)){
                        Toast.makeText(MainActivity.this, "Admin logged in ", Toast.LENGTH_SHORT).show();
                        Intent I = new Intent(MainActivity.this, AdminMainActivity.class);
                        startActivity(I);
                    }else if ((user_type.equals("User")) && (radioGroup.getCheckedRadioButtonId() == R.id.USER)){
                        Toast.makeText(MainActivity.this, "User logged in ", Toast.LENGTH_SHORT).show();
                        Intent I = new Intent(MainActivity.this, UserMainActivity.class);
                        startActivity(I);
                    }else{
                        Toast.makeText(MainActivity.this, "Select Correct User Type and Try Again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Login to continue", Toast.LENGTH_SHORT).show();
                }
            }
        };
        b_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent I = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(I);
            }
        });
        b_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = inp_user.getText().toString();
                String userPaswd = inp_password.getText().toString();
                if (userEmail.isEmpty()) {
                    inp_user.setError("Provide your Email first!");
                    inp_user.requestFocus();
                } else if (userPaswd.isEmpty()) {
                    inp_password.setError("Enter Password!");
                    inp_password.requestFocus();
                } else if (userEmail.isEmpty() && userPaswd.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Fields Empty!", Toast.LENGTH_SHORT).show();
                } else if (!(userEmail.isEmpty() && userPaswd.isEmpty())) {
                    firebaseAuth.signInWithEmailAndPassword(userEmail, userPaswd).addOnCompleteListener(MainActivity.this, new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Not sucessfull", Toast.LENGTH_SHORT).show();
                            } else {
                                startActivity(new Intent(MainActivity.this, UserMainActivity.class));
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}

