package cn.com.xpai;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class FileChooser extends ListActivity {
    
	private static final String TAG = "FileChooser";
    private File currentDir;
    private FileArrayAdapter adapter;
    private String flag = null;
    private ProgressDialog progressDialog;
    private String transcodeFilePath;
    private TranscoderImpl mTranscoderImpl;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == TranscoderImpl.UPDATE_TRANSCODE_PROGRESS) {
				progressDialog.updateProgress(msg.arg1);
			} else if(msg.what == TranscoderImpl.TRANSCODE_FINISHED) {
				progressDialog.dismiss();
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = new File("/sdcard/xpai");
        fill(currentDir);
        flag = getIntent().getStringExtra("Flag");
        progressDialog = new ProgressDialog(this, new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
	    			finish();
			}
		});
    }
    
    private void fill(File f)
    {
        File[]dirs = f.listFiles();
         this.setTitle("Current Dir: "+f.getName());
         List<Option>dir = new ArrayList<Option>();
         List<Option>fls = new ArrayList<Option>();
         try{
             for(File ff: dirs)
             {
                if(ff.isDirectory())
                    dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
                else
                {
                    fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
                }
             }
         }catch(Exception e)
         {
             
         }
         Collections.sort(dir);
         Collections.sort(fls);
         dir.addAll(fls);
         if(!f.getName().equalsIgnoreCase("sdcard"))
             dir.add(0,new Option("..","Parent Directory",f.getParent()));
         adapter = new FileArrayAdapter(FileChooser.this,R.layout.file_view,dir);
         this.setListAdapter(adapter);
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Option o = adapter.getItem(position);
        if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
                currentDir = new File(o.getPath());
                fill(currentDir);
        }
        else
        {
            onFileClick(o);
        }
    }
    
    private void onFileClick(Option o)
    {
    		if(flag == null) {
    			Toast.makeText(this, "File Clicked: "+o.getName(), Toast.LENGTH_SHORT).show();
    			Intent i = this.getIntent();
    			Bundle b = new Bundle();
    			b.putString("file_name", o.getPath());
    			i.putExtras(b);
    			setResult(RESULT_OK, i);
    			finish();
    		} else {
    			Log.i(TAG, "path--->" + o.getPath() + " name--->" + o.getName());
    			transcodeDialog(o).show();
    		}
    }
    
    private Dialog transcodeDialog(final Option o) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View loginView = factory.inflate(R.layout.transcode_dialog, null);
		Dialog loginDialog = new AlertDialog.Builder(this).setTitle(R.string.transcode_dialog_title).setView(
				loginView).setPositiveButton(R.string.login_dialog_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String startTime = ((EditText) loginView
								.findViewById(R.id.mv_starttime_edit)).getText()
								.toString();
						String duration = ((EditText) loginView
								.findViewById(R.id.mv_duration_edit)).getText()
								.toString();
						mTranscoderImpl = new TranscoderImpl(mHandler);
						if(null == startTime || "".equals(startTime)) {
							startTime = "0";
						} else {
							try {
								int stInt = Integer.parseInt(startTime);
								if(stInt < 0) {
									Toast.makeText(FileChooser.this, "开始时间不能为负数!", Toast.LENGTH_LONG).show();
									return;
								}
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(FileChooser.this, "开始时间不合法!", Toast.LENGTH_LONG).show();
								return;
							}
						}
						
						if(null == duration || "".equals(duration)) {
							duration = null;
						} else {
							try {
								int durInt = Integer.parseInt(duration);
								if(durInt <= 0) {
									Toast.makeText(FileChooser.this, "视频长度请输入大于0的整数!", Toast.LENGTH_LONG).show();
									return;
								}
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Toast.makeText(FileChooser.this, "视频长度不合法!", Toast.LENGTH_LONG).show();
								return;
							}
							
						}
						progressDialog.show();
						transcodeFilePath = "/sdcard/xpai/T_" + o.getName();
						mTranscoderImpl.transVideoFile(o.getPath(), startTime,
								duration, transcodeFilePath);
					}
				}).setNegativeButton(R.string.login_dialog_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						//finish();
						dialog.dismiss();
					}
				}).create();
		return loginDialog;
	}
}
