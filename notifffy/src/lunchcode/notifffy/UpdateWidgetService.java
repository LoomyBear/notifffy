//Hello fuckin' World!

package lunchcode.notifffy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import lunchcode.notifffy.R;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {

	int notificationID = 1;
	@Override
	public void onStart(Intent intent, int startId) {

		Log.d("myINFO", "UPDATE IS STARTED");
		
		//callNotification("New like received", "notifffy","Someone liked your shot! Tap to view ...");
		//int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

		// Checking if username still empty
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String pref_username = sharedPref.getString("user_name", "");

		if ( !pref_username.equals("") ) {

			// Calling preloader layout
			callPreloaderLayout();

			// Launching API thread
			new Thread(new Runnable() {
				public void run() {

					// Getting the username from the preferences
					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					String pref_username = sharedPref.getString("user_name", "");
					String url = "http://api.dribbble.com/players/"+ pref_username;

					HttpClient httpclient = new DefaultHttpClient();
					HttpGet httpget = new HttpGet(url);
					HttpResponse response;

					try {
						Log.d("myINFO", "Try is started");
						response = httpclient.execute(httpget);
						HttpEntity entity = response.getEntity();
						InputStream instream = entity.getContent();
						String line = "";
						BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
						StringBuilder json = new StringBuilder();
						while ((line = reader.readLine()) != null) {
							json.append(line);
						}

						JSONObject jsonObj = new JSONObject(json.toString());

						int likes_received = jsonObj.getInt("likes_received_count");
						int comments_received = jsonObj.getInt("comments_received_count");
						int followers = jsonObj.getInt("followers_count");
						String avatar_URL = jsonObj.getString("avatar_url");

						instream.close();

						setMyInt(likes_received, comments_received,followers, avatar_URL);

						// Calling default layout
						callDefaultLayout();

						Log.d("myINFO", "Try is ended");

					} catch (Exception e) {
						//callDefaultLayout();
						Log.d("myINFO", e.getMessage());
					}

				}

			}).start();
			
			// Auto refresh launch
			Boolean refreshEnabled = sharedPref.getBoolean("notification_switch", true);
			if ( refreshEnabled == true ) {
				launchUpdate();
			}
			
		} else {
			Log.d("myINFO", "No username found, update is not initiated!");
			callNoUsernameLayout();
		}

	}


	// Function to compare incomind data with the database
	private void setMyInt(int likes, int comms, int followers, String userpic) {

		DBAdapter db = new DBAdapter(this.getApplicationContext());
		db.open();

		// Checking if notifications are enabled
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Boolean pref_notification_switch = sharedPref.getBoolean("notification_switch", true);

		// Call DB
		Cursor cur = db.getAllActivities();
		if (cur.moveToFirst()) {
			long rowID = cur.getLong(0);

			int likesDB = cur.getInt(cur.getColumnIndex("current_likes"));
			int commsDB = cur.getInt(cur.getColumnIndex("current_comms"));
			int followersDB = cur.getInt(cur
					.getColumnIndex("current_followers"));
			String userPicDB = cur.getString(cur.getColumnIndex("userpic_url"));

			changeUserPic(userpic);

			if (likesDB < likes || commsDB < comms || followersDB < followers
					|| !userpic.equalsIgnoreCase(userPicDB)) {
				db.updateActivity(rowID, likes, comms, followers, userpic);

				// Inserting notification into widget
				ComponentName myWidget;
				AppWidgetManager manager = AppWidgetManager.getInstance(this.getApplicationContext());
				myWidget = new ComponentName(this.getApplicationContext(),MyWidgetProvider.class);
				RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),R.layout.widget_layout);
				remoteViews.setImageViewResource(R.id.activity_button,R.drawable.activity_notification);
				manager.updateAppWidget(myWidget, remoteViews); 

				// Checking if notifications are enabled in preferences
				if (pref_notification_switch == true) {
					// New like notification
					if (likesDB < likes) {
						callNotification("New like received", "notifffy","Someone liked your shot! Tap to view ...");
					}
					// New comment notification
					if (commsDB < comms) {
						callNotification("Comment received", "notifffy","New incoming comment! Tap to view ...");
					}
					// New follower notification
					if (followersDB < followers) {
						callNotification("You've been followed", "notifffy", "You have a new follower! Tap to view ...");
					}
				}	
			}

		} else {
			changeUserPic(userpic);
			db.insertActivity(likes, comms, followers, userpic);
		}

		db.close();

	}

	// Function to change userpic
	public void changeUserPic(String userpic) {
		URL url_value;

		try {
			ComponentName myWidget;
			AppWidgetManager manager = AppWidgetManager.getInstance(this.getApplicationContext());
			myWidget = new ComponentName(this.getApplicationContext(),MyWidgetProvider.class);

			// Checking if username still empty
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String pref_username = sharedPref.getString("user_name", "");

			if ( !pref_username.equals("") ) {
				RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget_layout);
				url_value = new URL(userpic);
				if (remoteViews != null) {
					Bitmap userpic_img = BitmapFactory.decodeStream(url_value.openConnection().getInputStream());
					Bitmap scaled_userpic_img = scaleBitmap(userpic_img, 50, 50);
					remoteViews.setImageViewBitmap(R.id.user_button,scaled_userpic_img);
				}
				manager.updateAppWidget(myWidget, remoteViews);
			}

		} catch (MalformedURLException e) {
			Log.d("myINFO", String.valueOf(e));
		} catch (IOException e) {
			Log.d("myINFO", String.valueOf(e));
		}
	}

	// Function to scale incoming bitmap to the desirable size
	public static Bitmap scaleBitmap(Bitmap bitmapToScale, float newWidth, float newHeight) {
		if (bitmapToScale == null) return null;

		int width = bitmapToScale.getWidth();
		int height = bitmapToScale.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(newWidth / width, newHeight / height);
		return Bitmap.createBitmap(bitmapToScale, 0, 0,bitmapToScale.getWidth(), bitmapToScale.getHeight(), matrix,true);

	}

	@SuppressLint("NewApi")
	public void callNotification(String tickerText, String contentTitle, String contentText) {

		// Getting preferences
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String pref_username = sharedPref.getString("user_name", "");
		String outputURL = "http://www.dribbble.com/" + pref_username + "/activity/incoming";

		Context ctx = getApplicationContext();
		final int NOTIF_ID = 1;

		NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Resources res = ctx.getResources();
		
		if (Build.VERSION.SDK_INT < 11) { 
			
			@SuppressWarnings("deprecation")
			String ns = Context.NOTIFICATION_SERVICE;
			Notification notification = new Notification(R.drawable.notification_icon_old, tickerText, System.currentTimeMillis());
			Intent notificationIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(outputURL));
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			notification.setLatestEventInfo(ctx, contentTitle, contentText, contentIntent);
			notification.flags = Notification.FLAG_AUTO_CANCEL;
			nm.notify(NOTIF_ID,notification);
			
		} else {
			Notification.Builder builder = new Notification.Builder(ctx);
			
			// Attaching tap to the notification
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(outputURL));
			browserIntent.putExtra("notificationID", notificationID);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, browserIntent, 0);
	
			builder
				.setContentIntent(contentIntent)
				.setSmallIcon(R.drawable.notification_icon)
				.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.notification_icon))
				.setTicker(tickerText)
				.setWhen(System.currentTimeMillis())
				.setAutoCancel(true)
				.setContentTitle(contentTitle)
				.setContentText(contentText);
	
			// Checking if notifications sound is enabled
			Boolean pref_notification_sound = sharedPref.getBoolean("notification_sound", true);
	
			if (pref_notification_sound == true) {
				builder.setDefaults(Notification.DEFAULT_SOUND);
			}
	
			Notification n = builder.build();
			nm.notify(NOTIF_ID, n);
		}

	}

	// Function to call preloader layout for the widget
	public void callPreloaderLayout() {
		ComponentName myWidget;
		AppWidgetManager manager = AppWidgetManager.getInstance(this.getApplicationContext());
		myWidget = new ComponentName(this.getApplicationContext(),MyWidgetProvider.class);
		RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),R.layout.widget_layout);
		//remoteViews.setViewVisibility(R.id.main_content, View.INVISIBLE);
		remoteViews.setViewVisibility(R.id.startup_overlay, View.INVISIBLE);
		remoteViews.setViewVisibility(R.id.preloader_overlay, View.VISIBLE);
		manager.updateAppWidget(myWidget, remoteViews);
	}

	// Function to call default layout for the widget
	public void callDefaultLayout() {
		ComponentName myWidget;
		AppWidgetManager manager = AppWidgetManager.getInstance(this.getApplicationContext());
		myWidget = new ComponentName(this.getApplicationContext(),MyWidgetProvider.class);
		RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),R.layout.widget_layout);
		//remoteViews.setViewVisibility(R.id.main_content, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.startup_overlay, View.INVISIBLE);
		remoteViews.setViewVisibility(R.id.preloader_overlay, View.INVISIBLE);
		manager.updateAppWidget(myWidget, remoteViews);
	}
	
	// Function to call no username layout for the widget
	public void callNoUsernameLayout() {
		ComponentName myWidget;
		AppWidgetManager manager = AppWidgetManager.getInstance(this.getApplicationContext());
		myWidget = new ComponentName(this.getApplicationContext(),MyWidgetProvider.class);
		RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(),R.layout.widget_layout);
		//remoteViews.setViewVisibility(R.id.main_content, View.INVISIBLE);
		remoteViews.setViewVisibility(R.id.startup_overlay, View.VISIBLE);
		remoteViews.setViewVisibility(R.id.preloader_overlay, View.INVISIBLE);
		manager.updateAppWidget(myWidget, remoteViews);
	}

	private void launchUpdate() {
		// Checking if refresh is enabled
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String refreshRate = sharedPref.getString("refresh_rate", "30");
		Long refreshRateLong = Long.parseLong(refreshRate)*60*1000;
		new Handler().postDelayed(new Runnable() {
			public void run() {

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

			}
		}, refreshRateLong);

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
