package other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ContentHandler;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.util.Log;

public class ParulaColorMap {
	
	public LinkedList<Integer> listColor = new LinkedList<Integer>();
	
	public LinkedList<Integer> getColor(Context currentContext){
		AssetManager assetManager = currentContext.getAssets();
		BufferedReader reader;
		InputStream is;
		String line;
		String[] colors;

		try {
			is = assetManager.open("parula");
			Log.w("open file","open file");
			reader = new BufferedReader(new InputStreamReader(is));
			Log.w("BufferedReader","BufferedReader done");
			while ((line = reader.readLine()) != null) {
				// do something with the line 
				colors = line.trim().split("\\s+");
				listColor.add(Color.rgb((int)(255*Float.parseFloat(colors[0])),(int)(255*Float.parseFloat(colors[1])), (int)(255* Float.parseFloat(colors[2]))));
				;
			};
			
		} catch (IOException e) {
			e.printStackTrace();
			Log.w("File not found","IOException");

		}
		
		return listColor;
	}
	
}
	
	
