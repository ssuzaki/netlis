package jp.codepanic.netlis;

import java.io.File;
import java.util.ArrayList;

import android.os.Environment;

public class RecList {

	public ArrayList<File>	_array = new ArrayList<File>();
	
	public RecList(){
	}
	
	public String getRecPath(){
		return Environment.getExternalStorageDirectory().getPath() + "/netlis";
	}
	
	public void delete(String filename){
		try{
			File file = new File(getRecPath() + "/" + filename);
			file.delete();
		}catch(Exception e){
		}
	}
	
	public void search(){
		
		_array.clear();
		
		File[]	aFiles = GetFileList( getRecPath() );

		if(aFiles != null){
			for(File file : aFiles)
			{
				if(!file.isDirectory())
					_array.add(file);
			}
		}
	}

	//
	//パスで指定されたフォルダ内のファイル／フォルダをソートして返す
	//
	public	File[]	GetFileList(String strPath)
	{
		//ソート用の独自オブジェクトクラス　ここでしか使わないから関数内で無理やり宣言
		final class Data
		{
			private File _data;

			public Data(File data)
			{
				_data = data;
			}

			public	File	getFile()
			{
				return	_data;
			}
			
			public	int	Compare(Data cmp)
			{
				String	str1 = _data.getAbsolutePath();
				String	str2 = cmp._data.getAbsolutePath();

				if(cmp == null || cmp._data == null || _data == null)
					return	0;
				
				if(_data.isDirectory() == cmp._data.isDirectory())
					return	str1.compareToIgnoreCase(str2);
				if(_data.isDirectory())
					return	-1;
				return	1;
			}
		}

		//ソート用比較関数 　ここでしか使わないから関数内で無理やり宣言
		final class DataComparator implements java.util.Comparator
		{
			public int compare(Object o1, Object o2)
			{
				return	((Data)o1).Compare((Data)o2);
			}
		}
		
		
		//strPathをファイルオブジェクトにする
		File	file = new File(strPath);

		//strPathがファイルだったらそのファイルが含まれるフォルダを処理対象とする
		if(file.isFile())
		{
			file = file.getParentFile();
			if(file == null)
				return	null;
		}

		int		i;
		File[]	afTmp;

		//フォルダ内のファイル配列を取得
		afTmp = file.listFiles();
		if(afTmp == null || afTmp.length == 0)
			return	null;
		
		//一度独自オブジェクトのリストに変換して、、、
		java.util.ArrayList	alist = new java.util.ArrayList();
		for(i = 0; i < afTmp.length; i++)
		{
			if(afTmp[i].isHidden() == false)	//隠しファイル／フォルダは無視
				alist.add(new Data(afTmp[i]));
		}

		//オブジェクトリストからオブジェクト配列に変換
		Object[]	aObject = alist.toArray();

		//javaのオブジェクト用メソッドを使ってソート
		java.util.Arrays.sort(aObject,new DataComparator());

		//オブジェクト配列からファイル配列に変換
		afTmp = new File[aObject.length];
		for(i = 0; i < aObject.length; i++)
			afTmp[i] = ((Data)aObject[i]).getFile();

		return	afTmp;
	}	
}
