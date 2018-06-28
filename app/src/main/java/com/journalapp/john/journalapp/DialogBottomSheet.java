package com.journalapp.john.journalapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.journalapp.john.journalapp.Models.Journal;
import com.journalapp.john.journalapp.Utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DialogBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener{

    private EditText mTitle;
    private EditText mBody;
    private TextView mDate;
    private TextView mYear;
    private View mView;
    private int mCurrentYear, mMonth, mDay;
    public  BottomSheetDialogFragment dialogFragment;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private Journal journal;
    private String mId;
    private  long mStartDate;

    public DialogBottomSheet() {
    }

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_bottom_sheet,container,false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        setUpViews();
        Calendar c = Calendar.getInstance();
        mCurrentYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mDate.setText(String.format("%s %d", Utils.getYear(mMonth + 1), mDay));
        mYear.setText(String.format("%d", mCurrentYear));
        if (journal != null) {
            mTitle.setText(journal.getTitle());
            mBody.setText(journal.getBody());
        }
        return mView;
    }

    private void setUpViews() {
        mTitle = mView.findViewById(R.id.edit_text_title);
        mBody = mView.findViewById(R.id.edit_text_body);
        Button mSaveBtn = mView.findViewById(R.id.save_btn);
        mDate = mView.findViewById(R.id.date_text_view);
        mYear = mView.findViewById(R.id.year_text_view);
        ImageView mCalenderBtn = mView.findViewById(R.id.calender_btn);
        mCalenderBtn.setOnClickListener(this);
        mSaveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_btn:
                String title = mTitle.getText().toString();
                String body = mBody.getText().toString();
                if (mStartDate == 0) mStartDate = new Date().getTime();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(body)) {
                    Journal journal = new Journal(title,body,mStartDate);
                    if (mId == null) {
                        saveJournal(journal ,null);
                    } else {
                        saveJournal(journal,mId);
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.error_empty), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.calender_btn:
                getDate();
                break;
        }
    }

    private void saveJournal(Journal journal, String id) {
        final Activity activity = getActivity();
        if (activity != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            final AlertDialog dialog = Utils.startProgress(getActivity(),"Saving Wait...");
            if (Utils.testForConnection(activity.getApplicationContext())) dialog.show();
            if (!Utils.testForConnection(activity.getApplicationContext())) dialogFragment.dismiss();
            if (user != null) {
               if (id == null) {
                   mDatabase.collection(getString(R.string.users_collection)).document(user.getUid()).collection(getString(R.string.journals_collection))
                           .add(journal)
                           .addOnSuccessListener(activity, new OnSuccessListener<DocumentReference>() {
                               @Override
                               public void onSuccess(DocumentReference documentReference) {
                                   Toast.makeText(activity.getApplicationContext(), R.string.entry, Toast.LENGTH_SHORT).show();
                                   if(dialogFragment != null) dialogFragment.dismiss();
                                   if (dialog.isShowing()) dialog.cancel();
                               }
                           })
                           .addOnFailureListener(getActivity(), new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                   if (dialog.isShowing()) dialog.cancel();
                               }
                           });
               } else {
                   mDatabase.collection(getString(R.string.users_collection)).document(user.getUid()).collection(getString(R.string.journals_collection))
                           .document(id)
                           .set(journal, SetOptions.merge())
                           .addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   Toast.makeText(activity.getApplicationContext(), R.string.entry, Toast.LENGTH_SHORT).show();
                                   if(dialogFragment != null) dialogFragment.dismiss();
                                   if (dialog.isShowing()) dialog.cancel();
                               }
                           }).addOnFailureListener(activity, new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                           if (dialog.isShowing()) dialog.cancel();
                       }
                   });


               }
            }
        }
    }

    private void getDate() {
        Calendar c = Calendar.getInstance();
        mCurrentYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()),
                new DatePickerDialog.OnDateSetListener() {

                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        mDate.setText(String.format("%s %d", Utils.getYear(monthOfYear + 1), dayOfMonth));
                        mYear.setText(String.format("%d", year));
                        returnDate(dayOfMonth,monthOfYear+1,year);
                    }
                }, mCurrentYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.show();
    }

    public void setJournal(Journal journal , String mId) {
       this.journal = journal;
       this.mId = mId;
    }

    private void returnDate(int day , int month , int year) {

        try {
            String dateString = day+"/"+month+"/"+year;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date = sdf.parse(dateString);
             mStartDate = date.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
            mStartDate = new Date().getTime();
        }
    }
}
