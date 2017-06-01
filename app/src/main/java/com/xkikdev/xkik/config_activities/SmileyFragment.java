package com.xkikdev.xkik.config_activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Settings;
import com.xkikdev.xkik.kikSmiley;
import com.xkikdev.xkik.kikUtil;

import java.io.IOException;


public class SmileyFragment extends Fragment {

    ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
    GridLayout gv;
    Button addb;
    Button impexp;
    Switch addTap;
    Settings settings;

    public SmileyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_smiley, container, false);
        gv = (GridLayout) v.findViewById(R.id.smileyGrid);
        addb = (Button) v.findViewById(R.id.addbyid);
        impexp = (Button) v.findViewById(R.id.importbutton);
        addTap = (Switch) v.findViewById(R.id.autosmiley);
        final Context c = this.getContext();

        try {
            settings = Settings.load(this.getActivity());
            addTap.setChecked(settings.getAutoSmiley());
        } catch (IOException e) {
            e.printStackTrace();
        }

        addTap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setAutoSmiley(isChecked,true);
            }
        });

        impexp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.contentframe,new SmileyImportFragment(),null)
                        .addToBackStack(null)
                        .commit();

            }
        });

        addb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(c)
                        .title("Enter Smiley ID")
                        .content("Please enter the smiley ID")
                        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                        .input("", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, final CharSequence input) {
                                // Do something
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        Looper.prepare();
                                        try {
                                            kikSmiley ks = kikUtil.smileyFromID(input.toString());
                                            if (ks != null) {
                                                settings.addSmiley(ks, true);
                                                renderSmiley(ks);
                                                Toast.makeText(c, "Added!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(c, "Error - Check internet connection or smiley ID", Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Toast.makeText(c, "Error - Check internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }.start();
                            }
                        }).show();
            }
        });

        for (kikSmiley s : settings.getSmileys()) {
            renderSmiley(s);

        }

        return v;
    }

    /**
     * Render a smiley in the grid
     *
     * @param s Smiley to render
     */
    private void renderSmiley(final kikSmiley s) {
        final Context c = this.getContext();
        final String sname = s.getTitle();
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageView iv = new ImageView(c);
                iv.setMinimumWidth(96);
                iv.setMinimumHeight(96);
                final ProgressBar loadingBar = new ProgressBar(c);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MaterialDialog.Builder(c)
                                .title("Delete Smiley")
                                .content("Are you sure you want to delete \"" + sname + "\"?")
                                .positiveText("Yes")
                                .negativeText("No")
                                .onAny(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        if (which == DialogAction.POSITIVE) {
                                            dialog.cancel();
                                            settings.deleteSmiley(s, true);
                                            gv.removeView(iv);
                                        }
                                    }
                                })
                                .build().show();
                    }
                });
                gv.addView(iv);
                gv.addView(loadingBar);
                imageLoader.displayImage("https://smiley-cdn.kik.com/smileys/" + s.getId() + "/96x96.png", iv, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        gv.removeView(loadingBar);
                        iv.setImageResource(R.drawable.ic_report_problem_black_24dp);

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        gv.removeView(loadingBar);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        gv.removeView(loadingBar);
                        iv.setImageResource(R.drawable.ic_report_problem_black_24dp);
                    }
                });
            }
        });

    }
}
