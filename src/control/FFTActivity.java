package control;

import static control.Main.FFT_SAMPLES;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.example.project2.R;
import com.example.project2.R.id;
import com.example.project2.R.layout;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import filecontrol.FFTView;
import filecontrol.FFTZoomView;

public class FFTActivity extends Activity {
	private String fileName;
	// private Button cutButton;
	private TextView fileTime1, fileTime2, fileTime3, fileTime4;
	private TextView zoomTime0, zoomTime1, zoomTime2, zoomTime3;
	private TextView zoomTime4;
	private TextView db1, db2, db3, db4;
	private FFTView fftView;
	private FFTZoomView zoomView;
	private LinearLayout shiftLeftLayout, shiftRightLayout;

	private DecimalFormat df;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fft_layout);

		fileName = getIntent().getBundleExtra("myPacket").getString("fileName");
		addID();

	}

	private void addID() {

		df = new DecimalFormat("#.#");
		df.setRoundingMode(RoundingMode.CEILING);

		shiftLeftLayout = (LinearLayout) findViewById(R.id.shiftleft);
		shiftRightLayout = (LinearLayout) findViewById(R.id.shiftright);

		shiftLeftLayout.setOnClickListener(shiftListener);
		shiftRightLayout.setOnClickListener(shiftListener);

		shiftLeftLayout.setOnTouchListener(listener);
		shiftRightLayout.setOnTouchListener(listener);

		fileTime1 = (TextView) findViewById(R.id.fileTime1);
		fileTime2 = (TextView) findViewById(R.id.fileTime2);
		fileTime3 = (TextView) findViewById(R.id.fileTime3);
		fileTime4 = (TextView) findViewById(R.id.fileTime4);

		zoomTime0 = (TextView) findViewById(R.id.zoomTime0);
		zoomTime1 = (TextView) findViewById(R.id.zoomTime1);
		zoomTime2 = (TextView) findViewById(R.id.zoomTime2);
		zoomTime3 = (TextView) findViewById(R.id.zoomTime3);
		zoomTime4 = (TextView) findViewById(R.id.zoomTime4);

		db1 = (TextView) findViewById(R.id.db1);
		db2 = (TextView) findViewById(R.id.db2);
		db3 = (TextView) findViewById(R.id.db3);
		db4 = (TextView) findViewById(R.id.db4);

		fftView = (FFTView) findViewById(R.id.fftView);
		fftView.setActivity(this);
		fftView.setFileName(fileName);

		zoomView = (FFTZoomView) findViewById(R.id.zoomView);
		zoomView.setActivity(this);
		fftView.setZoomView(zoomView);
		// zoomView.setActitivy(this);

	}

	OnClickListener shiftListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (fftView.getSampleIndex() == -1) {
				return;
			}
			if (v.equals(shiftLeftLayout)) {

				if (fftView.getSampleIndex() > FFT_SAMPLES) {
					int newIndex = fftView.getSampleIndex() - FFT_SAMPLES / 2;
					zoomView.zoom(newIndex);
					fftView.setSampleIndex(newIndex);
					fftView.invalidate();
				}

			} else if (v.equals(shiftRightLayout)) {

				if (fftView.getSampleIndex() < (fftView.getDecorder()
						.getSamples()[0].length - 1 - FFT_SAMPLES)) {
					int newIndex = fftView.getSampleIndex() + FFT_SAMPLES / 2;
					zoomView.zoom(newIndex);
					fftView.setSampleIndex(newIndex);
					fftView.invalidate();
				}

			}

		}
	};

	OnTouchListener listener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.setBackgroundColor(Color.GRAY);
				break;
			case MotionEvent.ACTION_UP:

				// set color back to default
				v.setBackgroundColor(Color.WHITE);
				break;
			}
			return false;
		}
	};

	public void setTime(int time) {
		fileTime4.setText(String.valueOf(time));
		fileTime3.setText(String.valueOf((time / 4 * 3)));
		fileTime2.setText(String.valueOf(time / 2));
		fileTime1.setText(String.valueOf(time / 4));

	}

	public void setDecibel(float value) {

		db4.setText(df.format(-1 * value));
		db3.setText(df.format(-1 * (value / 4 * 3)));
		db2.setText(df.format(-1 * value / 2));
		db1.setText(df.format(-1 * value / 4));

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

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();
		fftView.releaseThread();
		zoomView.releaseThread();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		super.onStop();
		fftView.releaseThread();
		zoomView.releaseThread();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		fftView.releaseThread();
		zoomView.releaseThread();
	}

}
