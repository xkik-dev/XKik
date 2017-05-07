package com.xkikdev.xkik.config_activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Settings;

import java.io.IOException;

public class ChatFragment extends Fragment {

    Switch readRecpt;
    Switch typingRecpt;
    Switch fakeCam;
    Switch lurkDetector;
    Settings settings;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        // Inflate the layout for this fragment

        try {
            settings = Settings.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        readRecpt = (Switch) rootView.findViewById(R.id.read_recpt_switch);
        typingRecpt = (Switch) rootView.findViewById(R.id.typing_recpt_switch);
        fakeCam = (Switch) rootView.findViewById(R.id.fake_cam_switch);
        lurkDetector = (Switch) rootView.findViewById(R.id.lurk_detector);
        readRecpt.setChecked(settings.isNoReadreceipt());
        typingRecpt.setChecked(settings.isNoTyping());
        fakeCam.setChecked(settings.getFakeCam());
        lurkDetector.setChecked(settings.isWhosLurking());
        readRecpt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setNoReadreceipt(isChecked);
            }
        });

        typingRecpt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setNoTyping(isChecked);
            }
        });

        fakeCam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setFakeCam(isChecked);
            }
        });

        lurkDetector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setWhosLurking(isChecked);
            }
        });

        return rootView;
    }

}
