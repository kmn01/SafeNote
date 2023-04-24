package com.trial.safenote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FloatingActionButton createnotefab;
    private DrawerLayout drawer;
    private FirebaseAuth firebaseAuth;

    @Override
    public void setContentView(View view) {
        drawer = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout frame = drawer.findViewById(R.id.content_frame);
        frame.addView(view);
        super.setContentView(drawer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = drawer.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawer, toolbar,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        firebaseAuth = FirebaseAuth.getInstance();

        createnotefab = findViewById(R.id.createnotefab);
        createnotefab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), CreateNoteActivity.class));
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(getApplicationContext(), NotesActivity.class));
                break;
            case R.id.nav_protectednotes:
                startActivity(new Intent(getApplicationContext(), AccessProtectedActivity.class));
                break;
            case R.id.nav_trash:
                startActivity(new Intent(getApplicationContext(), TrashActivity.class));
                break;
//            case R.id.nav_settings:
//                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
//                break;
//            case R.id.nav_help:
//                startActivity(new Intent(getApplicationContext(), HelpActivity.class));
//                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    protected void changeActivityTitle(String activityTitle) {
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(activityTitle);
        }
    }

    // logout menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout_menu, menu);
        return true;
    }

    // logout functionality
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout:
                firebaseAuth.signOut();
                finish();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }
}