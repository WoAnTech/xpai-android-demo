package cn.com.xpai.demo.player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;





import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class Globals {

	public static final String TABLE_PLAYLIST = "playlist";
	public static final String LOGTAG = "41CPlayer";
	public static boolean DEBUG = true;
	public static SQLiteDatabase dbConn;
	public static HashSet<String> fileFilter =  null;


	public static SQLiteDatabase getDbConn(Context context) {
		if (dbConn == null) {
			// dbConn = SQLiteDatabase.openOrCreateDatabase(getWeikdBbPath(),
			// null);
			SqliteOpenHelper openHelper = new SqliteOpenHelper(context);
			dbConn = openHelper.getWritableDatabase();
		}
		return dbConn;
	}

	public static void closeDbConn() {
		if (dbConn != null) {
			dbConn.close();
			dbConn = null;
		}
	}

	public static void ShowLog(String s) {
		if (Globals.DEBUG)
			Log.i(Globals.LOGTAG, s);
	}

	public static String getHardwareInfo() {
		try {
			FileReader reader = new FileReader("/proc/cpuinfo");
			BufferedReader br = new BufferedReader(reader);
			String line = "";
			String platform = "";
			String ver = "";
			String serial = "";

			while ((line = br.readLine()) != null) {
				line = line.replace("\n", "");
				if (line.startsWith("Hardware"))
					platform = line.split(":")[1].replace(" ", "");
				else if (line.startsWith("Revision"))
					ver = line.split(":")[1].replace(" ", "");
				else if (line.startsWith("Serial"))
					serial = line.split(":")[1].replace(" ", "");
			}
			br.close();
			reader.close();
			return String.format("%s-%s-%s", platform, ver, serial);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String getSdcardPath() {
		String sdcardPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		System.out.println("sdcardPath:" + sdcardPath);
		return sdcardPath;
	}

	public static ArrayList<VideoFile> getFilelist(Context context) {
		// TODO Auto-generated method stub
		ArrayList<VideoFile> lst = new ArrayList<VideoFile>();

		SQLiteDatabase conn = Globals.getDbConn(context);
		Cursor result = conn.rawQuery("SELECT id, name, path, pic,size  FROM "
				+ Globals.TABLE_PLAYLIST, null);
		result.moveToFirst();
		while (!result.isAfterLast()) {
			int id = result.getInt(0);
			String name = result.getString(1);
			String path = result.getString(2);
			String pic = result.getString(3);
			int size = result.getInt(4);
			result.moveToNext();

			// Video(int id, int chId, String vid, String site, String title,
			// Context context)
			// VideoInfo v = new VideoInfo(id, title, pic, downloadSize,
			// totalSize, st);
			VideoFile v = new VideoFile(name, path, pic, size);
			lst.add(v);
		}
		result.close();
		return lst;
	}

	public static HashSet<String> getFileFilter() {
		if (fileFilter == null) {
			fileFilter = new HashSet<String>();
			fileFilter.add("mkv");
			fileFilter.add("flv");
			fileFilter.add("wmv");
			fileFilter.add("ts");
			fileFilter.add("rm");
			fileFilter.add("rmvb");
			fileFilter.add("webm");
			fileFilter.add("mov");
			fileFilter.add("vstream");
			fileFilter.add("mpeg");
			fileFilter.add("f4v");
			fileFilter.add("avi");
			fileFilter.add("mkv");
			fileFilter.add("ogv");
			fileFilter.add("dv");
			fileFilter.add("divx");
			fileFilter.add("vob");
			fileFilter.add("asf");
			fileFilter.add("3gp");
			fileFilter.add("h264");
			fileFilter.add("h261");
			fileFilter.add("h263");
			fileFilter.add("mp4");
		}
		return fileFilter;
	}
	
    public static String getExtensionName(String filename) {   
        if ((filename != null) && (filename.length() > 0)) {   
            int dot = filename.lastIndexOf('.');   
            if ((dot >-1) && (dot < (filename.length() - 1))) {   
                return filename.substring(dot + 1);   
            }   
        }   
        return filename;   
    }  
    
    public static String getFileName(String filename) {   
        if ((filename != null) && (filename.length() > 0)) {   
            int dot = filename.lastIndexOf('/');   
            if ((dot >-1) && (dot < (filename.length() - 1))) {   
                return filename.substring(dot + 1);   
            }   
        }   
        return filename;   
    }  
    
    public static void execSql(String sql, Context context){
    	SQLiteDatabase conn = getDbConn(context);
   	 	conn.execSQL(sql);
    }
    
    public static int execSqlRetInt(String sql, Context context){
    	int ret = -1;
    	SQLiteDatabase conn = Globals.getDbConn(context);
   		Cursor result=conn.rawQuery(sql, null); 
    	result.moveToFirst(); 
    	while (!result.isAfterLast()) { 
    		ret =result.getInt(0); 
    		break;
    	   
    	} 
    	result.close(); 
    	return ret;
    }
    
    public static String getSettingKeyString(String key, Context context){
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);  
    	return sharedPref.getString(key, "");
    }
    
    public static void setSettingKeyString(String key, String value, Context context){
    	SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(context);  
    	SharedPreferences.Editor editor = setting.edit();
    	editor.putString(key, value);
   	 	editor.commit();
    }

}
