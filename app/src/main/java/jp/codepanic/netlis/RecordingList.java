package jp.codepanic.netlis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.format.DateFormat;

public class RecordingList {

	
	ArrayList<DownloadTask> _array = new ArrayList<DownloadTask>();
	
	public boolean isExist(Channel ch){
		for(DownloadTask task : _array){
			if( task._ch._MNT.equalsIgnoreCase(ch._MNT) ){
				return true;
			}
		}
		
		return false;
	}
	
	public void startRec(Channel ch){
		
		// ※コンストラクタでch渡すタイミングがないと直ぐに実行されないから
		//   あとでch参照してヌルポで落ちる対応
		DownloadTask task = new DownloadTask(ch);
		task.execute(ch);
		
		_array.add(task);
	}
	
	public void stopRec(Channel ch){
 
		for(DownloadTask task : _array){
			if( task._ch._MNT.equalsIgnoreCase(ch._MNT) ){
				task.cancel(false);
				
				_array.remove(task);
				
				break;
			}
		}
	}
	
	public void stopRecAll(){
		for(DownloadTask task : _array){
			task.cancel(false);
		}
		
		_array.clear();
	}
	
	
	public class DownloadTask extends AsyncTask<Channel, Integer, File> {

		public Channel _ch;
		
		DownloadTask(Channel ch){
			_ch = ch;
		}
		
		protected File doInBackground(Channel... params) {
			
//			_ch = params[0];
			String url = _ch.getPlayURL();
			
	        int responseCode = 0;
	        final int BUFFER_SIZE = 10240;
	        File file = null;
	        String filePath = "";
	        String fileName = "";

	        try {
	                URI uri = new URI(url);
//	              fileName = (new File(uri.getPath())).getName();
	                fileName = _ch._NAM + DateFormat.format(" - yyyyMMdd kkmmss", Calendar.getInstance()) + ".mp3";

	                HttpClient httpClient = new DefaultHttpClient();
	                HttpGet httpGet = new HttpGet(uri);
	                HttpResponse httpResponse = null;

	                httpClient.getParams().setParameter("http.connection.timeout", new Integer(15000));
	                httpResponse = httpClient.execute(httpGet);
	                responseCode = httpResponse.getStatusLine().getStatusCode();

	                // ファイルサイズを取得して、プログレスダイアログにセット
//	                mProgressDialog.setMax((int) httpResponse.getEntity().getContentLength());

	                if (responseCode == HttpStatus.SC_OK) {
	                        filePath = Environment.getExternalStorageDirectory().getPath() + "/netlis";
	                        new File(filePath).mkdir();

	                        file = new File(filePath, fileName);
	                        
	                        //
	                        // 既に存在する？ならリネーム
	                        //
//	                        int index = 2;
//	                        while(file.exists()){
//	                        	fileName = _ch._NAM + " - " + String.valueOf(index) + ".mp3";
//	                        	index ++;
//	                        }

	                        InputStream inputStream = httpResponse.getEntity().getContent();
	                        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
	                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file, false), BUFFER_SIZE);

	                        byte buffer[] = new byte[BUFFER_SIZE];
	                        int size = 0;
	                        while(-1 != (size = bufferedInputStream.read(buffer))) {
	                        	
	                        	if(isCancelled()){
	                        		return file;
	                        	}
	                        	
	                            bufferedOutputStream.write(buffer, 0, size);
	                                
	                            // onProgressUpdateを呼び出す
//	                          publishProgress(size);
	                        }

	                        bufferedOutputStream.flush();

	                        bufferedOutputStream.close();
	                        bufferedInputStream.close();

	                }
	                else if (responseCode == HttpStatus.SC_NOT_FOUND) {
	                }
	                else if (responseCode == HttpStatus.SC_REQUEST_TIMEOUT) {
	                }
	                else {
	                }
	        } catch (URISyntaxException e) {
	                e.printStackTrace();
	        } catch (ClientProtocolException e) {
	                e.printStackTrace();
	        } catch (IOException e) {
	                e.printStackTrace();
	        }

	        // 録音中リストから除外
	        _array.remove(this);
	        
	        return file;
		}
		
		

	}	
	
	
}
