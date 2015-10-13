package cn.com.xpai;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import cn.com.xpai.core.Manager.AudioEncoderType;

public class PopAudioEncoderTypeView extends PopupWindow {
	View view;
	Activity activity;
	private SettingItemAdapter settingItemAdapter;
	private int minBitRate;
	private RadioGroup radioGroup;
	private RadioButton amrNbRadioBtn;
	private RadioButton aacRadioBtn;
	private Spinner sampleRateSp;
	private TextView currentBitRateTV;
	private TextView minBitRateTV;
	private TextView maxBitRateTV;
	private TextView sampleRateShowTV;
	private TextView channelShowTV;
	private Spinner channelSp;
	private SeekBar seekBar;
	private LinearLayout sampleRateLL;
	private LinearLayout bitRateLL;
	private LinearLayout channelLL;
	private static final Integer[] sampleRates = {8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000,
		64000, 88200, 96000};
	private static final Integer[] channels = {1, 2};
	private boolean isFirstEnter = true;
	
	PopAudioEncoderTypeView(Activity activity, SettingItemAdapter settingItemAdapter, String title) {
		super(activity.getBaseContext());
		view =  (View)activity.getLayoutInflater().inflate(R.layout.audio_encoder_type, (ViewGroup)activity.findViewById(R.id.main_layout), false);
		view.setOnTouchListener(touchListener);
		setContentView(view);
		this.settingItemAdapter = settingItemAdapter;
		setFocusable(true);
		setAnimationStyle(R.style.AnimationFade);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		((TextView)view.findViewById(R.id.txt_title)).setText(title);
		initView();
		isFirstEnter = true;
		
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case  R.id.amr_nb_radio_btn:
					Config.audioEncoderType = AudioEncoderType.AMR_NB;
					Config.audioSampleRate = 8000;
					Config.audioBitRate = 12200;
					Config.channel = 1;
					bitRateLL.setVisibility(View.GONE);
					sampleRateSp.setVisibility(View.GONE);
					sampleRateShowTV.setVisibility(View.VISIBLE);
					sampleRateShowTV.setText(Config.audioSampleRate + "");
					currentBitRateTV.setText(Config.audioBitRate + "");
					channelSp.setVisibility(View.GONE);
					channelShowTV.setVisibility(View.VISIBLE);
					channelShowTV.setText(Config.channel + "");
					PopAudioEncoderTypeView.this.settingItemAdapter.notifyDataSetChanged();
				break;
				case R.id.aac_radio_btn:
					Config.audioEncoderType = AudioEncoderType.AAC;
					bitRateLL.setVisibility(View.VISIBLE);
					sampleRateSp.setVisibility(View.VISIBLE);
					sampleRateShowTV.setVisibility(View.GONE);
					currentBitRateTV.setText(Config.audioBitRate + "");
					channelSp.setVisibility(View.VISIBLE);
					channelShowTV.setVisibility(View.GONE);
					PopAudioEncoderTypeView.this.settingItemAdapter.notifyDataSetChanged();
				break;
				default:
				break;
				}
			}
		});
		
		if (Config.audioEncoderType == AudioEncoderType.AMR_NB) {
			radioGroup.check(R.id.amr_nb_radio_btn);
		} else {
			radioGroup.check(R.id.aac_radio_btn);
		}
		
		ArrayAdapter<Integer> sampleRateAdapter = new ArrayAdapter<Integer>(activity,
				android.R.layout.simple_spinner_item, sampleRates);
		sampleRateSp.setAdapter(sampleRateAdapter);
		sampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
		for (int position=0;position<sampleRates.length;position++) {
			if (Config.audioSampleRate == sampleRates[position]) {
				sampleRateSp.setSelection(position);
				break;
			}
		}
		
		sampleRateSp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Config.audioSampleRate = sampleRates[position];
				if ( !isFirstEnter) {
					Config.audioBitRate = (int)(Config.audioSampleRate * 1.2);
				} 
				isFirstEnter = false;
				seekBar.setMax(Config.audioSampleRate * 2);
				seekBar.setProgress((int)(Config.audioSampleRate * 1.2));
				minBitRateTV.setText((int)(Config.audioSampleRate / 2) + "");
				minBitRate = (int)(Config.audioSampleRate / 2);
				maxBitRateTV.setText(Config.audioSampleRate * 2 + "");
				currentBitRateTV.setText(Config.audioBitRate + "");
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		ArrayAdapter<Integer> channelAdapter = new ArrayAdapter<Integer>(activity,
				android.R.layout.simple_spinner_item, channels);
		channelSp.setAdapter(channelAdapter);
		channelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);  
		for (int position=0;position<channels.length;position++) {
			if (Config.channel == channels[position]) {
				channelSp.setSelection(position);
				break;
			}
		}
		
		channelSp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Config.channel = channels[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					int bitRate = (progress <= minBitRate ? minBitRate : progress);
					currentBitRateTV.setText(bitRate + "");
					Config.audioBitRate = bitRate;
				}
			}
		});
		
	}
	
	private void initView() {
		radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
		amrNbRadioBtn = (RadioButton) view.findViewById(R.id.amr_nb_radio_btn);
		aacRadioBtn = (RadioButton) view.findViewById(R.id.aac_radio_btn);
		sampleRateSp = (Spinner) view.findViewById(R.id.sample_rate_spinner);
		currentBitRateTV = (TextView) view.findViewById(R.id.bit_rate_txt);
		minBitRateTV = (TextView) view.findViewById(R.id.min_bit_rate_txt);
		maxBitRateTV = (TextView) view.findViewById(R.id.max_bit_rate_txt);
		sampleRateShowTV = (TextView) view.findViewById(R.id.sample_rate_txt_show);
		channelShowTV = (TextView) view.findViewById(R.id.channel_txt_show);
		seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
		channelSp = (Spinner) view.findViewById(R.id.channel_spinner);
		sampleRateLL = (LinearLayout) view.findViewById(R.id.sample_rate_ll);
		bitRateLL = (LinearLayout) view.findViewById(R.id.bit_rate_ll);
		channelLL = (LinearLayout) view.findViewById(R.id.channel_ll);
	}
	
	OnTouchListener touchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			 return true;
		}
	};
}
