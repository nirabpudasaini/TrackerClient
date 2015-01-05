package org.kll.tracker;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.kll.tracker.R;

public class UserSettings extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		
	}
	
	

}
