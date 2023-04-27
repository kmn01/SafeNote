package com.trial.safenote;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.trial.safenote.databinding.ActivityProtectedNotesBinding;

import java.util.HashMap;
import java.util.Map;

public class ProtectedNotesActivity extends BaseActivity {

    ActivityProtectedNotesBinding protectedNotesBinding;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter<NotesModel, NoteViewHolder> noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        protectedNotesBinding = ActivityProtectedNotesBinding.inflate(getLayoutInflater());
        setContentView(protectedNotesBinding.getRoot());
        changeActivityTitle("My Protected Notes");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Query query = firebaseFirestore.collection("notes")
                .document(firebaseUser.getUid())
                .collection("protectedusernotes")
                .orderBy("title", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<NotesModel> allusernotes =
                new FirestoreRecyclerOptions.Builder<NotesModel>()
                        .setQuery(query, NotesModel.class)
                        .build();

        noteAdapter = new FirestoreRecyclerAdapter<NotesModel, NoteViewHolder>(allusernotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull NotesModel model) {

                ImageView popupbutton = holder.itemView.findViewById(R.id.popupmenubutton);

//                holder.getNotetitle().setText(model.getTitle());
////                holder.getNotecontent().setText(model.getContent());
                try {
                    String title = model.getTitle();
//                    String content =
                    String email = firebaseUser.getEmail();
                    holder.getNotetitle().setText(Encryption.decryptText(title, getApplicationContext(), email));
//                    holder.getNotecontent().setText(Encryption.decryptText(content));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                String noteId = noteAdapter.getSnapshots().getSnapshot(position).getId();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), ViewNoteActivity.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("content", model.getContent());
                        intent.putExtra("noteId", noteId);
                        intent.putExtra("status", "protected");
                        view.getContext().startActivity(intent);
                    }
                });

                popupbutton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Intent intent = new Intent(view.getContext(), EditNoteActivity.class);
                                intent.putExtra("title", model.getTitle());
                                intent.putExtra("content", model.getContent());
                                intent.putExtra("noteId", noteId);
                                intent.putExtra("status", "protected");
                                view.getContext().startActivity(intent);
                                return false;
                            }
                        });
                        popupMenu.getMenu().add("Remove Protection").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                DocumentReference documentReference = firebaseFirestore
                                        .collection("notes")
                                        .document(firebaseUser.getUid())
                                        .collection("usernotes")
                                        .document();
                                Map<String, Object> note = new HashMap<>();
                                note.put("title", model.getTitle());
                                note.put("content", model.getContent());
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
                                return false;
                            }
                        });
                        popupMenu.getMenu().add("Move to Trash").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                DocumentReference documentReference = firebaseFirestore
                                        .collection("notes")
                                        .document(firebaseUser.getUid())
                                        .collection("deletedusernotes")
                                        .document();
                                Map<String, Object> note = new HashMap<>();
                                note.put("title", model.getTitle());
                                note.put("content", model.getContent());
                                documentReference.set(note).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Move to Trash Failed", Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(view.getContext(), "Note Moved to Trash", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(view.getContext(), "Move to Trash Failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.protected_notes_card_layout, parent, false);
                return new NoteViewHolder(view);

            }
        };

        recyclerView = findViewById(R.id.protected_notes_layout);
        recyclerView.setHasFixedSize(true);
        staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(noteAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (noteAdapter != null) {
            noteAdapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ProtectedNotesActivity.this, NotesActivity.class));
    }
}