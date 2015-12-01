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

    private static final String TAG = "ListenerService";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    public static final String COUNT_PATH = "/count";

    private static final String SEND_REQUEST_PATH = "/send-request";
    private static final String SEND_TEMP_PATH = "/send-temp";
    private static final String SEND_CITY_PATH = "/send-city";
    private static final String SEND_CODE_PATH = "/send-code";
    private static final String SEND_UPDATE_PATH = "/send-update";

    private static final String SEND_BITMAP_PATH      = "/send-bitmap";

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

    ArrayList<HashMap<String, String>> data;
    HashMap<String, String> result = new HashMap<>();

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
        mApiImg = getResources().getString(R.string.apiurl);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
//        LOGD(TAG, "onDataChanged: " + dataEvents);
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

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int i, Header[] headers, final byte[] response) {
                try {

                    JSONObject jsonWeather = new JSONObject(Utils.decodeUTF8(response));

                    JSONObject queryObject = jsonWeather.getJSONObject("query");
                    final JSONObject data = queryObject.getJSONObject("datos");
                    city = data.getString("city");
                    temp = data.getString("temp");
                    code = data.getString("code");
                    pais = data.getString("country");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessageWear(SEND_CITY_PATH, city);
                            sendMessageWear(SEND_CODE_PATH, code);
                            sendMessageWear(SEND_TEMP_PATH, temp + Celsius);

                            AsyncConnection(mApiImg + city + " " + pais);
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

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessageWear(SEND_UPDATE_PATH, temp + Celsius);
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

    public void AsyncConnection(String urlConnection) {

        mApiCall.get(urlConnection, null, new AsyncHttpResponseHandler() {

            final ArrayList arraylist = new ArrayList<>();

            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(int i, Header[] headers, final byte[] response) {

                try {

                    JSONObject jsonImages = new JSONObject(Utils.decodeUTF8(response));
                    final Integer status = jsonImages.getInt("responseStatus");

                    if (status == 403) {
                        String details = jsonImages.getString("responseDetails");
                    }

                    if (jsonImages.getJSONObject("responseData") != null) {
                        jsonResponse = jsonImages.getJSONObject("responseData");
                    }

                    if (status == 200) {
                        JSONArray jsonResults = jsonResponse.getJSONArray("results");

                        for (int imgs = 0; imgs < jsonResults.length(); imgs++) {

                            HashMap<String, String> images = new HashMap<>();

                            JSONObject imagesResult = jsonResults.getJSONObject(imgs);

                            String url = imagesResult.getString("url");

                            images.put("images", url);

                            arraylist.add(images);

                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int ran = Utils.randInt(0, 7);
                                data = arraylist;
                                result = data.get(ran);
                                String img = result.get("images");

                                Glide.with(ListenerService.this).load(img).asBitmap().override(800, 800).
                                        into(new SimpleTarget() {
                                            @Override
                                            public void onResourceReady(Object resource, GlideAnimation glideAnimation) {
                                                final Bitmap finalBitmap = (Bitmap) resource;

                                                sendPhoto(Utils.toAsset(finalBitmap));
                                            }
                                        });
                            }
                        });
                    }

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
