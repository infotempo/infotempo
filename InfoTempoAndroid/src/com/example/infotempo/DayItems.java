package com.example.infotempo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.content.SharedPreferences;
import android.text.format.Time;

public class DayItems {
	private ArrayList<DayItem> days;
	
	public DayItems() {
		days = new ArrayList<DayItem>();
	}
	public DayItem findDay(int d, int m, int y) {
		for (DayItem day : days) {
			if ((day.getDay()==d)&&(day.getMonth()==m)&&(day.getYear()==y)) {
				return day;
			}
		}
		return null;
	}
	public void addDay(int d, int m, int y, int cl) {
		if (findDay(d,m,y)==null) {
			days.add(new DayItem(d,m,y,cl));
		}
	}
	public void updateDay(int d, int m, int y, int cl) {
		DayItem di = findDay(d,m,y); 
		if (di==null) {
			days.add(new DayItem(d,m,y,cl));
		} else {
			di.setCouleur(cl);
		}
	}
	public int getColor(int d, int m, int y) {
		DayItem di = findDay(d,m,y);
		if (di!=null) {
			return CoulItem.getColor(di.getCouleur());
		}
		return 0x00000000;
	}
	public int getTextColor(int d, int m, int y) {
		DayItem di = findDay(d,m,y);
		if (di!=null) {
			return CoulItem.getTextColor(di.getCouleur());
		}
		return 0xffffffff;
	}
	public ArrayList<String> getYears() {
		ArrayList<String> s = new ArrayList<String>();
		boolean f;
		for (DayItem day : days) {
			f = false;
			for (String tmp : s) {
				if (tmp.equals(Integer.toString(day.getYear()))) {
					f = true;
					break;
				}
			}
			if (!f) {s.add(Integer.toString(day.getYear()));}
		}
		Collections.sort(s);
		Collections.reverse(s);
		return s;
	}
	public ArrayList<Integer> getYearMonths() {
		ArrayList<Integer> s = new ArrayList<Integer>();
		boolean f;
		for (DayItem day : days) {
			f = false;
			for (Integer tmp : s) {
				if (tmp == day.getYear()*100+day.getMonth()) {
					f = true;
					break;
				}
			}
			if (!f) {s.add(day.getYear()*100+day.getMonth());}
		}
		Collections.sort(s);
		//Collections.reverse(s);
		return s;
	}
	
	public void saveDays(SharedPreferences pref) {
		long d, n;
		int p, ymin, ymax;
		n = 3;
		Long cd;
		HashMap<Integer, Long> temp = new HashMap<Integer, Long>();
		ymin = pref.getInt("histoMinYear", 100000);
		ymax = pref.getInt("histoMaxYear", 0);
		SharedPreferences.Editor editor = pref.edit();
		for (DayItem day : days) {
			cd = temp.get(day.getYear()*100+day.getMonth());
			if (cd == null)	d = pref.getLong("histo"+(day.getYear()*100+day.getMonth()), 0); else d = cd;
			p = (day.getDay()-1) << 1;
			d &= ~(n << p);
			d |= (((long)(day.getCouleur())) << p);
			temp.put(day.getYear()*100+day.getMonth(),d);
			editor.putLong("histo"+(day.getYear()*100+day.getMonth()), d);
			if (day.getYear() < ymin) {
				ymin = day.getYear();
			}
			if (day.getYear() > ymax) {
				ymax = day.getYear();
			}
		}
		editor.putInt("histoMinYear",ymin);
		editor.putInt("histoMaxYear",ymax);
		Time ct = new Time();
    	ct.setToNow();
    	int cdate = ct.year*100*100+(ct.month+1)*100+(ct.monthDay);
    	editor.putInt("histoLastDate", cdate);
		editor.commit();
	}
	public void initFromPref(SharedPreferences pref) {
		long d;
		int i, p, y, ymin, ymax, m, v;
		ymin = pref.getInt("histoMinYear", 100000);
		ymax = pref.getInt("histoMaxYear", 0);
		for (y = ymin; y <= ymax; y++) {
			for (m = 1; m <= 12; m++) {
				d = pref.getLong("histo"+(y*100+m), 0);
				if (d > 0) {
					Time t = new Time();
				    t.set(1, m-1, y); // mois de 0 à 11
				    t.normalize(false);
				    p = 0;
				    for (i = 0; i<t.getActualMaximum(Time.MONTH_DAY); i++) {
				    	v = (int)((d >> p)&3);
				    	if (v > 0) addDay(i+1, m, y, v);
				    	p += 2;
				    }
				}
			}
		}
		if (days.isEmpty()) {
			Time t = new Time();
			t.setToNow();
			addDay(t.monthDay,t.month+1,t.year,-1);
		}
	}
}
