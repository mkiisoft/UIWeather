package com.mkiisoft.uiweather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.mkiisoft.uiweather.utils.DeviceName;
import com.mkiisoft.uiweather.utils.KeySaver;
import com.mkiisoft.uiweather.utils.ResizeAnimation;
import com.mkiisoft.uiweather.utils.Utils;
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

import java.io.InputStream;
import java.security.Key;

public class MainActivity extends Activity implements DataApi.DataListener,
        MessageApi.MessageListener,
        NodeApi.NodeListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    private static int REQUEST_IMAGE_CAPTURE = 1;

    private static final String SEND_TEMP_PATH        = "/send-temp";
    private static final String SEND_CITY_PATH        = "/send-city";
    private static final String SEND_CODE_PATH        = "/send-code";
    private static final String SEND_BITMAP_PATH      = "/send-bitmap";
    private static final String SEND_MODEL_PATH       = "/send-model";
    private static final String SEND_UPDATE_PATH      = "/send-update";

    private static final String SEND_REQUEST_PATH     = "/send-request";

    // Views
    private TextView     mTextTemp, mTextCity;
    private ImageView    bgImage;
    private ImageView    mRequest;
    private LinearLayout mWeatherIcon;
    private View         mLineColor;
    private ProgressBar  mProgressBar;

    final String white = "#00FFFFFF";
    final int size     = ViewGroup.LayoutParams.MATCH_PARENT;

    // Custom Weather Views
    private SunView          sunView;
    private WindView         windView;
    private MoonView         moonView;
    private CloudView        cloudView;
    private CloudThunderView thunderView;
    private CloudRainView    rainView;
    private CloudFogView     fogView;
    private CloudSunView     cloudSunView;
    private CloudMoonView    cloudMoonView;
    private CloudSnowView    cloudSnowView;

    // Get Device Name
    private String deviceName;

    private boolean mIsConnected = false;
    private String  mWoeid       = "468739";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.gravity = Gravity.START;

        sunView        = new SunView(this);
        windView       = new WindView(this);
        moonView       = new MoonView(this);
        cloudView      = new CloudView(this);
        thunderView    = new CloudThunderView(this);
        rainView       = new CloudRainView(this);
        fogView        = new CloudFogView(this);
        cloudSunView   = new CloudSunView(this);
        cloudMoonView  = new CloudMoonView(this);
        cloudSnowView  = new CloudSnowView(this);

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

        final Typeface font = Typeface.createFromAsset(getAssets(), "thin.otf");
        final Typeface thin = Typeface.createFromAsset(getAssets(), "ultra.otf");

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextTemp = (TextView) stub.findViewById(R.id.temp);
                mTextCity = (TextView) stub.findViewById(R.id.city);
                bgImage = (ImageView) stub.findViewById(R.id.bg_image);
                mWeatherIcon = (LinearLayout) stub.findViewById(R.id.weather);
                mLineColor = stub.findViewById(R.id.line_color);
                mRequest = (ImageView) stub.findViewById(R.id.request_weather);
                mProgressBar = (ProgressBar) stub.findViewById(R.id.progress_bar);

                mTextCity.setTypeface(thin);
                mTextTemp.setTypeface(font);

                mTextTemp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (KeySaver.isExist(getApplicationContext(), "woeid")) {
                            sendMessageWear(SEND_UPDATE_PATH, KeySaver.getStringSavedShare(getApplicationContext(), "woeid"));
                        } else {
                            sendMessageWear(SEND_UPDATE_PATH, mWoeid);
                        }
                    }
                });

                mRequest.setVisibility(View.VISIBLE);
                mRequest.setEnabled(true);

                mRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRequest.setVisibility(View.GONE);
                        mRequest.setEnabled(false);
                        mProgressBar.setVisibility(View.VISIBLE);

                        if (KeySaver.isExist(getApplicationContext(), "woeid")) {
                            sendMessageWear(SEND_REQUEST_PATH, KeySaver.getStringSavedShare(getApplicationContext(), "woeid"));
                        } else {
                            sendMessageWear(SEND_REQUEST_PATH, mWoeid);
                        }

                    }
                });
            }
        });

        KeySaver.saveShare(this, "start", "start");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        deviceName = DeviceName.getDeviceName();

        Log.e("Nombre", "" + deviceName);
    }

    public void setBackgroundImage(Bitmap bitmap) {
        bgImage.setImageBitmap(bitmap);
        bgImage.invalidate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {

        KeySaver.removeKey(this, "start");

        if (!mResolvingError) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e("Connected", "YES");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
        mIsConnected = true;
        sendMessageWear(SEND_MODEL_PATH, deviceName);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("Unconnected", "YES");

        mIsConnected = false;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (SEND_BITMAP_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset photoAsset = dataMapItem.getDataMap()
                            .getAsset(SEND_BITMAP_PATH);
                    new LoadBitmapAsyncTask().execute(photoAsset);

                }
            }
        }
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mProgressBar.setVisibility(View.GONE);
                mRequest.setVisibility(View.GONE);
                mRequest.setEnabled(false);

                if (messageEvent.getPath().equalsIgnoreCase(SEND_TEMP_PATH)) {
                    mTextTemp.setAlpha(0);
                    mTextTemp.setText(new String(messageEvent.getData()));
                } else if (messageEvent.getPath().equalsIgnoreCase(SEND_CITY_PATH)) {
                    mTextCity.setAlpha(0);
                    mTextCity.setText(new String(messageEvent.getData()));
                    initAnim();
                } else if (messageEvent.getPath().equalsIgnoreCase(SEND_CODE_PATH)) {
                    mWeatherIcon.setAlpha(0);
                    int codes = Integer.parseInt(new String(messageEvent.getData()));
                    mWeatherIcon.removeAllViews();
                    conditionCode(codes);
                } else if (messageEvent.getPath().equalsIgnoreCase(SEND_UPDATE_PATH)) {
                    mTextTemp.setText(new String(messageEvent.getData()));
                }
            }
        });
    }

    @Override
    public void onPeerConnected(Node node) {
        sendMessageWear(SEND_MODEL_PATH, deviceName);
    }

    @Override
    public void onPeerDisconnected(Node node) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void sendMessageWear( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes() ).await();
                }

            }
        }).start();
    }

    private class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Asset... params) {

            if(params.length > 0) {

                if(isCancelled()){
                    return null;
                }

                Asset asset = params[0];

                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        mGoogleApiClient, asset).await().getInputStream();

                if (assetInputStream == null) {
                    Log.w("WEAR", "Requested an unknown Asset.");
                    return null;
                }
                return BitmapFactory.decodeStream(assetInputStream);

            } else {
                Log.e("WEAR", "Asset must be non-null");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if(bitmap != null) {
                setBackgroundImage(bitmap);
            }
        }

        @Override
        protected void onCancelled(){

        }
    }

    private void initAnim(){
        mTextTemp.animate().alpha(0).setDuration(400);
        mTextCity.animate().alpha(0).setDuration(400);
        mWeatherIcon.animate().alpha(0).setDuration(400);
        final ResizeAnimation resetAnimation = new ResizeAnimation(mLineColor, 0);
        resetAnimation.setDuration(400);
        mLineColor.startAnimation(resetAnimation);
        mLineColor.animate().alpha(0).setDuration(400)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {

                        setAlpha(0);
                        mLineColor.setAlpha(1);
                        mTextCity.setTranslationY(-mTextCity.getHeight());

                        CharSequence text = mTextCity.getText();
                        float width = mTextCity.getPaint().measureText(text, 0, text.length());
                        int textWidth = Math.round(width);
                        float percent = (float) textWidth / 130 * 100;
                        int newWidth = (int) percent;

                        final ResizeAnimation resizeAnimation = new ResizeAnimation(mLineColor, newWidth);
                        resizeAnimation.setDuration(600);
                        resizeAnimation.setStartOffset(500);
                        mLineColor.startAnimation(resizeAnimation);

                        mTextCity.animate().alpha(1).setDuration(500)
                                .translationY(0).setStartDelay(600)
                                .setInterpolator(new DecelerateInterpolator())
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTextCity.setAlpha(1);
                                        mTextCity.setTranslationY(0);

                                        mTextTemp.animate().alpha(1).setDuration(500)
                                                .setStartDelay(200)
                                                .withEndAction(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mTextTemp.setAlpha(1);
                                                    }
                                                });

                                        mWeatherIcon.animate().alpha(1).setDuration(500)
                                                .setStartDelay(200)
                                                .withEndAction(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mWeatherIcon.setAlpha(1);
                                                    }
                                                });

                                    }
                                });
                    }
                });
    }

    private void setAlpha(float alpha){
        mTextTemp.setAlpha(alpha);
        mWeatherIcon.setAlpha(alpha);
    }

    private void conditionCode(int code) {
        if (code >= 26 && code <= 30) {
            mWeatherIcon.addView(cloudView);
        } else if (code >= 20 && code <= 21) {
            mWeatherIcon.addView(fogView);
        } else if (code == 32) {
            mWeatherIcon.addView(sunView);
        } else if (code >= 33 && code <= 34) {
            mWeatherIcon.addView(cloudSunView);
        } else if (code >= 37 && code <= 39) {
            mWeatherIcon.addView(thunderView);
        } else if (code == 45) {
            mWeatherIcon.addView(thunderView);
        } else if (code == 4) {
            mWeatherIcon.addView(thunderView);
        } else if (code == 24) {
            mWeatherIcon.addView(windView);
        } else if (code >= 9 && code <= 12) {
            mWeatherIcon.addView(rainView);
        } else if (code == 31) {
            mWeatherIcon.addView(moonView);
        } else if ((code >= 5 && code <= 8) || (code >= 13 && code <= 19)) {
            mWeatherIcon.addView(cloudSnowView);
        } else if (code == 3200) {
            mWeatherIcon.addView(sunView);
        }
    }
}
