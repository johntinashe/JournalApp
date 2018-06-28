package com.journalapp.john.journalapp;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
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

@SuppressWarnings("ALL")
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 150 ;
    private static final String TAG = LoginActivity.class.getSimpleName() ;
    private EditText mEmailUser;
    private EditText mPasswordUser;
    private Button mLoginBtn;
    private Button mGoogleBtn;
    private TextView mGoToRegister;
    private FirebaseAuth mAuth;
    private AlertDialog mDialogProg;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mGoToRegister = findViewById(R.id.sign_up_text_view);
        mGoToRegister.setOnClickListener(this);

        mEmailUser = findViewById(R.id.user_email_edit_text);
        mPasswordUser = findViewById(R.id.user_password_edit_text);
        mLoginBtn = findViewById(R.id.sign_up_btn);
        mLoginBtn.setOnClickListener(this);
        mGoogleBtn = findViewById(R.id.google_btn);
        mGoogleBtn.setOnClickListener(this);
        TextView mForgotPassword = findViewById(R.id.forgot_password_text_view);
        mForgotPassword.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_up_text_view:
                createTransitions();
                break;
            case R.id.forgot_password_text_view:
                resetPassword();
                break;
            case R.id.google_btn:
                signIn();
                break;
            case R.id.sign_up_btn:
                String email =mEmailUser.getText().toString();
                String password = mPasswordUser.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    if(password.length()>5) {
                        emailSignIn(email,password);
                    } else {
                        Toast.makeText(this, R.string.error_short_password, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, R.string.error_empty, Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void createTransitions() {
        Pair[] pairs = new Pair[5];
        pairs[0] = new Pair<View ,String>(mEmailUser,"email_transition");
        pairs[1] = new Pair<View ,String>(mPasswordUser,"password_transition");
        pairs[2] = new Pair<View ,String>(mLoginBtn,"login_sign_transition");
        pairs[3] = new Pair<View ,String>(mGoogleBtn,"google_btn_transition");
        pairs[4] = new Pair<View ,String>(mGoToRegister,"text_transition");

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,pairs);
        startActivity(new Intent(this,RegisterActivity.class),options.toBundle());
    }

    private void resetPassword(){
        if (!Utils.testForConnection(this)) {
            Toast.makeText(this, R.string.not_internet, Toast.LENGTH_SHORT).show();
        } else {

            Button send;
            final EditText email;
            final TextView error;

            final Dialog dialog = new Dialog(this);
            Rect displayRectangle = new Rect();
            Window window = getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);

            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);
            dialogView.setMinimumWidth((int) (displayRectangle.width() * 0.9f));
            dialog.setContentView(dialogView);

            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            send = dialogView.findViewById(R.id.reset_button);
            email = dialogView.findViewById(R.id.reset_email);
            error = dialogView.findViewById(R.id.reset_error);

            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (email.getText().length() > 0) {
                        dialog.dismiss();
                        mDialogProg = Utils.startProgress(LoginActivity.this, "Please Wait");
                        mDialogProg.show();
                        mAuth.sendPasswordResetEmail(email.getText().toString())
                                .addOnSuccessListener(LoginActivity.this, new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mDialogProg.cancel();
                                        Toast.makeText(LoginActivity.this, R.string.reset_email_sent, Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(LoginActivity.this, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.show();
                                        error.setText(e.getMessage());
                                        mDialogProg.cancel();
                                    }
                                });
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.error_message, Toast.LENGTH_SHORT).show();
                        if (mDialogProg != null)
                            mDialogProg.cancel();
                    }

                }
            });

        }

    }


    private void emailSignIn(String email, String password) {
        if (!Utils.testForConnection(this)) {
            Toast.makeText(this, R.string.not_internet, Toast.LENGTH_SHORT).show();
        } else {
            final AlertDialog dialog = Utils.startProgress(this, "Signing Wait");
            dialog.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    dialog.cancel();
                    goToMain();
                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.cancel();
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                            Toast.makeText(LoginActivity.this, "Authentication", Toast.LENGTH_SHORT).show();
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


    private void goToMain() {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
