package com.example.pandelivery;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.view.Menu;
public class UserMainActivity extends AppCompatActivity {
//Button signout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("User");

//        signout = findViewById(R.id.signout);


//        signout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick (View view){
//                Intent I = new Intent(UserMainActivity.this, MainActivity.class);
//                startActivity(I);
//            }
//
//        });
    }

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
            Intent I = new Intent(UserMainActivity.this, MainActivity.class);
            startActivity(I);
            return false;
        }
        if(id==R.id.List_View)
        {
            Intent I = new Intent(UserMainActivity.this, ListViewActivity.class);
            startActivity(I);
            return false;
        }
        return true;
    }
}
