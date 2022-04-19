package com.example.strangers.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.strangers.databinding.ActivityConnectingBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ConnectingActivity extends AppCompatActivity {

    ActivityConnectingBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    //    FirebaseFirestore database;
    boolean isOkay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
//        database = FirebaseDatabase.getInstance();
//        database = FirebaseFirestore.getInstance();
        String profile = getIntent().getStringExtra("profile");
        Glide.with(this)
                .load(profile)
                .into(binding.profile);

        String username;
        if (auth.getUid() != null) {
            username = auth.getUid();
        } else {
            username = getIntent().getStringExtra("username");
        }


        DatabaseReference myRef = database.getInstance().getReference();

        myRef.child("users")
                .orderByChild("status")
                .equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount() > 0) {
                            isOkay = true;
                            // Room Available
                            for (DataSnapshot childSnap : snapshot.getChildren()) {
                                myRef.child("users")
                                        .child(childSnap.getKey())
                                        .child("incoming")
                                        .setValue(username);
                                myRef.child("users")
                                        .child(childSnap.getKey())
                                        .child("status")
                                        .setValue(1);
                                Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);
                                String incoming = childSnap.child("incoming").getValue(String.class);
                                String createdBy = childSnap.child("createdBy").getValue(String.class);
                                boolean isAvailable = childSnap.child("isAvailable").getValue(Boolean.class);
                                intent.putExtra("username", username);
                                intent.putExtra("incoming", incoming);
                                intent.putExtra("createdBy", createdBy);
                                intent.putExtra("isAvailable", isAvailable);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            // Not Available



                            if (auth.getUid() != null) {
                                HashMap<String, Object> room = new HashMap<>();
                                room.put("incoming", username);
                                room.put("createdBy", username);
                                room.put("isAvailable", true);
                                room.put("status", 0);

                                myRef.child("users")
                                        .child(username)
                                        .setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
//                                    database.getReference()
                                        myRef
                                                .child("users")
                                                .child(username).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                if (snapshot.child("status").exists()) {
                                                    if (snapshot.child("status").getValue(Integer.class) == 1) {

                                                        if (isOkay)
                                                            return;

                                                        isOkay = true;
                                                        Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);
                                                        String incoming = snapshot.child("incoming").getValue(String.class);
                                                        String createdBy = snapshot.child("createdBy").getValue(String.class);
                                                        boolean isAvailable = snapshot.child("isAvailable").getValue(Boolean.class);
                                                        intent.putExtra("username", username);
                                                        intent.putExtra("incoming", incoming);
                                                        intent.putExtra("createdBy", createdBy);
                                                        intent.putExtra("isAvailable", isAvailable);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });
                                    }
                                });

                            }else{
                                HashMap<String, Object> room = new HashMap<>();
                                room.put("incoming", getIntent().getStringExtra("username"));
                                room.put("createdBy", getIntent().getStringExtra("username"));
                                room.put("isAvailable", true);
                                room.put("status", 0);
                                myRef.child("users")
                                        .child(getIntent().getStringExtra("username"))
                                        .setValue(room).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
//                                    database.getReference()
                                        myRef
                                                .child("users")
                                                .child(getIntent().getStringExtra("username")).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                if (snapshot.child("status").exists()) {
                                                    if (snapshot.child("status").getValue(Integer.class) == 1) {

                                                        if (isOkay)
                                                            return;

                                                        isOkay = true;
                                                        Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);
                                                        String incoming = snapshot.child("incoming").getValue(String.class);
                                                        String createdBy = snapshot.child("createdBy").getValue(String.class);
                                                        boolean isAvailable = snapshot.child("isAvailable").getValue(Boolean.class);
                                                        intent.putExtra("username", getIntent().getStringExtra("username"));
                                                        intent.putExtra("incoming", incoming);
                                                        intent.putExtra("createdBy", createdBy);
                                                        intent.putExtra("isAvailable", isAvailable);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });
                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }
}