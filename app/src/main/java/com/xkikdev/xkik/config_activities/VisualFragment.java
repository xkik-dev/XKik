package com.xkikdev.xkik.config_activities;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.xkikdev.xkik.ColorSetting;
import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Settings;
import com.xkikdev.xkik.StringSetting;
import com.pavelsikun.vintagechroma.ChromaDialog;
import com.pavelsikun.vintagechroma.OnColorSelectedListener;
import com.pavelsikun.vintagechroma.colormode.ColorMode;

import java.io.IOException;


public class VisualFragment extends Fragment {

    Settings settings;
    Switch accdate;
    ColorSetting[] colorSettings = new ColorSetting[]{
            /*new ColorSetting("Main Background", new String[]{"white"}, "#ffffffff"),
            new ColorSetting("Chat Background", new String[]{"chat_background_color","chat_info_background"},"#ffeeeeee"),*/
            new ColorSetting("Primary Text","gray_6","#ff373a4b"),
            new ColorSetting("Secondary Text","gray_5","#ff7a7d8e"),
            new ColorSetting("Tertiary Text","gray_4","#ffa9adc1"),
            new ColorSetting("App Bar Background","gray_1","#fffafafa")

    };

    StringSetting[] stringSettings = new StringSetting[]{
            new StringSetting("Type Message Text","activity_new_message_hint","Type a message..."),
            new StringSetting("Typing message","is_typing_","is typing...")
    };

    public VisualFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_visual, container, false);// Inflate the layout for this fragment
        try {
            settings = Settings.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ViewGroup parent = (ViewGroup) view.findViewById(R.id.color_tl);
        for (ColorSetting c : colorSettings) {
            parent.addView(genColorTweak(inflater, c.label, c.id, c.defval));
        }

        ViewGroup string_tl = (ViewGroup) view.findViewById(R.id.string_tl);
        for (StringSetting c : stringSettings) {
            string_tl.addView(genStringTweak(inflater, c.label, c.id, c.defval));
        }

        accdate=(Switch) view.findViewById(R.id.accdate_switch);
        accdate.setChecked(settings.getDateFormat()==1);
        accdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    settings.setDateFormat(1); // exact
                }else{
                    settings.setDateFormat(0); // no change
                }
            }
        });

        return view;
    }

    int getColor(String colorcode, int def) {
        int col;
        if (settings.getColors().containsKey(colorcode)) {
            col = settings.getColors().get(colorcode);
        } else {
            col = def;
        }
        return col;
    }

    String getString(String id,String def){
        if (settings.getStrings().containsKey(id)){
            return settings.getStrings().get(id);
        }else{
            return def;
        }
    }

    View genStringTweak(LayoutInflater inflater,String label, final String val_id, final String orig){
        View v = inflater.inflate(R.layout.string_change_tweak, null, false);
        Button b = (Button) v.findViewById(R.id.set_button);
        final EditText txt = (EditText) v.findViewById(R.id.string_et);
        txt.setHint(orig);
        txt.setText(getString(val_id,orig));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setString(val_id,txt.getText().toString());
            }
        });
        b.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                txt.setText(orig);
                settings.resetString(val_id);
                return true;
            }
        });

        return v;
    }

    View genColorTweak(LayoutInflater inflater, String label, final String colorcode[], final int default_color) {
        View v = inflater.inflate(R.layout.color_change_tweak, null, false);
        Button b = (Button) v.findViewById(R.id.sbar_button);
        final ImageView iv = (ImageView) v.findViewById(R.id.sbar_color_iv);
        b.setText(label);
        iv.setBackgroundColor(getColor(colorcode[0], default_color));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChromaDialog.Builder()
                        .initialColor(getColor(colorcode[0], default_color))
                        .colorMode(ColorMode.RGB)
                        .onColorSelected(new OnColorSelectedListener() {

                            @Override
                            public void onColorSelected(@ColorInt int color) {
                                iv.setBackgroundColor(color);
                                for (String c : colorcode){
                                    settings.setColor(c, color);
                                }

                            }
                        })
                        .create()
                        .show(getFragmentManager(), "ChromaDialog");
            }
        });
        b.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                iv.setBackgroundColor(default_color);
                for (String c : colorcode){
                    settings.resetColor(c);
                }
                return true;
            }
        });

        return v;
    }

}
