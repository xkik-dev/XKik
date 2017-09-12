package com.xkikdev.xkik;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class main_fragment extends Fragment {
    TextView statustxt;

    public main_fragment() {
        // Required empty public constructor
        super();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_fragment, container, false);// Inflate the layout for this fragment


        statustxt= (TextView) v.findViewById(R.id.statusText);
        new Thread(){
            @Override
            public void run() {
                final String s = Util.urlToString("https://raw.githubusercontent.com/xkik-dev/xkik-status/master/status.txt");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (s==null){
                            statustxt.setText("Could not fetch status.");
                        }
                        statustxt.setText(s);
                    }
                });
            }
        }.start();
        return v;
    }

}
