package com.mtvstudio.apkextractor;

import android.util.Log;

import com.mtvstudio.apkextractor.common.Constant;

import java.util.Locale;

/**
 * Created by sev_user on 5/26/2017.
 */

public class AppLog {

    private static final String TAG = "APK_EXTRACTOR_";


    public static void v(String string) {
        if (Constant.DEBUG_MODE) {
            Log.v(TAG , buildMessage(string));
        }
    }


    public static void d(String string) {
        Log.d(TAG , buildMessage(string));
    }


    public static void e(String string) {
        Log.e(TAG , buildMessage(string));
    }


    public static void i(String string) {
        Log.i(TAG , buildMessage(string));
    }

    /**

     * Formats the caller's provided message and prepends useful info like

     * calling thread ID and method name.

     */

    private static String buildMessage(String args) {

        String msg = args;

        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();



        String caller = "<unknown>";



        for (int i = trace.length - 1; i >= 0; i--) {

            if (trace[i].getClassName().contains("AppLog")) {

                if (i + 1 < trace.length) {

                    String callingClass = trace[i + 1].getClassName();

                    callingClass = callingClass.substring(callingClass.lastIndexOf('.') + 1);

                    callingClass = callingClass.substring(callingClass.lastIndexOf('$') + 1);

                    caller = callingClass + "." + trace[i + 1].getMethodName() + "[" + trace[i + 1].getLineNumber() + "]";

                }

                break;

            }

        }

        return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread().getId(), caller, msg);

    }
}
