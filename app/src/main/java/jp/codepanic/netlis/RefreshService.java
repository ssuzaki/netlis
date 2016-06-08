package jp.codepanic.netlis;

import android.app.IntentService;
import android.content.Intent;

public class RefreshService extends IntentService {

	final static String TAG = "RefreshService";

	public static MainActivity _main = null;
	
	public RefreshService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
//		Log.d("unko", "RefreshService");
		
		if(_main != null && intent != null && intent.getStringExtra("from") != null){

//			Log.d("unko", "RefreshService ok");
			
			boolean ui = intent.getStringExtra("from").equalsIgnoreCase("UI");
			
			if(_main != null)
				_main.refreshThreadFunc(ui);
		}else{
//			Log.d("unko", "RefreshService ng");

			// ※ここでヌルポ多発
			if(_main != null)
				_main.refreshThreadFunc(false);
		}
		
		// ここでサービス終了
	}

}
