package com.trial.safenote;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    public TextView getNotetitle() {
        return notetitle;
    }

    public void setNotetitle(TextView notetitle) {
        this.notetitle = notetitle;
    }

    public TextView getNotecontent() {
        return notecontent;
    }

    public void setNotecontent(TextView notecontent) {
        this.notecontent = notecontent;
    }
}
