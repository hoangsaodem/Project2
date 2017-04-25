package filecontrol;

import java.util.LinkedList;

import javax.security.auth.PrivateCredentialPermission;

import other.ParulaColorMap;

import android.R.anim;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ColorMapSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
   


	private Paint barPaint, backgroundPaint ;
	private Shader gradient;
	ColorMapSurfaceView thisSurfaceView;
    int width,height;
    
    public ColorMapSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        thisSurfaceView = this;
        // TODO Auto-generated constructor stub
        getHolder().addCallback(this);
    }

    public ColorMapSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        thisSurfaceView = this;
        // TODO Auto-generated constructor stub
        getHolder().addCallback(this);
    }
    

	public ColorMapSurfaceView(Context context) {
		super(context);
		thisSurfaceView = this;
		// TODO Auto-generated constructor stub
		getHolder().addCallback(this);
	}



	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
		
		setupDrawing();
		width = arg2;
		height = arg3;
		
		Canvas canvas = getHolder().lockCanvas();
		
		drawBackGround(canvas);
		drawColorBar(canvas);
		
		getHolder().unlockCanvasAndPost(canvas);
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub

		
	}
	

   
	LinkedList<Integer> listColor;
	float barWidthRatio = 0.8f;
	
    private void setupDrawing(){		
    	barPaint = new Paint();
    	barPaint.setStyle(Style.FILL);
    	barPaint.setAntiAlias(true);
		
		
		backgroundPaint = new Paint();
		backgroundPaint.setStyle(Style.FILL);
		backgroundPaint.setColor(Color.WHITE);
		
		ParulaColorMap colorMap = new ParulaColorMap();
		listColor = colorMap.getColor(getContext());
		
	}
    
	private void drawColorBar(Canvas canvas) {
		if(listColor.size()== 0){
			return;
		}
		int oldColor = 0, currentColor;
		Rect rect;
		float barWidth = (float)width * barWidthRatio;
		float maxBarWidth = 20f;
		
		barWidth  = (maxBarWidth>barWidth)? barWidth:maxBarWidth;
		
		float x1 = ((float)width - barWidth)/2;
		float x2 = x1+barWidth;
		
		float yUnit = (float)height / listColor.size();
		float y2, y1 = (float)height;
		
		for (int i = 0; i < listColor.size(); i++) {
			
			currentColor = listColor.get(i);
			if(oldColor==0){
				oldColor = currentColor;
			}
			
			y2 = y1;
			y1 -= yUnit;
			
			gradient = new LinearGradient(x1,y1,x1,y2,currentColor, oldColor, TileMode.CLAMP);
			barPaint.setShader(gradient);
			
			rect = new Rect((int)x1, (int)y1, (int)x2, (int)y2);
			canvas.drawRect(rect, barPaint);
			oldColor = currentColor;
		}
		
		Log.w("draw", "fucking red");
	}
	
	private void drawBackGround(Canvas canvas) {
		Rect rect = new Rect(0, 0, (int) width, (int) height);
		canvas.drawRect(rect, backgroundPaint);
	}
}
