package com.madnow.lgsdk.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Create by hanweiwei on 11/07/2018
 */
public final class TToast {
    private static Toast sToast;

    public static void show(Context context, String msg) {
        show(context, msg, Toast.LENGTH_SHORT);
    }

    public static void show(final Context context,final String msg, final int duration) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = getToast(context);
                if (toast != null) {
                    toast.setDuration(duration);
                    toast.setText(String.valueOf(msg));
                    toast.show();
                } else {
                    Log.i("TToast", "toast msg: " + String.valueOf(msg));
                }
            }
        });
    }

    @SuppressLint("ShowToast")
    private static Toast getToast(Context context) {
        if (context == null) {
            return sToast;
        }
//        if (sToast == null) {
//            synchronized (TToast.class) {
//                if (sToast == null) {
                    sToast = Toast.makeText(context.getApplicationContext(), "", Toast.LENGTH_SHORT);
//                }
//            }
//        }
        return sToast;
    }

    public static void reset() {
        sToast = null;
    }

}
