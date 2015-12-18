package cn.com.xpai.demo.player;

import java.util.Timer;
import java.util.TimerTask;

import org.cnnt.player.Player;
import org.cnnt.player.Surface;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xpai.R;


public class DemoPlayer extends Activity implements OnGestureListener {
	private String TAG = "DemoPlayer";
	private Player player = null;
	private View mHideContainer;
	private SeekBar mSeekBar;
	private View imgForward;
	private TextView currentTime, totalTime;
	private View imgPlay;
	private View imgFullScreen;
	private String fileName;
	private int totalDuration = 0;
	private Handler handler;

	private SeekUpdater seekUpdater = null;
	private GestureDetector gestureDetector = null;
	private Timer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ==================设置全屏=========================
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// ===================设置亮度========================
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = 0.5F;

		setContentView(R.layout.player);

		findViews();
		gestureDetector = new GestureDetector(this);

		handler = new Handler() {
			int playCount = 0;
			boolean retry = false;
			public void handleMessage(Message msg) {
				if (!Thread.currentThread().isInterrupted()) {
					if (msg.what != Player.MSG_PROGRESS_UPDATE) {
						Log.v(TAG, "receive msg : " + msg.what);
					}
					switch (msg.what) {
					case Player.MSG_OPEN_OK:
						Log.i(TAG, "读取视频文件成功: " + fileName);
						Toast.makeText(getApplication(), "读取视频文件 " + fileName + " 成功!", Toast.LENGTH_LONG).show();
						startUpdateProgress();
						break;
					case Player.MSG_OPEN_ERROR:
						Log.e(TAG, "读取视频文件失败: " + fileName);
						Toast.makeText(getApplication(), "读取视频文件 " + fileName + " 失败!", Toast.LENGTH_LONG).show();
						//这里可以做一些错误处理，如显示错误提示信息
						if (retry) {
							//只有当播放过程中，网络错误才retry
							//第一次播放就失败不会执行到这里
							reOpenPlayer();
						}
						break;
					case Player.MSG_PROGRESS_UPDATE:
						if (seekUpdater != null)
							seekUpdater.refresh();
						break;
					case Player.MSG_PLAYER_STOPPED:
						Toast.makeText(getApplication(), "播放结束", Toast.LENGTH_LONG).show();
						//为了测试稳定性，循环不停地播放同一个文件
						//reOpenPlayer();
						playCount ++;
						Log.d(TAG, String.format("--------------------- play count: %d ------------", playCount));
						break;
					case Player.MSG_READ_ERROR:
						Toast.makeText(getApplication(), "读取数据错误", Toast.LENGTH_LONG).show();
						/*下面代码不是必须的而是为了演示在直播过程中网络发生错误时，自动重连*/
						retry = true;
						reOpenPlayer();
						break;
					}
				}
				super.handleMessage(msg);
			}
		};

		Uri tmpUri = (Uri) this.getIntent().getData();
		if (tmpUri != null) {
			fileName = tmpUri.getPath();
		} else {
			Bundle bundle = this.getIntent().getExtras();
			if (bundle != null) {
				fileName = bundle.getString("PATH");
			}
		}
		newPlayer();
	}
	
	Surface surface = null;
	private void newPlayer() {
		Log.v("41C_Player", "play url: " + fileName);
		player = new Player(getApplication(), handler, fileName);
		player.setFullscreenMode(Player.FullscreenMode.FULLSCREEN);
		surface = new Surface(getApplication(), player);
	
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.gravity = Gravity.CENTER;
		surface.setLayoutParams(params);

		FrameLayout frameContainer = (FrameLayout) findViewById(R.id.framecontainer);
		frameContainer.addView(surface);
	}
	
	private void reOpenPlayer() {
		//等2s后再试，以免在网络断开后频繁重试消耗大量资源。
		Log.i(TAG, "reoOpenPlayer");
		try {
			Thread.currentThread().sleep(2000);
		} catch (Exception e){
			
		}
		player.onDestroy();
		FrameLayout frameContainer = (FrameLayout) findViewById(R.id.framecontainer);
		frameContainer.removeView(surface);
		newPlayer();
	}

	private void startUpdateProgress() {
		totalDuration = player.getDuration();
		totalTime.setText(formatTime(totalDuration / 1000));
		if (seekUpdater == null) {
			seekUpdater = new SeekUpdater();
			seekUpdater.startIt();
		}
	}

	private String formatTime(long sec) {
		int h = (int) sec / 3600;
		int m = (int) (sec % 3600) / 60;
		int s = (int) sec % 60;
		if (h == 0)
			return String.format("%02d:%02d", m, s);
		else
			return String.format("%d:%02d:%02d", h, m, s);
	}

	private void findViews() {
		mSeekBar = (SeekBar) findViewById(R.id.progressbar);
		currentTime = (TextView) findViewById(R.id.currenttime);
		totalTime = (TextView) findViewById(R.id.totaltime);

		imgPlay = findViewById(R.id.img_vp_play);
		imgPlay.setOnClickListener(imgPlayListener);
		imgFullScreen = findViewById(R.id.fs_shadow);
		imgFullScreen.setOnClickListener(imgFSListener);
		mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
		mHideContainer = findViewById(R.id.hidecontainer);
		mHideContainer.setOnClickListener(mVisibleListener);
		imgForward = findViewById(R.id.img_vp_forward);
		imgForward.setOnClickListener(mForwardListener);
	}
	
	OnClickListener mForwardListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.i("41C_Player", "forward btn clicked");
		}
	};
	
	//切换全屏模式
	OnClickListener imgFSListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			player.toggleFullScreen();
		}
	};

	OnClickListener imgPlayListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			ImageView img = (ImageView) v;
			if (player != null) {
				if (player.isPlaying()) {
					img.setImageResource(R.drawable.vp_play);
					seekUpdater.stopIt();
					player.pause();
				} else {
					img.setImageResource(R.drawable.vp_pause);
					if (seekUpdater != null) {
						seekUpdater.startIt();
					}
					player.play();
				}
			}

		}
	};

	OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int totalTime, seekTo = 0;
			int progress = seekBar.getProgress();

			if (player != null) {
				totalTime = player.getDuration();
				seekTo = totalTime / 1000 * progress;
				player.seekTo(seekTo);
				if(seekUpdater != null)
					seekUpdater.startIt();
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			if(seekUpdater != null)
			   seekUpdater.stopIt();
		}
	};

	OnClickListener mVisibleListener = new OnClickListener() {
		public void onClick(View v) {
			Log.i("Test", "onClick mVisibleListener");
			if ((mHideContainer.getVisibility() == View.GONE)
					|| (mHideContainer.getVisibility() == View.INVISIBLE)) {
				if (seekUpdater != null) {
					seekUpdater.startIt();
				}
				mHideContainer.setVisibility(View.VISIBLE);
			} else {
				mHideContainer.setVisibility(View.INVISIBLE);
				if (seekUpdater != null)
					seekUpdater.stopIt();
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 * 播放窗口切换到后台
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (null != seekUpdater) {
			seekUpdater.stopIt();
		}
		player.onActivityPause();
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 * 播放窗口恢复到前台
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (null != seekUpdater) {
			seekUpdater.startIt();
		}
		//通知播放器以恢复播放画面
		player.onActivityResume();
	}

	protected void onDestroy() {
		super.onDestroy();
		player.onDestroy();
		if (null != seekUpdater) {
			seekUpdater.stopIt();
		}
	}

	private class SeekUpdater {
		public void startIt() {
			timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					handler.sendEmptyMessage(Player.MSG_PROGRESS_UPDATE);
				}
			}, 1000, 1000);
		}

		public void stopIt() {
			if (timer != null)
				timer.cancel();
		}

		public void refresh() {
			if (currentTime != null) {
				long playedDuration = 0;
				if (player != null)
					playedDuration = player.getCurrentPosition();

				currentTime.setText(formatTime(playedDuration / 1000));
				if (totalDuration != 0) {
					int progress = (int) ((1000 * playedDuration) / totalDuration);
					mSeekBar.setProgress(progress);
				}
			}
		}
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		Log.i("Test", "onFling");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Log.i("Test", "onSingleTapUp");
		if ((mHideContainer.getVisibility() == View.GONE)
				|| (mHideContainer.getVisibility() == View.INVISIBLE)) {
			if (seekUpdater != null) {
				seekUpdater.startIt();
			}
			mHideContainer.setVisibility(View.VISIBLE);

		} else {
			mHideContainer.setVisibility(View.INVISIBLE);
			if (seekUpdater != null)
				seekUpdater.stopIt();
		}
		return false;
	}
}
