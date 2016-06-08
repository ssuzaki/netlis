package jp.codepanic.netlis;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import temp.ListPlayer;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener /*implements Runnable*/ {

    /** Log用tag */
    private final static String LOG_TAG = "netlis";
    /** 内容に合わせる */
    private final static int WC = LinearLayout.LayoutParams.WRAP_CONTENT;
    /** 親コンテンツに合わせる */
    private final static int FP = LinearLayout.LayoutParams.FILL_PARENT;
    /** URL入力用エディットボックス */
//    private EditText url_input;
    /** 結果表示用テキストビュー */
//    private TextView result_view;
    /**gzip追加有無 */
//    private CheckBox cb_Gzipx;

	// ユニークなIDを取得するために、R.layout.mainのリソースIDを使います
	private static int NOTIFICATION_ID_FAV  = R.layout.activity_main;
	private static int NOTIFICATION_ID_PLAY = R.layout.activity_main + 1;
    
	//
	// 録音
	//
	public ListPlayer 	listPlayer;
	public LinearLayout root;
	
    //
    // 設定
    //
    SharedPreferences _pref;
	final int REQUEST_CODE_CONFIG	= 0;
    
//    static ProgressDialog	_dlgProgress;
//    Thread _thread = null;
    
    Favorites		_fav = null;
    BlockList		_block = null;
    Histories		_his = null;
    RecordingList	_recording = null;
    RecList			_reclist = null;
    
    Date		_dateUpdate;
    int			_totalChannel;
    int			_totalListener;
    int			_totalFav;
    
    int			_sortColumnIndex 		= 0;
    boolean		_sortDsc 				= true;
    public static boolean _favoriteTop 	= true;
    boolean		_autoRefresh;
    int			_autoRefreshMin;
    boolean		_autoRefreshWakeup;
    boolean		_notifyFav;
    String		_notifyFavMusic;
    boolean		_notifyFavVibe;
    boolean		_notifyFavLamp;
    
    int			_sleepSec = 0;
    
    int			_flipBack;
    
    String		_playMount = null;
    String		_searchText= "";

	SwipeRefreshLayout _swipeRefresh;
    TextView	_txHeader;
    TextView	_txSearch;
    ViewFlipper	_viewFlipper;
    ListView	_listviewCh;
    ListView	_listviewFav;
    ListView	_listviewHis;
    ListView	_listviewBlock;
    ListView	_listviewRec;

//    MediaPlayer player = new MediaPlayer();
    AsyncPlayer 	_player = new AsyncPlayer("unko");
    AudioManager	_audioManager;
    
    Animation	_animLeftIn;
    Animation	_animLeftOut;
    Animation	_animRightIn;
    Animation	_animRightOut;
    
    ListViewItemAdapter _adapter;
    FavoriteItemAdapter _adapterFav;
    HistoryItemAdapter	_adapterHis;
    BlockItemAdapter 	_adapterBlock;
    RecItemAdapter		_adapterRec;
    ArrayList<Channel>	_arrayCH 		= new ArrayList<Channel>();	// 全件
    ArrayList<Channel>	_arrayCHSearch 	= new ArrayList<Channel>();	// 検索結果
    ArrayList<Channel>	_arrayCHNotified= new ArrayList<Channel>();	// お気に入り通知済み判定用
    Channel				_chCurrent;
    
//    ClickableImageView	_btnFavorite;
//    ClickableImageView	_btnReclist;
//    ClickableImageView	_btnHistory;
//    ClickableImageView	_btnBlocklist;
//    ClickableImageView	_btnRefresh;
//    ClickableImageView	_btnSort;
    
    TextView	_txDetailTitle;
    TextView	_txDetailDJ;
    TextView	_txDetailGNL;
    TextView	_txDetailDesc;
    TextView	_txDetailPURL;
    TextView	_txDetailURL;
    TextView	_txDetailSURL;
    TextView	_txDetailSoundQuality;
    TextView	_txDetailTime;
    TextView	_txDetailListener;
    ClickableImageView	_btnDetailFavorite;
    ClickableImageView	_btnDetailRec;
//    ClickableImageView	_btnDetailBlock;
//    ClickableImageView	_btnDetailShare;
//    Button		_btnFavAdd;
//    Button		_btnFavDelete;
//    Button		_btnFavSelectAll;
//    Button		_btnHisDelete;
//    Button		_btnHisSelectAll;
//    Button		_btnBlockAdd;
//    Button		_btnBlockDelete;
//    Button		_btnBlockSelectAll;
    
    TextView	_txReclistHeader;
    
    LinearLayout		_llPlayer;
    TextView			_txPlayerTitle;
    ClickableImageView	_btnPlayerPlay;
    SeekBar				_barPlayerVol;

//	Toolbar _toolbar;
	DrawerLayout _drawerLayout;
	ActionBarDrawerToggle _drawerToggle;
	ListView _sideListView;
	Dialog _loading;

	@Override
	public void onRefresh() {
		refresh();
	}

	public class SideMenuItemAdapter extends BaseAdapter{

		List<String> array = Arrays.asList(
				"番組表", "お気に入り", "録音フォルダ", "ブロックリスト", "再生履歴", "設定");

		@Override
		public int getCount() {
			return array.size();
		}

		@Override
		public Object getItem(int position) {
			return array.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			if(v == null){
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.side_menu_listview_item, null);
			}

			((TextView)v.findViewById(R.id.side_menu_text)).setText(array.get(position));

			return v;
		}
	}

	public class ListViewItemAdapter extends BaseAdapter{

    	TextView	tvTitle;
    	TextView	tvDesc;
    	TextView	tvGnl;
    	TextView	tvDJ;
    	TextView	tvCLN;
    	TextView	tvSound;
    	ImageView	imageFav;
    	ImageView	imageRecording;
    	ImageView	imageOnAir;
    	
		@Override
		public int getCount() {
			if( _searchText.isEmpty() )
				return _arrayCH.size();
			else
				return _arrayCHSearch.size();
		}

		@Override
		public Object getItem(int position) {
			if( _searchText.isEmpty() )
				return _arrayCH.get(position);
			else
				return _arrayCHSearch.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			
			if(v == null){
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.channel_listview_item, null);
			}
			
			tvTitle		= (TextView)v.findViewById(R.id.textTitle);
			tvDesc		= (TextView)v.findViewById(R.id.textDesc);
			tvGnl		= (TextView)v.findViewById(R.id.textGnl);
			tvDJ		= (TextView)v.findViewById(R.id.textDJ);
			tvCLN		= (TextView)v.findViewById(R.id.textCLN);
			tvSound		= (TextView)v.findViewById(R.id.textSound);
			imageFav	= (ImageView)v.findViewById(R.id.imageFavorite);
			imageRecording= (ImageView)v.findViewById(R.id.imageRecording);
			imageOnAir	= (ImageView)v.findViewById(R.id.imageOnAir);
			
			Channel ch = null;
			if( _searchText.isEmpty() )
				ch = _arrayCH.get(position);
			else
				ch = _arrayCHSearch.get(position);
			
			tvTitle.setText(ch._NAM);
			tvDesc.setText(ch._DESC);
			tvGnl.setText(ch._GNL);
			tvDJ.setText(ch._DJ);
			tvCLN.setText(ch._CLN);
			tvSound.setText( ch.getSoundQuality() );
			
			if(ch.isFavorite()){
			    Resources r = getResources();
			    Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.favorite);
			    
				imageFav.setImageBitmap(bmp);
				
//			    v.setBackgroundColor(0xFFC44E);
			}else{
				imageFav.setImageBitmap(null);
			}
			
			if(ch.isRecording()){
			    Resources r = getResources();
			    Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.recording);
			    
				imageRecording.setImageBitmap(bmp);
			}else{
				imageRecording.setImageBitmap(null);
			}
			
			if(ch._MNT.equalsIgnoreCase(_playMount)){
			    Resources r = getResources();
			    Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.onair);
			    
				imageOnAir.setImageBitmap(bmp);
			}else{
				imageOnAir.setImageBitmap(null);				
			}
			
			return v;
		}
    }
    
    public class FavoriteItemAdapter extends BaseAdapter{

    	TextView	tvMount;
    	TextView	tvTitle;
    	TextView	tvDesc;
    	TextView	tvGnl;
    	TextView	tvDJ;
    	
		@Override
		public int getCount() {
			return _fav._arrayFV.size();
		}

		@Override
		public Object getItem(int position) {
			return _fav._arrayFV.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {

			if(v == null){
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.favorite_listview_item, null);
			}			

			tvMount	= (TextView)v.findViewById(R.id.textFavMount);
			tvTitle	= (TextView)v.findViewById(R.id.textFavTitle);
			tvDesc	= (TextView)v.findViewById(R.id.textFavDesc);
			tvGnl	= (TextView)v.findViewById(R.id.textFavGnl);
			tvDJ	= (TextView)v.findViewById(R.id.textFavDJ);
			
			Favorite fav = _fav._arrayFV.get(position);
			
			tvMount.setText(fav._mount);
			tvTitle.setText(fav._nam);
			tvDesc.setText(fav._desc);
			tvGnl.setText(fav._gnl);
			tvDJ.setText(fav._DJ);
			
			return v;
		}
    }
    
    public class BlockItemAdapter extends BaseAdapter{

    	TextView	tvMount;
    	TextView	tvTitle;
    	TextView	tvDesc;
    	TextView	tvGnl;
    	TextView	tvDJ;
    	
		@Override
		public int getCount() {
			return _block._arrayBlock.size();
		}

		@Override
		public Object getItem(int position) {
			return _block._arrayBlock.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {

			if(v == null){
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.block_listview_item, null);
			}			

			tvMount	= (TextView)v.findViewById(R.id.textBlockMount);
			tvTitle	= (TextView)v.findViewById(R.id.textBlockTitle);
			tvDesc	= (TextView)v.findViewById(R.id.textBlockDesc);
			tvGnl	= (TextView)v.findViewById(R.id.textBlockGnl);
			tvDJ	= (TextView)v.findViewById(R.id.textBlockDJ);
			
			Block block = _block._arrayBlock.get(position);
			
			tvMount.setText(block._mount);
			tvTitle.setText(block._nam);
			tvDesc.setText(block._desc);
			tvGnl.setText(block._gnl);
			tvDJ.setText(block._DJ);
			
			return v;
		}
    }
    
    public class RecItemAdapter extends BaseAdapter{

    	TextView	tvTitle;
    	
		@Override
		public int getCount() {
			return _reclist._array.size();
		}

		@Override
		public Object getItem(int position) {
			return _reclist._array.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {

			if(v == null){
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.reclist_listview_item, null);
			}			

			tvTitle	= (TextView)v.findViewById(R.id.textReclistTitle);
			
			File file = _reclist._array.get(position);
			
			tvTitle.setText( file.getName() );
			
			return v;
		}
    }
    
    public class HistoryItemAdapter extends BaseAdapter{

    	TextView	tvTitle;
    	TextView	tvDesc;
    	TextView	tvGnl;
    	TextView	tvDJ;
    	TextView	tvSound;
    	ImageView	imageFav;
    	
		@Override
		public int getCount() {
			return _his._arrayHis.size();
		}

		@Override
		public Object getItem(int position) {
			return _his._arrayHis.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			
			if(v == null){
				LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.history_listview_item, null);
			}
			
			tvTitle		= (TextView)v.findViewById(R.id.textHisTitle);
			tvDesc		= (TextView)v.findViewById(R.id.textHisDesc);
			tvGnl		= (TextView)v.findViewById(R.id.textHisGnl);
			tvDJ		= (TextView)v.findViewById(R.id.textHisDJ);
			tvSound		= (TextView)v.findViewById(R.id.textHisSound);
			imageFav	= (ImageView)v.findViewById(R.id.imageHisFavorite);
			
			Channel ch = new Channel( _his._arrayHis.get(position) );
			
			tvTitle.setText(ch._NAM);
			tvDesc.setText(ch._DESC);
			tvGnl.setText(ch._GNL);
			tvDJ.setText(ch._DJ);
			tvSound.setText( ch.getSoundQuality() );
			
			if(ch.isFavorite()){
			    Resources r = getResources();
			    Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.favorite);
			    
				imageFav.setImageBitmap(bmp);
				
//			    v.setBackgroundColor(0xFFC44E);
			}else{
				imageFav.setImageBitmap(null);
			}
			
			return v;
		}
    }    
    
    
    
    
    
    
    
    
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			switch( _viewFlipper.getDisplayedChild() ){
			case 0:
//				exit();
//				this.finish();
				
				// ※HOMEボタンを押した場合と同じように終わる
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				startActivity(intent);				
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				sort();				// お気に入り先頭にするから
				updateListView();	// お気に入りが変更とかされるから
				updateHeader();
				flipBack();
				break;
			}
			return true;
			
		case KeyEvent.KEYCODE_VOLUME_UP:
			setVolume( getVolume() + 1 );
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			setVolume( getVolume() - 1 );
			return true;
		case KeyEvent.KEYCODE_VOLUME_MUTE:
			setVolume(0);
			return true;
		}
		
		return false;
	}
	
	int getVolume(){
		return _audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
	}
	
	void setVolume(int vol){
		_barPlayerVol.setProgress(vol);
		_audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_SHOW_UI);
	}
	
	void flip(int to, Animation in, Animation out){
		_flipBack = _viewFlipper.getDisplayedChild();
		
		_viewFlipper.setInAnimation(in);
		_viewFlipper.setOutAnimation(out);
		_viewFlipper.setDisplayedChild(to);
	}
	
	void flipBack(){
//		flip(_flipBack, _animLeftIn, _animRightOut);
		
		if(_flipBack == _viewFlipper.getDisplayedChild())
			_flipBack = 0;
		
		_viewFlipper.setInAnimation(_animLeftIn);
		_viewFlipper.setOutAnimation(_animRightOut);
		_viewFlipper.setDisplayedChild(_flipBack);
		
		if(_flipBack != 1 && _playMount == null)
			_llPlayer.setVisibility(View.GONE);
	}
	
	void exit(){
		
		if(_playMount != null || _recording._array.size() > 0){
		
			new AlertDialog.Builder(this)
		    .setIcon(R.drawable.ic_launcher)
			.setTitle("終了しますか？")
			.setMessage("再生・録音は停止されます。")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	doExit();
	            }
	        })
	        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            }
	        })
			.show();
			
		}else{
			doExit();
		}
	}
	
	void doExit(){
		stop();
		_recording.stopRecAll();
		
		_fav.close();
		_block.close();
		_his.close();
		
		this.finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RefreshService._main = this;
		ConfigActivity._main = this;
		_loading = Loading.getDialog(this);
		
		//
		// 検索用
		//
//		final Intent queryIntent = getIntent();
//		final String queryAction = queryIntent.getAction();
//		
//	    // ACTION_SEARCH の Intent で呼び出された場合  
//	    if (Intent.ACTION_SEARCH.equals(queryAction)) {         
//	        doSearchWithIntent(queryIntent);  
//	    }  
//	  
//	    // Quick Search Box から呼び出された場合  
//	    if (Intent.ACTION_VIEW.equals(queryAction)){  
//	        if(queryIntent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {  
//	            doSearchWithIntent(queryIntent);  
//	        }  
//	    }
		
		
		
		_pref = PreferenceManager.getDefaultSharedPreferences(this);

		_fav 		= new Favorites(getApplicationContext());
		_block		= new BlockList(getApplicationContext());
		_his 		= new Histories(getApplicationContext());
		_recording	= new RecordingList();
		_reclist	= new RecList();
		
		Channel._fav 		= _fav;
		Channel._block		= _block;
		Channel._recording	= _recording;
		
	    _animLeftIn		= AnimationUtils.loadAnimation(this, R.anim.left_in);
	    _animLeftOut	= AnimationUtils.loadAnimation(this, R.anim.left_out);
	    _animRightIn	= AnimationUtils.loadAnimation(this, R.anim.right_in);
	    _animRightOut	= AnimationUtils.loadAnimation(this, R.anim.right_out);

		_swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.channel_pull_to_refresh);
		_txHeader	= (TextView)findViewById(R.id.textHeader);
	    _txSearch	= (TextView)findViewById(R.id.textSearch);
		_viewFlipper= (ViewFlipper)findViewById(R.id.flipper);
		_listviewCh	= (ListView)findViewById(R.id.list);
		_listviewFav= (ListView)findViewById(R.id.listFavorites);
		_listviewHis= (ListView)findViewById(R.id.listHistory);
		_listviewBlock=(ListView)findViewById(R.id.listBlocklist);
		_listviewRec= (ListView)findViewById(R.id.listReclist);
//		_btnFavorite= (ClickableImageView)findViewById(R.id.btnFavorite);
//		_btnReclist	= (ClickableImageView)findViewById(R.id.btnRecList);
//		_btnHistory	= (ClickableImageView)findViewById(R.id.btnHistory);
//		_btnBlocklist	= (ClickableImageView)findViewById(R.id.btnBlocklist);
//		_btnRefresh	= (ClickableImageView)findViewById(R.id.btnRefresh);
//		_btnSort	= (ClickableImageView)findViewById(R.id.btnSort);
		
		_txDetailTitle	= (TextView)findViewById(R.id.detailTitle);
		_txDetailDJ		= (TextView)findViewById(R.id.detailDJ);
		_txDetailGNL	= (TextView)findViewById(R.id.detailGNL);
		_txDetailDesc	= (TextView)findViewById(R.id.detailDESC);
		_txDetailPURL	= (TextView)findViewById(R.id.detailPURL);
		_txDetailURL	= (TextView)findViewById(R.id.detailURL);
		_txDetailSURL	= (TextView)findViewById(R.id.detailSURL);
		_txDetailSoundQuality 	= (TextView)findViewById(R.id.detailSoundQuality);
		_txDetailTime			= (TextView)findViewById(R.id.detailTime);
		_txDetailListener		= (TextView)findViewById(R.id.detailListener);
		_btnDetailFavorite	= (ClickableImageView)findViewById(R.id.btnDetailFavorite);
		_btnDetailRec		= (ClickableImageView)findViewById(R.id.btnDetailRec);
//		_btnDetailBlock		= (ClickableImageView)findViewById(R.id.btnDetailBlock);
//		_btnDetailShare		= (ClickableImageView)findViewById(R.id.btnDetailShare);
//		_btnFavAdd		= (Button)findViewById(R.id.btnFavAdd);
//		_btnFavDelete	= (Button)findViewById(R.id.btnFavDelete);
//		_btnFavSelectAll= (Button)findViewById(R.id.btnFavSelectAll);
//		_btnHisDelete	= (Button)findViewById(R.id.btnHisDelete);
//		_btnHisSelectAll= (Button)findViewById(R.id.btnHisSelectAll);
//		_btnBlockAdd	= (Button)findViewById(R.id.btnBlockAdd);
//		_btnBlockDelete	= (Button)findViewById(R.id.btnBlockDelete);
//		_btnBlockSelectAll= (Button)findViewById(R.id.btnBlockSelectAll);
		
		_llPlayer		= (LinearLayout)findViewById(R.id.player);
	    _txPlayerTitle	= (TextView)findViewById(R.id.playerTitle);
	    _btnPlayerPlay	= (ClickableImageView)findViewById(R.id.playerPlay);
	    _barPlayerVol	= (SeekBar)findViewById(R.id.playerVolume);
		
	    
		// ハードウェアキーをメディア音量に割り当て
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// 録音フォルダ場所
		_txReclistHeader= (TextView)findViewById(R.id.textReclistHeader);
		_txReclistHeader.setText("録音フォルダ : " + _reclist.getRecPath());
		
		_audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		_barPlayerVol.setMax(_audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		_barPlayerVol.setProgress(getVolume());
	    
		
		_listviewCh.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int pos, long id) {
				
				Channel ch = null;
				if( _searchText.isEmpty() )
					ch = _arrayCH.get(pos);
				else 
					ch = _arrayCHSearch.get(pos);
				showChannelDetail(ch);
			}
			
			
		});
		
		_btnPlayerPlay.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (_chCurrent != null) {
						if (_playMount != null) {
							toast("Stop");
							stop();
						} else {
							if (play(_chCurrent)) {
								toast("Play");
							}
						}
					}
				}
				return false;
			}
		});
		
		_listviewFav.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				CheckBox chk = (CheckBox)v.findViewById(R.id.checkFavSelect);
				chk.setChecked( !chk.isChecked() );
			}
		});
		
		_listviewFav.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				showFavDlg(pos);
				return false;
			}
		});
		
		_listviewBlock.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				CheckBox chk = (CheckBox)v.findViewById(R.id.checkBlockSelect);
				chk.setChecked( !chk.isChecked() );
			}
		});
		
		_listviewBlock.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				showBlockDlg(pos);
				return false;
			}
		});

		_listviewRec.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				CheckBox chk = (CheckBox)v.findViewById(R.id.checkReclistSelect);
				chk.setChecked( !chk.isChecked() );
			}
		});
		
		_listviewRec.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				File file = _reclist._array.get(pos);
				
				try{
					Intent it = new Intent(Intent.ACTION_VIEW);
					Uri uri =Uri.parse("file://" + file.getAbsolutePath());
					it.setDataAndType(uri,"audio/mp3");
					startActivity(it);
				}catch(Exception e){
				}
				
				return false;
			}
		});
		
		_listviewHis.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				CheckBox chk = (CheckBox) v.findViewById(R.id.checkHisSelect);
				chk.setChecked(!chk.isChecked());
			}
		});
		
		_listviewHis.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long arg3) {
				Channel ch = new Channel(_his._arrayHis.get(pos));
				showChannelDetail(ch);

//				onItemLongClickメソッドでfalseを返してしまうと、
//				onItemLongClick()後、onItemClick()が動いてしまうので注意
				return true;
			}
		});
		
		_barPlayerVol.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				_audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_SHOW_UI);
			}
		});

		/**
		 * 更新中アニメーション設定とか
		 */
		_swipeRefresh.setColorSchemeColors(Color.rgb(0x00, 0x87, 0xFF), Color.WHITE);
		_swipeRefresh.setOnRefreshListener(this);

		initDrawer();
		initFloatingButton();
		load();
	}

	private void initFloatingButton(){
		final FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.floatig_btn_sort);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_sortDsc = !_sortDsc;
				sort();
//				updateSortButton();
//				btn.setBackgroundResource( _sortDsc ? R.drawable.arrow_down : R.drawable.arrow_up );
				btn.setImageResource(_sortDsc ? R.drawable.arrow_down : R.drawable.arrow_up);
				updateListView();
			}
		});
	}

	private void initDrawer(){
		_drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		// （3本線の）切り替えボタンの生成
		_drawerToggle = new ActionBarDrawerToggle(
				this,
				_drawerLayout,
				R.string.app_name,
				R.string.app_name){
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}
		};
		_drawerToggle.setDrawerIndicatorEnabled(true);
		_drawerLayout.setDrawerListener(_drawerToggle);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//		getSupportActionBar().setIcon(R.drawable.ic_launcher);

		_sideListView = (ListView) findViewById(R.id.listview);
		_sideListView.setAdapter(new SideMenuItemAdapter());
		_sideListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				_drawerLayout.closeDrawers();
//				_viewFlipper.setDisplayedChild(position);

				switch (position) {
					case 0: flipBack();				break;
					case 1: onClickFavorite(view);	break;
					case 2: onClickReclist(view);	break;
					case 3: onClickBlocklist(view);	break;
					case 4: onClickHistory(view);	break;
					case 5:	config();				break;
				}
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(_loading != null && _loading.isShowing()){
			_loading.dismiss();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
//		Log.d("unko", "onStart");
		refresh();
	}

	@Override
	protected void onResume() {
		super.onResume();
//		Log.d("unko", "onResume");
		
//		Intent intent = getIntent();
//		if(intent.getAction().equalsIgnoreCase("alarm")){
//			refresh();
//		}
	}

	void showChannelDetail(Channel ch){
		_txDetailTitle.setText(ch._NAM);
		_txDetailDJ.setText(ch._DJ);
		_txDetailGNL.setText(ch._GNL);
		_txDetailDesc.setText(ch._DESC);
		_txDetailPURL.setText(ch.getPlayURL());
		_txDetailURL.setText(ch._URL);
		_txDetailSURL.setText(ch._SURL);
		_txDetailSoundQuality.setText(ch.getSoundQuality());
		_txDetailTime.setText(ch._TIMS);
		_txDetailListener.setText(ch.getListener());
		
		_chCurrent = ch;
		
		updateDetailFavIcon();
		updateDetailRecIcon();
		
		flip(1, _animRightIn, _animLeftOut);
		
		_llPlayer.setVisibility(View.VISIBLE);
	}
	
	void showFavDelete(){
		new AlertDialog.Builder(this)
	    .setIcon(R.drawable.ic_launcher)
		.setTitle("選択したお気に入りを削除しますか？")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
            	ArrayList<String> list = new ArrayList<String>();
            	
				for(int i = 0; i < _listviewFav.getChildCount(); i++){
					View view = _listviewFav.getChildAt(i);
					CheckBox chk = (CheckBox)view.findViewById(R.id.checkFavSelect);
					
					if(chk.isChecked()){
						list.add( _fav._arrayFV.get(i)._mount );
					}
				}
				
				for(String mount : list){
					_fav.delete(mount);
				}
				
				updateFavListview();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
		.show();
	}
	
	void showBlockDelete(){
		new AlertDialog.Builder(this)
	    .setIcon(R.drawable.ic_launcher)
		.setTitle("選択した番組をブロック解除しますか？")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
            	ArrayList<String> list = new ArrayList<String>();
            	
				for(int i = 0; i < _listviewBlock.getChildCount(); i++){
					View view = _listviewBlock.getChildAt(i);
					CheckBox chk = (CheckBox)view.findViewById(R.id.checkBlockSelect);
					
					if(chk.isChecked()){
						list.add( _block._arrayBlock.get(i)._mount );
					}
				}
				
				for(String mount : list){
					_block.delete(mount);
				}
				
				updateBlockListView();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
		.show();
	}	
	
	void showHisDelete(){
		new AlertDialog.Builder(this)
	    .setIcon(R.drawable.ic_launcher)
		.setTitle("選択した再生履歴を削除しますか？")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
            	ArrayList<Integer> list = new ArrayList<Integer>();
            	
				for(int i = 0; i < _listviewHis.getChildCount(); i++){
					View view = _listviewHis.getChildAt(i);
					CheckBox chk = (CheckBox)view.findViewById(R.id.checkHisSelect);
					
					if(chk.isChecked()){
						list.add(i);
					}
				}
				
				Collections.reverse(list);
				
				for(int pos : list){
					_his.delete(pos);
				}
				
				updateHisListview();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
		.show();
	}	
	
	void showRecDelete(){
		new AlertDialog.Builder(this)
	    .setIcon(R.drawable.ic_launcher)
		.setTitle("選択した録音ファイルを削除しますか？")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
				for(int i = 0; i < _listviewRec.getChildCount(); i++){
					View view = _listviewRec.getChildAt(i);
					CheckBox chk = (CheckBox)view.findViewById(R.id.checkReclistSelect);
					TextView tx = (TextView)view.findViewById(R.id.textReclistTitle);
					
					if(chk.isChecked()){
						_reclist.delete( tx.getText().toString() );
					}
				}
				
				updateRecListView();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
		.show();
	}
	
	void updateFavListview(){
		_adapterFav = new FavoriteItemAdapter();
		_listviewFav.setAdapter(_adapterFav);
	}
	
	void updateHisListview(){
		_adapterHis = new HistoryItemAdapter();
		_listviewHis.setAdapter(_adapterHis);
	}
	
	void updateBlockListView(){
		_adapterBlock = new BlockItemAdapter();
		_listviewBlock.setAdapter(_adapterBlock);
	}
	
	void updateRecListView(){
		_reclist.search();
		
		_adapterRec = new RecItemAdapter();
		_listviewRec.setAdapter(_adapterRec);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		_drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		_drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(_drawerToggle.onOptionsItemSelected(item)){
			return true;
		}

		switch(item.getItemId()) {
			case R.id.menu_search:
				onSearchRequested();    // 2.1まで？
//			startSearch("", false, null, false);
				return true;
			case R.id.menu_searchOff:
				_searchText = "";
				updateSearchHead();
				updateListView();
				break;
			case R.id.menu_config:
				config();
				break;
			case R.id.menu_sleep:
				sleep();
				break;
			case R.id.menu_exit:
				exit();
				break;
		}

		return super.onOptionsItemSelected(item);
	}	
	
	void config(){
		Intent intent = new Intent(this, ConfigActivity.class);
		startActivityForResult(intent, REQUEST_CODE_CONFIG);
	}
	
	void screenOff(){
		// システム権限必要だからメーカーアプリでもない限りダメ
//		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		pm.goToSleep(3000);
		
		//
		// 画面を消す
		//
		// 現在の設定を保持
		int defTime = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
				
		// すぐ消す
		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);
		
		// 保持した値に戻しておく
		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, defTime);
	}
	
	void stopSleepTimer(){
		
		_sleepSec = 0;
		
		updateHeader();
		
		if(_timer != null){
			_timer.cancel();
			_timer = null;
		}
	}
	
	void sleep(){

		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = inflater.inflate(R.layout.dlg_sleep, (ViewGroup)this.findViewById(R.id.layout_root_sleep));

		final TextView textMin	= (TextView) layout.findViewById(R.id.textSleepMin);
		final SeekBar  barMin	= (SeekBar)	 layout.findViewById(R.id.barSleepMin);
		
		textMin.setText( String.valueOf(_sleepSec/60) + " min" );
		
		barMin.setProgress(_sleepSec/60);
		
		barMin.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				_sleepSec = progress * 60;
				textMin.setText( String.valueOf(_sleepSec/60) + " min" );
			}
		});
		
		final AlertDialog dlg = new AlertDialog.Builder(this)
		    .setIcon(R.drawable.ic_launcher)
		    .setTitle("スリープタイマー")
		    .setView(layout)
		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        @Override
				public void onClick(DialogInterface dialog, int whichButton) {
		        	
		        	stopSleepTimer();
		        	
		        	_sleepSec = barMin.getProgress() * 60;
		        	
		        	updateHeader();
		        	
		        	if(_sleepSec > 0){
			        	SleepTimerTask task = new SleepTimerTask();
			        	
			        	_timer = new Timer(true);
			        	_timer.schedule(task, 1000, 1000);	// １秒後に１秒間隔で
		        	}
		        }
		    })
		    .setNeutralButton("Stop", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					stopSleepTimer();
				}
			})
		    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        @Override
				public void onClick(DialogInterface dialog, int whichButton) {
		        }
		    }).create();
		
		dlg.show();
	}
	
	void updateDetailFavIcon(){
		if(_chCurrent != null){
		    Resources r = getResources();
		    
		    Bitmap bmp = null;
			
			if(_chCurrent.isFavorite()){
			    bmp = BitmapFactory.decodeResource(r, R.drawable.fav_on);
			}else{
			    bmp = BitmapFactory.decodeResource(r, R.drawable.fav_off);
			}
			
		    _btnDetailFavorite.setImageBitmap(bmp);
		}
	}
	
	void updateDetailRecIcon(){
		if(_chCurrent != null){
		    Resources r = getResources();
		    
		    Bitmap bmp = null;
			
			if(_chCurrent.isRecording()){
			    bmp = BitmapFactory.decodeResource(r, R.drawable.stop);
			}else{
			    bmp = BitmapFactory.decodeResource(r, R.drawable.rec);
			}
			
		    _btnDetailRec.setImageBitmap(bmp);
		}
	}

	void refresh(){
		_loading.show();

		_swipeRefresh.setEnabled(false);

		// IntentService始動
	    Intent intent = new Intent(this, RefreshService.class);
	    intent.putExtra("from", "UI");
	    this.startService(intent);
	}
	
	void sort(){
		
		ArrayList<Channel> array = null;
		
		if( _searchText.isEmpty() )
			array = _arrayCH;
		else
			array = _arrayCHSearch;
		
		switch(_sortColumnIndex){
		case 0:
			if(_sortDsc)
				Collections.sort(array, new ChannelCompListenerDsc());
			else
				Collections.sort(array, new ChannelCompListenerAsc());
			break;
			
		case 1:
			if(_sortDsc)
				Collections.sort(array, new ChannelCompTimeDsc());
			else
				Collections.sort(array, new ChannelCompTimeAsc());
			break;
		}
	}
	
//	void updateSortButton(){
//	    Resources r = getResources();
//	    Bitmap bmp = _sortDsc ?
//	    	BitmapFactory.decodeResource(r, R.drawable.arrow_down) :
//    		BitmapFactory.decodeResource(r, R.drawable.arrow_up);
//
//		_btnSort.setImageBitmap(bmp);
//	}
	
	void updateListView(){
		
		int pos = _listviewCh.getFirstVisiblePosition();
//		int y	= _listview.getChildAt(0).getTop();
		
		_adapter = new ListViewItemAdapter();
		_listviewCh.setAdapter(_adapter);
		
		_listviewCh.invalidate();
//		_listview.setSelectionFromTop(pos, y);
		_listviewCh.setSelectionFromTop(pos, 0);
		
		// データ更新、再描画（落ちる？
//		_adapter.notifyDataSetChanged();
	}
	
	void updateHeader(){
		
		_totalFav = 0;
		for(Channel ch : _arrayCH){
			if(ch.isFavorite())
				_totalFav ++;
		}
		
		// 表示形式を設定
		SimpleDateFormat sdf = new SimpleDateFormat("kk':'mm':'ss");
		
		String msg = 
				sdf.format(_dateUpdate) +		"更新  " + 
				String.valueOf(_totalChannel) + " ch  /  " +
				String.valueOf(_totalListener)+	" user  /  " +
				String.valueOf(_totalFav)+		" fav";
		
		if(_sleepSec > 0){
			
			int min = _sleepSec / 60;
			int sec = _sleepSec - (min * 60);
			
			msg += "  [" + String.valueOf(min) + ":" + String.format("%1$02d",sec) + "]";
		}
		
		_txHeader.setText(msg);
	}
	
	void updateSearchHead(){
		if( _searchText.isEmpty() ){
			_txSearch.setVisibility( View.GONE );
		}else{
			_txSearch.setText( "『" + _searchText + "』で検索中" );
			_txSearch.setVisibility( View.VISIBLE );
		}
	}

	void notifyFav(Channel chFav, boolean showUI){
		
		// 既に通知済みならスルー
		for(Channel ch : _arrayCHNotified){
			if(ch._MNT.equalsIgnoreCase(chFav._MNT))
				return;
		}
		
		_arrayCHNotified.add(chFav);
		
		
		String msg = chFav._NAM + " 放送開始";
		
		if(showUI)
			toast(msg);
		
		if(_notifyFav){

// old version
//			Notification n = new Notification();
//			n.icon = R.drawable.ic_launcher;
//			n.tickerText = msg;
//
//			Intent i = new Intent(getApplicationContext(), MainActivity.class);
//			PendingIntent pend = PendingIntent.getActivity(this, 0, i, 0);
//			n.setLatestEventInfo(
//				getApplicationContext(),
//				"ねとりす",
//				msg,
//				pend);

			Notification n = new Notification.Builder(getApplicationContext())
					.setContentTitle("ねとりす")
					.setContentText(msg)
					.setTicker(msg)		// 通知到着時に通知バーに表示(4.4まで) 5.0からは表示されない
					.setSmallIcon(R.drawable.ic_launcher)
					.build();

			//NotificationManagerへNotificationインスタンスを設定して発行！
			NotificationManager man = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			man.cancel(NOTIFICATION_ID_FAV);
			man.notify(NOTIFICATION_ID_FAV, n);
			
			
/*
			if(!_notifyFavMusic.equalsIgnoreCase("なし")){
		        RingtoneManager ringtoneManager = new RingtoneManager(getApplicationContext());
		        
		        // 通知音一覧が欲しい（setTypeしなければ着信音一覧）
		        ringtoneManager.setType( RingtoneManager.TYPE_NOTIFICATION );
		        
		        // 行セットが最初に返されたとき、カーソルの位置は -1, つまり最初の行の一つ前に設定されています。
		        int position = 0;
		        Cursor cursor = ringtoneManager.getCursor();
		        while(cursor.moveToNext()){
		        	if(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX).equalsIgnoreCase(_notifyFavMusic)){
		        		ringtoneManager.getRingtone(position).play();
		        		break;
		        	}
		        	position++;
		        }
			}
*/
			if(_notifyFavVibe){
				long pattern[] = {100, 100, 100, 100}; // 1000ミリ秒OFF→500ミリ秒ON→3000ミリ秒OFF→500ミリ秒ON→1000ミリ秒OFF→500ミリ秒ON
				((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(pattern, -1); // 定義したパターン・リピートなしでバイブレーション開始
			}
/*
			if(_notifyFavLamp){
//				NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//				
//				
//				Notification n = new Notification(); // Notificationの生成
//				
//			    n.icon = R.drawable.ic_launcher; // アイコンの設定
//			    n.tickerText = "This is a notification message..."; // メッセージの設定
//			 
//			    Intent i = new Intent(getApplicationContext(), MainActivity.class);
//			    PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
//			    n.setLatestEventInfo(getApplicationContext(), "TITLE", "TEXT", pi);
//			 
//			    n.defaults |= Notification.DEFAULT_LIGHTS; // デフォルトLED点滅パターンを設定
//			    
////				n.ledARGB = 0xff0000ff; // LEDを青色に点滅させる
////				n.ledOnMS = 3000; // 点灯する時間は3000ミリ秒
////				n.ledOffMS = 1000; // 消灯する時間は1000ミリ秒
////				n.flags |= Notification.FLAG_SHOW_LIGHTS; // LED点灯のフラグを追加する
//				
//				nm.notify(1, n); // 生成したNotificationを通知する
				
				
				
//				 //システムトレイに通知するアイコン
//				int icon = R.drawable.ic_launcher;
//				long when = System.currentTimeMillis();
//				 
//				Notification notification = new Notification(icon, "aaa", when);
//				String title = "bbb";
//				//ステータスバーをクリックした時に立ち上がるアクティビティ
//				Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
//				notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//				Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				PendingIntent intent =
//				PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
//				notification.setLatestEventInfo(getApplicationContext(), title, "ccc", intent);
//				 
//				//通知の種類　音 バイブにしている時は鳴らない　
////				notification.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE ;
//				notification.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL ;
//				notification.ledOnMS = 100;
//				notification.ledOffMS = 100;
//				notification.ledARGB = Color.BLUE;
//				 
//				NotificationManager notificationManager = (NotificationManager)
//				getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//				 
//				notificationManager.notify(0, notification);				
				
				
				
//				Log.d("unko", "lamp");
				
				
				
			    Intent intent = new Intent(this, MainActivity.class);
			    PendingIntent pending = PendingIntent.getActivity(this, 0,
			            intent, PendingIntent.FLAG_CANCEL_CURRENT);
			    Notification notify = new Notification();
			    notify.defaults = Notification.DEFAULT_LIGHTS;
			    notify.icon = R.drawable.ic_launcher;
//			    notify.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
//			    notify.ledARGB = 0xff00ff00;
//			    notify.ledOnMS = 500;
//			    notify.ledOffMS = 500;
			    notify.setLatestEventInfo(this, "TEST INFO",
			            "テストのノーティフィケーションです。", pending);
			    NotificationManager manager = (NotificationManager)this.
			            getSystemService(Activity.NOTIFICATION_SERVICE);
			    manager.notify(0, notify);				
			}
*/
		}
	}
	
//	@Override
//	public void run(){
//	}
	
	void removeNotifiedChannel(){
		
		ArrayList<Integer> array = new ArrayList<Integer>();
		
		for(int i = 0; i < _arrayCHNotified.size(); i++){
			Channel ch = _arrayCHNotified.get(i);
			if(!isFindChannel(ch))
				array.add(i);
		}
		
		Collections.reverse(array);
		
		for(int i : array){
			_arrayCHNotified.remove(i);
		}
	}
	
	boolean isFindChannel(Channel ch){
		for(Channel c : _arrayCH){
			if(c._MNT.equalsIgnoreCase(ch._MNT))
				return true;
		}
		
		return false;
	}
	
	public void refreshThreadFunc(boolean showUI){

		_totalChannel	= 0;
		_totalListener	= 0;
		
		boolean favorite = false;
		Channel chFav = null;
		
		String list = getList("http://yp.ladio.net/stats/list.v2.zdat");
		
		if(!list.isEmpty()){
			_arrayCH.clear();
			
			String[] array = list.split("\n\n");
			
			for(String data : array){
				
				Channel ch = new Channel(data);
				
				if(!ch.isBlock()){
					_arrayCH.add(ch);
				}
				
				_totalChannel 	++;
				_totalListener 	+= Integer.parseInt(ch._CLN);
//				_totalFav		+= _fav.isExist(ch) ? 1 : 0;
				
				if(!favorite && ch.isFavorite()){
					favorite = true;
					chFav = ch;
				}
			}
		}

		// 通知済みお気に入りが存在しなければ、通知済みから除外
		removeNotifiedChannel();
		
		// 通知
		if(favorite)
			notifyFav(chFav, showUI);
		
//		if(showUI){
		if( isForeground() ){
		
			sort();
			
		    // run内でUIの操作をしてしまうと、例外が発生する為、Handlerにバトンタッチ
	//	    handler.sendEmptyMessage(0);
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					updateListView();
		
					// 現在の時刻を取得
					_dateUpdate = new Date();
					
					updateHeader();
				
//					if(_dlgProgress != null){
//						_dlgProgress.dismiss();
//						_dlgProgress = null;
//					}

					// 引っ張り出し有効
					_swipeRefresh.setEnabled(true);
					// 更新アニメーションを非表示
					_swipeRefresh.setRefreshing(false);

					_loading.dismiss();
				}
			});
		}
		
		// 自動更新
		if(_autoRefresh)
			startAlarm();
	}
	
	private Handler handler = new Handler();

	void rec(Channel ch){
		
		toast("録音開始");
		
//		new DownloadM3U(this).execute( ch.getPlayURL() );
		_recording.startRec(ch);
	}
	
	void recStop(Channel ch){

		toast("録音停止");
		
		_recording.stopRec(ch);
	}
	
	boolean play(Channel ch){

//        try {
////        	Uri uri = Uri.parse("http://192.168.1.9/music/test.ogg");
////        	Uri uri = Uri.parse(url);
////         	MediaPlayer player = new MediaPlayer();
//         	player.reset();
//        	player.setDataSource(url);
//         	player.setAudioStreamType(AudioManager.STREAM_MUSIC);
////         	player.prepare();
//         	player.prepareAsync();
//        	player.start();
//        } catch(Exception e) {
//            System.out.println(e.toString());
//        }
		
		if(ch.isMP3()){
			Uri uri = Uri.parse( ch.getPlayURL() );
			_player.play(this, uri, false, AudioManager.STREAM_MUSIC);
			
			_playMount = ch._MNT;
			
			_his.add(ch.getData());
			
			updateListView();
			
		    Resources r = getResources();
		    Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.stop);
		    _btnPlayerPlay.setImageBitmap(bmp);
		    _txPlayerTitle.setText( _chCurrent._NAM );
			
//			new Handler().postDelayed( delayCheckPlay, 3000 );
			
		    //
		    // 実行中：通知
		    //
		    notifyGoing(ch._NAM + " 視聴中");
		    
			return true;
		}
		
		toast("現在 mp3 再生のみサポート");
		
		return false;

	}
	
	public void resetNotifyGoing(){
		notifyGoing("『ねとりす』常駐中");
	}
	
	public void cancelNotifyGoing(){
		NotificationManager man = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		man.cancel(NOTIFICATION_ID_PLAY);
	}
	
	void notifyGoing(String msg){
// old version
//		Notification n = new Notification();
//		n.icon = R.drawable.ic_launcher;
//		n.tickerText = msg;
//		n.flags = Notification.FLAG_ONGOING_EVENT;
//
//		Intent i = new Intent(getApplicationContext(), MainActivity.class);
//		PendingIntent pend = PendingIntent.getActivity(this, 0, i, 0);
//		n.setLatestEventInfo(
//			getApplicationContext(),
//			"ねとりす",
//			msg,
//			pend);

		Notification n = new Notification.Builder(getApplicationContext())
				.setContentTitle("ねとりす")
				.setContentText(msg)
				.setTicker(msg)		// 通知到着時に通知バーに表示(4.4まで) 5.0からは表示されない
				.setSmallIcon(R.drawable.ic_launcher)
				.setOngoing(true)
				.build();

		//NotificationManagerへNotificationインスタンスを設定して発行！
		NotificationManager man = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//		man.cancel(NOTIFICATION_ID_PLAY);
		man.notify(NOTIFICATION_ID_PLAY, n);		    
	}
	
//	final Runnable delayCheckPlay = new Runnable(){
//
//		@Override
//		public void run() {
//			if(!_audioManager.isMusicActive()){
//				toast("番組が見つかりませんでした。");
//				stop();
//			}
//		}
//	};
	
	void stop(){
		_player.stop();
		
		_playMount = null;
		
		if(_pref.getBoolean(ConfigActivity.key_common_notify, true)){
			resetNotifyGoing();
		}else{
			cancelNotifyGoing();
		}
		
	    Resources r = getResources();
	    Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.play);
	    _btnPlayerPlay.setImageBitmap(bmp);
	    _txPlayerTitle.setText("");
		
	    if(_viewFlipper.getDisplayedChild() != 1)
	    	_llPlayer.setVisibility(View.GONE);
	}
	
	
	
	
	
	
    /**
     * URLのパスからSDカード直下にファイルを取得する
     *
     * @param url 取得するURL
     */
    private String getList(String url) {
        try {
            URL path = new URL(url);
        } catch (MalformedURLException ex) {
//            result_view.setText(result_view.getText() + "URLが不正です\n");
            return "";
        }
        /**
         * HTTP GETリクエスト
         */
        HttpGet httpGet = new HttpGet(url);
//        if (cb_Gzipx.isChecked()) {
//            result_view.setText(result_view.getText() + "gzip圧縮\n");
            httpGet.setHeader("Accept-Encoding", "gzip, deflate");
//        } else {
//            result_view.setText(result_view.getText() + "gzip非圧縮\n");
//        }

        /** 読み込みサイズ */
        int size = 0;
        /** 読み込みバッファ */
        byte[] w = new byte[1024];
        /** 入力ストリーム */
        InputStream in = null;
        /** 受信用ストリーム */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        /**ファイル保存用ストリーム */
        FileOutputStream writefile = null;


        /** コンテンツ取得用HTTPクライアント */
        DefaultHttpClient httpClient = new DefaultHttpClient();
        /**取得結果 */
        HttpResponse execute = null;

        try {//URLへリクエスト
            execute = httpClient.execute(httpGet);

//            switch (execute.getStatusLine().getStatusCode()) {
//                case HttpStatus.SC_OK:
//                    result_view.setText(result_view.getText() + "Status 200 OK (HTTP/1.0 - RFC 1945)\n");
//                    break;
//                case HttpStatus.SC_MOVED_PERMANENTLY:
//                    result_view.setText(result_view.getText() + "Status 301 Moved Permanently (HTTP/1.0 - RFC 1945)\n");
//                    return "";
//                case HttpStatus.SC_MOVED_TEMPORARILY:
//                    result_view.setText(result_view.getText() + "Status 302 Moved Temporarily (Sometimes Found) (HTTP/1.0 - RFC 1945)\n");
//                    return "";
//                case HttpStatus.SC_NOT_FOUND:
//                    result_view.setText(result_view.getText() + "Status 404 Not Found (HTTP/1.0 - RFC 1945)\n");
//                    return "";
//                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
//                    result_view.setText(result_view.getText() + "Status 500 Server Error (HTTP/1.0 - RFC 1945)\n");
//                    return "";
//                case HttpStatus.SC_SERVICE_UNAVAILABLE:
//                    result_view.setText(result_view.getText() + "Status 503 Service Unavailable (HTTP/1.0 - RFC 1945)\n");
//                    return "";
//                default:
//                    result_view.setText(result_view.getText() + "Status " + execute.getStatusLine().getStatusCode() + "\n");
//                    return "";
//            }

        } catch (ClientProtocolException ex) {
            Log.e(LOG_TAG, "ClientProtocolException: " + ex.getMessage());
        } catch (IOException ex) {
            Log.e(LOG_TAG, "IOException: " + ex.getMessage());
        }
        try {//HttpStatus.SC_OKの場合取得開始
            /** 取得開始時刻 */
            Long stratTime = System.currentTimeMillis();
            
            if(execute == null)
            	return "";

            //gzip転送の有無で切り替え
            if (isGZipHttpResponse(execute)) {
                in = new GZIPInputStream(execute.getEntity().getContent());
            } else {
                in = execute.getEntity().getContent();
            }

            //読み込み処理
            while (true) {
                size = in.read(w);
                if (size <= 0) {
                    break;
                }
                out.write(w, 0, size);
            }
            in.close();
            /**
             * 取得終了時刻
             */
            Long endTime = System.currentTimeMillis();

//            result_view.setText(result_view.getText() + "取得時間:" + (endTime - stratTime) + "ms\n");
//            Log.i(LOG_TAG, "取得時間:"+(endTime - stratTime) + "ms");
            
//            //ファイルに保存
//            if(checkSDCard()){
//                /** 出力ファイルパスの取得 */
//                File dir = Environment.getExternalStorageDirectory();
//                File file = null;
//                if(url.length()-1==url.lastIndexOf("/")){
//                    file=File.createTempFile("test", ".html", dir);
//                }else{
//                    file=new File(dir.getAbsolutePath()+url.substring(url.lastIndexOf("/")));
//                }
//                Log.i(LOG_TAG, url);
//                writefile = new FileOutputStream(file);
//                writefile.write(out.toByteArray());
//                writefile.flush();
//                writefile.close();
//            }
    
        } catch (IOException ex) {
            Log.e(LOG_TAG,"IOException: "+ex.getMessage());
        } catch (IllegalStateException ex) {
            Log.e(LOG_TAG,"IllegalStateException: "+ex.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
//                    writefile.close();
                } catch (IOException ex) {
                }
            }
            httpClient.getConnectionManager().shutdown();
        }

        try {
			return out.toString("Shift_JIS");
		} catch (UnsupportedEncodingException e) {
		}
        
        return "";
    }

    /**
     * gzipが有効か判定する処理
     *
     * @param response HTTPレスポンス
     * @return gzip圧縮されているかの判定
     */
    private boolean isGZipHttpResponse(HttpResponse response) {
        Header header = response.getEntity().getContentEncoding();
        if (header == null) {
            return false;
        }
        String value = header.getValue();
        return (!TextUtils.isEmpty(value) && value.contains("gzip"));
    }

    /**
     * SDカードの状態をチェック
     * @return SDカードが着込み可かどうか
     */
    private boolean checkSDCard() {
        String status = Environment.getExternalStorageState();

        if (status.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
//            Toast.makeText(MainActivity.this,
//                    "SDカードが装着されている",
//                    Toast.LENGTH_LONG).show();
            //この状態が返ってきた場合は、読み書きが可能です。
            return true;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Toast.makeText(MainActivity.this,
                    "SDカードが装着されていますが、読み取り専用・書き込み不可です",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_REMOVED)) {
            Toast.makeText(MainActivity.this,
                    "SDカードが装着されていません",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_SHARED)) {
            Toast.makeText(MainActivity.this,
                    "SDカードが装着されていますが、USBストレージとしてPCなどに"
                    + "マウント中です", Toast.LENGTH_LONG).show();
            return false;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_BAD_REMOVAL)) {
            Toast.makeText(MainActivity.this,
                    "SDカードのアンマウントをする前に、取り外しました",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_CHECKING)) {
            Toast.makeText(MainActivity.this,
                    "SDカードのチェック中です",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_NOFS)) {
            Toast.makeText(MainActivity.this,
                    "SDカードは装着されていますが、ブランクであるか、"
                    + "またはサポートされていないファイルシステムを利用しています",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_UNMOUNTABLE)) {
            Toast.makeText(MainActivity.this,
                    "SDカードは装着されていますが、マウントすることができません",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (status.equalsIgnoreCase(Environment.MEDIA_UNMOUNTED)) {
            Toast.makeText(MainActivity.this,
                    "SDカードは存在していますが、マウントすることができません",
                    Toast.LENGTH_LONG).show();
            return false;
        } else {
            Toast.makeText(MainActivity.this,
                    "その他の要因で利用不可能",
                    Toast.LENGTH_LONG).show();
            return false;
        }

    }
    
    private Handler handlerToast = new Handler();
    
    void toast(final String msg){
    	
    	// ※表示元がAlerm？だからトースト消えない対策
    	handlerToast.post( new Runnable() {

			@Override
			public void run() {
		    	Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			}
    		
    	});
    	
    }
    
    void showFavDlg(final int pos){
    	
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = inflater.inflate(R.layout.dlg_favorite, (ViewGroup)this.findViewById(R.id.layout_root_favorite));

		final EditText editMount= (EditText) layout.findViewById(R.id.editFavMount);
		final EditText editTitle= (EditText) layout.findViewById(R.id.editFavTitle);
		final EditText editDesc	= (EditText) layout.findViewById(R.id.editFavDesc);
		final EditText editGnl	= (EditText) layout.findViewById(R.id.editFavGnl);
		final EditText editDJ	= (EditText) layout.findViewById(R.id.editFavDJ);
		
		if(pos >= 0){
			Favorite fav = _fav._arrayFV.get(pos);
	    	
			editMount.setText(fav._mount);
			editTitle.setText(fav._nam);
			editDesc.setText(fav._desc);
			editGnl.setText(fav._gnl);
			editDJ.setText(fav._DJ);
		}
		
		final AlertDialog dlg = new AlertDialog.Builder(this)
		    .setIcon(R.drawable.ic_launcher)
		    .setTitle("お気に入り")
		    .setView(layout)
		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        @Override
				public void onClick(DialogInterface dialog, int whichButton) {
		        	
		        	Favorite fav = new Favorite();
		        	
		        	fav._mount	= editMount.getText().toString().trim();
		        	fav._nam	= editTitle.getText().toString();
		        	fav._desc	= editDesc.getText().toString();
		        	fav._gnl	= editGnl.getText().toString();
		        	fav._DJ		= editDJ.getText().toString();
		        	
		        	if(pos >= 0){
		        		_fav.update(pos, fav);
		        	}else{
		        		_fav.add(fav);
		        	}
		        	
		        	updateFavListview();
		        }
		    })
		    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        @Override
				public void onClick(DialogInterface dialog, int whichButton) {
		        }
		    }).create();
		
		dlg.show();
    }

    void showBlockDlg(final int pos){
    	
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View layout = inflater.inflate(R.layout.dlg_favorite, (ViewGroup)this.findViewById(R.id.layout_root_favorite));

		final EditText editMount	= (EditText) layout.findViewById(R.id.editFavMount);
		final EditText editTitle	= (EditText) layout.findViewById(R.id.editFavTitle);
		final EditText editDesc	= (EditText) layout.findViewById(R.id.editFavDesc);
		final EditText editGnl		= (EditText) layout.findViewById(R.id.editFavGnl);
		final EditText editDJ		= (EditText) layout.findViewById(R.id.editFavDJ);
		
		if(pos >= 0){
			Block block = _block._arrayBlock.get(pos);
	    	
			editMount.setText(block._mount);
			editTitle.setText(block._nam);
			editDesc.setText(block._desc);
			editGnl.setText(block._gnl);
			editDJ.setText(block._DJ);
		}
		
		final AlertDialog dlg = new AlertDialog.Builder(this)
		    .setIcon(R.drawable.ic_launcher)
		    .setTitle("ブロック")
		    .setView(layout)
		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        @Override
				public void onClick(DialogInterface dialog, int whichButton) {
		        	
		        	Block block = new Block();
		        	
		        	block._mount	= editMount.getText().toString().trim();
		        	block._nam	= editTitle.getText().toString();
		        	block._desc	= editDesc.getText().toString();
		        	block._gnl	= editGnl.getText().toString();
		        	block._DJ		= editDJ.getText().toString();
		        	
		        	if(pos >= 0){
		        		_block._arrayBlock.set(pos, block);
		        	}else{
		        		_block._arrayBlock.add(block);
		        	}
		        	
		        	updateBlockListView();
		        }
		    })
		    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        @Override
				public void onClick(DialogInterface dialog, int whichButton) {
		        }
		    }).create();
		
		dlg.show();
    }

	//
	// 設定画面から戻ったらここ
	//
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == REQUEST_CODE_CONFIG){
			load();
		}
	}	
	
	// 検索用 Activity から呼び出されたとき   
	@Override  
	protected void onNewIntent(Intent intent) {  
//	    doSearchWithIntent(intent);
		
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	_searchText = intent.getStringExtra(SearchManager.QUERY);
//            mText.setText(Html.fromHtml(mText.getText().toString().replaceAll(
//                    "(" + query + ")", "<font color=\"red\">$1</font>")));

            // queryの保存
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SuggestionsProvider.AUTHORITY, SuggestionsProvider.MODE);
            suggestions.saveRecentQuery(_searchText, null);
            
            searchChannel();
    		updateSearchHead();
            updateListView();
        }		
	}
	
	void searchChannel(){
		String upper = _searchText.toUpperCase();
		
		_arrayCHSearch.clear();
		
		for(Channel ch : _arrayCH){
			if(ch._NAM.toUpperCase().indexOf(upper) >= 0){
				_arrayCHSearch.add(ch);
				continue;
			}
			if(ch._DESC.toUpperCase().indexOf(upper) >= 0){
				_arrayCHSearch.add(ch);
				continue;
			}
			if(ch._GNL.toUpperCase().indexOf(upper) >= 0){
				_arrayCHSearch.add(ch);
				continue;
			}
			if(ch._DJ.toUpperCase().indexOf(upper) >= 0){
				_arrayCHSearch.add(ch);
				continue;
			}
		}
	}
	
//	
//	private void doSearchWithIntent(final Intent queryIntent) {  
//	    // 検索文字列は SearchManager.QUERY というキーに入っている  
//	    final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);  
//	    doSearchWithQuery(queryString);  
//	}
//	
//	private void doSearchWithQuery(String query){
////		TextView v = (TextView)findViewById(R.id.textvi)
//	}
	
	boolean isForeground(){
//    	Log.d("unko", "isForeground");
    	
	    ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> processInfoList = am.getRunningAppProcesses();
	    for( RunningAppProcessInfo info : processInfoList){
	        if(info.processName.equals(getPackageName())){
	            if( info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
	            	
//	            	Log.d("unko", "foreground !");
	            	
	            	return true;
	            }
	        }
	    }
	    
	    return false;
	}
	
	void startAlarm(){
		
//		Log.d("unko", "startAlarm");
		
		// ReceivedActivityを呼び出すインテントを作成
		Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);
		
		// ブロードキャストを投げるPendingIntentの作成
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, i, 0);
		 
		// 現在時刻よりhoge分後
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());	// 現在時刻
		cal.add(Calendar.MINUTE, _autoRefreshMin);			// hoge分後
//		cal.add(Calendar.SECOND, 10);
		
		// タイプ
		int type = _autoRefreshWakeup ? 
				AlarmManager.RTC_WAKEUP :	// スリープなら復帰 
				AlarmManager.RTC;
		
		// 古いアラーム削除 -> アラーム設定
		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
		am.set(type, cal.getTimeInMillis(), sender);
	}
	
	void cancelAlarm(){
		// ReceivedActivityを呼び出すインテントを作成
		Intent i = new Intent(getApplicationContext(), AlarmReceiver.class);

		// ブロードキャストを投げるPendingIntentの作成
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, i, 0);

		// 古いアラーム削除
		AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		am.cancel(sender);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Channel
	//
//	public void onClickRefresh(View view){
//		refresh();
//	}
	
	public void onClickFavorite(View view){
		updateFavListview();
//カスタムだから効かないかも
//		_listviewFav.setItemsCanFocus(false);
//		_listviewFav.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);  
		
		flip(2, _animRightIn, _animLeftOut);
	}
	
	public void onClickReclist(View view){
		updateRecListView();
		
		flip(5, _animRightIn, _animLeftOut);
	}
	
	public void onClickBlocklist(View view){
		updateBlockListView();
		
		flip(4, _animRightIn, _animLeftOut);
	}
	
	public void onClickHistory(View view){
		updateHisListview();
		
		flip(3, _animRightIn, _animLeftOut);
	}
	
//	public void onClickSort(View view){
//		_sortDsc = !_sortDsc;
//		sort();
//		updateSortButton();
//		updateListView();
//	}

	
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Detail
	//
	public void onClickDetailFavorite(View view){
		if(_chCurrent != null){
			if(_chCurrent.isFavorite()){
				_fav.delete(_chCurrent._MNT);
				
				toast("お気に入りから削除しました。");
			}else{
				_fav.add(_chCurrent);
				
				toast("お気に入りに追加しました。");
			}
			
			updateDetailFavIcon();
		}
	}

	public void onClickDetailRec(View view){
		
		if(_recording.isExist(_chCurrent))
			recStop( _chCurrent );
		else
			rec( _chCurrent );
		
		updateDetailRecIcon();
	}

	public void onClickDetailBlock(View view){
		new AlertDialog.Builder(this)
	    .setIcon(R.drawable.ic_launcher)
		.setTitle("番組をブロックリストに追加しますか？")
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	_block.add( _chCurrent );
            	
				sort();
				updateListView();
				updateHeader();

            	flipBack();
            }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        })
		.show();
	}

	public void onClickDetailShare(View view){
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, _chCurrent.getShare());
            startActivity(intent);
        } catch (Exception e) {
        }
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	//
	// Favorite
	//
	public void onClickFavAdd(View view){
		showFavDlg(-1);
	}
	
	public void onClickFavDelete(View view){
		showFavDelete();
	}
	
	public void onClickFavSelectAll(View view){
		for(int i = 0; i < _listviewFav.getChildCount(); i++){
			View v = _listviewFav.getChildAt(i);
			CheckBox chk = (CheckBox)v.findViewById(R.id.checkFavSelect);
			chk.setChecked(true);
		}
	}
	

	
	///////////////////////////////////////////////////////////////////////////
	//
	// Blocklist
	//
	public void onClickBlockAdd(View view){
		showBlockDlg(-1);
	}

	public void onClickBlockSelectAll(View view){
		for(int i = 0; i < _listviewBlock.getChildCount(); i++){
			View v = _listviewBlock.getChildAt(i);
			CheckBox chk = (CheckBox)v.findViewById(R.id.checkBlockSelect);
			chk.setChecked(true);
		}
	}
	
	public void onClickBlockDelete(View view){
		showBlockDelete();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	//
	// History
	//
	public void onClickHistoryDelete(View view){
		showHisDelete();
	}
	
	public void onClickHistorySelectAll(View view){
		for(int i = 0; i < _listviewHis.getChildCount(); i++){
			View v = _listviewHis.getChildAt(i);
			CheckBox chk = (CheckBox)v.findViewById(R.id.checkHisSelect);
			chk.setChecked(true);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// Reclist
	//
	public void onClickReclistDelete(View view){
		showRecDelete();
	}

	public void onClickReclistSelectAll(View view){
		for(int i = 0; i < _listviewRec.getChildCount(); i++){
			View v = _listviewRec.getChildAt(i);
			CheckBox chk = (CheckBox)v.findViewById(R.id.checkReclistSelect);
			chk.setChecked(true);
		}
	}

// 今は使ってない	
//	void save(){
//		
//		Editor e = _pref.edit();
//		
//		e.putString(ConfigActivity.key_sort, String.valueOf(_sortColumnIndex));
//		e.putBoolean(ConfigActivity.key_favtop,	_favoriteTop);
//		e.putBoolean(ConfigActivity.key_auto_refresh, _autoRefresh);
//		e.putString(ConfigActivity.key_auto_refresh_min, String.valueOf(_autoRefreshMin));
//		e.putBoolean(ConfigActivity.key_auto_refresh_wakeup, _autoRefreshWakeup);
//		e.putBoolean(ConfigActivity.key_notify_fav, _notifyFav);
//		e.putString(ConfigActivity.key_notify_fav_music, _notifyFavMusic);
//		e.putBoolean(ConfigActivity.key_notify_fav_vibe, _notifyFavVibe);
//		e.putBoolean(ConfigActivity.key_notify_fav_lamp, _notifyFavLamp);
//		
//		e.commit();
//	}
	
	void load(){
		_sortColumnIndex= Integer.parseInt( _pref.getString(ConfigActivity.key_sort, "0") );
		_favoriteTop 	= _pref.getBoolean(ConfigActivity.key_favtop, true);
		_autoRefresh 	= _pref.getBoolean(ConfigActivity.key_auto_refresh, false);
		_autoRefreshMin = Integer.parseInt( _pref.getString(ConfigActivity.key_auto_refresh_min, "1") );
		_autoRefreshWakeup= _pref.getBoolean(ConfigActivity.key_auto_refresh_wakeup, false);
		_notifyFav		= _pref.getBoolean(ConfigActivity.key_notify_fav, true);
		_notifyFavMusic	= _pref.getString(ConfigActivity.key_notify_fav_music, "なし");
		_notifyFavVibe	= _pref.getBoolean(ConfigActivity.key_notify_fav_vibe, true);
		_notifyFavLamp	= _pref.getBoolean(ConfigActivity.key_notify_fav_lamp, true);
		
		if(_autoRefresh)
			cancelAlarm();
		
		if(_pref.getBoolean(ConfigActivity.key_common_notify, true))
			resetNotifyGoing();
		else
			cancelNotifyGoing();
	}
	
	Timer	_timer = null;	//new Timer(true);	// デーモンスレッドにする。プログラム終了時にスレッド終了待たない
	Handler handlerSleep = new Handler();
	
	class SleepTimerTask extends TimerTask{

		@Override
		public void run() {
			
			handlerSleep.post(new Runnable() {
				
				@Override
				public void run() {
					
					_sleepSec --;
					
					updateHeader();
					
					if(_sleepSec <= 0){
						stopSleepTimer();
						stop();
						screenOff();
					}
				}
			});
		}
		
	}
}
