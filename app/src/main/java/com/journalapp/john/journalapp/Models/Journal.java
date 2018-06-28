package com.journalapp.john.journalapp.Models;

@SuppressWarnings("ALL")
public class Journal {
    private String title;
    private String body;
    private long date;

    public Journal() {
    }

    public Journal(String title, String body, long date) {
        this.title = title;
        this.body = body;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
