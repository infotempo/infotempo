package com.example.infotempo;

import android.app.Application;
import android.content.Context;

public class InfoTempoApp extends Application {
	public static Context context;
	public static boolean updating;
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}
	
}
