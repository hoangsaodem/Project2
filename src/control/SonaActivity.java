package control;

import com.example.project2.R;
import com.example.project2.R.id;
import com.example.project2.R.layout;
import com.example.project2.R.menu;

import filecontrol.ColorMapSurfaceView;
import filecontrol.SonaView;
import filecontrol.WaveView;
import filecontrol.ZoomView;



import android.support.v7.app.ActionBarActivity;
import android.R.integer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.widget.TextView;

public class SonaActivity extends ActionBarActivity {


	private String fileName;

	private WaveView waveView;
	private SonaView sonaView;

	private TextView fileTime1, fileTime2, fileTime3, fileTime4;
	private TextView sonaTime0, sonaTime1, sonaTime2, sonaTime3, sonaTime4;

	private TextView fft0, fft1, fft2, fft3, fft4;
	
	private  SonaActivity activity;
	private ColorMapSurfaceView colorMapBar;
	
	private int fs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sona_layout);
		activity = this;
		fileName = getIntent().getBundleExtra("myPacket").getString("fileName");
		addID();
	}
	
	
	private void addID() {
		fileTime1 = (TextView) findViewById(R.id.fileTime1);
		fileTime2 = (TextView) findViewById(R.id.fileTime2);
		fileTime3 = (TextView) findViewById(R.id.fileTime3);
		fileTime4 = (TextView) findViewById(R.id.fileTime4);

		sonaTime0 = (TextView) findViewById(R.id.sonaTime0);
		sonaTime1 = (TextView) findViewById(R.id.sonaTime1);
		sonaTime2 = (TextView) findViewById(R.id.sonaTime2);
		sonaTime3 = (TextView) findViewById(R.id.sonaTime3);
		sonaTime4 = (TextView) findViewById(R.id.sonaTime4);
		
		fft0 = (TextView) findViewById(R.id.fft0);
		fft1 = (TextView) findViewById(R.id.fft1);
		fft2 = (TextView) findViewById(R.id.fft2);
		fft3 = (TextView) findViewById(R.id.fft3);
		fft4 = (TextView) findViewById(R.id.fft4);

		sonaView = (SonaView) findViewById(R.id.sonaView);
		waveView = (WaveView) findViewById(R.id.waveView);
		waveView.setTouchEnable(false);
		waveView.setFileName(fileName);
		waveView.setListener(new WaveViewListener() {

			@Override
			public void setTime(int time) {
				// TODO Auto-generated method stub
				activity.setTime(time);
				fs = waveView.getDecorder().getSampleRate();
				activity.setFFT();
			}

			@Override
			public void setupAfterDone(int time, int length, float[][] sample,
					float left, float right) {
				// TODO Auto-generated method stub
				sonaView.setDecorder(waveView.getDecorder());
				sonaView.transformSonagram();
			}

			@Override
			public void setupZoom(float left, float right) {
				// TODO Auto-generated method stub
				
			}
			

		});
		
		colorMapBar = (ColorMapSurfaceView) findViewById(R.id.colorMapBar);
		colorMapBar.invalidate();
		Log.w("Log sona activity","done");

	}
	
	public void setTime(int time) {
		fileTime4.setText(String.valueOf(time));
		fileTime3.setText(String.valueOf((time / 4 * 3)));
		fileTime2.setText(String.valueOf(time / 2));
		fileTime1.setText(String.valueOf(time / 4));
		
		sonaTime4.setText(String.valueOf(time));
		sonaTime3.setText(String.valueOf((time / 4 * 3)));
		sonaTime2.setText(String.valueOf(time / 2));
		sonaTime1.setText(String.valueOf(time / 4));

	}
	
	public void setFFT() {
		
		fft1.setText(String.valueOf(fs/8));
		fft2.setText(String.valueOf(fs/4));
		fft3.setText(String.valueOf(fs/8*3));
		fft4.setText(String.valueOf(fs/2));

	}
	@Override
	protected void onDestroy() {
		waveView.releaseThread();
		sonaView.releaseThread();
		super.onDestroy();
	}
	
}
