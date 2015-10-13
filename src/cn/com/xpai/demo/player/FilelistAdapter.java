package cn.com.xpai.demo.player;

import java.util.ArrayList;


import cn.com.xpai.R;
import cn.com.xpai.R.id;
import cn.com.xpai.R.layout;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FilelistAdapter extends BaseAdapter {

	private Context mContext;
	ArrayList<VideoFile> mDataFilelist;
	private TextView mTitle;
	private TextView mSize;

	public FilelistAdapter(ArrayList<VideoFile> filelist, Context context) {
		// TODO Auto-generated constructor stub
		mDataFilelist = filelist;
		mContext = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataFilelist.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
        return mDataFilelist.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
        return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		if(null == convertView) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.play_item,
				null);
			holder = new ViewHolder();
			holder.mTitle = (TextView) convertView.findViewById(R.id.video_title);
			holder.mSize = (TextView) convertView.findViewById(R.id.video_size);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		VideoFile v = mDataFilelist.get(position);
		holder.mTitle.setText(v.getName());
		holder.mSize.setText(v.getSize()/1024/1024 + "MB");
		return convertView;
	}
	
	class ViewHolder {
		TextView mTitle;
		TextView mSize;
	}

}
