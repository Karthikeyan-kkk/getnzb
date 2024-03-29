package com.rvl.android.getnzb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MonitorHellaNZB extends Activity{
	public static HellaNZB HELLACONNECTION = new HellaNZB();
	
	public static int REFRESH_INTERVAL = 8000; // Refresh interval in ms.
	public boolean PAUSED = false;
	public static final int MENU_PREFS = 1;
	public static final int MENU_PAUSE = 2;
	public static final int MENU_STOPCURRENT = 3;
	public static final int MENU_REFRESH = 4;
	private final Handler handler = new Handler();
	public ListView hellaqueue;
	
	private Timer t;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	 	this.setRequestedOrientation(
    			ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.monitorhellanzb);
		SharedPreferences prefs = GetNZB.preferences;
		REFRESH_INTERVAL = Integer.parseInt(prefs.getString("updateNewsgrabberInterval", "8000"));
		if(HELLACONNECTION.CONNECTED == false) HELLACONNECTION.connect();	
		autoQueueRefresh();
	}
	
	public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.hellanzbcontextmenu, menu);
		super.onCreateContextMenu(menu, view, menuInfo);
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		String nzbitem = hellaqueue.getAdapter().getItem((int)info.id).toString();
		String[] values = nzbitem.split("#");
		
		switch(item.getItemId()){
		case R.id.moveDownHellaNZBFile:
			HELLACONNECTION.call("down",values[2]);
			manualQueueRefresh();
			Toast.makeText(this, "Moved item down...", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.moveUpHellaNZBFile:
			HELLACONNECTION.call("up",values[2]);
			manualQueueRefresh();
			Toast.makeText(this, "Moved item up...", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.moveTopHellaNZBFile:
			HELLACONNECTION.call("force",values[2]);
			manualQueueRefresh();
			Toast.makeText(this, "Moved item to top...", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.moveBottonHellaNZBFile:
			HELLACONNECTION.call("last",values[2]);
			manualQueueRefresh();
			Toast.makeText(this, "Moved item to bottom...", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.deleteHellaNZBFile:
			HELLACONNECTION.call("dequeue",values[2]);
			manualQueueRefresh();
			Toast.makeText(this, "Item removed...", Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
	
	
	
    public boolean onCreateOptionsMenu(Menu menu){
		//menu.add(0, MENU_PREFS, 0, "Preferences");
    	menu.add(0, MENU_REFRESH, 0, "Refresh");
    	menu.add(0, MENU_STOPCURRENT, 0, " Cancel Current ");
		menu.add(0, MENU_PAUSE, 0, "Pause");
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()){
    	case MENU_REFRESH:
    		manualQueueRefresh();
			Toast.makeText(this, "Refreshed queue...", Toast.LENGTH_SHORT).show();
    		return true;
    	case MENU_PAUSE:
    		pauseHellaNZB();
    		return true;
    	case MENU_STOPCURRENT:
    		stopCurrentDownload();
    		return true;
    	}
    	return false;
    }
  
    
    @SuppressWarnings("unchecked")
	public void pauseHellaNZB(){
		HashMap<String,Object> globalinfo = (HashMap<String, Object>) HELLACONNECTION.call("status");
		String ispaused = globalinfo.get("is_paused").toString();
		if(ispaused.equals("true")){
			HELLACONNECTION.call("continue");
			Toast.makeText(this, "HellaNZB continued...", Toast.LENGTH_SHORT).show();

		}
		else if(ispaused.equals("false")){
			HELLACONNECTION.call("pause");
			Toast.makeText(this, "HellaNZB paused...", Toast.LENGTH_SHORT).show();
			manualQueueRefresh();
		}
    }
	@SuppressWarnings("unchecked")
	public void stopCurrentDownload(){
		HashMap<String,Object> globalinfo = (HashMap<String, Object>) HELLACONNECTION.call("status");
		Object[] tt = (Object[]) globalinfo.get("currently_downloading");
		if(tt.length == 0){
			Toast.makeText(this, "Currently not downloading...", Toast.LENGTH_LONG).show();
			return;
		}
		else{
			HELLACONNECTION.call("cancel");
			Toast.makeText(this, "Current download canceled...", Toast.LENGTH_SHORT).show();
			manualQueueRefresh();
		}
	}
	
	public void updateCurrentDownloadScreen(String status){
		String values[] = status.split("#");

		int remaining = Integer.parseInt(values[2]) - Integer.parseInt(values[3]);
		
		((TextView) findViewById(R.id.currentFilename)).setText(values[1]);
		((TextView) findViewById(R.id.eta)).setText(values[5]);
		((TextView) findViewById(R.id.sizeRemaining)).setText(Integer.toString(remaining)+" MB / "+values[2]+" MB");
		((TextView) findViewById(R.id.speed)).setText(values[4]+" KB/s");
		((ProgressBar) findViewById(R.id.currentProgress)).setProgress(Integer.parseInt(values[6]));
		if(values[0].equals("true")){
			((TextView) findViewById(R.id.eta)).setText("Paused");
		}
		
	}
	
	
	// Get string with current status of HellaNZB server.
	// makeup:0 <Paused true of false>#
	//		  1 <Name of .nzb file currently downloading>#
	//        2 <Total size in MB of currently downloading>#
	//        3 <MB Remaining>#
	//        4 <Downloadspeed in KB>#
	//        5 <Estimated time of arrival>#
	//        6 <Percent complete of current download>
	
	@SuppressWarnings("unchecked")
	public String getHellaNZBCurrentStatus(){
		String status = "";
		Log.d(Tags.LOG,"- getHellaNZBStatus(): retrieving status.");
		HashMap<String,Object> globalinfo = (HashMap<String, Object>) HELLACONNECTION.call("status");
		Object[] tt = (Object[]) globalinfo.get("currently_downloading");
		if(tt.length == 0){
			return(globalinfo.get("is_paused").toString()+"#Currently not downloading.#0#0#--#--:--#0");
		}
		HashMap<String,Object> currdlinfo = (HashMap<String, Object>) tt[0];
		
		status += globalinfo.get("is_paused").toString() + "#";
		status += currdlinfo.get("nzbName").toString() + "#";
		status += currdlinfo.get("total_mb").toString() + "#";
		status += globalinfo.get("queued_mb").toString() + "#";
		status += globalinfo.get("rate").toString() + "#";
		status += convertEta((Integer) globalinfo.get("eta")) + "#";
		status += globalinfo.get("percent_complete").toString();
		
		
			
		return status;
	}
	
	@SuppressWarnings("unchecked")
	public void updateHellaNZBQueueStatus(){
		if(PAUSED) return;
		hellaqueue = (ListView) findViewById(R.id.hellanzbQueueList);
		ArrayList<String> items = new ArrayList<String>();
 		ArrayAdapter<String> QueueAdapter =  new HellaNZBQueueRowAdapter(this,items);
		hellaqueue.setCacheColorHint(00000000);
		hellaqueue.setAdapter(QueueAdapter);
		registerForContextMenu(hellaqueue);
		String name="";
		String size="";
		String id="";
		HashMap<String,Object> globalinfo = (HashMap<String, Object>) HELLACONNECTION.call("status");
		Object[] tt = (Object[]) globalinfo.get("queued");
		if(tt.length == 0){
			// No items in queue
			return;
		}
		for(int c=0;c<tt.length;c++){
			HashMap<String,Object> queueitem = (HashMap<String, Object>) tt[c];
			 
			id   = queueitem.get("id").toString();
			name = queueitem.get("nzbName").toString();
			// Sometimes size isn't available (yet)...
			if(queueitem.containsKey("total_mb")) size = queueitem.get("total_mb").toString();			
			else size = "--";
			
			items.add(name+"#"+size+" MB"+"#"+id);                                       
			
		}
		QueueAdapter.notifyDataSetChanged();
	}

	public static String convertEta(int secs) {
		int hours = secs / 3600,
		remainder = secs % 3600,
		minutes = remainder / 60,
		seconds = remainder % 60; 
		String disHour = (hours < 10 ? "0" : "") + hours,
		disMinu = (minutes < 10 ? "0" : "") + minutes ,
		disSec = (seconds < 10 ? "0" : "") + seconds ;

		return(disHour +":"+ disMinu+":"+disSec);
	}	
	
	public void manualQueueRefresh(){
		if(HELLACONNECTION.CONNECTED && !PAUSED){		
			updateCurrentDownloadScreen(getHellaNZBCurrentStatus());
			updateHellaNZBQueueStatus();
		}
	}
	
	public void autoQueueRefresh() {
		
		t = new Timer();
		t.schedule(new TimerTask() {
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						if(HELLACONNECTION.CONNECTED && !PAUSED){						
							updateCurrentDownloadScreen(getHellaNZBCurrentStatus());
							updateHellaNZBQueueStatus();
						}
					}
				});
			}
		}, 0, REFRESH_INTERVAL);
	}
	protected void onDestroy() {
		super.onDestroy();
	}
	protected void onPause(){
		Log.d(Tags.LOG,"MonitorHellaNZB pausing.");
		this.PAUSED = true;
	
		super.onPause();
	}
	protected void onResume(){
		Log.d(Tags.LOG,"MonitorHellaNZB resuming.");
		this.PAUSED = false;
		super.onResume();
	}


}