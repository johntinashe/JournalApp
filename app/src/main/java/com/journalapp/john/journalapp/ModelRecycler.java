package com.journalapp.john.journalapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.journalapp.john.journalapp.Models.Journal;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ModelRecycler {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore mDatabase;
    private final Context context;
    private final FragmentManager manager;

    public ModelRecycler(Context application , FragmentManager manager) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        context = application;
        this.manager = manager;
    }

    public FirestoreRecyclerAdapter getAll() {
        FirebaseUser user = mAuth.getCurrentUser();
        String id;
        if (user != null) {
            id = user.getUid();
        } else{
            return null;
        }
        Query query = mDatabase.collection(context.getString(R.string.users_collection)).document(id).collection(context.getString(R.string.journals_collection))
                .orderBy("date", Query.Direction.DESCENDING);

        final FirestoreRecyclerOptions<Journal> options = new FirestoreRecyclerOptions.Builder<Journal>()
                .setQuery(query, Journal.class)
                .build();

        return new FirestoreRecyclerAdapter<Journal, JournalHolder>(options) {

            @NonNull
            @Override
            public JournalHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_single_item, parent, false);
                return new JournalHolder(view);
            }

            @SuppressLint("RecyclerView")
            @Override
            protected void onBindViewHolder(@NonNull JournalHolder holder, final int position, @NonNull Journal model) {
                holder.title.setText(model.getTitle());
                holder.body.setText(model.getBody());
                @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat(context.getString(R.string.date_format));
                String dateString = formatter.format(new Date((model.getDate())));
                holder.date.setText(dateString);

                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String id1 = options.getSnapshots().getSnapshot(position).getId();
                        String uid = mAuth.getCurrentUser().getUid();
                        mDatabase.collection(context.getString(R.string.users_collection)).document(uid)
                                 .collection(context.getString(R.string.journals_collection))
                                 .document(id1)
                                 .delete()
                                 .addOnFailureListener(new OnFailureListener() {
                                     @Override
                                     public void onFailure(@NonNull Exception e) {
                                         Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                     }
                                 });
                    }
                });

                holder.editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Journal journal = options.getSnapshots().getSnapshot(position).toObject(Journal.class);
                        String id = options.getSnapshots().getSnapshot(position).getId();
                        DialogBottomSheet bottomSheet = new DialogBottomSheet();
                        bottomSheet.show(manager, bottomSheet.getTag());
                        bottomSheet.setJournal(journal,id);
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Journal journal = options.getSnapshots().getSnapshot(position).toObject(Journal.class);
                        DialogViewJournal viewJournal = new DialogViewJournal();
                        viewJournal.show(manager,viewJournal.getTag());
                        viewJournal.setJournal(journal);
                    }
                });

            }
        };
    }
    
    public class JournalHolder extends RecyclerView.ViewHolder{

        private final TextView title;
        private final TextView body;
        private final TextView date;
        private final ImageView editBtn;
        private final ImageView deleteBtn;

        JournalHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.single_title);
            body = itemView.findViewById(R.id.single_body);
            date = itemView.findViewById(R.id.single_date_tv);
            editBtn = itemView.findViewById(R.id.journal_edit_btn);
            deleteBtn = itemView.findViewById(R.id.journal_delete_btn);
        }
    }
}
