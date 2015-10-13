package cn.com.xpai.demo.player;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteOpenHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "41c_player.db";
	private static final int DATABASE_VERSION = 1;
	private Context mContext;

	public SqliteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		onCreateTables(db);
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO 更改数据库版本的操作
	}

	private void onCreateTables(SQLiteDatabase db) {
		     
		db.execSQL("CREATE TABLE IF NOT EXISTS  " 
				+ Globals.TABLE_PLAYLIST 
				+ "( `id` int  primary key, " 
				+ "`path`  varchar (255) , " 
				+ "`name` varchar(255) , " 
				+ "`pic`  varchar(255)," 
				+ "`size`  int )");
		
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		// TODO 每次成功打开数据库后首先被执行
	}
}
