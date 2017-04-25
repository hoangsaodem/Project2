package record;

import android.media.MediaMetadataRetriever;
import android.util.Log;

public class WavFile {
	private String fileName;
	private String nameOnlyString;
	private String secondString = "";
	private String minusString = "";
	
	public WavFile(String fileName){
		this.fileName=fileName;
		nameOnlyString = fileName.substring(0,
				fileName.lastIndexOf(".") - 1);
		
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		mmr.setDataSource(WaveRecorder.projectFolder + "/" + fileName);
		String duration = mmr
				.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		
		float floatSecond = (float) Integer.parseInt(duration) / 1000;
		int second = 0;
		int minus = 0;
		second = Math.round(floatSecond);
		if (second >= 60) {
			minus = second / 60;
			second %= 60;
		}

		if (minus < 10) {
			minusString = "0" + minus;
		} else {
			minusString = String.valueOf(minus);

		}

		if (second < 10) {
			secondString = "0" + second;
		} else {
			secondString = String.valueOf(second);

		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getNameOnlyString() {
		return nameOnlyString;
	}

	public void setNameOnlyString(String nameOnlyString) {
		this.nameOnlyString = nameOnlyString;
	}

	public String getSecondString() {
		return secondString;
	}

	public void setSecondString(String secondString) {
		this.secondString = secondString;
	}

	public String getMinusString() {
		return minusString;
	}

	public void setMinusString(String minusString) {
		this.minusString = minusString;
	}
	
	
	
}
