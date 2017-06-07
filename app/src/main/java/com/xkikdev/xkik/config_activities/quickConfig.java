package com.xkikdev.xkik.config_activities;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.xkikdev.xkik.R;
import com.xkikdev.xkik.xkik_xposed;

/**
 * Class for managing quick toggleable settings
 */
public class quickConfig extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(xkik_xposed.resources.getLayout(R.layout.quicksettings), container);
        getDialog().setTitle("Quick Settings");

        Switch fakeCam = (Switch) v.findViewById(R.id.fcam_Switch);
        Switch disableRead = (Switch) v.findViewById(R.id.dis_read_recpt);
        Switch disableType = (Switch) v.findViewById(R.id.dis_typing_recpt);
        Switch whoLurk = (Switch) v.findViewById(R.id.whos_reading);
        Button donebutton = (Button) v.findViewById(R.id.donebutton);

        fakeCam.setChecked(xkik_xposed.settings.getFakeCam());
        disableRead.setChecked(xkik_xposed.settings.getNoReadreceipt());
        disableType.setChecked(xkik_xposed.settings.getNoTyping());
        whoLurk.setChecked(xkik_xposed.settings.getWhosLurking());

        fakeCam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                xkik_xposed.settings.setFakeCam(isChecked, false);
            }
        });

        disableRead.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                xkik_xposed.settings.setNoReadreceipt(isChecked, false);
            }
        });

        disableType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                xkik_xposed.settings.setNoTyping(isChecked, false);
            }
        });

        whoLurk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                xkik_xposed.settings.setWhosLurking(isChecked, false);
            }
        });

        donebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });

        return v;
    }
}
