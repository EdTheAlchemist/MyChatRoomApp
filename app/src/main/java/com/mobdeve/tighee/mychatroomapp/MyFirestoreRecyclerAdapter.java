package com.mobdeve.tighee.mychatroomapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

/*
 * The FirestoreRecyclerAdapter is is a modification of the regular Adapter and is able to integrate
 * with Firestore. According to the documentation: [The Adapter] binds a Query to a RecyclerView and
 * responds to all real-time events included items being added, removed, moved, or changed. Best
 * used with small result sets since all results are loaded at once.
 * See https://firebaseopensource.com/projects/firebase/firebaseui-android/firestore/readme/
 * */

public class MyFirestoreRecyclerAdapter extends FirestoreRecyclerAdapter<Message, MyViewHolder> {

    // We need to know who the current user is so that we can adjust their message to the right of
    // the screen and those who aren't to the left of the screen
    private String username;

    public MyFirestoreRecyclerAdapter(FirestoreRecyclerOptions<Message> options, String username) {
        super(options);
        this.username = username;
    }

    // Good old onCreateViewHolder. Nothing different here.
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;
    }

    // The onBindViewHolder is slightly different as you also get the "model". It was clear from the
    // documentation, but it seems that its discouraging the use of the position parameter. The
    // model passed in is actually the respective model that is about to be bound. Hence, why we
    // don't use position, and directly get the information from the model parameter.
    @Override
    protected void onBindViewHolder(MyViewHolder holder, int position, Message model) {
        holder.bindData(model);

        // Change alignment depending on whether the message is of the current user or not
        if(model.getUsername().equals(this.username)) { // Right align
            holder.rightAlignText();
        } else { // Left align
            holder.leftAlignText();
        }
    }
}
