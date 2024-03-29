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
    private String status;
    private String title;
    private String content;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    private int MAX_TITLE_LENGTH = 20;
    private int MAX_NOTE_LENGTH = 9999;

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
        title = data.getStringExtra("title");
        content = data.getStringExtra("content");
        status = data.getStringExtra("status");
//        editnote_title.setText(title);
//        editnote_content.setText(content);
//            String alias = firebaseUser.getEmail();
        DocumentReference df = firebaseFirestore.collection("details")
                .document(firebaseUser.getUid());
        df.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String alias = dataSnapshot.getString("alias");
                try {
                    editnote_title.setText(Encryption.decryptText(title, getApplicationContext(), alias));
                    editnote_content.setText(Encryption.decryptText(content, getApplicationContext(), alias));
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

        updatenotebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String updated_title = editnote_title.getText().toString();
                String updated_content = editnote_content.getText().toString();
                if (updated_title.length() > MAX_TITLE_LENGTH) {
                    // Input is too long, display an error message to the user
                    Toast.makeText(getApplicationContext(), "Note title is too long.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (updated_content.length() > MAX_NOTE_LENGTH) {
                    // Input is too long, display an error message to the user
                    Toast.makeText(getApplicationContext(), "Note content is too long.", Toast.LENGTH_SHORT).show();
                    return;
                }
                DocumentReference documentReference;
                switch (status) {
                    case "protected": {
                        documentReference = firebaseFirestore
                                .collection("notes")
                                .document(firebaseUser.getUid())
                                .collection("protectedusernotes")
                                .document(data.getStringExtra("noteId"));
                        break;
                    }
                    case "deleted": {
                        documentReference = firebaseFirestore
                                .collection("notes")
                                .document(firebaseUser.getUid())
                                .collection("deletedusernotes")
                                .document(data.getStringExtra("noteId"));
                        break;
                    }
                    default: {
                        documentReference = firebaseFirestore
                                .collection("notes")
                                .document(firebaseUser.getUid())
                                .collection("usernotes")
                                .document(data.getStringExtra("noteId"));
                    }
                }
                Map<String, Object> note = new HashMap<>();
//                note.put("title", updated_title);
//                note.put("content", updated_content);
//                    String alias = firebaseUser.getEmail();
                DocumentReference df = firebaseFirestore.collection("details")
                        .document(firebaseUser.getUid());
                df.get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        String alias = dataSnapshot.getString("alias");
                        try {
                            note.put("title", Encryption.encryptText(updated_title, getApplicationContext(), alias));
                            note.put("content", Encryption.encryptText(updated_content, getApplicationContext(), alias));
                            documentReference.set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(view.getContext(), "Updated Note", Toast.LENGTH_SHORT).show();
                                    switch(status) {
                                        case "protected": {
                                            startActivity(new Intent(EditNoteActivity.this, ProtectedNotesActivity.class));
                                            break;
                                        }
                                        case "deleted": {
                                            startActivity(new Intent(EditNoteActivity.this, TrashActivity.class));
                                            break;
                                        }
                                        default:
                                            startActivity(new Intent(EditNoteActivity.this, NotesActivity.class));
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(view.getContext(), "Update Note Failed", Toast.LENGTH_SHORT).show();
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
            }
        });
    }

    @Override
    public void onBackPressed() {
        switch(status) {
            case "protected": {
                startActivity(new Intent(EditNoteActivity.this, ProtectedNotesActivity.class));
                break;
            }
            case "deleted": {
                startActivity(new Intent(EditNoteActivity.this, TrashActivity.class));
                break;
            }
            default:
                startActivity(new Intent(EditNoteActivity.this, NotesActivity.class));
        }
    }
}