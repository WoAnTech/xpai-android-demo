package cn.com.xpai;

import cn.com.xpai.core.Transcoder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TranscoderImpl extends Transcoder{
	
	private static final String TAG = "TranscoderImpl";
	private Handler mHandler;
	public static final int UPDATE_TRANSCODE_PROGRESS = 0x10001;
	public static final int TRANSCODE_FINISHED = 0x10002;
	
	public TranscoderImpl(Handler handler) {
		mHandler = handler;
	}
	
	@Override
	public boolean onTransVideoFileUpdate(int progress) {
		// TODO Auto-generated method stub
		Message msg = mHandler.obtainMessage();
		msg.what = UPDATE_TRANSCODE_PROGRESS;
		msg.arg1 = progress;
		mHandler.sendMessage(msg);
		return true;
	}

	@Override
	public boolean onTransVideoFileFinished() {
		// TODO Auto-generated method stub
		//destroy();
		mHandler.sendEmptyMessage(TRANSCODE_FINISHED);
		return true;
	}

}
