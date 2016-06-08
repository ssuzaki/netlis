package jp.codepanic.netlis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
	public void onReceive(Context context, Intent intent){
    	
//    	Alarm Manager を使うときの注意点として、
//    	"onReceive() を返したあとは、すぐにスリープ状態になることがある" ということ
//    	これを回避するために、onReceive() 内で、
//    	別の wake lock policy を使ってスリープ状態をめるようにしてみる
    	
        // スクリーンオン  
//        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
//        PowerManager.WakeLock wl = pm.newWakeLock(
//        	PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
//        	PowerManager.ACQUIRE_CAUSES_WAKEUP	 |
//        	PowerManager.ON_AFTER_RELEASE,
//        	"netlis app Tag");
//        wl.acquire();
//        wl.release();
        
//    	Toast.makeText(context, "called ReceivedActivity", Toast.LENGTH_SHORT).show();
    	
        //
        // Mainを起こす。
        // android:launchMode="singleTop"なので二重機動されない
        // onResumeで目覚める
        //
//    	Intent i = new Intent(context, MainActivity.class);
//    	i.setAction("alarm");
//    	PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
//    	
//    	try{
//    		pi.send();
//    	}catch(Exception e){
//    	}
        
        
		// IntentService始動
	    Intent i = new Intent(context, RefreshService.class);
	    intent.putExtra("from", "Alert");
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    context.startService(i);
    }
    
}
