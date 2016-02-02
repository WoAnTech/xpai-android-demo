package cn.com.xpai;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;
import cn.com.xpai.core.Manager;
import cn.com.xpai.core.Manager.CameraID;
import cn.com.xpai.core.RecordMode;
import cn.com.xpai.demo.player.FilelistActivity;

class MainHandler extends Handler {
	Button btnChangeCamera = null;
	Button btnRecordPause = null;
	RecordButton btnRecord = null;
	TextView txtDuration = null;
	TextView txtFps = null;
	TextView txtNetSpeed = null;
	TextView txtCache = null;
	TextView txtSdkVer = null;
	TextView txtBytesSent = null;
	Button btnConn = null;
	Button btnPreview = null;
	Button btnPlayer = null;
	Button btnSetting = null;
	Button btnTakePicture = null;
	PopListView settingMenu = null;
	Button btnMute = null;
	Activity activity = null;
	final Animation netAnimation;
	final Animation	pauseAnimation;
	public final static int MSG_UPDATE_INFO = 11000;
	public final static int MSG_SWITCH_BTN_VISIBILITY = 11001;
	public final static int MSG_NETWORK_CONNECTED = 11002;
	public final static int MSG_NETWORK_DISCONNECT = 11003;
	
	private final static String TAG = "MainHandler";
	private int currentZoomLevel = 0, maxZoomLevel = 0;

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_UPDATE_INFO:
			updateRunInfo();
			break;
		case MSG_SWITCH_BTN_VISIBILITY:
			switchBtnVisibility();
			break;
		case MSG_NETWORK_CONNECTED:
			Toast.makeText(activity.getBaseContext(), "成功建立网络连接",
					Toast.LENGTH_SHORT).show();
			btnConn.setBackgroundResource(R.drawable.link);
			if (btnConn.getAnimation() != null && btnConn.getAnimation().hasStarted()) {
				btnConn.getAnimation().cancel();
				btnConn.getAnimation().reset();
			}
			break;
		case MSG_NETWORK_DISCONNECT:
			Toast.makeText(activity.getBaseContext(), "失去网络连接, error:" + msg.arg1,
					Toast.LENGTH_SHORT).show();
			btnConn.setBackgroundResource(R.drawable.link_break);
			if (btnConn.getAnimation() == null || btnConn.getAnimation().hasEnded()) {
				btnConn.startAnimation(netAnimation);
			}
			break;
		default:
			super.handleMessage(msg);
		}
	}
	
	/*
	 * 根据预览和录制状态更新各按钮显示
	 */
	final private void switchBtnVisibility() {
		if (Manager.isPreviewing() && null == btnPreview.getTag()) {
			btnPreview.setBackgroundResource(R.drawable.preview);
			btnPreview.setTag(new Object());
		} else if (!Manager.isPreviewing() && null != btnPreview.getTag()) {
			btnPreview.setBackgroundResource(R.drawable.preview_inactive);
			btnPreview.setTag(null);
		}
		if (Manager.isPreviewing()) {
			btnTakePicture.setVisibility(View.VISIBLE);
		} else {
			btnTakePicture.setVisibility(View.INVISIBLE);
		}
		switch (Manager.getRecordStatus()) {
		case IDLE:
			btnPreview.setVisibility(View.VISIBLE);
			btnChangeCamera.setVisibility(View.VISIBLE);
			btnSetting.setVisibility(View.VISIBLE);
			btnPlayer.setVisibility(View.VISIBLE);
			btnChangeCamera.setVisibility(View.VISIBLE);
			if (pauseAnimation.hasStarted()) {
				pauseAnimation.cancel();
				pauseAnimation.reset();
			}
			btnRecordPause.clearAnimation();
			btnRecordPause.setVisibility(View.INVISIBLE);
			btnMute.setVisibility(View.INVISIBLE);
			break;
		case RECORDING:
			btnPreview.setVisibility(View.INVISIBLE);
			//btnChangeCamera.setVisibility(View.INVISIBLE);
			btnSetting.setVisibility(View.INVISIBLE);
			//btnPlayer.setVisibility(View.INVISIBLE);
			//btnChangeCamera.setVisibility(View.INVISIBLE);
			btnRecordPause.setVisibility(View.VISIBLE);
			btnMute.setVisibility(View.VISIBLE);
			if (pauseAnimation.hasStarted()) {
				pauseAnimation.cancel();
				pauseAnimation.reset();
			}
			break;
		default:
		}
	}

	final private void updateRunInfo() {
		if (Manager.RecordStatus.RECORDING == Manager.getRecordStatus()) {
			txtDuration.setVisibility(View.VISIBLE);
			txtFps.setVisibility(View.VISIBLE);
			txtDuration.setText(String.format("Duration: %.2f",
					(float) Manager.getRecordDuration() / 1000));
			txtFps.setText(String.format("FPS: %d", Manager.getCurrentFPS()));
		} 
		btnRecord.update();
		txtNetSpeed.setText(String.format("Net: %.2f KBps",
				(float) Manager.getUploadingSpeed() / 1024));
		txtCache.setText(String.format("Cache: %.2f KByte",
				(float) Manager.getCacheRemaining() / 1024));
		txtBytesSent.setText(String.format("Sent: %.2f KByte", 
				(float) Manager.getBytesSent() / 1024));
	}

	private Timer mTimer = new Timer();// 定时器

	private View.OnClickListener btnConnListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (Manager.isConnected()) {
				// 调用对话框，询问是否要断开连接
				String title = "断开网络连接";
				if (Manager.RecordStatus.RECORDING == Manager.getRecordStatus()) {
					title = "模拟断网测试";
				}
				DialogFactory.confirmDialog("模拟断网", "确认要断开网络连接？",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Manager.disconnect();
							}
						});
			} else {
				// 调用建立连接对话框
				DialogFactory.getInstance(DialogFactory.CONNECTION_DIALOG)
						.show();
			}
		}
	};
	
	private View.OnClickListener btnPauseListen = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			switch (Manager.getRecordStatus()) {
			case RECORDING:
				Manager.pauseRecord();
				btnRecordPause.startAnimation(pauseAnimation);
				switchBtnVisibility();
				break;
			case PAUSE:
				Manager.resumeRecord();
				pauseAnimation.cancel();
				pauseAnimation.reset();
				switchBtnVisibility();
				break;
			default:
			}
		}
	};

	private View.OnClickListener btnTakePictureListener = new OnClickListener() {
		
		Camera.Size picSize = null;
		@Override
		public void onClick(View arg0) {
			if (Manager.isPreviewing()) {
				if (null == picSize) {
					List <Camera.Size> size_list = Manager.getSupportedPictureSizes();
					if (null != size_list) {
						for (int i= 0; i < size_list.size(); i++) {
							Camera.Size pic_size = size_list.get(i);
							//Demo中只简单地在日志中打印出支持的拍照图片的大小，
							//实际应用中可以提前通过此API提供一个列表选项给用户选择，并保存用户的设置
							Log.i(TAG, 
									String.format("support picture size :%dx%d", 
											pic_size.width, pic_size.height));
							if (null == picSize) {
								picSize = pic_size;
							} else {
								//Demo选取最小尺寸的拍照图片大小
								if (picSize.width > pic_size.width) {
									picSize = pic_size;
								}
							}
						}
					} else {
						Log.w(TAG, "Can't supported take picture");
					}
				}
				if (null != picSize) {
					Manager.takePicture("/sdcard/xpai", picSize.width, picSize.height);
				}
			}
		}
	};
	
	private View.OnClickListener btnPreviewListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (Manager.isPreviewing()) {
				Manager.stopPreview();
			} else {
				Manager.startPreview();
			}
			switchBtnVisibility();
		}
	};
	
	private View.OnClickListener btnMuteListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Log.i(TAG, "mute btn clicked");
			if(Config.recordMode == RecordMode.HwOnlyVideo || Config.recordMode == RecordMode.HwVideoSwAudio
					||Config.recordMode == RecordMode.SwAudioAndVideo || Config.recordMode == RecordMode.SwOnlyAudio) {
				boolean onoff = Manager.isMute();
				if (onoff) {
					btnMute.setBackgroundResource(R.drawable.mic_mute);
				} else {
					btnMute.setBackgroundResource(R.drawable.mic);
				}
				Manager.toggleMute(!onoff);
			} else {
				Toast.makeText(activity.getBaseContext(), "只有音频为软编的模式下才能进行麦克风操作！", Toast.LENGTH_SHORT)
				.show();
			}
		}
	};
	
	private View.OnClickListener btnPlayerListen = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(activity, FilelistActivity.class);
			activity.startActivity(intent);
		}
	};
	
	private View.OnClickListener btnSettingListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (null == settingMenu) {
				settingMenu = new PopListView(activity, new SettingItemAdapter(activity), "设置选项");
			}
			settingMenu.showAtLocation(v, Gravity.LEFT, 30, 10);
		}
	};
	
	private View.OnClickListener btnChangeCameraListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			boolean ret = false;
			if (Manager.getCurrentCameraId() == CameraID.CAMERA_BACK) {
				ret = Manager.switchCamera(CameraID.CAMERA_FRONT);
			} else {
				ret = Manager.switchCamera(CameraID.CAMERA_BACK);
			}
		}
	};
	
	public MainHandler(Activity activity) {
		btnChangeCamera = (Button) activity.findViewById(R.id.btn_change_camera);
		btnRecord = (RecordButton)activity.findViewById(R.id.btn_record);
		txtDuration = (TextView) activity.findViewById(R.id.txt_record_duration);
		txtFps = (TextView) activity.findViewById(R.id.txt_frame_rate);
		txtNetSpeed = (TextView) activity.findViewById(R.id.txt_net_speed);
		txtBytesSent = (TextView) activity.findViewById(R.id.txt_bytes_sent);
		txtCache = (TextView) activity.findViewById(R.id.txt_cache_remain);
		txtSdkVer = (TextView) activity.findViewById(R.id.txt_sdk_ver);
		btnConn = (Button) activity.findViewById(R.id.btn_connection);
		btnPreview = (Button) activity.findViewById(R.id.btn_preview);
		btnPlayer = (Button) activity.findViewById(R.id.btn_player);
		btnSetting = (Button) activity.findViewById(R.id.btn_setting);
		btnConn.setOnClickListener(btnConnListener);
		btnPreview.setOnClickListener(btnPreviewListener);
		btnPlayer.setOnClickListener(btnPlayerListen);
		btnSetting.setOnClickListener(btnSettingListener);
		btnChangeCamera.setOnClickListener(btnChangeCameraListener);
		btnRecordPause = (Button) activity.findViewById(R.id.btn_record_pause);
		btnRecordPause.setOnClickListener(btnPauseListen);
		btnTakePicture = (Button) activity.findViewById(R.id.btn_take_picture);
		btnTakePicture.setOnClickListener(btnTakePictureListener);
		btnMute = (Button) activity.findViewById(R.id.btn_mute);
		btnMute.setOnClickListener(btnMuteListener);
		
		this.activity = activity;
		
		txtSdkVer.setText("Sdk Ver: " + Manager.getSdkVersion());
		settingMenu = null;
		
		netAnimation = new FlickAnimation(500);
		btnConn.startAnimation(netAnimation);

		pauseAnimation = new FlickAnimation(200);
		
	    ZoomControls zoomControls = (ZoomControls) activity.findViewById(R.id.camera_zoom_control);
	    if(Manager.isZoomSupported()) {
	        maxZoomLevel = Manager.getMaxZoomLevel();
	        zoomControls.setIsZoomInEnabled(true);
	        zoomControls.setIsZoomOutEnabled(true);
	        zoomControls.setOnZoomInClickListener( new OnClickListener() {
	        	@Override
	        	public void onClick(View v) {
	        		if(currentZoomLevel++ < maxZoomLevel) {
	        			Manager.setZoom(currentZoomLevel, true);
	        		} else {
	        			currentZoomLevel = maxZoomLevel;
	        			Toast.makeText(MainHandler.this.activity.getBaseContext(), "画面已放至最大！", Toast.LENGTH_SHORT)
	    				.show();
	        		}
	        	}
	        });

	        zoomControls.setOnZoomOutClickListener(new OnClickListener() {
	        	@Override
	        	public void onClick(View v) {
	        		if(currentZoomLevel > 0) {
	        			Manager.setZoom(currentZoomLevel--, true);
	        		} else {
	        			Toast.makeText(MainHandler.this.activity.getBaseContext(), "画面已缩至最小！", Toast.LENGTH_SHORT)
	    				.show();
	        		}
	        	}
	        });  
	     } else {
	         zoomControls.setVisibility(View.GONE);
	     }
		
		// 创建定时线程执行更新任务
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				sendEmptyMessage(MSG_UPDATE_INFO);// 向Handler发送消息
			}
		}, 100, 500);// 定时任务
	}
	
	//闪烁动画
	class FlickAnimation extends AlphaAnimation {
		FlickAnimation(int duration) {
			super(1, 0); 
			setDuration(duration); // duration - half a second
			setInterpolator(new LinearInterpolator()); 
			setRepeatCount(Animation.INFINITE); // Repeat animation
			setRepeatMode(Animation.REVERSE); 
		}
	}
}