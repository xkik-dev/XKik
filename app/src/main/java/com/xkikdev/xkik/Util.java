package com.xkikdev.xkik;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Various utilities
 */

public class Util {

    /**
     * Prints stacktrace - useful for debugging
     * @param prefix Prefix to use for each line
     */
    public static void printStack(String prefix) {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i = 0; i < ste.length; i++) {
            XposedBridge.log(prefix + " " + i + ": " + ste[i].toString());
        }
    }

    /**
     * Gat a field from an object
     * @param obj The object
     * @param fld The field
     * @return The field, or null if it doesn't exist
     */
    public static Object getObjField(Object obj, String fld) {
        try {
            return XposedHelpers.getObjectField(obj, fld);
        } catch (NoSuchFieldError e) {
            return null;
        }

    }

    /**
     * Prints all declared fields of a object
     * @param start Object to analyze
     */
    public static void printDeclaredFields(Object start) {
        int lenlen = start.getClass().getDeclaredFields().length;
        for (int i = 0; i < lenlen; i++) {
            try {
                String name = start.getClass().getDeclaredFields()[i].getName();
                Object found = getObjField(start, name);
                XposedBridge.log("Declared field " + i + "(" + name + "): " + found.toString());

            } catch (Exception ignored) {

            }

        }
    }



    /**
     * Kills Kik
     *
     * @param activity Activity to display toast on; null for no toast
     * @throws IOException
     */
    public static void killKik(Activity activity) throws IOException {
        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream os = new DataOutputStream(p.getOutputStream());

        os.writeBytes("am force-stop kik.android\n");
        os.writeBytes("exit\n");
        os.flush();


        if (activity != null) {
            Toast.makeText(activity.getApplicationContext(), "Killed Kik", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getKikVersion(XC_LoadPackage.LoadPackageParam lpparam,PackageManager pm) {
        String apkName = lpparam.appInfo.sourceDir;
        String fullPath = Environment.getExternalStorageDirectory() + "/" + apkName;
        PackageInfo info = pm.getPackageArchiveInfo(fullPath, 0);
        return info.versionName;
    }
}
