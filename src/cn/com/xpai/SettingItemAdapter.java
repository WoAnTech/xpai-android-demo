package cn.com.xpai;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import cn.com.xpai.core.Manager;
import cn.com.xpai.core.Manager.AudioEncoderType;

public class SettingItemAdapter extends BaseAdapter implements OnItemClickListener {

	private Context context;
	private PopListView resolutionMenu;
	private PopListView recordTypeMenu;
	private PopSeekbar popNetworkTimeout;
	private PopSeekbar popBitrate;
	private PopListView fpsMenu;
	private PopEditTextView popEditText;
	private PopListView netWorkingAdaptive;
	private PopAudioEncoderTypeView audioEncoderTypeLV;
	private Activity activity;
	final private int MENU_RESOLUTION_IDX = 0;
	final private int MENU_BITRATE_IDX = 1;
	final private int MENU_NET_TIMEOUT_IDX = 2;
	final private int MENU_RECORD_TYPE_IDX = 3;
	final private int MENU_FPS_IDX = 4;
	final private int MENU_OUTPUT_TAG_IDX = 5;
	final private int MENU_NETWORKING_ADAPTIVE_IDX = 6;
	final private int MENU_AUDIO_ENCODER_TYPE_IDX = 7;
	
	SettingItemAdapter(Activity activity) {
		context = activity.getBaseContext();
		this.activity = activity;
		resolutionMenu = new PopListView(activity, new ResolutionListAdapter(activity, this), "设置分辨率");
		recordTypeMenu = new PopListView(activity, new RecordTypeAdapter(activity, this), "设置录制类型");
		fpsMenu = new PopListView(activity, new FpsListAdapter(activity, this), "设置FPS");
		popNetworkTimeout = new PopSeekbar(activity, 10, 100, Config.netTimeout,
				new PopSeekbar.onChangeListener() {
					
					@Override
					void onChanged(int value) {
						Config.netTimeout = value;
						Config.save();
						notifyDataSetChanged();
					}
				}, "设置网络超时时间");
		
		popBitrate = new PopSeekbar(activity, 200, 10240, Config.videoBitRate,
				new PopSeekbar.onChangeListener() {
					
					@Override
					void onChanged(int value) {
						Config.videoBitRate = value;
						Config.save();
						notifyDataSetChanged();
					}
				}, "设置视频码流");
		popEditText = new PopEditTextView(activity, "设置输出格式标签",
				new PopEditTextView.onFinishListener() {
					@Override
					void onFinished(String output_tag) {
						Config.output_tag = output_tag;
						Config.save();
						notifyDataSetChanged();
					}
				});
		netWorkingAdaptive = new PopListView(activity,
				new NetWorkingAdaptiveAdapter(activity, this), "设置网络自适应");
		audioEncoderTypeLV = new PopAudioEncoderTypeView(activity, this, "设置音频编码参数");
		audioEncoderTypeLV.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				Config.save();
				notifyDataSetChanged();
				Manager.setAudioRecorderParams(Config.audioEncoderType, Config.channel,
						Config.audioSampleRate, Config.audioBitRate);
			}
		});
	}
	
	@Override
	public int getCount() {
		return settingName.length;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	
	@Override
	public View getView(int position, View convert_view, ViewGroup parent) {
		ItemViewCache viewCache = null;
        if(convert_view == null) {  
            convert_view = LayoutInflater.from(context).inflate(R.layout.txt_item, null, true);  
            viewCache = new ItemViewCache();  
            viewCache.txtName = (TextView)convert_view.findViewById(R.id.txt_name);  
            viewCache.txtValue =(TextView)convert_view.findViewById(R.id.txt_value);
            viewCache.position = position;
            convert_view.setTag(viewCache);
        } else {
        		viewCache=(ItemViewCache)convert_view.getTag();  
        }
        viewCache.txtName.setText(settingName[position]);
        switch (position) {
        case MENU_RESOLUTION_IDX:
        	viewCache.txtValue.setText(String.format("%dx%d", Config.videoWidth, Config.videoHeight));
        	break;
        case MENU_BITRATE_IDX:
        	viewCache.txtValue.setText(Config.videoBitRate + "Kbit");
        	break;
        case MENU_NET_TIMEOUT_IDX:
        	viewCache.txtValue.setText(Config.netTimeout + "秒");
        	break;
        case MENU_RECORD_TYPE_IDX:
        	viewCache.txtValue.setText(Config.recordMode.toString());
        	break;
        case MENU_FPS_IDX:
        	viewCache.txtValue.setText(String.format("[%d, %d]",  Config.fpsRange[0], Config.fpsRange[1]));
        	break;
        case MENU_OUTPUT_TAG_IDX:
        	viewCache.txtValue.setText(Config.output_tag);
        	break;
        case MENU_NETWORKING_ADAPTIVE_IDX:
        	viewCache.txtValue.setText(Config.isOpenNetWorkingAdaptive? "开启":"关闭");
        	break;
        case MENU_AUDIO_ENCODER_TYPE_IDX:
        	viewCache.txtValue.setText(Config.audioEncoderType == AudioEncoderType.AMR_NB?
        			"AMR_NB":"AAC");
        	break;
        }
        return convert_view; 
	}
	
	private static class ItemViewCache {
		int position;
		TextView txtName;
		TextView txtValue;
	}

	private String [] settingName = new String []{"分辨率", "码流", "网络超时","录制类型",
			"FPS", "输出格式标签", "网络自适应", "音频编码参数"};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub

		ItemViewCache tag = (ItemViewCache)view.getTag();
		int [] location = new int[8];
		view.getLocationOnScreen(location);
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		int pos_x =  location[0] + view.getMeasuredWidth();
		Log.i("location", "x:" + location[0] + " y:" + location[1]);
		switch(position) {
		case MENU_RESOLUTION_IDX:
			resolutionMenu.showAtLocation(activity.findViewById(R.id.btn_setting),
					 Gravity.LEFT, pos_x, 10);
			break;
		case MENU_RECORD_TYPE_IDX:
			recordTypeMenu.showAtLocation(activity.findViewById(R.id.btn_setting),
					 Gravity.LEFT, pos_x, 10);
			break;
		case MENU_NET_TIMEOUT_IDX:
			popNetworkTimeout.showAtLocation(activity.findViewById(R.id.btn_setting),
					 Gravity.LEFT, pos_x, 10);
			break;
		case MENU_BITRATE_IDX:
			popBitrate.showAtLocation(activity.findViewById(R.id.btn_setting),
					 Gravity.LEFT, pos_x, 10);
			break;
		case MENU_FPS_IDX:
			fpsMenu.showAtLocation(activity.findViewById(R.id.btn_setting),
					 Gravity.LEFT, pos_x, 10);
			break;
		case MENU_OUTPUT_TAG_IDX:
			popEditText.showAtLocation(activity.findViewById(R.id.btn_setting),
					 Gravity.LEFT, pos_x, 10);
			break;
		case MENU_NETWORKING_ADAPTIVE_IDX:
			netWorkingAdaptive.showAtLocation(activity.findViewById(R.id.btn_setting),
					 Gravity.LEFT, pos_x, 10);
			break;
		case MENU_AUDIO_ENCODER_TYPE_IDX:
			audioEncoderTypeLV.showAtLocation(activity.findViewById(R.id.btn_setting),
					 Gravity.LEFT, pos_x, 10);
			break;
		}
	
	}
}
