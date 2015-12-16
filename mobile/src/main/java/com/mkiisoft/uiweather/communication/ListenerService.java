package com.mkiisoft.uiweather.communication;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mkiisoft.uiweather.WeatherActivity;
import com.mkiisoft.uiweather.utils.KeySaver;

import com.mkiisoft.uiweather.R;
import com.mkiisoft.uiweather.utils.ResizeAnimation;
import com.mkiisoft.uiweather.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

/**
 * Listens to DataItems and Messages from the local node.
 */
public class ListenerService extends WearableListenerService {

    private final String TAG = "ListenerService";

    private final String START_ACTIVITY_PATH     = "/start-activity";
    private final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    public  final String COUNT_PATH              = "/count";

    private final String SEND_REQUEST_PATH       = "/send-request";
    private final String SEND_TEMP_PATH          = "/send-temp";
    private final String SEND_CITY_PATH          = "/send-city";
    private final String SEND_CODE_PATH          = "/send-code";
    private final String SEND_UPDATE_PATH        = "/send-update";
    private final String SEND_UPDATE_CODE      = "/send-update-code";

    private final String SEND_BITMAP_PATH        = "/send-bitmap";

    String Fahrenheit = "\u00B0F";
    String Celsius = "\u00B0C";
    private String mWoeid = "468739";
    private String mApiURL;
    private String mApiImg;

    GoogleApiClient mGoogleApiClient;

    Handler handler;

    private String temp;
    private String code;
    private String city;
    private String pais;

    Utils.ApiCall mApiCall;
    private JSONObject jsonResponse;

    ArrayList<HashMap<String, String>> mData;
    HashMap<String, String> result = new HashMap<>();

    private String[] noImages = {
            "file:///android_asset/no_image_one.png",
            "file:///android_asset/no_image_two.png",
            "file:///android_asset/no_image_three.png",
            "file:///android_asset/no_image_four.png",
            "file:///android_asset/no_image_one.png",
            "file:///android_asset/no_image_two.png",
            "file:///android_asset/no_image_three.png",
            "file:///android_asset/no_image_four.png",
            "file:///android_asset/no_image_one.png",
            "file:///android_asset/no_image_two.png"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        handler = new Handler();

        mApiCall = new Utils.ApiCall();

        mApiURL = getResources().getString(R.string.api_url_woeid);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                return;
            }
        }

        // Loop through the events and send a message back to the node that created the data item.
        for (DataEvent event : events) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
            if (COUNT_PATH.equals(path)) {
                // Get the node id of the node that created the data item from the host portion of
                // the uri.
                String nodeId = uri.getHost();
                // Set the data of the message to be the bytes of the Uri.
                byte[] payload = uri.toString().getBytes();

                // Send the rpc
                Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, DATA_ITEM_RECEIVED_PATH,
                        payload);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (!KeySaver.isExist(getApplicationContext(), "start")) {
            if (messageEvent.getPath().equalsIgnoreCase(START_ACTIVITY_PATH)) {
                Intent intent = new Intent(this, WeatherActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (messageEvent.getPath().equalsIgnoreCase(SEND_REQUEST_PATH)) {
                AsyncWeather(mApiURL + new String(messageEvent.getData()));
            } else if (messageEvent.getPath().equalsIgnoreCase(SEND_UPDATE_PATH)){
                updateAsyncWeather(mApiURL + new String(messageEvent.getData()));
            }else {
                super.onMessageReceived(messageEvent);
            }
        }

    }

    public void sendMessageWear(final String path, final String text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes()).await();
                }

            }
        }).start();
    }

    @Override
    public void onPeerConnected(Node peer) {
        LOGD(TAG, "onPeerConnected: " + peer);
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        LOGD(TAG, "onPeerDisconnected: " + peer);
    }

    public static void LOGD(final String tag, String message) {

    }

    public void AsyncWeather(String urlConnection) {

        mApiCall.get(urlConnection, null, new AsyncHttpResponseHandler() {

            final ArrayList arraylist = new ArrayList<>();

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int i, Header[] headers, final byte[] response) {
                try {

                    JSONObject jsonWeather = new JSONObject(Utils.decodeUTF8(response));

                    JSONObject queryObject  = jsonWeather.getJSONObject("query");

                    final JSONObject data = queryObject.getJSONObject("datos");

                    city = data.getString("city");
                    temp = data.getString("temp");
                    code = data.getString("code");
                    pais = data.getString("country");

                    final JSONArray  photosObject = jsonWeather.getJSONArray("fotos");

                    if (photosObject.length() > 0 ){

                        for (int imgs = 0; imgs < photosObject.length(); imgs++) {

                            HashMap<String, String> images = new HashMap<>();

                            JSONObject imagesResult = photosObject.getJSONObject(imgs);

                            String url = imagesResult.getString("url_big");

                            images.put("url_big", url);

                            arraylist.add(images);

                        }

                    } else {

                        for (int imgs = 0; imgs < 10; imgs++) {

                            HashMap<String, String> images = new HashMap<>();

                            images.put("url_big", noImages[imgs]);


                            arraylist.add(images);

                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessageWear(SEND_CITY_PATH, city);
                            sendMessageWear(SEND_CODE_PATH, code);
                            sendMessageWear(SEND_TEMP_PATH, temp + Celsius);

                            int ran = Utils.randInt(0, photosObject.length());
                            mData = arraylist;
                            result = mData.get(ran);
                            final String img = result.get("url_big");

                            Glide.with(ListenerService.this).load(photosObject.length() > 0 ? img : Uri.parse(img)).asBitmap().override(800, 800).
                                    into(new SimpleTarget() {
                                        @Override
                                        public void onResourceReady(Object resource, GlideAnimation glideAnimation) {

                                            final Bitmap finalBitmap = (Bitmap) resource;

                                            sendPhoto(Utils.toAsset(finalBitmap));
                                        }
                                    });
                        }
                    });

                } catch (Exception e) {
                    System.out.print(e);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
    }

    public void updateAsyncWeather(String urlConnection) {

        mApiCall.get(urlConnection, null, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int i, Header[] headers, final byte[] response) {
                try {

                    JSONObject jsonWeather = new JSONObject(Utils.decodeUTF8(response));

                    JSONObject queryObject = jsonWeather.getJSONObject("query");
                    final JSONObject data = queryObject.getJSONObject("datos");
                    temp = data.getString("temp");
                    code = data.getString("code");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("si", "pide el watch face");
                            sendMessageWear(SEND_UPDATE_PATH, temp + Celsius);
                            sendMessageWear(SEND_UPDATE_CODE, code);
                        }
                    });

                } catch (Exception e) {
                    System.out.print(e);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private void sendPhoto(Asset asset) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(SEND_BITMAP_PATH);
        dataMap.getDataMap().putAsset(SEND_BITMAP_PATH, asset);
        dataMap.getDataMap().putLong("time", new Date().getTime());
        PutDataRequest request = dataMap.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.e(TAG, "Sending image was successful: " + dataItemResult.getStatus()
                                .isSuccess());
                    }
                });

    }

}
