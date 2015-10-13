package cn.com.xpai;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopListView extends PopupWindow {
	View view;
	ListView listView;
	Activity activity;
	private PopupWindow popupWindow;
	
	PopListView(Activity activity, ListAdapter la, String title) {
		super(activity.getBaseContext());
		view =  (View)activity.getLayoutInflater().inflate(R.layout.setting_menu, (ViewGroup)activity.findViewById(R.id.main_layout), false);
		listView = (ListView)view.findViewById(R.id.menu_list);
		view.setOnTouchListener(touchListener);
		listView.setAdapter(la);
		if (la instanceof SettingItemAdapter) {
			listView.setOnItemClickListener((SettingItemAdapter)la);
		}
		setContentView(view);
		setFocusable(true);
		setAnimationStyle(R.style.AnimationFade);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//listview的wrap_content无效，必须手动计算并设定listview宽度
		listView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		listView.getLayoutParams().width = listView.getMeasuredWidth();
		((TextView)view.findViewById(R.id.txt_title)).setText(title);
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
