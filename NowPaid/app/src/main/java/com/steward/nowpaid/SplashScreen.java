package com.steward.nowpaid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.steward.nowpaid.api.API;
import com.steward.nowpaid.api.APIEvent;
import com.steward.nowpaid.api.OnTestSession;

import maes.tech.intentanim.*;

public class SplashScreen extends AppCompatActivity implements APIEvent {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_screen);
        API.init("http://192.168.43.104:3000");
        API.instance.reportEvents(this);
        Session.load(this);
        API.instance.testSession(new OnTestSession() {
            @Override
            public void pass() {
                API.instance.disposeEvent();
                runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        startActivity(new Intent(SplashScreen.this, NowPaidActivity.class));
                        finish();
                        CustomIntent.customType(SplashScreen.this,"fadein-to-fadeout");
                    }, 2000);
                });
            }

            @Override
            public void fail() {
                API.instance.disposeEvent();
                runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        startActivity(new Intent(SplashScreen.this, LoginActivity.class));
                        finish();
                        CustomIntent.customType(SplashScreen.this,"fadein-to-fadeout");
                    }, 2000);
                });
            }
        });

    }

    @Override
    public void timeout() {
        runOnUiThread(() -> {
            final AlertDialog dialog = new AlertDialog.Builder(SplashScreen.this).create();
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
