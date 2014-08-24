package com.n3p7un3.itunescontroller;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class Settings extends PreferenceFragment {

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//addPreferencesFromReso
		this.getPreferenceManager().setSharedPreferencesName("com.n3p7un3.itunescontroller.prefs");
		addPreferencesFromResource(R.xml.preferences);
		
	}
}
