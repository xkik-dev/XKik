package com.xkikdev.xkik;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

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

    public Settings settings = null;
    public static DateFormat format = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

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
                Class recpt_mgr = XposedHelpers.findClass(hooks.kikRecptMgr, loadPackageParam.classLoader);

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
                Dev mode
                 */
                XposedHelpers.findAndHookMethod(hooks.kikDeviceUtils, loadPackageParam.classLoader, "f", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (settings.get_dev()) {
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
                XposedHelpers.findAndHookMethod(recpt_mgr, "a", String.class, String.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String key = (String) param.args[0];
                        String value = (String) param.args[1];

                        if (key.equalsIgnoreCase("type")) { // receipt type
                            XposedBridge.log("type: " + value);

                            if (value.equalsIgnoreCase("read")) { // read receipt
                                Util.printStack("read");
                                if (settings != null && settings.isNoReadreceipt()) {
                                    XposedBridge.log("Blocked a read receipt");
                                    param.setResult(null);
                                }
                            } else if (value.equalsIgnoreCase("is-typing")) { // typing receipt
                                if (settings != null && settings.isNoTyping()) {
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
