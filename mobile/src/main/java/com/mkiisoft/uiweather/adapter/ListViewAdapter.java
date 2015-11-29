package com.mkiisoft.uiweather.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mkiisoft.uiweather.R;
import com.mkiisoft.uiweather.utils.KeySaver;
import com.mkiisoft.uiweather.weather.CloudFogView;
import com.mkiisoft.uiweather.weather.CloudMoonView;
import com.mkiisoft.uiweather.weather.CloudRainView;
import com.mkiisoft.uiweather.weather.CloudSnowView;
import com.mkiisoft.uiweather.weather.CloudSunView;
import com.mkiisoft.uiweather.weather.CloudThunderView;
import com.mkiisoft.uiweather.weather.CloudView;
import com.mkiisoft.uiweather.weather.MoonView;
import com.mkiisoft.uiweather.weather.SunView;
import com.mkiisoft.uiweather.weather.WindView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mariano zorrilla on 11/8/15.
 */
public class ListViewAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<HashMap<String, String>> data;

    HashMap<String, String> resultp = new HashMap<>();

    // Temperature Type
    String Fahrenheit = "\u00B0F";
    String Celsius    = "\u00B0C";

    public ListViewAdapter(Context context,
                           ArrayList<HashMap<String, String>> arraylist) {
        this.context = context;
        data         = arraylist;

    }

    static class ViewHolderItem {
        // Views
        public LinearLayout mForecastView;
        public LinearLayout mForecastIcon;
        public TextView     mTempHigh;
        public TextView     mTempLow;
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final String code  = "code";
        final String high  = "high";
        final String low   = "low";
        final String white = "#00FFFFFF";
        final int size     = ViewGroup.LayoutParams.MATCH_PARENT;

        int lastPosition = -1;

        ViewHolderItem viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.forecast, parent, false);

            viewHolder = new ViewHolderItem();
            viewHolder.mForecastView = (LinearLayout) convertView.findViewById(R.id.forecast_view);
            viewHolder.mForecastIcon = (LinearLayout) convertView.findViewById(R.id.forecast_icon);
            viewHolder.mTempHigh     = (TextView) convertView.findViewById(R.id.temp_high);
            viewHolder.mTempLow      = (TextView) convertView.findViewById(R.id.temp_low);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.gravity = Gravity.START;

        resultp = data.get(position);

        SunView sunView              = new SunView(context);
        WindView windView            = new WindView(context);
        MoonView moonView            = new MoonView(context);
        CloudView cloudView          = new CloudView(context);
        CloudThunderView thunderView = new CloudThunderView(context);
        CloudRainView rainView       = new CloudRainView(context);
        CloudFogView fogView         = new CloudFogView(context);
        CloudSunView cloudSunView    = new CloudSunView(context);
        CloudMoonView cloudMoonView  = new CloudMoonView(context);
        CloudSnowView cloudSnowView  = new CloudSnowView(context);

        cloudView.setBgColor(Color.parseColor(white));
        cloudView.setLayoutParams(params);

        sunView.setBgColor(Color.parseColor(white));
        sunView.setLayoutParams(params);

        windView.setBgColor(Color.parseColor(white));
        windView.setLayoutParams(params);

        moonView.setBgColor(Color.parseColor(white));
        moonView.setLayoutParams(params);

        fogView.setBgColor(Color.parseColor(white));
        fogView.setLayoutParams(params);

        rainView.setBgColor(Color.parseColor(white));
        rainView.setLayoutParams(params);

        thunderView.setBgColor(Color.parseColor(white));
        thunderView.setLayoutParams(params);

        cloudSunView.setBgColor(Color.parseColor(white));
        cloudSunView.setLayoutParams(params);

        cloudMoonView.setBgColor(Color.parseColor(white));
        cloudMoonView.setLayoutParams(params);

        cloudSnowView.setBgColor(Color.parseColor(white));
        cloudSnowView.setLayoutParams(params);

        if(resultp != null) {

            viewHolder.mForecastIcon.removeAllViews();

            int lows = Integer.parseInt(resultp.get(low));
            int highs = Integer.parseInt(resultp.get(high));
            int codes = Integer.parseInt(resultp.get(code));

//            float celsiusLow = ((lows - 32) * 5 / 9);
//            float celsiusHigh = ((highs - 32) * 5 / 9);
            float fahrenheitLow = ((lows * 9) / 5) + 32;
            float fahrenheitHigh = ((highs * 9) / 5) + 32;

            int fahrenheitTempLow = Math.round(fahrenheitLow);
            int fahrenheitTempHigh = Math.round(fahrenheitHigh);

            if (KeySaver.isExist(context, "Fahrenheit")) {
                viewHolder.mTempLow.setText(fahrenheitTempLow + " " + Fahrenheit);
                viewHolder.mTempHigh.setText(fahrenheitTempHigh + " " + Fahrenheit);
            } else {
                viewHolder.mTempLow.setText(resultp.get(low) + " " + Celsius);
                viewHolder.mTempHigh.setText(resultp.get(high) + " " + Celsius);
            }

            if (codes >= 26 && codes <= 30) {
                viewHolder.mForecastIcon.addView(cloudView);
            } else if (codes >= 20 && codes <= 21) {
                viewHolder.mForecastIcon.addView(fogView);
            } else if (codes == 32) {
                viewHolder.mForecastIcon.addView(sunView);
            } else if (codes >= 33 && codes <= 34) {
                viewHolder.mForecastIcon.addView(cloudSunView);
            } else if (codes >= 37 && codes <= 39) {
                viewHolder.mForecastIcon.addView(thunderView);
            } else if (codes == 4) {
                viewHolder.mForecastIcon.addView(thunderView);
            } else if (codes == 24) {
                viewHolder.mForecastIcon.addView(windView);
            } else if (codes >= 9 && codes <= 12) {
                viewHolder.mForecastIcon.addView(rainView);
            } else if (codes == 31) {
                viewHolder.mForecastIcon.addView(moonView);
            } else if ((codes >= 5 && codes <= 8) || (codes >= 13 && codes <= 19)) {
                viewHolder.mForecastIcon.addView(cloudSnowView);
            } else if (codes == 3200) {
                viewHolder.mForecastIcon.addView(sunView);
            }

        }

        return convertView;
    }
}