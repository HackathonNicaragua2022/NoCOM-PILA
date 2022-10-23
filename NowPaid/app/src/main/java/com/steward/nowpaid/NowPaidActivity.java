package com.steward.nowpaid;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.steward.nowpaid.api.API;
import com.steward.nowpaid.api.APIEvent;
import com.steward.nowpaid.api.OnArrayResponse;
import com.steward.nowpaid.api.OnObjectResponse;
import com.steward.nowpaid.fragments.AccountFragment;
import com.steward.nowpaid.fragments.BussinessFragment;
import com.steward.nowpaid.fragments.MyBussiness;
import com.steward.nowpaid.model.BussinessItem;
import com.steward.nowpaid.model.ProductItem;
import com.steward.nowpaid.model.RatingItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class NowPaidActivity extends AppCompatActivity implements APIEvent {
    Fragment selected = null;
    public ArrayList<BussinessItem> bussns; // admin
    public BottomNavigationView nav;
    public static NowPaidActivity instance;

    public ActivityResultLauncher<Intent> editResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == 130) {
                    API.instance.getBussinessList(new OnArrayResponse() {
                        @Override
                        public void successfully(JSONArray array) {
                            try {
                                bussns.clear();
                                for(int i = 0; i < array.length(); i++){
                                    JSONObject itm = array.getJSONObject(i);
                                    BussinessItem item = new BussinessItem();
                                    item.id = itm.getString("id");
                                    item.name = itm.getString("name");
                                    item.user_id = itm.getString("user");
                                    item.icon = itm.getString("icon");
                                    item.visited = itm.getInt("visited");
                                    item.category = itm.getInt("category");
                                    item.rating = (float)itm.getDouble("rating");
                                    bussns.add(item);
                                }
                                selectFragment(1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nowpaid_activity);
        instance = this;
        bussns = new ArrayList<>();
        if(API.instance == null){return;}
        API.instance.reportEvents(this);
        API.instance.getBussinessList(new OnArrayResponse() {
            @Override
            public void successfully(JSONArray array) {
                try {
                    bussns.clear();
                    for(int i = 0; i < array.length(); i++){
                        JSONObject itm = array.getJSONObject(i);
                        BussinessItem item = new BussinessItem();
                        item.id = itm.getString("id");
                        item.name = itm.getString("name");
                        item.user_id = itm.getString("user");
                        item.icon = itm.getString("icon");
                        item.visited = itm.getInt("visited");
                        item.category = itm.getInt("category");
                        item.rating = (float)itm.getDouble("rating");
                        bussns.add(item);
                    }
                    loadUserInfo();
                } catch (JSONException e) {
                    e.printStackTrace();
                    API.log(e.toString());
                }
            }
        });
        nav = findViewById(R.id.bottom_nav);
        nav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    selectFragment(0);
                    break;
                case R.id.mybuss:
                    selectFragment(1);
                    break;
                case R.id.account:
                    selectFragment(2);
                    break;
            }
            return true;
        });
        nav.getMenu().getItem(1).setVisible(Session.property);
    }

    public void loadUserInfo() {
        API.instance.getUserInfo((res) -> {
            try {
                Session.userID = res.getString("_id");
                Session.name = res.getString("name");
                Session.location = res.getInt("location");
                Session.image = res.getString("image");
                Session.phone = res.getString("phone");
                selectFragment(0);
            } catch (JSONException e) {
                e.printStackTrace();
                API.log(e.toString());
            }
        });
    }

    public void selectFragment(int index) {
        switch (index) {
            case 0:
                selected = new BussinessFragment();
                break;
            case 1:
                selected = new MyBussiness();
                break;
            case 2:
                if(selected instanceof AccountFragment) return;
                selected = new AccountFragment();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selected).commit();
    }

    public void timeout() {
        runOnUiThread(() -> {
            final AlertDialog dialog = new AlertDialog.Builder(NowPaidActivity.this).create();
            dialog.setTitle("Error al conectarse");
            dialog.setMessage("Por favor revise su conexion a internet e intente de nuevo");
            dialog.setCancelable(false);
            dialog.setButton(AlertDialog.BUTTON_POSITIVE,"Aceptar",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.show();
        });
    }
    @Override
    public void unautorized() {

    }
}
