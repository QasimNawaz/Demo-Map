package com.example.alisons.demo_application.dialoge;

import android.app.ProgressDialog;
import android.content.Context;

public class WaitDialog {
    private static ProgressDialog waitDialog;

    public static void showWaitDialog(String message, Context mContext) {
        closeWaitDialog();
        waitDialog = new ProgressDialog(mContext);
        waitDialog.setTitle(message);
        waitDialog.show();
    }

    public static void closeWaitDialog() {
        try {
            waitDialog.dismiss();
        } catch (Exception e) {
            //
        }
    }
}
