package record;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import com.example.project2.R.id;

import android.R.integer;
import android.util.Log;

public class WavFileDecoder {
	private int totalDataSize;
	private short channels;
	private int sampleRate;
	private int bufferSize;
	private byte buffer[];
	private short bitsPerSample;
	private int soundDataSize;
	private double max;
	private int length;
	private Complex[][] fftSamples;
	//private float[][] computedFftSamples;

	private short[][] samples;
	private float[][] computedSamples;

	private String fileName;
	private FileInputStream fis;
	private float maxHeight = 0;
	private FastFourierTransformer fft = new FastFourierTransformer(
			DftNormalization.STANDARD);;
	private float maxDecibel;

	public WavFileDecoder(String fileName) {
		this.fileName = fileName;
	}

	public void decode() throws IOException {
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		decodeHeader();
		decodeData();

	}

	private void decodeHeader() throws IOException {
		Log.w("Decode", "" + read4ByteString());
		Log.w("Decode", "length = " + (totalDataSize = readIntLittleEndian()));
		Log.w("Decode", "" + read4ByteString());
		Log.w("Decode", "" + read4ByteString());
		Log.w("Decode", "" + readIntLittleEndian());
		Log.w("Decode", "" + readShortLittleEndian());
		Log.w("Decode", "channel = " + (channels = readShortLittleEndian()));
		Log.w("Decode", "sampleRate = " + (sampleRate = readIntLittleEndian()));
		Log.w("Decode", "" + readIntLittleEndian());
		Log.w("Decode", "" + readShortLittleEndian());
		Log.w("Decode", "bitsPerSample = "
				+ (bitsPerSample = readShortLittleEndian()));
		Log.w("Decode", "" + read4ByteString());
		Log.w("Decode", "" + readIntLittleEndian());

		soundDataSize = totalDataSize - 36;
	}

	private void decodeData() throws IOException {
		buffer = new byte[soundDataSize];
		fis.read(buffer);
		fis.close();
		samples = new short[channels][soundDataSize
				/ (channels * bitsPerSample / 8)];

		length = samples[0].length;
		int j = 0;
		
		if (channels == 1) {
			for (int i = 0; i < length; i++, j += 2) {
				try {
					byte b1 = (byte) (buffer[j] & 0xff);
					byte b2 = (byte) (buffer[j + 1] & 0xff);
					byte[] data = { b1, b2 };
					samples[0][i] = ByteBuffer.wrap(data)
							.order(ByteOrder.LITTLE_ENDIAN).getShort();
					samples[0][i] = (short) (-samples[0][i]);
				} catch (IndexOutOfBoundsException iobe) {
					Log.w("WaveRecorder", "Out of bounds somehow");
					break;
				}
			}
		} else if (channels == 2) {
			for (int i = 0; i < length; i++, j += 4) {
				try {
					byte b1 = (byte) (buffer[j] & 0xff);
					byte b2 = (byte) (buffer[j + 1] & 0xff);
					byte b3 = (byte) (buffer[j + 2] & 0xff);
					byte b4 = (byte) (buffer[j + 3] & 0xff);
					byte[] leftData = { b1, b2 };
					samples[0][i] = ByteBuffer.wrap(leftData)
							.order(ByteOrder.LITTLE_ENDIAN).getShort();
					samples[0][i] = (short) (-samples[0][i]);
					byte[] rightData = { b3, b4 };
					samples[1][i] = ByteBuffer.wrap(rightData)
							.order(ByteOrder.LITTLE_ENDIAN).getShort();
					samples[1][i] = (short) (-samples[1][i]);
				} catch (IndexOutOfBoundsException iobe) {
					Log.w("WaveRecorder", "Out of bounds somehow");
					break;
				}
			}
			Log.w(null, "length = " + length);
		} else {
			throw new IllegalStateException();
		}
		Log.w(null, "done read data");
	}

	// //////////////////////////////////////////////////
	private Complex[][] getFftSample(double[][] samples) {

		int channels = samples.length;
		fftSamples = new Complex[channels][];
		for (int i = 0; i < samples.length; i++) {
			fftSamples[i] = fft.transform(samples[i], TransformType.FORWARD);

		}

		return fftSamples;
	}
	public double[] getNormalFFTSamples(double[][] samples) {
		Complex[][] complexFFT = getFftSample(samples);
		int length = complexFFT[0].length/2;
		double[] sonaValues = new double[length];
		double temp = 0;
		double maxValue =0;
		for (int j = 0; j < length / 2; j++) {
			temp = complexFFT[0][j].abs();
			if(maxValue<temp){
				maxValue = temp;
			}
			
			sonaValues[j] = temp;
		}
		
		for (int j = 0; j < length / 2; j++) {
			sonaValues[j] /= maxValue;
			
		}
		
		return sonaValues;
		
	}
	// /////////////////////////////////////////////////////////////
	public float[][] getComputedFFTSamples(double[][] samples) {

		Complex[][] fftSamples = getFftSample(samples);
		int channels = fftSamples.length;
		int length = fftSamples[0].length;
		float[][] computedFftSamples = new float[channels][length / 2];
		double maxValue = 0;
		double minValue = Double.MAX_VALUE;
		double temp = 0;

		for (int i = 0; i < channels; i++) {
			for (int j = 0; j < length / 2; j++) {
				temp = fftSamples[i][j].abs();
				if (maxValue < temp) {
					maxValue = temp;					
				}
				if (minValue > temp) {
					minValue = temp;

				}
			}
		}

		maxDecibel = 20 * ((float) Math.log10(maxValue / minValue));
		for (int i = 0; i < channels; i++) {
			for (int j = 0; j < length / 2; j++) {
				computedFftSamples[i][j] = 1+20
						* ((float) Math
								.log(fftSamples[i][j].abs()/maxValue)/(float)Math.log(10))
						/ maxDecibel;	
			}
		}

		return computedFftSamples;
	}
	
	
	public float[] getComputedFFTSamples(double[] samples) {

		Complex[] fftSamples = new Complex[samples.length];
		int length = samples.length;
		for (int i = 0; i < length; i++) {
			fftSamples = fft.transform(samples, TransformType.FORWARD);

		}

		length = fftSamples.length;
		float[] computedFftSamples = new float[length / 2];
		double maxValue = 0;
		double minValue = Double.MAX_VALUE;
		double temp = 0;


		for (int j = 0; j < length / 2; j++) {
			temp = fftSamples[j].abs();
			if (maxValue < temp) {
				maxValue = temp;					
			}
			if (minValue > temp) {
				minValue = temp;

			}
		}
		

		maxDecibel = 20 * ((float) Math.log10(maxValue / minValue));
		float log10 = (float)Math.log(10);
		float computeDecibel = 20*log10/maxDecibel;
		
		for (int j = 0; j < length / 2; j++) {
			computedFftSamples[j] = 1+(float) Math.log(fftSamples[j].abs()/maxValue)*computeDecibel;	
		}

		return computedFftSamples;
	}

	private String read4ByteString() throws IOException {
		StringBuffer str = new StringBuffer("");
		for (int i = 0; i < 4; i++) {
			try {
				str.append((char) fis.read());
			} catch (NullPointerException npe) {
				Log.w("Decode", fis + "");
			}
		}
		return str.toString();
	}

	private int readIntLittleEndian() throws IOException {
		byte data[] = new byte[4];
		ByteBuffer bb = ByteBuffer.allocate(4);
		for (int i = 0; i < 4; i++) {
			data[i] = (byte) fis.read();
			bb = ByteBuffer.wrap(data);
		}
		return bb.order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	private short readShortLittleEndian() throws IOException {
		byte b1 = (byte) (fis.read() & 0xff);
		byte b2 = (byte) (fis.read() & 0xff);
		byte[] data = { b1, b2 };
		return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getShort();
	}

	public short[][] getSamples() {
		return samples;
	}

	public float[][] getComputedSamples() {
		computedSamples = new float[channels][length];
		for (int i = 0; i < channels; i++) {
			for (int j = 0; j < length; j++) {
				computedSamples[i][j] = (float) samples[i][j] / Short.MAX_VALUE;
			}
		}
		return computedSamples;
	}

	public float getMaxHeight() {
		return maxHeight;
	}

	public void setMaxHeight(float maxHeight) {
		this.maxHeight = maxHeight;
	}

	public int getTotalDataSize() {
		return totalDataSize;
	}

	public void setTotalDataSize(int totalDataSize) {
		this.totalDataSize = totalDataSize;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getSoundDataSize() {
		return soundDataSize;
	}

	public void setSoundDataSize(int soundDataSize) {
		this.soundDataSize = soundDataSize;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setSamples(short[][] samples) {
		this.samples = samples;
	}

	public short getChannels() {
		return channels;
	}

	public void setChannels(short channels) {
		this.channels = channels;
	}

	public short getBitsPerSample() {
		return bitsPerSample;
	}

	public void setBitsPerSample(short bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
	}

	public int getByteRate() {
		return sampleRate * channels * bitsPerSample / 8;
	}

	public float getMaxDecibel() {
		return maxDecibel;
	}

	public void setMaxDecibel(float maxDecibel) {
		this.maxDecibel = maxDecibel;
	}

}
