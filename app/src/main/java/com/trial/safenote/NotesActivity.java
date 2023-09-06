package com.trial.safenote;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
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
import com.trial.safenote.databinding.ActivityNotesBinding;

import java.util.HashMap;
import java.util.Map;

//public class NotesActivity extends AppCompatActivity {
public class NotesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter<NotesModel, NoteViewHolder> noteAdapter;

    private ActivityNotesBinding activityNotesBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityNotesBinding = ActivityNotesBinding.inflate(getLayoutInflater());
        setContentView(activityNotesBinding.getRoot());
        changeActivityTitle("My Notes");
//        setContentView(R.layout.activity_notes);
//        setContentView(R.layout.activity_notes_layout);
//        getSupportActionBar().setTitle("My Notes");

//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("My Notes");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Query query = firebaseFirestore.collection("notes")
                .document(firebaseUser.getUid())
                .collection("usernotes")
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
//                holder.getNotecontent().setText(model.getContent());
                try {
                    String title = model.getTitle();
                    String content = model.getContent();
//                    String alias = firebaseUser.getEmail();
                    DocumentReference df = firebaseFirestore.collection("details")
                            .document(firebaseUser.getUid());
                    df.get().addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            String alias = dataSnapshot.getString("alias");
                            try {
                                holder.getNotetitle().setText(Encryption.decryptText(title, getApplicationContext(), alias));
                                holder.getNotecontent().setText(Encryption.decryptText(content, getApplicationContext(), alias));
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

                String noteId = noteAdapter.getSnapshots().getSnapshot(position).getId();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), ViewNoteActivity.class);
                        intent.putExtra("title", model.getTitle());
                        intent.putExtra("content", model.getContent());
                        intent.putExtra("noteId", noteId);
                        intent.putExtra("status", "general");
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
                                intent.putExtra("status", "general");
                                view.getContext().startActivity(intent);
                                return false;
                            }
                        });
                        popupMenu.getMenu().add("Mark as Protected").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                DocumentReference documentReference = firebaseFirestore
                                        .collection("notes")
                                        .document(firebaseUser.getUid())
                                        .collection("protectedusernotes")
                                        .document();
                                Map<String, Object> note = new HashMap<>();
                                note.put("title", model.getTitle());
                                note.put("content", model.getContent());
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
                                                .collection("usernotes")
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_card_layout, parent, false);
                return new NoteViewHolder(view);

            }
        };

        recyclerView = findViewById(R.id.notes_layout);
        recyclerView.setHasFixedSize(true);
        staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(noteAdapter);
    }

//    public class NoteViewHolder extends RecyclerView.ViewHolder {
//
//        private TextView notetitle;
//        private TextView notecontent;
//        private TextView trial;
//        LinearLayout note;
//
//        public NoteViewHolder(@NonNull View itemView) {
//            super(itemView);
//            notetitle = itemView.findViewById(R.id.notetitle);
//            notecontent = itemView.findViewById(R.id.notecontent);
//        }
//    }

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

//    // logout menu
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.logout_menu, menu);
//        return true;
//    }
//
//    // logout functionality
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch(item.getItemId()){
//            case R.id.logout:
//                firebaseAuth.signOut();
//                finish();
//                startActivity(new Intent(NotesActivity.this, MainActivity.class));
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit app?")
                .setMessage("Are you sure you want to exit the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Exit the app
                        finishAffinity();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

}