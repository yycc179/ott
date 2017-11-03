package com.ott.webtv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.ott.webtv.VideoPlayInfo;
import com.ott.webtv.VideoBrowser.SOURCE_TYPE;
import com.ott.webtv.core.ContentManager;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.StoreManager;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.DATA_TYPE;
import com.ott.webtv.core.DataNode.RESLOUTION;
import com.ott.webtv.core.DataNode.Serial;
import com.ott.webtv.core.DataNode.Video;
import com.ott.webtv.R;

@SuppressLint("NewApi")
public class DetaiInfoView extends Activity implements OnHoverListener {

	private ImageButton mPlay, mDownLoader, mCollect;
	private TextView mvideoinfo_descripe, mFileName;
	private TextView mTvVideoList;
	private ImageView mVideoPrePic, mVideoType;
	private Button mResoulte;

	public int mOperateMode = 0;
	private boolean isSerialFragment = true;
	private Serial mSerialInfo;
	private Video mVideoInfo;

	private String[] mResoulteSupportList = new String[3];
	private String mCurrentFilePath = "";
	private Content mcontent;
	private int mCurSelectIndex = -1;
	private StoreManager<Content> favManager;
	private StoreManager<Content> hisManager;
	private Boolean bCollected = false;
	private PopDialog pop_dialog;

	private int mHomeStart;
	private int mCurSourceType;

	private final String TAG = "VideoDetailInfo";

	private VideoPlayInfo videoInfo = VideoPlayInfo.getInstace();
	private RelateFragment mFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videodetailinfo);

		Log.d(TAG, "---- onCreate --------");
		findViews();
		setViewListener();
		getIntentData();

		hisManager = StoreManager.getHisManager();
		favManager = StoreManager.getFavManager();

		updateVideoInfo(mCurSelectIndex); // show the video info of selected

		if (mcontent.getContentType() == DATA_TYPE.SERIAL) {
			isSerialFragment = true;
		} else if (mcontent.getContentType() == DATA_TYPE.VIDEO) {
			isSerialFragment = false;
			setFragment(false);
		}

		mResoulteSupportList[0] = getResources().getString(R.string.low);
		mResoulteSupportList[1] = getResources().getString(R.string.mid);
		mResoulteSupportList[2] = getResources().getString(R.string.high);
		mFileName.setText(mcontent.getTitle());

	}

	private void getIntentData() {
		mHomeStart = getIntent().getIntExtra("pageStart", 0);
		mCurSelectIndex = getIntent().getIntExtra("selectIndex", 0);
		mCurSourceType = getIntent().getIntExtra("sourceType", 0);

		switch (mCurSourceType) {
		case 0:
			mVideoType.setImageResource(R.drawable.detailpage_vod_sel);
			break;
		case 1:
			mVideoType.setImageResource(R.drawable.detail_title_live_sel);
			break;
		case 2:
			mVideoType.setImageResource(R.drawable.detail_title_history_sel);
			break;
		case 3:
			mVideoType.setImageResource(R.drawable.detail_title_favorite_sel);
			break;
		case 4:
			mVideoType.setImageResource(R.drawable.detail_title_search);
			break;
		default:
			break;
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "---- onResume --------");
		mPlay.requestFocus();
		CoreHandler.getInstace().setHandler(handler);
	}

	public void setVideoInfoIntoVideoData(int CurrentSelectIndex) {
		
		int curResoutleIndex = videoInfo.getCurrentResoutleIndex();
		RESLOUTION curResoutle = RESLOUTION.class.getEnumConstants()[curResoutleIndex];
		
		mResoulte.setText(mResoulteSupportList[curResoutleIndex]);
		int resoutleTimes = 0;
		
		while (resoutleTimes < 3) {
			if (isSerialFragment == true) {
				if (mSerialInfo != null	&& mSerialInfo.isSupport(mCurSelectSerialVideoIndex,curResoutle)) {
					if(mSerialInfo.getPlayURL(mCurSelectSerialVideoIndex,curResoutle) != null){
						videoInfo.setCurrentResoutleIndex(curResoutleIndex);
						break;
					}
				}
			}else{
				if (mVideoInfo != null && mVideoInfo.isSupport(curResoutle)) {
					if (mVideoInfo.getPlayURL(curResoutle) != null){
						videoInfo.setCurrentResoutleIndex(curResoutleIndex);
						break;
					}
				}
			}
			curResoutleIndex--;
			if (curResoutleIndex < 0)
				curResoutleIndex = 2;
			curResoutle = RESLOUTION.class.getEnumConstants()[curResoutleIndex];
			resoutleTimes++;
		}
		if(resoutleTimes < 3){
			mResoulte.setText(mResoulteSupportList[curResoutleIndex]);
		}
		
		mCurrentFilePath = mcontent.getTitle();
		videoInfo.setVideoName(mCurrentFilePath);

		if (favManager.isExist(mcontent)) {
			bCollected = true;
			mCollect.setImageResource(R.drawable.detail_btn_favorite_h);
		} else {
			bCollected = false;
			mCollect.setImageResource(R.drawable.detail_btn_favorite_n);
		}
	}

	private void initUIContent(String descripe, String largePic) {
		ImageLoader mGetImage;
		mGetImage = new ImageLoader();
		mGetImage.setDownLoaderImageView(mVideoPrePic);
		mGetImage.setImageFromUrl(largePic);

		mFileName.setText(mcontent.getTitle());

		mvideoinfo_descripe.setText(descripe);
		mvideoinfo_descripe.setMovementMethod(ScrollingMovementMethod
				.getInstance());
		mvideoinfo_descripe.setFocusable(true);
		mvideoinfo_descripe.setVerticalScrollBarEnabled(true);

		if ((mvideoinfo_descripe.getLineCount() / 10) < 1) {
			mvideoinfo_descripe.setFocusable(false);
			mvideoinfo_descripe.setClickable(false);
		}

	}

	private void findViews() {
		mVideoType = (ImageView) findViewById(R.id.videoType);
		mPlay = (ImageButton) findViewById(R.id.play);
		mDownLoader = (ImageButton) findViewById(R.id.downLoader);
		mCollect = (ImageButton) findViewById(R.id.collect);
		mResoulte = (Button) findViewById(R.id.resolution);
		mVideoPrePic = (ImageView) findViewById(R.id.detail_videoPreView);
		mFileName = (TextView) findViewById(R.id.filmName);
		mvideoinfo_descripe = (TextView) findViewById(R.id.videoinfo_descripe);
		mTvVideoList = (TextView) findViewById(R.id.videoList);
		mPlay.requestFocus();
		pop_dialog = new PopDialog(DetaiInfoView.this, dialogBackListener);
	}

	private void setViewListener() {
		mvideoinfo_descripe.setMovementMethod(ScrollingMovementMethod
				.getInstance());
		mvideoinfo_descripe
				.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View arg0, boolean arg1) {
						// TODO Auto-generated method stub
							mvideoinfo_descripe.scrollTo(0, 0);
					}
				});

		mPlay.setOnFocusChangeListener(buttonOnFocus);
		mDownLoader.setOnFocusChangeListener(buttonOnFocus);
		mCollect.setOnFocusChangeListener(buttonOnFocus);

		mPlay.setNextFocusLeftId(R.id.resolution);
		mResoulte.setNextFocusRightId(R.id.play);
		mPlay.setOnHoverListener(this);
		mDownLoader.setOnHoverListener(this);
		mCollect.setOnHoverListener(this);
		mResoulte.setOnHoverListener(this);
		mvideoinfo_descripe.setOnHoverListener(this);

		mPlay.setOnClickListener(buttonClick);
		mDownLoader.setOnClickListener(buttonClick);
		mCollect.setOnClickListener(buttonClick);
		mResoulte.setOnClickListener(buttonClick);

		// pop_dialog.setListener(null, null);
	}

	DialogInterface.OnKeyListener dialogBackListener = new DialogInterface.OnKeyListener() {

		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& keyCode == KeyEvent.KEYCODE_BACK) {
				pop_dialog.closeAnimation();
				Intent intent = new Intent();
				intent.putExtra("indexInDetail", mCurSelectIndex);
				setResult(100, intent);
				finish();
				return true;
			}
			return false;
		}
	};

	OnFocusChangeListener buttonOnFocus = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			// TODO Auto-generated method stub
			if ((mPlay == arg0) || (mDownLoader == arg0) || (mCollect == arg0)
					|| (mResoulte == arg0)) {
				if (arg1) {
					mPlay.setSelected(false);
					mDownLoader.setSelected(false);
					mCollect.setSelected(false);
				}
			}
		}
	};

	OnClickListener buttonClick = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (arg0 == mPlay) {
				if (CoreHandler.isNetworkConnected(DetaiInfoView.this)) {

					if (mcontent.getPlayFlag() == false) {
						pop_dialog.showWarning(R.string.not_support_play, null);
						return;
					} else {
						if (isSerialFragment) {
							getVideoTrueUrl();
						}else{
							playVideo();
						}
					}
				} else {
					pop_dialog.showWarning(R.string.network_fail, null);
				}
			} else if (arg0 == mDownLoader) {
				downLoaderVideo();
			} else if (arg0 == mCollect) {
				collectVideo();
			} else if (arg0 == mResoulte) {
				int weight = arg0.getWidth() - 22;
				int height = 44;// arg0.getHeight() - 18;
				spinnerPopWindow myPopList = new spinnerPopWindow(
						DetaiInfoView.this,
						videoInfo.getCurrentResoutleIndex(), mResoulte,
						mResoulteSupportList, handler,true);
				myPopList.showPopWindow(weight, height);
			}
		}
	};


	private void setFragment(boolean isSerialFragment) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Bundle bundler = new Bundle();
		bundler.putBoolean("isPlayer", false);
		if (isSerialFragment) {
			AlbumFragment fg = AlbumFragment.newInstance();
			bundler.putInt("totalCount", mSerialInfo.getTotalCount());
			bundler.putInt("currentSelectItem",
					mSerialInfo.getLastPlayIndex() + 1);
			fg.setArguments(bundler);
			ft.replace(R.id.fragment_layout, fg);
			mFragment = null;
		} else {
			mFragment = RelateFragment.newInstance();
			bundler.putInt("currentSelectItem", mCurSelectIndex);
			mFragment.setArguments(bundler);
			ft.replace(R.id.fragment_layout, mFragment);
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getButtonState()) {
		case MotionEvent.BUTTON_PRIMARY:
			System.out.println("MotionEvent.BUTTON_PRIMARY Left Click");
			break;
		case MotionEvent.BUTTON_SECONDARY:
			System.out.println("MotionEvent.BUTTON_PRIMARY Right Click");
			Intent intent = new Intent();
			intent.putExtra("indexInDetail", mCurSelectIndex);
			setResult(100, intent);
			finish();
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d(TAG, "" + keyCode);
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			if (isSerialFragment) {
				if (mPlay.hasFocus()) {
					mPlay.setSelected(true);
					setCurrentOperatMode(0);
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.putExtra("indexInDetail", mCurSelectIndex);
			setResult(100, intent);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d(TAG, "-----onStop-----");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		Log.d(TAG, "-----onDestroy-----");
	}

	public void setCurrentOperatMode(int mode) {
		mOperateMode = mode;
	}

	private int mCurSelectSerialVideoIndex = 0;

	public void setCurrentSelectItem(int currentPosition) {
		mCurSelectSerialVideoIndex = currentPosition;
	}

	public void doForFragmentSelectedItem() {

		if (mOperateMode == 0) {
			if (mcontent.getPlayFlag() == false) {
				pop_dialog.showWarning(R.string.not_support_play, null);
				return;
			} else {
				if (isSerialFragment) {
					getVideoTrueUrl();
				}else{
					playVideo();
				}
			}
		} else if (mOperateMode == 1) {
			downLoaderVideo();
		} else if (mOperateMode == 2) {
			collectVideo();
		}
	}

	private void getVideoTrueUrl() {
		pop_dialog.showAnimation();
		if (isSerialFragment) {
			CoreHandler.getInstace().loadPlayUrl(mSerialInfo,
					mCurSelectSerialVideoIndex);
		} else {
			CoreHandler.getInstace().loadPlayUrl(mVideoInfo, -1);
		}
	}

	public void updateVideoInfo(int videoIndex) {
		pop_dialog.showAnimation();

		if (mCurSourceType == SOURCE_TYPE.HISTORY.ordinal()) {

			if (!isSerialFragment) {
				if(mFragment == null){
					setFragment(false);
				}else{
					mFragment.updateData();
				}
			}
			mcontent = hisManager.get(mHomeStart + videoIndex);
		} else if (mCurSourceType == SOURCE_TYPE.FAV.ordinal()) {
			mcontent = favManager.get(mHomeStart + videoIndex);
		} else if (ContentManager.getCurrent() != null) {
			mcontent = ContentManager.getCurrent().getNode(videoIndex);
		} else {
			System.out.println("------ContentManager = null -----");
		}

		mVideoPrePic.setImageResource(R.drawable.default_image);
		mvideoinfo_descripe.setText("");
		CoreHandler.getInstace().setHandler(handler);
		CoreHandler.getInstace().loadExtraInfo(mcontent);
		mCurSelectIndex = videoIndex;
		
		System.out.println("-------- check fav content -----");
		if (favManager.isExist(mcontent)) {
			bCollected = true;
			mCollect.setImageResource(R.drawable.detail_btn_favorite_h);
		} else {
			bCollected = false;
			mCollect.setImageResource(R.drawable.detail_btn_favorite_n);
		}

	}

	public void playVideo() {

		String playUrl = null;
		int resoutleTimes = 0;
		int curResoutleIndex = videoInfo.getCurrentResoutleIndex();
		RESLOUTION curResoutle = RESLOUTION.class.getEnumConstants()[curResoutleIndex];
		Intent intent = getIntent();
		intent.putExtra("videoType", isSerialFragment); // serial or video

		intent.putExtra("pageStart", mHomeStart);
		intent.putExtra("sourceType", mCurSourceType);
		intent.putExtra("curContentIndex", mCurSelectIndex);
		while (resoutleTimes < 3) {

			if (isSerialFragment == true) {
				intent.putExtra("serialCurPlayIndex",
						mCurSelectSerialVideoIndex);

				System.out.println("-------- the selected index = "
						+ mCurSelectSerialVideoIndex);

				if (mSerialInfo != null
						&& mSerialInfo.isSupport(mCurSelectSerialVideoIndex,
								curResoutle)) {
					playUrl = mSerialInfo.getPlayURL(
							mCurSelectSerialVideoIndex, curResoutle);
					if (playUrl != null)
						break;
				}

			} else {

				if (mVideoInfo != null && mVideoInfo.isSupport(curResoutle)) {
					playUrl = mVideoInfo.getPlayURL(curResoutle);
					if (playUrl != null)
						break;
				}
			}

			curResoutleIndex++;
			if (curResoutleIndex > 2)
				curResoutleIndex = 0;
			curResoutle = RESLOUTION.class.getEnumConstants()[curResoutleIndex];
			resoutleTimes++;
		}

		if (playUrl != null) {
			intent.putExtra("playUrl", playUrl);
			videoInfo.setCurrentResoutleIndex(curResoutleIndex);
			mResoulte.setText(mResoulteSupportList[curResoutleIndex]);
		} else {
			pop_dialog.showToast(R.string.not_support_play);
			return;
		}
		intent.setClass(DetaiInfoView.this, VodPlayer.class);
		startActivityForResult(intent, 0);
	}

	public void downLoaderVideo() {
		pop_dialog.showToast(R.string.not_support_down);

	}

	public void collectVideo() {

		if (bCollected) {
			favManager.remove(mcontent);

			if (mCurSourceType == SOURCE_TYPE.FAV.ordinal()) {
				
				if(!isSerialFragment){
					mFragment.updateData();
				}
				int size = VideoBrowser.getInstance().getCurrentList().size();
				if (size <= mCurSelectIndex
						|| size % (VideoBrowser.GRID_PAGECONTENTSIZE) == 0) {
					mCurSelectIndex--;
				}

				if (mCurSelectIndex < 0) {
					mHomeStart -= VideoBrowser.GRID_PAGECONTENTSIZE;
					mCurSelectIndex = VideoBrowser.GRID_PAGECONTENTSIZE - 1;
				}

				if (mHomeStart >= 0) {
					updateVideoInfo(mCurSelectIndex);
				}
				if(mcontent.getContentType() == DATA_TYPE.VIDEO){
					if(mFragment == null){
						setFragment(false);
					}else{
						mFragment.updateData();
					}
				}
			}
			bCollected = false;
			mCollect.setImageResource(R.drawable.detail_btn_favorite_n);

		} else if (favManager.getSize() >= StoreManager.MAX_COUNT) {
			pop_dialog.showToast(R.string.favorite_full);
			
		} else {
			favManager.add(mcontent);
			bCollected = true;
			mCollect.setImageResource(R.drawable.detail_btn_favorite_h);
		}
		
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		int callbackIndex = mCurSelectIndex;
		if (data != null) {
			callbackIndex = data.getIntExtra("lastPlayIndex", mCurSelectIndex);
		}

		if (isSerialFragment) {
			// the play index is not equal the index of lastSelected
			// index,update the serialAdapter when back from play.java,
			if (mCurSelectSerialVideoIndex != callbackIndex) {
				mCurSelectSerialVideoIndex = callbackIndex;
				setFragment(true);
			}
		} else {
			System.out.println("------ playerIndex = " + callbackIndex);
			System.out.println("------ mCurSelectIndex = " + mCurSelectIndex);
			if (mCurSourceType == SOURCE_TYPE.HISTORY.ordinal()) {
				mFragment.setFocusIndex(0);
				updateVideoInfo(0);
			} else if (callbackIndex != mCurSelectIndex) {
				updateVideoInfo(callbackIndex);
			}
			// pop_dialog.showAnimation();//Jie.jia 20140902 remove the loading
			// animation when exit from player.
		}
		mResoulte.setText(mResoulteSupportList[videoInfo
				.getCurrentResoutleIndex()]);
	};

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == spinnerPopWindow.UPDATE_RESOLUTE) {
				videoInfo.setCurrentResoutleIndex(msg.arg1);
				mResoulte.setText(mResoulteSupportList[msg.arg1]);
			} else if (msg.what == CoreHandler.Callback.CBK_GET_EXTRA_DONE
					.ordinal()) {
				Log.d(TAG, "---get the extra info -----");
				mcontent = (Content) msg.obj;
				
				mPlay.requestFocus();
				if (mcontent.getContentType() == DATA_TYPE.SERIAL) {
					isSerialFragment = true;
					mSerialInfo = (Serial) mcontent;
					mSerialInfo = (Serial) hisManager.getInCache(mSerialInfo);
					mCurSelectSerialVideoIndex = mSerialInfo.getLastPlayIndex();
					mTvVideoList.setText(R.string.album_list);
				} else if (mcontent.getContentType() == DATA_TYPE.VIDEO) {
					isSerialFragment = false;
					mVideoInfo = (Video) mcontent;
					mTvVideoList.setText(R.string.relate_list);
				}

				initUIContent(mcontent.getDescription(),mcontent.getLargePicURL());
				if(!isSerialFragment){
					getVideoTrueUrl();
				}else{
					pop_dialog.closeAnimation();
					setVideoInfoIntoVideoData(mCurSelectSerialVideoIndex);
					setFragment(isSerialFragment);
				}
			} else if (msg.what == CoreHandler.Callback.CBK_GET_EXTRA_FAIL
					.ordinal()) {
				pop_dialog.closeAnimation();

				if (WebManager.reportBug(msg.arg1, msg.what, (Content) msg.obj,
						backHome)) {
					return;
				}

				if (isSerialFragment == true) {
					// setFragment(isSerialFragment);
					pop_dialog.showWarning(R.string.getextendinfo_fail,
							backHome);
				} else {
					pop_dialog.showWarning(R.string.getextendinfo_fail, null);
				}
			} else if (msg.what == CoreHandler.Callback.CBK_GET_URL_DONE
					.ordinal()) {
				pop_dialog.closeAnimation();
				
				if(!isSerialFragment){
					setVideoInfoIntoVideoData(mCurSelectSerialVideoIndex);
				}else{
					playVideo();
				}

			} else if (msg.what == CoreHandler.Callback.CBK_GET_URL_FAIL
					.ordinal()) {
				pop_dialog.closeAnimation();

				if (!WebManager.reportBug(msg.arg1, msg.what,
						(Content) msg.obj, backHome)) {
					pop_dialog.showWarning(R.string.parse_url_error, null);
				}

			} else if (msg.what == CoreHandler.Callback.CBK_NETWORK_CONNECT_FAIL
					.ordinal()) {
				pop_dialog.closeAnimation();
				pop_dialog.showWarning(R.string.network_fail, null);

			} else if (msg.what == CoreHandler.Callback.CBK_REPORT_BUG_DONE
					.ordinal()) {
				pop_dialog.showToast(R.string.report_ok);
			}
		};
	};

	DialogInterface.OnClickListener backHome = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.putExtra("indexInDetail", mCurSelectIndex);
			setResult(100, intent);
			finish();
		}
	};

	@Override
	public boolean onHover(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_HOVER_ENTER:
			System.out.println(v + " ACTION_HOVER_ENTER");
			v.requestFocusFromTouch();
			if(v == mvideoinfo_descripe){
				mvideoinfo_descripe.scrollTo(0, 0);
			}
			break;
		case MotionEvent.ACTION_HOVER_EXIT:
			if(v == mvideoinfo_descripe){
				mvideoinfo_descripe.scrollTo(0, 0);
			}
			System.out.println(v + " ACTION_HOVER_EXIT");
			break;
		}
		return false;
	}
}

class spinnerPopWindow extends PopupWindow implements OnItemClickListener {

	private Activity mContext;
	private spinnerAdapter mAdapter;
	private ListView mListView;
	private String text[];
	private int preSelection = 0;
	private Button mView;
	private int mViewWidth, mViewHight;
	public static final int UPDATE_RESOLUTE = 20;
	private boolean isResolution = false;
	Handler handler;

	public spinnerPopWindow(Activity context, int preSelection,
			View ClickButton, String text[], Handler handler,boolean isResolution) {
		this.mContext = context;
		this.preSelection = preSelection;
		this.mView = (Button) ClickButton;
		this.text = text;
		this.handler = handler;
		this.isResolution = isResolution;
		mViewWidth = mView.getWidth();
		mAdapter = new spinnerAdapter();
	}

	public void showPopWindow(int weight, int height) {
		if (text.length > 2) {
			setViewWidth(weight, height * 3);
			showAsDropDown(mView, 11, (-(height + 2) * 4 - 10));
		} else {
			setViewWidth(weight, height * text.length);
			showAsDropDown(mView, 11, (-(height + 3) * (text.length + 1) - 10));
		}
	}

	public void setViewWidth(int width, int hight) {
		mViewWidth = width;
		mViewHight = hight;
		createPopupList();
	}

	private void createPopupList() {
		System.out.println("----- the height = " + mViewWidth
				+ " ----- weight = " + mViewHight);
		View spinner = null;
		if(isResolution){
			 spinner = LayoutInflater.from(mContext).inflate(
					R.layout.spiner_resoult_listview, null, false);
	
			setContentView(spinner);
			setWidth(mViewWidth);
			setHeight(LayoutParams.WRAP_CONTENT);
		}else{
			 spinner = LayoutInflater.from(mContext).inflate(
					R.layout.spiner_listview, null, false);
	
			setContentView(spinner);
			setWidth(mViewWidth);
			setHeight(mViewHight);
		}
		setFocusable(true);
		setBackgroundDrawable(new ColorDrawable(0x00));
		mListView = (ListView) spinner.findViewById(R.id.spinner_listview);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setSelection(preSelection);
		mListView.requestFocusFromTouch();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		// mView.setText(text[arg2]);
		Message msg = handler.obtainMessage();
		msg.what = UPDATE_RESOLUTE;
		msg.arg1 = arg2;
		msg.obj = mView;
		handler.sendMessage(msg);
		dismiss();
		mView.setSelected(false);

	}

	private class spinnerAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return text.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			mHold hold = null;
			if (arg1 == null) {
				hold = new mHold();
				arg1 = LinearLayout.inflate(mContext, R.layout.spinnertext,
						null);
				hold.tv = (TextView) arg1.findViewById(R.id.spinner_textview);
				hold.cb = (CheckBox) arg1.findViewById(R.id.spinner_checkbox);
				arg1.setOnHoverListener(onHoverListener);
				arg1.setTag(hold);
			} else {
				hold = (mHold) arg1.getTag();
			}
			if (arg0 == preSelection) {
				hold.tv.setTextColor(Color.RED);
				hold.cb.setVisibility(View.VISIBLE);
			}
			hold.tv.setText(text[arg0]);
			return arg1;
		}

		OnHoverListener onHoverListener = new OnHoverListener() {
			
			@Override
			public boolean onHover(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				switch(arg1.getAction()){
				case MotionEvent.ACTION_HOVER_ENTER:
					arg0.setHovered(true);
					break;
				case MotionEvent.ACTION_HOVER_EXIT:
					arg0.setHovered(false);
					break;
				}
				return false;
			}
		};
		class mHold {
			TextView tv;
			CheckBox cb;
		}

	}
}
