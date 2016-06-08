package jp.codepanic.netlis;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class Histories {

	static final int 	DB_VERSION = 1;
	static final String DB = "history.db";
	
	final int MAX_NUM = 100;
	
	private static class DBHelper extends SQLiteOpenHelper {
	    public DBHelper(Context c) {
	        super(c, DB, null, DB_VERSION);
	    }
	    
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(
        		"create table history ( _id integer primary key autoincrement, data text not null );"
	        	);
	    }
	    
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        db.execSQL(
	        	"drop table history"
	        	);
	        onCreate(db);
	    }
	}	



	public ArrayList<String>	_arrayHis = new ArrayList<String>();
	
	static SQLiteDatabase _db;
	
	public Histories(Context c){
		DBHelper helper = new DBHelper(c);
		_db = helper.getWritableDatabase();
		
		load();
	}
	
	public void close(){
		save();
		_db.close();
	}
	
	public void add(String data){
		// 指定行から１件だけ削除する
//		_db.execSQL("delete from history ORDER BY _id DESC limit -1 offset " + String.valueOf(MAX_NUM) + ";");
//		_db.execSQL("delete from history where _id < (select _id from history ORDER BY _id DESC LIMIT -1 OFFSET " + String.valueOf(MAX_NUM) + ");");

		if(_arrayHis.size() >= MAX_NUM)
			_arrayHis.remove(MAX_NUM - 1);
		
//		_arrayHis.add(data);
		_arrayHis.add(0, data);
//		addDB(data);
	}
	
	public void delete(int pos){
		_arrayHis.remove(pos);
//		deleteDB(pos);
	}
	
	private void load(){
		_arrayHis.clear();
		
		Cursor cur = _db.rawQuery("select * from history", null);
		
		try{
			int data	= cur.getColumnIndex("data");
			
			if(cur.getCount() > 0 && cur.moveToFirst()){
				do{
					_arrayHis.add( cur.getString(data) );
				}while(cur.moveToNext());
			}
		}catch(SQLiteException e){
		}finally{
			cur.close();
		}
	}
	
	private void save(){
		_db.execSQL("delete from history");
		
		for(String data : _arrayHis){
			addDB(data);
		}
	}

	private void addDB(String data){
		ContentValues values = new ContentValues();
		
		values.put("data", data);
		
		_db.insert("history", null, values);
	}
	
//	private void deleteDB(int pos){
//		// 指定行から１件だけ削除する
////		_db.execSQL("delete from history ORDER BY _id DESC limit -1 offset " + String.valueOf(pos) + ";");
//		_db.execSQL("delete from history where _id < (select _id from history ORDER BY _id DESC LIMIT -1 OFFSET " + String.valueOf(pos) + ");");
//	}
}
