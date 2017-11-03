package com.ott.webtv;

import stb.input.keyboard_dialog.KeyboardDialog;
import stb.input.keyboard_dialog.KeyboardDialogStatusListener;
import stb.input.keyboard_dialog.KeyboardDialogUtil;
import stb.input.keyboard_dialog.TextSettingParams;
import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ott.webtv.VideoBrowser.SOURCE_TYPE;
import com.ott.webtv.core.ContentManager;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.DATA_TYPE;
import com.ott.webtv.core.DataNode.RESLOUTION;
import com.ott.webtv.core.DataNode.Serial;
import com.ott.webtv.core.DataNode.Video;
import com.ott.webtv.core.StoreManager;

@SuppressLint("NewApi")
public class VodPlayer extends Player implements OnClickListener,
		OnHoverListener {

	private View mLayoutPlayInfo, mLayoutPlayStatus;
	private CheckBox mAutoPlayCheckBox;
	private ImageView mPlayStatus, mPlayMenu;
	private SeekBar mPlayBar;
	private Button mResoulte, mAudio, mSubtitle, mSeek;
	private TextView mPlayTime;
	private static SurfaceView surfaceview;
	private int mCurrentPosition = 0;
	private int mDuration = 0;
	private int mCurSourceType;
	private String sTotaltime;
	private TextView mfilePath;
	private boolean isSerialVideo = true;
	private boolean showPlayStatus = true;
	private boolean isPlayerPrepare = false;
	private boolean bAutoPlay = true;
	private Serial mSerialInfo;
	private Video mVideoInfo;
	private String playUrl = "";
	private int mCurSerialFragmentSelectIndex = 0;
	private int mCurVideoSelectIndex = 0;
	private int mHomePageStart = 0; // from 0 -- xx
	private final String TAG = "PlayerActivity";

	private StoreManager<Content> historyManager = StoreManager.getHisManager();

	private VideoPlayInfo videoInfo = VideoPlayInfo.getInstace();
	RelateFragment mFragment;
	private Content mcontent;

	String resolutions[] = new String[3];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Log.d(TAG, "---- onCreate --------");
		setContentView(R.layout.vod_player);

		CoreHandler.getInstace().setHandler(handler);

		getIntentData();
		findViews();

		resolutions[0] = getResources().getString(R.string.low);
		resolutions[1] = getResources().getString(R.string.mid);
		resolutions[2] = getResources().getString(R.string.high);

		pop_dialog = new PopDialog(VodPlayer.this, dialogBackListener);

		mResoulte.setText(resolutions[videoInfo.getCurrentResoutleIndex()]);

		mfilePath.setText(VideoPlayInfo.getInstace().getVideoName());

		pop_dialog.showAnimation();
		CreatePlayer(VodPlayer.this, Playerhandler, surfaceview);
		CreateSubAudioObject();
	}

	DialogInterface.OnKeyListener dialogBackListener = new DialogInterface.OnKeyListener() {

		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& keyCode == KeyEvent.KEYCODE_BACK) {
				stopPlayAndSaveVideoinfo();
				backToDetailActivity();
				return true;
			}
			return false;
		}
	};

	private void getIntentData() {
		isSerialVideo = getIntent().getBooleanExtra("videoType", true);

		mCurVideoSelectIndex = getIntent().getIntExtra("curContentIndex", 0);

		mHomePageStart = getIntent().getIntExtra("pageStart", 0);

		mCurSourceType = getIntent().getIntExtra("sourceType", 0);

		if (mCurSourceType == SOURCE_TYPE.HISTORY.ordinal()) {
			mcontent = hisManager.get(mCurVideoSelectIndex + mHomePageStart);
		} else if (mCurSourceType == SOURCE_TYPE.FAV.ordinal()) {
			mcontent = favManager.get(mCurVideoSelectIndex + mHomePageStart);
		} else {
			mcontent = ContentManager.getCurrent()
					.getNode(mCurVideoSelectIndex);
		}

		if (isSerialVideo == true) {
			mSerialInfo = (Serial) mcontent;
			mSerialInfo = (Serial) historyManager.getInCache(mSerialInfo);
			mCurrentPosition = mSerialInfo.getLastPlayTime(mSerialInfo
					.getLastPlayIndex()) * 1000;
			mCurSerialFragmentSelectIndex = getIntent().getIntExtra(
					"serialCurPlayIndex", 0);
		} else {
			mVideoInfo = (Video) mcontent;
			mVideoInfo = (Video) historyManager.getInCache(mVideoInfo);
			mCurrentPosition = mVideoInfo.getLastPlayTime() * 1000;

			System.out.println("---------- mVideoInfo title = "
					+ mVideoInfo.getTitle());
			System.out.println("---------- currentPostion = "
					+ mCurrentPosition);
		}

		playUrl = getIntent().getStringExtra("playUrl");

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "---- onResume --------");
		if (isPlayerPrepare) {
			Log.d(TAG, "onResume,player start");
			if (mCurrentPosition == 0) {
				try {
					/* play start */
					initPlayer(playUrl);
				} catch (Exception e) {
					// TODO: handle exception
				}
			} else {
				/* play start */
				Log.d(TAG, " position===" + mCurrentPosition);
				initPlayer(playUrl);
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mLayoutPlayStatus.getVisibility() == View.VISIBLE) {
			closePlayStatusLayout();
		}
		if (mLayoutPlayInfo.getVisibility() == View.VISIBLE) {
			closePlayInfoLayout();
		}
		if (!isFinishing()) {
			System.out
					.println("---------finish() status------" + isFinishing());
			stopPlayAndSaveVideoinfo();
		}
		Log.d(TAG, "---- onPause --------");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		pop_dialog.closeAnimation();
		Log.d(TAG, "---- onStop --------");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mCurrentPosition = 0;
		handler.removeCallbacksAndMessages(null);
		Playerhandler.removeCallbacksAndMessages(null);
		Log.d(TAG, "---- onDestroy --------");
	}

	private void saveVideoPlayTime() {
		try {
			Content data = null;
			System.out.println("--------- mCurrentPosition = "
					+ mCurrentPosition);
			if (isSerialVideo) {
				data = mSerialInfo;
				mSerialInfo.setLastPlayTime(mCurSerialFragmentSelectIndex,
						mCurrentPosition / 1000);
				System.out
						.println("------------------ the index save into history = "
								+ mCurSerialFragmentSelectIndex);
			} else {
				data = mVideoInfo;
				mVideoInfo.setLastPlayTime(mCurrentPosition / 1000);
			}
			mCurrentPosition = 0;
			if (historyManager.isExist(data)) {
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

	private void backToDetailActivity() {
		stopPlayer();
		Intent intent = new Intent();
		if (isSerialVideo) {
			intent.putExtra("lastPlayIndex", mCurSerialFragmentSelectIndex);
		} else {
			intent.putExtra("lastPlayIndex", mCurVideoSelectIndex);
		}

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
		mPlayMenu = (ImageView) findViewById(R.id.play_menu);
		mPlayBar = (SeekBar) findViewById(R.id.play_progessbar);
		mPlayTime = (TextView) findViewById(R.id.play_time);
		mResoulte = (Button) findViewById(R.id.play_resoulte);

		mAudio = (Button) findViewById(R.id.play_audio);
		mSubtitle = (Button) findViewById(R.id.play_subtitle);
		mSeek = (Button) findViewById(R.id.play_seek);
		mfilePath = (TextView) findViewById(R.id.filmName);
		mAutoPlayCheckBox = (CheckBox) findViewById(R.id.autoplayBox);

		mPlayMenu.setOnClickListener(this);
		mPlayBar.setFocusable(false);
		mPlayBar.setClickable(false);

		mResoulte.setOnClickListener(this);
		mSubtitle.setOnClickListener(this);
		mAudio.setOnClickListener(this);
		mSeek.setOnClickListener(this);

		mResoulte.setOnHoverListener(this);
		mSubtitle.setOnHoverListener(this);
		mAudio.setOnHoverListener(this);
		mSeek.setOnHoverListener(this);

		mAutoPlayCheckBox.setChecked(true);
		mAutoPlayCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						// TODO Auto-generated method stub
						if (arg1) {
							bAutoPlay = true;
						} else {
							bAutoPlay = false;
						}
					}
				});
		mPlayBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if (fromUser) {
					System.out.println("------------- the progress = "
							+ progress + "   mDuration = " + mDuration);
					if (progress >= mDuration) {
						progress = mDuration - 1000;
					}
					startPlay_updateInfo(false, progress);
					handler.postDelayed(playStatus, 10000);
					pop_dialog.showAnimation();
				}
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getButtonState()) {
		case MotionEvent.BUTTON_PRIMARY:
			System.out.println("MotionEvent.BUTTON_PRIMARY Left Click");

			if (mLayoutPlayStatus.getVisibility() == View.GONE) {
				showPlayStatusLayout();
				mPlayStatus.setVisibility(View.GONE);
				handler.postDelayed(playStatus, 10000);
			} else {
				if (isPlaying()) {
					pauseVideo();
				} else {
					playVideo();
				}
			}
			break;
		case MotionEvent.BUTTON_SECONDARY:
			if (mLayoutPlayInfo.getVisibility() == View.VISIBLE) {
				closePlayInfoLayout();
				handler.removeCallbacks(playStatus);
				handler.removeCallbacks(updateProgressThread);
				return true;
			} else if (mLayoutPlayStatus.getVisibility() == View.VISIBLE) {
				closePlayStatusLayout();
				return true;
			} else {
				stopPlayAndSaveVideoinfo();
				backToDetailActivity();
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	private void pauseVideo() {
		mPlayStatus.setVisibility(View.VISIBLE);
		pausePlayer();
		showPlayStatus = false;
		System.out.println("------- pause ------");
	}

	private void playVideo() {
		mPlayStatus.setVisibility(View.GONE);
		System.out.println("------- play ------");
		handler.removeCallbacks(playStatus);
		handler.postDelayed(playStatus, 10000);
		startPlayer();
		handler.removeCallbacks(updateProgressThread);
		handler.post(updateProgressThread);
		showPlayStatus = true;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_ENTER) {
			if (mLayoutPlayStatus.getVisibility() == View.GONE) {
				showPlayStatusLayout();
				mPlayStatus.setVisibility(View.GONE);
				handler.postDelayed(playStatus, 10000);
			} else if (mcontent.isVodContent() == true) {
				if (clickTimes > 0) {

					handler.removeCallbacks(updateProgressThread);
					mPlayBar.setProgress(setPlayerTime);
					System.out.println("------- the setPlayerTime = "
							+ setPlayerTime);
					startPlay_updateInfo(false, setPlayerTime);
					mPlayBar.setSecondaryProgress(0);
					handler.postDelayed(playStatus, 10000);
					pop_dialog.showAnimation();
				} else {
					if (isPlaying()) {
						pauseVideo();
					} else {
						playVideo();
					}
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mLayoutPlayInfo.getVisibility() == View.VISIBLE) {
				closePlayInfoLayout();
				handler.removeCallbacks(playStatus);
				handler.removeCallbacks(updateProgressThread);
				if (mcontent != mTempContent) {
					if (mcontent.getContentType() == DATA_TYPE.SERIAL) {
						isSerialVideo = true;
					} else if (mcontent.getContentType() == DATA_TYPE.VIDEO) {
						isSerialVideo = false;
					}
					setFragment(isSerialVideo, true);
				}
				return true;
			} else if (mLayoutPlayStatus.getVisibility() == View.VISIBLE) {
				closePlayStatusLayout();
				return true;
			} else {
				stopPlayAndSaveVideoinfo();
				backToDetailActivity();
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			// if(isPlaying()){
			if (mLayoutPlayStatus.getVisibility() == View.VISIBLE) {
				closePlayStatusLayout();
			}
			if (mLayoutPlayInfo.getVisibility() == View.GONE) {
				if (mCurSourceType == SOURCE_TYPE.HISTORY.ordinal()
						&& !isSerialVideo) {
					mFragment.updateData();
				}
				mTempContent = mcontent;
				mTempSelectIndex = mCurVideoSelectIndex;
				showPlayInfoLayout();
				mResoulte.requestFocus();
			}
			// }
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			// judge is Serial or not
			if (mLayoutPlayInfo.getVisibility() != View.VISIBLE) {

				if (mLayoutPlayStatus.getVisibility() == View.GONE) {
					showPlayStatusLayout();
					handler.removeCallbacks(playStatus);
				} else {
					if (mcontent.isVodContent() == true && isPlaying()) {
						if (lastKeyCode != KeyEvent.KEYCODE_DPAD_RIGHT) {
							lastKeyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
							clickTimes = 0;
						}
						if (clickTimes == 0) {
							handler.removeCallbacks(playStatus);
						}
						clickTimes++;
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
					showPlayStatusLayout();
					handler.removeCallbacks(playStatus);
				} else {
					if (mcontent.isVodContent() == true && isPlaying()) {
						if (lastKeyCode != KeyEvent.KEYCODE_DPAD_LEFT) {
							lastKeyCode = KeyEvent.KEYCODE_DPAD_LEFT;
							clickTimes = 0;
						}
						if (clickTimes == 0) {
							handler.removeCallbacks(playStatus);
						}
						clickTimes++;
						setPlayerTime = setPlayerTime
								- ((clickTimes / 10) * 5000 + 10000);
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

	private void showPlayStatusLayout() {
		clickTimes = 0;
		setPlayerTime = getCurrentTime();
		handler.post(updateProgressThread);
		mLayoutPlayStatus.setVisibility(View.VISIBLE);
	}

	private void closePlayStatusLayout() {
		System.out.println("------ in close playstatus -----");

		clickTimes = 0;
		setPlayerTime = getCurrentTime();
		mPlayBar.setSecondaryProgress(0);
		mLayoutPlayStatus.setVisibility(View.GONE);
	}

	private void showPlayInfoLayout() {
		View gotoView = findViewById(R.id.gotoView);
		View checkboxView = findViewById(R.id.autoplay);
		if (mCurSourceType == SOURCE_TYPE.LIVE.ordinal()
				|| mcontent.isVodContent() == false) {
			gotoView.setVisibility(View.GONE);
		} else {
			gotoView.setVisibility(View.VISIBLE);
		}

		if (isSerialVideo) {
			checkboxView.setVisibility(View.VISIBLE);
		} else {
			checkboxView.setVisibility(View.GONE);
		}
		mLayoutPlayInfo.setVisibility(View.VISIBLE);
	}

	private void closePlayInfoLayout() {
		if (seekPop != null) {
			seekPop.closeSeekPopWindow();
		}

		if (myPopList != null) {
			myPopList.dismiss();
			myPopList = null;
		}
		mLayoutPlayInfo.setVisibility(View.GONE);
	}

	private void clearScreen(){
		pop_dialog.closeAnimation();
		closePlayInfoLayout();
		closePlayStatusLayout();
	}
	private void setFragment(boolean isSerialFragment, boolean updateFragemnt) {
		if (updateFragemnt) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Bundle bundler = new Bundle();
			bundler.putBoolean("isPlayer", true);
			if (isSerialFragment) {
				AlbumFragment fg = AlbumFragment.newInstance();
				bundler.putInt("totalCount", mSerialInfo.getTotalCount());
				bundler.putInt("currentSelectItem",
						mCurSerialFragmentSelectIndex + 1);
				fg.setArguments(bundler);
				ft.replace(R.id.fragment_layout, fg);
			} else {
				mFragment = RelateFragment.newInstance();
				bundler.putInt("currentSelectItem", mCurVideoSelectIndex);
				mFragment.setArguments(bundler);
				ft.remove(mFragment);
				ft.replace(R.id.fragment_layout, mFragment);
			}

			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		}

	}

	spinnerPopWindow myPopList;
	SeekPopWindow seekPop;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		int weight = v.getWidth() - 22;
		int height = 36;// v.getHeight() - 18;
		System.out.println("--------- the weight =  " + weight + "height = "
				+ height);
		if (v == mSubtitle) {
			myPopList = new spinnerPopWindow(VodPlayer.this,
					getSubSelectIndex(), v, getSubtitleInfo(), handler,false);
			myPopList.showPopWindow(weight * 2, height);

		} else if (v == mResoulte) {
			String[] mList = { getResources().getString(R.string.low),
					getResources().getString(R.string.mid),
					getResources().getString(R.string.high) };
			height = 44;// v.getHeight() - 18;
			myPopList = new spinnerPopWindow(VodPlayer.this,
					videoInfo.getCurrentResoutleIndex(), v, mList, handler,true);
			myPopList.showPopWindow(weight, height);

		} else if (v == mAudio) {
			myPopList = new spinnerPopWindow(VodPlayer.this,
					getAudioSelectIndex(), v, getAudioInfo(), handler,false);
			myPopList.showPopWindow(weight * 2, height);
		} else if (v == mSeek) {
			closePlayInfoLayout();
			seekPop = new SeekPopWindow();
			seekPop.showSeekPopWindow();

		} else if (v == mPlayMenu) {
			if (mLayoutPlayStatus.getVisibility() == View.VISIBLE) {
				closePlayStatusLayout();
			}
			if (mLayoutPlayInfo.getVisibility() == View.GONE) {
				// setFragment(isSerialVideo);
				if (mCurSourceType == SOURCE_TYPE.HISTORY.ordinal()
						&& !isSerialVideo) {
					mFragment.updateData();
				}
				showPlayInfoLayout();
				mResoulte.requestFocus();
			}
		}
	}

	@Override
	public boolean onHover(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_HOVER_ENTER:
			v.requestFocusFromTouch();
			break;
		default:
			break;
		}
		return false;
	}

	Runnable playStatus = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (showPlayStatus) {
				closePlayStatusLayout();
			}
		}
	};

	Runnable updateProgressThread = new Runnable() {

		@Override
		public void run() {
			if (isPlayerPrepare && isPlaying()) {
				int curPosition = 0;
				try {
					curPosition = getCurrentTime();
				} catch (Exception e) {
					// TODO: handle exception
				}

				if (mLayoutPlayStatus.getVisibility() == View.VISIBLE) {

					String nowTime = stringForTime(curPosition);
					if (mCurSourceType == SOURCE_TYPE.LIVE.ordinal()
							|| mcontent.isVodContent() == false) {
						// if (mCurSourceType == SOURCE_TYPE.LIVE.ordinal() ||
						// mDuration == 0) {
						mPlayTime.setText(getResources().getString(
								R.string.time_init));
						mPlayBar.setMax(100);
						mPlayBar.setProgress(mPlayBar.getMax());
					} else {
						mPlayTime.setText(nowTime + "/" + sTotaltime);
						mPlayBar.setProgress(curPosition);
					}
				}
				mCurrentPosition = curPosition;
				handler.postDelayed(updateProgressThread, 1000);
			}
		}

	};

	@SuppressLint("DefaultLocale")
	public String stringForTime(long millis) {
		int totalSeconds = (int) millis / 1000;
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = (totalSeconds / 60 / 60) % 24;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds)
				.toString();
	}

	private void prepareToPlay() {
		// TODO Auto-generated method stub
		System.out.println("------ the onPrepared start----------");
		isPlayerPrepare = true;
		// pop_dialog.closeAnimation();
		Log.d(TAG, "the prepareToPlay mCurrentPosition = " + mCurrentPosition);
		if (getTotalTime() - mCurrentPosition < 2000)
			mCurrentPosition = 0;

		if (isSerialVideo == true
				&& mCurSerialFragmentSelectIndex != mSerialInfo
						.getLastPlayIndex()) {
			mCurrentPosition = 0;
		}
		if (mCurrentPosition > 1000) {
			pop_dialog.showConfirm(R.string.continue_play,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							startPlay_updateInfo(false, mCurrentPosition);
						}
					}, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							startPlay_updateInfo(true, 0);
						}
					});

		} else {
			startPlay_updateInfo(true, 0);
		}
		System.out.println("------ the onPrepared end----------");
	}

	// callStartToPlay: call mplay.start() playFromZero: is need to seek(0)
	// playPosition: if (playFromZero == false),set other position
	private void startPlay_updateInfo(boolean playFromZero, int playPosition) {

		mDuration = getTotalTime();
		System.out.println("------total time = " + mDuration);
		System.out.println("------seekPosition = " + playPosition);
		sTotaltime = stringForTime(mDuration);
		mPlayBar.setMax(mDuration);

		if (!playFromZero) {
			if (!isPlaying()) {
				startPlayer();
			}
			setSeekTime(playPosition);
		} else {
			startPlayer();
		}
		mPlayStatus.setVisibility(View.GONE);
		handler.postDelayed(playStatus, 10000);
		// String nowPlayTime = stringForTime(playPosition);
		// mPlayTime.setText(nowPlayTime + "/"+ sTotaltime);
		mPlayBar.setProgress(playPosition);
		showPlayStatusLayout();
		mCurrentPosition = 0;

		setFragment(isSerialVideo, true);

	}

	private StoreManager<Content> favManager = StoreManager.getFavManager();
	private StoreManager<Content> hisManager = StoreManager.getHisManager();
	private Content mTempContent = null; // used for change serial from video,
											// display album but not play it
	private int mTempSelectIndex = 0;

	public void playSelectedVideo(int mSelectIndex) {
		// TODO Auto-generated method stub

		closePlayStatusLayout();
		pop_dialog.showAnimation();

		if (isSerialVideo) {
			closePlayInfoLayout();
			stopPlayAndSaveVideoinfo();
			mCurSerialFragmentSelectIndex = mSelectIndex;
			mcontent = mTempContent;
			mCurVideoSelectIndex = mTempSelectIndex;
			CoreHandler.getInstace().loadPlayUrl(mSerialInfo,
					mCurSerialFragmentSelectIndex);
		} else {
			mTempSelectIndex = mSelectIndex;
			if (mCurSourceType == SOURCE_TYPE.HISTORY.ordinal()) {
				mTempContent = hisManager.get(mSelectIndex + mHomePageStart);
			} else if (mCurSourceType == SOURCE_TYPE.FAV.ordinal()) {
				mTempContent = favManager.get(mSelectIndex + mHomePageStart);
			} else {
				mTempContent = ContentManager.getCurrent()
						.getNode(mSelectIndex);
			}

			if (mTempContent.getContentType() == DATA_TYPE.VIDEO) {
				closePlayInfoLayout();
				stopPlayAndSaveVideoinfo();
			}
			CoreHandler.getInstace().loadExtraInfo(mTempContent);
			mfilePath.setText(mTempContent.getTitle());
		}
	}

	@SuppressLint("HandlerLeak")
	Handler Playerhandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (Command.values()[msg.what]) {
			case CMD_PLAYER_ERROR:
//				pop_dialog.closeAnimation();
//				closePlayInfoLayout();
//				closePlayStatusLayout();
				clearScreen();
				pop_dialog.showWarning(R.string.player_error,
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
				mCurrentPosition = 0;
//				closePlayInfoLayout();
//				closePlayStatusLayout();
				clearScreen();
				stopPlayAndSaveVideoinfo();
				if (isSerialVideo
						&& bAutoPlay
						&& (mCurSerialFragmentSelectIndex < mSerialInfo
								.getTotalCount() - 1)) {
					System.out.println("----- the mCurIndex = "
							+ mCurSerialFragmentSelectIndex);
					System.out.println("----- the Serial TotalCount() = "
							+ mSerialInfo.getTotalCount());

					mCurSerialFragmentSelectIndex++;

					CoreHandler.getInstace().loadPlayUrl(mSerialInfo,
							mCurSerialFragmentSelectIndex);
				} else {
					backToDetailActivity();
				}
				break;
			case CMD_PLAYER_PREPARED:
				prepareToPlay();
				break;
			case CMD_PLAYER_SUFACE_DESTORYED:
				System.out.println("---------- the surface destory --------");
				isPlayerPrepare = false;
				break;
			case CMD_PLAYER_SUFACE_CREATED:
				System.out.println("---------- the surface create --------");
				initPlayer(playUrl);
				break;
			case CMD_PLAYER_BUFFER_START:
				System.out.println("---------- buffer start --------");
				if ((mLayoutPlayInfo.getVisibility() != View.VISIBLE)
						&& (seekPop == null || (seekPop != null && !seekPop
								.isSeekPopupShow())) && isPlaying()) {
					System.out.println("---------- show pop_dialog --------");
					pop_dialog.showAnimation();
				}
				break;
				
			case CMD_PLAYER_BUFFER_END:
				System.out.println("---------- buffer end --------");
				pop_dialog.closeAnimation();
				break;
			case CMD_PALYER_NETWORK_CONNECT_FAIL:
				clearScreen();
				stopPlayAndSaveVideoinfo();
				backToDetailActivity();
				break;
			default:
				break;
			}
		}
	};

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == spinnerPopWindow.UPDATE_RESOLUTE) {
				if (msg.obj == mResoulte) {
					System.out.println("-------currentIndex = "
							+ videoInfo.getCurrentResoutleIndex());
					if (videoInfo.getCurrentResoutleIndex() != msg.arg1) {
						if (isSupprotResoulte(msg.arg1)) {
							System.out.println("------other resolution's playUrl = "+ playUrl);
							mResoulte.setText(resolutions[msg.arg1]);
							videoInfo.setCurrentResoutleIndex(msg.arg1);
							handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									closePlayInfoLayout();
									stopPlayer();
									initPlayer(playUrl);
								}
							}, 300);
						} else {
							pop_dialog
									.showToast(R.string.not_support_resoultion);
							return;
						}
					}
				} else if (msg.obj == mAudio) {
					setAudioIndex(msg.arg1);

				} else if (msg.obj == mSubtitle) {
					setSubtitleIndex(msg.arg1);
				}
			} else if (msg.what == CoreHandler.Callback.CBK_GET_EXTRA_DONE
					.ordinal()) {

				pop_dialog.closeAnimation();
				mTempContent = (Content) msg.obj;

				if (mTempContent.getContentType() == DATA_TYPE.SERIAL) {
					isSerialVideo = true;
					mSerialInfo = (Serial) mTempContent;
					mSerialInfo = (Serial) hisManager.getInCache(mSerialInfo);
					mCurSerialFragmentSelectIndex = mSerialInfo
							.getLastPlayIndex();
					setFragment(true, true);
				} else if (mTempContent.getContentType() == DATA_TYPE.VIDEO) {
					closePlayInfoLayout();
					// stopPlayAndSaveVideoinfo();
					isSerialVideo = false;
					mVideoInfo = (Video) mTempContent;
					mcontent = mTempContent;
					mCurVideoSelectIndex = mTempSelectIndex;
					CoreHandler.getInstace().loadPlayUrl(mVideoInfo, -1);
					saveVideoPlayTime();
				}

			} else if (msg.what == CoreHandler.Callback.CBK_GET_EXTRA_FAIL
					.ordinal()) {
				System.out.println("------------ CBK_GET_EXTRA_FAIL ---------");
				if (!WebManager.reportBug(msg.arg1, msg.what,
						(Content) msg.obj, backDetail)) {
					pop_dialog.showWarning(R.string.getextendinfo_fail,
							backDetail);
				}
				stopPlayAndSaveVideoinfo();

			} else if (msg.what == CoreHandler.Callback.CBK_GET_URL_DONE
					.ordinal()) {
				pop_dialog.closeAnimation();

				if (isSerialVideo) {

					mCurrentPosition = mSerialInfo
							.getLastPlayTime(mCurSerialFragmentSelectIndex) * 1000;

				} else {
					mVideoInfo = (Video) historyManager.getInCache(mcontent);
					mCurrentPosition = mVideoInfo.getLastPlayTime() * 1000;

				}
				int resoutleTimes = 0;
				int curResoutleIndex = videoInfo.getCurrentResoutleIndex();
				while (resoutleTimes < 3) {
					if (isSupprotResoulte(curResoutleIndex)) {
						videoInfo.setCurrentResoutleIndex(curResoutleIndex);
						mResoulte.setText(resolutions[curResoutleIndex]);
						initPlayer(playUrl);
						return;
					}
					curResoutleIndex++;
					if (curResoutleIndex > 2)
						curResoutleIndex = 0;
					resoutleTimes++;
				}
			} else if (msg.what == CoreHandler.Callback.CBK_GET_URL_FAIL
					.ordinal()) {
				pop_dialog.closeAnimation();
				if (!WebManager.reportBug(msg.arg1, msg.what,
						(Content) msg.obj, backDetail)) {
					pop_dialog
							.showWarning(R.string.parse_url_error, backDetail);
				}

			} else if (msg.what == CoreHandler.Callback.CBK_NETWORK_CONNECT_FAIL
					.ordinal()) {
				pop_dialog.closeAnimation();
				pop_dialog.showWarning(R.string.network_fail, null);
			}

		};
	};

	private boolean isSupprotResoulte(int curResoluteIndex) {
		RESLOUTION curResolution = RESLOUTION.class.getEnumConstants()[curResoluteIndex];
		System.out.println("---curResolution = "+curResolution+"----  curResoluteIndex = " + curResoluteIndex);
		if (isSerialVideo) {
			if (mSerialInfo != null
					&& mSerialInfo.isSupport(mCurSerialFragmentSelectIndex,curResolution)) {
				playUrl = mSerialInfo.getPlayURL(mCurSerialFragmentSelectIndex,
						curResolution);
				System.out.println("---play----  PlayUrl = " + playUrl);
				return true;
			}
		} else {
			if (mVideoInfo != null && mVideoInfo.isSupport(curResolution)) {
				playUrl = mVideoInfo.getPlayURL(curResolution);
				System.out.println("---play----   PlayUrl = " + playUrl);
				return true;
			}
		}
		return false;
	}

	private class SeekPopWindow {
		private PopupWindow popView;
		private View view;
		private Button mSeekOk;
		private Button[] timeButton = new Button[3];
		private TextView mTotalTime;
		private KeyboardDialog mKeyboardDialog = null;

		public void showSeekPopWindow() {

			if (popView == null) {
				view = LayoutInflater.from(VodPlayer.this).inflate(
						R.layout.vodplay_popup_seek, null, false);
				popView = new PopupWindow(view, 583, 250, true);
				mTotalTime = (TextView) view.findViewById(R.id.seek_totaltime);

				mSeekOk = (Button) view.findViewById(R.id.seek_ok);
				timeButton[0] = (Button) view.findViewById(R.id.time_hour);
				timeButton[1] = (Button) view.findViewById(R.id.time_min);
				timeButton[2] = (Button) view.findViewById(R.id.time_sec);

				view.setFocusable(true);
				view.setFocusableInTouchMode(true);
				popView.setFocusable(true);
				popView.setBackgroundDrawable(new BitmapDrawable());
				popView.setOutsideTouchable(true);

				view.setOnKeyListener(buttonKeyListener);
				mSeekOk.setOnKeyListener(buttonKeyListener);
				mSeekOk.setOnHoverListener(editHoverListener);
				for (int i = 0; i < 3; i++) {
					timeButton[i].setOnKeyListener(buttonKeyListener);
					timeButton[i].setOnClickListener(buttonClickListener);
					timeButton[i].setOnHoverListener(editHoverListener);
					timeButton[i].setOnFocusChangeListener(editFocusChanged);
				}

				popView.showAtLocation(view, Gravity.NO_GRAVITY, 348, 143);
				mSeekOk.setOnClickListener(buttonOnClick);
				initSeekTime();
				timeButton[0].requestFocus();
			}
		}

		public boolean isSeekPopupShow() {
			if (popView != null) {
				return popView.isShowing();
			}
			return false;
		}

		public void closeSeekPopWindow() {
			if (popView != null) {
				mResoulte.setSelected(false);
				popView.dismiss();
				popView = null;
			}
		}

		OnFocusChangeListener editFocusChanged = new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				Button button = (Button)v;
				if (!hasFocus) {
				if(!button.getText().toString().equals("")){
					int setTime = Integer.parseInt(button.getText().toString());
						if (button == timeButton[1] || button == timeButton[2]) {
							if (setTime > 59) {
								pop_dialog.showToast(R.string.time_input_error);
								button.setText(59+"");
								return;
							}
						}
						int len = String.valueOf(setTime).length();
						String inputTime = ("0" + String.valueOf(setTime))
								.substring(len - 1, len + 1);
						button.setText(inputTime);
					} else {
						button.setHintTextColor(Color.WHITE);
					}
				} else {
					inputTimes = 0;
				if(button.getText().toString().equals("")){
					button.setHintTextColor(Color.rgb(44, 44, 158));
					}
				}
			}
		};

		OnHoverListener editHoverListener = new OnHoverListener() {

			@Override
			public boolean onHover(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_HOVER_ENTER:
					v.requestFocusFromTouch();
					break;
				}
				return false;
			}
		};

		int inputTimes = 0;
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
					if ((arg0 == timeButton[0]) || (arg0 == timeButton[1]) || (arg0 == timeButton[2])) {
						if (arg1 == KeyEvent.KEYCODE_0|| arg1 == KeyEvent.KEYCODE_1
							|| arg1 == KeyEvent.KEYCODE_2 || arg1 == KeyEvent.KEYCODE_3
								|| arg1 == KeyEvent.KEYCODE_4 || arg1 == KeyEvent.KEYCODE_5
								|| arg1 == KeyEvent.KEYCODE_6 || arg1 == KeyEvent.KEYCODE_7
								|| arg1 == KeyEvent.KEYCODE_8 || arg1 == KeyEvent.KEYCODE_9) {
								inputTimes++;
								if(inputTimes <= 2){
									int bValue = arg1 - KeyEvent.KEYCODE_0;
									setTimeText(inputTimes,arg0,bValue);
								}
						}
					}
				}
				return false;
			}
		};

		private void setTimeText(int times,View view,int value){
			
			if(times == 1){
				int intTime = value;
				System.out.println("-------first input time = "+String.valueOf(intTime));
				((Button) view).setText(String.valueOf(intTime));
			}else if(times == 2){
				String timeString = ((Button) view).getText().toString();
				int oldTime = Integer.parseInt(timeString);
				
				int intTime = oldTime*10 + value;
				System.out.println("-------second input time = "+String.valueOf(intTime));
				((Button) view).setText(String.valueOf(intTime));
			}
			
		};
		
		OnClickListener buttonClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setKeyboardParam((Button) v);
			}
		};

		private void setKeyboardParam(Button tv) {
			System.out.println();
			mKeyboardDialog = KeyboardDialogUtil
					.obtainKeyboardDialog(VodPlayer.this);
			TextSettingParams textSettingParams = new TextSettingParams();
			textSettingParams.mMaxLength = 2;
			textSettingParams.mInputType = InputType.TYPE_CLASS_NUMBER;
			String oldText = (tv.getText().toString().equals("")) ? tv
					.getHint().toString() : tv.getText().toString();
			mKeyboardDialog.showEditDialog(oldText,
					mKeyboardDialogStatusListener, textSettingParams);

		}

		private KeyboardDialogStatusListener mKeyboardDialogStatusListener = new KeyboardDialogStatusListener() {

			@Override
			public void onDialogDone(String ret) {
				for (int i = 0; i < 3; i++) {
						if (timeButton[i].hasFocus()) {
							if(i == 1 || i == 2){
							try{
								if(Integer.parseInt(ret) <= 59){
									timeButton[i].setText(ret);
								}
							}catch (NumberFormatException e){
								System.out.println("----goto  input time > 59 -------");
							}
						}else{
							timeButton[i].setText(ret);
						}
					}
					inputTimes = 0;
				}
			}

			@Override
			public void onDialogForceClose() {
						inputTimes = 0;
			}
			
			@Override
			public void onTextChange(String arg0) {
				if(timeButton[1].hasFocus() || timeButton[2].hasFocus()){
					try{
						if(Integer.parseInt(arg0) > 59){
							pop_dialog.showToast(R.string.time_input_error);
						}
					}catch (NumberFormatException e){
						System.out.println("----goto  input time > 59 -------");
					}
				}
			}

			@Override
			public void onDialogShow() {
				// TODO Auto-generated method stub
			}

			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				return super.onKeyDown(keyCode, event);
			}

		};

		public void initSeekTime() {
			int curtime = 0;
			String[] curTimes = new String[3];
			int getTimes[] = new int[3];
			try {
				curtime = getCurrentTime();
			} catch (Exception e) {
				// TODO: handle exception
			}
			int totalSeconds = curtime / 1000;
			getTimes[2] = totalSeconds % 60; // second
			getTimes[1] = (totalSeconds / 60) % 60; // minutes
			getTimes[0] = (totalSeconds / 60 / 60) % 60; // hours

			for (int i = 0; i < 3; i++) {
				int len = String.valueOf(getTimes[i]).length();
				curTimes[i] = ("0" + String.valueOf(getTimes[i])).substring(
						len - 1, len + 1);
				timeButton[i].setHint(curTimes[i]);
			}

			mTotalTime.setText("Total Time : " + sTotaltime);
		}

		public int getSeekUIProgress() {
			int progress = 0;
			String times[] = new String[3];
			for (int i = 0; i < 3; i++) {
				times[i] = (!(timeButton[i].getText().toString().equals(""))) ? timeButton[i]
						.getText().toString() : timeButton[i].getHint()
						.toString();
				System.out.println("----- time = " + times[i]);
			}
			int hour = Integer.parseInt(times[0]);
			int min = Integer.parseInt(times[1]);
			int sec = Integer.parseInt(times[2]);
			progress = (hour * 60 * 60 + min * 60 + sec) * 1000;
			return progress;
		}

		OnClickListener buttonOnClick = new OnClickListener() {
			@SuppressLint("ShowToast")
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (popView != null) {
					popView.dismiss();
					popView = null;
				}
				int setSeekTime = getSeekUIProgress();
				if (setSeekTime >= mDuration) {
					pop_dialog.showWarning(R.string.time_input_error, null);
					return;
				}
				closePlayInfoLayout();
				startPlay_updateInfo(false, setSeekTime);
			}
		};
	}

	DialogInterface.OnClickListener backDetail = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			stopPlayer();
			backToDetailActivity();
		}
	};

}

class VideoPlayInfo {
	private static final VideoPlayInfo VIDEOINFO = new VideoPlayInfo();
	String[] resoulteList;
	String videoName;
	int currentResoutleIndex = 0;

	public static VideoPlayInfo getInstace() {
		return VIDEOINFO;
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

}
