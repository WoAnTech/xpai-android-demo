package cn.com.xpai;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import cn.com.xpai.core.Manager;

public class SaveVideoFileAdapter extends BaseAdapter {

	private SettingItemAdapter settingAdapter;
	private Context context;
	
	private List<Boolean> list;
	
	public SaveVideoFileAdapter(Activity activity, SettingItemAdapter setting) {
		settingAdapter = setting;
		context = activity.getBaseContext();
		list = new ArrayList<Boolean>();
		list.add(true);
		list.add(false);
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convert_view, ViewGroup parent) {
		Boolean isOn = (Boolean) getItem(position);
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
            		Config.isSavingVideoFile = (Boolean)v.getTag();
                Config.save();
                notifyDataSetChanged();
                settingAdapter.notifyDataSetChanged();
            }
        });
        viewCache.radioBtn.setTag(isOn);
        ItemViewCache cache = (ItemViewCache)convert_view.getTag();
        if(isOn) {
        		cache.txtName.setText("是");
        } else {
        		cache.txtName.setText("否");
        }
        if (Config.isSavingVideoFile == isOn) {
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
