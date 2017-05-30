package com.xkikdev.xkik.config_activities;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Settings;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import easyfilepickerdialog.kingfisher.com.library.model.DialogConfig;
import easyfilepickerdialog.kingfisher.com.library.model.SupportFile;
import easyfilepickerdialog.kingfisher.com.library.view.FilePickerDialogFragment;

public class SmileyImportFragment extends Fragment {
    Settings settings;
    ActionProcessButton imptxt;
    ActionProcessButton impdb;
    ActionProcessButton exptxt;


    public SmileyImportFragment() {
        // Required empty public constructor
        try {
            settings = Settings.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buttonError(ActionProcessButton b, Context c, String error) {
        b.setProgress(-1);
        setAllButtons(true);
        Toast.makeText(c, error, Toast.LENGTH_SHORT);
    }

    private void setAllButtons(boolean to) {
        imptxt.setEnabled(to);
        impdb.setEnabled(to);
        exptxt.setEnabled(to);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_smiley_import, container, false);

        imptxt = (ActionProcessButton) v.findViewById(R.id.imptxt);
        impdb = (ActionProcessButton) v.findViewById(R.id.impdb);
        exptxt = (ActionProcessButton) v.findViewById(R.id.exptxt);
        imptxt.setMode(ActionProcessButton.Mode.ENDLESS);
        impdb.setMode(ActionProcessButton.Mode.ENDLESS);
        exptxt.setMode(ActionProcessButton.Mode.ENDLESS);

        imptxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                DialogConfig dialogConfig = new DialogConfig.Builder()
                        .supportFiles(new SupportFile(".txt", 0), new SupportFile(".xsmileydb", R.drawable.ic_insert_emoticon_black_24dp))
                        .build();
                new FilePickerDialogFragment.Builder()
                        .configs(dialogConfig)
                        .onFilesSelected(new FilePickerDialogFragment.OnFilesSelectedListener() {
                            @Override
                            public void onFileSelected(List<File> list) {
                                File f = list.get(0);
                                try {
                                    String data = FileUtils.readFileToString(f, "UTF-8");
                                    imptxt.setProgress(1);
                                    if (settings == null) {
                                        buttonError(imptxt, v.getContext(), "Failed to load settings");
                                        return;
                                    }
                                    if (data.startsWith("xsmileys:")) { // simple check
                                        String dta = data.substring(9);
                                        String[] ids = dta.split(",");
                                        imptxt.setMode(ActionProcessButton.Mode.PROGRESS);
                                        setAllButtons(false);
                                        for (int i = 0; i < ids.length; i++) {
                                            Float pct = (i / (ids.length * 1.0F)) * 100;
                                            imptxt.setProgress((int) Math.floor(pct));
                                        }
                                        imptxt.setProgress(100);
                                        setAllButtons(true);
                                    } else {
                                        imptxt.setProgress(-1);
                                        buttonError(imptxt, v.getContext(), "Invalid File");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    buttonError(imptxt, v.getContext(), "Error reading file");
                                }
                            }
                        })
                        .build()
                        .show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), null);
            }
        });


        return v;
    }

}
