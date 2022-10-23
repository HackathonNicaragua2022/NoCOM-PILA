package com.steward.nowpaid.fragments;

import androidx.fragment.app.Fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.steward.nowpaid.Constants;
import com.steward.nowpaid.NowPaidActivity;
import com.steward.nowpaid.R;
import com.steward.nowpaid.Session;
import com.steward.nowpaid.api.API;
import com.steward.nowpaid.api.OnObjectResponse;

import org.json.JSONObject;

public class AccountFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment,container,false);
        TextView tvName = (TextView) view.findViewById(R.id.tvUserName);
        TextView tvUserInfo = (TextView) view.findViewById(R.id.tvUserInfo);
        tvName.setText(Session.name);
        tvUserInfo.setText("Localidad: " + Constants.locations[Session.location] + "\nNumero de Telefono: " + Session.phone);
        ((Button)view.findViewById(R.id.btnLogOut)).setOnClickListener((v)-> {
            API.instance.logout(new OnObjectResponse() {
                @Override
                public void successfully(JSONObject object) {
                    restartApp();
                }
            });
        });
        return view;
    }

    private void restartApp() {
        PackageManager pmgr = NowPaidActivity.instance.getPackageManager();
        Intent intent = pmgr.getLaunchIntentForPackage(NowPaidActivity.instance.getPackageName());
        ComponentName cn = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(cn);
        startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }
}
