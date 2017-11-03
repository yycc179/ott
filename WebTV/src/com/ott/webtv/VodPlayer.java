package com.ott.webtv;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ott.webtv.SelectedVideoData.VideoPlayInfo;
import com.ott.webtv.core.ContentManager;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.StoreManager;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.RESLOUTION;
import com.ott.webtv.core.DataNode.Serial;
import com.ott.webtv.core.DataNode.Video;
import com.ott.webtv.R;

@SuppressLint("NewApi")
public class VodPlayer extends Player implements OnClickListener {

	private View mLayoutPlayInfo, mLayoutPlayStatus;
	private ImageView mPlayStatus;
	private SeekBar mPlayBar;
	private Button mResoulte, mAudio, mSubtitle, mSeek;
	private TextView mPlayTime;
	private static SurfaceView surfaceview;
	private int mCurrentPosition = 0;
	private int mDuration = 0;
	private String sTotaltime;
	private TextView mfilePath;
	private boolean isSerialVideo = true;
	private boolean showPlayStatus = true;
	private boolean isPlayerPrepare = false;
	private Serial mSerialInfo;
	private Video mVideoInfo;
	private String playUrl = "";
	private int mCurFragmentSelectItem = 0;
	private ArrayList<HashMap<String, String>> mRelateVideoList;
	private Content mCurrentContent;
	private final String TAG = "PlayerActivity";

	private StoreManager<Content> historyManager = StoreManager.getHisManager();

	private VideoPlayInfo videoInfo = SelectedVideoData.VideoPlayInfo.getInstace();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "---- onCreate --------");
		setContentView(R.layout.player);

		CoreHandler.getInstace().setHandler(handler);

		getIntentData();
		findViews();
		pop_dialog = new PopDialog(VodPlayer.this, null);
		mResoulte.setText(videoInfo.getResoulteList()[videoInfo.getCurrentResoutleIndex()]);
		mfilePath.setText(SelectedVideoData.VideoPlayInfo.getInstace().getVideoName());
		
		pop_dialog.showAnimation();
		initPlayer(VodPlayer.this,Playerhandler, surfaceview);
	}

	
	@SuppressWarnings("unchecked")
	private void getIntentData(){
		isSerialVideo = getIntent().getBooleanExtra("videoType", true);
		mRelateVideoList = (ArrayList<HashMap<String, String>>) getIntent()
				.getSerializableExtra("curPageList");
		if (isSerialVideo == true) {
			mSerialInfo = (Serial) getIntent().getSerializableExtra("videoContent");
			mSerialInfo = (Serial) historyManager.getInCache(mSerialInfo);
			mCurrentPosition = mSerialInfo.getLastPlayTime(mSerialInfo.getLastPlayIndex()) * 1000;
		} else {
			mVideoInfo = (Video) getIntent().getSerializableExtra("videoContent");
			mVideoInfo = (Video) historyManager.getInCache(mVideoInfo);
			mCurrentPosition = mVideoInfo.getLastPlayTime() * 1000;
		}
		playUrl = videoInfo.getCurrentVideoUrl().get(videoInfo.getCurrentResoutleIndex());
		mCurFragmentSelectItem = getIntent().getIntExtra("currentPlayIndex", 30);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("---- onResume --------isPlayerPrepare ="+isPlayerPrepare);
	    if(isPlayerPrepare){
	           Log.d(TAG,"onResume,player start");
	           if (currentTimeToSave == 0) {
	                try {
	                    /*play start*/
	                	 initPlayer(playUrl);						
	                } catch (Exception e) {
	                    // TODO: handle exception
	                }
	            } else {
	                /*play start*/
	                Log.d(TAG," position==="+currentTimeToSave);
//	                initPlayer(playUrl);
	                startPlayer();
	            }
	        }  
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
			
		System.out.println("---- onPause --------");
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		stopPlayAndSaveVideoinfo();
		System.out.println("---- onStop --------");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		pop_dialog.closeAnimation();
		mCurrentPosition = 0;	
		handler.removeCallbacksAndMessages(null);
		Playerhandler.removeCallbacksAndMessages(null);
	}

	private int currentTimeToSave = 0;
	
	private void saveVideoPlayTime() {
		try {
			Content data = null;
			if (isSerialVideo) {
				data = mSerialInfo;
				mSerialInfo.setLastPlayTime(mCurFragmentSelectItem,currentTimeToSave / 1000);
			} else {
				data = mVideoInfo;
				mVideoInfo.setLastPlayTime(currentTimeToSave / 1000);
			}
			
			if(historyManager.isExist(data)){
				historyManager.remove(data);
			}
			historyManager.add(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stopPlayAndSaveVideoinfo() { 
		stopPlayer();
		saveVideoPlayTime();
	}

	private void backToDetailActivity(){
		if(mLayoutPlayStatus.getVisibility() == View.VISIBLE){
//			mLayoutPlayStatus.setVisibility(View.GONE);
			
			closePlayStatus();
		}
		if(mLayoutPlayInfo.getVisibility() == View.VISIBLE){
//			mLayoutPlayInfo.setVisibility(View.GONE);
			closePlayInfo();
		}
		Intent intent = new Intent();
		intent.putExtra("lastPlayIndex", mCurFragmentSelectItem);
		setResult(0, intent);
		handler.removeCallbacksAndMessages(null);
		Playerhandler.removeCallbacksAndMessages(null);
		finish();
	}


	private void findViews() {
		surfaceview = (SurfaceView) findViewById(R.id.movieView);
		mLayoutPlayInfo = findViewById(R.id.play_info);
		mLayoutPlayStatus = findViewById(R.id.Play_status);
		mPlayStatus = (ImageView) findViewById(R.id.playstatus_img);
		mPlayBar = (SeekBar) findViewById(R.id.play_progessbar);
		mPlayTime = (TextView) findViewById(R.id.play_time);
		mResoulte = (Button) findViewById(R.id.play_resoulte);

		mAudio = (Button) findViewById(R.id.play_audio);
		mSubtitle = (Button) findViewById(R.id.play_subtitle);
		mSeek = (Button) findViewById(R.id.play_seek);
		mfilePath = (TextView) findViewById(R.id.filmName);

		mResoulte.setOnClickListener(this);
		mSubtitle.setOnClickListener(this);
//		mPlayBar.setOnSeekBarChangeListener(seekBarListener);
		mPlayBar.setFocusable(false);
		mPlayBar.setClickable(false);
		mAudio.setOnClickListener(this);
		mSeek.setOnClickListener(this);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_ENTER) {
			if (mLayoutPlayStatus.getVisibility() == View.GONE) {
				showPlayStatus();
				mPlayStatus.setVisibility(View.GONE);
				handler.postDelayed(playStatus, 10000);
			} else {
				if(clickTimes > 0){
					
					handler.removeCallbacks(updateProgressThread);
					mPlayBar.setProgress(setPlayerTime);
					System.out.println("------- the setPlayerTime = "+setPlayerTime);
					startPlay_updateInfo(false,false,setPlayerTime);//
					mPlayBar.setSecondaryProgress(0);
					handler.postDelayed(playStatus, 10000);
					pop_dialog.showAnimation();
				}else{
					if (isPlaying()) {
						mPlayStatus.setVisibility(View.VISIBLE);
						pausePlayer();
						showPlayStatus = false;
						System.out.println("------- in pause ------");
					} else {
						mPlayStatus.setVisibility(View.GONE);
						System.out.println("------- in start ------");
						handler.removeCallbacks(playStatus);
						handler.postDelayed(playStatus, 10000);
						startPlayer();
						handler.post(updateProgressThread);
						showPlayStatus = true;
					}
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mLayoutPlayInfo.getVisibility() == View.VISIBLE) {
//				mLayoutPlayInfo.setVisibility(View.GONE);
				closePlayInfo();
				handler.removeCallbacks(playStatus);
				handler.removeCallbacks(updateProgressThread);
				return true;
			} else if (mLayoutPlayStatus.getVisibility() == View.VISIBLE) {
//				mLayoutPlayStatus.setVisibility(View.GONE);
				closePlayStatus();
				return true;
			} else {
				
				stopPlayAndSaveVideoinfo();
				backToDetailActivity();
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if(isPlaying()){
				if (mLayoutPlayStatus.getVisibility() == View.VISIBLE) {
//					mLayoutPlayStatus.setVisibility(View.GONE);
					closePlayStatus();
				}
				if (mLayoutPlayInfo.getVisibility() == View.GONE) {
					setFragment(isSerialVideo);
//					mLayoutPlayInfo.setVisibility(View.VISIBLE);
					showPlayInfo();
					mResoulte.requestFocus();
				}
			}
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			//  judge is Serial or not 
			if (mLayoutPlayInfo.getVisibility() != View.VISIBLE) {

				if (mLayoutPlayStatus.getVisibility() == View.GONE) {
					showPlayStatus();
					handler.removeCallbacks(playStatus);
				} else {
					if (isPlaying()) {
						if (lastKeyCode != KeyEvent.KEYCODE_DPAD_RIGHT) {
							lastKeyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
							clickTimes = 0;
						}
						if (clickTimes == 0) {
							handler.removeCallbacks(playStatus);
						}
						clickTimes++;
						System.out.println("-----change time per click = "
								+ ((clickTimes / 10) * 5000 + 10000));
						setPlayerTime = setPlayerTime
								+ ((clickTimes / 10) * 5000 + 10000);
						mPlayBar.setSecondaryProgress(setPlayerTime);
						
						if (setPlayerTime >= mDuration) {
							setPlayerTime = mDuration - 1000;
						}
						
					}
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (mLayoutPlayInfo.getVisibility() != View.VISIBLE) {

				if (mLayoutPlayStatus.getVisibility() == View.GONE) {
					showPlayStatus();
					handler.removeCallbacks(playStatus);
				} else {
					if (isPlaying()) {
						if (lastKeyCode != KeyEvent.KEYCODE_DPAD_LEFT) {
							lastKeyCode = KeyEvent.KEYCODE_DPAD_LEFT;
							clickTimes = 0;
						}
						if (clickTimes == 0) {
							handler.removeCallbacks(playStatus);
						}
						clickTimes++;
						System.out.println("-----change time per click = "
								+ (clickTimes / 10) * 5000 + 10000);
						setPlayerTime = setPlayerTime
								- ((clickTimes / 10) * 5000 + 10000);
						// if(setPlayerTime >= mDuration-5000){
						// setPlayerTime = mDuration-5000;
						// }
						if (setPlayerTime <= 0) {
							setPlayerTime = 0;
						}
						mPlayBar.setSecondaryProgress(setPlayerTime);
					}
				}
			}
		}

		return super.onKeyDown(keyCode, event);
	}
	
	private int setPlayerTime = 0;
	private int clickTimes = 0;
	private int lastKeyCode = KeyEvent.KEYCODE_0;
	
	private void showPlayStatus(){
		clickTimes = 0;
		setPlayerTime = getCurrentTime();
		handler.post(updateProgressThread);
		mLayoutPlayStatus.setVisibility(View.VISIBLE);
	}
	
	private void closePlayStatus(){
		System.out.println("------ in close playstatus -----");
		clickTimes = 0;
		setPlayerTime = getCurrentTime();
		mPlayBar.setSecondaryProgress(0);
		mLayoutPlayStatus.setVisibility(View.GONE);
	}
	
	private void showPlayInfo(){
		mLayoutPlayInfo.setVisibility(View.VISIBLE);
	}
	
	private void closePlayInfo(){
		if(seekPop != null){
			seekPop.closeSeekPopWindow();
		}
		
		if(myPopList != null){
			myPopList.dismiss();
			myPopList = null;
		}
		mLayoutPlayInfo.setVisibility(View.GONE);
	}
	private boolean haveShowFragment = false;
	private void setFragment(boolean isSerialFragment) {
		if(!haveShowFragment){
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Bundle bundler = new Bundle();
			bundler.putBoolean("isPlayer", true);
			if (isSerialFragment) {
				AlbumFragment fg = AlbumFragment.newInstance();
				bundler.putInt("totalCount", mSerialInfo.getTotalCount());
				bundler.putInt("currentSelectItem", mCurFragmentSelectItem);
				fg.setArguments(bundler);
				ft.replace(R.id.fragment_layout, fg);
			} else {
				RelateFragment fg = RelateFragment.newInstance();
	
				fg.setArguments(bundler);
				ft.replace(R.id.fragment_layout, fg);
			}
	
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			haveShowFragment = true;
		}
	}

	spinnerPopWindow myPopList;
	SeekPopWindow seekPop;
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		int weight = v.getWidth()-22;
		int height = v.getHeight()-18;
		System.out.println("--------- the weight =  "+weight+"height = "+height);
		if (v == mSubtitle) {
			String[] mList = {getResources().getString(R.string.none)};
			myPopList = new spinnerPopWindow(VodPlayer.this, 1,v, mList, handler);
			myPopList.showPopWindow(weight,height);
			
		} else if (v == mResoulte) {
			String[] mList = videoInfo.getResoulteList();
			myPopList = new spinnerPopWindow(VodPlayer.this,videoInfo.getCurrentResoutleIndex(), v, mList, handler);
			myPopList.showPopWindow(weight,height);
		} else if (v == mAudio) {
			String[] mList = {getResources().getString(R.string.none)};
			myPopList = new spinnerPopWindow(VodPlayer.this, 1,v, mList, handler);
			myPopList.showPopWindow(weight,height);
		} else {
			seekPop = new SeekPopWindow();
			seekPop.showSeekPopWindow();
		}
	}

	Runnable playStatus = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("------- in play status ruannable-------"+showPlayStatus);
			if (showPlayStatus) {
//				mLayoutPlayStatus.setVisibility(View.GONE);
				closePlayStatus();
			}
		}
	};
	
	Runnable updateProgressThread = new Runnable() {

		@Override
		public void run() {
			if(isPlayerPrepare && isPlaying())
			{
				int curPosition = 0;
				try {
					curPosition = getCurrentTime(); 
					
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				if(mLayoutPlayStatus.getVisibility() == View.VISIBLE){
					
					String nowTime = stringForTime(curPosition);
					mPlayTime.setText(nowTime + "/" + sTotaltime);
					mPlayBar.setProgress(curPosition);
				}
				
				mCurrentPosition = currentTimeToSave = curPosition;
				handler.postDelayed(updateProgressThread,1000);
			}
		}

	};

	@SuppressLint("DefaultLocale")
	public String stringForTime(long millis) {
		int totalSeconds = (int) millis / 1000;
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = (totalSeconds / 60 / 60) % 24;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
	}

	
	

	private void prepareToPlay() {
		// TODO Auto-generated method stub
		System.out.println("------ the onPrepared start----------");
		isPlayerPrepare = true;
		//pop_dialog.closeAnimation();
		Log.d(TAG, "the prepareToPlay mCurrentPosition = "+ mCurrentPosition);
		if(getTotalTime() - mCurrentPosition < 2000)
			mCurrentPosition = 0;
		if (mCurrentPosition > 0) {
			pop_dialog.showConfirm(R.string.continue_play,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							startPlay_updateInfo(true, false, mCurrentPosition);
						}
					}, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							startPlay_updateInfo(true, true, 0);
						}
					});					
			
		}else{
			startPlay_updateInfo(true,true,0);
		}
		System.out.println("------ the onPrepared end----------");
	}

	// callStartToPlay: call mplay.start()   playFromZero: is need to seek(0)  playPosition: if (playFromZero == false),set other position 
	private void startPlay_updateInfo(boolean callStartToPlay,boolean playFromZero,int playPosition){

		mDuration = getTotalTime();
		sTotaltime = stringForTime(mDuration);
		mPlayBar.setMax(mDuration);
		
		if(!playFromZero){
//			if(getTotalTime() - playPosition < 5000)
//				 playPosition = 0;
			setSeekTime(playPosition);
		}
		if(callStartToPlay){
			startPlayer();
		}
//		handler.post(updateProgressThread);
		handler.postDelayed(playStatus, 10000);
		String nowPlayTime = stringForTime(playPosition);
		mPlayTime.setText(nowPlayTime + "/"+ sTotaltime);
		mPlayBar.setProgress(playPosition);
		showPlayStatus();
		mCurrentPosition = 0;
	}

	public void playSelectedVideo(int mSelectIndex) {
		// TODO Auto-generated method stub
//			mLayoutPlayInfo.setVisibility(View.GONE);
			closePlayInfo();
			stopPlayAndSaveVideoinfo();
			mCurFragmentSelectItem = mSelectIndex;
			if (isSerialVideo) {
				videoInfo.setCurrentVideoResoulteAndUrl(mCurFragmentSelectItem,mSerialInfo);
				playUrl = videoInfo.getCurrentVideoUrl().get(videoInfo.getCurrentResoutleIndex());
			}else{
				mCurrentContent = ContentManager.getCurrent().getNode(mSelectIndex);
				CoreHandler.getInstace().loadExtraInfo(mCurrentContent);
				mfilePath.setText(getIntent().getStringExtra("filePath") + "/"+ mRelateVideoList.get(mSelectIndex).get("videoName"));

			}
	}

	
	 @SuppressLint("HandlerLeak")
	Handler Playerhandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(Command.values()[msg.what]){
			case CMD_PLAYER_ERROR:
//				System.out.println("---back detail ----");
//				pop_dialog.closeAnimation();
				closePlayInfo();
				closePlayStatus();
				pop_dialog.showWarning(
						getResources().getString(R.string.player_error),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								backToDetailActivity();
								
							}
						});	
				stopPlayAndSaveVideoinfo();
				break;
			case CMD_PLAYER_COMPLETE:
				currentTimeToSave = 0;
				stopPlayAndSaveVideoinfo();
				backToDetailActivity();
				break;
			case CMD_PLAYER_PREPARED:
				prepareToPlay();
				break;
			case CMD_PLAYER_SUFACE_DESTORYED:
				System.out.println("---------- the surface destory --------");
				isPlayerPrepare = false;
				break;
			case CMD_PLAYER_SUFACE_CREATED:
				initPlayer(playUrl);
				break;
			default:
					break;
			}
		}
	 };
	 
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				if (msg.obj == mResoulte) {
					videoInfo.setCurrentResoutleIndex(msg.arg1);
					System.out.println("-------currentIndex = "+videoInfo.getCurrentResoutleIndex());
					System.out.println("------- msg.arg1 = "+ msg.arg1);
					if (videoInfo.getCurrentResoutleIndex() != msg.arg1)
					{
						playUrl = videoInfo.getCurrentVideoUrl().get(msg.arg1);
						stopPlayAndSaveVideoinfo();
						closePlayInfo();
						initPlayer(playUrl);
					}
				}else if (msg.obj == mAudio){
					
				}else if (msg.obj == mSubtitle){
					
				}
			} else if (msg.what == CoreHandler.Callback.CBK_GET_EXTRA_DONE.ordinal()) {

				pop_dialog.closeAnimation();
				
				if (isSerialVideo == true) {
					mSerialInfo = (Serial) historyManager.getInCache(mCurrentContent);
					mCurrentPosition = mSerialInfo.getLastPlayTime(mSerialInfo.getLastPlayIndex()) * 1000;
					videoInfo.setCurrentVideoResoulteAndUrl(mCurFragmentSelectItem, mSerialInfo);
					playUrl = videoInfo.getCurrentVideoUrl().get(videoInfo.getCurrentResoutleIndex());
				} else if (isSerialVideo == false) {
					mVideoInfo = (Video) historyManager.getInCache(mCurrentContent);
					mCurrentPosition = mVideoInfo.getLastPlayTime() * 1000;
					videoInfo.setCurrentVideoResoulteAndUrl(mVideoInfo);
					playUrl = videoInfo.getCurrentVideoUrl().get(videoInfo.getCurrentResoutleIndex());
				}
				initPlayer(playUrl);
			} else if(msg.what == CoreHandler.Callback.CBK_GET_EXTRA_FAIL.ordinal()){
				System.out.println("------------ CBK_GET_EXTRA_FAIL ---------");
				pop_dialog.showWarning(getResources().getString(R.string.getextendinfo_fail), 	new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								backToDetailActivity();
								
							}
						});	
				stopPlayAndSaveVideoinfo();
				
			}
		};
	};

private class SeekPopWindow {
	private PopupWindow popView;
	private View view;
	private Button mSeekOk;
	private TextView[] tm = new TextView[6];
	private Button[] timeBg = new Button[3];
	private TextView mTotalTime;
	
	private int curIdx = 0;
	private int oldIdex = -1;

	public void showSeekPopWindow() {

		if (popView == null) {
			view = LayoutInflater.from(VodPlayer.this).inflate(R.layout.popup_seek, null, false);
			popView = new PopupWindow(view, 583, 250, true);
			tm[0] = (TextView) view.findViewById(R.id.seek_time1);
			tm[1] = (TextView) view.findViewById(R.id.seek_time2);
			tm[2] = (TextView) view.findViewById(R.id.seek_time3);
			tm[3] = (TextView) view.findViewById(R.id.seek_time4);
			tm[4] = (TextView) view.findViewById(R.id.seek_time5);
			tm[5] = (TextView) view.findViewById(R.id.seek_time6);
			mTotalTime = (TextView) view.findViewById(R.id.seek_totaltime);
			
			
			mSeekOk = (Button) view.findViewById(R.id.seek_ok);
			timeBg[0] = (Button)view.findViewById(R.id.time_hour);
			timeBg[1] = (Button)view.findViewById(R.id.time_min);
			timeBg[2] = (Button)view.findViewById(R.id.time_sec);
			
			view.setFocusable(true);
			view.setFocusableInTouchMode(true);
			view.setOnKeyListener(buttonKeyListener);
			mSeekOk.setOnKeyListener(buttonKeyListener);
			for(int i=0;i <3;i++){
				timeBg[i].setOnKeyListener( buttonKeyListener);
			}

			popView.showAtLocation(view, Gravity.NO_GRAVITY, 348, 143);
			mSeekOk.setOnClickListener(buttonOnClick);
			initSeekTime();
//			curIdx = 0;
			timeBg[0].requestFocus();
		}
	}
	
	public void closeSeekPopWindow(){
		if (popView != null) {
			mResoulte.setSelected(false);
			popView.dismiss();
			popView = null;
		}
	}
	OnKeyListener buttonKeyListener = new OnKeyListener() {
		@SuppressLint("ShowToast")
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
				if (arg1 == KeyEvent.KEYCODE_BACK) {
					if (popView != null) {
						mResoulte.setSelected(false);
						popView.dismiss();
						popView = null;
					}
				}
				if ((arg0 == timeBg[0]) || (arg0 == timeBg[1]) || (arg0 == timeBg[2])) {
					if (arg1 == KeyEvent.KEYCODE_0
							|| arg1 == KeyEvent.KEYCODE_1
							|| arg1 == KeyEvent.KEYCODE_2
							|| arg1 == KeyEvent.KEYCODE_3
							|| arg1 == KeyEvent.KEYCODE_4
							|| arg1 == KeyEvent.KEYCODE_5
							|| arg1 == KeyEvent.KEYCODE_6
							|| arg1 == KeyEvent.KEYCODE_7
							|| arg1 == KeyEvent.KEYCODE_8
							|| arg1 == KeyEvent.KEYCODE_9) {
						int bValue = arg1 - KeyEvent.KEYCODE_0;
						if (gotoCheckEnterTimeNum(arg0, bValue)) {
							
							setTimeText(arg0,bValue);
							arg1 = KeyEvent.KEYCODE_DPAD_RIGHT;
						} else {
							Toast.makeText(VodPlayer.this, "input error!!!",2000).show();
						}
					}
				}
				if(arg1 == KeyEvent.KEYCODE_DPAD_RIGHT){
					oldIdex = curIdx;
					if(curIdx >= 5){
						curIdx = 6;
						oldIdex = 5;
					}else{
						curIdx++;
					}
					updateCursor(oldIdex,curIdx,true);
					if(curIdx == 6){
						System.out.println(" move to seek_ok");
						if(mSeekOk.hasFocus() == false){
							mSeekOk.requestFocus();
						}
						return true;  // not movie the focus to the next button
					}else{
						return true;
					}
				}else if(arg1 == KeyEvent.KEYCODE_DPAD_LEFT){
					oldIdex = curIdx;
					if(curIdx == 0){
						curIdx = 0;
					}else if(curIdx == 6){
						oldIdex = curIdx = 5;
					}else{
						curIdx--;
					}
					updateCursor(oldIdex,curIdx,false);
					if(oldIdex%2 == 1 && mSeekOk.hasFocus() == false){
						System.out.println(" not movie the focus to the prev button");
						return true;  // not movie the focus to the next button
					}
				}
			}
			return false;
		}
	};

	private void setTimeText(View view,int value){
		String timeString = ((TextView) view).getText().toString();
		
		int intTime = Integer.parseInt(timeString);
		if(curIdx%2 == 0){
			intTime = value*10+intTime%10;
			int len = String.valueOf(intTime).length();
			String timeSet = ("0" + String.valueOf(intTime)).substring(len-1,len+1);
			
			((TextView) view).setText(timeSet);
		}else if(curIdx%2 == 1){
			intTime = value+intTime/10*10;
			int len = String.valueOf(intTime).length();
			String timeSet = ("0" + String.valueOf(intTime)).substring(len-1,len+1);
			((TextView) view).setText(timeSet);
		}
		
	};
	private void updateCursor(int mOldIdx,int mCurIndex,boolean toRight){
		if(toRight == true){
			if(mCurIndex != 6){
				tm[mCurIndex].setVisibility(View.VISIBLE);
			}
			tm[mOldIdx].setVisibility(View.GONE);
			if(mCurIndex <=5 && mCurIndex%2 == 0){
				timeBg[mCurIndex/2].requestFocus();
			}
		}else if(toRight == false){
			
			if(mOldIdx == 0){
				tm[mCurIndex].setVisibility(View.VISIBLE);
			}else{
				tm[mOldIdx].setVisibility(View.GONE);
				tm[mCurIndex].setVisibility(View.VISIBLE);
			}
		}
	}
	
	public boolean gotoCheckEnterTimeNum(View v, int key) {
		boolean ret = true;
		if ((v == timeBg[0])) {
			return true;
		} else if ((v == timeBg[1]) || (v == timeBg[2])) {
			if (curIdx == 2 || curIdx == 4){
				if(key > 5)
					ret = false;
			}
		}
		return ret;
	}

	public void initSeekTime() {
		int curtime = 0;
		String[] curTimes = new String[3];
		try {
			curtime = getCurrentTime();
		} catch (Exception e) {
			// TODO: handle exception
		}
		int totalSeconds = curtime / 1000;
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = (totalSeconds / 60 / 60) % 60;

		int len = String.valueOf(hours).length();
		curTimes[0] = ("0" + String.valueOf(hours)).substring(len-1,len+1);

		len = String.valueOf(minutes).length();
		curTimes[1] = ("0" + String.valueOf(minutes)).substring(len-1,len+1);
		
		len = String.valueOf(seconds).length();
		curTimes[2] = ("0" + String.valueOf(seconds)).substring(len-1,len+1);
		for (int i = 0; i < 3; i++) {
			System.out.println("------ the curtime = "+curTimes[i]);
			timeBg[i].setText(curTimes[i]);
		}
		
		mTotalTime.setText("Total Time :"+sTotaltime);
	}

	public int getSeekUIProgress() {
		int progress = 0;

		int hour = Integer.parseInt(timeBg[0].getText().toString());
		System.out.println("----- horu = " + hour);
		int min = Integer.parseInt(timeBg[1].getText().toString());
		System.out.println("----- min = " + min);
		int sec = Integer.parseInt(timeBg[2].getText().toString());
		System.out.println("----- sec = " + sec);
		progress = (hour * 60 * 60 + min * 60 + sec) * 1000;
		System.out.println("----- progress = " + progress);
		return progress;
	}

	OnClickListener buttonOnClick = new OnClickListener() {
		@SuppressLint("ShowToast")
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (popView != null) {
				mResoulte.setSelected(false);
				popView.dismiss();
				popView = null;
			}
			int setSeekTime = getSeekUIProgress();
			if (setSeekTime >= mDuration-5000) {
				Toast.makeText(VodPlayer.this, " the time input is error",2000).show();
				return;
			}
//			mLayoutPlayInfo.setVisibility(View.GONE);
			closePlayInfo();
			startPlay_updateInfo(false,false,setSeekTime);
//			mLayoutPlayInfo.setVisibility(View.GONE);
//			mLayoutPlayStatus.setVisibility(View.VISIBLE);
//			handler.removeCallbacks(playStatus);
//			handler.postDelayed(playStatus, 5000);
//			handler.removeCallbacks(updateProgressThread);
//			handler.post(updateProgressThread);
//			setSeekTime(setSeekTime);
//			mPlayBar.setProgress(setSeekTime);
//			mPlayTime.setText(stringForTime(setSeekTime) + "/" + stringForTime(mDuration));
		}
	};
}
}
class SelectedVideoData {
	
	static class VideoPlayInfo{
		private static final  VideoPlayInfo VIDEOINFO = new VideoPlayInfo();
		String[] resoulteList;
		String videoName;
		int currentResoutleIndex = 0;
		ArrayList<Integer> videoresoulteList = new ArrayList<Integer>();
		ArrayList<String> videourlList = new ArrayList<String>();
		public static VideoPlayInfo getInstace(){
			return VIDEOINFO;
		}
		public String[] getResoulteList() {
			return resoulteList;
		}
		public void setResoulteList(String[] resoulteList) {
			this.resoulteList = resoulteList;
		}
		public String getVideoName() {
			return videoName;
		}
		public void setVideoName(String videoName) {
			this.videoName = videoName;
		}
		
		public int getCurrentResoutleIndex() {
			return currentResoutleIndex;
		}
		public void setCurrentResoutleIndex(int currentResoutleIndex) {
			this.currentResoutleIndex = currentResoutleIndex;
		}
		
		public void setCurrentVideoResoulteAndUrl(int selectIndex,Serial serialInfo) 
		{
			videoresoulteList.clear();
			videourlList.clear();
			
			if (serialInfo.isSupport(RESLOUTION.SD)) {
				videoresoulteList.add(R.string.sd);
				videourlList.add(serialInfo.getPlayURL(selectIndex, RESLOUTION.SD));
			} 
			
			if (serialInfo.isSupport( RESLOUTION.HD720)) {
				videoresoulteList.add(R.string.hd720);
				videourlList.add(serialInfo.getPlayURL(selectIndex, RESLOUTION.HD720));
			}
			
			if (serialInfo.isSupport( RESLOUTION.HD1080)) {
				videoresoulteList.add(R.string.hd1080);
				videourlList.add(serialInfo.getPlayURL(selectIndex, RESLOUTION.HD1080));
			}
		}
		
		public void setCurrentVideoResoulteAndUrl(Video videoInfo) 
		{
			videoresoulteList.clear();
			videourlList.clear();
			
			if (videoInfo.isSupport(RESLOUTION.SD)) {
				videoresoulteList.add(R.string.sd);
				videourlList.add(videoInfo.getPlayURL(RESLOUTION.SD));
			} 
			
			if (videoInfo.isSupport(RESLOUTION.HD720)) {
				videoresoulteList.add(R.string.hd720);
				videourlList.add(videoInfo.getPlayURL(RESLOUTION.HD720));
			} 

			if (videoInfo.isSupport(RESLOUTION.HD1080)) {
				videoresoulteList.add(R.string.hd1080);
				videourlList.add(videoInfo.getPlayURL(RESLOUTION.HD1080));
			}
		}
		
		public ArrayList<Integer> getCurrentVideoResoulte(){
			return 	videoresoulteList;		
		}
		
		public ArrayList<String> getCurrentVideoUrl(){
			return 	videourlList;		
		}

	}
}