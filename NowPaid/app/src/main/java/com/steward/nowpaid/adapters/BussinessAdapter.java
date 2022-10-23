package com.steward.nowpaid.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.steward.nowpaid.Constants;
import com.steward.nowpaid.DownloadImageTask;
import com.steward.nowpaid.R;
import com.steward.nowpaid.api.API;
import com.steward.nowpaid.model.BussinessItem;

import java.io.InputStream;

public class BussinessAdapter extends ArrayAdapter<BussinessItem>
{
	public BussinessAdapter(Context ctx) {
		super(ctx, R.layout.bussiness_item);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		BussinessItem buss = getItem(position);
		View sample = LayoutInflater.from(getContext()).inflate(R.layout.bussiness_item,null);
		TextView tvBussinessName = sample.findViewById(R.id.tvBussinessName);
		TextView tvBussinessType = sample.findViewById(R.id.tvBussinessType);
		TextView tvBussinessRating = sample.findViewById(R.id.tvBussinessRating);
		ImageView ivBussinessImage = sample.findViewById(R.id.ivBussinessImage);
		tvBussinessName.setText(buss.name);
		tvBussinessType.setText(Constants.categories[buss.category]);
		tvBussinessRating.setText(String.format("%.1f",buss.rating).replace(".",","));
		new DownloadImageTask((res) -> {
			ivBussinessImage.setImageBitmap(res);
		},buss).execute(API.instance.host + "/" + buss.icon);
		return sample;
	}

	public interface OnDownloaded {
		void finished(Bitmap bmp);
	}

	public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		OnDownloaded listener;
		BussinessItem item;

		public DownloadImageTask(OnDownloaded listen, BussinessItem item) {
			this.listener = listen;
			this.item = item;
		}

		protected Bitmap doInBackground(String... urls) {
			if(item.bmp_temp != null) {
				return item.bmp_temp;
			}
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
				API.log(urldisplay);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			item.bmp_temp = mIcon11;
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			listener.finished(result);
		}
	}
}
