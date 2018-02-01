package com.xkikdev.xkik;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xkikdev.xkik.config_activities.VisualFragment;
import com.xkikdev.xkik.config_activities.quickConfig;
import com.xkikdev.xkik.datatype_parsers.msgText;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.xkikdev.xkik.hooks.kikMessage;

/**
 * Main xposed class
 */

@SuppressLint("SimpleDateFormat")
public class xkik_xposed implements IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {
    private static final String kikChatFragment = hooks.kikChatFragment;
    private static final String kikCamObj = "kik.android.c.d";
    public static Settings settings = null;
    public static XModuleResources resources;
    private static DateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
    private final int longvidTime = (int) TimeUnit.MINUTES.toMillis(2);
    private Context chatContext = null;
    private Pattern fromPattern = Pattern.compile("from=\"(.*?)\"");
    private Pattern msgIdPattern = Pattern.compile("msgid id=\"(.*?)\"");
    private Pattern useridPattern = Pattern.compile("(.*)_[^_]*");
    private Pattern timestampPattern = Pattern.compile("cts=\"(.*?)\"");
    private Object smileyManager = null;
    private Class smileyClass;
    private String MODULE_PATH;

    private void updateSmileys(Class smileyClass) {
        if (smileyManager == null || smileyClass == null) {
            return;
        }
        List<Object> e = new ArrayList<>();
        for (kikSmiley s : settings.getSmileys()) {
            e.add(kikUtil.gen_smiley(smileyClass, s.title, s.text, s.id, s.idate));
        }
        XposedHelpers.callMethod(smileyManager, "a", e);
    }

    private void updateSmileys(Class smileyClass, kikSmiley smiley) {
        if (smileyManager == null || smileyClass == null || smiley == null) {
            return;
        }
        List<Object> e = new ArrayList<>();
        e.add(kikUtil.gen_smiley(smileyClass, smiley));
        XposedHelpers.callMethod(smileyManager, "a", e);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        final xposedObject[] camObj = new xposedObject[1];
        if (!loadPackageParam.packageName.equals(hooks.kikPKG)) {
            return;
        }
        settings = Settings.load(); // load settings
        if (settings.getNoHook()) {
            return;
        }
        format.setTimeZone(TimeZone.getDefault()); // set timezone, to keep accurate date correct


        /*
         * Since kik has classes & classes2.dex, this fixes it
         */
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam aparam) throws Throwable {
                smileyClass = XposedHelpers.findClass(hooks.kikSmileyObj, loadPackageParam.classLoader);

                /*
                Get KIK context
                 */
                XposedHelpers.findAndHookMethod(hooks.kikActivityInit, loadPackageParam.classLoader, "a", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        chatContext = ((Context) param.args[0]);
                        super.afterHookedMethod(param);
                    }
                });

                if (settings.getLongCam()) { // In order to avoid checking in each method, this is set
                    /*
                This sets camObj so it doesn't interfere with any other apps that use MediaRecorder
                 */
                    XposedHelpers.findAndHookMethod(kikCamObj, loadPackageParam.classLoader, "a", String.class, new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            camObj[0] = new xposedObject(param.thisObject);
                            super.beforeHookedMethod(param);
                        }
                    });



                /*
                Sets camera max duration to longvidTime
                 */
                    XposedHelpers.findAndHookMethod(MediaRecorder.class, "setMaxDuration", int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (param.thisObject == null || camObj[0] == null) {
                                return;
                            }
                            if (param.thisObject.equals
                                    (camObj[0].get("i"))) {
                                param.args[0] = longvidTime;
                            }
                            super.beforeHookedMethod(param);
                        }
                    });

                    /*
                Sets camera max file size to 20mb
                 */
                    XposedHelpers.findAndHookMethod(MediaRecorder.class, "setMaxFileSize", long.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (param.thisObject == null || camObj[0] == null) {
                                return;
                            }
                            if (param.thisObject.equals
                                    (camObj[0].get("i"))) {
                                param.args[0] = 20971520L; // 20mb is the max KIK file size playable
                            }
                            super.beforeHookedMethod(param);
                        }
                    });

                /*
                Modifies the circle progress bar while recording to be correct with the modified
                time
                 */
                    XposedHelpers.findAndHookMethod(hooks.kikCircleBar, loadPackageParam.classLoader, "a", int.class, new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            float f = ((int) methodHookParam.args[0]) * 1F;
                            float calc = 360.0f * ((f) / ((float) longvidTime));
                            xposedObject thiso = new xposedObject(methodHookParam.thisObject);
                            thiso.getXObj("_shutterButton").call("a", calc);
                            thiso.getXObj("_videoTime").call("setText", XposedHelpers.callStaticMethod(XposedHelpers.findClass(hooks.kikRecordTextMgr, loadPackageParam.classLoader), "a", methodHookParam.args[0]));
                            return null;
                        }
                    });

                /*
                Adjusts the onTick method to calculte with the new, modified time
                 */
                    XposedHelpers.findAndHookMethod(hooks.kikCameraTimer, loadPackageParam.classLoader, "onTick", long.class, new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            int tim = (int) Math.max(0, longvidTime - ((long) methodHookParam.args[0]));
                            new xposedObject(methodHookParam.thisObject).getXObj("a").set("h", tim);
                            new xposedObject(methodHookParam.thisObject).getXObj("a").getXObj("r").call("b", tim);
                            return null;
                        }
                    });

                /*
                Modifies the CountDownTimer to use the modded time
                 */
                    XposedHelpers.findAndHookConstructor(hooks.kikCameraTimer, loadPackageParam.classLoader, kikCamObj, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            new xposedObject(param.thisObject).set("mMillisInFuture", longvidTime);
                            super.afterHookedMethod(param);
                        }
                    });
                }

                /*
                Settings button create; Hook onLongClick
                 */
                XposedHelpers.findAndHookMethod(hooks.kikConversationFragment, loadPackageParam.classLoader, "onCreateView", LayoutInflater.class
                        , ViewGroup.class, Bundle.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                View v = (View) XposedHelpers.getObjectField(param.thisObject, "_settingsButton");
                                v.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        quickConfig qc = new quickConfig();
                                        qc.show(((Activity) v.getRootView().getContext()).getFragmentManager(), "quick_config");
                                        return true;
                                    }
                                });
                                super.afterHookedMethod(param);
                            }
                        });

                /*
                On smiley click
                 */
                XposedHelpers.findAndHookMethod(hooks.smileyView, loadPackageParam.classLoader, "a", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (settings.getAutoSmiley()) {
                            //String type = (String) Util.getObjField(param.thisObject,"f");
                            final String id = (String) Util.getObjField(param.thisObject, "b");
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        kikSmiley ks = kikUtil.smileyFromID(id);
                                        settings.addSmiley(ks, false);
                                        updateSmileys(smileyClass, ks);
                                        if (ks != null) {
                                            kikToast("Smiley \"" + ks.title + "\" Added!");
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    super.run();
                                }
                            }.start();
                            param.setResult(null);
                        }
                        super.beforeHookedMethod(param);
                    }
                });

                /*
                Smiley Manager
                 */
                XposedHelpers.findAndHookConstructor(hooks.kikSmileyManager, loadPackageParam.classLoader, Context.class, "kik.core.interfaces.ad", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        smileyManager = param.thisObject;
                        updateSmileys(smileyClass);
                        super.afterHookedMethod(param);
                    }
                });


                /*
                Camera spoofing, for gallery images
                 */
                XposedHelpers.findAndHookConstructor(hooks.kikContentMessage, loadPackageParam.classLoader, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (settings.getFakeCam()) {
                            String type = (String) param.args[0];
                            if (type.equalsIgnoreCase("com.kik.ext.gallery")) {
                                param.args[0] = "com.kik.ext.camera";
                            } else if (type.equalsIgnoreCase("com.kik.ext.video-gallery")) {
                                param.args[0] = "com.kik.ext.video-camera";
                            }
                        }
                        super.beforeHookedMethod(param);
                    }
                });


                /*
                    Who's lurking feature
                 */
                XposedHelpers.findAndHookMethod(hooks.KIK_RECEIPT_RECV, loadPackageParam.classLoader, "a", hooks.kikReceiptParser, new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        Vector<String> msgs = (Vector<String>) new xposedObject(param.thisObject).get("m");

                        if (new xposedObject(param.thisObject).get("l").equals(500)) {
                            String whoJID = (String) new xposedObject(param.thisObject).getXObj("b").get("d");
                            String from;
                            Matcher userMatcher = useridPattern.matcher(whoJID);
                            if (userMatcher.find()) {
                                from = userMatcher.group(1);
                            } else {
                                return;
                            }
                            if (from == null || from.equals("warehouse@talk.kik.com")) { // avoids kik internal classes
                                return;
                            }
                            for (String uuid : msgs) {
                                settings.addWhoread(from, uuid);
                            }
                            if (settings.getLurkingToast()) {
                                kikToast(from + " saw your message!");
                            }
                        }
                    }
                });

                /*
                Who's lurking displayer
                 */
                XposedHelpers.findAndHookMethod(hooks.kABSTRACT_MESSAGE_VIEW_MODEL, loadPackageParam.classLoader, "a", hooks.kABSTRACT_MESSAGE_VIEW_MODEL, Long.class, Boolean.class, kikMessage, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Boolean on = (Boolean) param.args[2];

                        if (on) {
                            String UUID = new msgText(new xposedObject(param.args[0]).get("g")).getUUID();
                            XposedBridge.log("opend msg " + UUID);
                            if (settings.getWhoread().containsKey(UUID)) {
                                String sby = "Seen by ";
                                sby += StringUtils.join(settings.getWhoread().get(UUID).getStrarr().toArray(), ", ");

                                param.setResult(param.getResult() + " - " + sby);
                            }
                        }
                        super.afterHookedMethod(param);
                    }
                });

                /*
                Dev mode
                 */
                XposedHelpers.findAndHookMethod(hooks.kikDeviceUtils, loadPackageParam.classLoader, "f", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (settings.getDev()) {
                            param.setResult(true);
                        }
                        super.beforeHookedMethod(param);
                    }
                });

                /*
                Set date display format, instead of "5 minutes ago"
                 */
                XposedHelpers.findAndHookMethod(hooks.kikDateDisplayManager, loadPackageParam.classLoader, "a", long.class, long.class, Resources.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        /*
                        param 0 = older time
                        param 1 = current time
                        more params will possibly be added in the future
                        */
                        switch (settings.getDateFormat()) {
                            case 1:
                                long old_time = (long) param.args[0];
                                //long diff = (long)param.args[1]-(long)param.args[0];
                                String formatted = format.format(old_time);
                                param.setResult(formatted);

                                break;
                            case 2:

                                break;
                            default:
                                break;
                        }
                        super.beforeHookedMethod(param);
                    }
                });

                /*
                Receipt blocker
                 */
                XposedHelpers.findAndHookMethod(hooks.kikRecptMgr, loadPackageParam.classLoader, "d", String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                        String key = (String) param.args[0];
                        String value = (String) param.args[1];
                        if (key.equalsIgnoreCase("type")) { // receipt type
                            //XposedBridge.log("type: " + value);

                            if (value.equalsIgnoreCase("read")) { // read receipt
                                if (settings != null && settings.getNoReadreceipt()) {
                                    XposedBridge.log("Blocked a read receipt");
                                    param.setResult(null);
                                }
                            } else if (value.equalsIgnoreCase("is-typing")) { // typing receipt
                                if (settings != null && settings.getNoTyping()) {
                                    XposedBridge.log("Blocked a typing receipt");
                                    param.setResult(null);
                                }
                            }

                        }
                        super.beforeHookedMethod(param);
                    }
                });

                /*
                Sets all incoming messages to white to fix the transparency issue
                 */
                XposedBridge.hookAllConstructors(XposedHelpers.findClass(hooks.kikBubbleFrameLayout, loadPackageParam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        //Gets the root of all incoming messages
                        View view = (View) XposedHelpers.callMethod(param.thisObject, "getRootView");

                        //Default color in the case of a default parameter or lack of settings.
                        int backgroundColor = Color.WHITE;
                        try {
                            if (settings != null && settings.getColors().containsKey(VisualFragment.incomingColor)) {
                                //Gets the color from the settings
                                backgroundColor = settings.getColors().get(VisualFragment.incomingColor);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //Sets the background
                        view.setBackgroundColor(backgroundColor);

                        //In the case of transparent item, it will correct it on any change
                        //Changes may turn color into transparent, this reverts it
                        final int finalColor = backgroundColor;
                        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                            @Override
                            public void onViewAttachedToWindow(View v) {
                                //If transparent and resets it
                                if (v.getBackground() == null) {
                                    v.setBackgroundColor(finalColor);
                                    v.invalidate();
                                }
                            }

                            @Override
                            public void onViewDetachedFromWindow(View v) {
                            }
                        });
                    }
                });

                /*
                Gets the chat fragment. Currently does nothing but
                plans include adding animated backgrounds
                 */
                XposedHelpers.findAndHookMethod(kikChatFragment, loadPackageParam.classLoader, "onCreateView", LayoutInflater.class, ViewGroup.class, Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        //Gets the Kik activity
                        final Activity kact = (Activity) XposedHelpers.callMethod(param.thisObject, "getActivity");
                        //Gets the layout where we can find the background
                        final FrameLayout fl = (FrameLayout) param.getResult();

                        //Make sure there is a file selected
                        if (!settings.getFileList().isEmpty()) {
                            //Randomizes the selection
                            File file = settings.getFileList().get(new Random().nextInt(
                                    settings.getFileList().size()));

                            //If file is real
                            if (file.exists()) {
                                FileInputStream streamIn = new FileInputStream(file);

                                //Decodes the image
                                Bitmap bitmap = BitmapFactory.decodeStream(streamIn); //This gets the image

                                streamIn.close();

                                //Sets the background as a drawable
                                fl.getChildAt(0).setBackground(new BitmapDrawable(kact.getResources(), bitmap)); //background
                            }
                        } else if (settings.isBETA()) {
                            new Thread() {
                                @Override
                                public void run() {
                                    final View v = (View) XposedHelpers.getObjectField(param.thisObject, "_messageRecyclerView");
                                    final Bitmap b = Bitmap.createBitmap(768, 1280 / 2, Bitmap.Config.ARGB_8888);
                                    final Canvas c = new Canvas(b);
                                    final WaveView wv = new WaveView(768, 1280 / 2);
                                    wv.setWaveColor(Color.RED, Color.GREEN);
                                    wv.setShowWave(true);
                                    int time = 0;
                                    while (true) {
                                        time++;
                                        if (v != null) {
                                            if (wv.getWaveShiftRatio() >= 1F) {
                                                wv.setWaveShiftRatio(0F);
                                            }

                                            wv.setAmplitudeRatio((float) Math.abs(Math.sin(.04 * time) * .005));
                                            wv.setWaveShiftRatio(wv.getWaveShiftRatio() + 0.01F);
                                            wv.draw(c);

                                            kact.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    v.setBackground(new BitmapDrawable(v.getResources(), b));
                                                }
                                            });
                                            try {
                                                Thread.sleep(10);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            }.start();
                        }
                    }
                });

                 /*
                started working on do not disturb

                XposedHelpers.findAndHookMethod(XposedHelpers.findClass(hooks.KikApplication, loadPackageParam.classLoader), "a", kikMessage, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Class<?> messageClass = param.args[0].getClass();
                        String JID = String.valueOf(messageClass.getMethod("i");
                        XposedBridge.log("Debug" + JID);
                        super.afterHookedMethod(param);
                    }
                });

                */

                /*
                Manages bypass options
                 */
                XposedBridge.hookAllMethods(XposedHelpers.findClass(hooks.kikContentMessage, loadPackageParam.classLoader), "y", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if(settings.getBypassSave()) param.setResult(false);
                        super.afterHookedMethod(param);
                    }
                });

                /*
                Manages most of the media options
                 */
                XposedBridge.hookAllMethods(XposedHelpers.findClass(hooks.kikMsgClass, loadPackageParam.classLoader), "a", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.getResult() != null && param.getResult().getClass().getName().equals(hooks.kikContentMessage)) {
                            if (settings.getDisableFwd()) {
                                XposedHelpers.callMethod(param.getResult(), "a", "allow-forward", "false");
                            }
                            if (settings.getDisableSave()) {
                                XposedHelpers.callMethod(param.getResult(), "a", "disallow-save", "true");
                            }
                            if (settings.getAutoLoop()) {
                                XposedHelpers.callMethod(param.getResult(), "a", "video-should-loop", "true");
                            }
                            if (settings.getAutoMute()) {
                                XposedHelpers.callMethod(param.getResult(), "a", "video-should-be-muted", "true");
                            }
                            if (settings.getAutoPlay()) {
                                XposedHelpers.callMethod(param.getResult(), "a", "video-should-autoplay", "true");
                            }

                        }
                        super.afterHookedMethod(param);
                    }
                });

                /*
                Changes GIF filter to "R"
                 */
                XposedHelpers.findAndHookMethod(hooks.kikGifApi, loadPackageParam.classLoader, "a", String.class, hooks.kikGifSearchRating, Locale.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (settings.getUnfilterGIFs()) {
                            Class c = param.args[1].getClass();
                            param.args[1] = Enum.valueOf(c, "GifSearchRatingR");
                            super.beforeHookedMethod(param);
                        }
                    }
                });

            }
        });

    }

    /**
     * Generates a toast using KIK's context
     *
     * @param text Text to display on toast
     */
    private void kikToast(final String text) {
        if (chatContext == null) {
            return;
        }

        ((Activity) chatContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(chatContext.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void handleInitPackageResources(final XC_InitPackageResources.InitPackageResourcesParam resParam) throws Throwable {
        if (!resParam.packageName.equals(hooks.kikPKG)) {
            return;
        }
        final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resParam.res);

        if (settings != null && settings.getDarkBg()) {
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "bottom_incoming_bubble_mask", modRes.fwd(R.drawable.bottom_incoming_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "bottom_outgoing_bubble_mask", modRes.fwd(R.drawable.bottom_outgoing_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "bottom_outgoing_image_bubble_mask", modRes.fwd(R.drawable.bottom_outgoing_image_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "middle_incoming_bubble_mask", modRes.fwd(R.drawable.middle_incoming_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "middle_outgoing_bubble_mask", modRes.fwd(R.drawable.middle_outgoing_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "middle_outgoing_image_bubble_mask", modRes.fwd(R.drawable.middle_outgoing_image_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "outgoing_top_round_bubble_mask", modRes.fwd(R.drawable.outgoing_top_round_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "outgoing_top_square_bubble_mask", modRes.fwd(R.drawable.outgoing_top_square_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "single_incoming_bubble_mask", modRes.fwd(R.drawable.single_incoming_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "single_outgoing_bubble_mask", modRes.fwd(R.drawable.single_outgoing_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "single_outgoing_image_bubble_mask", modRes.fwd(R.drawable.single_outgoing_image_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "top_incoming_bubble_mask", modRes.fwd(R.drawable.top_incoming_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "top_outgoing_bubble_mask", modRes.fwd(R.drawable.top_outgoing_bubble_mask));
            resParam.res.setReplacement(hooks.kikPKG, "drawable", "top_outgoing_image_bubble_mask", modRes.fwd(R.drawable.top_outgoing_image_bubble_mask));

            resParam.res.hookLayout(hooks.kikPKG, "layout", "activity_chat", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(LayoutInflatedParam layoutInflatedParam) throws Throwable {

                    View rv = layoutInflatedParam.view.findViewById(layoutInflatedParam.res.getIdentifier("messages_list", "id", hooks.kikPKG));
                    rv.setBackgroundColor(Color.rgb(43, 43, 43));
                }
            });
        }

        resources = XModuleResources.createInstance(MODULE_PATH, resParam.res);

        if (settings == null) {
            return;
        }

        resParam.res.hookLayout(hooks.kikPKG, "layout", "outgoing_message_bubble", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam layoutInflatedParam) throws Throwable {
                scrollingText((TextView) layoutInflatedParam.view.findViewById(layoutInflatedParam.res.getIdentifier("message_timestamp", "id", hooks.kikPKG)));
            }
        });

        resParam.res.hookLayout(hooks.kikPKG, "layout", "kik_back_button", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam layoutInflatedParam) throws Throwable {
                scrollingText((TextView) layoutInflatedParam.view.findViewById(layoutInflatedParam.res.getIdentifier("title_view", "id", hooks.kikPKG)));
            }
        });

        resParam.res.hookLayout(hooks.kikPKG, "layout", "list_entry_conversations", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam layoutInflatedParam) throws Throwable {
                scrollingText((TextView) layoutInflatedParam.view.findViewById(layoutInflatedParam.res.getIdentifier("conversation_name", "id", hooks.kikPKG)));
            }
        });

        /*
        Replaces colors
         */
        for (final String c : settings.getColors().keySet()) {
            if (c.startsWith("#xkik")) {
                // special conditions handled here
            } else {
                try {
                    resParam.res.setReplacement(hooks.kikPKG, "color", c, settings.getColors().get(c));
                } catch (android.content.res.Resources.NotFoundException ex) {
                    XposedBridge.log("Skipping unknown resource " + c);
                }
            }
        }

        /*
        Replaces strings
         */
        for (String s : settings.getStrings().keySet()) {
            try {
                resParam.res.setReplacement(hooks.kikPKG, "string", s, settings.getStrings().get(s));
            } catch (android.content.res.Resources.NotFoundException ex) {
                XposedBridge.log("Skipping unknown resource " + s);
            }
        }

    }

    public static void scrollingText(TextView tv){
        if (settings.getScrollingtxt()) {
            tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tv.setSelected(true);
            tv.setSingleLine(true);
            tv.setMarqueeRepeatLimit(-1);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }


}
