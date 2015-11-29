package com.mkiisoft.uiweather.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.google.android.gms.wearable.Asset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class Utils {

    public enum Conditions {

        cloudy(26),
        sun(32),
        thunder(4),
        rain(10);

        private int _value;

        Conditions(int Value) {
            this._value = Value;
        }

        public int getValue() {
            return _value;
        }

        public static Conditions fromInt(int i) {
            for (Conditions b : Conditions.values()) {
                if (b.getValue() == i) {
                    return b;
                }
            }
            return null;
        }
    }

    int value;
    Conditions conditions = Conditions.fromInt(value);

    public static void saveFile( File filename, byte[] data ) throws IOException {
        FileOutputStream fOut = new FileOutputStream( filename );

        fOut.write( data );

        fOut.flush();
        fOut.close();
    }

    public static String decodeUTF8(byte[] bytes) {
        return new String(bytes, Charset.forName("UTF-8"));
    }

    public static void unbindDrawables(View view) {

        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
            view.setBackgroundDrawable(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));

            }
            ((ViewGroup) view).removeAllViews();
        }
        System.gc();
    }

    public static byte[] readFile( File filename ) throws IOException {
        FileInputStream fIn = new FileInputStream( filename );

        byte[] buffer = new byte[ fIn.available() ];
        fIn.read(buffer);
        fIn.close();
        return buffer;
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size=1024;
        try {
            byte[] bytes=new byte[buffer_size];
            for(;;) {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch(Exception ex){
            System.out.print(ex);
        }
    }

    public static boolean testConection(Context context){
        boolean HaveConnectedWifi = false;
        boolean HaveConnectedMobile = false;
        boolean result;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    HaveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    HaveConnectedMobile = true;
        }
        result = HaveConnectedWifi || HaveConnectedMobile;
        return result;
    }

    public static int randInt(int min, int max) {

        return (int)(Math.random() * ((max - min) + 1)) + min;
    }

    public static void animateTextView(int initialValue, int finalValue, final TextView textview) {
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(0.4f);
        int start = Math.min(initialValue, finalValue);
        int end = Math.max(initialValue, finalValue);
        int difference = Math.abs(finalValue - initialValue);
        Handler handler = new Handler();
        if(finalValue < 0){
            for (int count = 0; count >= finalValue; count--) {
                int time = Math.round(decelerateInterpolator.getInterpolation((((float) count) / difference)) * 100) * count;
                final int finalCount = ((initialValue < finalValue) ? initialValue - count : count);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText(finalCount + "");
                    }
                }, time);
            }
        }else{
            for (int count = start; count <= end; count++) {
                int time = Math.round(decelerateInterpolator.getInterpolation((((float) count) / difference)) * 100) * count;
                final int finalCount = ((initialValue > finalValue) ? initialValue - count : count);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textview.setText(finalCount + "");
                    }
                }, time);
            }
        }
    }

    /**
     *
     * Class to animate the background color of a View
     *
     * @param viewToAnimateItBackground the View that will animate
     * @param colorFrom int from Color. Could be generate trough Color.parse for HEX
     * @param colorTo   int to Color. Could be generate trough Color.parse for HEX
     * @param durationInMs final duration for the animation
     *
     */

    public static void animateBetweenColors(final View viewToAnimateItBackground, final int colorFrom, final int colorTo,
                                            final int durationInMs) {
        final ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            ColorDrawable colorDrawable = new ColorDrawable(colorFrom);

            @Override
            public void onAnimationUpdate(final ValueAnimator animator) {
                colorDrawable.setColor((Integer) animator.getAnimatedValue());
                viewToAnimateItBackground.setBackgroundDrawable(colorDrawable);
            }
        });
        if (durationInMs >= 0)
            colorAnimation.setInterpolator(new DecelerateInterpolator());
            colorAnimation.setStartDelay(200);
            colorAnimation.setDuration(durationInMs);
        colorAnimation.start();
    }

    /**
     * Static Class to generate "ellipse" string from API > 1 for TextViews.
     *
     * You must setText at first to know the final line count and the setText
     * again to generate the final effect.
     *
     * @param textView the TextView you want to apply the ellipse style.
     * @param maxLines the max number of lines you need to show and ellipse. If 0, will return full text.
     * @return will return full text if 0 or max lines with ellipse if set > 1.
     */
    public static String maxLines(TextView textView, int maxLines) {
        if (textView.getLineCount() > maxLines && textView.getLineCount() > 0 && maxLines > 0) {
            int lineEndIndex = textView.getLayout().getLineEnd(maxLines - 1);
            String line = textView.getText().subSequence(0, lineEndIndex - 3) + "...";

            return line;
        }else{
            String normal = textView.getText().toString();
            return normal;
        }
    }

    public static int determineScreenDensity(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int density = metrics.densityDpi;

        if (density==DisplayMetrics.DENSITY_HIGH) {
            Log.e("DENSITY_HIGH ", String.valueOf(density));
            return 4;
        }
        else if (density==DisplayMetrics.DENSITY_MEDIUM) {
            Log.e("DENSITY_MEDIUM ", String.valueOf(density));
            return 3;
        }
        else if (density==DisplayMetrics.DENSITY_LOW) {
            Log.e("DENSITY_LOW ", String.valueOf(density));
            return 3;
        }
        else if (density==DisplayMetrics.DENSITY_XHIGH){
            Log.e("DENSITY_XHIGH ", String.valueOf(density));
            return 5;
        }
        else if (density==DisplayMetrics.DENSITY_XXHIGH){
            Log.e("DENSITY_XXHIGH ", String.valueOf(density));
            return 5;
        }
        else if (density==DisplayMetrics.DENSITY_XXXHIGH){
            Log.e("DENSITY_XXXHIGH ", String.valueOf(density));
            return 5;
        }
        else if (density==DisplayMetrics.DENSITY_280){
            Log.e("DENSITY_280 ", String.valueOf(density));
            return 4;
        }
        else{
            Log.e("ELSE ", String.valueOf(density));
            return 6;
        }
    }

    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private static Asset toAsset(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public static Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }

        return bitmap;
    }

}