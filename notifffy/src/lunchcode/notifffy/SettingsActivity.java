package lunchcode.notifffy;

import lunchcode.notifffy.R;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notifffy_settings);

		// Display the fragment as the main content.
		getFragmentManager()
			.beginTransaction()
			.replace(android.R.id.content, new SettingsFragment())
			.commit();
	}

	public void onBackPressed() {

		Context c = getApplicationContext();
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(c);

		// Get all ids
		ComponentName thisWidget = new ComponentName(c,MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// Build the intent to call the service
		Intent refreshIntent = new Intent(c.getApplicationContext(),UpdateWidgetService.class);
		refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);    

		// Update the widgets via the service
		c.startService(refreshIntent);
		finish();

	}

	public static class SettingsFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.prefs);
		}
	}

}
