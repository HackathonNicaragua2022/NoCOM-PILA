package com.steward.nowpaid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class BussinessActivity extends AppCompatActivity implements APIEvent
{
    String phonenumber = "";
    String link_google_maps = "";
    String id = "";
    TextView tvBName,tvInformation,tvBRating;
    MaterialRatingBar mbrR;
    SliderAdapterBuss adp;
    LinearLayout layProducts, lvReviews;
    FloatingActionButton reviewBuss;
    boolean detected_review = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bussiness_activity);
        id = getIntent().getExtras().getString("id");
        API.instance.reportEvents(this);
        tvBName = findViewById(R.id.tvBName);
        tvInformation = findViewById(R.id.tvBInfo);
        tvBRating = findViewById(R.id.tvRating);
        mbrR = findViewById(R.id.mrbRating);
        SliderView sliderView = findViewById(R.id.imageSlider);
        reviewBuss = findViewById(R.id.btnRate);
        adp = new SliderAdapterBuss(this);
        sliderView.setSliderTransformAnimation(SliderAnimations.CUBEINROTATIONTRANSFORMATION);
        sliderView.setSliderAdapter(adp);
        layProducts = findViewById(R.id.layProducts);
        lvReviews = findViewById(R.id.lvReviews);
        findViewById(R.id.btnUbication).setOnClickListener((view) -> {
            Uri gmmIntentUri = Uri.parse(link_google_maps);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            API.instance.disposeEvent();
            startActivity(mapIntent);
        });
        findViewById(R.id.btnWhatsapp).setOnClickListener((view) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String uri = "whatsapp://send?phone=505" + phonenumber + "&text=Hola";
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        });
        reviewBuss.setOnClickListener((v) -> {
            if(detected_review) {

            } else {
                rateBussiness();
            }
        });
        updateInfo();
    }

    private void updateInfo() {
        API.instance.requestDetailsBuss(id, new OnObjectResponse() {
            @Override
            public void successfully(final JSONObject res) {
                runOnUiThread(() -> {
                    try {
                        tvBName.setText(res.getString("name"));
                        phonenumber = res.getString("property_phone");
                        link_google_maps = res.getString("google_maps");
                        tvInformation.setText("Propietario: " + res.getString("property_name") + "\n" +
                                "No. Telefono: " + phonenumber);
                        float rating = (float)res.getDouble("rating");
                        tvBRating.setText(String.format("%.1f",rating).replace(".",","));
                        mbrR.setRating(rating);
                        JSONArray images = res.getJSONArray("images");
                        JSONArray menu = res.getJSONArray("menu");
                        JSONArray ratings = res.getJSONArray("ratings");
                        adp.renewItems(new ArrayList<String>());
                        for(int i = 0;i < images.length(); i++) {
                            adp.addItem(images.getString(i));
                        }
                        adp.notifyDataSetChanged();
                        layProducts.removeAllViews();
                        lvReviews.removeAllViews();
                        for(int i = 0;i < menu.length(); i++) {
                            JSONObject itm = menu.getJSONObject(i);
                            View sample = LayoutInflater.from(BussinessActivity.this).inflate(R.layout.product_item,null);
                            TextView tvName = sample.findViewById(R.id.tvName);
                            TextView tvPrice = sample.findViewById(R.id.tvPrice);
                            ImageView ivIcon = sample.findViewById(R.id.ivCategoryImage);
                            tvName.setText(itm.getString("name"));
                            tvPrice.setText(String.format("%.2f",itm.getDouble("price"))+" "+itm.getString("money"));
                            new DownloadImageTask(ivIcon).execute(API.instance.host + "/" + itm.getString("image"));
                            layProducts.addView(sample);
                        }
                        RatingItem[] ratings_ordered = convert(ratings);
                        for(int i = 0;i < ratings_ordered.length; i++) {
                            View sample = LayoutInflater.from(BussinessActivity.this).inflate(R.layout.rating_item,null);
                            TextView tvClientName = sample.findViewById(R.id.tvClientName);
                            TextView tvClientReview = sample.findViewById(R.id.tvClientReview);
                            MaterialRatingBar mrbBussinessRating = sample.findViewById(R.id.mbrClientRating);
                            tvClientName.setText(ratings_ordered[i].name);
                            tvClientReview.setText(ratings_ordered[i].review);
                            mrbBussinessRating.setRating(ratings_ordered[i].rating);
                            lvReviews.addView(sample);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private RatingItem[] convert(JSONArray array) {
        try{
            RatingItem[] items = new RatingItem[array.length()];
            int offset = 0, ignore = -1;
            for(int i = 0;i < array.length(); i++) {
                JSONObject itm = array.getJSONObject(i);
                if(itm.getString("user").contains(Session.userID)) {
                    items[offset] = new RatingItem(itm.getString("name"),itm.getString("text"),itm.getInt("stars"));
                    offset++;
                    ignore = i;
                    reviewBuss.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                    reviewBuss.setImageResource(R.drawable.ic_close);
                    detected_review = true;
                    break;
                }
            }
            for(int i = 0;i < array.length(); i++) {
                JSONObject itm = array.getJSONObject(i);
                if(i != ignore) {
                    items[offset] = new RatingItem(itm.getString("name"),itm.getString("text"),itm.getInt("stars"));
                    offset++;
                }
            }
            return items;
        }catch (Exception e){}
        return null;
    }

    public void rateBussiness() {
        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(BussinessActivity.this).create();
        dialog.setTitle("Poner reseña");
        View view = LayoutInflater.from(this).inflate(R.layout.rate_bussiness,null);
        final EditText etReviewText = view.findViewById(R.id.etRateText);
        final MaterialRatingBar mrbBuss = view.findViewById(R.id.rateBussStar);
        dialog.setView(view);
        dialog.setCancelable(false);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"Cancelar",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE,"Aceptar",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
               API.instance.rateBussiness(id, mrbBuss.getRating(), etReviewText.getText().toString(), new OnObjectResponse() {
                   @Override
                   public void successfully(JSONObject object) {
                       runOnUiThread(() -> {
                           Toast.makeText(BussinessActivity.this, "Gracias por su opinion!", Toast.LENGTH_SHORT).show();
                           updateInfo();
                       });
                   }
               });
               dialog.dismiss();
            }
        });
        dialog.show();
    }

    public class SliderAdapterBuss extends SliderViewAdapter<SliderAdapterBuss.SliderAdapterVH> {

        private Context context;
        private List<String> mSliderItems = new ArrayList<>();

        public SliderAdapterBuss(Context context) {
            this.context = context;
        }

        public void renewItems(List<String> sliderItems) {
            this.mSliderItems = sliderItems;
            notifyDataSetChanged();
        }

        public void deleteItem(int position) {
            this.mSliderItems.remove(position);
            notifyDataSetChanged();
        }

        public void addItem(String sliderItem) {
            this.mSliderItems.add(sliderItem);
            notifyDataSetChanged();
        }

        @Override
        public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.bussiness_image, null);
            return new SliderAdapterVH(inflate);
        }

        @Override
        public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {
            new DownloadImageTask(viewHolder.imageViewBackground).execute(API.instance.host + "/" + mSliderItems.get(position));
        }

        @Override
        public int getCount() {
            //slider view count could be dynamic size
            return mSliderItems.size();
        }

        class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

            View itemView;
            ImageView imageViewBackground;

            public SliderAdapterVH(View itemView) {
                super(itemView);
                imageViewBackground = itemView.findViewById(R.id.ivBussinessImageSlider);
                this.itemView = itemView;
            }
        }
    }

    public void timeout() {
        runOnUiThread(() -> {
            final AlertDialog dialog = new AlertDialog.Builder(BussinessActivity.this).create();
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