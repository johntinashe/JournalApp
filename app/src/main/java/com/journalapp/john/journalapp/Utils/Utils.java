package com.journalapp.john.journalapp.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;

import com.journalapp.john.journalapp.R;

import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class Utils {

    public static AlertDialog startProgress(Context context ,String title) {
        AlertDialog mDialogProg = new SpotsDialog(context, R.style.Custom);
        mDialogProg.setCancelable(false);
        mDialogProg.setCanceledOnTouchOutside(true);
        mDialogProg.setTitle(title);
       return mDialogProg;
    }

    public static String getYear(int i) {
        String months [] ;
        months = new String[ 13 ];
        months[ 0 ] = null;
        months[ 1 ] = "January";
        months[ 2 ] = "February";
        months[ 3 ] = "March";
        months[ 4 ] = "April";
        months[ 5 ] = "May";
        months[ 6 ] = "June";
        months[ 7 ] = "July";
        months[ 8 ] = "August";
        months[ 9 ] = "September";
        months[ 10 ] = "October";
        months[ 11 ] = "November";
        months[ 12 ] = "December";
        return months[i];
    }

    public  static boolean testForConnection(Context context) {
        boolean connected;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connected = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isAvailable()
                && connectivityManager.getActiveNetworkInfo().isConnected();
        return connected;
    }
}
