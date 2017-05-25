package com.xkikdev.xkik;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.robv.android.xposed.XposedHelpers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Utilities for Kik itself, i.e generating kik objects
 */

public class kikUtil {

    private static OkHttpClient client = new OkHttpClient();

    static Object gen_smiley(Class smiley_class, kikSmiley smiley) {
        return gen_smiley(smiley_class, smiley.title, smiley.text, smiley.id, smiley.idate);
    }

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
    static Object gen_smiley(Class smiley_class, String title, String text, String id, long installdate) {
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

    public static kikSmiley smileyFromID(final String id) throws IOException {
        Request request = new Request.Builder()
                .url("https://sticker-service.appspot.com/v2/smiley/" + id)
                .build();

        Response response = client.newCall(request).execute();
        String resp = response.body().string();
        if (resp.equals("not found")) {
            return null;
        }
        try {
            return getSmileyFromJSON(id, new JSONObject(resp));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    private static kikSmiley getSmileyFromJSON(String id, JSONObject response) throws JSONException {
        kikSmiley smiley;
        String type = response.getString("type");
        String name = response.getString("name");
        smiley = new kikSmiley(name, type, id, System.currentTimeMillis());
        return smiley;
    }

}
