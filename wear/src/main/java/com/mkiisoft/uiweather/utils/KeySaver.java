package com.mkiisoft.uiweather.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import java.util.Map;

public class KeySaver {
	private static final String AWKEY = "projects";
	private static final String AWPREFIX = "images_";
	
	public static String getIMEI( Activity a ) {
		TelephonyManager telephonyManager = (TelephonyManager) a.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
	
	public static String getDeviceID( Activity a ) {
		return Secure.getString(a.getBaseContext().getContentResolver(), Secure.ANDROID_ID);
	}
	
	public static void saveShare(Activity a,String keyname ,boolean f ) {
		SharedPreferences settings = a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean( AWPREFIX + keyname, f);
	    editor.commit();
	}
	
	public static void saveShare(Activity a,String keyname ,int f ) {
		SharedPreferences settings = a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(AWPREFIX + keyname, f);
	    editor.commit();
	}

	public static void saveShare(Activity a,String keyname ,String f ) {
		SharedPreferences settings = a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putString(AWPREFIX + keyname, f);
	    editor.commit();
	}

	public static void saveShare(Context a,String keyname ,String f ) {
		SharedPreferences settings = a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString( AWPREFIX + keyname, f);
		editor.commit();
	}

	
	public static boolean getBoolSavedShare(Activity a, String keyname) {
		SharedPreferences settings = a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
		return settings.getBoolean( AWPREFIX + keyname, false);
	}

	public static int getIntSavedShare(Activity a, String keyname) {
		SharedPreferences settings = a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
		return settings.getInt( AWPREFIX + keyname, -1);
	}

	public static String getStringSavedShare(Activity a, String keyname) {
		SharedPreferences settings = a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
		return settings.getString( AWPREFIX + keyname, null);
	}
	
	public static String getStringSavedShare(Context context, String keyname) {
		SharedPreferences settings = context.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
		return settings.getString( AWPREFIX + keyname, null);
	}
	
	public static Map<String,?> getAll(Activity a){
		SharedPreferences settings = a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
		return settings.getAll();
	}
	
	public static String getPrefix(){
		return AWPREFIX;
	}

    public static SharedPreferences getShared(Activity a){
        return a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
    }
	
	public static void removeKey (Activity a,String keyname) {
		SharedPreferences settings = a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
		if (isExist(a,keyname)){
            SharedPreferences.Editor editor = settings.edit();
            editor.remove(AWPREFIX+keyname);
            editor.commit();
        }
	}
	
	public static boolean isExist(Activity a,String keyname) {
		//si no existe retorna false
		SharedPreferences settings = a.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
		return settings.contains(AWPREFIX+keyname);
	}

		public static boolean isExist(Context context,String keyname) {
		//si no existe retorna false
		SharedPreferences settings = context.getSharedPreferences(AWKEY, Context.MODE_PRIVATE);
		return settings.contains(AWPREFIX+keyname);
	}


}
