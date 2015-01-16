package com.example.infotempo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

public class CDJFragment extends Fragment {
	private View fragmentView;
	LinearLayout linear;
	ListView lv;
	private ArrayList<CoulItem> m_CoulItem = null;
    private CoulItemAdapter m_adapter;
    private int TimeHP, TimeHC;
    private String[] joursem = {"dim","lun","mar","mer","jeu","ven","sam"};
    public static final CharSequence[] itemsZoneEJP =
    	{"Nord", "Provence, Alpes, Côte d'Azur", "Ouest", "Sud"};
    public boolean needupdate;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (fragmentView != null) {
			View oldParent = (View) fragmentView.getParent();
			if(oldParent != container) ((ViewGroup)oldParent).removeView(fragmentView);
		} else {
		fragmentView = (LinearLayout) inflater.inflate(R.layout.coul_jour_list_layout, container, false);
		linear = (LinearLayout)fragmentView.findViewById(R.id.linearView);
	    lv = (ListView)fragmentView.findViewById(R.id.listView1);
	    m_CoulItem = new ArrayList<CoulItem>();
	    m_adapter = new CoulItemAdapter(this.getActivity(), R.layout.coulitemlayout, m_CoulItem);
        lv.setAdapter(this.m_adapter);

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            	try {
            	AlertDialog alertDialog;
            	alertDialog = new AlertDialog.Builder(CDJFragment.this.getActivity()).create();
            	CoulItem ci = (CoulItem) lv.getItemAtPosition(position);
            	if (ci!=null) {
            		alertDialog.setMessage(ci.getDesc());
            	}
            	alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, CDJFragment.this.getActivity().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    // click listener on the alert box
                    public void onClick(DialogInterface arg0, int arg1) {
                        // the button was clicked
                        arg0.dismiss();
                    }
                });
            	alertDialog.show();
            	}
            	catch (Exception e) {
            		//Log.e("exception",e.getMessage());
            	}
            	//Log.d("CLICK",Integer.toString(position));
            }
          });
		}
        if (needupdate) InfoTempoService.launchService(this.getActivity(), InfoTempoService.ACTION_APP_UPDATE);
		return fragmentView;
	}

    private String intToDateStr(int dt) {
    	Time tmp = InfoTempoData.intToDate(dt);
    	return joursem[tmp.weekDay]+" "+InfoTempoData.intToDate(dt).format("%d/%m/%Y");
    }
	
	public void updateView(InfoTempoData itd) {
    	TimeHP = itd.debutHP;
    	TimeHC = itd.finHP;
    	m_CoulItem.clear();
    	CoulItem ci = new CoulItem();
    	ci.setTitre("Aujourd'hui: le "+intToDateStr(itd.dtAuj));
    	ci.setDesc(itd.descAuj);
    	ci.setCouleur(itd.clAuj);
    	m_CoulItem.add(ci);
    	
    	ci = new CoulItem();
    	ci.setTitre("Demain: le "+intToDateStr(itd.dtDemain));
    	ci.setDesc(itd.descDemain);
    	ci.setCouleur(itd.clDemain);
    	m_CoulItem.add(ci);
    	
    	ci = new CoulItem();
    	ci.setTitre("Nombre de jours restants");
    	ci.setDesc(itd.descNbJours);
    	ci.setNbBleu(itd.n1, itd.t1);
    	ci.setNbBlanc(itd.n2, itd.t2);
    	ci.setNbRouge(itd.n3, itd.t3);
    	m_CoulItem.add(ci);
    	
    	m_adapter.notifyDataSetChanged();
    	
    	CheckBox cb = (CheckBox)fragmentView.findViewById(R.id.checkBoxAlert);
    	cb.setChecked(itd.alarmEnabled);
    	
    	TextView tv = (TextView)fragmentView.findViewById(R.id.bt1ZoneEJP);
    	tv.setVisibility(itd.modeEJP?View.VISIBLE:View.GONE);

    	tv.setText("Zone EJP: "+(itd.zoneEJP!=-1 ? itemsZoneEJP[itd.zoneEJP] : "non définie"));
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat();
    	dateFormat.applyPattern("'le 'dd/MM' à 'HH'h'mm");
    	Date date = new Date();
    	tv = (TextView)fragmentView.findViewById(R.id.tvLastUpd);
    	if (itd.lastUpdate == 0) {
        	tv.setText(": -");
    	} else {
        	date.setTime(itd.lastUpdate);
        	tv.setText(": "+dateFormat.format(date));
    	}
    	tv = (TextView)fragmentView.findViewById(R.id.tvNextUpd);
    	if (itd.nextUpdate == 0) {
        	tv.setText(": -");
    	} else {
	    	date.setTime(itd.nextUpdate);
	    	tv.setText(": "+dateFormat.format(date));
    	}
	}
	
	private class CoulItemAdapter extends ArrayAdapter<CoulItem> {

        private ArrayList<CoulItem> items;

        public CoulItemAdapter(Context context, int textViewResourceId, ArrayList<CoulItem> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)CDJFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.coulitemlayout, null);
                }
                CoulItem ci = items.get(position);
                if (ci != null) {
                        TextView tt = (TextView) v.findViewById(R.id.RowTitle);
                        if (tt != null) {
                              tt.setText(ci.getTitre());
                        }
                        
                        TextView tv;
                        LayoutParams lp;
                        int TimeDay = TimeHC - TimeHP;
                        
                    	LinearLayout ll = (LinearLayout) v.findViewById(R.id.linearLayout1);
                    	LinearLayout ll2 = (LinearLayout) v.findViewById(R.id.linearLayout3);
                    	
                        if (ci.isError()) {
                        	ll.setVisibility(View.GONE);
                        	ll2.setVisibility(View.GONE);
                        } else {
                        	ll2.setVisibility(View.VISIBLE);
	                        if (ci.isNbJours()) {
	                        	ll.setVisibility(View.GONE);
		                        tv = (TextView) v.findViewById(R.id.chn);
		                        tv.setBackgroundColor(CoulItem.getColor(CoulItem.BLEU));
		                        tv.setText(ci.getNbJours(CoulItem.BLEU));
		                        tv.setTextColor(CoulItem.getTextColor(CoulItem.BLEU));
		                        lp = (LayoutParams) tv.getLayoutParams();
		                        lp.weight = 8; 
		                        tv.setLayoutParams(lp);
		                        tv = (TextView) v.findViewById(R.id.chp);
		                        tv.setBackgroundColor(CoulItem.getColor(CoulItem.BLANC));
		                        tv.setText(ci.getNbJours(CoulItem.BLANC));
		                        tv.setTextColor(CoulItem.getTextColor(CoulItem.BLANC));
		                        lp = (LayoutParams) tv.getLayoutParams();
		                        lp.weight = 8; 
		                        tv.setLayoutParams(lp);
		                        tv = (TextView) v.findViewById(R.id.chc);
		                        tv.setVisibility(View.VISIBLE);
		                        tv.setBackgroundColor(CoulItem.getColor(CoulItem.ROUGE));
		                        tv.setText(ci.getNbJours(CoulItem.ROUGE));
		                        tv.setTextColor(CoulItem.getTextColor(CoulItem.ROUGE));
		                        lp = (LayoutParams) tv.getLayoutParams();
		                        lp.weight = 8; 
		                        tv.setLayoutParams(lp);
	                        } else {
	                        	ll.setVisibility(View.VISIBLE);
		                        tv = (TextView) v.findViewById(R.id.lhn);
		                        lp = (LayoutParams) tv.getLayoutParams();
		                        if (TimeHC<TimeHP) {
		                        	tv.setText(Integer.toString(TimeHC)+'h');
		                        	lp.weight = TimeHP-TimeHC;
		                        } else {
		                        	tv.setText("0h");
		                        	lp.weight = TimeHP;
		                        }
		                        tv.setLayoutParams(lp);
		                        
		                        tv = (TextView) v.findViewById(R.id.lhp);
		                        lp = (LayoutParams) tv.getLayoutParams();
		                        tv.setText(Integer.toString(TimeHP)+'h');
		                        if (TimeHC<TimeHP) {
		                        	lp.weight = 24 + TimeDay - 1;
		                        } else {
			                        lp.weight = TimeDay;
		                        }
		                        tv.setLayoutParams(lp);
		                        
		                        tv = (TextView) v.findViewById(R.id.lhc);
	                        	tv.setText(Integer.toString(TimeHC)+'h');
	                        	lp = (LayoutParams) tv.getLayoutParams();
	                        	tv.setVisibility(View.VISIBLE);
	                        	if (TimeHC<TimeHP) {
		                        	lp.weight = 1;
		                        } else {
		                        	lp.weight = TimeHC;
	                        	}
	                        	tv.setLayoutParams(lp);
		                        
		                        tv = (TextView) v.findViewById(R.id.chn);
		                        tv.setText("");
		                        tv.setBackgroundColor(CoulItem.getColor(0));
		                        lp = (LayoutParams) tv.getLayoutParams();
		                        if (TimeHC<TimeHP) {
		                        	lp.weight = TimeHP-TimeHC;
		                        } else {
		                        	lp.weight = TimeHP;
		                        }
		                        tv.setLayoutParams(lp);
		                        tv = (TextView) v.findViewById(R.id.chp);
		                        tv.setBackgroundColor(ci.getColor());
		                        tv.setText(ci.getTexte());
		                        tv.setTextColor(ci.getTextColor());
		                        lp = (LayoutParams) tv.getLayoutParams();
		                        if (TimeHC<TimeHP) {
		                        	lp.weight = 24 + TimeDay;
		                        } else {
		                        	lp.weight = TimeDay;
		                        }
		                        tv.setLayoutParams(lp);
		                        tv = (TextView) v.findViewById(R.id.chc);
		                        tv.setText("");
		                        tv.setBackgroundColor(CoulItem.getColor(0));
		                        if (TimeHC<TimeHP) {
		                        	tv.setVisibility(View.GONE);
		                        } else {
		                        	tv.setVisibility(View.VISIBLE);
		                        	lp = (LayoutParams) tv.getLayoutParams();
		                        	lp.weight = TimeHC; 
		                        	tv.setLayoutParams(lp);
		                        }
	                        }   
                        }
                }
                return v;
        }
	}
	
}
