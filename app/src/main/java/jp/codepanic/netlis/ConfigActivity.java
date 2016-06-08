package jp.codepanic.netlis;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.SearchRecentSuggestions;
import android.widget.Toast;

public class ConfigActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static MainActivity _main = null;
	
	public static final String key_common_notify	= "common_notify";
	public static final String key_sort				= "sort";
	public static final String key_favtop			= "favorite_top";
	public static final String key_auto_refresh 	= "auto_refresh";
	public static final String key_auto_refresh_min = "auto_refresh_min";
	public static final String key_auto_refresh_wakeup = "auto_refresh_wakeup";
	public static final String key_notify_fav		= "notify_fav";
	public static final String key_notify_fav_music = "notify_fav_music";
	public static final String key_notify_fav_vibe 	= "notify_fav_vibe";
	public static final String key_notify_fav_lamp 	= "notify_fav_lamp";
	
	public static final String key_version	= "version";
	public static final String key_share	= "share";
	public static final String key_review	= "review";
	public static final String key_clear_search = "clear_search";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceScreen share = (PreferenceScreen)findPreference(key_share);
		share.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				tweet();
				return true;
			}
		});

		PreferenceScreen review = (PreferenceScreen)findPreference(key_review);
		review.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				review();
				return true;
			}
		});

		PreferenceScreen clearSearch = (PreferenceScreen)findPreference(key_clear_search);
		clearSearch.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clearSearch();
				return true;
			}
		});
		
		update();
	}

	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	 
	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	// 設定値が変更されたときにSummaryを変更する
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,  String key) {
		update();
	}	
	
	void update(){
		updateNotifyGoing();
		updateSortCategory();
//		updateFavoriteTop();
		updateNotifyFav();
//		updateNotifyFavMusic();
		updateNotifyFavVibe();
//		updateNotifyFavLamp();
		updateAutoRefreshMin();
		updateAutoRefreshWakeup();
		updateVersion();
	}
	
	void updateNotifyGoing(){
		CheckBoxPreference chk = (CheckBoxPreference)getPreferenceScreen().findPreference(key_common_notify);
		if(_main != null){
			if(chk.isChecked())
				_main.resetNotifyGoing();
			else
				_main.cancelNotifyGoing();
		}
	}
	
	void updateSortCategory(){
		String[] items = getResources().getStringArray(R.array.sort_array);
		ListPreference list = (ListPreference)getPreferenceScreen().findPreference(key_sort);
		list.setSummary( items[ Integer.parseInt(list.getValue()) ] );
	}
	
//	void updateFavoriteTop(){
//		CheckBoxPreference chk = (CheckBoxPreference)getPreferenceScreen().findPreference(key_favtop);
//	}
	
	void updateAutoRefreshMin(){
//		String[] items = getResources().getStringArray(R.array.auto_refresh_min_array);
		ListPreference list = (ListPreference)getPreferenceScreen().findPreference(key_auto_refresh_min);
		list.setSummary( list.getValue() + "分" );
		list.setEnabled( isAutoRefresh() );
	}
	
	void updateAutoRefreshWakeup(){
		CheckBoxPreference chk = (CheckBoxPreference)getPreferenceScreen().findPreference(key_auto_refresh_wakeup);
		chk.setEnabled( isAutoRefresh() && isNotifyFav() );
	}
	
	boolean isAutoRefresh(){
		CheckBoxPreference chk = (CheckBoxPreference)getPreferenceScreen().findPreference(key_auto_refresh);
		return chk.isChecked();
	}
	
	boolean isNotifyFav(){
		CheckBoxPreference chk = (CheckBoxPreference)getPreferenceScreen().findPreference(key_notify_fav);
		return chk.isChecked();
	}
	
	void updateNotifyFav(){
		CheckBoxPreference chk = (CheckBoxPreference)getPreferenceScreen().findPreference(key_notify_fav);
		chk.setEnabled( isAutoRefresh() );
	}
	
	void updateNotifyFavMusic(){
		ListPreference list = (ListPreference)getPreferenceScreen().findPreference(key_notify_fav_music);
		list.setSummary( list.getValue() );
		list.setEnabled( isAutoRefresh() && isNotifyFav() );
		
		//
		// 通知音 一覧
		//
		ArrayList<CharSequence> array = new ArrayList<CharSequence>();
		array.add("なし");
		
        RingtoneManager ringtoneManager = new RingtoneManager(getApplicationContext());
        
        // 通知音一覧が欲しい（setTypeしなければ着信音一覧）
        ringtoneManager.setType( RingtoneManager.TYPE_NOTIFICATION );
        
        // 行セットが最初に返されたとき、カーソルの位置は -1, つまり最初の行の一つ前に設定されています。
        Cursor cursor = ringtoneManager.getCursor();
        while(cursor.moveToNext()){
        	array.add( cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX) );
        }
        
        CharSequence entry[] = array.toArray(new CharSequence[]{});
        
        list.setEntries(entry);
		list.setEntryValues(entry);
	}
	
	void updateNotifyFavVibe(){
		CheckBoxPreference chk = (CheckBoxPreference)getPreferenceScreen().findPreference(key_notify_fav_vibe);
		chk.setEnabled( isAutoRefresh() && isNotifyFav() );
	}
	
	void updateNotifyFavLamp(){
		CheckBoxPreference chk = (CheckBoxPreference)getPreferenceScreen().findPreference(key_notify_fav_lamp);
		chk.setEnabled( isAutoRefresh() && isNotifyFav() );
	}
	
	void updateVersion(){
		Preference version = getPreferenceScreen().findPreference(key_version);
		version.setSummary( getVersionName() );
	}

	void tweet(){
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
//            intent.setType("application/twitter"); だめだった  
            intent.putExtra(Intent.EXTRA_TEXT, "Android版 ねとらじアプリ「ねとりす」公開中 https://play.google.com/store/apps/details?id=jp.codepanic.netlis #ねとらじ");
            startActivity(intent);
        } catch (Exception e) {
        }
    }
	
	void review(){
		try{
			Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=jp.codepanic.netlis");
			Intent i = new Intent(Intent.ACTION_VIEW,uri);
			startActivity(i);
		}catch(Exception e){
		}
	}
	
	void clearSearch(){
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
				getApplicationContext(),
			    SuggestionsProvider.AUTHORITY, 
			    SuggestionsProvider.MODE);

		suggestions.clearHistory();
		
		toast("検索履歴をすべて削除しました。");
	}
	
    void toast(String msg){
    	Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


//	void showVersion(){
//	    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
//	    alertDialog.setIcon(R.drawable.ic_launcher);
//	    alertDialog.setTitle( getString(R.string.app_name) );
//	    alertDialog.setMessage( getVersionName() );
//	    alertDialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
//	        @Override
//			public void onClick(DialogInterface dialog,int whichButton) {
//	            setResult(RESULT_OK);
//	        }
//	    });
//	    alertDialog.create();
//	    alertDialog.show();
//	}	
	
	String getVersionName(){
        int versionCode = 0;
        String versionName = "";
        PackageManager packageManager = this.getPackageManager();
 
        try {
               PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
               versionCode = packageInfo.versionCode;
               versionName = packageInfo.versionName;
        } catch (NameNotFoundException e) {
               e.printStackTrace();
        }
        
        return "version : " + versionName;
	}    
    
}
