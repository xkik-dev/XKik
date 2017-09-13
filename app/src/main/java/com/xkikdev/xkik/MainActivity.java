package com.xkikdev.xkik;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
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
import com.xkikdev.xkik.chathead.chatheadReceiver;
import com.xkikdev.xkik.chathead.chatheadService;
import com.xkikdev.xkik.config_activities.ChatFragment;
import com.xkikdev.xkik.config_activities.LicensesFragment;
import com.xkikdev.xkik.config_activities.SmileyFragment;
import com.xkikdev.xkik.config_activities.TechnicalFragment;
import com.xkikdev.xkik.config_activities.VisualFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    chatheadService chs;
    chatheadReceiver chr = new chatheadReceiver();
    private boolean bound;
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            chatheadService.LocalBinder binder = (chatheadService.LocalBinder) service;
            chs = binder.getService();
            bound = true;
            chs.minimize();
            chatheadReceiver.setService(chs);

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            chatheadReceiver.setService(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, chatheadService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(chr, new IntentFilter("com.xkikdev.xkik.msgget"));

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
     *
     * @param item Item chosen
     * @return Success yes/no
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
