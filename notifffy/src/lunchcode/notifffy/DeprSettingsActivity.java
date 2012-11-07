package lunchcode.notifffy;

import lunchcode.notifffy.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class DeprSettingsActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}