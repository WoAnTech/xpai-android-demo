package cn.com.xpai;

import java.util.ArrayList;
import java.util.List;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import cn.com.xpai.core.Manager;


class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "Preview";
	private SurfaceHolder mHolder;
	private Activity activity;
	private int screenWidth;
	private int screenHeight;
	
	public Preview(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		activity = (Activity) context;
        Display display =  activity.getWindowManager().getDefaultDisplay(); 
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
        Log.i(TAG, "screen width:" + screenWidth + " height:" + screenHeight);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Manager.setPreviewSurface(this);
		if (!Manager.isPreviewing())
			Manager.startPreview();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.w(TAG, "Destroying surface!");
		Manager.stopRecord();
		Manager.stopPreview();
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.i(TAG, "Surface Changed!");
		Manager.setPreviewSize(w,h);
		setOnTouchListener(new OnTouchListener() {
	        private float x;
	        private float y;
	        private int tolerance = 50;

	        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            switch(event.getAction()){
	            case MotionEvent.ACTION_MOVE:
	                return false; //This is important, if you return TRUE the action of swipe will not take place.
	            case MotionEvent.ACTION_DOWN:
	                x = event.getX();
	                y = event.getY();
	                float x_p = x/screenWidth;
	                float y_p = y/screenHeight;
	                int t_x = (int) (-1000 + x * 2000 / screenWidth);
	                int t_y = (int) (-1000 + y * 2000 / screenHeight);
	                Rect rect = new Rect();
	                rect.left = t_x - 150;
	                rect.top =  t_y - 150;
	                rect.right = t_x + 150;
	                rect.bottom =  t_y + 150;
	                rect.left = rect.left < -1000 ? -1000 : rect.left;
	                rect.top = rect.top < -1000 ? -1000 : rect.top;
	                rect.right = rect.right > 1000 ? 1000 : rect.right;
	                rect.bottom = rect.bottom > 1000 ? 1000 : rect.bottom;
	                Log.i(TAG, "touch x:" + x + " y:" + y);
	                Log.i(TAG, "touch rect:" + rect.top + "," + rect.left +  "," + rect.bottom + "," + rect.right);
	                Camera.Area area = new Camera.Area(rect, 10);
	                List<Camera.Area> list = new ArrayList<Camera.Area>();
	                list.add(area);
	                Manager.setMeteringAreas(list);
	                break;
	            case MotionEvent.ACTION_UP:
	            	break;
	            }
	            return false;
	        }
	    });
	}


}