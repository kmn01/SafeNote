package com.trial.safenote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewNoteActivity extends AppCompatActivity {

    FloatingActionButton editnotebutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note); 

        editnotebutton = findViewById(R.id.editnotefab);
        editnotebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewNoteActivity.this, EditNoteActivity.class);
                startActivity(intent);
            }
        });
    }
}