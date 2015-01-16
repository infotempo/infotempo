package com.example.infotempo;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.Time;

public class InfoTempoService extends Service {

	public static final String EXTRA_FLAG_BOOT = "com.example.extraflag.boot";
	public static final String EXTRA_FLAG_APP_UPDATE = "com.example.extraflag.app_update";
	public static final String ACTION_UPDATE = "com.example.infotempo.ACTION_UPDATE";
	public static final String ACTION_APP_UPDATE = "com.example.infotempo.ACTION_APP_UPDATE";
	public static final String ACTION_UPDATE_UI = "com.example.infotempo.ACTION_UPDATE_UI";
	public static final String ACTION_UPDATE_MODE = "com.example.infotempo.ACTION_UPDATE_MODE";
	public static final String ACTION_UPDATE_HISTO = "com.example.infotempo.ACTION_UPDATE_HISTO";
	public static final String ACTION_START_UPDATE = "com.example.infotempo.ACTION_START_UPDATE";
	private static final int INFOTEMPO_NOTIFICATION_ID = 1;
	private static final String root = "http://www.example.com/";

	private UpdateTask updateTask;
	private SharedPreferences pref;
	private AlarmManager am;
	private boolean isBoot;
	private boolean isAppUpdate;
	private static volatile long seedUniquifier = 8682522807148012L;

	public static void launchService(Context context, String action) {
		Intent serviceIntent = new Intent();
        serviceIntent.setAction(action);
        context.sendBroadcast(serviceIntent);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
    public void onCreate() 
    {
		super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {
    	isBoot = intent.getBooleanExtra(EXTRA_FLAG_BOOT, false);
    	isAppUpdate = intent.getBooleanExtra(EXTRA_FLAG_APP_UPDATE, false);
    	
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
    	if (isBoot) {
    		SharedPreferences.Editor editor = pref.edit();
    		editor.putInt("rc", 0);
    		editor.commit();
    	}
    	
    	am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    	
    	updateTask = new UpdateTask();
    	updateTask.execute(this);
    	
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() 
    {
    	super.onDestroy();
    }
    
    private class UpdateTask extends AsyncTask<Context, Void, Boolean> {
    	
    	private InfoTempoData itd;

    	private int toInteger(String input)  
    	{  
    	   try  
    	   {  
    	      return Integer.parseInt(input);  
    	   }  
    	   catch(Exception e)  
    	   {  
    	      return -1;  
    	   }  
    	}
    	
    	private int extractDateFromString(String s) {
    		int numMois = -1;
    		int numAnnee = -1;
    		int numJour = -1;
    		String[] temp;
    		String[] moisS = {"j","f","m","a","m","j","j","a","s","o","n","d"};
    		String[] moisE = {"r","r","s","l","i","n","t","t","e","e","e","e"};
   		  	String delimiter = " ";
   		  	String tmpMois;
   		  	temp = s.toLowerCase().split(delimiter);
    		if (temp.length>=3) {
    			for(int j = 0; j < moisS.length; j++) {
    				tmpMois = temp[temp.length-2];
    				if ((tmpMois.startsWith(moisS[j]))&&(tmpMois.endsWith(moisE[j]))) {
    					numMois = j;
    				}
    			}
    			numAnnee = toInteger(temp[temp.length-1]);
    			numJour = toInteger(temp[temp.length-3]);
    		}
    		if ((numJour>=0)&&(numMois>=0)&&(numAnnee>=0)) {
    			return (((numAnnee-2000)*100+numMois+1)*100+numJour);
    		} else {return -1;}
    	}

    	private int extractTomorrowTimeFromString(String s) {
    		String tmp = s.substring(s.length()-4,s.length()-2);
    		return toInteger(tmp);
    	}
    	
    	private boolean isTodayString(String s) {
    		return s.toLowerCase().startsWith("aujourd");
    	}
    	
    	private boolean isTomorrowString(String s) {
    		return s.toLowerCase().startsWith("demain");
    	}

    	private int extractColorFromImgSrc(String s) {
    		String[] temp;
   		  	String delimiter = "/";
   		  	String s2;
   		  	temp = s.toLowerCase().split(delimiter);
    		if (temp.length>0) {
    			s2 = temp[temp.length-1];
    			if (s2.contains("_oui")) {
    				return CoulItem.ROUGE;
    			} else {
    				if (s2.contains("_non")) {
    					return CoulItem.BLANC;
    				}
    			}
    		}
    		return CoulItem.INCONNUE;
    	}
    	
    	private void dlDataFromWeb() {
    		boolean res = false;
    		int retryCount = 0;
    		while ((!res)&&(retryCount<4)) {
	    		if (itd.modeEJP) {
	    			res = parseEJP();
	    		} else {
	        		res = parse(0);
	        		if (!res) res = parse(1);
	        		if (!res) res = parse(2);
	    		}
	    		retryCount++;
    		}
    	}
    	
    	private long checkTime(long newTime, long currentTime) {
    		if ((newTime<currentTime)||(currentTime==0)) {
    			return newTime;
    		} else {
    			return currentTime;
    		}
    	}
    	
        @Override
        protected Boolean doInBackground(Context... params) {
        	Time t = new Time();
        	t.setToNow();
        	
        	Time ct = new Time();
        	ct.setToNow();
        	ct.set(0,t.minute+15,t.hour,t.monthDay,t.month,t.year);
    		ct.normalize(false);
    		Intent intent = new Intent(params[0], InfoTempoBroadcastReceiver.class);
    	    intent.setAction(ACTION_UPDATE);
    	    PendingIntent pendingIntent = PendingIntent.getBroadcast(params[0], 0, intent, 0);
    	    am.set(AlarmManager.RTC, ct.toMillis(false), pendingIntent);
    	    
        	itd = new InfoTempoData();
        	itd.loadData(pref);
        	
        	itd.nextUpdate = ct.toMillis(false);
        	itd.saveNextUpdate(pref);

        	long l = 0;
        	ct = new Time();
        	ct.setToNow();
        	
        	int coul_a_avoir = 0;
        	int coul_obtenues = 0;
        	
        	if (itd.widgetEnabled||isAppUpdate||itd.twEnabled) {
        		coul_a_avoir = 1;
        	}
        	if ((t.hour*100+t.minute>(itd.hrPrevDemain-1)*100)&&(isAppUpdate||itd.widgetEnabled||itd.alarmEnabled||itd.alarm2Enabled||itd.twEnabled)) {
        		coul_a_avoir += 2;
        	}

        	if (itd.clAuj!=-1) {coul_obtenues = 1;}
        	if (itd.clDemain!=-1) {coul_obtenues += 2;}
        	if (((coul_a_avoir&coul_obtenues)!=coul_a_avoir)&&(!isBoot)) {
        		dlDataFromWeb();
        		itd.lastUpdate = ct.toMillis(false);
        		itd.saveLastUpdate(pref);
        	}
        	
        	if ((t.hour*100+t.minute<itd.debutHP*100)&&(itd.widgetEnabled||itd.twEnabled)) {
        		ct.set(0,0,itd.debutHP,t.monthDay,t.month,t.year);
        		l = checkTime(ct.toMillis(false),l);
        	}
        	if ((t.hour*100+t.minute<itd.finHP*100)&&(itd.widgetEnabled||itd.twEnabled)) {
        		ct.set(0,0,itd.finHP,t.monthDay,t.month,t.year);
        		l = checkTime(ct.toMillis(false),l);
        	}
        	
        	ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        	MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        	activityManager.getMemoryInfo(memoryInfo);
        	
        	if ((t.hour*100+t.minute<(itd.hrPrevDemain-1)*100)&&(itd.widgetEnabled||itd.alarmEnabled||itd.alarm2Enabled||itd.twEnabled)) {
        		Random r = new Random(++seedUniquifier + System.nanoTime()+memoryInfo.availMem);
        		ct.set(r.nextInt(60*2)*3,1,itd.hrPrevDemain-1,t.monthDay,t.month,t.year);
        		ct.normalize(false);
        		l = checkTime(ct.toMillis(false),l);
        	}
        	if ((t.hour*100+t.minute<itd.hrPrevDemain*100)&&(itd.widgetEnabled||itd.alarmEnabled||itd.alarm2Enabled||itd.twEnabled)) {
        		Random r = new Random(++seedUniquifier + System.nanoTime()+memoryInfo.availMem);
        		ct.set(r.nextInt(60*2)*3,1,itd.hrPrevDemain,t.monthDay,t.month,t.year);
        		ct.normalize(false);
        		l = checkTime(ct.toMillis(false),l);
        	}
    		if (itd.widgetEnabled||itd.twEnabled) {
        		ct.set(0,0,itd.debutHP,t.monthDay+1,t.month,t.year);
        		ct.normalize(false);
        		l = checkTime(ct.toMillis(false),l);
    		}
    		if (itd.alarmEnabled||itd.alarm2Enabled) {
        		Random r = new Random(++seedUniquifier + System.nanoTime()+memoryInfo.availMem);
        		ct.set(r.nextInt(60*2)*3,1,itd.hrPrevDemain-1,t.monthDay+1,t.month,t.year);
        		ct.normalize(false);
        		l = checkTime(ct.toMillis(false),l);
    		}
    		if (itd.alarmEnabled||itd.alarm2Enabled) {
        		Random r = new Random(++seedUniquifier + System.nanoTime()+memoryInfo.availMem);
        		ct.set(r.nextInt(60*2)*3,1,itd.hrPrevDemain,t.monthDay+1,t.month,t.year);
        		ct.normalize(false);
        		l = checkTime(ct.toMillis(false),l);
    		}
        	coul_obtenues = 0;
        	if (itd.clAuj!=-1) {coul_obtenues = 1;}
        	if (itd.clDemain!=-1) {coul_obtenues += 2;}
        	if ((coul_a_avoir&coul_obtenues)!=coul_a_avoir) {
        		itd.retryCount++;
        		int delay = 30;
        		switch(itd.retryCount) {
        			case 1: {delay = 1;break;} 
        			case 2: {delay = 1;break;} 
        			case 3: {delay = 1;break;} 
        			case 4: {delay = 1;break;} 
        			case 5: {delay = 2;break;}
        			case 6: {delay = 5;break;}
        			case 7: {delay = 5;break;}
        			case 8: {delay = 10;break;}
        			case 9: {delay = 15;break;}
        			default: {delay = 30;break;}
        		}
        		ct.set(t.second,t.minute+delay,t.hour,t.monthDay,t.month,t.year);
        		ct.normalize(false);
        		l = checkTime(ct.toMillis(false),l);
        	} else {
        		itd.retryCount = 0;
        	}
        	
        	itd.saveRetryCount(pref);

        	pendingIntent = PendingIntent.getBroadcast(params[0], 0, intent, 0);
        	if (l>0) {
            	ct.set(l);
        		am.set(AlarmManager.RTC, ct.toMillis(false), pendingIntent);
        	} else {
        		am.cancel(pendingIntent);
        	}
        	
        	itd.saveMainData(pref);
        	itd.nextUpdate = ct.toMillis(false);
        	itd.saveNextUpdate(pref);
        	if ((itd.alarmEnabled)&&(itd.dtAuj!=itd.lastAlert)/*&&(t.hour*100+t.minute>=itd.hrPrevDemain*100)*/&&(itd.clDemain==CoulItem.ROUGE)) {
        		notify(params[0],"InfoTempo: jour rouge demain.","Jour rouge demain.",R.drawable.stat_notify_rouge, itd.notifSound);
	        	itd.saveLastAlertDate(pref);
        	}
        	if ((!itd.modeEJP)&&(itd.alarm2Enabled)&&(itd.dtAuj!=itd.lastAlert)/*&&(t.hour*100+t.minute>=itd.hrPrevDemain*100)*/&&(itd.clDemain==CoulItem.BLANC)) {
        		notify(params[0],"InfoTempo: jour blanc demain.","Jour blanc demain.",R.drawable.stat_notify_blanc, itd.notifSound2);
	        	itd.saveLastAlertDate(pref);
        	}
        	
        	return true;
        }
 
        private void notify(Context context, CharSequence tickerText, CharSequence contentText, int icon, String SoundUri) {
        	String ns = Context.NOTIFICATION_SERVICE;
        	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        	long when = System.currentTimeMillis();

        	Notification notification = new Notification(icon, tickerText, when);
        	CharSequence contentTitle = "InfoTempo";
        	Intent notificationIntent = new Intent(context, InfoTempoMainActivity.class);
        	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        	notification.flags = Notification.FLAG_AUTO_CANCEL;
        	Uri uri = (SoundUri==null?(Uri)null:Uri.parse(SoundUri));
        	notification.sound = uri;
        	if (itd.vibrate) {
        		notification.vibrate = new long[] {10,200,100,200,100,200};
        	}
        	
        	mNotificationManager.notify(INFOTEMPO_NOTIFICATION_ID, notification);
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
        	InfoTempoApp.updating = false;
        	Intent intent = new Intent();
        	intent.setAction(ACTION_UPDATE_UI);
        	sendBroadcast(intent);
        	
        	stopSelf();
        }

    	@Override
		protected void onPreExecute() {
    		InfoTempoApp.updating = true;
        	Intent intent = new Intent();
        	intent.setAction(ACTION_START_UPDATE);
        	sendBroadcast(intent);
        	itd = new InfoTempoData();
        	itd.loadData(pref);
		}

		public boolean isInteger( String input )  
    	{  
    	   try  
    	   {  
    	      Integer.parseInt( input );  
    	      return true;  
    	   }  
    	   catch( Exception e)  
    	   {  
    	      return false;  
    	   }  
    	}

    	public boolean parse(int urlMode) {
    		try {
    			Document doc = null;
    			if (urlMode == 1) {
	    			Connection connection1 = Jsoup.connect("http://bleuciel.edf.com/?redirect=false");
	    			Response response1 = connection1.execute();
	    			Map<String, String> cookies = response1.cookies();
	    			/*Document document1 =*/ response1.parse(); // If necessary.
	    			// ...
	    			
	    			Connection connection2 = Jsoup.connect("http://bleuciel.edf.com/abonnement-et-contrat/les-prix/les-prix-de-l-electricite/option-tempo/la-couleur-du-jour-2585.html?coe_i_id=2585");
	    			for (Entry<String, String> cookie : cookies.entrySet()) {
	    			    connection2.cookie(cookie.getKey(), cookie.getValue());
	    			}
	    			Response response2 = connection2.execute();
	    			cookies.putAll(response2.cookies());
	    			Document document2 = response2.parse();
	    			doc = document2;
    			}
    			if (urlMode == 0) {
	    			Connection connection1 = Jsoup.connect("https://particuliers.edf.com/gestion-de-mon-contrat/options-tarifaires/option-tempo/la-couleur-du-jour-2585.html&redirect=false");
	    			Response response1 = connection1.execute();
	    			doc = response1.parse();
    			}
    			if (urlMode == 2) {
	    			Connection connection1 = Jsoup.connect(root+"infotempo.php");
	    			Response response1 = connection1.execute();
	    			doc = response1.parse();
    			}
    			Elements divs = doc.select("div.TempoDay");
    			String titleItem;
    			String descItem;
    			int dtItem;
    			int clItem;
    			for (Element div : divs) {
    				titleItem = "";
    				descItem = "";
    				dtItem = 0;
    				clItem = -1;
    			    Element elt = div.select("h4").first();
    			    if (elt!=null) {
    			    	titleItem = elt.text();
    			    	Element descElt = div.select("div.contentText").first();
    			    	if (descElt!=null) {
    			    		descItem = descElt.text();
    			    	}
    			    	Element tmpHPElt = div.select("span[class=hours left]").first();
    			    	if (tmpHPElt!=null) {
    			    		String tmp = tmpHPElt.text();
    			    		if (tmp.length()>1) {
    			    			tmp = tmp.substring(0,tmp.length()-1);
    			    			if (isInteger(tmp)) {
    			    				itd.debutHP = Integer.parseInt(tmp);
    			    			}
    			    		}
    			    	}
    			    	tmpHPElt = div.select("span[class=hours right]").first();
    			    	if (tmpHPElt!=null) {
    			    		String tmp = tmpHPElt.text();
    			    		if (tmp.length()>1) {
    			    			tmp = tmp.substring(0,tmp.length()-1);
    			    			if (isInteger(tmp)) {
    			    				itd.finHP = Integer.parseInt(tmp);
    			    			}
    			    		}
    			    	}
    				    Element tempoColorList = div.select("ul.tempoColor").first();
    				    if (tempoColorList!=null) {
    					    Element tempoColor = tempoColorList.select("li.blue").first();
    					    if ((tempoColor!=null)&(tempoColor.text().equalsIgnoreCase("X"))) {
    					    	clItem = CoulItem.BLEU;
    					    } else {
    					    	tempoColor = tempoColorList.select("li.white").first();
    						    if ((tempoColor!=null)&(tempoColor.text().equalsIgnoreCase("X"))) {
    						    	clItem = CoulItem.BLANC;
    						    } else {
    						    	tempoColor = tempoColorList.select("li.red").first();
    							    if ((tempoColor!=null)&(tempoColor.text().equalsIgnoreCase("X"))) {
    								    clItem = CoulItem.ROUGE;
    							    } else {
    								    clItem = CoulItem.INCONNUE;
    							    }
    						    }
    					    }
    				    }
    				    if (isTodayString(titleItem)) {
    				    	itd.descAuj = descItem;
    				    } else {
    				    	if (isTomorrowString(titleItem)) {
    				    		itd.descDemain = descItem;
    				    		itd.hrPrevDemain = extractTomorrowTimeFromString(descItem);
    				    	}
    				    }
    				    dtItem = extractDateFromString(titleItem);
    				    if (dtItem==itd.dtAuj) {
    				    	itd.clAuj = clItem;
    				    } else {
    				    	if (dtItem==itd.dtDemain) {
    				    		itd.clDemain = clItem;
    				    	}
    				    }
    			    } else {
    				    Element tempoDaysList = div.select("ul#TempoRemainingDays").first();
    				    if (tempoDaysList!=null) {
    				    	Element descElt = div.select("div.contentText").first();
    				    	if (descElt!=null) {
    				    		itd.descNbJours = descElt.text();
    				    	}
    					    Element tempoDays = tempoDaysList.select("li.blueDay").first();
    					    Elements elts;
    					    int n1 = -1;
    					    int n2 = -1;
    					    if (tempoDays!=null) {
    					    	elts = tempoDays.getAllElements();
    					    	for (Element tmpElt : elts) {
    					    		if (isInteger(tmpElt.text())) {
    					    			if (n1<0) {n1 = Integer.parseInt(tmpElt.text());}
    					    			else {n2 = Integer.parseInt(tmpElt.text());}
    					    		}
    					    	}
    					    	itd.n1 = n1; itd.t1 = n2;
    					    }
    					    n1 = -1;
    					    n2 = -1;
    				    	tempoDays = tempoDaysList.select("li.whiteDay").first();
    					    if (tempoDays!=null) {
    					    	elts = tempoDays.getAllElements();
    					    	for (Element tmpElt : elts) {
    					    		if (isInteger(tmpElt.text())) {
    					    			if (n1<0) {n1 = Integer.parseInt(tmpElt.text());}
    					    			else {n2 = Integer.parseInt(tmpElt.text());}
    					    		}
    					    	}
    					    	itd.n2 = n1; itd.t2 = n2;
    					    }
    					    n1 = -1;
    					    n2 = -1;
    				    	tempoDays = tempoDaysList.select("li.redDay").first();
    					    if (tempoDays!=null) {
    					    	elts = tempoDays.getAllElements();
    					    	for (Element tmpElt : elts) {
    					    		if (isInteger(tmpElt.text())) {
    					    			if (n1<0) {n1 = Integer.parseInt(tmpElt.text());}
    					    			else {n2 = Integer.parseInt(tmpElt.text());}
    					    		}
    					    	}
    					    	itd.n3 = n1; itd.t3 = n2;
    					    }
    				    }
    			    }
    			}
    			return true;
    		} catch (IOException e) {
    			return false;
    		}
    	}

    	public boolean parseEJP() {
    		try {
    			Connection connection1 = Jsoup.connect("http://bleuciel.edf.com/?redirect=false");
    			Response response1 = connection1.execute();
    			Map<String, String> cookies = response1.cookies();

    			Connection connection2 = Jsoup.connect("http://bleuciel.edf.com/abonnement-et-contrat/les-prix/les-prix-de-l-electricite/option-ejp/l-observatoire-2584.html");
    			for (Entry<String, String> cookie : cookies.entrySet()) {
    			    connection2.cookie(cookie.getKey(), cookie.getValue());
    			}
    			Response response2 = connection2.execute();
    			cookies.putAll(response2.cookies());
    			Document document2 = response2.parse();
    			Document doc = document2;
    			
    			Elements divs = doc.select("table[class=w_skinnedTable reacapEJPDay]");
    			String titleItem;
    			int dtItem;
    			int clItem;
    			int i;
    			for (Element div : divs) {
    				titleItem = "";
    				dtItem = 0;
    				clItem = -1;
    				Element elt = div.select("caption").first();
    				if (elt!=null) {
    					titleItem = elt.text();
    					dtItem = extractDateFromString(titleItem);
    					Element elt2 = div.select("tbody").first();
    					if (elt2!=null) {
    						Elements imgs = elt2.select("img");
    						i = 0;
    						for (Element img : imgs) {
    							if (i==itd.zoneEJP) {
    								clItem = extractColorFromImgSrc(img.attr("src"));
    							}
    							i++;
    						}
    					}
    				    if (dtItem==itd.dtAuj) {
    				    	itd.clAuj = clItem;
    				    } else {
    				    	if (dtItem==itd.dtDemain) {
    				    		itd.clDemain = clItem;
    				    	}
    				    }
    				}
    			}
    			doc = Jsoup.connect("http://edf-ejp-tempo.sfr-sh.fr/index.php?m=eh").get();
    			divs = doc.select("table.w_skinnedTable");
    			int n = 0;
    			itd.t1 = 0;
    			itd.t2 = 343;
    			Time ct = new Time();
            	ct.setToNow();
            	Time endt = new Time();
            	endt.set(59,59,23,31,3-1,ct.year);
            	endt.normalize(false);
            	int y = ct.year;
            	if (Time.compare(ct, endt)>0) { y++;}
            	Time tt = new Time();
            	tt.set(0,0,0,29, 2-1, y);
            	tt.normalize(false);
            	if (tt.month==(2-1)) {
            		itd.t2++;
            	}
            	
    			itd.t3 = 22;
    			itd.n1 = 0;
    			itd.n3 = -1;
    			for (Element div : divs) {
    				if (n==1) {
    					Element elt2 = div.select("tbody").first();
    					if (elt2!=null) {
        					Elements cols = elt2.select("td");
    						i = 0;
    						for (Element col : cols) {
    							if (i==itd.zoneEJP) {
    								itd.n3 = toInteger(col.text());
    							}
    							i++;
    						}
    					}
    				}
    			    n++;
    			}
            	ct.set(0,0,0,ct.monthDay,ct.month,ct.year);
            	ct.normalize(false);
            	tt.set(0,0,0,31,3-1,y);
            	tt.normalize(false);
            	long dt1 = ct.toMillis(false);
            	long dt2 = tt.toMillis(false);
    			itd.n2 = (int) (daysBetween(dt1, dt2)-itd.n3);
    			if (itd.clAuj==CoulItem.INCONNUE) {itd.n2++;}

    			itd.debutHP = 7;
    			itd.descAuj = "Depuis le 1er décembre 2006, la France est divisée en 4 zones EJP.";
    			itd.descDemain = "Cette information est en principe mise à jour vers 17h30.";
    			itd.descNbJours = "Cette information n'est pas contractuelle, elle est sans valeur d'engagement.";
    			itd.finHP = 1;
    			itd.hrPrevDemain = 17;
    			
    			return true;
    		} catch (IOException e) {
    			return false;
    		}
    	}
    	
    	public long daysBetween(long firstSeconds, long secondSeconds) {
    		long SECONDS_PER_DAY = 24 * 60 * 60;
    		firstSeconds /= 1000;
    		secondSeconds /= 1000;
    		long difference = secondSeconds - firstSeconds;
    		if (difference >= 0) {
    		difference += SECONDS_PER_DAY / 2;
    		} else {
    		difference -= SECONDS_PER_DAY / 2;
    		}
    		difference /= SECONDS_PER_DAY;
    		return difference;
    	}
    	
}
    
}
