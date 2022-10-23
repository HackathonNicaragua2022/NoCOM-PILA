package com.steward.nowpaid;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SignInActivity extends AppCompatActivity {
    TextInputEditText etEmail, etPassword, etName, etPhoneNumber;
    TextInputLayout layToken, layPassword, layName,layPhoneNumber;
    ImageView ivAccount;
    AutoCompleteTextView spLocation;
    CheckBox cbProp;
    boolean secondStep = false;
    String imageID = "";

    public ActivityResultLauncher<Intent> chooseResultActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        ivAccount.setImageURI(selectedImageUri);
                        String ext = getfileExtension(selectedImageUri);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signin);
        layToken = findViewById(R.id.layREmail);
        layName = findViewById(R.id.layName);
        layPassword = findViewById(R.id.layRPassword);
        cbProp = findViewById(R.id.cbProperty);
        etEmail = findViewById(R.id.etREmail);
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etRPassword);
        spLocation = findViewById(R.id.spLocation);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        layPhoneNumber = findViewById(R.id.layPhoneNumber);
        ivAccount = findViewById(R.id.AccountImage);
        ArrayAdapter<String> adap = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new String[]{"Test"});
        spLocation.setAdapter(adap);
        etPassword.setOnFocusChangeListener((view,b) -> {
            etPassword.setError(null);
        });
        etEmail.setOnFocusChangeListener((view,b) -> {
            etPassword.setError(null);
        });
        etName.setOnFocusChangeListener((view,b) -> {
            etName.setError(null);
        });
        ((Button)findViewById(R.id.btnSignBack)).setOnClickListener((view) -> {
            layName.setError(null);
            layToken.setError(null);
            layPassword.setError(null);
            etName.setError(null);
            if(secondStep) {
                ((LinearLayout)findViewById(R.id.firstStep)).setVisibility(View.VISIBLE);
                ((LinearLayout)findViewById(R.id.secondStep)).setVisibility(View.GONE);
                ((Button)findViewById(R.id.btnSignBack)).setVisibility(View.INVISIBLE);
                ((Button)findViewById(R.id.btnSign)).setText("SIGUIENTE");
                ((TextView)findViewById(R.id.txtSteps)).setText("Primero ingrese su correo y contraseña");
                secondStep = false;
            }
        });
        ((Button)findViewById(R.id.btnSelectImageAccount)).setOnClickListener((view) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            chooseResultActivity.launch(Intent.createChooser(intent,
                    "Selecciona una imagen"));
        });
        ((Button)findViewById(R.id.btnSign)).setOnClickListener((view) -> {
            layName.setError(null);
            layToken.setError(null);
            layPassword.setError(null);
            etName.setError(null);
            if(!secondStep) {
                ((LinearLayout)findViewById(R.id.firstStep)).setVisibility(View.GONE);
                ((LinearLayout)findViewById(R.id.secondStep)).setVisibility(View.VISIBLE);
                ((Button)findViewById(R.id.btnSignBack)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.txtSteps)).setText("Ingrese su informacion personal");
                ((Button)findViewById(R.id.btnSign)).setText("FINALIZAR");
            } else {
                signin();
            }
            secondStep = true;
        });

    }

    private void signin() {
        if(etName.getText().toString().length() == 0) {
            layName.setError("Nombre invalido");
            return;
        }
        if(etEmail.getText().toString().length() == 0) {
            layToken.setError("Correo invalido");
            ((Button)findViewById(R.id.btnSignBack)).performClick();
            return;
        }
        if(etPassword.getText().toString().length() == 0) {
            layPassword.setError("Contraseña invalida");
            ((Button)findViewById(R.id.btnSignBack)).performClick();
            return;
        }
        if(etPhoneNumber.getText().toString().length() != 8) {
            etPhoneNumber.setError("Numero invalido");
            return;
        }
        if(spLocation.getText().toString().length() == 0) {
            Toast.makeText(this, "Seleccione una localidad", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(SignInActivity.this, NowPaidActivity.class));
        finish();
    }

    private String getfileExtension(Uri uri)
    {
        String extension;
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension= mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        return extension;
    }
}
