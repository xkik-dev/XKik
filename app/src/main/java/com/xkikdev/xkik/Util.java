package com.xkikdev.xkik;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Various utilities
 */

public class Util {

    static Pattern pfpgtr = Pattern.compile("<meta property=\"og:image\".*?content=\"(.*?)\"\\/>");

    /**
     * Prints stacktrace - useful for debugging
     *
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
     *
     * @param obj The object
     * @param fld The field
     * @return The field, or null if it doesn't exist
     */
    static Object getObjField(Object obj, String fld) {
        try {
            return XposedHelpers.getObjectField(obj, fld);
        } catch (NoSuchFieldError e) {
            return null;
        }

    }

    /**
     * Prints all declared fields of a object
     *
     * @param start Object to analyze
     */
    public static void printDeclaredFields(Object start) {
        int lenlen = start.getClass().getDeclaredFields().length;
        for (int i = 0; i < lenlen; i++) {
            try {
                String name = start.getClass().getDeclaredFields()[i].getName();
                Object found = getObjField(start, name);
                if (found != null) {
                    XposedBridge.log("Declared field " + i + "(" + name + "): " + found.toString());
                }

            } catch (Exception ignored) {

            }

        }
    }


    /**
     * Kills Kik
     *
     * @param activity Activity to use for killing KIK
     * @throws IOException If killing failed
     */
    public static void killKik(Activity activity) throws IOException {
        if (activity != null) {
            if (killKIKService(activity)) {
                Toast.makeText(activity.getApplicationContext(), "Killed Kik in background.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static boolean killKIKService(Activity activity) throws IOException {
        ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            String packageName = serviceInfo.service.getPackageName();

            if (packageName.equals("kik.android")) {
                Process suProcess = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
                os.writeBytes("adb shell" + "\n");
                os.flush();
                os.writeBytes("am force-stop kik.android" + "\n");
                os.flush();
                return true;
            }
        }
        return false;
    }


    public static String getKikVersion(XC_LoadPackage.LoadPackageParam lpparam, PackageManager pm) {
        String apkName = lpparam.appInfo.sourceDir;
        String fullPath = Environment.getExternalStorageDirectory() + "/" + apkName;
        PackageInfo info = pm.getPackageArchiveInfo(fullPath, 0);
        return info.versionName;
    }

    public static void writeToFile(String out, String savedir) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(savedir, "UTF-8");
        writer.print(out);
        writer.close();
    }

    /**
     * Reads a url to a string, ignoring newlines
     *
     * @param url the url
     * @return the url contents
     */
    public static String urlToString(String url) {
        try {
            String out = "";
            URL urlo = new URL(url);
            InputStream is = urlo.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null)
                out += line;

            br.close();
            is.close();
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Calendar epoch2Calendar(long epoch){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(epoch);
        return c;
    }

    public static Bitmap getUserProfilePicture(String uname){
        String in = urlToString("http://kik.me/"+uname);
        Matcher m = pfpgtr.matcher(in);
        if (m.find()){
            String pfpurl = m.group(1);
            if (pfpurl.equals("")){
                return null;
            }else{
                return getBitmapFromURL(pfpurl);
            }

        }
        return null;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
}
