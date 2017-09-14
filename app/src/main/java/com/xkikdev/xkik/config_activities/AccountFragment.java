package com.xkikdev.xkik.config_activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Settings;

import java.io.IOException;

public class AccountFragment extends Fragment {

    Settings settings;

    public AccountFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);// Inflate the layout for this fragment
        try {
            settings = Settings.load(this.getActivity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return v;
    }

}
