package cn.com.xpai.demo.player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


import cn.com.xpai.R;
import cn.com.xpai.R.id;
import cn.com.xpai.R.layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class FilelistActivity extends Activity {

	private static final String TAG = "FilelistActivity";
	private ListView mViewFilelist;
	private FilelistAdapter mAdapterFilelist;
	private ArrayList<VideoFile> mDataFilelist = new ArrayList<VideoFile>();
	private View mViewFileRefresh;
	private TextView mViewScanInfo;
	private Handler mHandler;
	private Context mContext = FilelistActivity.this;
	private int mNumFreshThread = 0;
	
	private  final int MSG_UPDATA_LIST  = 0;
	private  final int MSG_UPDATA_FINISH= 1;
	private  final int MSG_UPDATA_INIT	= 2;
	private  final int MSG_UPDATA_INFO	= 3;
	private Button mInputBtn;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ==================设置全屏=========================
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.file_list);
		findViews();
		setListen();

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (!Thread.currentThread().isInterrupted()) {
					switch (msg.what) {
					case MSG_UPDATA_INIT:
						Log.i(TAG, "MSG_UPDATA_INIT");
						mViewScanInfo.setVisibility(View.VISIBLE);
						mViewScanInfo.setText("列表更新中...");
						break;
					case MSG_UPDATA_INFO:
						Log.i(TAG, "MSG_UPDATA_INFO");
//						mViewScanInfo.setText("扫描：" + msg.obj);
						break;
					case MSG_UPDATA_LIST:
						/*
						在这里通知数据集更新会有异常，
						java.lang.IllegalStateException: 
							The content of the adapter has changed but ListView did not receive a notification. Make sure the content of your adapter is not modified from a background thread, but only from the UI thread. [in ListView(2131099657, class android.widget.ListView) with Adapter(class cn.com.xpai.demo.player.FilelistAdapter)]
						除非更新时暂停后台线程改变数据集
						*/
						//mAdapterFilelist.notifyDataSetChanged();
						break;
					case MSG_UPDATA_FINISH:
						Collections.sort(mDataFilelist);
						mAdapterFilelist.notifyDataSetChanged();
						Globals.setSettingKeyString("firstStart", "false", mContext);
						mViewScanInfo.setVisibility(View.GONE);
						break;
					}
				}
				super.handleMessage(msg);
			}
		};

		//if(Globals.getSettingKeyString("firstStart", mContext).equals("")){
			FilelistRefreshThread t = new FilelistRefreshThread(mHandler);
			t.start();
		//}
	}

	private void findViews() {
		mViewFilelist = (ListView) findViewById(R.id.fileListView);
		mViewFileRefresh = (View) findViewById(R.id.fileRefreshView);
		mViewScanInfo = (TextView) findViewById(R.id.scanInfoView);
		mInputBtn = (Button) findViewById(R.id.button_input);
	}

	private void setListen() {
		mDataFilelist = Globals.getFilelist(mContext);
		Globals.ShowLog("mDataFilelist:" + mDataFilelist.size());
		mAdapterFilelist = new FilelistAdapter(mDataFilelist, mContext);
		mViewFilelist.setAdapter(mAdapterFilelist);
		mViewFilelist.setOnItemClickListener(filelistOnItemClickListener);
		mViewFileRefresh.setOnClickListener(fileRefreshOnClickListener);
	}

	private OnClickListener fileRefreshOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			FilelistRefreshThread t = new FilelistRefreshThread(mHandler);
			t.start();
		}
		
	};
	
	private  DialogInterface.OnClickListener inputUrlOkListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface arg0, int arg1) {
			AlertDialog dialog2 =AlertDialog.class.cast(arg0);
			EditText edit = (EditText) dialog2.findViewById(1);
			input_url = edit.getText().toString();
			Log.i("Player", "input url: " + input_url);
			VideoFile vf = new VideoFile("", input_url, "", 0);
			startPlayer(vf);
		}
	};
	
	private String input_url = null;
	public void onInputBtnClick(View view) {
			EditText text_input = new EditText(this);
			text_input.setId(1);
			if (null == input_url) {
				//text_input.setText("live_http://");
			} else {
				text_input.setText(input_url);
			}
			AlertDialog.Builder db = new AlertDialog.Builder(this);
			db.setTitle("请输入播放地址").setIcon(
				     android.R.drawable.ic_dialog_info).
				     	setView(text_input).setPositiveButton("播放", this.inputUrlOkListener)
				     .setNegativeButton("取消", null).show();
	};
	
	private OnItemClickListener filelistOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			VideoFile v = mDataFilelist.get(position);
			startPlayer(v);

		}
	};

	private void startPlayer(VideoFile video) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();

		bundle.putString("TITLE", video.getName());
		bundle.putString("PATH", video.getPath());
		intent.putExtras(bundle);
		intent.setClass(FilelistActivity.this, DemoPlayer.class);
		startActivity(intent);
	}

	class FilelistRefreshThread extends Thread {

		private ArrayList<String> mFilelist = new ArrayList<String>();
		private Handler mHandler;

		public FilelistRefreshThread(Handler handler) {
			super();
			mHandler = handler;
		}

		@Override
		public void run() {
			synchronized (this){
				if(mNumFreshThread >0)
					return ;
				mNumFreshThread += 1;
			}		
			mHandler.sendEmptyMessage(MSG_UPDATA_INIT);
			String sdcard = Globals.getSdcardPath();
			getFileList(sdcard, mFilelist);
			mHandler.sendEmptyMessage(MSG_UPDATA_FINISH);
			synchronized (this){
				mNumFreshThread -= 1;
			}
		}

		final private void getFileList(String strPath, ArrayList<String> filelist) {
			File dir = new File(strPath);
			File[] files = dir.listFiles();

			if (files == null)
				return;
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					String dirName = files[i].getName();
					// ==============过滤掉隐藏文件夹==================
					if (!dirName.startsWith(".")){
						Message msg = mHandler.obtainMessage(MSG_UPDATA_INFO, files[i].getAbsolutePath());
						//mHandler.sendMessage(msg);
						getFileList(files[i].getAbsolutePath(), filelist);
					}
				} else {
					String fullPath = files[i].getAbsolutePath().toLowerCase();
					String prefix = Globals.getExtensionName(fullPath);
					if (Globals.getFileFilter().contains(prefix)) {
						// String fileName = Globals.getFileName(fullPath);
						File f = new File(fullPath);
						String fileName = f.getName();
						long size = f.length();
						String sql = "select size from "
								+ Globals.TABLE_PLAYLIST + " where path='"
								+ fullPath + "'";
						int exist = Globals.execSqlRetInt(sql, mContext);
						if (exist < 0) {
							sql = "insert into " + Globals.TABLE_PLAYLIST
									+ " (name, path, pic ,size ) values ('"
									+ fileName + "', '" + fullPath + "', '',"
									+ size + ")";
							Globals.execSql(sql, mContext);
							
							mDataFilelist.add(new VideoFile(fileName, fullPath, "", (int)size));
							mHandler.sendEmptyMessage(MSG_UPDATA_LIST);
						}
					}

				}
			}
		}

	}

}
