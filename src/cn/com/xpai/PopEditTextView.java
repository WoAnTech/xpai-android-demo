package cn.com.xpai;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopEditTextView extends PopupWindow {
	View view;
	EditText editText;
	Activity activity;
	private onFinishListener listener;
	
	PopEditTextView(Activity activity, String title, onFinishListener listener) {
		super(activity.getBaseContext());
		view =  (View)activity.getLayoutInflater().inflate(R.layout.setting_edittext, (ViewGroup)activity.findViewById(R.id.main_layout), false);
		editText = (EditText)view.findViewById(R.id.editText);
		view.setOnTouchListener(touchListener);
		this.listener = listener;
		setContentView(view);
		setFocusable(true);
		setAnimationStyle(R.style.AnimationFade);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		((TextView)view.findViewById(R.id.txt_title)).setText(title);
		editText.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				String output_tag = editText.getText().toString().trim();
				//if(output_tag != null && !"".equals(output_tag))
				PopEditTextView.this.listener.onFinished(output_tag);
			}
		});
	}
	
	public static abstract class onFinishListener {
		abstract void onFinished(String output_tag);
	}
	
	
	OnTouchListener touchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			 if (isShowing()) {
				 dismiss();
			 }
			 return true;
		}
	};
}
