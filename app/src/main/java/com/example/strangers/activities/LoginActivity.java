package com.example.strangers.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.strangers.R;
import com.example.strangers.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 11;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    //    FirebaseFirestore database;
    KProgressHUD progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        progress = KProgressHUD.create(this);
        progress.setDimAmount(0.5f);


        if (mAuth.getCurrentUser() != null) {
            goToNextActivity();
        }

        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("248877293055-4ml84viqhfl4q1fg0bej9mq8pmr3ermq.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(intent, RC_SIGN_IN);
                //startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }
        });

        findViewById(R.id.loginAnonymous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInAnonymously();
            }
        });

    }

    void logInAnonymously() {
        progress.show();
        JSONObject user = new JSONObject();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://randomuser.me/api/")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    progress.dismiss();
                    throw new IOException("Unexpected code " + response);
                } else {
                    // do something wih the result
                    try {
                        JSONArray array = new JSONArray(new JSONObject(response.body().string()).getJSONArray("results").toString());

                        String fullName = new JSONObject(String.valueOf(array.get(0))).getJSONObject("name").getString("first")
                                + " " + new JSONObject(String.valueOf(array.get(0))).getJSONObject("name").getString("last");
                        String email = new JSONObject(String.valueOf(array.get(0))).getString("email");
                        String photo = new JSONObject(String.valueOf(array.get(0))).getJSONObject("picture").getString("large");
                        String username = new JSONObject(String.valueOf(array.get(0))).getJSONObject("login").getString("username");
//                        Log.e("MESSAGE", String.valueOf(array.get(0)));
                        user.put("fullName", fullName);
                        user.put("email", email);
                        user.put("photo", photo);
                        user.put("username", username);

                        User firebaseUser = new User(username, fullName, photo, "Unknown", 500);
                        database.getReference()
                                .child("profiles")
                                .child(username)
                                .setValue(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progress.dismiss();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                            .putExtra("username", username)
                                            .putExtra("profile", photo));
                                    finishAffinity();
                                } else {
                                    progress.dismiss();
                                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

        });


    }


    void goToNextActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult();
            authWithGoogle(account.getIdToken());
        }
    }

    void authWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            User firebaseUser = new User(user.getUid(), user.getDisplayName(), user.getPhotoUrl().toString(), "Unknown", 500);
                            database.getReference()
                                    .child("profiles")
                                    .child(user.getUid())
                                    .setValue(firebaseUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finishAffinity();
                                    } else {
                                        Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            //Log.e("profile", user.getPhotoUrl().toString());
                        } else {
                            Log.e("err", task.getException().getLocalizedMessage());
                        }
                    }
                });
    }
}