package control;

import java.io.File;
import java.util.ArrayList;

import record.WavFile;
import record.WaveRecorder;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.project2.R;

public class ListActitivy extends Fragment {

	private ListView fileListView;
	private ArrayList<WavFile> model;
	private WavAdapter adapter = null;
	private Boolean isFirstTime = true;
	private TextView refreshTextView;

	private View view;
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.list_file_layout);
//		addID();
//	}
	
	

	private void addID() {
		refreshTextView = (TextView) view.findViewById(R.id.refresh);
		refreshTextView.setOnClickListener(onChange);
		fileListView = (ListView) view.findViewById(R.id.wav_file_list);
		reloadList();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.list_file_layout, container, false);
		
		addID();
		return view;
	}

	private void reloadList() {
		if (model != null) {
			adapter.clear();
		}
		model = new ArrayList<WavFile>();

		File folder = new File(WaveRecorder.projectFolder);
		File[] listofFiles = folder.listFiles();
		for (File file : listofFiles) {
			if (file.isFile()) {
				if (file.getName().endsWith(".wav")
						&& !file.getName().equals("temp.wav")) {

					// Log.w(null, file.getName());
					WavFile wavFile = new WavFile(file.getName());
					model.add(wavFile);
				} else if (file.getName().equalsIgnoreCase("temp.wav")) {

					file.delete();
				}
			}
		}
		adapter = new WavAdapter(view.getContext(),
				android.R.layout.simple_list_item_single_choice, model);
		fileListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		registerForContextMenu(fileListView);
	}

	private OnClickListener onChange = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			reloadList();
		}
	};

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.wav_file_list) {			
			getActivity().getMenuInflater().inflate(R.menu.list_file_menu, menu);
			
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final WavFile wavFile = (WavFile) adapter.getItem(info.position);

		if (item.getItemId() == R.id.waveMenu) {
			Intent myIntent = new Intent(view.getContext(),
					WaveControlActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("fileName", wavFile.getFileName());
			myIntent.putExtra("myPacket", bundle);
			startActivity(myIntent);

		} else if (item.getItemId() == R.id.playMenu) {
			Intent myIntent = new Intent(view.getContext(), PlayActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("fileName", wavFile.getFileName());
			myIntent.putExtra("myPacket", bundle);
			startActivity(myIntent);
		} else if (item.getItemId() == R.id.fftMenu) {
			Intent myIntent = new Intent(view.getContext(), FFTActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("fileName", wavFile.getFileName());
			myIntent.putExtra("myPacket", bundle);
			startActivity(myIntent);
		} else if (item.getItemId() == R.id.deleteFile) {
			new AlertDialog.Builder(view.getContext())
					.setTitle("Delete")
					.setMessage(
							"Are you sure want to delete "
									+ wavFile.getFileName() + "?")
					.setNegativeButton("Delete",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// TODO Auto-generated method stub
									File file = new File(
											WaveRecorder.projectFolder + "/"
													+ wavFile.getFileName());

									if (file.delete()) {
										model.remove(info.position);
										adapter.notifyDataSetChanged();
										Toast.makeText(
												view.getContext(),
												wavFile.getNameOnlyString()
														+ " has been deleted",
												Toast.LENGTH_SHORT).show();
										reloadList();
									} else {
										Toast.makeText(view.getContext(), "Can not delete",
												Toast.LENGTH_SHORT).show();
									}
								}
							}).setPositiveButton("No", null).show();

		} else if (item.getItemId() == R.id.sonaMenu) {
			Intent myIntent = new Intent(view.getContext(), SonaActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("fileName", wavFile.getFileName());
			myIntent.putExtra("myPacket", bundle);
			startActivity(myIntent);
		}
		isFirstTime = false;
		return super.onContextItemSelected(item);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub

		super.onResume();
		if (!isFirstTime) {
			reloadList();
		}
	}

}
