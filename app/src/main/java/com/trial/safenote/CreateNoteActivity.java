package com.trial.safenote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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

public class CreateNoteActivity extends AppCompatActivity {

    EditText createnotetitle, createnotecontent;
    FloatingActionButton savenote;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;

    private int MAX_TITLE_LENGTH = 20;
    private int MAX_NOTE_LENGTH = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        savenote = findViewById(R.id.savenote);
        createnotetitle = findViewById(R.id.createnote_title);
        createnotecontent = findViewById(R.id.createnote_content);

        firebaseAuth = firebaseAuth.getInstance();
        firebaseFirestore = firebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getInstance().getCurrentUser();

        savenote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = createnotetitle.getText().toString();
                String content = createnotecontent.getText().toString();
                if (title.length() > MAX_TITLE_LENGTH) {
                    // Input is too long, display an error message to the user
                    Toast.makeText(getApplicationContext(), "Note title is too long.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (content.length() > MAX_NOTE_LENGTH) {
                    // Input is too long, display an error message to the user
                    Toast.makeText(getApplicationContext(), "Note content is too long.", Toast.LENGTH_SHORT).show();
                    return;
                }
                DocumentReference documentReference = firebaseFirestore
                        .collection("notes")
                        .document(firebaseUser.getUid())
                        .collection("usernotes")
                        .document();
                Map<String, Object> note = new HashMap<>();
//                note.put("title", title);
//                note.put("content", content);
                try {
//                    String alias = firebaseUser.getEmail();
                    DocumentReference df = firebaseFirestore.collection("details")
                            .document(firebaseUser.getUid());
                    df.get().addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            String alias = dataSnapshot.getString("alias");
                            try {
                                note.put("title", Encryption.encryptText(title, getApplicationContext(), alias));
                                note.put("content", Encryption.encryptText(content, getApplicationContext(), alias));
                                documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Created Note", Toast.LENGTH_SHORT).show();
                                        view.getContext().startActivity(new Intent(CreateNoteActivity.this, NotesActivity.class));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Create Note Failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to get user details.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        // error occurred while getting documents
                        Toast.makeText(getApplicationContext(), "Failed to get user details.", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}