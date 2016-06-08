package jp.codepanic.netlis;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class BlockList {

	static final int 	DB_VERSION = 2;
	static final String DB = "blocklist.db";
	
	private static class DBHelper extends SQLiteOpenHelper {
	    public DBHelper(Context c) {
	        super(c, DB, null, DB_VERSION);
	    }
	    
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(
        		"create table blocklist ( _id integer primary key autoincrement, mount text not null, nam text, desc text, gnl text, dj text );"
	        	);
	    }
	    
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        db.execSQL(
	        	"drop table blocklist"
	        	);
	        onCreate(db);
	    }
	}	



	public ArrayList<Block>	_arrayBlock = new ArrayList<Block>();
	
	static SQLiteDatabase _db;
	
	public BlockList(Context c){
		DBHelper helper = new DBHelper(c);
		_db = helper.getWritableDatabase();
		
		load();
	}
	
	public void close(){
		_db.close();
	}
	
	public void add(Channel ch){
		
		Block block = new Block(ch._MNT, ch._NAM, ch._DESC, ch._GNL, ch._DJ);
		
		_arrayBlock.add(block);
		addDB(block);
	}
	
	public void delete(String mount){
		
		for(int index = 0; index < _arrayBlock.size(); index++){
			if(_arrayBlock.get(index)._mount.equalsIgnoreCase(mount)){
				_arrayBlock.remove(index);
				deleteDB(mount);
				break;
			}
		}
		
	}
	
	public boolean isExist(Channel ch){
		
		for(Block block : _arrayBlock){
			if(block._mount.equalsIgnoreCase(ch._MNT))
				return true;
		}
		
		return false;
	}
	
	private void load(){
		_arrayBlock.clear();
		
		Cursor cur = _db.rawQuery("select * from blocklist", null);
		
		try{
			int mount	= cur.getColumnIndex("mount");
			int nam		= cur.getColumnIndex("nam");
			int desc	= cur.getColumnIndex("desc");
			int gnl		= cur.getColumnIndex("gnl");
			int dj		= cur.getColumnIndex("dj");
			
			if(cur.getCount() > 0 && cur.moveToFirst()){
				do{
					Block block = new Block();
					
					block._mount 	= cur.getString( mount );
					block._nam		= cur.getString( nam );
					block._desc	= cur.getString( desc );
					block._gnl		= cur.getString( gnl );
					block._DJ		= cur.getString( dj );
					
					_arrayBlock.add(block);
				}while(cur.moveToNext());
			}
		}catch(SQLiteException e){
		}finally{
			cur.close();
		}
	}

	private void addDB(Block block){
		ContentValues values = new ContentValues();
		
		values.put("mount", block._mount);
		values.put("nam", 	block._nam);
		values.put("desc",	block._desc);
		values.put("gnl",	block._gnl);
		values.put("dj",	block._DJ);
		
		_db.insert("blocklist", null, values);
	}
	
	private void deleteDB(String mount){
		_db.delete("blocklist", "mount = ?", new String[]{ mount });
	}
	

}
