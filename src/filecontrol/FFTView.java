package filecontrol;

import static control.Main.FFT_SAMPLES;

import java.io.IOException;

import record.WavFileDecoder;
import record.WaveRecorder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;

import android.view.MotionEvent;
import android.view.View;

import control.FFTActivity;

public class FFTView extends View implements View.OnTouchListener {
	private Bitmap canvasBitmap;
	private Canvas drawCanvas;
	private Paint pathPaint, canvasPaint, trymPaint, asixPaint,
			backgroundPaint;
	private float paintSize = 1f;
	private FFTZoomView zoomView;
	private CreateWaveFormTask task;
	private float width, height;
	private float sample[][];
	private int sampleIndex = -1;
	private float touchX;
	private String fileName;
	private WavFileDecoder decorder;
	private int sampleRate;
	private FFTActivity activity;
	// private float limitBorder;

	private boolean zoomEnale = false;

	public FFTView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

	}

	private void setupDrawing() {
		pathPaint = new Paint();
		pathPaint.setAntiAlias(true);
		pathPaint.setStrokeWidth(paintSize);
		pathPaint.setStyle(Paint.Style.STROKE);
		pathPaint.setStrokeJoin(Paint.Join.ROUND);
		pathPaint.setStrokeCap(Paint.Cap.ROUND);
		pathPaint.setColor(Color.GREEN);

		canvasPaint = new Paint(Paint.DITHER_FLAG);

		trymPaint = new Paint();
		// trymPaint.setAntiAlias(true);
		trymPaint.setStrokeWidth(paintSize);
		trymPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		trymPaint.setColor(Color.WHITE);
		trymPaint.setAlpha(96);

		asixPaint = new Paint();
		asixPaint.setAntiAlias(true);
		asixPaint.setStrokeWidth(paintSize);
		asixPaint.setStyle(Paint.Style.STROKE);
		asixPaint.setStrokeJoin(Paint.Join.ROUND);
		asixPaint.setStrokeCap(Paint.Cap.ROUND);
		asixPaint.setColor(Color.WHITE);
		asixPaint.setAlpha(69);
		backgroundPaint = new Paint();
		backgroundPaint.setStyle(Style.FILL);
		backgroundPaint.setColor(Color.BLACK);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub

		if (!zoomEnale) {
			return true;
		}

		touchX = arg1.getX();
		sampleIndex = Math.round(sample[0].length * touchX / width);
		if (sampleIndex < FFT_SAMPLES / 2) {
			sampleIndex = FFT_SAMPLES / 2;
		}
		if (sampleIndex > sample[0].length - 1 - FFT_SAMPLES / 2) {
			sampleIndex = sample[0].length - 1 - FFT_SAMPLES / 2;
		}
		zoomView.zoom(sampleIndex);

		invalidate();
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// super.onDraw(canvas);
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		if (zoomEnale) {
			int start = Math.round((sampleIndex - FFT_SAMPLES / 2) / scale);
			int end = Math.round((sampleIndex + FFT_SAMPLES / 2) / scale);
			canvas.drawRect(new Rect(start, 0, end, (int) height), trymPaint);
		}
		if (zoomView!=null&&zoomView.isZooming()) {
			activity.setZoomTime(0, sampleRate / 2);
			zoomView.setZooming(false);
		}
	}

	private float scale = 0;

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
		setupDrawing();
		width = this.getWidth();
		height = this.getHeight();

		setOnTouchListener(this);
		task = new CreateWaveFormTask();
		task.execute(fileName);
	}

	public void releaseThread() {
		if (task != null) {
			task.cancel(true);
		}
		task = null;
	}

	private class CreateWaveFormTask extends AsyncTask<String, Integer, Void> {
		int time;
		public CreateWaveFormTask() {

		}

		@Override
		protected Void doInBackground(String... params) {
			String fileName = params[0];
			// TODO Auto-generated method stub
			decorder = new WavFileDecoder(WaveRecorder.projectFolder + "/"
					+ fileName);
			try {

				decorder.decode();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (isCancelled()) {
				return null;
			}

			sample = decorder.getComputedSamples();
			drawWave(0, sample[0].length - 1);
			scale = sample[0].length / width;
			sampleRate = decorder.getSampleRate();
			time = Math.round((float) sample[0].length / sampleRate * 1000);

			zoomView.setDecoder(decorder);
			zoomView.setScale((float) 1.0 / FFT_SAMPLES
					* decorder.getSampleRate() / 2);
			zoomEnale = true;
			zoomView.zoom(sample[0].length / 2);


			publishProgress(4);

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);

			int value = values[0];
			if (value == 3) {
				invalidate();
			} else if (value == 4) {
				activity.setTime(time);
				
				activity.setZoomTime(0, decorder.getSampleRate() / 2);
			}
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		private void drawWave(int start, int end) {
			Rect rect = new Rect(0, 0, (int) width, (int) height);
			drawCanvas.drawRect(rect, backgroundPaint);
			publishProgress(3);

			int length = end - start;

			float yUnit = (float) height / 2;
			drawCanvas.drawLine(0, yUnit, width, yUnit, asixPaint);
			drawCanvas.drawLine(0, yUnit / 2, width, yUnit / 2, asixPaint);
			drawCanvas.drawLine(0, yUnit / 2 * 3, width, yUnit / 2 * 3,
					asixPaint);

			drawCanvas.drawLine(width / 2, 0, width / 2, height, asixPaint);
			drawCanvas.drawLine(width / 4, 0, width / 4, height, asixPaint);
			drawCanvas.drawLine(width / 4 * 3, 0, width / 4 * 3, height,
					asixPaint);

			if (length <= 0) {
				publishProgress(3);
				return;
			}
			float Unit = (float) height / (sample.length * 2);
			yUnit = (float) height / (sample.length * 2);
			float xUnit = (float) width / (length);
			PointF currPointF, oldPointF;

			// path.moveTo(0, sample[0][start] * yUnit + yUnit);

			for (int i = 0; i < sample.length; i++) {
				currPointF = new PointF(0, -sample[i][start] * Unit + yUnit);
				for (int j = start; j <= end; j++) {
					if (isCancelled()) {
						return;
					}
					Path path = new Path();
					float beginX = xUnit * (j - start);
					float beginY = sample[i][j] * Unit + yUnit;
					oldPointF = currPointF;
					currPointF = new PointF(beginX, beginY);
					path.moveTo(oldPointF.x, oldPointF.y);
					path.lineTo(beginX, beginY);
					drawCanvas.drawPath(path, pathPaint);
					// publishProgress(3);
				}
				publishProgress(3);
				yUnit += (float) height / (sample.length);
			}
			publishProgress(3);
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public WavFileDecoder getDecorder() {
		return decorder;
	}

	public FFTZoomView getZoomView() {
		return zoomView;
	}

	public void setZoomView(FFTZoomView zoomView) {
		this.zoomView = zoomView;
	}

	public FFTActivity getActivity() {
		return activity;
	}

	public void setActivity(FFTActivity activity) {
		this.activity = activity;
	}

	public void setPaintSize(float size) {
		paintSize = size;
		pathPaint.setStrokeWidth(paintSize);
		trymPaint.setStrokeWidth(paintSize);
		asixPaint.setStrokeWidth(paintSize);
	}

	public int getSampleIndex() {
		return sampleIndex;
	}

	public void setSampleIndex(int sampleIndex) {
		this.sampleIndex = sampleIndex;
	}

}
