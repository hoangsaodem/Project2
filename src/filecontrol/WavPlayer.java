package filecontrol;

import java.io.IOException;

import record.WaveRecorder;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

public class WavPlayer {
	
	private MediaPlayer player;
	private String fileName;
	private int currentPosition;

	public WavPlayer(String fileName) {
		this.fileName = fileName;
		
	}

	public void play() {
		stop();

		try {
			player = new MediaPlayer();
			
			player.reset();
			player.setDataSource(WaveRecorder.projectFolder + "/" + fileName);
			player.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		player.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {
				Log.i("Completion Listener", "Song Complete");
			}
		});
		player.setLooping(true);
		player.start();
	}

	public void pause() {
		if (player != null) {
			currentPosition = player.getCurrentPosition();
			player.pause();
		}
	}

	public void stop() {
		if (player != null) {
			try {
				player.stop();
				player.release();
				player = null;

			} catch (IllegalStateException ise) {
				Log.w(null, "Not playing");
			}
		}
	}

	public void resume() {
		if (player != null) {
			player.seekTo(currentPosition);
			player.start();
		}
	}


}
