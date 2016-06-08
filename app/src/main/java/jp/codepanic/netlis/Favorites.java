package jp.codepanic.netlis;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class Favorites {

	static final int 	DB_VERSION = 2;
	static final String DB = "favorite.db";
	
	private static class DBHelper extends SQLiteOpenHelper {
	    public DBHelper(Context c) {
	        super(c, DB, null, DB_VERSION);
	    }
	    
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(
        		"create table favorite ( _id integer primary key autoincrement, mount text not null, nam text, desc text, gnl text, dj text );"
	        	);
	    }
	    
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        db.execSQL(
	        	"drop table favorite"
	        	);
	        onCreate(db);
	    }
	}	



	public ArrayList<Favorite>	_arrayFV = new ArrayList<Favorite>();
	
	static SQLiteDatabase _db;
	
	public Favorites(Context c){
		DBHelper helper = new DBHelper(c);
		_db = helper.getWritableDatabase();
		
		load();
	}
	
	public void close(){
		_db.close();
	}
	
	public void add(Channel ch){
		Favorite fav = new Favorite(ch._MNT, ch._NAM, ch._DESC, ch._GNL, ch._DJ);
		add(fav);
	}
	
	public void add(Favorite fav){
		_arrayFV.add(fav);
		addDB(fav);
	}
	
	public void update(int pos, Favorite fav){
		_arrayFV.set(pos, fav);
		deleteDB(fav._mount);
		addDB(fav);
	}
	
	public void delete(String mount){
		
		for(int index = 0; index < _arrayFV.size(); index++){
			if(_arrayFV.get(index)._mount.equalsIgnoreCase(mount)){
				_arrayFV.remove(index);
				deleteDB(mount);
				break;
			}
		}
		
	}
	
	public boolean isExist(Channel ch){
		
		for(Favorite fav : _arrayFV){
			if(fav._mount.equalsIgnoreCase(ch._MNT))
				return true;
		}
		
		return false;
	}
	
	private void load(){
		_arrayFV.clear();
		
		Cursor cur = _db.rawQuery("select * from favorite", null);
		
		try{
			int mount	= cur.getColumnIndex("mount");
			int nam		= cur.getColumnIndex("nam");
			int desc	= cur.getColumnIndex("desc");
			int gnl		= cur.getColumnIndex("gnl");
			int dj		= cur.getColumnIndex("dj");
			
			if(cur.getCount() > 0 && cur.moveToFirst()){
				do{
					Favorite fv = new Favorite();
					
					fv._mount 	= cur.getString( mount );
					fv._nam		= cur.getString( nam );
					fv._desc	= cur.getString( desc );
					fv._gnl		= cur.getString( gnl );
					fv._DJ		= cur.getString( dj );
					
					_arrayFV.add(fv);
				}while(cur.moveToNext());
			}
		}catch(SQLiteException e){
		}finally{
			cur.close();
		}
	}

//	private void save(){
//		_db.execSQL("drop table favorite");
//		
//		for(Favorite fv : _arrayFV){
//			addDB(fv);
//		}
//	}
	
	private void addDB(Favorite fav){
		ContentValues values = new ContentValues();
		
		values.put("mount", fav._mount);
		values.put("nam", 	fav._nam);
		values.put("desc",	fav._desc);
		values.put("gnl",	fav._gnl);
		values.put("dj",	fav._DJ);
		
		_db.insert("favorite", null, values);
	}
	
	private void deleteDB(String mount){
		_db.delete("favorite", "mount = ?", new String[]{ mount });
	}
	
//	public void deleteAll(){
//		_db.delete("favorite", "_id like '%'", null);
//	}
	
//	private boolean isExistDB(String mount){
//		
//		// SQLインジェクション対応済
//		
//		SQLiteStatement state = null;
//		
//		try{
//			state = _db.compileStatement("select count(*) from favorite where mount = ?");
//			state.bindString(1, mount);
//			long len = state.simpleQueryForLong();
//			if(len > 0)
//				return true;
//		}catch(SQLiteDatabaseCorruptException e){
//			
//		}catch(Exception ex){
//			
//		}finally{
//			if(state != null)
//				state.close();
//		}
//		
//		return false;
//	}
}
