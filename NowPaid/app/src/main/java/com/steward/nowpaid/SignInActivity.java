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
import com.steward.nowpaid.api.API;
import com.steward.nowpaid.api.APIEvent;
import com.steward.nowpaid.api.OnLoggingListener;
import com.steward.nowpaid.api.OnObjectResponse;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SignInActivity extends AppCompatActivity implements APIEvent {
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
                        API.instance.requestUploadImage(getBaseContext().getContentResolver().openInputStream(selectedImageUri), ext, new OnObjectResponse() {
                            @Override
                            public void successfully(JSONObject res) {
                                try {
                                    imageID = res.getString("id") + "." + ext;
                                } catch (Exception e) {}
                            }
                        });
                    } catch (FileNotFoundException e) {
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
        ArrayAdapter<String> adap = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, Constants.locations);
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
        API.instance.reportEvents(this);
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
        API.instance.singin(etName.getText().toString(), etPhoneNumber.getText().toString(), imageID, Constants.getIndexLoc(spLocation.getText().toString()), cbProp.isChecked(), etEmail.getText().toString(), etPassword.getText().toString(), new OnObjectResponse() {
            @Override
            public void successfully(JSONObject result) {
                runOnUiThread(() -> {
                    try {
                        if(result.getString("message").startsWith("user_exist")) {
                            layToken.setError("Correo invalido");
                            ((Button)findViewById(R.id.btnSignBack)).performClick();
                            Toast.makeText(SignInActivity.this, "Ya existe una cuenta vinculada a este correo.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        API.instance.disposeEvent();
                        Session.save(SignInActivity.this);
                        startActivity(new Intent(SignInActivity.this, NowPaidActivity.class));
                        finish();
                    } catch (Exception e){
                    }
                });
            }
        });
    }

    @Override
    public void timeout() {
        runOnUiThread(() -> {
            final AlertDialog dialog = new AlertDialog.Builder(SignInActivity.this).create();
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
    private String getfileExtension(Uri uri)
    {
        String extension;
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        extension= mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
        return extension;
    }
}
