package cn.com.xpai;

import android.app.Activity;
import android.content.SharedPreferences;
import cn.com.xpai.core.Manager.AudioEncoderType;
import cn.com.xpai.core.RecordMode;

public class Config {

	final static String PREFS_NAME = "XPAndroid";

	static String userName = "";
	static String userPass = ""; //for test, not been encrypt.
	static int timeOut = 5 * 1000;
	static int retryConnectTimes = 3;

	static String mvHost = "";
	static String mvPort = "";
	static String mvHost_default = "ps.zhiboyun.com";
	static String mvPort_default = "9999";
	//通过connectCloud api与直播云建立连接
	static String getVSUrl = "http://c.zhiboyun.com/api/20140928/get_vs";
	//连接私有云(其中192.168.1.1只是个例子，具体填写私有云服务器地址)
	static String privateCloudGetVSUrl = "http://192.168.1.1/api/20140928/get_vs";
	static String serviceCode = "";
	static String output_tag = "";
	static boolean isOpenNetWorkingAdaptive = true;
	static boolean isSavingVideoFile = false;
	static boolean isConnectToTcpPort = false;
	static int connectionMode = 0;//0:连接直播云 1:连接私有云 2:连接视频服务器
	//音频编码类型 默认为AMR_NB
	static AudioEncoderType audioEncoderType = AudioEncoderType.AMR_NB;
	//声道
	static int channel = 1;
	//音频采样率
	static int audioSampleRate = 8000;
	//音频比特率
	static int audioBitRate = 12200;
	static int hwMode = 0;
	static int videoWidth = 0;
	static int videoHeight = 0;
	static int videoBitRate = 320; //bitrate单位kbit

	static int photoWidth = 2048;
	static int photoHeight = 1536;
	
	static int netTimeout = 0; //网络超时时间，0代表不判断， 单位是秒
	
	static SharedPreferences sp;
	
	//决定拍传流中包含音频或视频的组合
	static RecordMode recordMode = RecordMode.HwAudioAndVideo;
	
	static int [] fpsRange = new int[2];
	
	static String playUrl = "http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8";
	
	static void load(Activity a) {
		SharedPreferences settings = a.getSharedPreferences(PREFS_NAME, 0);
		sp = settings;
		netTimeout = sp.getInt("net_time_out", netTimeout);
		photoWidth = sp.getInt("photo_width", photoWidth);
		photoHeight = sp.getInt("photo_height", photoHeight);	

		videoBitRate = sp.getInt("bit_rate", videoBitRate);
		videoWidth = sp.getInt("video_width", videoWidth);
		videoHeight = sp.getInt("video_height", videoHeight);
		serviceCode = sp.getString("service_code", serviceCode);
		mvPort = sp.getString("mv_port", mvPort);
		mvHost = sp.getString("mv_host", mvHost);
		retryConnectTimes = sp.getInt("retry_connect_times", 3);
		timeOut = sp.getInt("time_out", timeOut);
		userName = sp.getString("user_name", userName);
		userPass = sp.getString("user_pass", userPass);
		playUrl = sp.getString("play_url", playUrl);
		int st_value = sp.getInt("stream_type", recordMode.value());
		RecordMode st = RecordMode.cast(st_value);
		if (st != null) {
			recordMode = st;
		} else {
			recordMode = RecordMode.HwAudioAndVideo;
		}
		fpsRange[0] = sp.getInt("min_fps", 0);
		fpsRange[1] = sp.getInt("max_fps", 0);
		isConnectToTcpPort = sp.getBoolean("is_connect_to_tcp", false);
		output_tag = sp.getString("output_tag", output_tag);
		isOpenNetWorkingAdaptive = sp.getBoolean("net_adaptive", isOpenNetWorkingAdaptive);
		int at_value = sp.getInt("audio_encoder_type", cast(audioEncoderType));
		if (at_value == 0) {
			audioEncoderType = AudioEncoderType.AMR_NB;
		} else {
			audioEncoderType = AudioEncoderType.AAC;
		}
		channel = sp.getInt("channel", channel);
		audioSampleRate = sp.getInt("audio_sample_rate", audioSampleRate);
		audioBitRate = sp.getInt("audio_bit_rate", audioBitRate);
		connectionMode = sp.getInt("connection_mode", connectionMode);
		privateCloudGetVSUrl = sp.getString("p_getvs_url", privateCloudGetVSUrl);
		isSavingVideoFile = sp.getBoolean("is_saving_video_file", isSavingVideoFile);
	}
	
	static void save() {
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt("net_time_out", netTimeout);
		editor.putInt("photo_width", photoWidth);
		editor.putInt("photo_height", photoHeight);	

		editor.putInt("bit_rate", videoBitRate);
		editor.putInt("video_width", videoWidth);
		editor.putInt("video_height", videoHeight);
		editor.putString("service_code", serviceCode);
		editor.putString("mv_port", mvPort);
		editor.putString("mv_host", mvHost);
		editor.putInt("retry_connect_times", retryConnectTimes);
		editor.putInt("time_out", timeOut);
		editor.putString("user_name", userName);
		editor.putString("user_pass", userPass);
		editor.putString("play_url", playUrl);
		editor.putInt("stream_type", recordMode.value());
		editor.putInt("min_fps", fpsRange[0]);
		editor.putInt("max_fps", fpsRange[1]);
		editor.putBoolean("is_connect_to_tcp", isConnectToTcpPort);
		editor.putString("output_tag", output_tag);
		editor.putBoolean("net_adaptive", isOpenNetWorkingAdaptive);
		editor.putInt("audio_encoder_type", cast(audioEncoderType));
		editor.putInt("channel", channel);
		editor.putInt("audio_sample_rate", audioSampleRate);
		editor.putInt("audio_bit_rate", audioBitRate);
		editor.putInt("connection_mode", connectionMode);
		editor.putString("p_getvs_url", privateCloudGetVSUrl);
		editor.putBoolean("is_saving_video_file", isSavingVideoFile);
		editor.commit();
	}
	
	private static int cast(AudioEncoderType type) {
		int typeInt = -1;
		if(type == AudioEncoderType.AMR_NB) {
			typeInt = 0;
		} else {
			typeInt = 1;
		}
		return typeInt;
	}
}
