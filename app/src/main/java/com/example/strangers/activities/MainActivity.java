package com.example.strangers.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.strangers.databinding.ActivityMainBinding;
import com.example.strangers.models.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaopiz.kprogresshud.KProgressHUD;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    //    FirebaseFirestore database;
    long coins = 0;
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int requestCode = 1;
    User user;
    KProgressHUD progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        progress = KProgressHUD.create(this);
        progress.setDimAmount(0.5f);
        progress.show();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (auth.getUid() != null) {

            database.getReference().child("profiles")
                    .child(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            progress.dismiss();
                            user = snapshot.getValue(User.class);
                            coins = user.getCoins();

//                            binding.coins.setText("You have: " + coins);

                            Glide.with(MainActivity.this)
                                    .load(user.getProfile())
                                    .into(binding.profilePicture);
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
        }else{
            database.getReference().child("profiles")
                    .child(getIntent().getStringExtra("username"))
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            progress.dismiss();
                            user = snapshot.getValue(User.class);
                            coins = user.getCoins();

//                            binding.coins.setText("You have: " + coins);

                            Glide.with(MainActivity.this)
                                    .load(user.getProfile())
                                    .into(binding.profilePicture);
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });

        }

        binding.findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPermissionsGranted()) {
                        if(auth.getUid() != null){
                            database.getReference().child("profiles")
                                    .child(currentUser.getUid())
                                    .child("coins")
                                    .setValue(coins);
                            Intent intent = new Intent(MainActivity.this, ConnectingActivity.class);
                            intent.putExtra("profile", user.getProfile());
                            startActivity(intent);
                        }
                        else{
                            database.getReference().child("profiles")
                                    .child(getIntent().getStringExtra("username"))
                                    .child("coins")
                                    .setValue(coins);
                            Intent intent = new Intent(MainActivity.this, ConnectingActivity.class);
                            intent.putExtra("username", getIntent().getStringExtra("username"))
                            .putExtra("profile",getIntent().getStringExtra("profile"));
                            startActivity(intent);
                        }

                        //startActivity(new Intent(MainActivity.this, ConnectingActivity.class));

                } else {
                    askPermissions();
                }
            }
        });


    }

    void askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionsGranted() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }

}