package com.trial.safenote;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class NotesActivity extends AppCompatActivity {

    FloatingActionButton createnotefab;
    RecyclerView recyclerView;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter<NotesModel, NoteViewHolder> noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        getSupportActionBar().setTitle("My Notes");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        createnotefab = findViewById(R.id.createnotefab);
        createnotefab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NotesActivity.this, CreateNoteActivity.class));
            }
        });

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

                holder.notetitle.setText(model.getTitle());
                holder.notecontent.setText(model.getContent());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(view.getContext(), ViewNoteActivity.class);
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
                                view.getContext().startActivity(intent);
                                return false;
                            }
                        });
                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                Toast.makeText(view.getContext(), "Note Deleted", Toast.LENGTH_SHORT).show();
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout, parent, false);
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

    public class NoteViewHolder extends RecyclerView.ViewHolder {

        private TextView notetitle;
        private TextView notecontent;
        private TextView trial;
        LinearLayout note;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle = itemView.findViewById(R.id.notetitle);
            notecontent = itemView.findViewById(R.id.notecontent);
        }
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
                startActivity(new Intent(NotesActivity.this, MainActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}