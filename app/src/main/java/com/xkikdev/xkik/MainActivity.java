package com.xkikdev.xkik;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.xkikdev.xkik.config_activities.ChatFragment;
import com.xkikdev.xkik.config_activities.LicensesFragment;
import com.xkikdev.xkik.config_activities.SmileyFragment;
import com.xkikdev.xkik.config_activities.TechnicalFragment;
import com.xkikdev.xkik.config_activities.VisualFragment;

import br.liveo.interfaces.OnItemClickListener;
import br.liveo.model.HelpLiveo;
import br.liveo.model.Navigation;
import br.liveo.navigationliveo.NavigationLiveo;

public class MainActivity extends NavigationLiveo implements OnItemClickListener {
    private HelpLiveo mHelpLiveo;

    @Override
    public void onInt(Bundle savedInstanceState) {
        this.userBackground.setImageResource(R.drawable.xkik_drawer);
        mHelpLiveo = new HelpLiveo();
        mHelpLiveo.add("Home", R.drawable.ic_home_white_24dp);
        mHelpLiveo.addSubHeader("Modifications");
        mHelpLiveo.add("Chat Tweaks", R.drawable.ic_chat_white_24dp);
        mHelpLiveo.add("Visual Tweaks", R.drawable.ic_remove_red_eye_white_24dp);
        mHelpLiveo.add("Technical Tweaks", R.drawable.ic_settings_white_24dp);
        mHelpLiveo.add("Emoticons Manager", R.drawable.ic_insert_emoticon_white_24dp);
        mHelpLiveo.add("Licenses", R.drawable.ic_description_white_24dp);
        with(this, Navigation.THEME_DARK)
                .addAllHelpItem(this.mHelpLiveo.getHelp())
                .startingPosition(0)
                .removeFooter()
                .backgroundList(R.color.xkik_material_grey)
                .colorItemDefault(R.color.nliveo_white)
                .selectorCheck(R.color.nliveo_black_light)
                .colorItemSelected(R.color.nliveo_red_colorPrimary)
                .colorLineSeparator(R.color.nliveo_transparent)
                .build();
        Settings.verifyStoragePermissions(this); // make sure we can access settings
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config);
    }
    @Override
    public void onItemClick(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new main_fragment();
                break;
            case 2:
                fragment = new ChatFragment();
                break;
            case 3:
                fragment = new VisualFragment();
                break;
            case 4:
                fragment = new TechnicalFragment();
                break;
            case 5:
                fragment = new SmileyFragment();
                break;
            case 6:
                fragment = new LicensesFragment();
                break;
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }
        setElevationToolBar(0.0f);
    }
}