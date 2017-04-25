package filecontrol;

import static control.Main.FFT_SAMPLES;
import record.WavFileDecoder;
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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import control.FFTActivity;

public class FFTZoomView extends View implements View.OnTouchListener {

	private Canvas drawCanvas;
	private Bitmap canvasBitmap;
	private Paint pathPaint, backgroundPaint, asixPaint, canvasPaint;
	private float scale;
	private float paintSize = 1f;

	private WavFileDecoder decorder;
	private float width, height;
	private float sample[][];

	private boolean isZooming = false;
	private ZoomWaveFormTask task;
	private int zoomOffset = FFT_SAMPLES / 2;
	private FFTActivity activity;
	int start, end;

	public FFTZoomView(Context context, AttributeSet attrs) {
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
		switch (arg1.getAction()) {
		case MotionEvent.ACTION_DOWN:
			float touchX = arg1.getX();
			if (sample != null) {
				int middle = Math.round(touchX / width * (end - start)) + start;
				zoom(middle, 0);
			}
			break;

		default:
			break;
		}

		return false;
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
		setupDrawing();
		width = this.getWidth();
		height = this.getHeight();
		task = new ZoomWaveFormTask();
		setOnTouchListener(this);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// super.onDraw(canvas);
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
	}

	public void zoom(Integer... params) {
		if (task == null) {
			task = new ZoomWaveFormTask();
		}
		if (task.getStatus() == AsyncTask.Status.RUNNING
				|| task.getStatus() == AsyncTask.Status.PENDING) {
			// invalidate(); //??
			task.cancel(true);
			task = null;
		}

		task = new ZoomWaveFormTask();
		if (params.length == 1) {
			task.execute(params[0]);
		} else {
			task.execute(params[0], 0);
		}

	}

	public void releaseThread() {
		if (task != null) {
			task.cancel(true);
		}
		task = null;
	}

	private float decibel;

	private class ZoomWaveFormTask extends AsyncTask<Integer, Integer, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			if (params.length == 1) {
				// TODO Auto-generated method stub

				int sampleIndex = params[0];
				double[][] cuttedSamples = new double[decorder.getChannels()][FFT_SAMPLES];
				short[][] samples = decorder.getSamples();
				
				start = sampleIndex - FFT_SAMPLES / 2;
			
				for (int i = 0; i < decorder.getChannels(); i++) {
					for (int j = 0; j < FFT_SAMPLES; j++) {
						if (isCancelled()) {
							return null;
						}
						cuttedSamples[i][j] = samples[i][j + start];
					}
				}
				if (isCancelled()) {
					return null;
				}
				sample = decorder.getComputedFFTSamples(cuttedSamples);
				// scale = (float)1.0/SAMPLE_INDEX*decorder.getSampleRate()/2;

				start = 0;
				end = sample[0].length - 1;
				zoomOffset = FFT_SAMPLES / 2;
				decibel = decorder.getMaxDecibel();
			
				publishProgress(3);

			} else {
				isZooming = true;
				int middle = params[0];
				start = middle - zoomOffset;
				if (start < 0) {
					start = 0;
				}
				end = middle + zoomOffset;
				if (end > sample[0].length - 1) {
					end = sample[0].length - 1;
				}
				zoomOffset /= 2;
				publishProgress(2);
			}
			drawZoomedWave(start, end);
			if (isCancelled()) {
				return null;
			}

			publishProgress(1);
			return null;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			int value = values[0];
			if (value == 1) {
				invalidate();
			} else if (value == 2) {
				activity.setZoomTime(Math.round(start * scale),
						Math.round(end * scale));
			} else if (value == 3) {
				activity.setDecibel(decibel);
			}

		}

		private void drawZoomedWave(int start, int end) {
			if (isCancelled()) {
				return;
			}
			Rect rect = new Rect(0, 0, (int) width, (int) height);
			drawCanvas.drawRect(rect, backgroundPaint);
			int length = end - start;
			if (length <= 0) {
				Log.w(null, "length = 0");
				cancel(true);
				return;
			}

			float yUnit = (float) height / 2;
			drawCanvas.drawLine(0, yUnit, width, yUnit, asixPaint);
			drawCanvas.drawLine(0, yUnit / 2, width, yUnit / 2, asixPaint);
			drawCanvas.drawLine(0, yUnit / 2 * 3, width, yUnit / 2 * 3,
					asixPaint);

			float Unit = (float) height / (sample.length);
			float xUnit = (float) width / (length);
			yUnit = (float) height / (sample.length);
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
					float beginY = -sample[i][j] * Unit + yUnit;
					oldPointF = currPointF;
					currPointF = new PointF(beginX, beginY);
					path.moveTo(oldPointF.x, oldPointF.y);
					path.lineTo(beginX, beginY);
					drawCanvas.drawPath(path, pathPaint);

				}
				yUnit += (float) height / (sample.length);
			}
			drawCanvas.drawLine(width / 2, 0, width / 2, height, asixPaint);
			drawCanvas.drawLine(width / 4, 0, width / 4, height, asixPaint);
			drawCanvas.drawLine(width / 4 * 3, 0, width / 4 * 3, height,
					asixPaint);

		}
	}

	public void setPaintSize(float size) {
		paintSize = size;
		pathPaint.setStrokeWidth(paintSize);
		asixPaint.setStrokeWidth(paintSize);
	}

	public WavFileDecoder getDecoder() {
		return decorder;
	}

	public void setDecoder(WavFileDecoder decoder) {
		this.decorder = decoder;
	}

	public boolean isZooming() {
		return isZooming;
	}

	public void setZooming(boolean isZooming) {
		this.isZooming = isZooming;
	}

	public FFTActivity getActivity() {
		return activity;
	}

	public void setActivity(FFTActivity activity) {
		this.activity = activity;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

}
