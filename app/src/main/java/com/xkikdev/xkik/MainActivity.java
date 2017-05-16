package com.xkikdev.xkik;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.xkikdev.xkik.config_activities.ChatFragment;
import com.xkikdev.xkik.config_activities.LicensesFragment;
import com.xkikdev.xkik.config_activities.SmileyFragment;
import com.xkikdev.xkik.config_activities.TechnicalFragment;
import com.xkikdev.xkik.config_activities.VisualFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // main activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // toolbar
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.contentframe, new main_fragment()).commit();

        Settings.verifyStoragePermissions(this); // make sure we can access settings


        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Navbar item chosen
     * @param item Item chosen
     * @return Success yes/no
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentManager fm = getSupportFragmentManager();
        int id = item.getItemId();

        if (id == R.id.nav_xkikmain) {
            fm.beginTransaction().replace(R.id.contentframe, new main_fragment()).commit(); // main fragment
        } else if (id == R.id.nav_xkik_recpt) {
            fm.beginTransaction().replace(R.id.contentframe, new ChatFragment()).commit(); // receipt manager
        } else if (id == R.id.nav_xkik_visual) {
            fm.beginTransaction().replace(R.id.contentframe, new VisualFragment()).commit(); // visual manager
        } else if (id == R.id.nav_xkik_tech) {
            fm.beginTransaction().replace(R.id.contentframe, new TechnicalFragment()).commit(); // tech manager
        } else if (id == R.id.nav_xkik_smiley) {
            fm.beginTransaction().replace(R.id.contentframe, new SmileyFragment()).commit(); // smiley manager
        } else if (id == R.id.nav_xkik_license) {
            fm.beginTransaction().replace(R.id.contentframe, new LicensesFragment()).commit(); // licences / credits
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
