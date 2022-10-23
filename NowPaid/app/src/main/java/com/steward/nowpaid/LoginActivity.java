package com.steward.nowpaid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText etEmail,etPassword;
    TextInputLayout layToken, layPassword;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        layToken = findViewById(R.id.layEmail);
        layPassword = findViewById(R.id.layPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPassword.setOnFocusChangeListener((view,b) -> {
            etPassword.setError(null);
        });
        etEmail.setOnFocusChangeListener((view,b) -> {
            etPassword.setError(null);
        });
        ((Button)findViewById(R.id.btnLogin)).setOnClickListener((view)->{
            layToken.setError(null);
            layPassword.setError(null);
            if(etEmail.getText().toString().length() == 0) {
                layToken.setError("Correo invalido");
                return;
            }
            if(etPassword.getText().toString().length() == 0) {
                layPassword.setError("Contraseña invalida");
                return;
            }
            login();
        });
        ((Button)findViewById(R.id.btnSignIn)).setOnClickListener((view) -> {
            startActivity(new Intent(LoginActivity.this, SignInActivity.class));
            finish();
        });
        ((TextView)findViewById(R.id.tvForget)).setOnClickListener((view) -> {
            final AlertDialog dialog = new AlertDialog.Builder(LoginActivity.this).create();
            dialog.setTitle("Contacte a un administrador");
            dialog.setMessage("Solicite a un administrador el reestablecimiento de su contraseña");
            dialog.setButton(AlertDialog.BUTTON_POSITIVE,"Aceptar",new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
            dialog.show();
		});
    }

    private void login() {
        if(etEmail.getText().toString().length() == 0) {
            layToken.setError("Correo invalido");
            layPassword.setError(null);
            return;
        }
        if(etPassword.getText().toString().length() == 0) {
            layPassword.setError("Contraseña invalida");
            layToken.setError(null);
            return;
        }
        startActivity(new Intent(LoginActivity.this, NowPaidActivity.class));
        finish();
    }
}
