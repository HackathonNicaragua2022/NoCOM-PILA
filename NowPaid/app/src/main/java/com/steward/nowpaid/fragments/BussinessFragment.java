package com.steward.nowpaid.fragments;

import android.content.Intent;
import android.graphics.Color;
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

import com.steward.nowpaid.BussinessActivity;
import com.steward.nowpaid.Constants;
import com.steward.nowpaid.Session;
import com.steward.nowpaid.adapters.BussinessAdapter;
import com.steward.nowpaid.api.API;
import com.steward.nowpaid.model.BussinessItem;
import com.steward.nowpaid.NowPaidActivity;
import com.steward.nowpaid.R;

public class BussinessFragment extends Fragment
{
	BussinessAdapter bussiness;
	int pressed = 0;
	LinearLayout layCategories;

	@Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bussiness_fragment,container,false);
		layCategories = view.findViewById(R.id.layCategories);
		ListView lvBussiness = view.findViewById(R.id.lvBussiness);
		lvBussiness.setDividerHeight(0);
		bussiness = new BussinessAdapter(NowPaidActivity.instance);
		lvBussiness.setAdapter(bussiness);
		lvBussiness.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					API.instance.disposeEvent();
					Intent intn = new Intent(NowPaidActivity.instance, BussinessActivity.class);
					intn.putExtra("id",((BussinessItem)bussiness.getItem(p3)).id);
					startActivity(intn);
				}
			});
		press(0);
		return view;
    }

	private void press(int index) {
		pressed = index;
		layCategories.removeAllViews();
		for(int i = 0; i < Constants.categories.length; i++) {
			View sample = LayoutInflater.from(getContext()).inflate(R.layout.category_item,null);
			TextView tvName = sample.findViewById(R.id.tvCategoryName);
			tvName.setText(Constants.categories[i]);
			if(index == i) {
				sample.findViewById(R.id.layCatInd).setBackgroundColor(Color.GRAY);
				tvName.setTextColor(Color.WHITE);
			}
			final int j = i;
			sample.setOnClickListener((v) -> {
				press(j);
			});
			layCategories.addView(sample);
		}
		bussiness.clear();
		for(BussinessItem itm : NowPaidActivity.instance.bussns) {
			if((!Session.property || Session.property && !itm.user_id.contains(Session.userID)) && (index == 0 || itm.category == index)) {
				bussiness.add(itm);
			}
		}
		bussiness.notifyDataSetChanged();
	}
}
