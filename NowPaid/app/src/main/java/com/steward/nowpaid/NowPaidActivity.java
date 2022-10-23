package com.steward.nowpaid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NowPaidActivity extends AppCompatActivity {

    Fragment selected = null;
    public BottomNavigationView nav;
    public static NowPaidActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_paid);
        nav = findViewById(R.id.bottom_nav);


    }

    public void selectFragment(int index) {

    }
}