package control;

public interface WaveViewListener {
	void setTime(int time);
	
	void setupZoom(float left, float right);
	
	void setupAfterDone(int time,int length, float[][] sample , float left, float right);
}
