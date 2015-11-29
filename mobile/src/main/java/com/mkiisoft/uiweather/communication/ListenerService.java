package com.mkiisoft.uiweather.communication;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.mkiisoft.uiweather.WeatherActivity;
import com.mkiisoft.uiweather.utils.KeySaver;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Listens to DataItems and Messages from the local node.
 */
public class ListenerService extends WearableListenerService {

    private static final String TAG = "ListenerService";

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    public static final String COUNT_PATH = "/count";

    public static final String GESTURE_START_POINT  = "/gesture_start_point";


    GoogleApiClient mGoogleApiClient;

    Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        handler = new Handler();

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
//        LOGD(TAG, "onDataChanged: " + dataEvents);
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        if(!mGoogleApiClient.isConnected()) {
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

        if (!KeySaver.isExist(getApplicationContext(), "start")){
            if( messageEvent.getPath().equalsIgnoreCase( START_ACTIVITY_PATH ) ) {
                Intent intent = new Intent( this, WeatherActivity.class );
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( intent );
            }
            else if (messageEvent.getPath().equalsIgnoreCase(GESTURE_START_POINT))
            {
                Log.e("aca que es", new String(messageEvent.getData()));

            } else {
                super.onMessageReceived( messageEvent );
            }
        }

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

}
