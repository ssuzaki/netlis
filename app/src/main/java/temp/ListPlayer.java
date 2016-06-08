package temp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import jp.codepanic.netlis.MainActivity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

public class ListPlayer implements Runnable {
	private String [] uris;
	private TextView tv;
	private MainActivity activity;
	private MediaPlayer player;
	private boolean keepPlaying = true;
	
	public ListPlayer(MainActivity activity, String[] uris) {
		this.uris = uris;
		this.activity = activity;
		activity.listPlayer = this;
	}
	
	public void run() {
//Log.d("unko", "ListPlayer run");
    	for (String uri:uris) {
    		if (!keepPlaying) {
    			break;
    		}
    		System.out.println("About to play " + uri);
    		play1(uri);
    		synchronized(this) {
    			try {	
    				this.wait();
    			} catch (Exception e) {
    				System.out.println("play failed " + e.toString());
    			}
    		}
    	}
	}
	
	private void play1(String uriStr) {
//		Log.d("unko", "ListPlayer play1");
//		setTrackInfo(uriStr);
        try {   
        	// Try the URL directly (ok for Android 3.0 upwards)
        	tryMediaPlayer(uriStr);
        } catch(Exception e) {
			// Try downloading the file and then playing it - needed for Android 2.2
			try {
				downloadToLocalFile(uriStr, "audiofile.mp3");
	        	File localFile = activity.getFileStreamPath("audiofile.mp3");
	        	tryMediaPlayer(localFile.getAbsolutePath());
			} catch(Exception e2) {
				System.out.println("File error " + e2.toString());
			}
        }
	}
	
	private void downloadToLocalFile(String uriStr, String filename) throws Exception {
//Log.d("unko", "ListPlayer downloadToLocalFile");
		URL url = new URL(Uri.encode(uriStr, ":/"));
		BufferedInputStream reader = 
				new BufferedInputStream(url.openStream());
		
    	File f = new File("audiofile.mp3");
		FileOutputStream fOut = activity.openFileOutput("audiofile.mp3",
                Context.MODE_WORLD_READABLE);
		BufferedOutputStream writer = new BufferedOutputStream(fOut); 
		
		byte[] buff = new byte[1024]; 
		int nread;
		System.out.println("Downloading");
		while ((nread = reader.read(buff, 0, 1024)) != -1) {
			writer.write(buff, 0, nread);
		}
		writer.close();
	}
	
	private void tryMediaPlayer(String uriStr) throws Exception {
       	player = new MediaPlayer();
    	player.setOnCompletionListener(new OnCompletionListener() {
    		public void onCompletion(MediaPlayer player) {
    			synchronized(ListPlayer.this) {
    				ListPlayer.this.notifyAll();
    			}
    		}
    	});
    	player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    	player.setDataSource(uriStr);
    	player.prepare();
    	player.start();		
	}
	
	public void stopCurrentTrack() {
		player.stop();
		synchronized(this) {
			notifyAll();
		}
	}
	
	public void stopPlaying() {
		keepPlaying = false;
		player.stop();
		synchronized(this) {
			notifyAll();
		}
	}
	
//	public void setTrackInfo(String uri) {
//		String[] parts = uri.split("/");
//		String trackNameExt = parts[parts.length-1];
//		final String trackName = trackNameExt.split("[.]")[0];
//		final String artist = parts[parts.length-2];
//		System.out.println("Track info " + trackName + " plus " + artist);
//			
//		// download cover if possible - I keep them as cover.jpg in the CD's directory
//		// Rebuild path with last part changed
//		parts[parts.length-1] = "cover.jpg";
//		StringBuffer result = new StringBuffer();
//		if (parts.length > 0) {
//			result.append(parts[0]);
//			for (int i = 1; i < parts.length; i++) {
//				result.append("/");
//				result.append(parts[i]);
//			 }	
//		} 
//		
//		// Download url and either get a valid image or null
//		ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
//		int count = 0;
//		try {
//			URL url = new URL(Uri.encode(result.toString(), ":/"));
//			System.out.println("Opening stream " + url.toString());
//			BufferedInputStream reader = new BufferedInputStream(url.openStream());
//			byte[] buff = new byte[1024]; 
//			int nread;
//
//			while ((nread = reader.read(buff, 0, 1024)) != -1) {
//				imageStream.write(buff, 0, nread);
//				count += nread;
//			}
//		} catch(Exception e) {
//			System.out.println("Image failed " + e.toString());
//		}
//		// try to decode - get valid image or nulll
//		final Bitmap image = BitmapFactory.decodeByteArray(imageStream.toByteArray(), 0, count);
//
//		 // Set info in GUI - run in GUI thread
//		 new Thread(new Runnable() {
//			 public void run() {
//				 activity.root.post(new Runnable() {
//					 public void run() {
//						 activity.setTrackInfo(artist, trackName, image);
//					 }
//				 });
//			 }
//		 }).start();
//	}
}
