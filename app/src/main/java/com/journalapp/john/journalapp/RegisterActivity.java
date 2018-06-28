package com.journalapp.john.journalapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.journalapp.john.journalapp.Models.User;
import com.journalapp.john.journalapp.Utils.Utils;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = RegisterActivity.class.getSimpleName() ;
    private EditText mEmailUser;
    private EditText mPasswordUser;
    private TextView mUsername;
    private static final int RC_SIGN_IN =100;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView mGoToLogin = findViewById(R.id.login_text_view);
        mGoToLogin.setOnClickListener(this);

        mEmailUser = findViewById(R.id.user_email_edit_text);
        mPasswordUser = findViewById(R.id.user_password_edit_text);
        Button mSignUpBtn = findViewById(R.id.sign_up_btn);
        Button mGoogleBtn = findViewById(R.id.google_btn);
        mUsername = findViewById(R.id.username);
        mGoogleBtn.setOnClickListener(this);
        mSignUpBtn.setOnClickListener(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                task.isSuccessful();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);

                            User user1 = new User(Objects.requireNonNull(user).getDisplayName(), Objects.requireNonNull(user.getPhotoUrl()).toString());
                            mDatabase.collection(getString(R.string.users_collection)).document(user.getUid())
                                     .set(user1, SetOptions.merge());
                            goToMain();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void signIn() {
        if (!Utils.testForConnection(this)) {
            Toast.makeText(this, R.string.not_internet, Toast.LENGTH_SHORT).show();
        } else {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_text_view:
                onBackPressed();
                break;

            case R.id.google_btn:
                signIn();
                break;
            case R.id.sign_up_btn:
                String email =mEmailUser.getText().toString();
                String password = mPasswordUser.getText().toString();
                String username = mUsername.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {
                    if(password.length()>5) {
                        registerWithEmail(email,password,username);
                    } else {
                        Toast.makeText(this, R.string.error_short_password, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.error_empty, Toast.LENGTH_SHORT).show();
                }

        }
    }

    private void registerWithEmail(String email, String password, final String name) {
        if (!Utils.testForConnection(this)) {
            Toast.makeText(this, R.string.not_internet, Toast.LENGTH_SHORT).show();
        } else {
            final AlertDialog dialog = Utils.startProgress(this, getString(R.string.registering));
            dialog.show();

            mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    User user = new User(name, "default");
                    String id = authResult.getUser().getUid();
                    mDatabase.collection(getString(R.string.users_collection)).document(id).set(user)
                            .addOnSuccessListener(RegisterActivity.this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.cancel();
                                    goToMain();
                                }
                            });
                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.cancel();
                    Toast.makeText(RegisterActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void goToMain() {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
