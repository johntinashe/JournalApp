package com.journalapp.john.journalapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.journalapp.john.journalapp.Models.Journal;
import com.journalapp.john.journalapp.Utils.Utils;

import java.util.Calendar;
import java.util.Date;

public class DialogViewJournal extends BottomSheetDialogFragment {

    private View mView;
    private TextView mTitle;
    private TextView mBody;
    private TextView mDate;
    private TextView mYear;
    private Journal journal;

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_view,container,false);
        setUpViews();
        if (journal != null) {
            mTitle.setText(journal.getTitle());
            mBody.setText(journal.getBody());
            //SimpleDateFormat formatter = new SimpleDateFormat(getContext().getString(R.string.date_format));
            //String dateString = formatter.format(new Date((journal.getDate())));
            // SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date(journal.getDate());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            mDate.setText(String.format("%s %d", Utils.getYear(calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.DAY_OF_MONTH)));
            mYear.setText(String.format("%d", calendar.get(Calendar.YEAR)));
        }
        return mView;

    }

    private void setUpViews() {
        mTitle = mView.findViewById(R.id.view_text_title);
        mBody = mView.findViewById(R.id.view_text_body);
        mDate = mView.findViewById(R.id.view_date_text_view);
        mYear = mView.findViewById(R.id.view_year_text_view);
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }
}
