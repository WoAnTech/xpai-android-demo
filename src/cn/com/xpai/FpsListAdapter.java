package cn.com.xpai;

import java.util.List;

import cn.com.xpai.R;
import cn.com.xpai.core.Manager;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

public class FpsListAdapter extends BaseAdapter {

	private Context context;
	private List<int []> list;
	SettingItemAdapter settingAdapter;
	
	public FpsListAdapter(Activity activity, SettingItemAdapter setting) {
		context = activity.getBaseContext();
		settingAdapter = setting;
	}
	
	@Override
	public int getCount() {
		list = Manager.getSupportedFps();
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
		int [] fps_range = list.get(position);
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
				int [] fps_range = (int []) v.getTag();
				Config.fpsRange = fps_range;
				Config.save();
				notifyDataSetChanged();
				settingAdapter.notifyDataSetChanged();
				Manager.setVideoFpsRange(fps_range[0], fps_range[1]);
			}
		});
		viewCache.radioBtn.setTag(fps_range);
		ItemViewCache cache = (ItemViewCache) convert_view.getTag();
		String res_str = String.format("%d ~ %d", fps_range[0],
				fps_range[1]);
		cache.txtName.setText(res_str);
		if (Config.fpsRange[0] == fps_range[0]
				&& Config.fpsRange[1] == fps_range[1]) {
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
