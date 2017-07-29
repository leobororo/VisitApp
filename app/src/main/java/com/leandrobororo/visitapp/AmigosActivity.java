package com.leandrobororo.visitapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.leandrobororo.visitapp.model.Amigo;

import java.util.ArrayList;

public class AmigosActivity extends AppCompatActivity {

    private AdapterListAmigos adapter;
    private ListView listAmigos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amigos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        ArrayList<Amigo> amigos = (ArrayList<Amigo>) extras.get("amigos");

        listAmigos = (ListView) findViewById(R.id.listAmigos);
        adapter = new AdapterListAmigos(AmigosActivity.this, amigos);
        listAmigos.setAdapter(adapter);
    }

}
