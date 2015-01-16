package com.example.infotempo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//import android.util.Log;

public class InfoTempoBroadcastReceiver extends BroadcastReceiver {

	private void launchService(Context context, boolean isBoot, boolean isAppUpdate) {
		//Log.v("InfoTempoBroadcastReceiver", "Launching service [boot:"+isBoot+", appupdate:"+isAppUpdate+"]");
		Intent i = new Intent();
		i.setAction("com.example.infotempo.InfoTempoService");
		i.putExtra(InfoTempoService.EXTRA_FLAG_BOOT, isBoot);
		i.putExtra(InfoTempoService.EXTRA_FLAG_APP_UPDATE, isAppUpdate);
		context.startService(i);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.v("InfoTempoBroadcastReceiver", "InfoTempoBroadcastReceiver -- onReceive()");
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			launchService(context,true,false);
		} else {
			if (intent.getAction().equals(InfoTempoService.ACTION_UPDATE)) {
				launchService(context,false,false);
			} else {
				if (intent.getAction().equals(InfoTempoService.ACTION_APP_UPDATE)) {
					launchService(context,false,true);
				}
			}
		}
	}

}
