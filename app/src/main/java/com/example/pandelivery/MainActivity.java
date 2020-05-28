package com.example.pandelivery;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Defining Variables
    public EditText inp_user, inp_password;
    Button b_login;
    TextView b_register;
    TextView b_forgotPass;
    CheckBox showPass;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Welcome to Pandelivery");

        firebaseAuth = FirebaseAuth.getInstance();
        inp_user = findViewById(R.id.email);
        inp_password = findViewById(R.id.password);
        b_login = findViewById(R.id.submit);
        b_register = findViewById(R.id.register);
        b_forgotPass = findViewById(R.id.forgotpwd);
        radioGroup = findViewById(R.id.usertype);
        showPass = findViewById(R.id.pwdshow);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                checkLogin();
            }
        };


        b_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent I = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(I);
            }
        });
        showPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showPass.isChecked()){
                    inp_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else{
                    inp_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
//                String displaypwd = inp_password.getText().toString();
//                inp_password.setText(displaypwd);
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
                            }
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        b_forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = inp_user.getText().toString();
                if (userEmail.isEmpty()) {
                    inp_user.setError("Provide your Email first!");
                    inp_user.requestFocus();
                }
                FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("Firebase", "Email sent.");
                                    Toast.makeText(MainActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d("Firebase", "Error! Try Later");
                                    Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


    }

//    public void addItem(){
//        // Create a new user with a first, middle, and last name
//        Map<String, Object> user = new HashMap<>();
//        user.put("first", "Alan");
//        user.put("middle", "Mathison");
//        user.put("last", "Turing");
//        user.put("born", 1912);
//
//        // Add a new document with a generated ID
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error adding document", e);
//                    }
//                });
//    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    public void checkLogin(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName == null){
                Toast.makeText(MainActivity.this, "Corrupted User", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut();
                return;
            }
            String[] parts = displayName.split(",");
            String user_type = parts[0];
            if ((user_type.equals("Admin")) && (radioGroup.getCheckedRadioButtonId() == R.id.ADMIN)){
                Toast.makeText(MainActivity.this, "Admin logged in ", Toast.LENGTH_SHORT).show();
                Intent I = new Intent(MainActivity.this, AdminMainActivity.class);
                startActivity(I);
                finish();
            }else if ((user_type.equals("User")) && (radioGroup.getCheckedRadioButtonId() == R.id.USER)){
                Toast.makeText(MainActivity.this, "User logged in ", Toast.LENGTH_SHORT).show();
                Intent I = new Intent(MainActivity.this, UserMainActivity.class);
                startActivity(I);
                finish();
            }else{
                Toast.makeText(MainActivity.this, "Select Correct User Type and Try Again.", Toast.LENGTH_LONG).show();
                Log.d("Firebase:", "User Type should be " + parts[0]);
                firebaseAuth.signOut();
            }
        }
    }
}

