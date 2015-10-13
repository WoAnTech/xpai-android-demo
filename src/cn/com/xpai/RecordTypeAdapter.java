package cn.com.xpai;

import java.lang.reflect.Array;
import java.util.List;
import cn.com.xpai.core.Manager;
import cn.com.xpai.core.RecordMode;
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

public class RecordTypeAdapter extends BaseAdapter {

	private SettingItemAdapter settingAdapter;
	private Context context;
	
	private List<RecordMode> list;
	
	public RecordTypeAdapter(Activity activity, SettingItemAdapter setting) {
		settingAdapter = setting;
		context = activity.getBaseContext();
	}
	
	@Override
	public int getCount() {
		list = Manager.getSupportedRecordModes();
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
		RecordMode record_mode = list.get(position);
		ItemViewCache viewCache = null;
        if(convert_view == null){  
            convert_view = LayoutInflater.from(context).inflate(R.layout.radio_btn_item, null, true);  
            viewCache = new ItemViewCache();  
            viewCache.txtName = (TextView)convert_view.findViewById(R.id.txt_name);  
            viewCache.radioBtn =(RadioButton)convert_view.findViewById(R.id.radio_btn);
            convert_view.setTag(viewCache);
        } else {
        	viewCache = (ItemViewCache) convert_view.getTag();
        }
        viewCache.radioBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Config.recordMode = (RecordMode)v.getTag();
                Config.save();
                notifyDataSetChanged();
                settingAdapter.notifyDataSetChanged();
            }
        });
        viewCache.radioBtn.setTag(record_mode);
        ItemViewCache cache = (ItemViewCache)convert_view.getTag();
        cache.txtName.setText(record_mode.toString());
        if (Config.recordMode == record_mode) {
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
