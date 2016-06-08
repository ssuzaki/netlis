package jp.codepanic.netlis;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


public class SearchResultActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      setContentView(R.layout.search_result_activity);
        
        Intent intent = getIntent(); 
        
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
           //⇒インテントのアクションがActionSearchだった場合
            
            //検索バーにて指定したキーワードを取り出す。
            String keyword = intent.getStringExtra(SearchManager.QUERY);
            
            //受け取った「keyword」を元に検索処理を行う。
            //たとえば、DBの中を一致検索するなど。
            
            //ここでは受け取った「keyword」をToast表示します。
            Toast.makeText(this, "検索キーワードは「" + keyword + "」です。", Toast.LENGTH_LONG).show();
        }
        
        finish();
    }
}
