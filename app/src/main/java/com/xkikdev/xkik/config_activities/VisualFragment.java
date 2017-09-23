package com.xkikdev.xkik.config_activities;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.pavelsikun.vintagechroma.ChromaDialog;
import com.pavelsikun.vintagechroma.OnColorSelectedListener;
import com.pavelsikun.vintagechroma.colormode.ColorMode;
import com.xkikdev.xkik.ColorSetting;
import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Settings;
import com.xkikdev.xkik.StringSetting;

import java.io.File;
import java.io.IOException;
import java.util.List;

import easyfilepickerdialog.kingfisher.com.library.model.DialogConfig;
import easyfilepickerdialog.kingfisher.com.library.model.SupportFile;
import easyfilepickerdialog.kingfisher.com.library.view.FilePickerDialogFragment;


public class VisualFragment extends Fragment {

    Settings settings;
    Switch accdate;
    Switch darkbg;
    Button setBackground;

    ColorSetting[] colorSettings = new ColorSetting[]{
            /*new ColorSetting("Main Background", new String[]{"white"}, "#ffffffff"),
            new ColorSetting("Chat Background", new String[]{"chat_background_color","chat_info_background"},"#ffeeeeee"),*/
            new ColorSetting("Primary Text", "gray_6", "#ff373a4b"),
            new ColorSetting("Secondary Text", "gray_5", "#ff7a7d8e"),
            new ColorSetting("Tertiary Text", "gray_4", "#ffa9adc1"),
            new ColorSetting("App Bar Background", "gray_1", "#fffafafa"),
            new ColorSetting("White", "white", "#ffeeeeee"),
    };

    StringSetting[] stringSettings = new StringSetting[]{
            new StringSetting("Type Message Text", "activity_new_message_hint", "Type a message..."),
            new StringSetting("Typing message", "is_typing_", "is typing...")
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
            settings = Settings.load(this.getActivity());
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


        setBackground = (Button) view.findViewById(R.id.background_picture);
        setImagePicker();
        accdate = (Switch) view.findViewById(R.id.accdate_switch);
        darkbg = (Switch) view.findViewById(R.id.darkbg_switch);
        accdate.setChecked(settings.getDateFormat() == 1);
        darkbg.setChecked(settings.getDarkBg());
        accdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings.setDateFormat(1, true); // exact
                } else {
                    settings.setDateFormat(0, true); // no change
                }
            }
        });
        darkbg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setDarkBg(isChecked);
            }
        });

        return view;
    }

    /**
     * When clicked on the image picker, creates a dialog that will let the user choose images for the background.
     */
    private void setImagePicker()
    {
        setBackground.setText("Background Image/s");
        setBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                DialogConfig dialogConfig = new DialogConfig.Builder()
                        .supportFiles(new SupportFile(".jpg", android.R.drawable.ic_menu_gallery),
                                new SupportFile(".jpeg", android.R.drawable.ic_menu_gallery),
                                new SupportFile(".png", android.R.drawable.ic_menu_gallery),
                                new SupportFile(".bmp", android.R.drawable.ic_menu_gallery))
                        .enableMultipleSelect(true)
                        .build();
                new FilePickerDialogFragment.Builder()
                        .configs(dialogConfig)
                        .onFilesSelected(new FilePickerDialogFragment.OnFilesSelectedListener() {
                            @Override
                            public void onFileSelected(final List<File> list) {
                                if(settings != null) {
                                    settings.setFileList(list, false);
                                }
                            }
                        })
                        .build()
                        .show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), null);
            }
        });
    }

    /**
     * Get a configured color
     *
     * @param colorcode Color ID/Code
     * @param def       Default
     * @return The color, default if not set
     */
    int getColor(String colorcode, int def) {
        int col;
        if (settings.getColors().containsKey(colorcode)) {
            col = settings.getColors().get(colorcode);
        } else {
            col = def;
        }
        return col;
    }

    /**
     * Get a configured string
     *
     * @param id  String ID
     * @param def Default
     * @return The string, default if not set
     */
    String getString(String id, String def) {
        if (settings.getStrings().containsKey(id)) {
            return settings.getStrings().get(id);
        } else {
            return def;
        }
    }

    View genStringTweak(LayoutInflater inflater, String label, final String val_id, final String orig) {
        View v = inflater.inflate(R.layout.string_change_tweak, null, false);
        Button b = (Button) v.findViewById(R.id.set_button);
        final EditText txt = (EditText) v.findViewById(R.id.string_et);
        txt.setHint(orig);
        txt.setText(getString(val_id, orig));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setString(val_id, txt.getText().toString(), true);
            }
        });
        b.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                txt.setText(orig);
                settings.resetString(val_id, true);
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
                                for (String c : colorcode) {
                                    settings.setColor(c, color, true);
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
                for (String c : colorcode) {
                    settings.resetColor(c, true);
                }
                return true;
            }
        });

        return v;
    }

}
