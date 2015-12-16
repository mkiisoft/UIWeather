package com.mkiisoft.uiweather.communication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.mkiisoft.uiweather.MainActivity;
import com.mkiisoft.uiweather.utils.KeySaver;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Listens to DataItems and Messages from the local node.
 */
public class ListenerService extends WearableListenerService {

    private final String TAG = "ListenerService";

    private final String START_ACTIVITY_PATH     = "/start-activity";
    private final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
    public  final String COUNT_PATH              = "/count";
    private final String SEND_UPDATE_PATH        = "/send-update";
    private final String SEND_UPDATE_CODE        = "/send-update-code";

    private final String SEND_ANALOG_PATH = "/send-analog";
    private final String SEND_CTOF_PATH   = "/send-fahrenheit";

    private final String SEND_WOEID_PATH         = "/send-woeid";


    GoogleApiClient mGoogleApiClient;

    Handler handler;

    Context context;

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
                Intent intent = new Intent( this, MainActivity.class );
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( intent );
            } else {
                super.onMessageReceived( messageEvent );
            }
        }

        if (messageEvent.getPath().equalsIgnoreCase(SEND_WOEID_PATH)){
            KeySaver.saveShare(getApplicationContext(), "woeid", new String(messageEvent.getData()));

        }

        if (messageEvent.getPath().equalsIgnoreCase(SEND_ANALOG_PATH)){

            String isTrue = new String(messageEvent.getData());

            if(isTrue.contentEquals("true")){
                KeySaver.saveShare(getApplicationContext(), "analog", new String(messageEvent.getData()));
            } else if (isTrue.contentEquals("false")) {
                KeySaver.removeKey(getApplicationContext(), "analog");
            }


        }

        if (messageEvent.getPath().equalsIgnoreCase(SEND_CTOF_PATH)){

            String isTrue = new String(messageEvent.getData());

            if(isTrue.contentEquals("true")){
                KeySaver.saveShare(getApplicationContext(), "is_fahrenheit", new String(messageEvent.getData()));
            } else if (isTrue.contentEquals("false")) {
                KeySaver.removeKey(getApplicationContext(), "is_fahrenheit");
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

    public void sendMessageWear(final String path, final String text){
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, text.getBytes())
                            .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                    Log.d("LOGUEO", "SendMessageStatus: " + sendMessageResult.getStatus());
                                }
                            });
                }
            }
        }).start();

    }

}
