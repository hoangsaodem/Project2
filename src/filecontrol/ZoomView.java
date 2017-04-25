package filecontrol;

import control.WaveControlActivity;
import control.ZoomViewListener;
import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ZoomView extends View {
	//private WaveControlActivity actitivy;
	private Canvas drawCanvas;
	private Bitmap canvasBitmap;
	private Paint pathPaint, backgroundPaint, asixPaint, canvasPaint;

	private float paintSize = 1f;

	private int startOffset = 0;
	private int endOffset = 0;
	private int length;
	private float cutLeft, cutRight;
	private float width, height;
	private float sample[][];
	
	private int totalTime;
	ZoomWaveFormTask task;

	public ZoomViewListener getListener() {
		return listener;
	}

	public void setListener(ZoomViewListener listener) {
		this.listener = listener;
	}

	private ZoomViewListener listener;
	
	
	
	public ZoomView(Context context, AttributeSet attrs) {
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
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
		setupDrawing();
		width = this.getWidth();
		height = this.getHeight();		
		task = new ZoomWaveFormTask();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// super.onDraw(canvas);
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
	}

	public void zoom() {

		if (task.getStatus() == AsyncTask.Status.RUNNING
				|| task.getStatus() == AsyncTask.Status.PENDING) {
			// invalidate(); //??
			task.cancel(true);
			task = null;
		}

		task = new ZoomWaveFormTask();
		task.execute(cutLeft, cutRight);

	}

	public void releaseThread() {
		if (task.getStatus() == AsyncTask.Status.RUNNING
				|| task.getStatus() == AsyncTask.Status.PENDING) {

			task.cancel(true);
		}
		task = null;
	}

	private class ZoomWaveFormTask extends AsyncTask<Float, Integer, Void> {
		private int startTime, endTime;

		@Override
		protected Void doInBackground(Float... params) {
			// TODO Auto-generated method stub
			startOffset = Math.round(length * params[0] / width);
			endOffset = Math.round(length * params[1] / width);
			startTime = Math.round(totalTime * params[0] / width);
			endTime = Math.round(totalTime * params[1] / width);
			
			drawZoomedWave(startOffset, endOffset);
			if (isCancelled()) {
				// Log.w(null, "cancel hihi");
				return null;
			}
			publishProgress(1);
			publishProgress(2);
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
				listener.setZoomTime(startTime, endTime);
			}

		}


		private void drawZoomedWave(int start, int end) {
			if (isCancelled()) {
				// Log.w(null, "cancel hihi");
				return;
			}			
			Rect rect = new Rect(0, 0, (int) width, (int) height);
			drawCanvas.drawRect(rect, backgroundPaint);
			int length = end - start;
			if (length <= 0) {				
				cancel(true);
				return;
			}			
			if (isCancelled()) {
				return;
			}
			
			float yUnit = (float) height / 2;
			drawCanvas.drawLine(0, yUnit, width, yUnit, asixPaint);
			drawCanvas.drawLine(0, yUnit / 2, width, yUnit / 2, asixPaint);
			drawCanvas.drawLine(0, yUnit / 2 * 3, width, yUnit / 2 * 3,
					asixPaint);
			
			
			float Unit = (float) height / (sample.length *2);
			float xUnit = (float) width / (length);
			yUnit = (float) height / (sample.length *2);
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
					float beginY = sample[i][j] * Unit+yUnit ;
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

	public void setupZoom(float cutLeft, float cutRight) {
		this.cutLeft = cutLeft;
		this.cutRight = cutRight;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}

	public float[][] getSample() {
		return sample;
	}

	public void setSample(float[][] sample) {
		this.sample = sample;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length-1;
	}


	public int getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(int totalTime) {
		this.totalTime = totalTime;
	}

}
