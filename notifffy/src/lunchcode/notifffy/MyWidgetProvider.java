package lunchcode.notifffy;

import lunchcode.notifffy.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MyWidgetProvider extends AppWidgetProvider {

	// our actions for our buttons
	public static String ACTION_WIDGET_REFRESH = "ActionReceiverRefresh";
	public static String ACTION_WIDGET_SETTINGS = "ActionReceiverSettings";
	public static String ACTION_WIDGET_ACTIVITY = "ActionReceiverActivity";
	public static String ACTION_WIDGET_USER = "ActionReceiverUser";
	public static String ACTION_WIDGET_MAIN = "ActionReceiverMain";
	
	public void onEnabled(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		// SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(_context);
		// String pref_username = sharedPref.getString("user_name", "");
		
		// Binding actions to the buttons
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget_layout);

		Intent active = new Intent(context, MyWidgetProvider.class);
		PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);

		active = new Intent(context, MyWidgetProvider.class);
		active.setAction(ACTION_WIDGET_REFRESH);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0,active, 0);
		remoteViews.setOnClickPendingIntent(R.id.refresh_button,actionPendingIntent);

		active = new Intent(context, MyWidgetProvider.class);
		active.setAction(ACTION_WIDGET_ACTIVITY);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0,active, 0);
		remoteViews.setOnClickPendingIntent(R.id.activity_button,actionPendingIntent);
		
		active = new Intent(context, MyWidgetProvider.class);
		active.setAction(ACTION_WIDGET_SETTINGS);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0,active, 0);
		remoteViews.setOnClickPendingIntent(R.id.widget_startup_button,actionPendingIntent);

		active = new Intent(context, MyWidgetProvider.class);
		active.setAction(ACTION_WIDGET_USER);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0,active, 0);
		remoteViews.setOnClickPendingIntent(R.id.user_button,actionPendingIntent);

		active = new Intent(context, MyWidgetProvider.class);
		active.setAction(ACTION_WIDGET_MAIN);
		actionPendingIntent = PendingIntent.getBroadcast(context, 0,active, 0);
		remoteViews.setOnClickPendingIntent(R.id.logo, actionPendingIntent);

		// Commit widget changes
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
		
		// Refreshing procedures ...

		// Get all ids
		ComponentName thisWidget = new ComponentName(context,MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		// Build the intent to call the service
		Intent refreshIntent = new Intent(context.getApplicationContext(),UpdateWidgetService.class);
		refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,allWidgetIds);

		// Update the widgets via the service
		context.startService(refreshIntent);

	}

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d("myINFO", intent.getAction());

		if (intent.getAction().equals(ACTION_WIDGET_REFRESH)) {

			Toast.makeText(context, "Refreshing ...", Toast.LENGTH_SHORT).show();
			refreshActivityButton(context);
			
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	    	
	    	// Get all ids
		    ComponentName thisWidget = new ComponentName(context,MyWidgetProvider.class);
		    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		
			// Build the intent to call the service
			Intent refreshIntent = new Intent(context,UpdateWidgetService.class);
			refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);    

			// Update the widgets via the service
			context.startService(refreshIntent);

		} else if (intent.getAction().equals(ACTION_WIDGET_SETTINGS)) {

			Context c = context.getApplicationContext();

			if (Build.VERSION.SDK_INT < 11) {
				Intent settingsIntent = new Intent(c,lunchcode.notifffy.DeprSettingsActivity.class);
				settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				c.startActivity(settingsIntent);
			} else {
				Intent settingsIntent = new Intent(c,lunchcode.notifffy.SettingsActivity.class);
				settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				c.startActivity(settingsIntent);
			}
			
		} else if (intent.getAction().equals(ACTION_WIDGET_ACTIVITY)) {

			// Refreshing activity button to initial state
			refreshActivityButton(context);
			
			// Opening browser window
			Toast.makeText(context, "Launching incomming activity ...",Toast.LENGTH_SHORT).show();
			callBrowserWindow(context, "activity/incoming");

		} else if (intent.getAction().equals(ACTION_WIDGET_MAIN)) {

			Toast.makeText(context, "Launching dribbble ...",Toast.LENGTH_SHORT).show();
			refreshActivityButton(context);

			// Open Browser Window
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.dribbble.com"));
			browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(browserIntent);

		} else if (intent.getAction().equals(ACTION_WIDGET_USER)) {

			Toast.makeText(context, "Launching profile page ...",Toast.LENGTH_SHORT).show();
			refreshActivityButton(context);
			
			callBrowserWindow(context, "");

		}
		
		super.onReceive(context, intent);
	
	}

	public void callBrowserWindow(Context context, String subj) {

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String pref_username = sharedPref.getString("user_name", "");

		String outputURL = "http://www.dribbble.com/" + pref_username + "/" + subj;

		// Open Browser Window
		Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(outputURL));
		browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(browserIntent);
	} 
	
	public void refreshActivityButton(Context context) {
		ComponentName myWidget;
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		myWidget = new ComponentName(context,MyWidgetProvider.class);
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget_layout);
		remoteViews.setImageViewResource(R.id.activity_button,R.drawable.activity_standby);
		manager.updateAppWidget(myWidget, remoteViews);
	}

}
