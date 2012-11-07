package lunchcode.notifffy;


import lunchcode.notifffy.R;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class NotifffyWidget extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_notifffy_startup);

		Button settingButton = (Button) findViewById(R.id.settings_button);
		settingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				callSettings();
			}
		});
	}

	/** Toast maker */
	public void makeToast(String toast, int duration) {
		Toast.makeText(getApplicationContext(), toast, duration).show();
	}

	public void callSettings() {
		Context c = getApplicationContext();

		if (Build.VERSION.SDK_INT < 11) {
			startActivity(new Intent(this, DeprSettingsActivity.class));
		} else {
			Intent intent = new Intent(c,SettingsActivity.class);  
			this.startActivity(intent);
		}	
	}
}
