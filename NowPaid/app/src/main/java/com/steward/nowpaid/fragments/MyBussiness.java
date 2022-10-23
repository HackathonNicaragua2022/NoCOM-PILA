package com.steward.nowpaid.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.steward.nowpaid.BussinessActivity;
import com.steward.nowpaid.Constants;
import com.steward.nowpaid.EditBussinessActivity;
import com.steward.nowpaid.NowPaidActivity;
import com.steward.nowpaid.R;
import com.steward.nowpaid.Session;
import com.steward.nowpaid.adapters.BussinessAdapter;
import com.steward.nowpaid.api.API;
import com.steward.nowpaid.model.BussinessItem;

public class MyBussiness extends Fragment
{
    BussinessAdapter bussiness;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_bussiness_fragment,container,false);
        ListView lvBussiness = view.findViewById(R.id.lvMyBussiness);
        lvBussiness.setDividerHeight(0);
        bussiness = new BussinessAdapter(NowPaidActivity.instance);
        lvBussiness.setAdapter(bussiness);
        lvBussiness.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
                API.instance.disposeEvent();
                Intent intn = new Intent(NowPaidActivity.instance, EditBussinessActivity.class);
                intn.putExtra("id", ((BussinessItem)bussiness.getItem(p3)).id);
                intn.putExtra("publish",false);
                NowPaidActivity.instance.editResult.launch(intn);
            }
        });
        bussiness.clear();
        for(BussinessItem itm : NowPaidActivity.instance.bussns) {
            if(itm.user_id.contains(Session.userID) ) {
                bussiness.add(itm);
            }
        }
        bussiness.notifyDataSetChanged();
        ((FloatingActionButton)view.findViewById(R.id.btnAddBuss)).setOnClickListener((v) -> {
            API.instance.disposeEvent();
            Intent intn = new Intent(NowPaidActivity.instance, EditBussinessActivity.class);
            intn.putExtra("id", "");
            intn.putExtra("publish",true);
            NowPaidActivity.instance.editResult.launch(intn);
        });
        return view;
    }
}
