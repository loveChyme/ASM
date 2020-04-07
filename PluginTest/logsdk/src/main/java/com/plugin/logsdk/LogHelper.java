package com.plugin.logsdk;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.View;


/**
 * <p />Author: chenming
 * <p />E-mail: cm1@erongdu.com
 * <p />Date: 2020-04-01 15:00
 * <p />Description:
 */
public class LogHelper {
    private static final Object mLock = new Object();
    private static LogHelper instance;
    public static LogHelper getInstance(){
        synchronized (mLock) {
            if (instance == null)
                instance = new LogHelper();
        }
        return instance;
    }

    private static final String TAG = "TestLog";
    public void logDetail(View view) {
        System.out.println("123456789");
        Activity activity = getActivityFromContext(view.getContext());
        if (activity != null) {
            Log.d(TAG, activity.getClass().getCanonicalName());
        }
        Log.d(TAG, "植入日志代码");
    }

    public static Activity getActivityFromContext(Context context) {
        Activity activity = null;
        try {
            if (context != null) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                } else if (context instanceof ContextWrapper) {
                    while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                        context = ((ContextWrapper) context).getBaseContext();
                    }
                    if (context instanceof Activity) {
                        activity = (Activity) context;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }

}
