package com.example.infotempo;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.widget.RemoteViews;

public class InfoTempoWidget extends AppWidgetProvider {
	private InfoTempoData itd;
	private SharedPreferences pref;
	private boolean isAppUpdate;
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		itd = new InfoTempoData();
		itd.widgetEnabled = false;
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		itd.saveWidget(pref);
		InfoTempoService.launchService(context, InfoTempoService.ACTION_APP_UPDATE);
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		itd = new InfoTempoData();
		itd.widgetEnabled = true;
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		itd.saveWidget(pref);
		InfoTempoService.launchService(context, InfoTempoService.ACTION_APP_UPDATE);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		isAppUpdate = (intent.getAction().equals(InfoTempoService.ACTION_START_UPDATE));
		if ((intent.getAction().equals(InfoTempoService.ACTION_UPDATE_UI))||(isAppUpdate)) {
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		    ComponentName thisAppWidget = new ComponentName(context.getPackageName(), InfoTempoWidget.class.getName());
		    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

		    onUpdate(context, appWidgetManager, appWidgetIds);
		}
	}

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
 
        itd = new InfoTempoData();
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        itd.loadData(pref);
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId, itd, isAppUpdate);
        }
    }
 
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
    		int appWidgetId, InfoTempoData itd, boolean isAppUpdate) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.infotempo_widget_layout);
        int cl1;
        int cl2;
        int dt2;
        Time t = new Time();
        t.setToNow();
        if (!itd.modeEJP) {
	        if ((t.hour<itd.debutHP)||(t.hour>=itd.finHP)) {
	        	views.setImageViewResource(R.id.widget_imgDayNight, R.drawable.moon);
	        } else {
	        	views.setImageViewResource(R.id.widget_imgDayNight, R.drawable.sun);
	        }
        } else {
        	views.setImageViewResource(R.id.widget_imgDayNight, R.drawable.ejp);
        }
        if (isAppUpdate) {
        	views.setImageViewResource(R.id.widget_btRefresh, R.drawable.dl);
        } else {
        	views.setImageViewResource(R.id.widget_btRefresh, R.drawable.refresh);
        }
        cl1 = itd.getCurrentColor();
        cl2 = itd.getNextColor();
        dt2 = itd.getNextDate();
        views.setTextColor(R.id.widget_lblDt1, CoulItem.getTextColor(cl1));
        switch(cl1) {
        	case 1: {cl1 = R.drawable.widget_label_bg_blue;break;}
        	case 2: {cl1 = R.drawable.widget_label_bg_white;break;}
        	case 3: {cl1 = R.drawable.widget_label_bg_red;break;}
        	default: {cl1 = R.drawable.widget_label_bg_gray;break;}
        }
        views.setInt(R.id.widget_lblDt1, "setBackgroundResource", cl1);
        
        views.setTextColor(R.id.widget_lblDt2, CoulItem.getTextColor(cl2));
        switch(cl2) {
	    	case 1: {cl2 = R.drawable.widget_label_bg_blue;break;}
	    	case 2: {cl2 = R.drawable.widget_label_bg_white;break;}
	    	case 3: {cl2 = R.drawable.widget_label_bg_red;break;}
	    	default: {cl2 = R.drawable.widget_label_bg_gray;break;}
        }
        views.setInt(R.id.widget_lblDt2, "setBackgroundResource", cl2);
        
        Time tmp = InfoTempoData.intToDate(dt2);
        String tmpj = "";
        if (tmp.monthDay==1) {tmpj = "er";}
        views.setTextViewText(R.id.widget_lblDt2, "le "+tmp.monthDay+tmpj);
        
        views.setTextViewText(R.id.widget_lblCpt1, ""+itd.n1);
        views.setTextColor(R.id.widget_lblCpt1,CoulItem.getColor(CoulItem.BLEU));
        views.setTextViewText(R.id.widget_lblCpt2, ""+itd.n2);
        views.setTextColor(R.id.widget_lblCpt2,CoulItem.getColor(CoulItem.BLANC));
        views.setTextViewText(R.id.widget_lblCpt3, ""+itd.n3);
        views.setTextColor(R.id.widget_lblCpt3,0xffff3a51);
        views.setTextViewText(R.id.widget_lblCptT1, ""+itd.t1);
        views.setTextColor(R.id.widget_lblCptT1,CoulItem.getColor(CoulItem.BLEU));
        views.setTextViewText(R.id.widget_lblCptT2, ""+itd.t2);
        views.setTextColor(R.id.widget_lblCptT2,CoulItem.getColor(CoulItem.BLANC));
        views.setTextViewText(R.id.widget_lblCptT3, ""+itd.t3);
        views.setTextColor(R.id.widget_lblCptT3,0xffff3a51);
        
        Intent intent = new Intent(context, InfoTempoMainActivity.class);
    	PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
    	views.setOnClickPendingIntent(R.id.infotempo_widget, pendingIntent);
    	
    	intent = new Intent(context, InfoTempoBroadcastReceiver.class);
	    intent.setAction(InfoTempoService.ACTION_APP_UPDATE);
	    pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
	    views.setOnClickPendingIntent(R.id.widget_btRefresh, pendingIntent);
	    
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    
}
