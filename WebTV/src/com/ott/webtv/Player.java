package com.ott.webtv;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.ethernet.EthernetManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public  class Player extends Activity{
	private MediaPlayer mPlayer;
	private Context context;
	private Handler handler;
	private SurfaceHolder surfaceholder;
	protected PopDialog pop_dialog;
    private IntentFilter mFilter;
    private BroadcastReceiver mEthStateReceiver;
    private ConnectivityManager connMgr;
    private boolean bDisConnect = false;
	protected enum Command{
		CMD_PLAYER_SUFACE_CREATED,CMD_PLAYER_PREPARED,CMD_PLAYER_ERROR,CMD_PLAYER_COMPLETE,CMD_PLAYER_SUFACE_DESTORYED,
		CMD_PLAYER_BUFFER_START,CMD_PLAYER_BUFFER_END,CMD_PALYER_NETWORK_CONNECT_FAIL
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		bDisConnect = false;
        mFilter = new IntentFilter();
        mFilter.addAction(EthernetManager.ETHERNET_STATE_CHANGED_ACTION);
        mFilter.addAction(EthernetManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        
        
         connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       
        mEthStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(context, intent);
            }
        };
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		registerReceiver(mEthStateReceiver, mFilter);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unregisterReceiver(mEthStateReceiver);
	}
	
	private void handleEvent(Context context, Intent intent) {
		String action = intent.getAction();
		// final android.net.NetworkInfo ethernet = connMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		NetworkInfo ethernet = connMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
		NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		System.out.println("###### network action = " + action);
		System.out.println("###### network ethernet status = " + ethernet);
		System.out.println("###### network wifi status= " + wifi);
//		if (EthernetManager.ETHERNET_STATE_CHANGED_ACTION.equals(action) || WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
		if(!wifi.isAvailable() && !ethernet.isAvailable()){
				if(!bDisConnect){
					System.out.println("------exit player----");
					pop_dialog.showToast(R.string.exitPlayer);
					handler.postDelayed(quitPlayer, 3000);
					bDisConnect = true;
				}
		}else{
			bDisConnect = false;
		}
//		}
	}
	
	 Runnable quitPlayer = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			 sendHandlerMsg(Command.CMD_PALYER_NETWORK_CONNECT_FAIL);
		}
	};
	 
	@SuppressWarnings("deprecation")
	public void CreatePlayer(Context context,Handler handler,SurfaceView surfaceview){
		this.context = context;
		this.handler = handler;
		surfaceholder = surfaceview.getHolder();
		surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceholder.addCallback(surfaceholdCallBack);
	}
		
	protected void initPlayer(String url){
		Uri newVideoUri;
		pop_dialog.showAnimation();
		newVideoUri = Uri.parse("mcp://" + url);
		System.out.println("-------- the playURI = "+newVideoUri);
		mPlayer = new MediaPlayer();
		playerListener pL = new playerListener();
		try {
			mPlayer.setDataSource(context, newVideoUri);
			mPlayer.setDisplay(surfaceholder);
			mPlayer.prepareAsync();
			
			mPlayer.setOnBufferingUpdateListener(pL);
			mPlayer.setOnErrorListener(pL);
			mPlayer.setOnCompletionListener(pL);
			mPlayer.setOnPreparedListener(pL);
			mPlayer.setOnSeekCompleteListener(pL);
			mPlayer.setOnInfoListener(pL);
			mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void pausePlayer(){
		if(mPlayer != null)
			mPlayer.pause();
	}
	
	protected void startPlayer(){
		pop_dialog.closeAnimation();
		if(mPlayer != null)
			mPlayer.start();
	}
	
	protected void stopPlayer(){
		System.out.println("------stop player ------");
		pop_dialog.closeAnimation();
		if(mPlayer != null){
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	protected int getTotalTime(){
		
		if(mPlayer != null){
			
			return mPlayer.getDuration();
		}
		return 0;
	}
	
	protected int getCurrentTime(){
		
		if(mPlayer != null){
			
			return mPlayer.getCurrentPosition();
		}
		return 0;
	}
	protected void setSeekTime(int position){
		pop_dialog.showAnimation();
		if(mPlayer != null){
			mPlayer.seekTo(position);
		}
	}
	
	protected boolean isPlaying(){
		if(mPlayer != null){
			return mPlayer.isPlaying();
		}
		return false;
	}
	
	private void sendHandlerMsg(Command type){
		System.out.println("____Play SendCmd "+type);
		Message msg = handler.obtainMessage(type.ordinal());
		msg.sendToTarget();
	}
	
	Callback surfaceholdCallBack = new Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			sendHandlerMsg(Command.CMD_PLAYER_SUFACE_DESTORYED);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
//			startPlayer();
			sendHandlerMsg(Command.CMD_PLAYER_SUFACE_CREATED);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
		}
	};
	
	
	private class playerListener implements OnBufferingUpdateListener, OnErrorListener, OnInfoListener, OnSeekCompleteListener, OnPreparedListener, OnCompletionListener{

		@Override
		public void onCompletion(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			System.out.println("------ the player is onCompletion----------");
			sendHandlerMsg(Command.CMD_PLAYER_COMPLETE);
		}

		@Override
		public void onPrepared(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			pop_dialog.closeAnimation();
			if (mPlayer.getVideoHeight() == 0 || mPlayer.getVideoWidth() == 0) {
				System.out.println("------ the h+w = 0 -------");
				sendHandlerMsg(Command.CMD_PLAYER_ERROR);
				return;
			}
			sendHandlerMsg(Command.CMD_PLAYER_PREPARED);
		}

		@Override
		public void onSeekComplete(MediaPlayer arg0) {
			// TODO Auto-generated method stub
			pop_dialog.closeAnimation();
		}

		@Override
		public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			if(arg1 == MediaPlayer.MEDIA_INFO_BUFFERING_START){
				sendHandlerMsg(Command.CMD_PLAYER_BUFFER_START);
			}else if(arg1 == MediaPlayer.MEDIA_INFO_BUFFERING_END){
				sendHandlerMsg(Command.CMD_PLAYER_BUFFER_END);
			}
			return false;
		}

		@Override
		public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			System.out.println("------ the player is error----------");
			pop_dialog.showAnimation();
			sendHandlerMsg(Command.CMD_PLAYER_ERROR);
			return true;
		}

		@Override
		public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
			// TODO Auto-generated method stub
		}
	}
	
	SubtitleAudio mSubAudObj = null;
	public void CreateSubAudioObject(){
		mSubAudObj = new SubtitleAudio();
	}
	
	public int getSubSelectIndex(){
		
		int index = mSubAudObj.getIntParameter(SubtitleAudio.INVOKE_ID_GET_SUB_SELECT) ;
		return index;
	}
	
	public int getAudioSelectIndex(){
		
		int index = mSubAudObj.getIntParameter(SubtitleAudio.INVOKE_ID_GET_AUDIO_SELECT) ;
		return index;
	}
	
	public void setSubtitleIndex(int index){
		mSubAudObj.setParameter(SubtitleAudio.INVOKE_ID_SET_SUB_TRACK_SELECT, index);
	}
	
	public void setAudioIndex(int index){
		mSubAudObj.setParameter(SubtitleAudio.INVOKE_ID_SET_AUDIO_TRACK_SELECT, index);
	}
	
	public String[] getSubtitleInfo(){
		return mSubAudObj.subtitleGetInfo();
	}
	
	public String[] getAudioInfo(){
		return mSubAudObj.AudioGetInfo();
	}
	
class SubtitleAudio{
		
	    private int subtitleCount;
	    private int audioTrackCount;
	    private static final int INVOKE_ID_SET_SUB_TRACK_SELECT = 4000;
	    private static final int INVOKE_ID_GET_SUB_SELECT = 4103;
		private static final int INVOKE_ID_GET_SUB_TRACK_INFO_TOTAL = 4102;
		private static final int INVOKE_ID_SET_AUDIO_TRACK_SELECT = 3001;
		private static final int INVOKE_ID_GET_AUDIO_TRACK_INFO_TOTAL = 3102;
		private static final int INVOKE_ID_GET_AUDIO_SELECT = 3104;

		public boolean setParameter(int key,int value){
			Parcel request = (mPlayer).newRequest();
			request.writeInt(key);
			request.writeInt(value);
			Parcel reply = Parcel.obtain();
			mPlayer.invoke(request, reply);
			reply.recycle();
			return true;
		}
		
		public int getIntParameter(int key) {
			int value;
			Parcel request = mPlayer.newRequest();
			request.writeInt(key);
			Parcel reply = Parcel.obtain();
			mPlayer.invoke(request, reply);
			value = reply.readInt();
			//reply.readInt();
			return value;
		}
		
		public Parcel getParcelParameter(int key) {
			Parcel request = mPlayer.newRequest();
			request.writeInt(key);
			Parcel reply = Parcel.obtain();
			mPlayer.invoke(request, reply);
			//reply.recycle();
			return reply;
		}
		
		@SuppressLint("Recycle")
		public String[] subtitleGetInfo(){
			String[] subtitleInfo;
			Parcel parcel = Parcel.obtain();
			parcel = getParcelParameter(INVOKE_ID_GET_SUB_TRACK_INFO_TOTAL);
			subtitleCount = parcel.readInt();   
			System.out.println("------- the subtitleCount is ------"+subtitleCount);
			
			if(subtitleCount == 0){
				subtitleInfo = new String[1];
				subtitleInfo[0] = (getResources().getString(R.string.none));
				return subtitleInfo;
			}
			subtitleInfo = new String[subtitleCount];
			for(int i = 0;i < subtitleCount; i++){
				subtitleInfo[i] = parcel.readString();
			}
			return subtitleInfo;
		}
		
		@SuppressLint("Recycle")
		public String[] AudioGetInfo(){
			String[] audioInfo;
			Parcel parcel = Parcel.obtain();
			parcel = getParcelParameter(INVOKE_ID_GET_AUDIO_TRACK_INFO_TOTAL);
			audioTrackCount = parcel.readInt();   
			System.out.println("------- the audioCount is ------"+audioTrackCount);
			
			if(audioTrackCount == 0){
				audioInfo = new String[1];
				audioInfo[0] = (getResources().getString(R.string.none));
				return audioInfo;
			}		
			audioInfo = new String[audioTrackCount];
			for(int i = 0;i < audioTrackCount; i++){
				audioInfo[i] = parcel.readString();
			}
			return audioInfo;
		}
	}
}