package com.trial.safenote;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewNoteActivity extends AppCompatActivity {

    private FloatingActionButton editnotebutton;
    private TextView viewnote_title;
    private TextView viewnote_content;
    private Intent data;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        viewnote_title = findViewById(R.id.viewnote_title);
        viewnote_content = findViewById(R.id.viewnote_content);
        editnotebutton = findViewById(R.id.editnotefab);

        data = getIntent();
        viewnote_title.setText(data.getStringExtra("title"));
        viewnote_content.setText(data.getStringExtra("content"));
        status = data.getStringExtra("status");

        editnotebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditNoteActivity.class);
                intent.putExtra("title", data.getStringExtra("title"));
                intent.putExtra("content", data.getStringExtra("content"));
                intent.putExtra("noteId", data.getStringExtra("noteId"));
                intent.putExtra("status", data.getStringExtra("status"));
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        switch(status) {
            case "protected": {
                startActivity(new Intent(ViewNoteActivity.this, ProtectedNotesActivity.class));
                break;
            }
            case "deleted": {
                startActivity(new Intent(ViewNoteActivity.this, TrashActivity.class));
                break;
            }
            default:
                startActivity(new Intent(ViewNoteActivity.this, NotesActivity.class));
        }
    }
}