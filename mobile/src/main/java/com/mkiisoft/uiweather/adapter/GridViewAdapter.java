package com.mkiisoft.uiweather.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mkiisoft.uiweather.R;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mkiisoft.uiweather.R;
import com.mkiisoft.uiweather.WeatherActivity;
import com.mkiisoft.uiweather.utils.SquareImageView;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mariano on 11/8/15.
 */
public class GridViewAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<HashMap<String, String>> data;

    HashMap<String, String> resultp = new HashMap<String, String>();

    public GridViewAdapter(Context context,
                           ArrayList<HashMap<String, String>> arraylist) {
        this.context = context;
        data = arraylist;

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

    public View getView(final int position, final View convertView, ViewGroup parent) {

        final String img = "images";
        final String imgId = "imageId";
        final String title = "content";

        final SquareImageView appicon;
        final Exception e = null;

        final TextView mTitle;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.grid_item, parent, false);

        resultp = data.get(position);

        final String[] titleIndex = new String[position];

        itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                resultp = data.get(position);
                Intent intent = new Intent(context, WeatherActivity.class);

                intent.putExtra("url", resultp.get(img));
                intent.putExtra("id", resultp.get(imgId));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(intent);
            }
        });

        appicon = (SquareImageView) itemView.findViewById(R.id.grid_thumb_img);
        mTitle = (TextView) itemView.findViewById(R.id.title_text);
        mTitle.setAlpha(0);

        mTitle.setText(Html.fromHtml(resultp.get(title)));

        Glide.with(context)
                .load(resultp.get(img))
                .asBitmap()
                .placeholder(R.drawable.ic_loading)
                .skipMemoryCache(false)
                .into(new SimpleTarget() {

                    @Override
                    public void onResourceReady(final Object resource, final GlideAnimation glideAnimation) {
                        appicon.setImageBitmap((Bitmap) resource);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                if (resource != null) {
                                    Palette.from((Bitmap) resource).generate(new Palette.PaletteAsyncListener() {
                                        @Override
                                        public void onGenerated(Palette palette) {
                                            int vibrant = palette.getVibrantColor(0x3333FF);
                                            if (vibrant == setInteger(R.integer.empty_img_color)) {
                                                mTitle.setBackgroundColor(setColor(R.color.colorPrimary));
                                            } else {
                                                mTitle.setBackgroundColor(vibrant);
                                            }
                                            mTitle.animate().alpha(1).setDuration(300);
                                        }
                                    });
                                }
                            }
                        }, 0);
                    }

                    @Override
                     public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        Glide.with(context).load(R.drawable.ic_img_broken).skipMemoryCache(false).into(appicon);
                        mTitle.setBackgroundColor(setColor(R.color.colorPrimary));
                        mTitle.animate().alpha(1).setDuration(300);
                        mTitle.setText(setText(R.string.img_error));
                    }
                });

        return itemView;
    }

    private int setColor(int color) {
        context.getResources().getColor(color);
        return color;
    }

    private Drawable setDrawable(int id) {
        Drawable img = context.getResources().getDrawable(id);
        return img;
    }

    private int setInteger(int integer) {
        context.getResources().getInteger(integer);
        return integer;
    }

    private String setText(int id) {
        String text = context.getResources().getString(id);
        return text;
    }
}