package com.example.infotempo;

import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.text.format.Time;

public class InfoTempoData {
	public int dtHier;
	public int dtAuj;
	public int dtDemain;
	public int clHier;
	public int clAuj;
	public int clDemain;
	public int debutHP;
	public int finHP;
	public int hrPrevDemain;
	public boolean widgetEnabled;
	public boolean alarmEnabled;
	public boolean alarm2Enabled;
	public boolean twEnabled;
	public int retryCount;
	public int lastAlert;
	public int n1;
	public int t1;
	public int n2;
	public int t2;
	public int n3;
	public int t3;
	public String descAuj;
	public String descDemain;
	public String descNbJours;
	public String notifSound;
	public String notifSound2;
	public boolean vibrate;
	public boolean modeEJP;
	public int zoneEJP;
	public long lastUpdate;
	public long nextUpdate;

	public int dateToInt(Time date) {
		return ((date.year-2000)*100+date.month+1)*100+date.monthDay;
	}
	
	public static Time intToDate(int intDate) {
		Time tmp = new Time();
		tmp.set(intDate%100, ((intDate/100)%100)-1, (intDate/10000)+2000);
		tmp.normalize(false);
		return tmp;
	}
	
	public InfoTempoData() {
		super();
		clHier = -1;
		clAuj = -1;
		clDemain = -1;
		lastAlert = 0;
		modeEJP = false;
		zoneEJP = -1;
	}

	public void loadData(SharedPreferences pref) {
		
		Time hier = new Time();
    	hier.setToNow();
    	hier.set(0,0,0,hier.monthDay-1, hier.month, hier.year);
    	hier.normalize(false);
    	dtHier = dateToInt(hier);
    	
    	Time demain = new Time();
    	demain.setToNow();
    	demain.set(0,0,0,demain.monthDay+1, demain.month, demain.year);
    	demain.normalize(false);
    	dtDemain = dateToInt(demain);
    	
    	Time auj = new Time();
    	auj.setToNow();
    	dtAuj = dateToInt(auj);
    	
		clHier = -1;
		clAuj = -1;
		clDemain = -1;
		
		int d = pref.getInt("d0", -1);
		if (d == dtHier) {
			clHier = pref.getInt("c0", -1); 
		} else {
			if (d == dtAuj) {
				clAuj = pref.getInt("c0", -1);
			} else {
				if (d == dtDemain) {
					clDemain = pref.getInt("c0", -1);
				}
			}
		}
		d = pref.getInt("d1", -1);
		if (d == dtHier) {
			clHier = pref.getInt("c1", -1); 
		} else {
			if (d == dtAuj) {
				clAuj = pref.getInt("c1", -1);
			} else {
				if (d == dtDemain) {
					clDemain = pref.getInt("c1", -1);
				}
			}
		}
		d = pref.getInt("d2", -1);
		if (d == dtHier) {
			clHier = pref.getInt("c2", -1); 
		} else {
			if (d == dtAuj) {
				clAuj = pref.getInt("c2", -1);
			} else {
				if (d == dtDemain) {
					clDemain = pref.getInt("c2", -1);
				}
			}
		}
		
		debutHP = pref.getInt("dHP", 6);
		finHP = pref.getInt("fHP", 22);
		hrPrevDemain = pref.getInt("hpd", 17);
		n1 = pref.getInt("n1",0); t1 = pref.getInt("t1",0);
		n2 = pref.getInt("n2",0); t2 = pref.getInt("t2",0);
		n3 = pref.getInt("n3",0); t3 = pref.getInt("t3",0);
		descAuj = pref.getString("desca", "Votre journée Tempo se divise en deux périodes : les Heures Pleines et les Heures Creuses. Quelle que soit la couleur du jour, vous bénéficiez d'un Tarif Heures Creuses.");
		descDemain = pref.getString("descd", "Cette information est réactualisée tous les jours à partir de 17h.");
		descNbJours = pref.getString("descn", "Attention : cette information n'est pas contractuelle, elle est sans valeur d'engagement.");
		widgetEnabled = pref.getBoolean("we", false);
		alarmEnabled = pref.getBoolean("ae", false);
		alarm2Enabled = pref.getBoolean("a2e", false);
		twEnabled = pref.getBoolean("twe", false);
		retryCount = pref.getInt("rc", 0);
		lastAlert = pref.getInt("la", 0);
		Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		String uripath = (uri==null)?null:uri.toString();
		notifSound = pref.getString("nsnd", uripath);
		if (notifSound.equals("<null>")) {notifSound = null;}
		notifSound2 = pref.getString("nsnd2", uripath);
		if (notifSound2.equals("<null>")) {notifSound2 = null;}
		vibrate = pref.getBoolean("vib", false);
		modeEJP = pref.getBoolean("ejp", false);
		zoneEJP = pref.getInt("zejp", -1);
		lastUpdate = pref.getLong("lu", 0);
		nextUpdate = pref.getLong("nu", 0);
		
	}

	public void saveMainData(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("d0", dtHier);
		editor.putInt("d1", dtAuj);
		editor.putInt("d2", dtDemain);
		editor.putInt("c0", clHier);
		editor.putInt("c1", clAuj);
		editor.putInt("c2", clDemain);
		editor.putInt("dHP", debutHP);
		editor.putInt("fHP", finHP);
		editor.putInt("hpd", hrPrevDemain);
		editor.putInt("n1", n1); editor.putInt("t1", t1);
		editor.putInt("n2", n2); editor.putInt("t2", t2);
		editor.putInt("n3", n3); editor.putInt("t3", t3);
		editor.putString("desca", descAuj);
		editor.putString("descd", descDemain);
		editor.putString("descn", descNbJours);
    	editor.commit();
	}
	
	public void saveRetryCount(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("rc", retryCount);
    	editor.commit();
	}

	public void saveAlert(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("ae", alarmEnabled);
    	editor.commit();
	}

	public void saveAlert2(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("a2e", alarm2Enabled);
    	editor.commit();
	}

	public void saveNotifSounds(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("nsnd", (notifSound==null)?"<null>":notifSound);
		editor.putString("nsnd2", (notifSound2==null)?"<null>":notifSound2);
    	editor.commit();
	}

	public void saveVibrate(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("vib", vibrate);
    	editor.commit();
	}

	public void saveWidget(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("we", widgetEnabled);
    	editor.commit();
	}

	public void saveTextraWidget(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("twe", twEnabled);
    	editor.commit();
	}
	
	public void saveLastAlertDate(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		Time tmp = new Time();
    	tmp.setToNow();
    	lastAlert = dateToInt(tmp);
		editor.putInt("la", lastAlert);
    	editor.commit();
	}

	public void resetLastAlertDate(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
    	lastAlert = 0;
		editor.putInt("la", lastAlert);
    	editor.commit();
	}

	public void saveModeEJP(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean("ejp", modeEJP);
    	editor.commit();
	}
	
	public void saveZoneEJP(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("zejp", zoneEJP);
    	editor.commit();
	}
	
	public void setMode(boolean pModeEJP) {
		//if (modeEJP!=pModeEJP) {
	    	modeEJP = pModeEJP;
	    	clAuj = CoulItem.INCONNUE;
	    	clDemain = CoulItem.INCONNUE;
	    	clHier = CoulItem.INCONNUE;
	    	descAuj = "";
	    	descDemain = "";
	    	descNbJours = "";
	    	lastAlert = 0;
	    	retryCount = 0;
	    	n1 = -1;
	    	n2 = -1;
	    	n3 = -1;
	    	t1 = 0;
	    	t2 = 0;
	    	t3 = 0;
		//}
	}

	public void saveLastUpdate(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putLong("lu", lastUpdate);
    	editor.commit();
	}

	public void saveNextUpdate(SharedPreferences pref) {
		SharedPreferences.Editor editor = pref.edit();
		editor.putLong("nu", nextUpdate);
    	editor.commit();
	}
	
	public int getCurrentColor() {
		int cl1;
        //int cl2;
        //int dt2;
        Time t = new Time();
        t.setToNow();
        if (t.hour<debutHP) {
        	if (t.hour>finHP) cl1 = 0; else	cl1 = clHier;
        	//cl2 = clAuj;
        	//dt2 = dtAuj;
        } else {
        	cl1 = clAuj;
        	//cl2 = clDemain;
        	//dt2 = dtDemain;
        }
        return cl1;
	}

	public int getNextColor() {
		//int cl1;
        int cl2;
        //int dt2;
        Time t = new Time();
        t.setToNow();
        if (t.hour<debutHP) {
        	//cl1 = clHier;
        	if (t.hour>finHP) cl2 = 0; else	cl2 = clAuj;
        	//dt2 = dtAuj;
        } else {
        	//cl1 = clAuj;
        	cl2 = clDemain;
        	//dt2 = dtDemain;
        }
        return cl2;
	}

	public int getNextDate() {
		//int cl1;
        //int cl2;
        int dt2;
        Time t = new Time();
        t.setToNow();
        if (t.hour<debutHP) {
        	//cl1 = clHier;
        	//cl2 = clAuj;
        	dt2 = dtAuj;
        } else {
        	//cl1 = clAuj;
        	//cl2 = clDemain;
        	dt2 = dtDemain;
        }
        return dt2;
	}
	
}
