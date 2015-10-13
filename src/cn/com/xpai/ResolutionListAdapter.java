package cn.com.xpai;

import java.util.List;

import cn.com.xpai.R;
import cn.com.xpai.R.id;
import cn.com.xpai.R.layout;
import cn.com.xpai.core.Manager;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class ResolutionListAdapter extends BaseAdapter {

	private Context context;
	private List<Manager.Resolution> list;
	SettingItemAdapter settingAdapter;
	
	public ResolutionListAdapter(Activity activity, SettingItemAdapter setting) {
		context = activity.getBaseContext();
		settingAdapter = setting;
	}
	
	@Override
	public int getCount() {
		list = Manager.getSupportedVideoResolutions();
		if (list == null) {
			return 0;
		}
		return list.size();
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
		Manager.Resolution resolution = list.get(position);
		ItemViewCache viewCache = null;
		if (convert_view == null) {
			convert_view = LayoutInflater.from(context).inflate(R.layout.radio_btn_item, null, true);
			viewCache = new ItemViewCache();
			viewCache.txtName = (TextView) convert_view.findViewById(R.id.txt_name);
			viewCache.radioBtn = (RadioButton) convert_view.findViewById(R.id.radio_btn);
			convert_view.setTag(viewCache);

		} else {
			viewCache = (ItemViewCache) convert_view.getTag();
		}

		viewCache.radioBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Manager.Resolution res = (Manager.Resolution) v.getTag();
				Config.videoWidth = res.width;
				Config.videoHeight = res.height;
				Config.videoBitRate = res.width;//分辨率 设置的同时 设置对应的码流
				Config.save();
				notifyDataSetChanged();
				settingAdapter.notifyDataSetChanged();
				Manager.setVideoResolution(Config.videoWidth,
						Config.videoHeight);
			}
		});
		viewCache.radioBtn.setTag(resolution);
		ItemViewCache cache = (ItemViewCache) convert_view.getTag();
		String res_str = String.format("%dx%d", resolution.width,
				resolution.height);
		cache.txtName.setText(res_str);
		if (resolution.width == Config.videoWidth
				&& resolution.height == Config.videoHeight) {
			cache.radioBtn.setChecked(true);
		} else {
			cache.radioBtn.setChecked(false);
		}
		return convert_view;
	}
	
	private static class ItemViewCache {
		TextView txtName;
		RadioButton radioBtn;
	}

}
