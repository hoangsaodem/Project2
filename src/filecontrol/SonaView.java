package filecontrol;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import other.ParulaColorMap;

import record.WavFileDecoder;
import record.WaveRecorder;
import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;



public class SonaView extends View {

	enum Part{
		Left,
		Middle,
		Right
	}
	private WavFileDecoder decorder;


	private Bitmap canvasBitmap;
	private Paint canvasPaint,backgroundPaint;

	private int width,height;



	private CreateSonaTask task1, task2, task3;
	public SonaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// super.onDraw(canvas);
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

	}
	private void setupDrawing() {


		canvasPaint = new Paint(Paint.DITHER_FLAG);


		
		backgroundPaint = new Paint();
		backgroundPaint.setStyle(Style.FILL);
		backgroundPaint.setColor(Color.BLACK);
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		setupDrawing();
		width = this.getWidth();
		height = this.getHeight();
	}
	
	public void transformSonagram(){
		//drawBackGround();
		
		task1 = new CreateSonaTask();
		//task2 = new CreateSonaTask();
		//task3 = new CreateSonaTask();
	    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
		    task1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Part.Left);
		    //task2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Part.Middle);
		    //task3.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Part.Right);

	    }
	    else{
			task1.execute(Part.Left);
			//task2.execute(Part.Middle);
			//task3.execute(Part.Right);

	    }

	}
	public void releaseThread() {
		releaseTask(task1);
		//releaseTask(task2);
		//releaseTask(task3);
		Log.w("Log sona","release");

	}
	
	private void releaseTask(CreateSonaTask task) {
		if(task==null){
			return;
		}
		if (task.getStatus() == AsyncTask.Status.RUNNING
				|| task.getStatus() == AsyncTask.Status.PENDING) {
			task.cancel(true);
		}
		task = null;
	}
	private class CreateSonaTask extends AsyncTask<SonaView.Part, Integer, Void> {


		
		public CreateSonaTask() {
			
		}
		private int start, end;
		private float timeUnit;
		private float currentTime;
		private float oldTime;
		private float yUnit;
		int jump;

		//jump = 4 mili second
		float fftJump = (float) 5.0/1000;
		
		int FFT_SAMPLES = 128;
		double[][] cuttedSamples = new double[1][FFT_SAMPLES];
		
		
		private Canvas drawCanvas;
		LinkedList<Integer> listColor;
		@Override
		protected Void doInBackground(SonaView.Part... params) {
			setupDrawCanvas();
			drawBackGround();

			Part part = params[0];
			ParulaColorMap colorMap = new ParulaColorMap();
			listColor = colorMap.getColor(getContext());
			jump  = (int) (fftJump*decorder.getSampleRate());
			jump = (jump==0)?1:jump;
			
			short[] samples = decorder.getSamples()[0];
			timeUnit = ((float)width) /samples.length;
			
			
			yUnit = (float) height / (FFT_SAMPLES/2);
			
			start = FFT_SAMPLES/2;
			end = samples.length - FFT_SAMPLES/2;
			

//			if(part==Part.Left){
//				start = FFT_SAMPLES/2;
//				end = samples.length/2;
//			}else {
//				//part.right
//				start = samples.length/2;
//				end = samples.length - FFT_SAMPLES/2;
//			}
			start = FFT_SAMPLES/2;
			end = samples.length - FFT_SAMPLES/2;

			currentTime = start*timeUnit;

			for(int i = start; i  < end ; i+= jump){
				if (isCancelled()) {
					return null;
				}
				
				oldTime = currentTime;
				currentTime +=timeUnit*jump;
				
				cuttedSamples = new double[1][FFT_SAMPLES];
				for (int j = 0; j < FFT_SAMPLES; j ++) {
					if (isCancelled()) {
						return null;
					}
					cuttedSamples[0][j] =samples[j + i - FFT_SAMPLES/2];
				}
				//decorder.getComputedFFTSamples(cuttedSamples);
				drawSona(decorder.getComputedFFTSamples(cuttedSamples)[0]);
				publishProgress(0);
			}
			// TODO Auto-generated method stub
			Log.w("Log sona","done");
			publishProgress(0);
			return null;
			
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			
			int value = values[0];
			if(value==0){
				invalidate();
				
			}
		
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
		float x1,x2,y1,y2;
		Shader gradient;
		private Paint pathPaint;
		Rect rect;
		
		private int oldColor;
		private int currentColor;
		
		private void drawSona(float[] sonaValues){
			
			 x1 = oldTime;
			 x2 = currentTime;
			 y1 = yUnit*(sonaValues.length);
			 y2 = 0;

			for(int i = 0 ; i < sonaValues.length; i++){
				if (isCancelled()) {
					return;
				}
				int index = Math.round(sonaValues[i]*255);
				currentColor = listColor.get(index);
				if(oldColor==0){
					oldColor = currentColor;
				}
				
				y2 = y1;
				y1 -= yUnit;
				
				gradient = new LinearGradient(x1,y1,x1,y2,currentColor, oldColor, TileMode.CLAMP);
				pathPaint.setShader(gradient);
				
				rect = new Rect((int)x1, (int)y1, (int)x2, (int)y2);
				drawCanvas.drawRect(rect,pathPaint);
				oldColor = currentColor;

			}
		}
		
		private void setupDrawCanvas() {
			drawCanvas = new Canvas(canvasBitmap);
			pathPaint = new Paint();
			pathPaint.setAntiAlias(true);
			pathPaint.setStyle(Paint.Style.FILL);

		}
		
		private void drawBackGround() {
			Rect rect = new Rect(0, 0, (int) width, (int) height);
			drawCanvas.drawRect(rect, backgroundPaint);
		}

	}


	
	public WavFileDecoder getDecorder() {
		return decorder;
	}

	public void setDecorder(WavFileDecoder decorder) {
		this.decorder = decorder;
	}

}
