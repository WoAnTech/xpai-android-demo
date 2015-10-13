package cn.com.xpai;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.SeekBar;

public class PopSeekbar extends PopupWindow {
	View view;
	ListView listView;
	Activity activity;
	private PopupWindow popupWindow;
	int minValue;
	int maxValue;
	PopSeekbar(Activity activity, int min, int max, int value, PopSeekbar.onChangeListener listener, String title) {
		super(activity.getBaseContext());
		view =  (View)activity.getLayoutInflater().inflate(R.layout.seek_bar, (ViewGroup)activity.findViewById(R.id.main_layout), false);
		view.setOnTouchListener(touchListener);
		setContentView(view);
		setFocusable(true);
		setAnimationStyle(R.style.AnimationFade);
		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		((TextView)view.findViewById(R.id.txt_title)).setText(title);
		((TextView)view.findViewById(R.id.txt_min)).setText(Integer.toString(min));
		((TextView)view.findViewById(R.id.txt_max)).setText(Integer.toString(max));
		SeekBar sb = (SeekBar)view.findViewById(R.id.seek_bar);
		sb.setMax(100);
		minValue = min;
		maxValue = max;
		sb.setProgress((int)(((float)(value - min)/max) * 100));
		this.listener = listener;
		sb.setOnSeekBarChangeListener(onChangeListener);
	}
	
	OnTouchListener touchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			 if (isShowing()) {
				 dismiss();
			 }
			 return true;
		}
	};
	
	public static abstract class onChangeListener {
		abstract void onChanged(int value);
	}
	
	private onChangeListener listener;
	OnSeekBarChangeListener onChangeListener = new  SeekBar.OnSeekBarChangeListener () {
		int progress;
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				if (null != listener) {
					this.progress = progress;
				}
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Log.i("A", "onStopTrackingTouch");
			int value = minValue + (int)(((float)progress / 100) * (maxValue - minValue));
			listener.onChanged(value);
		}
	};
}
