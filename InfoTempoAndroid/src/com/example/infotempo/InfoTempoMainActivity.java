package com.example.infotempo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.CheckBox;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class InfoTempoMainActivity extends SherlockFragmentActivity implements ActionBar.TabListener {
	private SharedPreferences pref;
	private InfoTempoData itd;
	private ActionBar.Tab cdjTab;
	private CDJFragment cdjFragment;
	private ActionBar.Tab histoTab;
	private HistoFragment histoFragment;
	private boolean inRefresh;
	private ProgressDialog progressDialog;
	private ActionBar mActionBar;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	    pref = PreferenceManager.getDefaultSharedPreferences(this);
	    itd = new InfoTempoData();
	    inRefresh = false;
        
        mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mActionBar.setHomeButtonEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(true);
        cdjTab = mActionBar.newTab();
        cdjTab.setText("Couleur du jour");
        cdjTab.setTabListener(this);
        mActionBar.addTab(cdjTab);
        histoTab = mActionBar.newTab();
        histoTab.setText("Historique");
        histoTab.setTabListener(this);
        mActionBar.addTab(histoTab);
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(InfoTempoService.ACTION_START_UPDATE);
        intentfilter.addAction(InfoTempoService.ACTION_UPDATE_UI);
        intentfilter.addAction(InfoTempoService.ACTION_UPDATE_MODE);
        intentfilter.addAction(InfoTempoService.ACTION_UPDATE_HISTO);
        registerReceiver(broadcastReceiver, intentfilter);
        //refreshData();
        
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
	}

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction transaction) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction transaction) {
        if (tab==cdjTab) {
        	if (cdjFragment==null) {
        		//Log.d("tab","create CDJFragment");
        		cdjFragment = new CDJFragment();
        		if (cdjFragment != null) cdjFragment.needupdate = true;
        		transaction.add(R.id.tabContent, cdjFragment);
        	} else {
        		//Log.d("tab","attach CDJFragment");
        		transaction.attach(cdjFragment);
        	}
        } else {
        	if (histoFragment==null) {
        		//Log.d("tab","create HistoFragment");
        		histoFragment = new HistoFragment();
        		transaction.add(R.id.tabContent, histoFragment);
        	} else {
        		//Log.d("tab","attach HistoFragment");
        		transaction.attach(histoFragment);
        	}
        	//InfoTempoService.launchService(this, InfoTempoService.ACTION_UPDATE_HISTO);
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
    	if (tab==cdjTab) {
    		transaction.detach(cdjFragment);
    	} else {
    		transaction.detach(histoFragment);
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.itmenu, menu);
		return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    itd.loadData(pref);
	    menu.findItem(R.id.itemmode).setTitle(itd.modeEJP?"Mode tempo":"Mode EJP");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		    case R.id.itempref:
		    	startActivityForResult(new Intent(this, ParamActivity.class),3);
		    	return true;
		    case R.id.itemmode:
		    	itd.loadData(pref);
		    	itd.setMode(!itd.modeEJP);
		    	itd.saveModeEJP(pref);
		    	itd.saveMainData(pref);
		    	itd.saveRetryCount(pref);
		    	itd.resetLastAlertDate(pref);
		    	//((InfoTempoApplication)getApplication()).HistoCoul = null;
		    	if (cdjFragment != null) cdjFragment.needupdate = true;
		    	if (histoFragment != null) histoFragment.histoCoul = null;
		    	//if (mActionBar.getSelectedTab() == histoTab) {
		    	//	InfoTempoService.launchService(this, InfoTempoService.ACTION_UPDATE_HISTO);
		    	//} else {
		    		InfoTempoService.launchService(this, InfoTempoService.ACTION_UPDATE_MODE);
		    	//}
		    	return true;
		    case R.id.iteminfo:
		    	startActivity(new Intent(this, AboutActivity.class));
		    	return true;
		    case R.id.itemrefresh:
		    	if (mActionBar.getSelectedTab() == histoTab) {
		    		SharedPreferences.Editor editor = pref.edit();
		    		editor.putInt("histoLastDate", 0);
		    		editor.commit();
		    		InfoTempoService.launchService(this, InfoTempoService.ACTION_UPDATE_HISTO);
		    	} else refreshData();
		    	return true;
		    default:
		    	return super.onOptionsItemSelected(item);
		}
	}

	private void startUpdate() {
		if (!inRefresh) {
			inRefresh = true;
			progressDialog = ProgressDialog.show(this, "", 
	                "Actualisation des données du jour...", true);
		}
	}
	
	private void refreshData() {
		if (!inRefresh) {
			InfoTempoService.launchService(this, InfoTempoService.ACTION_APP_UPDATE);
		}
	}
	
	public void onChkAlertClick(View view) {
		itd.alarmEnabled = ((CheckBox)view).isChecked();
		itd.saveAlert(pref);
		refreshData();
	}
	
	public void onBtZoneEJPClick(View view) {
		selectZoneEJP();
	}
	
	public void selectZoneEJP() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zone EJP");
        builder.setMessage("Vous pouvez modifier la zone EJP dans les paramètres.");
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
		        startActivityForResult(new Intent(InfoTempoMainActivity.this, ParamActivity.class),3);
			}
		});

        builder.setCancelable(false);
        builder.create().show();
	}
	
    private void updateUI(Intent intent) {
    	itd.loadData(pref);
        if (cdjFragment != null) cdjFragment.updateView(itd);
    	progressDialog.dismiss();
    	if (cdjFragment != null) cdjFragment.needupdate = false;
    	inRefresh = false;
    	if (mActionBar.getSelectedTab() == histoTab) {
    		InfoTempoService.launchService(this, InfoTempoService.ACTION_UPDATE_HISTO);
    	}
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
	    	if (cdjFragment != null) cdjFragment.needupdate = true;
	    	if (histoFragment != null) histoFragment.histoCoul = null;
			refreshData();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}   
	
	private void updateMode() {
		itd.loadData(pref);
		if (itd.modeEJP&&(itd.zoneEJP==-1)) {
			selectZoneEJP();
		} else {
			refreshData();
		}
	}
	
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if (intent.getAction().equals(InfoTempoService.ACTION_START_UPDATE)) {
        		try {
        			startUpdate();
        		}
        		catch (Exception e) {
        			
        		}
        	} else {
        		if (intent.getAction().equals(InfoTempoService.ACTION_UPDATE_UI)) {
        			try {
        				updateUI(intent);
        			}
        			catch (Exception e) {
        				
        			}
        		} else {
        			if (intent.getAction().equals(InfoTempoService.ACTION_UPDATE_MODE)) {
        				try {
        					updateMode();
        				}
        				catch (Exception e) {
        					
        				}
        			}
        		}
        	}
        	if (intent.getAction().equals(InfoTempoService.ACTION_UPDATE_HISTO)) {
        		//Log.d("UPDATE","HISTO");
    			try {
    				if (histoFragment != null) {
    					//Log.d("UPDATE","GO");
    					histoFragment.update(InfoTempoMainActivity.this);
    				}
    			}
    			catch (Exception e) {
    				
    			}
        	}
        }
    };

}
