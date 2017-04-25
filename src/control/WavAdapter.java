package control;

import java.util.ArrayList;

import record.WavFile;
import record.WaveRecorder;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.project2.R;

public class WavAdapter extends ArrayAdapter<WavFile> {

	private ArrayList<WavFile> model;

	public WavAdapter(Context context, int resource, ArrayList<WavFile> model) {
		super(context, resource, model);
		// TODO Auto-generated constructor stub
		this.model = model;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = convertView;
		WavHolder wavHolder = null;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.list_file_row, parent, false);
			wavHolder = new WavHolder(view);
			view.setTag(wavHolder);

		} else {
			wavHolder = (WavHolder) convertView.getTag();

		}

		wavHolder.pupulate(model.get(position));
		return view;

	}

	private class WavHolder {

		private TextView fileNameTV;
		private TextView lengthTV;	

		WavHolder(View row) {

			lengthTV = (TextView) row.findViewById(R.id.lengthTV);
			fileNameTV = (TextView) row.findViewById(R.id.fileNameTV);

			
		}

		public void pupulate(WavFile file) {
			fileNameTV.setText(file.getNameOnlyString());
			lengthTV.setText(file.getMinusString() + ":" + file.getSecondString());
		}
	}

}
