package com.example.infotempo;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

public class HistoFragment extends Fragment {
	private HistoPagerAdapter histoPagerAdapter;
	private View fragmentView;
	private ViewPager viewPager;
	private boolean disableUpdate;
	DayItems histoCoul;
	private ArrayList<Integer> histoMonths;
	Integer[] dayid = { R.id.j1, R.id.j2, R.id.j3, R.id.j4, R.id.j5, R.id.j6, R.id.j7, R.id.j8, R.id.j9, R.id.j10,
			R.id.j11, R.id.j12, R.id.j13, R.id.j14, R.id.j15, R.id.j16, R.id.j17, R.id.j18, R.id.j19, R.id.j20,
			R.id.j21, R.id.j22, R.id.j23, R.id.j24, R.id.j25, R.id.j26, R.id.j27, R.id.j28, R.id.j29, R.id.j30,
			R.id.j31, R.id.j32, R.id.j33, R.id.j34, R.id.j35, R.id.j36, R.id.j37, R.id.j38, R.id.j39, R.id.j40,
			R.id.j41, R.id.j42
	};
	Integer[] weekid = { R.id.snum1, R.id.snum2, R.id.snum3, R.id.snum4, R.id.snum5, R.id.snum6};
	private boolean inRefresh;
    private SharedPreferences pref;
    private InfoTempoData itd;
    private ProgressDialog progressDialog;
    private Spinner s1, s2;
    private int lockupds1, lockupds2;
    private boolean saveHisto;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (fragmentView != null) {
			View oldParent = (View) fragmentView.getParent();
			if(oldParent != container) ((ViewGroup)oldParent).removeView(fragmentView);
		} else {
		inRefresh = false;
		fragmentView = (LinearLayout) inflater.inflate(R.layout.histo_pager_layout, container, false);
		s1 = (Spinner) fragmentView.findViewById(R.id.spinner1);
		s2 = (Spinner) fragmentView.findViewById(R.id.spinner2);
		if (s1!=null) {
	    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.Months, android.R.layout.simple_spinner_item);
	    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    	s1.setAdapter(adapter);
	    }
		s1.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,	int arg2, long arg3) {
				if (lockupds1==0) {
					if (s2.getAdapter() != null) {
						String s = (String) s2.getAdapter().getItem(s2.getSelectedItemPosition());
						if (histoMonths != null) {
							try {
								int i = histoMonths.indexOf(toInteger(s)*100+arg2+1);
								if (i < 0) updateSpinners(viewPager.getCurrentItem());
								else viewPager.setCurrentItem(i,true);
							}
							catch (Exception e) {
								
							}
						}
					}
				} else lockupds1--;
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		s2.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,	int arg2, long arg3) {
				if (lockupds2==0) {
					if (s2.getAdapter() != null) {
						String s = (String) s2.getAdapter().getItem(arg2);
						if (histoMonths != null) {
							try {
								int i = histoMonths.indexOf(toInteger(s)*100+s1.getSelectedItemPosition()+1);
								if (i < 0) updateSpinners(viewPager.getCurrentItem());
								else viewPager.setCurrentItem(i,true);
							}
							catch (Exception e) {
								
							}
						}
					}
				} else lockupds2--;
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		viewPager = (ViewPager) fragmentView.findViewById(R.id.histopager);
		histoPagerAdapter = new HistoPagerAdapter(inflater);
		viewPager.setAdapter(histoPagerAdapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				updateSpinners(position);
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
		});
		pref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		itd = new InfoTempoData();
	    itd.loadData(pref);
		}
		//Log.d("HISTO","lastdate "+pref.getInt("histoLastDate", 0));
	    InfoTempoService.launchService(this.getActivity(), InfoTempoService.ACTION_UPDATE_HISTO);
		return fragmentView;
	}
	
	public void update(Activity a) {
		//Toast.makeText(this, "Histo update", Toast.LENGTH_SHORT).show();
		/*itd.loadData(pref);
		if (itd.modeEJP&&(itd.zoneEJP==-1)) {
			selectZoneEJP();
		} else */{
			/*if (!inRefresh) {
				inRefresh = true;
				Log.d("HISTO","update");
				new LoadHistoThread(this.getActivity()).execute();
			}*/
			if (!inRefresh) {
				itd.loadData(pref);
				inRefresh = true;
				//histoCoul = app.HistoCoul;
				progressDialog = null;
				int lastdate = pref.getInt("histoLastDate", 0);
				Time ct = new Time();
	        	ct.setToNow();
	        	int cdate = ct.year*100*100+(ct.month+1)*100+(ct.monthDay);
				if ((!itd.modeEJP)&&(cdate > lastdate)) {
					histoCoul = null;
					saveHisto = true;
				}
				if (histoCoul == null) progressDialog = ProgressDialog.show(a, "", "Chargement des données d'historique...", true);
			    new Thread() {
			      public void run() {
			    	  try {
			    		if (histoCoul==null) {
			    			if (itd.modeEJP) {parseEJP();} else {parse();}
			    			if (histoCoul != null) {
					    		histoMonths = histoCoul.getYearMonths();
					    	}
			    		}
			    	  }
			    	  catch (Exception e) {
			    		  //Log.e("Error",e.getMessage());
			    	  }
			    	  mHandler.sendEmptyMessage(0);
			    	  // dismiss the progressdialog   
			    	  //progressDialog.dismiss();
			      }
			    }.start();
			}
		}
	}
	
	// Define the Handler that receives messages from the thread and update the progress
    private Handler mHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
    	    if ((s2!=null)&&(histoCoul!=null)) {
    	    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(s2.getContext(), android.R.layout.simple_spinner_item,histoCoul.getYears());
    	    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	    	try {
    	    		s2.setAdapter(adapter);
    	    	}
    	    	catch (Exception e) {
    	    		
    	    	}
    	    	if (saveHisto) {
    	    		histoCoul.saveDays(pref);
    	    		saveHisto = false;
    	    		//Log.d("HISTO","save histo");
    	    	}
    	    }
    	    if (progressDialog != null) progressDialog.dismiss();
			histoPagerAdapter.notifyDataSetChanged();
			viewPager.setCurrentItem(histoPagerAdapter.getCount()-1,true);
    	    inRefresh = false;
    	}
    };
	
	private void updateView(View v, int month, int year) {
		if (disableUpdate) return;
		if (histoCoul == null) return;
	    Time t = new Time();
	    t.set(1, month-1, year); // mois de 0 à 11
	    t.normalize(false);
	    int pj = t.weekDay;
	    if (pj==0) {pj = 7;}
	    pj--;
	    TextView tv;
	    CoulItem ci = new CoulItem();
	    DayItem di;
	    int i;
	    for (i = 0; i<pj; i++) {
	    	tv = (TextView) v.findViewById(dayid[i]);
	    	if (tv!=null) {
	    	    //Log.i("upd",Integer.toString(month));
	    		tv.setText(" ");
	    		tv.setBackgroundColor(0x00000000);
	    	}
	    }
	    for (i = 0; i<t.getActualMaximum(Time.MONTH_DAY); i++) {
	    	tv = (TextView) v.findViewById(dayid[i+pj]);
	    	if (tv!=null) {
	    		tv.setText(Integer.toString(i+1));
	    		tv.setBackgroundColor(histoCoul.getColor(i+1, month, year));
	    		tv.setTextColor(histoCoul.getTextColor(i+1, month, year));
	    		di = histoCoul.findDay(i+1, month, year);
	    		if (di != null) ci.addNb(di.getCouleur());
	    	}
	    }
	    for (i = t.getActualMaximum(Time.MONTH_DAY)+pj; i<42; i++) {
	    	tv = (TextView) v.findViewById(dayid[i]);
	    	if (tv!=null) {
	    		tv.setText(" ");
	    		tv.setBackgroundColor(0x00000000);
	    	}
	    }
	    for (i = 0; i<6; i++) {
	    	tv = (TextView) v.findViewById(weekid[i]);
	    	if (tv!=null) {
	    		tv.setText('S'+Integer.toString(t.getWeekNumber()+i));
	    	}
	    }
	    TableRow tr;
	    tr = (TableRow) v.findViewById(R.id.tableRow5);
	    if (tr!=null) {
		    if (t.getActualMaximum(Time.MONTH_DAY)+pj>=29) {
		    	tr.setVisibility(View.VISIBLE);
		    } else {
		    	tr.setVisibility(View.GONE);
		    }
	    }
	    tr = (TableRow) v.findViewById(R.id.tableRow6);
	    if (tr!=null) {
		    if (t.getActualMaximum(Time.MONTH_DAY)+pj>=36) {
		    	tr.setVisibility(View.VISIBLE);
		    } else {
		    	tr.setVisibility(View.GONE);
		    }
	    }
	    tv = (TextView) v.findViewById(R.id.chn);
        tv.setBackgroundColor(CoulItem.getColor(CoulItem.BLEU));
        tv.setText(ci.getNbJours2(CoulItem.BLEU));
        tv.setTextColor(CoulItem.getTextColor(CoulItem.BLEU));
        tv = (TextView) v.findViewById(R.id.chp);
        tv.setBackgroundColor(CoulItem.getColor(CoulItem.BLANC));
        tv.setText(ci.getNbJours2(CoulItem.BLANC));
        tv.setTextColor(CoulItem.getTextColor(CoulItem.BLANC));
        tv = (TextView) v.findViewById(R.id.chc);
        tv.setBackgroundColor(CoulItem.getColor(CoulItem.ROUGE));
        tv.setText(ci.getNbJours2(CoulItem.ROUGE));
        tv.setTextColor(CoulItem.getTextColor(CoulItem.ROUGE));	    
	    v.setVisibility(View.VISIBLE);
	}
	
	private class HistoPagerAdapter extends PagerAdapter {
		private View spareView;
		private LayoutInflater inflater;
		
		public HistoPagerAdapter(LayoutInflater inflater) {
			this.inflater = inflater;
			spareView = null;
		}
		
		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
			spareView = (View) object;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			View v;
			if (spareView != null) {
				v = spareView;
				spareView = null;
			} else {
				v = inflater.inflate(R.layout.histo_calendar, (ViewGroup) container, false);
			}
			if (histoMonths != null) {
				int year = histoMonths.get(position)/100;
				int month = histoMonths.get(position)-year*100;
				updateView(v, month, year);
			}
			((ViewPager) container).addView(v,0);
			return v;
		}

		@Override
		public int getCount() {
			if (histoMonths == null) return 0;
			return histoMonths.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == ((View) arg1);
		}
		
	}
	
	/*private class LoadHistoThread extends AsyncTask<Void, Void, Void> {

		private Context context;
		private ProgressDialog progressDialog;
		
		public LoadHistoThread(Context context) {
			super();
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			progressDialog = null;
			if (histoCoul==null) progressDialog = ProgressDialog.show(context, "", "Chargement des données d'historique...");
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Log.d("HISTO","start 0");
	    		if (histoCoul==null) {
	    			Log.d("HISTO","start 1");
			    	if (itd.modeEJP) {parseEJP();} else {parse();}
			    	if (histoCoul != null) {
			    		Log.d("HISTO","start 2");
			    		histoMonths = histoCoul.getYearMonths();
			    		Log.d("HISTO",""+histoMonths.size());
			    	}
			    	Log.d("HISTO","start 3");
			    }
			}
			catch (Exception e) {
	    		  //Log.e("Error",e.getMessage());
	    	}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (progressDialog != null) progressDialog.dismiss();
			inRefresh = false;
			histoPagerAdapter.notifyDataSetChanged();
			viewPager.setCurrentItem(histoPagerAdapter.getCount()-1);
		}
		
	}*/
	
	public void parse() {
		try {
			histoCoul = new DayItems();
			histoCoul.initFromPref(pref);
			int yy,ymin,ymax;
			ymin = pref.getInt("histoMinYear", 100000);
			ymax = pref.getInt("histoMaxYear", 0);
			int lastdate = pref.getInt("histoLastDate", 0);
			Time ct = new Time();
        	ct.setToNow();
        	int cdate = ct.year*100*100+(ct.month+1)*100+(ct.monthDay);
			if (cdate <= lastdate) return;
			//if (true) return;
			//Log.d("http","connect");
			//Document doc = Jsoup.connect("http://bleuciel.edf.com/abonnement-et-contrat/les-prix/les-prix-de-l-electricite/option-tempo/l-historique-52426.html").get();
			Document doc = Jsoup.connect("http://edf-ejp-tempo.sfr-sh.fr/index.php?m=th").get();
			Elements annees = doc.select("option");
			for (Element annee : annees) {
			    //Log.i("annee",annee.id()+" "+annee.hasAttr("selected"));
			    yy = Integer.parseInt(annee.id());
			    boolean b = ((histoCoul.findDay(1, 9, yy) != null)&&(histoCoul.findDay(31, 8, yy+1) != null));
			    if ((!annee.hasAttr("selected"))&&(b)) { //((yy >= ymin)||(yy <= ymax))) {
			    	//Log.d("HISTO","skip "+annee.id());
			    	continue;
			    }
			    //Log.d("HISTO","load from web "+annee.id());
			    Document doc2 = (annee.hasAttr("selected") ? doc : Jsoup.connect("http://edf-ejp-tempo.sfr-sh.fr/index.php?m=th").data("selectAnnee",annee.id(),"am","form").post());
			    Element tb = doc2.select("table#tempo").first();
			    if (tb!=null) {
			    	Elements tbrows = tb.select("tr");
			    	int numrow = 0;
			    	for (Element row : tbrows) {
			    		if (numrow>0) {
			    			Elements jours = row.select("td");
			    			int nj = 1;
			    			int cl;
			    			int d;
			    			int m;
			    			int y;
			    			for (Element jour : jours) {
			    				if (!jour.className().equalsIgnoreCase("tempo_titre")) {
			    					cl = 0;
			    					if (jour.className().equalsIgnoreCase("tempo_b")) {
			    						cl = 1;
			    					}
			    					if (jour.className().equalsIgnoreCase("tempo_w")) {
			    						cl = 2;
			    					}
			    					if (jour.className().equalsIgnoreCase("tempo_r")) {
			    						cl = 3;
			    					}
			    					if (!jour.className().equalsIgnoreCase("tempo_nd")) {
			    						//Log.i(Integer.toString(numrow+8)+'/'+Integer.toString(nj),jour.className());
			    						d = nj;
			    						m = numrow+8;
			    						y = Integer.parseInt(annee.id());
			    						if (m>12) {m-=12;y++;}
			    						if (cl>0) {histoCoul.updateDay(d, m, y, cl);}
			    					}
			    					nj++;
			    				}
			    			}
			    		}
			    		numrow++;
			    	}
			    	//mHandler.sendEmptyMessage(0);
			    }
			}
			//Document doc = Jsoup.connect("http://edf-ejp-tempo.sfr-sh.fr/index.php?m=th").data("selectAnnee","2009","am","form").post();
			/*Elements divs = doc.select("th.tempo_titre");
			try {
			Log.d("année: ",divs.first().text());
			} catch (Exception e) {
				Log.e("erreur",e.getMessage());
			}*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//Log.d("exception",e.getMessage());
/*			CoulItem ci = new CoulItem();
	    	ci.setTitre("Erreur de connexion");
	    	ci.setDesc(e.getMessage());
	    	ci.setError(true);
	    	//ci.setTexte("Impossible d'accéder au site.");
	    	m_CoulItem.add(ci);*/
		}
	}

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
	
	private int extractDateFromStringJJ_MM_AAAA(String s) {
		int numMois = -1;
		int numAnnee = -1;
		int numJour = -1;
		String[] temp;
		  	String delimiter = "/";
		  	//String tmpMois;
		  	temp = s.replace(' ','/').split(delimiter);
		if (temp.length>=3) {
			numJour = toInteger(temp[temp.length-3]);
			numMois = toInteger(temp[temp.length-2])-1;
			numAnnee = toInteger(temp[temp.length-1]);
		}
		if ((numJour>=0)&&(numJour<=31)&&(numMois>=0)&&(numMois<=12)&&(numAnnee>=0)) {
			return (((numAnnee-2000)*100+numMois+1)*100+numJour);
		} else {return -1;}
	}
	
	public void parseEJP() {
		try {
			histoCoul = new DayItems();
			Time ct = new Time();
        	ct.setToNow();
        	ct.set(0,0,0,ct.monthDay,ct.month,ct.year);
        	ct.normalize(false);
        	Time t2 = new Time();
        	t2.set(0,0,0,1,4-1,ct.year);
        	t2.normalize(false);
        	int y = ct.year;
        	if (Time.compare(ct, t2)<0) { y--;}
        	t2.set(0,0,0,1,4-1,y);
        	t2.normalize(false);
        	boolean fini = false;
        	while (!fini) {
        		histoCoul.addDay(t2.monthDay, t2.month+1, t2.year, CoulItem.BLANC);
        		t2.monthDay++;
        		t2.normalize(false);
        		fini = (Time.compare(t2, ct)>=0);
        	}
        	if (itd.clAuj!=CoulItem.INCONNUE) {histoCoul.updateDay(ct.monthDay, ct.month+1, ct.year, itd.clAuj);}
			
			Document doc = Jsoup.connect("http://edf-ejp-tempo.sfr-sh.fr/index.php?m=eh").get();
			Element elt = doc.select("table.w_skinnedTable").first();
			int i;
			int d;
			Time dt;
			if (elt!=null) {
				Element elt2 = elt.select("tbody").first();
				if (elt2!=null) {
					Elements rows = elt2.select("tr");
					for (Element row : rows) {
						Elements cols = row.select("td");
						i = 0;
						for (Element col : cols) {
							if (i==itd.zoneEJP) {
								//Log.d("HISTO",col.text());
								d = extractDateFromStringJJ_MM_AAAA(col.text());
								if (d>0) {
									dt = itd.intToDate(d);
									//Log.d("HISTO",""+dt.monthDay+"-"+(dt.month+1)+"-"+dt.year);
									histoCoul.updateDay(dt.monthDay, dt.month+1, dt.year, CoulItem.ROUGE);
								}
							}
							i++;
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//Log.d("exception",e.getMessage());
		}
	}

	private void updateSpinners(int position) {
		int year = histoMonths.get(position)/100;
		int month = histoMonths.get(position)-year*100-1;
		if (s1!=null) {
			if (month != s1.getSelectedItemPosition()) lockupds1++;
			s1.setSelection(month);
		}
		if (s2!=null) {
		    int za;
			for(int z=0;z<s2.getCount();z++) {
				try {
					za = Integer.parseInt(s2.getItemAtPosition(z).toString());
				}
				catch (Exception e) {
					za = 0;
				}
				if (za == year) {
					if (z != s2.getSelectedItemPosition()) lockupds2++;
					s2.setSelection(z);
					break;
				}
			}
		}
	}
	
}
