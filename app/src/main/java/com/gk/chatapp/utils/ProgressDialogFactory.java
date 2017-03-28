package com.gk.chatapp.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by ke.gao on 2017/3/28.
 */

public class ProgressDialogFactory {

    public static ProgressDialog getProgressDialog(Context context,String message){
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage(message);
        return dialog;
    }

}
