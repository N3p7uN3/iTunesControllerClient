package com.n3p7un3.itunescontroller;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingsview);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new Settings()).commit();
		//getFragmentManager().beginTransaction().show(new Settings()).commit();
		//Intent i = new Intent(this, Settings.class);
		//startActivityForResult(i, 0);
		
	}
}
