package com.rvl.android.getnzb;

import java.util.ArrayList;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LocalNZBRowAdapter extends ArrayAdapter<String>{
	Activity context;
	private ArrayList<String> items = new ArrayList<String>();
	
	public LocalNZBRowAdapter(Activity activity, ArrayList<String> items){
		super(activity, R.layout.localfilelist, items);
		this.context = activity;
		this.items = items;
	}

	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater inflater = context.getLayoutInflater();
		View row = inflater.inflate(R.layout.localfilelist, null);
		final TextView filename = (TextView) row.findViewById(R.id.filename);
		filename.setText(items.get(position));
		
		((TextView) row.findViewById(R.id.size)).setText("123 MB");
		((TextView) row.findViewById(R.id.age)).setText("0.2 Days ago");

		return row;
	}

	
}