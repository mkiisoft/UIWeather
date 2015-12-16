package com.mkiisoft.uiweather;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.mkiisoft.uiweather.adapter.ListViewAdapter;
import com.mkiisoft.uiweather.utils.AutoFitTextView;
import com.mkiisoft.uiweather.utils.CubicBezierInterpolator;
import com.mkiisoft.uiweather.utils.DeviceName;
import com.mkiisoft.uiweather.utils.KeySaver;
import com.mkiisoft.uiweather.utils.OnSwipeTouchListener;
import com.mkiisoft.uiweather.utils.ResizeAnimation;
import com.mkiisoft.uiweather.utils.SquareImageView;
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
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import io.codetail.animation.SupportAnimator;

/**
 * Created by mariano-zorrilla on 17/11/15.
 */

public class WeatherActivity extends AppCompatActivity implements DataApi.DataListener,
        MessageApi.MessageListener,
        NodeApi.NodeListener,
        ConnectionCallbacks,
        OnConnectionFailedListener {

    // API Response
    private JSONObject jsonResponse;

    // Array list from APIs
    final ArrayList arraylist = new ArrayList<>();
    final ArrayList arrayhistory = new ArrayList<>(11);
    final ArrayList arraylistForecast = new ArrayList<>(5);
    final ArrayList arraylistPhotos = new ArrayList<>(10);

    // HashMap to get Key/Value
    HashMap<String, String> result = new HashMap<>();
    HashMap<String, String> resultHistory = new HashMap<>();

    // ArrayList to get position
    ArrayList<HashMap<String, String>> mData;
    ArrayList<HashMap<String, String>> dataHistory;

    // API Strings URIs
    private String mApiCatchQuery, mApiCatchWoeid;

    // Views
    private View mLineColor;
    private ImageView toSettings;
    private RelativeLayout mMainBg;
    private LinearLayout mWeatherIcon;
    private SquareImageView mWeatherImage;
    private TextView mTitleCity, mTextType;
    private AutoFitTextView mTextTemp;

    // FAB states
    public final int FAB_STATE_COLLAPSED = 0;
    public final int FAB_STATE_EXPANDED = 1;

    public int FAB_CURRENT_STATE = FAB_STATE_COLLAPSED;

    ImageButton mFab, mCollapseFabButton;
    RelativeLayout mExpandedView;
    EditText edit;
    ListView mHistoryList;

    // Weather Views
    private CloudView cloudView;
    private SunView sunView;
    private WindView windView;
    private MoonView moonView;
    private CloudSunView cloudSunView;
    private CloudThunderView thunderView;
    private CloudRainView rainView;
    private CloudFogView fogView;
    private CloudMoonView cloudMoonView;
    private CloudSnowView cloudSnowView;

    // Temperature settings
    String Fahrenheit = "\u00B0F";
    String Celsius = "\u00B0C";
    int celsiusTemp;

    private String code, temp;
    private String mSearchBox;
    private String mNextColor = "#23282E";

    private AVLoadingIndicatorView mProgress;

    // swipe views
    private RelativeLayout mMainCity, mMainTemp, mChildTemp;
    private FrameLayout mMainBtn;
    private ListView mListForecast;

    // Adapter
    private ListViewAdapter mAdapter;
    private SimpleAdapter mAdapterHistory;

    // Access to Utils
    Utils.ApiCall mApiCall;
    Utils.ApiCallSync mApiCatch;
    private boolean connready;

    // Custom views setup
    private LinearLayout.LayoutParams param;
    final String white = "#00FFFFFF";
    final int match = ViewGroup.LayoutParams.MATCH_PARENT;

    // Variables
    private boolean isFistTime = true;
    private boolean fromHistory = false;
    private String queryURL = "New York City";

    // Catch Weather Cities
    private String mName, mCode, mCountry;
    private int mWoeid;
    private int mCount = 0;

    // Wear Connection
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;
    private boolean mIsConnected = false;

    //  TAG
    private final String TAG = "Wear";

    // Wear device name
    private String wearModel = "Android Wear";

    /**
     * Request code for launching the Intent to resolve Google Play services errors.
     */
    private final int REQUEST_RESOLVE_ERROR = 1000;

    private final String START_ACTIVITY_PATH = "/start-activity";
    private final String SEND_TEMP_PATH = "/send-temp";
    private final String SEND_CITY_PATH = "/send-city";
    private final String SEND_CODE_PATH = "/send-code";
    private final String SEND_BITMAP_PATH = "/send-bitmap";
    private final String SEND_MODEL_PATH = "/send-model";
    private final String SEND_WOEID_PATH = "/send-woeid";

    private final int REQUEST_SETTINGS = 1001;

    private LinearLayout mWearLayout;
    private TextView mWearModel;

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
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_weather2);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        } else {
            setStatusBarTranslucent(true);
            ActionBar mAction = getSupportActionBar();
            if (mAction != null) {
                mAction.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mAction.setTitle("");
            }
        }

        if (KeySaver.isExist(WeatherActivity.this, "query-city")) {
            queryURL = KeySaver.getStringSavedShare(WeatherActivity.this, "query-city");
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiCall = new Utils.ApiCall();
        mApiCatch = new Utils.ApiCallSync();

        param = new LinearLayout.LayoutParams(match, match);
        param.gravity = Gravity.START;

        initCustomWeatherViews();
        decorateCustomWeatherViews();

        String deviceName = DeviceName.getDeviceName();

        mProgress = (AVLoadingIndicatorView) findViewById(R.id.progress_balls);

        mMainBg = (RelativeLayout) findViewById(R.id.main_bg);

        // animated views swipe up and down
        mMainCity  = (RelativeLayout) findViewById(R.id.city_main);
        mMainTemp  = (RelativeLayout) findViewById(R.id.temp_main);
        mMainBtn   = (FrameLayout)    findViewById(R.id.btn_main);
        mChildTemp = (RelativeLayout) findViewById(R.id.temp_child);

        toSettings = (ImageView) findViewById(R.id.to_settings);
        toSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(WeatherActivity.this, WeatherSettings.class);
                i.putExtra("wear", mIsConnected);
                i.putExtra("model", wearModel);
                startActivityForResult(i, REQUEST_SETTINGS);
            }
        });

        mChildTemp.setAlpha(0);
        mChildTemp.setEnabled(false);
        mChildTemp.setTranslationY(+mChildTemp.getHeight());

        mListForecast = (ListView) findViewById(R.id.list_forecast);
        mListForecast.setTranslationY(+mListForecast.getHeight());

        mAdapter = new ListViewAdapter(WeatherActivity.this, arraylistForecast);
        mListForecast.setAdapter(mAdapter);

        mListForecast.setOnTouchListener(new OnSwipeTouchListener(this) {

            public void onSwipeTop() {
                if (!mChildTemp.isEnabled()) {
                    swipeUp(true);
                }
            }

            public void onSwipeRight() {
            }

            public void onSwipeLeft() {
            }

            public void onSwipeBottom() {
                if (mChildTemp.isEnabled()) {
                    swipeDown(true);
                }
            }

        });

        mMainBg.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mMainBg.getRootView().getHeight() - mMainBg.getHeight();
                if (heightDiff > 50) {
                    WeatherActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }
            }
        });
        mMainBg.setOnTouchListener(new OnSwipeTouchListener(this) {

            public void onSwipeTop() {
                if (!mChildTemp.isEnabled()) {
                    swipeUp(true);
                }
            }

            public void onSwipeRight() {
                int randoms = Utils.randInt(0, 7);
                result = mData.get(randoms);
                String images = result.get("url_big");
                Glide.with(WeatherActivity.this).load(images).override(800, 800).into(mWeatherImage);
            }

            public void onSwipeLeft() {
                int randoms = Utils.randInt(0, 7);
                result = mData.get(randoms);
                String images = result.get("url_big");
                Glide.with(WeatherActivity.this).load(images).override(800, 800).into(mWeatherImage);
            }

            public void onSwipeBottom() {

                if (mChildTemp.isEnabled()) {
                    swipeDown(true);
                }
            }

        });

        Typeface font = Typeface.createFromAsset(getAssets(), "thin.otf");
        Typeface thin = Typeface.createFromAsset(getAssets(), "ultra.otf");

        mApiCatchQuery = getResources().getString(R.string.api_url_query);
        mApiCatchWoeid = getResources().getString(R.string.api_url_woeid);

        mSearchBox = getResources().getString(R.string.searchbox);

        mWeatherImage = (SquareImageView) findViewById(R.id.weather_main);
        mTitleCity = (TextView) findViewById(R.id.title_city);
        mLineColor = findViewById(R.id.line_color);
        mTitleCity.setTypeface(font);

        mFab = (ImageButton) findViewById(R.id.fab);
        mExpandedView = (RelativeLayout) findViewById(R.id.expanded_view);
        mCollapseFabButton = (ImageButton) findViewById(R.id.act_collapse);

        mHistoryList = (ListView) findViewById(R.id.list_history);
        mHistoryList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0));
        mAdapterHistory = new SimpleAdapter(this, arrayhistory, R.layout.history_item, new String[]{"history"},
                new int[]{R.id.item_text_history});
        mHistoryList.setAdapter(mAdapterHistory);
        mHistoryList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                arrayhistory.remove(position);
                mAdapterHistory.notifyDataSetChanged();
                return true;
            }
        });

        mHistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fromHistory = true;
                dataHistory = arrayhistory;
                resultHistory = dataHistory.get(position);
                queryURL = resultHistory.get("history");
                edit.setText(queryURL);
                mCollapseFabButton.performClick();
            }
        });

        mWearLayout = (LinearLayout) findViewById(R.id.wear_connect_layout);
        mWearModel = (TextView) findViewById(R.id.wear_model_text);

        mWearModel.setTypeface(thin);

        edit = (EditText) findViewById(R.id.search_box);

        edit.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && edit.getText().toString().trim().length() > 0) {
                    if (v != null) {
                        mCollapseFabButton.performClick();
                    }
                    return true;
                } else if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && edit.getText().toString().trim().length() == 0) {
                    Toast.makeText(WeatherActivity.this, mSearchBox, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        mWeatherIcon = (LinearLayout) findViewById(R.id.weather_img);

        mTextTemp = (AutoFitTextView) findViewById(R.id.text_temp);
        mTextType = (TextView) findViewById(R.id.text_type);
        mTextTemp.setTypeface(thin);
        mTextType.setTypeface(font);

        connready = Utils.testConection(this);
        if (connready) {
            SyncAsyncConenection(mApiCatchQuery + queryURL);
        } else {
            Toast.makeText(this, getResources().getString(R.string.connection), Toast.LENGTH_SHORT).show();
        }

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (arrayhistory.size() >= 1) {
                    mHistoryList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }

                edit.setText("");
                revealView(mExpandedView);
                FAB_CURRENT_STATE = FAB_STATE_EXPANDED;
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
                WeatherActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFab.setVisibility(View.GONE);
                    }
                }, 50);
                mCollapseFabButton.animate().rotationBy(220).setDuration(250).start();


            }
        });

        mCollapseFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (edit.getText().toString().trim().length() == 0) {
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                } else {
                    queryURL = edit.getText().toString();
                    HashMap<String, String> history = new HashMap<>();
                    history.put("history", edit.getText().toString());
                    if (!fromHistory) {
                        arrayhistory.add(0, history);
                    }
                    fromHistory = false;
                    if (arrayhistory.size() > 10) {
                        arrayhistory.remove(10);
                    }
                    KeySaver.removeKey(WeatherActivity.this, "Fahrenheit");
                    mAdapterHistory.notifyDataSetChanged();
                    arraylistForecast.clear();
                    mAdapter.notifyDataSetChanged();
                    if (mData != null) {
                        mData.clear();
                    } else if (result != null) {
                        result.clear();
                    } else if (arraylist != null) {
                        arraylist.clear();
                    }

                    initViews();
                    searchReset();

                    if (view != null) {
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                mCount = 0;

                                if (connready) {
                                    SyncAsyncConenection(mApiCatchQuery + queryURL);
                                } else {
                                    Toast.makeText(WeatherActivity.this, getResources().getString(R.string.connection), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }

                hideView(mExpandedView);

                FAB_CURRENT_STATE = FAB_STATE_COLLAPSED;

                mCollapseFabButton.animate().rotationBy(-220).setDuration(200).start();
            }
        });

        mTextType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoopAnimBottom(mTextType);
                LoopAnimTop(mTextTemp);
            }
        });

    }

    private class WearAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            List<Node> connectedNodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();
            if (connectedNodes.size() > 0) {
                mIsConnected = true;
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String post) {
            if (mIsConnected) {
                showWear();
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    public void SyncAsyncConenection(String urlConnection) {

        mApiCall.get(urlConnection, null, new JsonHttpResponseHandler() {
            ArrayAdapter<String> arrayAdapter;

            @Override
            public void onStart() {
                mChildTemp.setEnabled(true);
                mProgress.setVisibility(View.VISIBLE);
                mProgress.animate().alpha(1).setStartDelay(500).setDuration(400).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.setAlpha(1);
                    }
                });
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    JSONObject getResponse = new JSONObject("" + response);
                    JSONObject getPlaces = getResponse.getJSONObject("places");
                    final int getTotal = getPlaces.getInt("total");

                    if (getTotal == 0) {
                        Toast.makeText(WeatherActivity.this, getResources().getString(R.string.no_results), Toast.LENGTH_SHORT).show();
                    }

                    arrayAdapter = new ArrayAdapter<>(WeatherActivity.this,
                            android.R.layout.select_dialog_singlechoice);

                    JSONArray getPlace = getPlaces.getJSONArray("place");
                    if (mCount == 2) {
                        JSONObject placeObj = getPlace.getJSONObject(0);
                        JSONObject attrsObj = placeObj.getJSONObject("country attrs");
                        mName = placeObj.getString("name");
                        mCountry = placeObj.getString("country");
                        mCode = attrsObj.getString("code");
                        mWoeid = attrsObj.getInt("woeid");

                    } else {
                        for (int i = 0; i < getPlace.length(); i++) {

                            JSONObject placeObj = getPlace.getJSONObject(i);
                            JSONObject attrsObj = placeObj.getJSONObject("country attrs");
                            mName = placeObj.getString("name");
                            mCountry = placeObj.getString("country");
                            mCode = attrsObj.getString("code");
                            mWoeid = placeObj.getInt("woeid");

                            arrayAdapter.add(mName + ", " + mCountry);

                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (getTotal == 1 || mCount == 2) {
                                mCount = 0;
                                sendMessageWear(SEND_WOEID_PATH, String.valueOf(mWoeid));
                                AsyncWeather(mApiCatchWoeid + mWoeid);
                                KeySaver.saveShare(WeatherActivity.this, "query-city", queryURL);
                            } else {
                                mCount++;
                                AlertDialog.Builder builderSingle = new AlertDialog.Builder(WeatherActivity.this);
                                builderSingle.setTitle(getResources().getString(R.string.select_location));

                                builderSingle.setNegativeButton(
                                        getResources().getString(R.string.cancel_simple),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });

                                builderSingle.setAdapter(
                                        arrayAdapter,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                queryURL = arrayAdapter.getItem(which);
                                                SyncAsyncConenection(mApiCatchQuery + queryURL);
                                            }
                                        });
                                builderSingle.show();
                            }

                        }
                    });

                } catch (JSONException e) {
                    Toast.makeText(WeatherActivity.this, getResources().getString(R.string.no_results), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {

            }

        });

    }

    public void AsyncWeather(String urlConnection) {
        mApiCall.get(urlConnection, null, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {

                mProgress.animate().alpha(0).setDuration(400).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mProgress.setAlpha(0);
                        mProgress.setVisibility(View.GONE);
                    }
                });

            }

            @Override
            public void onSuccess(int i, Header[] headers, final byte[] response) {
                try {

                    int forecastSize = Utils.determineScreenDensity(WeatherActivity.this);

                    JSONObject jsonWeather = new JSONObject(Utils.decodeUTF8(response));

                    JSONObject queryObject = jsonWeather.getJSONObject("query");
                    final JSONObject data = queryObject.getJSONObject("datos");
                    temp = data.getString("temp");
                    code = data.getString("code");

                    JSONArray forecast = jsonWeather.getJSONArray("forecast");
                    final JSONArray photos = jsonWeather.getJSONArray("fotos");

                    if(photos.length() > 0) {

                        for (int p = 0; p < photos.length(); p++) {

                            HashMap<String, String> photosHash = new HashMap<>();
                            JSONObject photosObj = photos.getJSONObject(p);
                            photosHash.put("url_big", photosObj.getString("url_big"));
                            photosHash.put("title", photosObj.getString("title"));

                            arraylistPhotos.add(photosHash);

                        }

                    } else {

                        for (int p = 0; p < 10; p++) {

                            HashMap<String, String> photosHash = new HashMap<>();
                            photosHash.put("url_big", noImages[p]);

                            arraylistPhotos.add(photosHash);

                        }
                    }

                    for (int f = 0; f < forecastSize; f++) {

                        HashMap<String, String> forecastHash = new HashMap<>();
                        JSONObject forecastObj = forecast.getJSONObject(f);

                        forecastHash.put("code", forecastObj.getString("code"));
                        forecastHash.put("date", forecastObj.getString("date"));
                        forecastHash.put("high", forecastObj.getString("high"));
                        forecastHash.put("low", forecastObj.getString("low"));

                        arraylistForecast.add(forecastHash);

                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            int ran = Utils.randInt(0, 9);
                            mData = arraylistPhotos;
                            result = mData.get(ran);
                            String img = result.get("url_big");
                            Glide.with(WeatherActivity.this).load(photos.length() > 0 ? img : Uri.parse(img)).asBitmap().override(800, 800).
                                    into(new SimpleTarget() {
                                        @Override
                                        public void onResourceReady(Object resource, GlideAnimation glideAnimation) {

                                            mTitleCity.setTranslationY(-50);
                                            mTitleCity.setText(mName + " " + mCode);
                                            CharSequence text = mTitleCity.getText();
                                            float width = mTitleCity.getPaint().measureText(text, 0, text.length());
                                            int textWidth = Math.round(width);
                                            float percent = (float) textWidth / 130 * 100;
                                            int newWidth = (int) percent;
                                            final Bitmap finalBitmap = (Bitmap) resource;

                                            mWeatherImage.animate().alpha(0.2f).setDuration(400).withEndAction(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mWeatherImage.setAlpha(0.2f);
                                                    mWeatherImage.setImageBitmap(finalBitmap);
                                                    mWeatherImage.animate().alpha(1).setDuration(800).withEndAction(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mTitleCity.animate().alpha(1).translationY(0).setStartDelay(200).setDuration(600).setInterpolator(new DecelerateInterpolator());
                                                            if (!isFistTime) {
                                                                isFistTime = false;
                                                            }
                                                            sendMessageWear(SEND_WOEID_PATH, String.valueOf(mWoeid));
                                                        }
                                                    });
                                                }
                                            });
                                            final ResizeAnimation resizeAnimation = new ResizeAnimation(mLineColor, newWidth);
                                            resizeAnimation.setDuration(600);
                                            resizeAnimation.setStartOffset(400);
                                            mLineColor.startAnimation(resizeAnimation);

                                            if (mIsConnected) {
                                                sendPhoto(Utils.toAsset(finalBitmap));
                                                sendMessageWear(SEND_CITY_PATH, mName + " " + mCode);
                                            }

                                            if(mTextType.getText().toString().contentEquals(Celsius) && KeySaver.isExist(WeatherActivity.this, "is_fahrenheit")){

                                                new Handler().postDelayed(new Runnable() {
                                                    public void run() {
                                                        mTextType.performClick();
                                                    }
                                                }, 500);

                                            }
                                        }

                                        @Override
                                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                            if (mData.size() > 0) {
                                                int randoms = Utils.randInt(0, 9);
                                                result = mData.get(randoms);
                                                String images = result.get("url_img");
                                                Glide.with(WeatherActivity.this).load(images).override(800, 800).into(mWeatherImage);
                                            }
                                        }
                                    });

                            int temps = Integer.parseInt(temp);
                            int codes = Integer.parseInt(code);

                            float celsiusf = ((temps - 32) * 5 / 9);
                            float fahrenheitf = ((temps * 9) / 5) + 32;
                            celsiusTemp = Math.round(fahrenheitf);

                            if (codes >= 26 && codes <= 30) {
                                mWeatherIcon.addView(cloudView);
                            } else if (codes >= 20 && codes <= 21) {
                                mWeatherIcon.addView(fogView);
                            } else if (codes == 32) {
                                mWeatherIcon.addView(sunView);
                            } else if (codes >= 33 && codes <= 34) {
                                mWeatherIcon.addView(cloudSunView);
                            } else if (codes >= 37 && codes <= 39) {
                                mWeatherIcon.addView(thunderView);
                            } else if (codes == 45) {
                                mWeatherIcon.addView(thunderView);
                            } else if (codes == 4) {
                                mWeatherIcon.addView(thunderView);
                            } else if (codes == 24) {
                                mWeatherIcon.addView(windView);
                            } else if (codes >= 9 && codes <= 12) {
                                mWeatherIcon.addView(rainView);
                            } else if (codes == 31) {
                                mWeatherIcon.addView(moonView);
                            } else if ((codes >= 5 && codes <= 8) || (codes >= 13 && codes <= 19)) {
                                mWeatherIcon.addView(cloudSnowView);
                            } else if (codes == 46) {
                                mWeatherIcon.addView(cloudSnowView);
                            } else if (codes == 3200) {
                                mWeatherIcon.addView(sunView);
                            } else {
                                mWeatherIcon.addView(sunView);
                            }

                            mWeatherIcon.setTranslationX(-mWeatherIcon.getWidth());
                            mWeatherIcon.animate().alpha(1f).translationX(0).setDuration(1000).setInterpolator(new DecelerateInterpolator());
                            Utils.animateTextView(0, Integer.parseInt(temp), mTextTemp);

                            sendMessageWear(SEND_TEMP_PATH, temp + Celsius);
                            sendMessageWear(SEND_CODE_PATH, code);

                            mTextTemp.setTranslationY(+mTextTemp.getHeight());
                            mTextType.setTranslationY(-mTextType.getHeight());

                            mTextTemp.animate().alpha(1).translationY(0).setDuration(1200).setInterpolator(new DecelerateInterpolator());
                            mTextType.animate().alpha(1).translationY(0).setDuration(1000).setInterpolator(new DecelerateInterpolator());

                            if (temps >= 10 && temps <= 18) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#031e2b"), 1500);
                                mNextColor = "#031e2b";
                            } else if (temps >= 0 && temps <= 9) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#062f42"), 1500);
                                mNextColor = "#062f42";
                            } else if (temps < 0) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#063d56"), 1500);
                                mNextColor = "#063d56";
                            } else if (temps >= 19 && temps <= 29) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#211501"), 1500);
                                mNextColor = "#211501";
                            } else if (temps >= 30 && temps <= 35) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#301f02"), 1500);
                                mNextColor = "#301f02";
                            } else if (temps >= 36) {
                                Utils.animateBetweenColors(mMainBg, Color.parseColor(mNextColor), Color.parseColor("#422a04"), 1500);
                                mNextColor = "#422a04";
                            }

                            mChildTemp.setEnabled(false);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_SETTINGS) {
            if(resultCode == WeatherActivity.RESULT_OK){
                if(mTextType.getText().toString().contentEquals(Celsius) && KeySaver.isExist(WeatherActivity.this, "is_fahrenheit")){
                    mTextType.performClick();
                } else  if (mTextType.getText().toString().contentEquals(Fahrenheit) && !KeySaver.isExist(WeatherActivity.this, "is_fahrenheit")) {
                    mTextType.performClick();
                }
            }
            if (resultCode == WeatherActivity.RESULT_CANCELED) {

            }
        }
    }

    public void initCustomWeatherViews() {
        sunView       = new SunView(WeatherActivity.this);
        windView      = new WindView(WeatherActivity.this);
        moonView      = new MoonView(WeatherActivity.this);
        cloudView     = new CloudView(WeatherActivity.this);
        thunderView   = new CloudThunderView(WeatherActivity.this);
        rainView      = new CloudRainView(WeatherActivity.this);
        fogView       = new CloudFogView(WeatherActivity.this);
        cloudSunView  = new CloudSunView(WeatherActivity.this);
        cloudMoonView = new CloudMoonView(WeatherActivity.this);
        cloudSnowView = new CloudSnowView(WeatherActivity.this);
    }

    public void decorateCustomWeatherViews() {

        cloudView.setBgColor(Color.parseColor(white));
        cloudView.setLayoutParams(param);

        sunView.setBgColor(Color.parseColor(white));
        sunView.setLayoutParams(param);

        windView.setBgColor(Color.parseColor(white));
        windView.setLayoutParams(param);

        moonView.setBgColor(Color.parseColor(white));
        moonView.setLayoutParams(param);

        fogView.setBgColor(Color.parseColor(white));
        fogView.setLayoutParams(param);

        rainView.setBgColor(Color.parseColor(white));
        rainView.setLayoutParams(param);

        thunderView.setBgColor(Color.parseColor(white));
        thunderView.setLayoutParams(param);

        cloudSunView.setBgColor(Color.parseColor(white));
        cloudSunView.setLayoutParams(param);

        cloudMoonView.setBgColor(Color.parseColor(white));
        cloudMoonView.setLayoutParams(param);

        cloudSnowView.setBgColor(Color.parseColor(white));
        cloudSnowView.setLayoutParams(param);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    public void revealView(View myView) {

        int cx = (mFab.getLeft() + mFab.getRight()) / 2;
        int cy = (mFab.getTop() + mFab.getBottom()) / 2;

        int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

        SupportAnimator anim = io.codetail.animation.ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
        anim.setDuration(300);

        myView.setVisibility(View.VISIBLE);

        slideView(edit);

        anim.start();


    }

    public void hideView(final View myView) {


        int cx = (mFab.getLeft() + mFab.getRight()) / 2;
        int cy = (mFab.getTop() + mFab.getBottom()) / 2;

        int initialRadius = myView.getWidth();

        SupportAnimator anim = io.codetail.animation.ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);
        anim.setDuration(300);
        anim.setInterpolator(getLinearOutSlowInInterpolator());

        anim.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                myView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mFab.setVisibility(View.VISIBLE);
            }
        }, 200);

        anim.start();

    }

    public void slideView(View view) {
        ObjectAnimator slide = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 112, 0);
        slide.setDuration(500);
        slide.setInterpolator(getLinearOutSlowInInterpolator());
        slide.start();
    }

    public Interpolator getLinearOutSlowInInterpolator() {
        return new CubicBezierInterpolator(0, 0, 0.2, 1);
    }

    public Interpolator getFastInSlowOutInterpolator() {
        return new CubicBezierInterpolator(0.4, 0, 0.2, 1);
    }

    public void LoopAnimBottom(final TextView view) {
        view.animate().alpha(0).translationY(+view.getHeight()).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.setAlpha(0);
                if (mTextType.getText().toString().contentEquals(Celsius)) {
                    view.setText(Fahrenheit);
                    mTextTemp.setText("" + celsiusTemp);
                    KeySaver.saveShare(WeatherActivity.this, "Fahrenheit", true);
                } else {
                    view.setText(Celsius);
                    mTextTemp.setText("" + temp);
                    KeySaver.removeKey(WeatherActivity.this, "Fahrenheit");
                }
                view.setTranslationY(-view.getHeight());
                view.animate().alpha(1).translationY(0).setDuration(500).setInterpolator(new DecelerateInterpolator());
            }
        });
    }

    public void LoopAnimTop(final TextView view) {
        view.animate().alpha(0).translationY(-view.getHeight()).setDuration(500).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
            @Override
            public void run() {
                view.setAlpha(0);
                view.setTranslationY(+view.getHeight());
                view.animate().alpha(1).translationY(0).setDuration(500).setInterpolator(new DecelerateInterpolator());
            }
        });
    }

    public void initViews() {
        final ResizeAnimation resizeAnimation = new ResizeAnimation(mLineColor, 0);
        resizeAnimation.setDuration(400);
        mLineColor.startAnimation(resizeAnimation);
        mTitleCity.animate()
                .alpha(0)
                .translationY(-50)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mTitleCity.setAlpha(0);
                        mTitleCity.setTranslationY(-50);
                    }
                });

    }

    public void searchReset() {
        mWeatherIcon.animate()
                .alpha(0)
                .translationX(-mWeatherIcon.getWidth())
                .setDuration(400)
                .setStartDelay(400)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mWeatherIcon.setTranslationX(-mWeatherIcon.getWidth());
                        mWeatherIcon.setAlpha(0f);
                        mWeatherIcon.removeAllViews();
                        mWeatherIcon.invalidate();
                    }
                });

        mTextTemp.animate()
                .alpha(0)
                .translationY(-mTextTemp.getHeight())
                .setDuration(400)
                .setStartDelay(400)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mTextTemp.setTranslationY(+mTextTemp.getHeight());
                        mTextTemp.setAlpha(0);
                    }
                });

        mTextType.animate()
                .alpha(0)
                .translationY(+mTextType.getHeight())
                .setDuration(400)
                .setStartDelay(400)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mTextType.setTranslationY(-mTextType.getHeight());
                        mTextType.setAlpha(0);
                        mTextType.setText(Celsius);

                    }
                });
    }

    @Override
    public void onDestroy() {
        KeySaver.removeKey(WeatherActivity.this, "Fahrenheit");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mExpandedView.getVisibility() == View.VISIBLE) {
            hideView(mExpandedView);
            FAB_CURRENT_STATE = FAB_STATE_COLLAPSED;
            mCollapseFabButton.animate().rotationBy(-220).setDuration(200).start();
        } else {
            finish();
        }
    }

    public void swipeUp(boolean enabled) {
        if (enabled) {
            if (mIsConnected) {
                hideWear();
            }
            mAdapter.notifyDataSetChanged();
            mMainCity.animate().translationY(-mMainBg.getHeight()).setDuration(600).setStartDelay(200).setInterpolator(new AccelerateDecelerateInterpolator());
            mMainTemp.animate().translationY(-mMainBg.getHeight()).setDuration(600).setStartDelay(400).setInterpolator(new AccelerateDecelerateInterpolator());
            mMainBtn.animate().translationY(-mMainBg.getHeight()).setDuration(600).setStartDelay(300).setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mChildTemp.setEnabled(true);
                            mChildTemp.setTranslationY(0);
                            mListForecast.setTranslationY(0);
                            mChildTemp.animate().alpha(1).setDuration(500).setStartDelay(200).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    mChildTemp.setAlpha(1);
                                    mListForecast.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mWeatherIcon.setEnabled(false);
                                            mListForecast.setEnabled(true);
                                        }
                                    });

                                }
                            });
                        }
                    });
        }

    }

    public void swipeDown(boolean enabled) {
        if (enabled) {
            mWeatherIcon.setEnabled(true);
            mChildTemp.setEnabled(false);
            mChildTemp.animate().alpha(0).setDuration(600).setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mChildTemp.setAlpha(0);
                            mChildTemp.setTranslationY(+mChildTemp.getHeight());
                            mListForecast.setTranslationY(+mListForecast.getHeight());
                            mListForecast.setEnabled(false);
                            if (mIsConnected) {
                                showWear();
                            }
                        }
                    });
            mMainTemp.animate().translationY(0).setDuration(600).setStartDelay(400).setInterpolator(new AccelerateDecelerateInterpolator());
            mMainBtn.animate().translationY(0).setDuration(600).setStartDelay(500).setInterpolator(new AccelerateDecelerateInterpolator());
            mMainCity.animate().translationY(0).setDuration(600).setStartDelay(600).setInterpolator(new AccelerateDecelerateInterpolator());
        }
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
        if (!mResolvingError) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override //ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        Log.e(TAG, "Google API Client was connected");
        mResolvingError = false;
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);

        new WearAsyncTask().execute("");
    }

    @Override //ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        Log.e(TAG, "Connection to Google API client was suspended");
    }

    @Override //OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Connection to Google API client has failed");
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        }
    }

    @Override //DataListener
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.e(TAG, "onDataChanged: " + dataEvents);
    }

    @Override //MessageListener
    public void onMessageReceived(final MessageEvent messageEvent) {

        if (messageEvent.getPath().equalsIgnoreCase(SEND_MODEL_PATH)) {

            mIsConnected = true;

            String Unknown = new String(messageEvent.getData());

            if (Unknown.contains("Unknown")) {
                mWearModel.setText(wearModel);
            } else {
                wearModel = new String(messageEvent.getData());
                mWearModel.setText(wearModel);
            }

            if (mWearLayout.getVisibility() == View.INVISIBLE) {
                showWear();
            }
        }

    }

    @Override //NodeListener
    public void onPeerConnected(final Node peer) {
        Log.e(TAG, "onPeerConnected: " + peer);

    }

    @Override //NodeListener
    public void onPeerDisconnected(final Node peer) {
        Log.e(TAG, "onPeerDisconnected: " + peer);
        mIsConnected = false;
        if (mWearLayout.getVisibility() == View.VISIBLE) {
            hideWear();
        }
    }

    public void sendStartActivityMessage(String node) {
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, START_ACTIVITY_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        }
                    }
                }
        );
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

    private void showWear() {
        mWearLayout.setTranslationX(+mWearLayout.getWidth());
        mWearLayout.setVisibility(View.VISIBLE);
        mWearLayout.animate().translationX(0).setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mWearLayout.setTranslationX(0);
                    }
                });
    }

    private void hideWear() {
        mWearLayout.animate().setDuration(500).translationX(+mWearLayout.getWidth())
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mWearLayout.setVisibility(View.INVISIBLE);
                    }
                });
    }

}
