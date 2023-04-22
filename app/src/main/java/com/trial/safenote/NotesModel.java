package com.trial.safenote;

import androidx.annotation.Keep;

@Keep
public class NotesModel {

    private String title;
    private String content;

    public NotesModel() {

    }
    public NotesModel(String title, String content) {
        this.title = title;
        this.content = content;
    }
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
