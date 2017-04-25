package record;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import filecontrol.RecordView;

import android.R.integer;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;

public class WaveRecorder {

	private AudioRecord audioRecord;
	
	private Context context;
	public static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyyy-mm-dd-hh-mm-ss");;
	public static String projectFolder = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ "Project2"
			+ File.separator + "RecordedFiles";
	private boolean isRecording = false;
	// config
	private int bufferSize;
	private int channelCount;
	private int longSampleRate;
	// handle record
	private Thread recordingThread = null;
	private File tempFile;
	private byte[] data;
	private RecordView recordView;
	// constant
	public final int WAV_HEADER_SIZE = 44;

	public WaveRecorder(Context context) {
		this.context = context;

		File dirFile = new File(projectFolder);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

	}

	public void stopRecording(boolean isPause) {
		//
		isRecording = false;
		if (audioRecord != null) {
			longSampleRate = audioRecord.getSampleRate();
			channelCount = audioRecord.getChannelCount();
			audioRecord.release();
		}
		recordingThread = null;
		audioRecord = null;
		if (isPause == true) {
			return;
		}
		final EditText txtUrl = new EditText(context);
		new AlertDialog.Builder(context)
				.setTitle("Save record")
				.setMessage("Your file name: ")
				.setView(txtUrl)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								saveFile(txtUrl.getText().toString().trim());
							}
						}).show();
		recordView.stopDrawing();

	}

	public void saveFile(String fileName) {

		// tao file name
		Date date = new Date();
		String dateString = dateFormat.format(date);
		String path = projectFolder + File.separator + fileName + "-"
				+ dateString + ".wav";

		long totalAudioLen;
		long totalDataLen;
		long byteRate;
		byte[] data = new byte[bufferSize];
		// write temp > file
		try {
			FileOutputStream out = new FileOutputStream(path);
			FileInputStream in = new FileInputStream(tempFile.getAbsolutePath());

			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			byteRate = 16 * longSampleRate * channelCount / 8;
			// write header
			out.write(
					writeWaveFileHeader(totalAudioLen, totalDataLen,
							longSampleRate, channelCount, byteRate), 0,
					WAV_HEADER_SIZE);
			// write data
			while (in.read(data) != -1) {
				out.write(data);
				Log.w(null, "COPYING...");
			}

			in.close();
			out.close();
			tempFile.delete();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean startRecording(int channel, int sampleRate) {
		// config
		audioRecord = findAudioRecord(channel, sampleRate);
		if (audioRecord == null) {
			return false;
		}
		audioRecord.startRecording();
		// create temp file
		tempFile = new File(projectFolder + File.separator + "temp.wav");
		if (!tempFile.exists()) {
			try {
				tempFile.createNewFile();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		// create write thread
		isRecording = true;
		recordingThread = new Thread(new Runnable() {
			public void run() {
				writeAudioDataToFile();
			}
		});
		recordingThread.start();
		//
		recordView.startDrawing();
		return true;
	}

	private void writeAudioDataToFile() {

		data = new byte[bufferSize];
		FileOutputStream fos = null;

		try {

			// write 2 temp file
			fos = new FileOutputStream(tempFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int read = 0;

		if (fos != null) {
			while (isRecording) {
				read = audioRecord.read(data, 0, bufferSize);
				if (read > 0) {
					try {
						fos.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (read == AudioRecord.ERROR_INVALID_OPERATION) {
					Log.e("Recording", "Invalid operation error");
					break;
				} else if (read == AudioRecord.ERROR_BAD_VALUE) {
					Log.e("Recording", "Bad value error");
					break;
				} else if (read == AudioRecord.ERROR) {
					Log.e("Recording", "Unknown error");
					break;
				}
			}
			// record xong
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public static byte[] writeWaveFileHeader(long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = 16; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		return header;
	}

	public AudioRecord findAudioRecord(int channel, int sampleRate) {
		short channelConfig = (short) ((channel == 1) ? AudioFormat.CHANNEL_IN_MONO
				: AudioFormat.CHANNEL_IN_STEREO);

		for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_16BIT,
				AudioFormat.ENCODING_PCM_8BIT }) {
			try {
				bufferSize = AudioRecord.getMinBufferSize(sampleRate,
						channelConfig, audioFormat);
				if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {

					// check if we can instantiate and have a success
					AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT,
							sampleRate, channelConfig, audioFormat, bufferSize);
					// mediaRecorder = new MediaRecorder();
					// mediaRecorder.setAudioChannels(channel);
					// mediaRecorder
					// .setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					// mediaRecorder
					// .setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
					// mediaRecorder.prepare();
					if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
						// this.sampleRate = sampleRate;
						// this.channelConfig = channelConfig;
						// this.audioFormat = audioFormat;
						bufferSize = AudioRecord.getMinBufferSize(sampleRate,
								channelConfig, audioFormat);

						Log.w(null, "created");
						return recorder;

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Log.w(null, "not created");
		return null;
	}

	public byte[] getData() {
		return data;
	}

	public void setRecordView(RecordView recordView) {
		this.recordView = recordView;
	}

	public AudioRecord getAudioRecord() {
		return audioRecord;
	}

}
