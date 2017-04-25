package control;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.example.project2.R;

import filecontrol.WavPlayer;

public class PlayActivity extends Activity {
	private String fileName;
	private ImageButton playButton, stopButton;
	private WavPlayer playerControl;

	private final int STOP = 1;
	private final int PLAYING = 2;
	private final int PAUSE = 3;

	private int status = STOP;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.play_layout);
		fileName = getIntent().getBundleExtra("myPacket").getString("fileName");
		playerControl = new WavPlayer(fileName);
		addID();
	}

	private void addID() {

		playButton = (ImageButton) findViewById(R.id.playImgBtn);
		playButton.setOnClickListener(clickListener);

		stopButton = (ImageButton) findViewById(R.id.stopImgBtn);
		stopButton.setOnClickListener(clickListener);
		stopButton.setEnabled(false);
	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.playImgBtn:
				if (status == STOP) {
					status = PLAYING;
					stopButton.setEnabled(true);
					playerControl.play();
					// chuyen ve pause
					playButton.setImageResource(R.drawable.pause);
				} else if (status == PLAYING) {
					status = PAUSE;
					playerControl.pause();
					playButton.setImageResource(R.drawable.play);
				} else if (status == PAUSE) {
					playerControl.resume();
					status = PLAYING;
					playButton.setImageResource(R.drawable.pause);
				}
				break;
			case R.id.stopImgBtn:
				if (status == PLAYING) {
					playerControl.stop();
					stopButton.setEnabled(false);
					status = STOP;
					playButton.setImageResource(R.drawable.play);
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		playerControl.stop();
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		playerControl.stop();
		// TODO Auto-generated method stub
		super.onStop();
	}

}
