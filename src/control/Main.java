package control;

import preferences.SettingPrefences;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.project2.R;

public class Main extends FragmentActivity {
	public static int CHANNEL = 1;
	public static int SAMPLE_RATE = 8000;
	public static int DELAY_TIME = 40;
	public static int FFT_SAMPLES = 512;
	ViewPager tab;
	FragmentAdapter tabAdapter;
	ActionBar actionBar;
	private ViewPager viewPager;
	SharedPreferences pref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		tabAdapter = new FragmentAdapter(getSupportFragmentManager());
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		pref.registerOnSharedPreferenceChangeListener(changeListener);
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		viewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar = getActionBar();
						actionBar.setSelectedNavigationItem(position);
					}
				});
		viewPager.setAdapter(tabAdapter);
		actionBar = getActionBar();
		// Enable Tabs on Action Bar
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabReselected(android.app.ActionBar.Tab tab,
					FragmentTransaction ft) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(android.app.ActionBar.Tab tab,
					FragmentTransaction ft) {
				// TODO Auto-generated method stub
			}
		};
		// Add New Tab
		actionBar.addTab(actionBar.newTab().setText("Record")
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("List File")
				.setTabListener(tabListener));

		CHANNEL = Integer.parseInt(pref.getString("Record Mode", "1"));
		SAMPLE_RATE = Integer.parseInt(pref.getString("Sample Rate", "8000"));
		DELAY_TIME = 1000 / Integer
				.parseInt(pref.getString("Frame Rate", "25"));
		FFT_SAMPLES = Integer.parseInt(pref.getString("FFT Samples", "512"));

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.record_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		startActivity(new Intent(this, SettingPrefences.class));
		return super.onOptionsItemSelected(item);
	}

	private OnSharedPreferenceChangeListener changeListener = new OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			// TODO Auto-generated method stub
			if (key.equals("Sample Rate")) {
				SAMPLE_RATE = Integer.parseInt(pref.getString("Sample Rate",
						"8000"));
				Toast.makeText(Main.this,
						"Sample Rate change to " + SAMPLE_RATE,
						Toast.LENGTH_SHORT).show();
			} else if (key.equals("Record Mode")) {
				CHANNEL = Integer.parseInt(pref.getString("Record Mode", "1"));
				Toast.makeText(
						Main.this,
						"Record mode change to "
								+ ((CHANNEL == 1) ? "MONO" : "STEREO"),
						Toast.LENGTH_SHORT).show();
			} else if (key.equals("Frame Rate")) {
				DELAY_TIME = 1000 / Integer.parseInt(pref.getString(
						"Frame Rate", "25"));

				Toast.makeText(
						Main.this,
						"Frame Rate change to "
								+ pref.getString("Frame Rate", "25"),
						Toast.LENGTH_SHORT).show();
			} else if (key.equals("FFT Samples")) {
				FFT_SAMPLES = Integer.parseInt(pref.getString("FFT Samples",
						"512"));
				Toast.makeText(Main.this,
						"FFT Samples change to " + FFT_SAMPLES,
						Toast.LENGTH_SHORT).show();
			}

		}
	};

}
