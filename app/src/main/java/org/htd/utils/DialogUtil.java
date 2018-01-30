package org.htd.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;


/**
 * Created by www on 2017/11/16.
 */

public class DialogUtil {
    /**
     * 这是兼容的 AlertDialog
     */
    public static void showDialog(Context context, String message, String sure,
                                  String cancel, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("温馨提示").setCancelable(false).setPositiveButton(sure, listener).
                setNegativeButton(cancel, listener).setMessage(message).show();
    }

    public static ProgressDialog showProgressDialog(Context context) {
        ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("正在更新，请勿关闭！");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.setProgress(0);
        return pd;
    }

    public static AlertDialog createAlertDialog(Context context, View view) {
        AlertDialog dialog = new AlertDialog.Builder(context).setCancelable(true).create();
        dialog.setView(view);
        return dialog;
    }



}
