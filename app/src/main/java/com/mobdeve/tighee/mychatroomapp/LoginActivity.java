package com.mobdeve.tighee.mychatroomapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    // log for the log.d()
    private final String TAG = "LoginActivity";

    // Views needed for the layout
    private EditText usernameEtv;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialization of the views
        this.usernameEtv = findViewById(R.id.usernameEtv);
        this.loginBtn = findViewById(R.id.enterBtn);

        // On click, check if the input username is in the DB. If it isn't, then prompt the user
        // that the username will be recorded into the DB. If it is in the DB, then proceed to the
        // ChatRoomActivity with the username in the sending intent.
        this.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameEtv.getText().toString();

                // Get the DB from Firebase
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                // Get the User collection reference
                CollectionReference usersRef = db.collection(MyFirestoreReferences.USERS_COLLECTION);

                // Query the User collection reference to find entries with the given username
                Query query = usersRef.whereEqualTo(
                        MyFirestoreReferences.USERNAME_FIELD,
                        username);
                // Perform the query and add an OnCompleteListener to know when the query has
                // finished
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // If there are no results, then there is no sign of the username in
                            // the DB.
                            if(task.getResult().isEmpty()) {
                                showNewUserDialog(usersRef, username);
                            } else {
                                moveToChatRoomActivity(username);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });
    }

    /*
     * This function was made to reduce the amount of code in OnCreate. Basically, it generates a
     * Dialog informing the user that the username has not been found and that the app would like
     * to create an account for them. Two options are presented to the user: (Yes) to proceed with
     * "creating" the account and pushing the username into the DB and (No) to simply cancel the
     * operation.
     * */
    private void showNewUserDialog(CollectionReference usersRef, String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This seems to be a new account. Would you like for us to create an account for you?");
        builder.setCancelable(true);

        builder.setPositiveButton(
            "Yes",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Similar to the ContentValues when we were discussing SQLite, we can use a
                    // HashMap to store a key value pair (String, Object pair). This HashMap will be
                    // sent to the DB and the strings will form the fields, while the objects will
                    // form the values.
                    Map<String, Object> data = new HashMap<>();
                    // We are only storing the username in the User Collection
                    data.put(MyFirestoreReferences.USERNAME_FIELD, username);

                    // add() is like insert(); with the User Collection, we add the HashMap values.
                    // We don't include an ID because straight up adding a value without an ID auto
                    // generates the ID. If you want to set the ID, you'd want to use set().
                    // Additionally, we add an OnSuccessListener and OnFailureListener. They don't
                    // do anything important right now, but you can see that we can retrieve the ID
                    // in case you'd like to utilize it client side.
                    usersRef.add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                // If the add was successful, we'd want to move to the
                                // ChatRoomActivity
                                moveToChatRoomActivity(username);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });


                }
            });

        builder.setNegativeButton(
            "No",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /*
     * This method was created to reduce the lines of code needed since this is called twice -- one
     * if the username was found and two if the user agrees to encode the username into the DB.
     * */
    private void moveToChatRoomActivity(String username) {
        Intent i = new Intent(LoginActivity.this, ChatRoomActivity.class);
        // We send the username instead of querying it in the ChatRoomActivity to reduce the number
        // of network requests our app will make.
        i.putExtra(IntentKeys.USERNAME.name(), username);
        startActivity(i);
    }
}