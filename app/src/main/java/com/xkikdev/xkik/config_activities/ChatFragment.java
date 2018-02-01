package com.xkikdev.xkik.config_activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Settings;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.IOException;

public class ChatFragment extends Fragment {

    Switch readRecpt;
    Switch typingRecpt;
    Switch fakeCam;
    Switch lurkDetector;
    Switch lurkToast;
    Switch longCam;
    Switch disableFwd;
    Switch disableSave;
    Switch disableFilter;
    Switch autoloop;
    Switch automute;
    Switch autoplay;
    Switch bypassSave;
    ExpandableLayout lurkEL;
    Settings settings;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        // Inflate the layout for this fragment

        try {
            settings = Settings.load(this.getActivity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        readRecpt = rootView.findViewById(R.id.read_recpt_switch);
        typingRecpt = rootView.findViewById(R.id.typing_recpt_switch);
        fakeCam = rootView.findViewById(R.id.fake_cam_switch);
        lurkDetector = rootView.findViewById(R.id.lurk_detector);
        lurkToast = rootView.findViewById(R.id.lurk_toast);
        longCam = rootView.findViewById(R.id.long_cam);
        disableFwd = rootView.findViewById(R.id.disable_fwd);
        disableSave = rootView.findViewById(R.id.disable_save);
        disableFilter = rootView.findViewById(R.id.unfilter_gif);
        autoloop = rootView.findViewById(R.id.auto_loop_video);
        automute = rootView.findViewById(R.id.auto_mute_video);
        autoplay = rootView.findViewById(R.id.auto_play_video);
        lurkEL = rootView.findViewById(R.id.toastEL);
        bypassSave = rootView.findViewById(R.id.bypass_save);
        readRecpt.setChecked(settings.getNoReadreceipt());
        typingRecpt.setChecked(settings.getNoTyping());
        fakeCam.setChecked(settings.getFakeCam());
        longCam.setChecked(settings.getLongCam());

        disableFwd.setChecked(settings.getDisableFwd());
        disableFwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setDisableFwd(isChecked);
            }
        });
        bypassSave.setChecked(settings.getBypassSave());
        bypassSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setBypassSave(isChecked);
            }
        });
        autoloop.setChecked(settings.getAutoLoop());
        autoloop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setAutoLoop(isChecked);
            }
        });
        automute.setChecked(settings.getAutoMute());
        automute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setAutoMute(isChecked);
            }
        });
        autoplay.setChecked(settings.getAutoPlay());
        autoplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setAutoplay(isChecked);
            }
        });
        disableSave.setChecked(settings.getDisableSave());
        disableSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setDisableSave(isChecked);
            }
        });

        disableFilter.setChecked(settings.getUnfilterGIFs());
        disableFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setUnfilterGIFs(isChecked);
            }
        });


        readRecpt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setNoReadreceipt(isChecked, true);
            }
        });



        lurkToast.setChecked(settings.getLurkingToast());
        lurkToast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setLurkingToast(isChecked);
            }
        });

        typingRecpt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setNoTyping(isChecked, true);
            }
        });

        fakeCam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setFakeCam(isChecked, true);
            }
        });

        lurkDetector.setChecked(settings.getWhosLurking());
        lurkEL.setExpanded(settings.getWhosLurking());
        lurkDetector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setWhosLurking(isChecked, true);
                if (isChecked){
                    lurkEL.expand();
                }else{
                    lurkEL.collapse();
                }
            }
        });

        longCam.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setLongCam(isChecked);
            }
        });

        return rootView;
    }

}
