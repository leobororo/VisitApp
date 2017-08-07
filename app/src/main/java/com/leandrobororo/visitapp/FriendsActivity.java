package com.leandrobororo.visitapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.leandrobororo.visitapp.adapters.AdapterFriendsList;
import com.leandrobororo.visitapp.model.Friend;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        ArrayList<Friend> friends = (ArrayList<Friend>) extras.get(getString(R.string.extra_amigos));

        ListView listAmigos = (ListView) findViewById(R.id.listAmigos);
        AdapterFriendsList adapter = new AdapterFriendsList(FriendsActivity.this, friends);
        listAmigos.setAdapter(adapter);
    }
}
