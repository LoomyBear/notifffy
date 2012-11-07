package lunchcode.notifffy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase; import android.database.sqlite.SQLiteOpenHelper; import android.util.Log;

public class DBAdapter {
	
	static final String KEY_ROWID = "_id";
	static final String KEY_LIKES = "current_likes";
	static final String KEY_COMMS = "current_comms";
	static final String KEY_FOLLOWERS = "current_followers";
	static final String KEY_USERPIC = "userpic_url";
	static final String TAG = "DBAdapter";
	static final String DATABASE_NAME = "notifffyDB";
	static final String DATABASE_TABLE = "notifffy";
	static final int DATABASE_VERSION = 1;
	static final String DATABASE_CREATE =
			"create table notifffy" +
			"(_id integer primary key autoincrement, " + 
			"current_likes integer not null," +
			"current_comms integer not null," +
			"current_followers integer not null," +
			"userpic_url text not null);";
	final Context context;
	DatabaseHelper DBHelper;
	SQLiteDatabase db;
	
	public DBAdapter(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context); }
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION); }
		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(DATABASE_CREATE); } catch (SQLException e) {
					e.printStackTrace(); }
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS notifffy");
			onCreate(db); }
	}
	
	//---opens the database---
	public DBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this; }
	
	//---closes the database---
	public void close() {
		DBHelper.close(); }
	
	//---inserts---
	public long insertActivity(int likes, int comms, int followers, String userpic) {
		ContentValues initialValues = new ContentValues(); 
		initialValues.put(KEY_LIKES, likes); 
		initialValues.put(KEY_COMMS, comms);
		initialValues.put(KEY_FOLLOWERS, followers);
		initialValues.put(KEY_USERPIC, userpic);
		return db.insert(DATABASE_TABLE, null, initialValues);
	}
	//---deletes---
	public boolean deleteActivity(long rowId) {
		return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0; }
	//---retrieves all the contacts---
	public Cursor getAllActivities() {
		return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_LIKES, KEY_COMMS, KEY_FOLLOWERS, KEY_USERPIC}, null, null, null, null, null);
	}
	//---retrieves---
	public Cursor getActivity(long rowId) throws SQLException {
		Cursor mCursor =
				db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_LIKES, KEY_COMMS, KEY_FOLLOWERS, KEY_USERPIC}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) { mCursor.moveToFirst();
		}
		return mCursor; }
	//---updates---
	public boolean updateActivity(long rowId, int likes, int comms, int followers, String userpic) {
		ContentValues args = new ContentValues();
		args.put(KEY_LIKES, likes);
		args.put(KEY_COMMS, comms);
		args.put(KEY_FOLLOWERS, followers);
		args.put(KEY_USERPIC, userpic);
		return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
