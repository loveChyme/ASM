package com.plugin.logsdk;

import android.view.View;

/**
 * <p />Author: chenming
 * <p />E-mail: cm1@erongdu.com
 * <p />Date: 2020-03-31 17:49
 * <p />Description:
 */
@SuppressWarnings("unused")
public class TestLog {
    public static void logDetail(View view) {
        LogHelper.getInstance().logDetail(view);
    }
}
