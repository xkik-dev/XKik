package com.xkikdev.xkik;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xkikdev.xkik.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class main_fragment extends Fragment {


    public main_fragment() {
        // Required empty public constructor
        super();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_fragment, container, false);
    }

}
