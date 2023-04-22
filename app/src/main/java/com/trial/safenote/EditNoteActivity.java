package com.trial.safenote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditNoteActivity extends AppCompatActivity {

    private EditText editnote_title;
    private EditText editnote_content;
    private FloatingActionButton updatenotebutton;

    private Intent data;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        editnote_title = findViewById(R.id.editnote_title);
        editnote_content = findViewById(R.id.editnote_content);
        updatenotebutton = findViewById(R.id.updatenote);

        firebaseAuth = firebaseAuth.getInstance();
        firebaseFirestore = firebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getInstance().getCurrentUser();

        data = getIntent();
        String title = data.getStringExtra("title");
        String content = data.getStringExtra("content");
        editnote_title.setText(title);
        editnote_content.setText(content);

        updatenotebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String updated_title = editnote_title.getText().toString();
                String updated_content = editnote_content.getText().toString();
                DocumentReference documentReference = firebaseFirestore
                        .collection("notes")
                        .document(firebaseUser.getUid())
                        .collection("usernotes")
                        .document(data.getStringExtra("noteId"));
                Map<String, Object> note = new HashMap<>();
                note.put("title", updated_title);
                note.put("content", updated_content);
                documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(view.getContext(), "Updated Note", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditNoteActivity.this, NotesActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(view.getContext(), "Update Note Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}