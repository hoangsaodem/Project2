package filecontrol;

import java.io.IOException;

import record.WavFileDecoder;
import record.WaveRecorder;
import android.app.Activity;
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
import control.WaveControlActivity;
import control.WaveViewListener;

public class WaveView extends View implements View.OnTouchListener {
	//private Activity actitivy;
	private Bitmap canvasBitmap;
	private Canvas drawCanvas;
	private Paint pathPaint, canvasPaint, trymPaint, asixPaint,
			backgroundPaint;
	private float paintSize = 1f;
	//private ZoomView zoomView;
	private CreateWaveFormTask task;
	private float width, height;
	private float sample[][];

	private float cutLeft, cutRight;
	private boolean onLeftMoving = false;
	private boolean onRightMoving = false;
	private String fileName;
	private WavFileDecoder decorder;
	private float limit;
	// private float limitBorder;
	private boolean touchEnable = true;
	


	private boolean zoomEnale = false;
	private WaveViewListener listener;

	public WaveViewListener getListener() {
		return listener;
	}
	public void setListener(WaveViewListener listener) {
		this.listener = listener;
	}
	
	
	public WaveView(Context context, AttributeSet attrs) {
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
		if(!touchEnable){
			return true;
		}
		if (!zoomEnale) {
			return true;
		}
		float touchX = arg1.getX();

		switch (arg1.getAction()) {
		case MotionEvent.ACTION_DOWN:

			break;
		case MotionEvent.ACTION_MOVE:

			if (Math.abs(touchX - cutLeft) < limit) {
				if (touchX >= cutRight) {
					onLeftMoving = false;
					onRightMoving = true;
				} else {
					if (!onRightMoving) {
						onLeftMoving = true;
						onRightMoving = false;
					}
				}
			}

			if (Math.abs(touchX - cutRight) < limit) {
				if (touchX <= cutLeft) {
					onLeftMoving = true;
					onRightMoving = false;
				} else {
					if (!onLeftMoving) {
						onLeftMoving = false;
						onRightMoving = true;
					}
				}
			}

			if (onLeftMoving) {
				if (touchX >= 0) {
					cutLeft = touchX;
				}
			}
			if (onRightMoving) {
				if ((width - touchX) >= 0) {
					cutRight = touchX;

				}
			}

			break;
		case MotionEvent.ACTION_UP:
			// if (onLeftMoving || onRightMoving) {
			// if (zoomEnale) {
			// zoomView.setupZoom(cutLeft, cutRight);
			// zoomView.zoom();
			// }
			// }
			// onLeftMoving = false;
			// onRightMoving = false;

			onLeftMoving = false;
			onRightMoving = false;
			break;
		default:
			return false;
		}
		invalidate();
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// super.onDraw(canvas);
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		// canvas.drawLine(cutLeft, 0, cutLeft, height, trymPaint);
		// canvas.drawLine(cutRight, 0, cutRight, height, trymPaint);

		canvas.drawRect(new Rect(0, 0, (int) cutLeft, (int) height), trymPaint);
		canvas.drawRect(new Rect((int) cutRight, 0, (int) width, (int) height),
				trymPaint);

		if (onLeftMoving || onRightMoving) {
			if (zoomEnale) {
				listener.setupZoom(cutLeft, cutRight);
				
			}
		}

	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
		setupDrawing();
		width = this.getWidth();
		height = this.getHeight();

		cutLeft = 0;
		cutRight = width;

		limit = width / 5;

		setOnTouchListener(this);
		task = new CreateWaveFormTask();
		task.execute(fileName);
	}

	public void releaseThread() {
		if (task.getStatus() == AsyncTask.Status.RUNNING
				|| task.getStatus() == AsyncTask.Status.PENDING) {
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

			time = Math.round((float) sample[0].length * 1000
					/ decorder.getSampleRate());
			Log.w("time", "time " + time);
			Log.w("sample[0].length", "sample[0].length " + sample[0].length);
			Log.w("rate", "rate" + decorder.getSampleRate());
			publishProgress(4);

			listener.setupAfterDone(time, sample[0].length,sample,cutLeft,cutRight);
			zoomEnale = true;
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
				listener.setTime(time);
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


	public void setPaintSize(float size) {
		paintSize = size;
		pathPaint.setStrokeWidth(paintSize);
		trymPaint.setStrokeWidth(paintSize);
		asixPaint.setStrokeWidth(paintSize);
	}
	
	public void setTouchEnable(boolean touchEnable) {
		this.touchEnable = touchEnable;
	}


}


