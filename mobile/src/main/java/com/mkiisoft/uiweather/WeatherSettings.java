package com.mkiisoft.uiweather;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.mkiisoft.uiweather.utils.KeySaver;

/**
 * Created by mariano zorrilla on 12/13/15.
 */
public class WeatherSettings extends AppCompatActivity implements DataApi.DataListener,
        MessageApi.MessageListener,
        NodeApi.NodeListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Wear Connection
    private final int REQUEST_RESOLVE_ERROR = 1000;
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    // Paths to Wear Service
    private final String SEND_ANALOG_PATH = "/send-analog";
    private final String SEND_CTOF_PATH   = "/send-fahrenheit";

    // Toggles
    private ToggleButton isFahrenheit, isAnalog;
    private String TRUE  = "true";
    private String FALSE = "false";

    // Views
    private ImageView mWearIcon;
    private TextView  mWearConnect;

    // Intent
    private Intent  weatherIntent;
    private boolean getWearState;

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_settings);

        weatherIntent = getIntent();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("SETTINGS");
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            setStatusBarTranslucent(true);
            ActionBar mAction = getSupportActionBar();
            if (mAction != null) {
                mAction.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mAction.setTitle("SETTINGS");
                mAction.setDisplayShowHomeEnabled(true);
                mAction.setDisplayHomeAsUpEnabled(true);
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mWearIcon    = (ImageView) findViewById(R.id.wear_icon_settings);
        mWearConnect = (TextView)  findViewById(R.id.wear_connected_settings);

        isFahrenheit = (ToggleButton) findViewById(R.id.convert_temp_toggle);
        isAnalog     = (ToggleButton) findViewById(R.id.analog_toggle);

        getWearState = weatherIntent.getBooleanExtra("wear", false);

        if (getWearState){
            mWearIcon.setImageDrawable(getResources().getDrawable(R.drawable.watch));
            mWearConnect.setText(weatherIntent.getStringExtra("model") + " " + getString(R.string.wear_connected));
            isAnalog.setEnabled(true);
            isAnalog.setAlpha(1.0f);
        } else {
            mWearIcon.setImageDrawable(getResources().getDrawable(R.drawable.watchnot));
            mWearConnect.setText(getString(R.string.wear_connected_not));
            isAnalog.setEnabled(false);
            isAnalog.setAlpha(0.5f);
        }

        if(KeySaver.isExist(WeatherSettings.this, "is_fahrenheit")){
            isFahrenheit.setChecked(true);
        }else{
            isFahrenheit.setChecked(false);
        }

        if(KeySaver.isExist(WeatherSettings.this, "is_analog")){
            isAnalog.setChecked(true);
        }else{
            isAnalog.setChecked(false);
        }

        isFahrenheit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isCheckedFahrenheit) {
                if (isCheckedFahrenheit) {
                    if (!KeySaver.isExist(WeatherSettings.this, "is_fahrenheit")) {
                        KeySaver.saveShare(WeatherSettings.this, "is_fahrenheit", true);
                        sendMessageWear(SEND_CTOF_PATH, TRUE);
                    }
                } else {
                    if (KeySaver.isExist(WeatherSettings.this, "is_fahrenheit")) {
                        KeySaver.removeKey(WeatherSettings.this, "is_fahrenheit");
                        sendMessageWear(SEND_CTOF_PATH, FALSE);
                    }
                }
            }
        });

        isAnalog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isCheckedAnalog) {
                if (isCheckedAnalog) {
                    if (!KeySaver.isExist(WeatherSettings.this, "is_analog")) {
                        KeySaver.saveShare(WeatherSettings.this, "is_analog", true);
                        sendMessageWear(SEND_ANALOG_PATH, TRUE);
                    }
                } else {
                    if (KeySaver.isExist(WeatherSettings.this, "is_analog")) {
                        KeySaver.removeKey(WeatherSettings.this, "is_analog");
                        sendMessageWear(SEND_ANALOG_PATH, FALSE);
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed(){
        Intent returnIntent = new Intent();
        setResult(WeatherSettings.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
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
        mResolvingError = false;
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }

    @Override
    public void onPeerConnected(Node node) {

    }

    @Override
    public void onPeerDisconnected(Node node) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError) {
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(WeatherSettings.RESULT_OK, returnIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
