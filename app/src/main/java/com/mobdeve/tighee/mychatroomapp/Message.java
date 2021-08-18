package com.mobdeve.tighee.mychatroomapp;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    private long id;
    private String username;
    private String message;
    // From documentation about @ServerTimestamp: Annotation used to mark a timestamp field to be
    // populated with a server timestamp. If a POJO being written contains null for a
    // @ServerTimestamp-annotated field, it will be replaced with a server-generated timestamp.
    private @ServerTimestamp Date timestamp;

    // Do not remove this. This is needed by Firebase when it creates instances of our model class.
    public Message() {

    }

    // This was actually not used at all in our app, but its here regardless.
    public Message(String username, String message, long id, Date timestamp) {
        this.username = username;
        this.message = message;
        this.id = id;
        this.timestamp = timestamp;
    }

    // Please make sure to observe proper standards when naming your getters and setters or else
    // they won't be mapped by Firebase when it generates objects.
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timeStamp) {
        this.timestamp = timeStamp;
    }
}
