package control;

import record.WaveRecorder;
import android.R.bool;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import static control.Main.CHANNEL;
import static control.Main.SAMPLE_RATE;
import com.example.project2.R;

import filecontrol.RecordView;

public class RecordActivity extends Fragment {

	private WaveRecorder recorder;
	private boolean isRecording = false;
	private TextView statusTextView;
	private ImageView recordImage;
	private RecordView recordView;
	private View view;

	// @Override
	// protected void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// setContentView(R.layout.record_layout);
	// addID();
	// }


	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.record_layout, container, false);
		recorder = new WaveRecorder(view.getContext());
		addID();
		return view;
	}

	private void addID() {
		statusTextView = (TextView) view.findViewById(R.id.statusTV);

		recordImage = (ImageView) view.findViewById(R.id.recordImage);
		recordImage.setOnClickListener(onClickListener);

		recordView = (RecordView) view.findViewById(R.id.recordView);
		recorder.setRecordView(recordView);
		recordView.setRecorder(recorder);
	}

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub

			if (isRecording) {

				isRecording = false;
				recorder.stopRecording(false);
				statusTextView.setText("Touch the picture to record");

			} else {

				isRecording = recorder.startRecording(CHANNEL, SAMPLE_RATE);
				Log.w(null, "channel = " + CHANNEL);
				Log.w(null, "sample rate = " + SAMPLE_RATE);
				if (isRecording) {
					recordView.drawRecordBorder();
					statusTextView.setText(((CHANNEL == 1) ? "MONO "
							: "STEREO ")
							+ " / Sample Rate: "
							+ SAMPLE_RATE
							+ "\nPress again to stop");
				} else {
					Toast.makeText(view.getContext(), "Not supported mode",
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	};

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		destroyRecord();
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		destroyRecord();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		destroyRecord();
	}

	private void destroyRecord() {
		if (isRecording) {
			isRecording = false;
			recorder.stopRecording(true);
			statusTextView.setText("Touch the picture to record");
		}
		recordView.stopDrawing();
	}

}
