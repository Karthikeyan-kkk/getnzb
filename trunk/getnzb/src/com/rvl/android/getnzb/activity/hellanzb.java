package com.rvl.android.getnzb.activity;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import com.rvl.android.getnzb.R;
import com.rvl.android.getnzb.getnzb;
import com.rvl.android.getnzb.tags;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class hellanzb extends Activity {

	public static URI uri;
	public static XMLRPCClient client;	
	public static boolean CONNECTED = false;
	public static HashMap<String,Object> hellareturn = null;
	public static final int MENU_PREFS = 0;
	public static final int MENU_QUIT = 1;
	public static final int CONNECT_OK = 1;
	public static final int CONNECT_FAILED_NO_SETTINGS = 2;
	public static final int CONNECT_FAILED_OTHER = 3;
	AlertDialog.Builder builder;
	AlertDialog alert;
	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(tags.LOG,"- Starting HellaNZB Activity!");
		builder = new AlertDialog.Builder(this);
		setContentView(R.layout.hellanzb);
		TextView statusbar = (TextView) findViewById(R.id.hellastatus);
		if(!CONNECTED) connect();
		listfiles();
	}
	
	
	public Dialog onCreateDialog(int id) {
		
		switch(id) {
	
		case CONNECT_FAILED_NO_SETTINGS:
			Log.d(tags.LOG,"Creating Alert Dialog 'no settings;");
			builder.setTitle("No HellaNZB hostname.");
			Log.d(tags.LOG, "set title");
			builder.setMessage("GetNZB is not correctly configured. Do it now?");
			Log.d(tags.LOG, "set message");
			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					showsettings();
				}
			});
			Log.d(tags.LOG, "set pos button");
			builder.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			Log.d(tags.LOG, "set neg button.");
			
			Log.d(tags.LOG,"Created builder.");
			alert = builder.create();
			Log.d(tags.LOG,"Assigned builder.");
			return alert;
		}
		return null;
	}
	
	

    public void button_handler_hellanzb(View v){
    	switch(v.getId()){
    
    	}
    }
    
	
	public int connect(){
 		Log.d(tags.LOG,"- hellanzb.connect()");
		Log.d(tags.LOG,"connect(): Getting preferences");
		SharedPreferences prefs = getnzb.preferences;
		String hellahost = prefs.getString("hellanzb_hostname", "");
		if(hellahost == ""){
			Log.d(tags.LOG,"connect(): Hostname is empty, settings not there?");
			showDialog(CONNECT_FAILED_NO_SETTINGS);
			
			return CONNECT_FAILED_NO_SETTINGS;
		}
		TextView statusbar = (TextView) findViewById(R.id.hellastatus);
		
		if(!hellahost.matches("(https?)://.+")) 
			hellahost = "http://" + hellahost;
		try {
			Log.d(tags.LOG,"connecty(): Creating URI: "+hellahost + ":" + prefs.getString("hellanzb_port", "8760"));
			uri = URI.create(hellahost + ":" + prefs.getString("hellanzb_port", "8760"));

			Log.d(tags.LOG,"connect(): Creating Client");
			client = new XMLRPCClient(uri.toURL());
			
			Log.d(tags.LOG,"Authenticating with:'"+prefs.getString("hellanzbpassword", "")+"'");
			client.setBasicAuthentication("hellanzb", prefs.getString("hellanzbpassword",""));
			
			Log.d(tags.LOG,"connecty(): Calling 'aolsay'");
			if(client.call("aolsay") != ""){
				String message = "Connected";
				statusbar.setText(message);
				CONNECTED = true;
				return CONNECT_OK;
			}
			
		} catch (MalformedURLException e) {
			Log.d(tags.LOG,"connect() failed: MalformedURLException:"+e.getMessage());
			return CONNECT_FAILED_OTHER;
		} catch (XMLRPCException e) {
			Log.d(tags.LOG,"connect() failed: XMLRPCException:"+e.getMessage());
			return CONNECT_FAILED_OTHER;
		}
		return CONNECT_FAILED_OTHER;
	}
	public void hellastatus(){
		TextView statusbar = (TextView) findViewById(R.id.hellastatus);
		Log.d(tags.LOG,"Calling status");
		hellareturn = (HashMap<String, Object>) hellanzbcall("status");
		statusbar.setText(hellareturn.get("is_paused").toString());
	}
	
    public void listfiles(){
    	if(!CONNECTED) connect();
    	Log.d(tags.LOG, "- hellanzb.listfiles()");
    	setContentView(R.layout.hellanzb);
    	TextView statusbar = (TextView) findViewById(R.id.hellastatus);
    	statusbar.setText("Local files. Click to upload:");
    	// -- Bind the itemlist to the itemarray with the arrayadapter
    	ArrayList<String> items = new ArrayList<String>();
    	ArrayAdapter<String> aa = new ArrayAdapter<String>(this,com.rvl.android.getnzb.R.layout.itemslist,items);
    	
    	ListView itemlist = (ListView) findViewById(R.id.filelist01);
    	itemlist.setCacheColorHint(00000000);
    			
    	itemlist.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
					String list[] = fileList();					
					new uploadfile(list[position]).execute(list[position]);
			}	
    	});
    	   	
    	itemlist.setAdapter(aa);
    	String list[] = fileList();
		for(int c=0;c<list.length;c++){
			items.add(list[c]);
			Log.d(tags.LOG,"List of local files: "+list[c]); 
		}
	
    	aa.notifyDataSetChanged();
    }
    
	public Object hellanzbcall(String command) {
 		Log.d(tags.LOG,"- hellanzb.hellanzbcall(c)");
		try {
			if(CONNECTED) {
				return client.call(command);
			} else{
				Log.d(tags.LOG,"hellanzbcall(): Not connected, connecting first.");
				connect();
				return client.call(command);
			}
		} catch(XMLRPCException e) {
			Log.e(tags.LOG, "hellanzbcall(): "+e.getMessage());
			CONNECTED = false;
		}
		return null;
	}

	public Object hellanzbcall(String command, String extra1) {
 		Log.d(tags.LOG,"- hellanzb.hellanzbcall(c,e)"); 	
		try {
			if(CONNECTED) {
				return client.call(command, extra1);
				
			} else{
				Log.d(tags.LOG,"hellanzbcall(): Not connected, connecting first.");
				connect();
				return client.call(command, extra1);
			}
		} catch(XMLRPCException e) {
			Log.e(tags.LOG, "hellanzbcall(): "+e.getMessage());
			CONNECTED = false;
		}
		return null;
	}
	
	public Object hellanzbcall(String command, String extra1, String extra2) {
 		Log.d(tags.LOG,"- hellanzb.hellanzbcall(c,e,e)");
		try {
			if(CONNECTED) {
				return client.call(command, extra1, extra2);
				
			} else{
				Log.d(tags.LOG,"hellanzbcall(): Not connected, connecting first.");
				connect();
				return client.call(command, extra1, extra2);
			}
		} catch(XMLRPCException e) {
			Log.e(tags.LOG, "hellanzbcall(): "+e.getMessage());
			CONNECTED = false;
		}
		return null;
	}

	private class uploadfile extends AsyncTask<String, Void, Void>{
 		
		ProgressDialog uploaddialog = new ProgressDialog(hellanzb.this);
		String filename;
		@SuppressWarnings("unused")
		uploadfile(final String name){
			this.filename = name;
		}
		
	   	protected void onPreExecute(){
			Log.d(tags.LOG,"- hellanzb.uploadfile.preExecute()");
    		this.uploaddialog.setMessage("Uploading '"+this.filename+"' to HellaNZB server...");
    		this.uploaddialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		this.uploaddialog.show();
    	}
		
		@Override
		protected Void doInBackground(String... params) {
			Log.d(tags.LOG,"- hellanzb.uploadfile.doInBackground()");
			String filename = params[0];
			Log.d(tags.LOG,"Complete filename: "+getFilesDir()+"/"+filename);
			File nzbfile = new File(getFilesDir()+"/"+filename);
			String filedata;
			try {
				filedata = readfile(nzbfile);
				HashMap<String, Object> response = (HashMap<String, Object>) hellanzbcall("enqueue", nzbfile.getName(), filedata);

			} catch (IOException e) {
				Log.d(tags.LOG,"uploadfile(): IOException: "+e.getMessage());
			}
			// Delete file after uploading...
			Log.d(tags.LOG,"Deleting file:"+filename);
			deleteFile(filename);
			return null;
		}
		protected void onPostExecute(final Void unused){
			Log.d(tags.LOG,"- hellanzb.uploadfile.onPostExecute()");
			this.uploaddialog.dismiss();
    		// Reload file list...
    		listfiles();
    		return;
		}

	}
	

	
	public static String readfile(File file) throws IOException {
		Log.d(tags.LOG,"readfile(): Converting file to string. ("+file.getAbsolutePath()+")");
        FileInputStream stream = new FileInputStream(file);
        try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc
                                .size());
                /* Instead of using default, pass in a decoder. */
                return Charset.defaultCharset().decode(bb).toString();
        } 
        finally 
        {
                stream.close();
        }
	}
	private void showsettings() {
		startActivity(new Intent(this, preferences.class));
	}
}