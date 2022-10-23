package com.steward.nowpaid;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.steward.nowpaid.api.API;
import com.steward.nowpaid.api.APIEvent;
import com.steward.nowpaid.api.OnObjectResponse;
import com.steward.nowpaid.model.BussinessItem;
import com.steward.nowpaid.model.ProductItem;
import com.steward.nowpaid.model.RatingItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class EditBussinessActivity extends AppCompatActivity implements APIEvent
{
    String id = "";
    boolean publish = false;
    LinearLayout layNameBuss, layGoogleMap, layMenuProducts, layImages;
    EditText etName, etGoogleMap;
    AutoCompleteTextView spCategories;
    ImageView ivIcon,ivImageMenu;
    JSONObject data_mutable;
    int upload_target = -1;
    String temp_menu_imageID = "";

    public ActivityResultLauncher<Intent> chooseResultActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        switch (upload_target){
                            case 0: // logo
                                ivIcon.setImageURI(selectedImageUri);
                                break;
                            case 1: {
                                View sample = LayoutInflater.from(EditBussinessActivity.this).inflate(R.layout.grid_image, null);
                                ImageView ivIconc = sample.findViewById(R.id.item_image);
                                ivIconc.setImageURI(selectedImageUri);
                                layImages.addView(sample);
                            }
                                break;
                            case 2:
                            {
                                ivImageMenu.setImageURI(selectedImageUri);
                            }
                                break;
                        }
                        String ext = getfileExtension(selectedImageUri);
                        API.instance.requestUploadImage(getBaseContext().getContentResolver().openInputStream(selectedImageUri), ext, new OnObjectResponse() {
                            @Override
                            public void successfully(JSONObject res) {
                                try {
                                    String imageID = res.getString("id") + "." + ext;
                                    switch (upload_target) {
                                        case 0: // logo
                                            data_mutable.remove("icon");
                                            data_mutable.put("icon", imageID);
                                            break;
                                        case 1: // imagenes
                                            JSONArray ari = data_mutable.getJSONArray("images");
                                            ari.put(imageID);
                                            data_mutable.remove("images");
                                            data_mutable.put("images",ari);
                                            break;
                                        case 2: // menu
                                            temp_menu_imageID = imageID;
                                            break;
                                    }
                                } catch (Exception e) {}
                            }
                        });
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editing_bussiness_activity);
        API.instance.reportEvents(this);
        id = getIntent().getExtras().getString("id");
        publish = getIntent().getExtras().getBoolean("publish");
        layMenuProducts = findViewById(R.id.layMenuProducts);
        layNameBuss = findViewById(R.id.layNameBuss);
        layGoogleMap = findViewById(R.id.layGoogleMap);
        etName = findViewById(R.id.etNameBuss);
        etGoogleMap = findViewById(R.id.etGoogleMap);
        spCategories = findViewById(R.id.spCategory);
        ivIcon = findViewById(R.id.bussIcon);
        if(!publish) {
            API.instance.requestDetailsBuss(id, new OnObjectResponse() {
                @Override
                public void successfully(JSONObject res) {
                    try {
                        data_mutable = res;
                        runOnUiThread(()-> {
                            loadJSON();
                        });
                    } catch (Exception e){
                    }
                }
            });
        } else {
            try {
                data_mutable = new JSONObject("{\"name\":\"\"," +
                                "\"category\":0," +
                                "\"google_maps\":\"\"," +
                                "\"images\":[]," +
                                "\"menu\":[]," +
                                "\"icon\":\"\"}");
                loadJSON();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        layImages = findViewById(R.id.layImages);
        ArrayAdapter<String> adap = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, Constants.categories_fix);
        spCategories.setAdapter(adap);
        ((Button)findViewById(R.id.btnAddProduct)).setOnClickListener((v) -> {
            addMenuDialog();
        });
        ((Button)findViewById(R.id.btnSelectImageIcon)).setOnClickListener((v) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            chooseResultActivity.launch(Intent.createChooser(intent,
                    "Selecciona una imagen"));
            upload_target = 0;
        });
        ((Button)findViewById(R.id.btnUploadImage)).setOnClickListener((v) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            chooseResultActivity.launch(Intent.createChooser(intent,
                    "Selecciona una imagen"));
            upload_target = 1;
        });
        ((FloatingActionButton)findViewById(R.id.btnSaveBuss)).setOnClickListener((v) -> {
            try {
                data_mutable.remove("id");
                data_mutable.put("id", id);
                data_mutable.remove("name");
                data_mutable.put("name", etName.getText().toString());
                data_mutable.remove("google_maps");
                data_mutable.put("google_maps", etGoogleMap.getText().toString());
                data_mutable.remove("category");
                data_mutable.put("category",Constants.getIndexCat(spCategories.getText().toString()));
                if(publish) {
                    API.instance.publishBussiness(data_mutable, new OnObjectResponse() {
                        @Override
                        public void successfully(JSONObject object) {
                            runOnUiThread(() -> {
                                Toast.makeText(EditBussinessActivity.this, publish ? "Negocio creado correctamente" : "Negocio Editado correctamente", Toast.LENGTH_SHORT).show();
                                setResult(130, new Intent());
                                finish();
                            });
                        }
                    });
                } else {
                    API.instance.editBussiness(data_mutable, new OnObjectResponse() {
                        @Override
                        public void successfully(JSONObject object) {
                            runOnUiThread(() -> {
                                Toast.makeText(EditBussinessActivity.this, publish ? "Negocio editado correctamente" : "Negocio Editado correctamente", Toast.LENGTH_SHORT).show();
                                setResult(130, new Intent());
                                finish();
                            });
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void loadJSON() {
        try {
            API.log(data_mutable.toString());
            etName.setText(data_mutable.getString("name"));
            etGoogleMap.setText(data_mutable.getString("google_maps"));
            if(data_mutable.getInt("category") > 0) {
                spCategories.setText(Constants.categories_fix[data_mutable.getInt("category") - 1]);
            }
            if(data_mutable.getString("icon").length() > 0) {
                new DownloadImageTask(ivIcon).execute(API.instance.host + "/" + data_mutable.getString("icon"));
            }
            JSONArray menu = data_mutable.getJSONArray("menu");
            JSONArray images = data_mutable.getJSONArray("images");
            for(int i = 0;i < menu.length(); i++) {
                JSONObject itm = menu.getJSONObject(i);
                View sample = LayoutInflater.from(EditBussinessActivity.this).inflate(R.layout.product_item,null);
                TextView tvName = sample.findViewById(R.id.tvName);
                TextView tvPrice = sample.findViewById(R.id.tvPrice);
                ImageView ivIcon = sample.findViewById(R.id.ivCategoryImage);
                tvName.setText(itm.getString("name"));
                tvPrice.setText(String.format("%.2f",itm.getDouble("price"))+" "+itm.getString("money"));
                new DownloadImageTask(ivIcon).execute(API.instance.host + "/" + itm.getString("image"));
                layMenuProducts.addView(sample);
            }
            for(int i = 0;i < images.length(); i++) {
                String itm = images.getString(i);
                View sample = LayoutInflater.from(EditBussinessActivity.this).inflate(R.layout.grid_image,null);
                ImageView ivIcon = sample.findViewById(R.id.item_image);
                new DownloadImageTask(ivIcon).execute(API.instance.host + "/" + itm);
                layImages.addView(sample);
            }
        }catch (Exception e){
            API.log(e.toString());
        }
    }

    public void addMenuDialog() {
        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(EditBussinessActivity.this).create();
        dialog.setTitle("Agregar Producto");
        View view = LayoutInflater.from(this).inflate(R.layout.add_menu_item,null);
        final EditText etMenuName = view.findViewById(R.id.etProductName);
        final EditText etMenuPrice = view.findViewById(R.id.etProductPrice);
        final AutoCompleteTextView spMoney = view.findViewById(R.id.spMoney);
        spMoney.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new String[]{"C$","$"}));
        ivImageMenu = view.findViewById(R.id.menuImage);
        ((Button)view.findViewById(R.id.btnSelectImage)).setOnClickListener((v) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            chooseResultActivity.launch(Intent.createChooser(intent,
                    "Selecciona una imagen"));
            upload_target = 2;
        });
        dialog.setView(view);
        dialog.setCancelable(false);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"Cancelar",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE,"Agregar",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    JSONArray mns = data_mutable.getJSONArray("menu");
                    mns.put(new JSONObject("{\"name\":\"" + etMenuName.getText().toString() + "\"," +
                            "\"price\": "+etMenuPrice.getText().toString() + "," +
                            "\"money\": \""+spMoney.getText().toString() + "\"," +
                            "\"image\":\""+temp_menu_imageID+"\"}"));
                    data_mutable.remove("menu");
                    data_mutable.put("menu", mns);
                    layMenuProducts.removeAllViews();
                    for(int i = 0;i < mns.length(); i++) {
                        JSONObject itm = mns.getJSONObject(i);
                        View sample = LayoutInflater.from(EditBussinessActivity.this).inflate(R.layout.product_item,null);
                        TextView tvName = sample.findViewById(R.id.tvName);
                        TextView tvPrice = sample.findViewById(R.id.tvPrice);
                        ImageView ivIcon = sample.findViewById(R.id.ivCategoryImage);
                        tvName.setText(itm.getString("name"));
                        tvPrice.setText(String.format("%.2f",itm.getDouble("price"))+" "+itm.getString("money"));
                        new DownloadImageTask(ivIcon).execute(API.instance.host + "/" + itm.getString("image"));
                        layMenuProducts.addView(sample);
                    }
                }catch (Exception e){}
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void timeout() {
        runOnUiThread(() -> {
            final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(EditBussinessActivity.this).create();
            dialog.setTitle("Error al conectarse");
            dialog.setMessage("Por favor revise su conexion a internet e intente de nuevo");
            dialog.setCancelable(false);
            dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE,"Aceptar",new DialogInterface.OnClickListener(){
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