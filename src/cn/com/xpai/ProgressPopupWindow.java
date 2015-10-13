package cn.com.xpai;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.com.xpai.core.Manager;

public class ProgressPopupWindow {
	private TextView titleTv;
	private ProgressBar progressBar;
	private TextView progressTv;
	private Button cancleBtn;
	private Dialog dialog;
	public ProgressPopupWindow(Context context) {
		View progressPopupWindowLayout = (View)LayoutInflater.from(context).inflate(R.layout.progress_popup_window_layout, null);
		titleTv = (TextView) progressPopupWindowLayout.findViewById(R.id.progress_popup_window_layout_title);
		progressTv = (TextView) progressPopupWindowLayout.findViewById(R.id.progress_popup_window_layout_progress_tv);
		progressBar = (ProgressBar) progressPopupWindowLayout.findViewById(R.id.progress_popup_window_layout_progress_bar);
		cancleBtn = (Button) progressPopupWindowLayout.findViewById(R.id.progress_popup_window_layout_cancle_btn);
		// 取消单击按钮单击事件
		cancleBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//UploadVideoFile.stopUpLoad();
				//Manager.stopRecord();
				dialog.dismiss();
			}
		});
		dialog = new Dialog(context, android.R.style.Theme_Panel);
		//dialog.setCancelable(false);
		dialog.setContentView(progressPopupWindowLayout);
		progressBar.setMax(100);
	}

	/**
	 * 设置进度窗口标题
	 */
	public void setTitle(String title,String btnText) {
		titleTv.setText(title);
		cancleBtn.setText(btnText);
	}

	/**
	 * 设置进度总量
	 */
	public void setMax(int max) {
		progressBar.setMax(max);
	}

	/**
	 * 设置进度
	 */
	public void setProgress(int progress) {
		progressTv.setText(progress + "%");
		progressBar.setProgress(progress);
	}
	
	public void setOnDissmissListener(OnDismissListener listener){
		dialog.setOnDismissListener(listener);
	}

	/**
	 * 显示进度窗口
	 */
	public void show() {
		if(dialog.isShowing())return;
		dialog.show();
	}

	/**
	 * 销毁进度窗口
	 */
	public void dismiss() {
		dialog.dismiss();
	}
}
