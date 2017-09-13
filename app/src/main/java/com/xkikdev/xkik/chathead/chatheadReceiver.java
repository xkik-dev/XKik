package com.xkikdev.xkik.chathead;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.xkikdev.xkik.datatype_parsers.msgText;


public class chatheadReceiver extends BroadcastReceiver {

    static chatheadService chs;

    public static void setService(chatheadService ch) {
        chs = ch;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        msgText mt = new Gson().fromJson(intent.getStringExtra("msg"), msgText.class);
        if (chs != null) {
            chs.handleRecvMsg(mt);
        }
    }
}
