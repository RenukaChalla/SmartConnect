package com.rfid.smartconnect.smartconnect;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnbind;
    private Button btnpair;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnbind = (Button) findViewById(R.id.btnbind);
        btnbind.setOnClickListener(this);
        btnpair = (Button) findViewById(R.id.btnpair);
        btnbind.setOnClickListener(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnbind){
            Intent btintent = new Intent(this, BTActivity.class);
            startActivity(btintent);
        }else if(v.getId() == R.id.btnpair){
            Intent nfcintent = new Intent(this, MainActivity.class);
            startActivity(nfcintent);
        }
    }
}
