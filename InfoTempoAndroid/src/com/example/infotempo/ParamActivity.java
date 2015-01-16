package com.example.infotempo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

public class ParamActivity extends SherlockActivity {

	private ArrayList<Integer> m_ParamItem = null;
    private ParamItemAdapter m_adapter;
    private Uri[] mSelectedRingTone;
    private SharedPreferences pref;
    private InfoTempoData itd;
    private HashMap<Integer,Integer> zonesEJP;
    private HashMap<String,String> depNomNum;
    private HashMap<String,String> depNumNom;
    private HashMap<String,Integer> dep_reg;
    private HashMap<Integer,String> regions;
    final CharSequence[] itemsZoneEJP = {"Nord", "Provence, Alpes, Côte d'Azur", "Ouest", "Sud"};
	private ArrayList<String> depArray;
    

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.param_layout);
	    
	    pref = PreferenceManager.getDefaultSharedPreferences(this);
	    itd = new InfoTempoData();
	    itd.loadData(pref);
	    
	    ActionBar mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(true);
        
        if (itd.modeEJP) {
        	zonesEJP = new HashMap<Integer, Integer>();
        	zonesEJP.put(31, 0);
        	zonesEJP.put(22, 0);
        	zonesEJP.put(23, 0);
        	zonesEJP.put(25, 0);
        	zonesEJP.put(21, 0);
        	zonesEJP.put(41, 0);
        	zonesEJP.put(42, 0);
        	zonesEJP.put(43, 0);
        	zonesEJP.put(11, 0);
        	zonesEJP.put(24, 0);
        	zonesEJP.put(54, 0);
        	zonesEJP.put(93, 1);
        	zonesEJP.put(53, 2);
        	zonesEJP.put(52, 2);
        	zonesEJP.put(26, 3);
        	zonesEJP.put(72, 3);
        	zonesEJP.put(74, 3);
        	zonesEJP.put(83, 3);
        	zonesEJP.put(82, 3);
        	zonesEJP.put(73, 3);
        	zonesEJP.put(91, 3);
        	
        	depNomNum = new HashMap<String, String>();
        	depNumNom = new HashMap<String, String>();
        	depArray = new ArrayList<String>();
        	dep_reg = new HashMap<String, Integer>();
        	String[] tmp;
        	try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("departements.txt")));
				String mLine = reader.readLine();
				while (mLine != null) {
					tmp = mLine.split(";");
					if (tmp.length == 4) {
						depNomNum.put(tmp[1], tmp[2]);
						depNumNom.put(tmp[2], tmp[1]);
						depArray.add(tmp[1]);
						try {
							dep_reg.put(tmp[2], Integer.valueOf(tmp[3]));
						} catch (NumberFormatException ne) {
							ne.printStackTrace();
						}
					}
					mLine = reader.readLine();
				}	
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
            Collections.sort(depArray);
        	regions = new HashMap<Integer,String>();
        	try {
    			BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("regions.txt")));
    			String mLine = reader.readLine();
    			while (mLine != null) {
    				tmp = mLine.split(";");
    				if (tmp.length == 2) {
    					try {
        					regions.put(Integer.valueOf(tmp[0]), tmp[1]);
    					} catch (NumberFormatException ne) {
    						ne.printStackTrace();
    					}
    				}
    				mLine = reader.readLine();
    			}	
    			reader.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        }
        
	    mSelectedRingTone = new Uri[2];
	    mSelectedRingTone[0] = (itd.notifSound==null?(Uri)null:(Uri)Uri.parse(itd.notifSound));
	    mSelectedRingTone[1] = (itd.notifSound2==null?(Uri)null:(Uri)Uri.parse(itd.notifSound2));
	    
	    ListView lv;
	    lv = (ListView)findViewById(R.id.paramListView);
	    m_ParamItem = new ArrayList<Integer>();
	    m_ParamItem.add(0);
	    m_ParamItem.add(1);
	    m_ParamItem.add(2);
	    if (itd.modeEJP) m_ParamItem.add(3);
	    m_adapter = new ParamItemAdapter(this, R.layout.param_item_layout, m_ParamItem);
        lv.setAdapter(this.m_adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                int position, long id) {
            	CheckBox cb = (CheckBox) view.findViewById(R.id.paramNotifyCheckBox);
            	if (position<2) {
	            	if ((cb!=null)&&(cb.isEnabled())) {
		            	Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		            	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
		            	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
		            	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
		            	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
		            	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mSelectedRingTone[position]);
		            	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Sonnerie de notification");
		            	startActivityForResult(intent, position);
	            	}
            	} else {
            		if (position==2) {
	            		if ((cb!=null)&&(cb.isEnabled())) {
	            			cb.setChecked(!cb.isChecked());
	            			itd.vibrate = cb.isChecked();
	            			itd.saveVibrate(pref);
	            		}
            		} else {
            			AlertDialog.Builder builder = new AlertDialog.Builder(ParamActivity.this);
            	        builder.setTitle("Sélectionnez votre département");
            	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ParamActivity.this, android.R.layout.select_dialog_item, depArray);
            	        builder.setSingleChoiceItems(adapter, itd.zoneEJP, new DialogInterface.OnClickListener(){
            	            public void onClick(DialogInterface dialogInterface, int item) {
            	                dialogInterface.dismiss();
            	                itd.loadData(pref);
            	                if (!pref.getString("depejp", "").equals(depNomNum.get(depArray.get(item)))) {
               	                    itd.zoneEJP = getZoneEJPFromDep(depNomNum.get(depArray.get(item)));
               	                    SharedPreferences.Editor editor = pref.edit();
               	                    editor.putString("depejp", depNomNum.get(depArray.get(item)));
               	                    editor.commit();
               	                    itd.saveZoneEJP(pref);
               	    		    	itd.setMode(itd.modeEJP);
               	    		    	itd.saveMainData(pref);
               	    		    	itd.saveRetryCount(pref);
               	    		    	itd.resetLastAlertDate(pref);
            	                }
            	                m_adapter.notifyDataSetChanged();
            	                setResult(Activity.RESULT_OK);
            	                return;
            	            }
            	        });
            	        builder.setCancelable(false);
            	        builder.create().show();
            		}
            	}
            }
          });
        m_adapter.notifyDataSetChanged();
        setResult(Activity.RESULT_CANCELED);
	}

	private int getZoneEJPFromDep(String dep) {
		Integer reg = dep_reg.get(dep);
		if (reg != null) {
			Integer zone = zonesEJP.get(reg);
			if (zone != null) {
				return zone;
			}
		}
		return -1;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			setResult(Activity.RESULT_OK);
			Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			mSelectedRingTone[requestCode] = uri;
			if (uri != null) {
				if (requestCode==0) {
					itd.notifSound = uri.toString();
				} else {
					itd.notifSound2 = uri.toString();
				}
			} else {
				if (requestCode==0) {
					itd.notifSound = null;
				} else {
					itd.notifSound2 = null;
				}
			}
			itd.saveNotifSounds(pref);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onChkParamNotifyClick(View v) {
		CheckBox cb = (CheckBox) v;
		Integer i = (Integer)(v.getTag());
		if (i.intValue()==0) {
			itd.alarmEnabled = cb.isChecked();
			itd.saveAlert(pref);
		} else {
			if (i.intValue()==1) {
				itd.alarm2Enabled = cb.isChecked();
				itd.saveAlert2(pref);
			} else {
				if (i.intValue()==2) {
					itd.vibrate = cb.isChecked();
					itd.saveVibrate(pref);
				}
			}
		}
		setResult(Activity.RESULT_OK);
	}
	
	private class ParamItemAdapter extends ArrayAdapter<Integer> {

        private ArrayList<Integer> items;

        public ParamItemAdapter(Context context, int textViewResourceId, ArrayList<Integer> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.param_item_layout, null);
                }
                Integer i = items.get(position);
                if (i != null) {
                		CheckBox cb = (CheckBox) v.findViewById(R.id.paramNotifyCheckBox);
                		ImageView iv = (ImageView) v.findViewById(R.id.imageViewParamExpander);
                		LinearLayout ll = (LinearLayout) v.findViewById(R.id.paramLines);
                		TextView l1 = (TextView) v.findViewById(R.id.paramTextLine1);
                		TextView l2 = (TextView) v.findViewById(R.id.paramTextLine2);
                		if (cb!=null) {
                			cb.setTag(i);
	                		if (i.intValue()==0) {
	                			if (ll!=null) {ll.setVisibility(View.INVISIBLE);}
	                			cb.setText("Notifier les jours rouges");
	                			cb.setChecked(itd.alarmEnabled);
	                			cb.setVisibility(View.VISIBLE);
	                			cb.setEnabled(true);
	                			if (iv!=null) {iv.setVisibility(View.VISIBLE);}
	                		} else {
	                			if (i.intValue()==1) {
	                				if (ll!=null) {ll.setVisibility(View.INVISIBLE);}
		                			cb.setText("Notifier les jours blancs");
		                			cb.setChecked(itd.alarm2Enabled);
		                			cb.setVisibility(View.VISIBLE);
		                			cb.setEnabled(!itd.modeEJP);
		                			if (iv!=null) {iv.setVisibility(View.VISIBLE);}
	                			} else {
	                				if (i.intValue()==2) {
	                					if (ll!=null) {ll.setVisibility(View.INVISIBLE);}
	                					cb.setText("Vibreur");
	                					cb.setChecked(itd.vibrate);
	                					cb.setVisibility(View.VISIBLE);
	                					cb.setEnabled(true);
	                					if (iv!=null) {iv.setVisibility(View.INVISIBLE);}
	                				} else {
	                					cb.setText("Zone EJP");
	                					cb.setVisibility(View.INVISIBLE);
	                					cb.setEnabled(false);
	                					if (l1!=null) {
	                						l1.setText("Zone EJP");
	                					}
	                					if (l2!=null) {
	                						String dep = pref.getString("depejp", "");
	                						int zejp = getZoneEJPFromDep(dep);
	                						if (zejp == -1) {
	                							l2.setText("Zone non définie");
	                						} else {
	                							l2.setText(depNumNom.get(dep)+" ("+regions.get(dep_reg.get(dep))+"): zone "+itemsZoneEJP[itd.zoneEJP]);
               							        l2.setSelected(true);
	                						}
	                					}
	                					if (ll!=null) {ll.setVisibility(View.VISIBLE);}
	                					if (iv!=null) {iv.setVisibility(View.VISIBLE);}
	                				}
	                			}
	                		}
                		}
                }
                return v;
        }
	}

}
