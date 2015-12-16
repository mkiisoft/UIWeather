package com.mkiisoft.uiweather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.AnalogClock;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.mkiisoft.uiweather.utils.Analog24HClock;
import com.mkiisoft.uiweather.utils.DateOverlay;
import com.mkiisoft.uiweather.utils.KeySaver;
import com.mkiisoft.uiweather.utils.SunPositionOverlay;
import com.mkiisoft.uiweather.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class UIWeatherWatchFace extends CanvasWatchFaceService {

    private static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    private static final int MSG_UPDATE_TIME = 0;

    private View myLayout;
    private ImageView bgWatchFace, codeWatchFace;
    private TextView tempWatchFace, clockWatchFace;

    private GoogleApiClient mGoogleApiClient;

    protected long mWeatherInfoReceivedTime;
    protected long mWeatherInfoRequiredTime;
    //10000 for testing
    protected int mRequireInterval = 14400000;

    protected Resources mResources;
    protected AssetManager mAsserts;

    private final String SEND_UPDATE_PATH = "/send-update";
    private final String SEND_UPDATE_CODE = "/send-update-code";

    private final String SEND_ANALOG_PATH = "/send-analog";
    private final String SEND_CTOF_PATH   = "/send-fahrenheit";

    private boolean isAnalog     = false;
    private boolean isFahrenheit = false;

    protected long UPDATE_RATE_MS;
    private String mWoeid = "468739";
    public static final float SUN_POSITION_OVERLAY_SCALE = 0.61345f;

    private String temp = "";
    private String code = "";;
    private Analog24HClock mClock;
    private SunPositionOverlay mSunPositionOverlay;
    private float celsiusf, fahrenheitf;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements
            MessageApi.MessageListener,
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            DataApi.DataListener, NodeApi.NodeListener {

        @Override
        public void onConnected(Bundle bundle) {

            Wearable.MessageApi.addListener(mGoogleApiClient, new MessageApi.MessageListener() {
                @Override
                public void onMessageReceived(MessageEvent messageEvent) {

                    if (messageEvent.getPath().equalsIgnoreCase(SEND_UPDATE_CODE)) {
                        code = new String(messageEvent.getData());
                    }

                    if (messageEvent.getPath().equalsIgnoreCase(SEND_UPDATE_PATH)) {
                        String sub = new String(messageEvent.getData());

                        temp = sub.substring(0, sub.length() - 2);

                        if(KeySaver.isExist(UIWeatherWatchFace.this, "is_fahrenheit")){
                            temp = String.valueOf(Utils.convertCelciusToFahrenheit(Float.parseFloat(temp))) + "°F";
                        } else {
                            temp = temp + "°C";
                        }
                    }

                    if (messageEvent.getPath().equalsIgnoreCase(SEND_ANALOG_PATH)) {
                        String isTrue = new String(messageEvent.getData());

                        if(isTrue.contentEquals("true")){
                            isAnalog = true;
                        } else  {
                            isAnalog = false;
                        }
                    }

                    if (messageEvent.getPath().equalsIgnoreCase(SEND_CTOF_PATH)) {
                        String isTrue = new String(messageEvent.getData());

                        if(isTrue.contentEquals("true")){

                            if(temp.length() > 2) {

                                temp = temp.substring(0, temp.length() - 2);
                                temp = String.valueOf(Utils.convertCelciusToFahrenheit(Float.parseFloat(temp))) + "°F";
                            }

                        } else if (isTrue.contentEquals("false")) {

                            if(temp.length() > 2) {

                                temp = temp.substring(0, temp.length() - 2);
                                temp = String.valueOf(Utils.convertFahrenheitToCelcius(Float.parseFloat(temp))) + "°C";

                            }
                        }
                    }
                }
            });

            Wearable.NodeApi.addListener(mGoogleApiClient, new NodeApi.NodeListener() {
                @Override
                public void onPeerConnected(Node node) {

                }

                @Override
                public void onPeerDisconnected(Node node) {

                }
            });


            Wearable.DataApi.addListener(mGoogleApiClient, new DataApi.DataListener() {
                @Override
                public void onDataChanged(DataEventBuffer dataEvents) {
                }
            });
            requireWeatherInfo();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            for (int i = 0; i < dataEvents.getCount(); i++) {
                DataEvent event = dataEvents.get(i);
                DataMap dataMap = DataMap.fromByteArray(event.getDataItem().getData());
            }
        }

        @Override
        public void onPeerConnected(Node node) {
            requireWeatherInfo();
        }

        @Override
        public void onPeerDisconnected(Node node) {
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        @Override
        public void onMessageReceived(MessageEvent messageEvent) {

        }

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();

                mClock.setTimezone(TimeZone.getTimeZone(intent.getStringExtra("time-zone")));
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        boolean mAmbient;

        Time mTime;

        boolean mLowBitAmbient;

        private int specW, specH;
        private View myLayout;
        private final Point displaySize = new Point();

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mGoogleApiClient = new GoogleApiClient.Builder(UIWeatherWatchFace.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            setWatchFaceStyle(new WatchFaceStyle.Builder(UIWeatherWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            mResources = UIWeatherWatchFace.this.getResources();
            mAsserts   = UIWeatherWatchFace.this.getAssets();

            final Typeface font = Typeface.createFromAsset(getAssets(), "thin.otf");
            final Typeface thin = Typeface.createFromAsset(getAssets(), "ultra.otf");

            specW = View.MeasureSpec.makeMeasureSpec(displaySize.x,
                    View.MeasureSpec.EXACTLY);
            specH = View.MeasureSpec.makeMeasureSpec(displaySize.y,
                    View.MeasureSpec.EXACTLY);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myLayout = inflater.inflate(R.layout.watch_face, null);

            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            display.getSize(displaySize);

            bgWatchFace    = (ImageView) myLayout.findViewById(R.id.bg_watchface);
            codeWatchFace  = (ImageView) myLayout.findViewById(R.id.code_watchface);
            tempWatchFace  = (TextView)  myLayout.findViewById(R.id.temp_watchface);
            clockWatchFace = (TextView)  myLayout.findViewById(R.id.clock_watchface);

            mClock = new Analog24HClock(UIWeatherWatchFace.this);
            mSunPositionOverlay = new SunPositionOverlay(UIWeatherWatchFace.this);

            initializeClock();

            clockWatchFace.setTypeface(font);

            mTime = new Time();

            mWeatherInfoRequiredTime = System.currentTimeMillis() - (DateUtils.SECOND_IN_MILLIS * 58);
            mGoogleApiClient.connect();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();
                registerReceiver();
                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
                mClock.setTimezone(TimeZone.getDefault());

            } else {

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.MessageApi.removeListener(mGoogleApiClient, this);
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    Wearable.NodeApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }

                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            UIWeatherWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            UIWeatherWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            specW = View.MeasureSpec.makeMeasureSpec(displaySize.x, View.MeasureSpec.EXACTLY);
            specH = View.MeasureSpec.makeMeasureSpec(displaySize.y, View.MeasureSpec.EXACTLY);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
            requireWeatherInfo();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {

                }
                invalidate();
            }

            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            canvas.save();

            myLayout.measure(specW, specH);
            myLayout.layout(0, 0, myLayout.getMeasuredWidth(), myLayout.getMeasuredHeight());
            BitmapDrawable bgDrawable = new BitmapDrawable(getResources(), Utils.StringToBitMap(KeySaver.getStringSavedShare(UIWeatherWatchFace.this, "bitmapBg")));
            bgWatchFace.setImageDrawable(bgDrawable);

            tempWatchFace.setText(temp);

            myLayout.draw(canvas);

            mTime.setToNow();
            String text = String.format("%02d:%02d", mTime.hour, mTime.minute);

            clockWatchFace.setText(text);

            mClock.setTime(System.currentTimeMillis());

            if(isAnalog){
                mClock.measure(specW, specH);
                mClock.layout(0, 0, myLayout.getMeasuredWidth(), myLayout.getMeasuredHeight());
                mClock.draw(canvas);
                clockWatchFace.setVisibility(View.GONE);
            } else {
                mClock.measure(0, 0);
                mClock.layout(0, 0, 0, 0);
                mClock.draw(canvas);
                clockWatchFace.setVisibility(View.VISIBLE);
            }

            if(KeySaver.isExist(UIWeatherWatchFace.this, "analog")) {
                mClock.measure(specW, specH);
                mClock.layout(0, 0, myLayout.getMeasuredWidth(), myLayout.getMeasuredHeight());
                mClock.draw(canvas);
                clockWatchFace.setVisibility(View.GONE);
            } else {
                mClock.measure(0, 0);
                mClock.layout(0, 0, 0, 0);
                mClock.draw(canvas);
                clockWatchFace.setVisibility(View.VISIBLE);
            }

            if(!code.isEmpty()){
                conditionCode(Integer.parseInt(code));
            }

            canvas.restore();

        }

        private void initializeClock() {
            mClock.clearDialOverlays();
            mSunPositionOverlay.setScale(SUN_POSITION_OVERLAY_SCALE);
            mSunPositionOverlay.setShadeAlpha(60);
            mClock.addDialOverlay(mSunPositionOverlay);

            final DateOverlay dateOverlay = new DateOverlay(0.1875f, -0.1875f, 0.0625f);
            mClock.addDialOverlay(dateOverlay);
        }

        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        protected final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();

                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = 1000 - (timeMs % 1000);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                            requireWeatherInfo();
                        }
                        break;
                }
            }
        };

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<UIWeatherWatchFace.Engine> mWeakReference;

        public EngineHandler(UIWeatherWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            UIWeatherWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    protected void requireWeatherInfo() {
        if (!mGoogleApiClient.isConnected())
            return;

        long timeMs = System.currentTimeMillis();

        // The weather info is still up to date.
        if ((timeMs - mWeatherInfoReceivedTime) <= mRequireInterval)
            return;

        // Try once in a min.
        if ((timeMs - mWeatherInfoRequiredTime) <= DateUtils.MINUTE_IN_MILLIS)
            return;

        mWeatherInfoRequiredTime = timeMs;
        if (KeySaver.isExist(getApplicationContext(), "woeid")) {
            sendMessageWear(SEND_UPDATE_PATH, KeySaver.getStringSavedShare(getApplicationContext(), "woeid"));
        } else {
            sendMessageWear(SEND_UPDATE_PATH, mWoeid);
        }
    }

    public void sendMessageWear(final String path, final String text) {
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

    private void conditionCode(int code) {

        if (code >= 26 && code <= 30) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.cloudview));
        } else if (code >= 20 && code <= 21) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.fogview));
        } else if (code == 32) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.sunview));
        } else if (code >= 33 && code <= 34) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.cloudsunview));
        } else if (code >= 37 && code <= 39) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.thunderview));
        } else if (code == 45) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.thunderview));
        } else if (code == 4) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.thunderview));
        } else if (code == 24) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.windview));
        } else if (code >= 9 && code <= 12) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.rainview));
        } else if (code == 31) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.moonview));
        } else if ((code >= 5 && code <= 8) || (code >= 13 && code <= 19)) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.cloudsnowview));
        } else if (code == 46) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.cloudsnowview));
        } else if (code == 3200) {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.sunview));
        } else {
            codeWatchFace.setImageDrawable(getDrawable(R.drawable.sunview));
        }
    }
}
