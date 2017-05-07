package com.xkikdev.xkik;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Settings class
 */

public class Settings {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    int save_version = 1;
    private boolean noReadreceipt = false; // read receipt allowed
    private boolean noTyping = false; // typing blocked
    private boolean fakeCamera = false; // fake camera enabled
    private boolean whosLurking = false;
    private int dateFormat = 0; // date format, currently only 0 and 1
    boolean devMode = false; // devMode mode enabled
    private HashMap<String, Integer> colors = new HashMap<String, Integer>(); // color settings
    private HashMap<String, String> strings = new HashMap<String, String>(); // string settings

    /**
     * Loads settings
     *
     * @return Settings object
     * @throws IOException
     */
    public static Settings load() throws IOException {
        if (getSaveFile().exists()) {
            return new Gson().fromJson(FileUtils.readFileToString(getSaveFile(), "UTF-8"), Settings.class);
        } else {
            Settings set = new Settings();
            set.save();
            return set;
        }
    }

    public boolean getNoReadreceipt() {
        return noReadreceipt;
    }

    public boolean getNoTyping() {
        return noTyping;
    }

    public void setDev(boolean b) {
        devMode = b;
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getWhosLurking(){
        return whosLurking;
    }

    public void setWhosLurking(boolean b){
        whosLurking = b;
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getDev() {
        return this.devMode;
    }

    public boolean getFakeCam() {
        return fakeCamera;
    }

    public void setDateFormat(int fmt) {
        dateFormat = fmt;
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setFakeCam(boolean b){
        fakeCamera = b;
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNoReadreceipt(boolean value) {
        noReadreceipt = value;
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setNoTyping(boolean value) {
        noTyping = value;
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setColor(String id, int color) {
        colors.put(id, color);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetColor(String id) {
        if (colors.containsKey(id)) {
            colors.remove(id);
        }
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setString(String id, String val) {
        strings.put(id, val);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetString(String id) {
        if (strings.containsKey(id)) {
            strings.remove(id);
        }
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getStrings() {
        return strings;
    }

    public HashMap<String, Integer> getColors() {
        return colors;
    }

    /**
     * Gets save directory
     *
     * @return Save directory as file
     */
    public static File getSaveDir() {
        File savedir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "XKik"+File.separator);
        if (!savedir.exists()) {
            savedir.mkdir();
        }
        return savedir;
    }

    /**
     * Gets save file
     *
     * @return save file
     */
    public static File getSaveFile() {
        return new File(getSaveDir().getPath() + File.separator + "config.json");
    }

    public void save() throws IOException {
        FileUtils.writeStringToFile(getSaveFile(), new Gson().toJson(this).toString(), "UTF-8", false);
        Util.killKik(null);
    }


}
