package cn.com.xpai;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
public class ProgressDialog {
	private ProgressPopupWindow progressPopupWindow;
	public ProgressDialog(Context context,OnDismissListener listener){
		this.progressPopupWindow = new ProgressPopupWindow(context);
		progressPopupWindow.setTitle("正在进行视频打点","关闭进度条");
		progressPopupWindow.setOnDissmissListener(listener);
	}
	
	public void show(){
		progressPopupWindow.setMax(100);
		progressPopupWindow.show();
	}
	
	public void updateProgress(int progress){
		progressPopupWindow.setProgress(progress);
	}
	
	public void dismiss(){
		progressPopupWindow.dismiss();	
	}
}
