package cn.com.xpai;

import cn.com.xpai.core.AHandler;
import cn.com.xpai.core.Const;
import cn.com.xpai.core.Manager;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class XPHandler extends AHandler {

	private static final String TAG = "XPHandler";
	public static final int MSG_SHOW_CONNECTION_DIALOG = 0x10002;
	public static final int ERR_NOT_REGISTER_DIALOG_FACTORY = 0x10003;
	public static final int ERR_CONNECTION_TIMEOUT = 0x10005;
	public static final int SUCCESS_AUTH = 0x10013;

	public static final int EXIT_APP = 0x10021;
	public static final int APP_PREPARE_RECORD = 0x10022;
	public static final int APP_START_RECORD = 0x10023;
	public static final int APP_STOP_RECORD = 0x10024;

	public static final int NEED_LOGIN = 0x10031;
	public static final int SHOW_MESSAGE = 0x10032;
	public static final int MSG_CONNECTED = 0x10033;
	
	public boolean isRetryRecord = false;
	public boolean isStartRecord = false;
	boolean isConnected = false;

	private static XPHandler instance = null;

	private static Context mContext = null;
	
	static final String MSG_CONTENT = "msg_content";

	public static void register(Context context) {
		mContext = context;
		getInstance();
	}

	public static XPHandler getInstance() {
		if (instance == null) {
			instance = new XPHandler();
		}
		return instance;
	}

	public Context getContext() {
		return mContext;
	}

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_SHOW_CONNECTION_DIALOG:
			DialogFactory.getInstance(DialogFactory.CONNECTION_DIALOG).show();
			break;
		case ERR_NOT_REGISTER_DIALOG_FACTORY:
			DialogFactory.register(mContext);
			break;
		case ERR_CONNECTION_TIMEOUT:
			Toast.makeText(mContext, "Connection timeout!", Toast.LENGTH_SHORT)
					.show();
			DialogFactory.getInstance(DialogFactory.CONNECTION_FAILED_DIALOG,
					(String) msg.obj).show();
			break;
		case EXIT_APP:
			break;
		case NEED_LOGIN:
			Toast.makeText(mContext, "need login!", Toast.LENGTH_SHORT)
			.show();
			DialogFactory.getInstance(DialogFactory.LOGIN_DIALOG).show();
			break;
		case SHOW_MESSAGE:
			Bundle bdl = msg.getData();
			String text = bdl.getString("msg_content");
			Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
		super.handleMessage(msg);
	}

	//收到服务器的握手信息，在此方法中发出认证请求
	@Override
	public boolean onHandshake() {
		Log.d(TAG, "onHandshake");
		Message msg = new Message();
		msg.what = NEED_LOGIN;
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}

	public void exitApp() {
		if (isRetryRecord || isStartRecord) {
			Manager.stopRecord();
		}
	}

	//用户认证结果通知
	@Override
	public boolean onAuthResponse(boolean auth_result) {
		if (auth_result) {
			Log.i(TAG, "Login ok");
			Message msg = new Message();
			msg.what = SHOW_MESSAGE;
			Bundle bdl = msg.getData();
			bdl.putString(MSG_CONTENT, "登录成功, 请按MENU键继续操作!");
			msg.setData(bdl);
			XPHandler.getInstance().sendMessage(msg);
		} else {
			Log.i(TAG, "Login failed");
			Message msg = new Message();
			msg.what = NEED_LOGIN;
			XPHandler.getInstance().sendMessage(msg);
		}
		return true;
	}

	//连接成功通知
	@Override
	public boolean onConnected() {
		Log.i(TAG, "Connect to server successfully");
		isConnected = true;
		Message msg = new Message();
		msg.what = MainHandler.MSG_NETWORK_CONNECTED;
		XPAndroid.mainHandler.sendMessage(msg);
		return true;
	}

	//收到网络异常通知
	@Override
	public boolean onConnectFail(int error_no) {
		Log.i(TAG, "connection lost");
		if (isConnected) {
			Message msg = new Message();
			msg.what = MainHandler.MSG_NETWORK_DISCONNECT;
			msg.arg1 = error_no;
			XPAndroid.mainHandler.sendMessage(msg);
		} else {
			//建立连接失败，显示对话框让用户确认连接参数
			Message msg = new Message();
			msg.what = ERR_CONNECTION_TIMEOUT;
			XPHandler.getInstance().sendMessage(msg );
		}
		return true;
	}
	
	//收到服务器为当前录制的直播视频分配的ID
	@Override
	public boolean onStreamIdNotify(String stream_id) {
		Log.i(TAG, "Get Stream ID: " + stream_id);
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		bdl.putString(MSG_CONTENT, "Got Stream ID:" + stream_id);
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}

	//收到文件上传开始通知
	@Override
	public boolean onUploadFileStart(String file_name) {
		Log.i(TAG, "onUploadFileStart: " + file_name);
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		bdl.putString(MSG_CONTENT, "Start uploading file:" + file_name);
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}
	
	//收到文件上传结束通知
	@Override
	public boolean onUploadFileEnd(String file_id, String file_name) {
		Log.i(TAG, "onUploadFileEnd: " + file_id + " " + file_name);
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		bdl.putString(MSG_CONTENT, "Finished uploading file:" + file_id);
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}

	//收到照片文件生成通知
	@Override
	public boolean onTakePicture(String file_name) {
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		if (null == file_name) {
			Log.i(TAG, "take picture fail" );
			bdl.putString(MSG_CONTENT, "Take picture failed");
		} else {
			Log.i(TAG, "take picture ok: " + file_name);
			bdl.putString(MSG_CONTENT, "Take picture: " + file_name);
		}
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		XPAndroid.lastPictureFileName = file_name;
		return true;
	}
	
	//收到服务器传来的文本消息
	@Override
	public boolean onRecvTextMessage(String from, String what) {
		String text = String.format("Recv text msg from %s: %s", from, what);
		Log.i(TAG, text);
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		bdl.putString(MSG_CONTENT, text);
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}
	
	//收到录制停止通知
	@Override
	public boolean onRecordFinished(long data_size, int duration) {
		String text = String.format("Finished Record %s, %s", data_size, duration);
		Log.i(TAG, text);
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		bdl.putString(MSG_CONTENT, text);
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}
	
	//收到视频上传完毕通知
	@Override
	public boolean onStreamUploaded(String stream_id) {
		String text = String.format("Finished uploading stream %s", stream_id);
		Log.i(TAG, text);
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		bdl.putString(MSG_CONTENT, text);
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}
	
	//收到本地视频文件名及路径的通知
	@Override
	public boolean onLocalFilename(String fname) {
		String text = String.format("Video File name:%s", fname);
		Log.i(TAG, text);
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		bdl.putString(MSG_CONTENT, text);
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}
	
	/*
	 * 续传或上传离线视频文件解析开始通知
	 * 续传或上传离线文件分为两个步骤， 一个步骤是对视频文件的解析，另一个步骤是向服务器传送数据
	 * 这里是解析步骤开始的回调
	 * @param duration 视频文件时长
	 */
	public boolean onParseVideoFileStart(int duration, int file_size) {
		String text = String.format("parse Video File start, duration: %d, file size: %d"
				,duration, file_size);
		Log.i(TAG, text);
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		bdl.putString(MSG_CONTENT, text);
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}
	
	
	/*
	 * 续传或上传离线视频文件解析结束通知
	 * 当此回调发生时不代表视频已经全部上传完毕，
	 * 视频上传完毕的条件是：收到此回调后，并且Manager.getCacheRemaining() 为零才是全部上传完成
	 * @audio_pkt_cnt 音频包数目
	 * @video_pkt_cnt 视频包数目
	 * @data_size 数据总量(byte为单位)
	 * 
	 */
	public boolean onParseVideoFileEnd(int audio_pkt_cnt, int video_pkt_cnt, int data_size) {
		String text = String.format("parse Video File end,  audio pkt: %d, video pkt: %d data_size: %d", 
				audio_pkt_cnt, video_pkt_cnt, data_size);
		Log.i(TAG, text);
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		bdl.putString(MSG_CONTENT, text);
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}
	
	/*
	 * 续传或上传离线文件解析进度更新
	 * @param processed_data_size 已经完成处理的数据量
	 * @param file_size 文件大小
	 */
	
	int last_progress = 0;
	public boolean onParseVideoFileUpdate(int processed_data_size, int file_size) {
		//这是计算进度的简单算法, 注意计算进度时要考虑到发送cache中未发送的数据量
		float fp = ((float)(processed_data_size)) / (file_size + Manager.getCacheRemaining());
		int progress = (int) (fp * 100);
		if (last_progress == progress) {
			return true; //避免打印过多重复的日志信息
		}
		last_progress = progress;
		String text = String.format("uploading progress: %d",  progress);
		Log.d(TAG, text);
		return true;
	}
	
	//收到本地视频文件名及路径的通知
	@Override
	public boolean onRecvAudioMessage(int data_size, String fname) {
		String text = String.format("Recv Audio Message, size: %d, name: %s",
				data_size, fname);
		Log.i(TAG, text);
		Message msg = new Message();
		msg.what = SHOW_MESSAGE;
		Bundle bdl = new Bundle();
		bdl.putString(MSG_CONTENT, text);
		msg.setData(bdl);
		XPHandler.getInstance().sendMessage(msg);
		return true;
	}

	 /*
     * 断线重连失败，此视频无法续传,可能原因是续传已经超时
     */
    @Override
    public boolean onResumeLiveFail(int error_code) {
            Message msg = new Message();
            msg.what = SHOW_MESSAGE;
            Bundle bdl = new Bundle();
            bdl.putString(MSG_CONTENT, "重连失败，当前直播无法续传，放弃重连服务器!");
            msg.setData(bdl);
            XPHandler.getInstance().sendMessage(msg);
            return true;
    }

    /*
     * 正在尝试断线续传
     */
    @Override
    public boolean onTryResumeLive() {
            Log.i(TAG, "网络错误，正在尝试恢复直播");
            return true;
    }

    public void onResumeLiveOk() {
            Message msg = new Message();
            msg.what = SHOW_MESSAGE;
            Bundle bdl = new Bundle();
            bdl.putString(MSG_CONTENT, "恢复直播成功");
            msg.setData(bdl);
            XPHandler.getInstance().sendMessage(msg);
            //自动测试断线重连, 过5秒后自动断线
            //getInstance().sendEmptyMessageDelayed(MSG_DELAY_DISCONNECT, 5000);
    }

}
