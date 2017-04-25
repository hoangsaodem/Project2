package record;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import filecontrol.WaveView;
import filecontrol.ZoomView;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class RecordCut {
	private WavFileDecoder decoder;
	private int start;
	private int end;
	private WaveView view;

	public RecordCut(WaveView view, ZoomView zoomView) {
		this.view = view;
		this.decoder = view.getDecorder();
		this.start = zoomView.getStartOffset();
		this.end = zoomView.getEndOffset();
	}

	public void makeWavFile() {
		final EditText txtUrl = new EditText(view.getContext());
		new AlertDialog.Builder(view.getContext())
				.setTitle("Save record")
				.setMessage("Your file name: ")
				.setView(txtUrl)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								CutFileTask task = new CutFileTask();
								task.execute(txtUrl.getText().toString().trim());

							}
						}).show();

	}

	private class CutFileTask extends AsyncTask<String, Integer, Void> {

		@Override
		protected Void doInBackground(String... params) {
			
			// TODO Auto-generated method stub
			String fileName = params[0];
			Date date = new Date();
			String dateString = WaveRecorder.dateFormat.format(date);
			String path = WaveRecorder.projectFolder + File.separator + fileName
					+ "-" + dateString + ".wav";
			short samples[][] = decoder.getSamples();

			try {
				File cutFile = new File(path);
				if (!cutFile.exists()) {
					cutFile.createNewFile();
				}
				FileOutputStream fos;
				int length = end - start + 1;
				fos = new FileOutputStream(cutFile);
				byte header[] = WaveRecorder.writeWaveFileHeader(2 * length
						* decoder.getChannels(), 2 * length * decoder.getChannels()
						+ 36, decoder.getSampleRate(), decoder.getChannels(),
						decoder.getByteRate());
				fos.write(header, 0, 44);
				int channels = decoder.getChannels();
				Log.w(null, "begin...");
				byte data[] = new byte[2 * channels * length];
				int k = 0;
				for (int i = start; i < end; i++) {
					for (int j = 0; j < channels; j++) {
						short l = (short) -samples[j][i];
						data[k] = (byte) (l & 0x00ff);
						data[k + 1] = (byte) ((l >> 8) & 0x00ff);
						k += 2;
					}

				}
				Log.w(null, "writing...");
				fos.write(data, 0, data.length);
				fos.close();
				publishProgress(1);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			int value = values[0];
			if (value == 1) {
				Toast.makeText(view.getContext(), "Making file is done",
						Toast.LENGTH_SHORT).show();
				((Activity) view.getContext()).finish();
			}
		}

	}

}
