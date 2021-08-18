package com.mobdeve.tighee.mychatroomapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity {

    private static final String TAG = "ChatRoomActivity";

    // Views needed
    private TextView welcomeTv;
    private EditText messageEtv;
    private Button sendBtn;

    // RecyclerView Components
    private RecyclerView recyclerView;
    // Replacement of the base adapter view
    private MyFirestoreRecyclerAdapter myFirestoreRecyclerAdapter;

    // DB reference
    private FirebaseFirestore dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.welcomeTv = findViewById(R.id.welcomeTv);
        this.messageEtv = findViewById(R.id.messageEtv);
        this.sendBtn = findViewById(R.id.sendBtn);
        this.recyclerView = findViewById(R.id.recyclerView);

        // Get username sent from the LoginActivity and display it with the welcoming text
        String username = getIntent().getStringExtra(IntentKeys.USERNAME.name());
        this.welcomeTv.setText("Welcome, " + username + "!");

        // Get the messages from the Message Collection
        this.dbRef = FirebaseFirestore.getInstance();
        Query query = dbRef
                .collection(MyFirestoreReferences.MESSAGE_COLLECTION)
                .orderBy(MyFirestoreReferences.TIMESTAMP_FIELD);

        /*
         * IMPLEMENTING THE RECYCLERVIEW WITH A TWIST
         * In the next part here, we're going to set up the adapter for the RecyclerView; however,
         * this isn't like how we'd normally set up an adapter we've done before. For this, we want
         * our RecyclerView to be updated whenever anything in our DB changes; hence, we'll use the
         * FirestoreRecyclerAdapter<OurModel, OurViewHolder>. This integrates with Firebase
         * seamlessly.
         * see: https://github.com/firebase/FirebaseUI-Android/blob/master/firestore/README.md#using-the-firestorerecycleradapter
         * */

        // We first define options for our FirestoreRecyclerAdapter. The documentation for this is
        // quite poor, but all we can assess is that (1) the adapter is going to be able to build a
        // <Message and (2) utilize the query for Messages we defined awhile ago.
        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(query, Message.class)
                .build();

        // We pass the options and the username into our custom FirestoreRecyclerAdapter class. We
        // could opt to just define the class in this class, but I like keeping things separate;
        // hence, why I'm passing in the username so the adapter knows how to adjust the left or
        // right align of the message.
        this.myFirestoreRecyclerAdapter = new MyFirestoreRecyclerAdapter(options, username);

        // After which, we simply assign the adapter to our RecyclerView.
        this.recyclerView.setAdapter(this.myFirestoreRecyclerAdapter);

        // The layout for the RecyclerView is a little different din, but nothing new. Here, we
        // want our messages to start from the bottom; hence, the stack from end being set to true.
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setSmoothScrollbarEnabled(true);
        this.recyclerView.setLayoutManager(linearLayoutManager);

        /*
         * The send button logic revolves around pushing a message instance to the DB. With this,
         * we use the username, message, and serverTimestamp field. We don't pass the ID because am
         * add() method call without setting the ID auto generates the ID.
         * */
        this.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEtv.getText().toString();

                // Ready the values of the message
                Map<String, Object> data = new HashMap<>();
                data.put(MyFirestoreReferences.USERNAME_FIELD, username);
                data.put(MyFirestoreReferences.MESSAGE_FIELD, message);
                data.put(MyFirestoreReferences.TIMESTAMP_FIELD, FieldValue.serverTimestamp());

                // Send the data off to the Message collection
                dbRef.collection(MyFirestoreReferences.MESSAGE_COLLECTION).add(data)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                            // "Reset" the message in the EditText
                            messageEtv.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // When our app is open, we need to have the adapter listening for any changes in the data.
        // To do so, we'd want to turn on the listening using the appropriate method in the onStart
        // or onResume (basically before the start but within the loop)
        this.myFirestoreRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // We want to eventually stop the listening when we're about to exit an app as we don't need
        // something listening all the time in the background.
        this.myFirestoreRecyclerAdapter.stopListening();
    }
}