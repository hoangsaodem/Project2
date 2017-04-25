package filecontrol;

import static control.Main.CHANNEL;
import static control.Main.DELAY_TIME;
import static control.Main.SAMPLE_RATE;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import record.WaveRecorder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

;
public class RecordView extends View {
	private Bitmap canvasBitmap;
	private Canvas drawCanvas;
	private Paint pathPaint, canvasPaint, backgroundPaint;
	private float paintSize = 2f;
	private byte[] data;
	private float width;
	private float height;
	private WaveRecorder recorder;
	private boolean isFinish;

	public RecordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

	}

	private void setupDrawing() {
		pathPaint = new Paint();
		pathPaint.setAntiAlias(false);
		pathPaint.setStrokeWidth(paintSize);
		pathPaint.setStyle(Paint.Style.STROKE);
		pathPaint.setStrokeJoin(Paint.Join.ROUND);
		pathPaint.setStrokeCap(Paint.Cap.ROUND);
		pathPaint.setColor(Color.GREEN);

		canvasPaint = new Paint(Paint.DITHER_FLAG);

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
		// listTasks.add(task1);
		// listTasks.add(task2);
		// listTasks.add(task3);
		// listTasks.add(task4);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		// super.onDraw(canvas);
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

	}

	public void drawRecordBorder() {
		rect = new Rect(0, 0, (int) width, (int) height);
		drawCanvas.drawRect(rect, backgroundPaint);
		invalidate();
	}

	public void deleteRecordWave() {
		invalidate();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
				invalidate();

			}
		}, DELAY_TIME * 5);

	}

	private Handler handler = new Handler();
	int jump;
	private CreateWaveFormTask task = new CreateWaveFormTask();
	// private CreateWaveFormTask task2 = new CreateWaveFormTask();
	// private CreateWaveFormTask task3 = new CreateWaveFormTask();
	// private CreateWaveFormTask task4 = new CreateWaveFormTask();
	// private ArrayList<CreateWaveFormTask> listTasks = new
	// ArrayList<CreateWaveFormTask>();
	// private int numberOfThreads = 4;
	Rect rect;

	public void startDrawing() {
		isFinish = false;
		jump = (int) SAMPLE_RATE / 8000;
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (!isFinish) {
					RecordView.this.data = recorder.getData();

					// int k = 1;
					// drawCanvas.drawRect(rect, backgroundPaint);
					// for (CreateWaveFormTask task : listTasks) {
					// task.cancel(true);
					// task = new CreateWaveFormTask();
					// task.execute(k);
					// k++;
					// }

					task.cancel(true);
					task = new CreateWaveFormTask();
					task.execute(0);
					handler.postDelayed(this, DELAY_TIME);
				} else {
					Log.w(null, "done");
					drawCanvas.drawColor(Color.TRANSPARENT,
							PorterDuff.Mode.CLEAR);
					invalidate();
				}
			}
		}, DELAY_TIME);
	}

	public void stopDrawing() {
		isFinish = true;
		// for (CreateWaveFormTask task : listTasks) {
		// task.cancel(true);
		// task = null;
		// task = new CreateWaveFormTask();
		// }
		task.cancel(true);
		task = null;
		task = new CreateWaveFormTask();
		deleteRecordWave();

	}

	private class CreateWaveFormTask extends AsyncTask<Integer, Integer, Void> {

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
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
			if (isCancelled()) {
				DELAY_TIME *= 2;
				return;
			}
			int value = values[0];
			if (value == 1) {
				Toast.makeText(RecordView.this.getContext(), "null..",
						Toast.LENGTH_SHORT).show();
			} else if (value == 2) {
				Toast.makeText(RecordView.this.getContext(), "nothing..",
						Toast.LENGTH_SHORT).show();
			} else if (value == 3) {
				invalidate();
			}

		}

		@Override
		protected Void doInBackground(Integer... params) {
			// int value = params[0];
			drawCanvas.drawRect(rect, backgroundPaint);
			// TODO Auto-generated method stub
			if (data == null) {
				publishProgress(1);
				return null;
			}
			int length = data.length;

			if (length <= 0) {
				publishProgress(2);
				return null;
			}
			// Log.w(null, "task" + value + "drawing");
			short samples = 0;
			float xUnit = (float) jump * width / (length);
			float Unit = (float) height / (CHANNEL * 2);
			float yUnit = (float) height / (CHANNEL * 2);

			PointF currPointF, oldPointF;

			// int start = length / numberOfThreads * (value - 1);
			// int end = length / numberOfThreads * value;

			int start = 0;
			int end = length;

			for (int i = 0; i < CHANNEL; i++) {

				byte b1 = (byte) (data[start + 2 * i] & 0xff);
				byte b2 = (byte) (data[start + 1 + 2 * i] & 0xff);
				byte[] firstSample = { b1, b2 };
				samples = ByteBuffer.wrap(firstSample)
						.order(ByteOrder.LITTLE_ENDIAN).getShort();
				samples = (short) (-samples);
				currPointF = new PointF(xUnit * start, samples * Unit
						/ Short.MAX_VALUE + yUnit);

				for (int j = start; j < end; j += CHANNEL * 2 * jump) {
					if (isCancelled()) {
						DELAY_TIME *= 2;
						// Log.w(null, "cancel");

						return null;
					}
					b1 = (byte) (data[j + 2 * i] & 0xff);
					b2 = (byte) (data[j + 1 + 2 * i] & 0xff);
					byte[] sample = { b1, b2 };

					samples = ByteBuffer.wrap(sample)
							.order(ByteOrder.LITTLE_ENDIAN).getShort();
					samples = (short) (-samples);

					Path path = new Path();
					float beginX = xUnit * (j);
					float beginY = samples * Unit / Short.MAX_VALUE + yUnit;
					oldPointF = currPointF;
					currPointF = new PointF(beginX, beginY);
					path.moveTo(oldPointF.x, oldPointF.y);
					path.lineTo(beginX, beginY);
					drawCanvas.drawPath(path, pathPaint);
				}
				yUnit += (float) height / CHANNEL;
			}

			publishProgress(3);
			return null;
		}

	}

	public void setRecorder(WaveRecorder recorder) {
		this.recorder = recorder;
	}

}
