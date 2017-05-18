package com.xkikdev.xkik;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Main xposed class
 */

public class xkik_xposed implements IXposedHookLoadPackage, IXposedHookInitPackageResources {

    public static DateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
    public Settings settings = null;
    Activity chatContext = null;
    private Pattern fromPattern = Pattern.compile("from=\"(.*?)\"");
    private Pattern msgIdPattern = Pattern.compile("msgid id=\"(.*?)\"");
    private Pattern useridPattern = Pattern.compile("(.*)_[^_]*");
    private Object smileyManager = null;
    private Class smileyClass;


    public void updateSmileys(Class smileyClass) {
        if (smileyManager == null || smileyClass == null) {
            return;
        }
        List<Object> e = new ArrayList<>();
        for (kikSmiley s : settings.getSmileys()) {
            e.add(kikUtil.gen_smiley(smileyClass, s.title, s.text, s.id, s.idate));
        }
        XposedHelpers.callMethod(smileyManager, "a", e);
    }

    public void updateSmileys(Class smileyClass, kikSmiley smiley) {
        if (smileyManager == null || smileyClass == null || smiley == null) {
            return;
        }
        List<Object> e = new ArrayList<>();
        e.add(kikUtil.gen_smiley(smileyClass, smiley));
        XposedHelpers.callMethod(smileyManager, "a", e);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("kik.android")) {
            return;
        }
        settings = Settings.load(); // load settings
        format.setTimeZone(TimeZone.getDefault()); // set timezone, to keep accurate date correct

        /*
         * Since kik has classes & classes2.dex, this fixes it
         */
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                smileyClass = XposedHelpers.findClass(hooks.kikSmileyObj, loadPackageParam.classLoader);

                XposedHelpers.findAndHookMethod(hooks.kikActivityInit, loadPackageParam.classLoader, "a", Activity.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        chatContext = ((Activity) param.args[0]);
                        super.afterHookedMethod(param);
                    }
                });

                /*
                On smiley click
                 */
                XposedHelpers.findAndHookMethod(hooks.smileyView, loadPackageParam.classLoader, "onClick", View.class, new XC_MethodHook() {
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
                if (settings.getWhosLurking()) { // using this before hooking since it's very intensive when receiving a receipt
                    XposedHelpers.findAndHookMethod(hooks.KIK_RECEIPT_RECV, loadPackageParam.classLoader, "a", "kik.core.net.g", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            char[] txtBuf = (char[]) XposedHelpers.getObjectField(param.args[0], "srcBuf");
                            String resp = String.valueOf(txtBuf);
                            if (resp.contains("type=\"read\"")) {
                                Matcher fromMch = fromPattern.matcher(resp);
                                Matcher uuidMch = msgIdPattern.matcher(resp);
                                String from = null;
                                String uuid = null; // currently no use for this, but eventually will map a seen receipt to a specific message
                                if (fromMch.find()) {
                                    from = fromMch.group(1);
                                    Matcher userMatcher = useridPattern.matcher(from);
                                    if (userMatcher.find()) {
                                        from = userMatcher.group(1);
                                    }
                                }
                                if (uuidMch.find()) {
                                    uuid = uuidMch.group(1);
                                }
                                if (chatContext != null) {

                                    final String finalFrom = from;
                                    if (from != null && !from.equals("warehouse@talk.kik.com")) { // avoids some of the internal kik classes
                                        kikToast(finalFrom + " saw your message!");
                                    }

                                }
                                XposedBridge.log("from: " + from);
                            }
                            super.beforeHookedMethod(param);
                        }
                    });
                }

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
                XposedHelpers.findAndHookMethod(hooks.kikRecptMgr,loadPackageParam.classLoader, "a", String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String key = (String) param.args[0];
                        String value = (String) param.args[1];

                        if (key.equalsIgnoreCase("type")) { // receipt type
                            XposedBridge.log("type: " + value);

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
            }
        });

    }

    public void kikToast(final String text) {
        if (chatContext == null) {
            return;
        }
        chatContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(chatContext.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resParam) throws Throwable {
        if (!resParam.packageName.equals("kik.android")) {
            return;
        }

        if (settings == null) {
            return;
        }


        for (String c : settings.getColors().keySet()) { // replace colors
            try {
                resParam.res.setReplacement("kik.android", "color", c, settings.getColors().get(c));
            } catch (android.content.res.Resources.NotFoundException ex) {
                XposedBridge.log("Skipping unknown resource " + c);
            }
        }

        for (String s : settings.getStrings().keySet()) { // replace strings
            try {
                resParam.res.setReplacement("kik.android", "string", s, settings.getStrings().get(s));
            } catch (android.content.res.Resources.NotFoundException ex) {
                XposedBridge.log("Skipping unknown resource " + s);
            }
        }

    }
}
