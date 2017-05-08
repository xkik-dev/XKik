package com.xkikdev.xkik;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Utilities for Kik itself, i.e generating kik objects
 */

public class kikUtil {


    /**
     * Generate a kik smiley type, currently beta
     *
     * @param smiley_class smiley manager class
     * @param title        smiley title
     * @param text         smiley text used to generate it, i.e <3
     * @param id           smiley id
     * @param installdate  smiley install ID
     * @return a smiley object
     */
    public static Object gen_smiley(Class smiley_class, String title, String text, String id, long installdate) {
        /*
        a = name
        b = id
        c and d = category/text
        e = is active
        f = ? (seems to always be true)
        g = bought time

        so constructor for it should be

        name,category,id,category,time
         */

        return XposedHelpers.newInstance(smiley_class, title, text, id, text, installdate);

    }

}
