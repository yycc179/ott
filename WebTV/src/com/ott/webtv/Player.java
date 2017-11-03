package com.ott.webtv;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public  class Player extends Activity{
	private MediaPlayer mPlayer;
	private Context context;
	private Handler handler;
	private SurfaceHolder surfaceholder;
	protected PopDialog pop_dialog;
	
	protected enum Command{
		CMD_PLAYER_SUFACE_CREATED,CMD_PLAYER_PREPARED,CMD_PLAYER_ERROR,CMD_PLAYER_COMPLETE,CMD_PLAYER_SUFACE_DESTORYED
	}
	
	@SuppressWarnings("deprecation")
	public void initPlayer(Context context,Handler handler,SurfaceView surfaceview){
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
		Message msg = handler.obtainMessage(type.ordinal());
		msg.sendToTarget();
	}
	
	Callback surfaceholdCallBack = new Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			System.out.println("------ the surface end----------");
			sendHandlerMsg(Command.CMD_PLAYER_SUFACE_DESTORYED);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			System.out.println("------ the surface created----------");
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
	
	
}