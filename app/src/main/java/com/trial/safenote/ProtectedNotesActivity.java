package com.trial.safenote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.trial.safenote.databinding.ActivityNotesBinding;
import com.trial.safenote.databinding.ActivityProtectedNotesBinding;

public class ProtectedNotesActivity extends BaseActivity {

    ActivityProtectedNotesBinding protectedNotesBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        protectedNotesBinding = ActivityProtectedNotesBinding.inflate(getLayoutInflater());
        setContentView(protectedNotesBinding.getRoot());
        changeActivityTitle("My Protected Notes");
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ProtectedNotesActivity.this, NotesActivity.class));
    }
}