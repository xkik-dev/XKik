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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    int save_version = 1;
    private boolean noHook = false;
    private boolean devMode = false; // devMode mode enabled
    private boolean noReadreceipt = false; // read receipt allowed
    private boolean noTyping = false; // typing blocked
    private boolean fakeCamera = false; // fake camera enabled
    private boolean whosLurking = false;
    private boolean autoSmiley = false;
    private boolean darkBg = false;
    private boolean disableSave = false;
    private boolean disableFwd = false;
    private boolean autoloop = false;
    private boolean autoplay = false;
    private boolean automute = false;
    private boolean unfilterGIFs = false;
    private boolean mainacctenabled = true;
    private boolean BETA = false;
    private transient Activity creator;
    private boolean longCam = false;
    private int dateFormat = 0; // date format, currently only 0 and 1
    private List<File> fileList = new ArrayList<>();
    private HashMap<String, Integer> colors = new HashMap<String, Integer>(); // color settings
    private HashMap<String, String> strings = new HashMap<String, String>(); // string settings
    private ArrayList<kikSmiley> smileys = new ArrayList<>();
    private ArrayList<KikAccount> extraAccts = new ArrayList<>();
    private HashMap<String, longStringarray> whoread = new HashMap<>();

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity app activity
     */
    static void verifyStoragePermissions(Activity activity) {
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
            set.save(true);
            return set;
        }
    }

    public static Settings load(Activity creator) throws IOException {
        Settings s = load();
        s.setCreator(creator);
        return s;
    }

    /**
     * Gets save directory
     *
     * @return Save directory as file
     */
    public static File getSaveDir() {
        File savedir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "XKik" + File.separator);
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
    private static File getSaveFile() {
        return new File(getSaveDir().getPath() + File.separator + "config.json");
    }

    public boolean getNoHook() {
        return noHook;
    }

    public void setNoHook(boolean noHook) {
        this.noHook = noHook;
    }


    public List<File> getFileList() {
        return fileList;
    }

    public void setFileList(List<File> value, boolean kill) {
        fileList = value;
        trySave(kill);
    }


    /**
     * Get the who read hashmap
     *
     * @return the who read hashmap
     */
    public HashMap<String, longStringarray> getWhoread() {
        return whoread;
    }

    /**
     * Add a user who read a message
     *
     * @param who  Who read it
     * @param uuid UUID of message
     */
    public void addWhoread(String who, String uuid) {
        if (whoread.containsKey(uuid)) {
            if (!whoread.get(uuid).contains(who)) {
                whoread.get(uuid).addStrarr(who);
            }
        } else {
            whoread.put(uuid, new longStringarray(System.currentTimeMillis() + 604800000L, new ArrayList<String>()));
            whoread.get(uuid).addStrarr(who);
        }
        purgeWhoread();
        try {
            save(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Purges the config file from old read notifications, to keep file size small
     */
    public void purgeWhoread() {
        for (String key : whoread.keySet()) {
            if (whoread.get(key).getLong() < System.currentTimeMillis()) {
                whoread.remove(key);
            }
        }
    }

    public ArrayList<KikAccount> getExtraAccts() {
        return extraAccts;
    }

    public KikAccount getAcct(String name) {
        for (KikAccount ka : getExtraAccts()) {
            if (ka.getName().equals(name)) {
                return ka;
            }
        }
        return null;
    }

    public void addExtraAcct(String name) {
        extraAccts.add(new KikAccount(name));
    }

    public void removeExtraAcct(String name) {
        KikAccount rm = getAcct(name);
        if (rm != null) {
            extraAccts.remove(rm);
        }
    }

    public boolean getMainacctenabled() {
        return mainacctenabled;
    }

    public void setMainacctenabled(boolean mainacctenabled) {
        this.mainacctenabled = mainacctenabled;
    }

    public boolean isBETA() {
        return BETA;
    }

    public void setBETA(boolean BETA) {
        this.BETA = BETA;
    }

    public boolean getDarkBg() {
        return darkBg;
    }

    public void setDarkBg(boolean darkBg) {
        this.darkBg = darkBg;
        trySave();
    }

    private void trySave() {
        trySave(true);
    }

    public Activity getCreator() {
        return creator;
    }

    public void setCreator(Activity creator) {
        this.creator = creator;
    }

    public boolean getLongCam() {
        return longCam;
    }

    public void setLongCam(boolean longCam) {
        this.longCam = longCam;
        trySave();
    }

    public boolean getAutoSmiley() {
        return autoSmiley;
    }

    public void setAutoSmiley(boolean autoSmiley, boolean kill) {
        this.autoSmiley = autoSmiley;
        trySave(kill);
    }

    private void trySave(boolean kill) {
        try {
            save(kill);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get currently added smileys
     *
     * @return currently added smileys
     */
    public ArrayList<kikSmiley> getSmileys() {
        return smileys;
    }

    /**
     * Add a smiley
     *
     * @param ks   The smiley
     * @param kill Kill kik once added?
     */
    public void addSmiley(kikSmiley ks, boolean kill) {
        if (ks == null) {
            return;
        }
        if (!containsSmiley(ks)) {
            smileys.add(ks);
            trySave(kill);
        }
    }

    /**
     * Remove a smiley
     *
     * @param ks   The smiley
     * @param kill Kill kik once removed?
     */
    public void deleteSmiley(kikSmiley ks, boolean kill) {
        if (ks == null) {
            return;
        }
        if (containsSmiley(ks)) {
            smileys.remove(ks);
            trySave(kill);
        }
    }

    /**
     * Checks if smiley is already added
     *
     * @param ks The smiley
     * @return If it exists or not
     */
    public boolean containsSmiley(kikSmiley ks) {
        for (kikSmiley smil : getSmileys()) {
            if (smil.id.equals(ks.id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if smiley is already added
     *
     * @param id The smiley ID
     * @return If it exists or not
     */
    public boolean containsSmiley(String id) {
        for (kikSmiley smil : getSmileys()) {
            if (smil.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean getNoReadreceipt() {
        return noReadreceipt;
    }

    public void setNoReadreceipt(boolean value, boolean kill) {
        noReadreceipt = value;
        trySave(kill);
    }

    public boolean getNoTyping() {
        return noTyping;
    }

    public void setNoTyping(boolean value, boolean kill) {
        noTyping = value;
        trySave(kill);
    }

    public boolean getWhosLurking() {
        return whosLurking;
    }

    public void setWhosLurking(boolean b, boolean kill) {
        whosLurking = b;
        trySave(kill);
    }

    public boolean getDev() {
        return this.devMode;
    }

    public void setDev(boolean b, boolean kill) {
        devMode = b;
        trySave(kill);
    }

    public boolean getFakeCam() {
        return fakeCamera;
    }

    public void setFakeCam(boolean b, boolean kill) {
        fakeCamera = b;
        trySave(kill);
    }

    public int getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(int fmt, boolean kill) {
        dateFormat = fmt;
        trySave(kill);
    }

    public void setColor(String id, int color, boolean kill) {
        colors.put(id, color);
        trySave(kill);
    }

    public boolean getDisableSave() {
        return disableSave;
    }

    public void setDisableSave(boolean disableSave) {
        this.disableSave = disableSave;
        trySave();
    }

    public boolean getDisableFwd() {
        return disableFwd;
    }

    public void setDisableFwd(boolean disableFwd) {
        this.disableFwd = disableFwd;
        trySave();
    }

    public boolean getAutoLoop() {
        return autoloop;
    }

    public void setAutoLoop(boolean AutoLoop) {
        this.autoloop = AutoLoop;
        trySave();
    }

    public boolean getAutoMute() {
        return automute;
    }

    public void setAutoMute(boolean AutoMute) {
        this.automute = AutoMute;
        trySave();
    }

    public boolean getAutoPlay() {
        return autoplay;
    }

    public void setAutoplay(boolean autoPlay) {
        this.autoplay = autoPlay;
        trySave();
    }

    public boolean getUnfilterGIFs() {
        return unfilterGIFs;
    }

    public void setUnfilterGIFs(boolean unfilterGIFs) {
        this.unfilterGIFs = unfilterGIFs;
        trySave();
    }

    /**
     * Reset a color to it's default value
     *
     * @param id Color ID
     */
    public void resetColor(String id, boolean kill) {
        if (colors.containsKey(id)) {
            colors.remove(id);
        }
        trySave(kill);
    }

    public void setString(String id, String val, boolean kill) {
        strings.put(id, val);
        trySave(kill);
    }

    /**
     * Reset a string to it's default value
     *
     * @param id String ID
     */
    public void resetString(String id, boolean kill) {
        if (strings.containsKey(id)) {
            strings.remove(id);
        }
        trySave(kill);
    }

    public HashMap<String, String> getStrings() {
        return strings;
    }

    public HashMap<String, Integer> getColors() {
        return colors;
    }

    public void save(boolean kill) throws IOException {
        FileUtils.writeStringToFile(getSaveFile(), new Gson().toJson(this).toString(), "UTF-8", false);
        if (kill) {
            Util.killKik(creator);
        }
    }


}
