package com.trial.safenote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
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

public class ViewNoteActivity extends AppCompatActivity {

    private FloatingActionButton editnotebutton;
    private FloatingActionButton protectnotebutton;
    private TextView viewnote_title;
    private TextView viewnote_content;
    private Intent data;
    private String status;
    private String title;
    private String content;
    private String noteId;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        viewnote_title = findViewById(R.id.viewnote_title);
        viewnote_content = findViewById(R.id.viewnote_content);
        editnotebutton = findViewById(R.id.editnotefab);
        protectnotebutton = findViewById(R.id.protectnotefab);

        viewnote_title.setMovementMethod(new ScrollingMovementMethod());
        viewnote_content.setMovementMethod(new ScrollingMovementMethod());

        data = getIntent();
        title = data.getStringExtra("title");
        content = data.getStringExtra("content");
        noteId = data.getStringExtra("noteId");
//        viewnote_title.setText(title);
//        viewnote_content.setText(content);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        try {
//            String alias = firebaseUser.getEmail();
            DocumentReference df = firebaseFirestore.collection("details")
                    .document(firebaseUser.getUid());
            df.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String alias = dataSnapshot.getString("alias");
                    try {
                        viewnote_title.setText(Encryption.decryptText(title, getApplicationContext(), alias));
                        viewnote_content.setText(Encryption.decryptText(content, getApplicationContext(), alias));
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
        status = data.getStringExtra("status");

        editnotebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), EditNoteActivity.class);
                intent.putExtra("title", title);
                intent.putExtra("content", content);
                intent.putExtra("noteId", noteId);
                intent.putExtra("status", status);
                view.getContext().startActivity(intent);
            }
        });
        protectnotebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status.equals("protected")){
//                    Toast.makeText(getApplicationContext(), "Already Marked as Protected", Toast.LENGTH_SHORT).show();
                    DocumentReference documentReference = firebaseFirestore
                            .collection("notes")
                            .document(firebaseUser.getUid())
                            .collection("usernotes")
                            .document();
                    Map<String, Object> note = new HashMap<>();
                    note.put("title", title);
                    note.put("content", content);
                    documentReference.set(note).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Restore Note Failed", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            DocumentReference documentReference = firebaseFirestore
                                    .collection("notes")
                                    .document(firebaseUser.getUid())
                                    .collection("protectedusernotes")
                                    .document(noteId);
                            documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(view.getContext(), "Note Protection Removed", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(view.getContext(), "Protection Remove Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    return;
                }
                DocumentReference documentReference = firebaseFirestore
                        .collection("notes")
                        .document(firebaseUser.getUid())
                        .collection("protectedusernotes")
                        .document();
                Map<String, Object> note = new HashMap<>();
                note.put("title", title);
                note.put("content", content);
                documentReference.set(note).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Mark as Protected Failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DocumentReference documentReference = firebaseFirestore
                                .collection("notes")
                                .document(firebaseUser.getUid())
                                .collection("usernotes")
                                .document(noteId);
                        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(view.getContext(), "Note Marked as Protected", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(view.getContext(), "Mark as Protected Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
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