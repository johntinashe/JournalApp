package com.journalapp.john.journalapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private CircleImageView mProfileImage;
    private FirestoreRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_toolbar,null,true);
        actionBar.setCustomView(view);
        mProfileImage = view.findViewById(R.id.img_profile);
        mProfileImage.setOnClickListener(this);
        FloatingActionButton mAddBtn = findViewById(R.id.add_btn);
        mAddBtn.setOnClickListener(this);

        RecyclerView mRecyclerView = findViewById(R.id.recycler_view_all);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null) {
                    Intent login = new Intent(getApplicationContext(),LoginActivity.class);
                    login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(login);
                    finish();
                }
            }
        };
        setUpUser();
        ModelRecycler modelRecycler = new ModelRecycler(getApplicationContext(),getSupportFragmentManager());
        mAdapter = modelRecycler.getAll();
        if(mAdapter !=null) mRecyclerView.setAdapter(mAdapter);
    }

    private void popupMenu() {
        PopupMenu popup = new PopupMenu(MainActivity.this, mProfileImage);
        popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                signOut();
                return true;
            }
        });

        popup.show();
    }

    private void setUpUser() {
        if(mAuth.getCurrentUser() != null) {
            final FirebaseUser user = mAuth.getCurrentUser();
            Picasso.with(getApplicationContext()).load(user.getPhotoUrl()).placeholder(R.drawable.ic_action_avatar).networkPolicy(NetworkPolicy.OFFLINE).into(mProfileImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(getApplicationContext()).load(user.getPhotoUrl()).into(mProfileImage);
                }
            });
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
        if (mAuth.getCurrentUser() == null) {
            Intent login = new Intent(this,LoginActivity.class);
            login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(login);
            finish();
        } else {
            mAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
       // if (mAdapter  != null) mAdapter.stopListening();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_btn:
                DialogBottomSheet bottomSheetFragment = new DialogBottomSheet();
                bottomSheetFragment.dialogFragment = bottomSheetFragment;
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                break;
            case R.id.img_profile:
                popupMenu();
                break;
        }
    }

    private void signOut() {
        if (mAuth.getCurrentUser() != null) mAuth.signOut();
    }

}
