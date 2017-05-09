package com.xkikdev.xkik.config_activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Settings;
import com.xkikdev.xkik.kikSmiley;
import com.xkikdev.xkik.kikUtil;

import java.io.IOException;


public class SmileyFragment extends Fragment {

    ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance
    GridLayout gv;
    Button addb;
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
        final Context c = this.getContext();

        try {
            settings = Settings.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                                new Thread(){
                                    @Override
                                    public void run() {
                                        super.run();
                                        Looper.prepare();
                                        try {
                                            kikSmiley ks = kikUtil.smileyFromID(input.toString());
                                            if (ks!=null){
                                                settings.addSmiley(ks,true);
                                                renderSmiley(ks);
                                                Toast.makeText(c,"Added!",Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(c,"Error - Check internet connection or smiley ID",Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Toast.makeText(c,"Error - Check internet connection",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }.start();
                            }
                        }).show();
            }
        });

        for (kikSmiley s : settings.getSmileys()){
            renderSmiley(s);

        }

        return v;
    }

    private void renderSmiley(final kikSmiley s) {
        final Context c = this.getContext();
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView iv = new ImageView(c);
                gv.addView(iv);
                imageLoader.displayImage("https://smiley-cdn.kik.com/smileys/"+s.getId()+"/96x96.png",iv);
            }
        });

    }
}
