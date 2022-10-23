package com.steward.nowpaid;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.steward.nowpaid.databinding.SplashScreenBinding;

import maes.tech.intentanim.CustomIntent;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashScreen.this, LoginActivity.class));
            finish();
            CustomIntent.customType(SplashScreen.this,"fadein-to-fadeout");
        }, 2000);
    }
}