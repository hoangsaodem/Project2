package control;

import record.RecordCut;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.project2.R;

import filecontrol.WaveView;
import control.WaveViewListener;
import filecontrol.ZoomView;

public class WaveControlActivity extends Activity {

	private String fileName;
	// private Button cutButton;

	private WaveView waveView;
	private ZoomView zoomView;

	private TextView fileTime1, fileTime2, fileTime3, fileTime4;
	private TextView zoomTime0, zoomTime1, zoomTime2, zoomTime3, zoomTime4;

	WaveControlActivity activity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wave_layout);
		activity = this;
		fileName = getIntent().getBundleExtra("myPacket").getString("fileName");
		addID();

	}

	private void addID() {
		fileTime1 = (TextView) findViewById(R.id.fileTime1);
		fileTime2 = (TextView) findViewById(R.id.fileTime2);
		fileTime3 = (TextView) findViewById(R.id.fileTime3);
		fileTime4 = (TextView) findViewById(R.id.fileTime4);

		zoomTime0 = (TextView) findViewById(R.id.zoomTime0);
		zoomTime1 = (TextView) findViewById(R.id.zoomTime1);
		zoomTime2 = (TextView) findViewById(R.id.zoomTime2);
		zoomTime3 = (TextView) findViewById(R.id.zoomTime3);
		zoomTime4 = (TextView) findViewById(R.id.zoomTime4);

		waveView = (WaveView) findViewById(R.id.waveView);
		
		zoomView = (ZoomView) findViewById(R.id.zoomView);
		
		waveView.setListener(new WaveViewListener() {
			
			@Override
			public void setTime(int time) {
				// TODO Auto-generated method stub
				activity.setTime(time);
				
			}

			@Override
			public void setupAfterDone(int time, int length, float[][] sample,
					float left, float right) {
				// TODO Auto-generated method stub
				zoomView.setTotalTime(time);
				zoomView.setLength(length);
				zoomView.setSample(sample);
				setupZoom(left, right);

			}

			@Override
			public void setupZoom(float left, float right) {
				// TODO Auto-generated method stub
				zoomView.setupZoom(left, right);
				zoomView.zoom();
			}
		});
		
		waveView.setFileName(fileName);

		zoomView.setListener(new ZoomViewListener() {
			
			@Override
			public void setZoomTime(int startTime, int endTime) {
				// TODO Auto-generated method stub
				
				activity.setZoomTime(startTime, endTime);
			}
		});

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		// Toast.makeText(this, "back", Toast.LENGTH_SHORT).show();
		super.onBackPressed();
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.drawing_wave, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.cutItem:
			RecordCut recordCut = new RecordCut(waveView, zoomView);
			recordCut.makeWavFile();
			break;
		case R.id.size1:
			zoomView.setPaintSize(1);
			break;
		case R.id.size2:
			zoomView.setPaintSize(2);
			break;
		case R.id.size3:
			zoomView.setPaintSize(3);
			break;
		case R.id.size4:
			zoomView.setPaintSize(4);
			break;
		
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// Toast.makeText(this, "destroy", Toast.LENGTH_SHORT).show();
		waveView.releaseThread();
		zoomView.releaseThread();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		// Toast.makeText(this, "stop", Toast.LENGTH_SHORT).show();
		super.onStop();
	}

	public void setTime(int time) {
		fileTime4.setText(String.valueOf(time));
		fileTime3.setText(String.valueOf((time / 4 * 3)));
		fileTime2.setText(String.valueOf(time / 2));
		fileTime1.setText(String.valueOf(time / 4));

	}

	public void setZoomTime(int startTime, int endTime) {
		float duration = endTime - startTime;
		zoomTime4.setText(String.valueOf(endTime));
		zoomTime3.setText(String.valueOf(Math.round((duration / 4 * 3)
				+ startTime)));
		zoomTime2
				.setText(String.valueOf(Math.round((duration / 2) + startTime)));
		zoomTime1
				.setText(String.valueOf(Math.round((duration / 4) + startTime)));
		zoomTime0.setText(String.valueOf(startTime));

	}

}
