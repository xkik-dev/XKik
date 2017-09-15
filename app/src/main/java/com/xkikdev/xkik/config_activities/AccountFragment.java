package com.xkikdev.xkik.config_activities;


import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.xkikdev.xkik.KikAccount;
import com.xkikdev.xkik.R;
import com.xkikdev.xkik.Settings;
import com.xkikdev.xkik.Util;

import java.io.IOException;
import java.util.regex.Pattern;

public class AccountFragment extends Fragment {

    Settings settings;
    ViewGroup accts;
    String activefolder;
    Pattern nopunct = Pattern.compile("[^A-Za-z0-9]");

    public AccountFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);// Inflate the layout for this fragment

        Button addAcct = (Button) v.findViewById(R.id.addAcct);

        accts = (ViewGroup) v.findViewById(R.id.acctTbl);

        try {
            settings = Settings.load(this.getActivity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        addAcct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getContext())
                        .title("Warning!")
                        .content("This feature is IN BETA!! It might cause issues with Kik or your device. ")
                        .neutralText("Ok")
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new MaterialDialog.Builder(getContext())
                                        .title("New Account")
                                        .content("Enter a name for your new account profile")
                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                        .input("A-Z 0-9", "", new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                if (nopunct.matcher(input).find() || input.toString().equalsIgnoreCase("main") || input.toString().isEmpty()) {
                                                    new MaterialDialog.Builder(getContext())
                                                            .title("Error!")
                                                            .content("That name is not valid.")
                                                            .neutralText("Ok")
                                                            .show();
                                                } else {

                                                    if (settings.getAcct(input.toString()) != null) {
                                                        new MaterialDialog.Builder(getContext())
                                                                .title("Error!")
                                                                .content("That name already exists.")
                                                                .neutralText("Ok")
                                                                .show();
                                                    } else {
                                                        settings.addExtraAcct(input.toString());
                                                        refreshAccts();
                                                    }


                                                }
                                            }
                                        }).show();
                            }
                        })
                        .show();
            }
        });

        activefolder = Environment.getDataDirectory() + "/data/kik.android";
        refreshAccts(inflater);

        return v;
    }

    public void refreshAccts() {
        refreshAccts(LayoutInflater.from(getContext()));
    }

    public void refreshAccts(LayoutInflater inflater) {
        accts.removeAllViews();
        accts.addView(genMAcctTweak(inflater, !settings.getMainacctenabled()));
        for (KikAccount ka : settings.getExtraAccts()) {
            accts.addView(genAcctTweak(inflater, ka.getName(), !ka.isActive()));
        }
    }

    View genAcctTweak(LayoutInflater inflater, final String label, boolean enabled) {
        View v = inflater.inflate(R.layout.account_tweak, null, false);
        Button b = (Button) v.findViewById(R.id.acct_button);
        b.setText(label);
        b.setEnabled(enabled);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    switchAccounts(getActiveAcct(), label);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        b.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new MaterialDialog.Builder(getContext())
                        .title("Delete?")
                        .content("Are you sure you want to delete '" + label + "'?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which.equals(DialogAction.POSITIVE)) { // yes, delete
                                    try {
                                        Util.killKik(getActivity());
                                        settings.removeExtraAcct(label);
                                        settings.save(false);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    String foldername = Environment.getDataDirectory() + "/data/kik.android~" + label + "/";
                                    Util.sudo("rm -rf " + foldername);
                                    refreshAccts();
                                } else {
                                    dialog.cancel();
                                }
                            }
                        }).show();
                return true;
            }
        });

        return v;
    }

    View genMAcctTweak(LayoutInflater inflater, boolean enabled) {
        View v = inflater.inflate(R.layout.account_tweak, null, false);
        Button b = (Button) v.findViewById(R.id.acct_button);
        b.setText("MAIN ACCOUNT");
        b.setEnabled(enabled);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    switchAccounts(getActiveAcct(), "MAIN");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return v;
    }

    String getActiveAcct() {
        if (settings.getMainacctenabled()) {
            return "MAIN";
        }
        for (KikAccount ka : settings.getExtraAccts()) {
            if (ka.isActive()) {
                return ka.getName();
            }
        }
        return null;
    }

    void deactivateAcct(String key) {
        String foldername = Environment.getDataDirectory() + "/data/kik.android~" + key + "/";
        Util.sudo("mv " + activefolder + " " + foldername);
    }

    void activateAcct(String key) {
        String foldername = Environment.getDataDirectory() + "/data/kik.android~" + key + "/";
        Util.sudo("mkdir " + foldername,
                "mv " + foldername + " " + activefolder);
        if (!key.equals("MAIN")) {
            Util.sudo("chmod 757 " + activefolder); // since I cannot get main folder owner, this is a temporary fix
        }

    }

    /**
     * Switches two accounts
     *
     * @param acctA Account to remove
     * @param acctB Account to replace with
     */
    void switchAccounts(String acctA, String acctB) throws IOException {
        Util.killKik(getActivity());
        if (acctA.equals("MAIN")) {
            settings.setMainacctenabled(false);
        } else if (acctB.equals("MAIN")) {
            settings.setMainacctenabled(true);
        }

        if (!acctA.equals("MAIN")) {
            settings.getAcct(acctA).setActive(false);
        }
        if (!acctB.equals("MAIN")) {
            settings.getAcct(acctB).setActive(true);
        }

        settings.save(false);
        deactivateAcct(acctA);
        activateAcct(acctB);
        refreshAccts();
    }


}
