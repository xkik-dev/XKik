package com.xkikdev.xkik.config_activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Settings;

import java.io.IOException;

public class TechnicalFragment extends Fragment {

    public TechnicalFragment() {
        // Required empty public constructor
    }

    Settings settings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_technical, container, false);

        try {
            settings = Settings.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Switch dmode_switch = (Switch) v.findViewById(R.id.devmode_switch);
        dmode_switch.setChecked(settings.getDev());
        dmode_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    DialogInterface.OnClickListener dev_warning = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    settings.setDev(true);
                                    AlertDialog.Builder b = new AlertDialog.Builder(v.getContext());
                                    b.setTitle("Ok.").setMessage("Enabled. Dev tools are accessible via\nSettings>Your account>A/B Tests.\n\nThe smiley shop " +
                                            "will be non functional while dev tools are enabled.").show();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    dmode_switch.setChecked(false);
                                    settings.setDev(false);
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Are you sure?").setMessage("Kik developers have included tools " +
                            "within Kik that test various functions. These are only intended to be used by" +
                            " Kik developers when making Kik, but this setting enables said tools anyways. These can be very risky and may get you banned! Are you " +
                            "sure you want to enable Kik developer features?").setPositiveButton("Yes, I accept the risks", dev_warning)
                            .setNegativeButton("No!", dev_warning).show();
                } else {
                    settings.setDev(false);
                }

            }
        });


        // Inflate the layout for this fragment
        return v;
    }
}
