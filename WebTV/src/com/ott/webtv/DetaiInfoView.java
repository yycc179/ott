package com.ott.webtv;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.ott.webtv.SelectedVideoData.VideoPlayInfo;
import com.ott.webtv.core.ContentManager;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.StoreManager;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.DATA_TYPE;
import com.ott.webtv.core.DataNode.Serial;
import com.ott.webtv.core.DataNode.Video;
import com.ott.webtv.R;

@SuppressLint("NewApi")
public class DetaiInfoView extends Activity {

	private ImageButton mPlay,mDownLoader,mCollect;
	private TextView mvideoinfo_descripe,mFileName;
	private ImageView mVideoPrePic,mVideoType;
	private Button mResoulte;
	
	public int mOperateMode = 0;
	private boolean isSerialFragment = true;
	private Serial mSerialInfo;
	private Video mVideoInfo;
	
	private String[] mResoulteSupportList;
	private String mCurrentFilePath="";
	private Content mcontent;
	private ArrayList<HashMap<String,String>> mRelateVideoList;
	private int mCurSelectIndex = -1;
	private StoreManager<Content> favManager = StoreManager.getFavManager();
	private StoreManager<Content> hisManager = StoreManager.getHisManager();
	private Boolean bCollected = false;
//	private AlertDialog bufferDialog;
//	private View dialoglayout;
	private PopDialog pop_dialog = null;
	
//	private static int UPDATE_COLLECT_ICON = 21;
	public static int UPDATE_RESOULTE = 20;
	
	private final String TAG = "VideoDetailInfo";
	
	private VideoPlayInfo videoInfo = SelectedVideoData.VideoPlayInfo.getInstace();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videodetailinfo);
		
		Log.d(TAG, "onCreate");
		findViews();
		setViewListener();
		getIntentData();
		
		if (haveInited == false) {
			pop_dialog.showAnimation();
			new Thread() {
				public void run() {
					{
						updateVideoInfo(getIntent().getIntExtra("selectIndex",0)); // show the video info of selected
						if (mcontent.getContentType() == DATA_TYPE.SERIAL) {
							isSerialFragment = true;
						} else if (mcontent.getContentType() == DATA_TYPE.VIDEO) {
							isSerialFragment = false;
						}
						setFragment(isSerialFragment);
					}

				};
			}.start();
			haveInited = true;
		}
	}
	
	@SuppressWarnings("unchecked")
    private void getIntentData(){
    	mRelateVideoList = (ArrayList<HashMap<String,String>>)getIntent().getSerializableExtra("curPageList");
		
    	switch((getIntent().getIntExtra("sourceType", 0))){
    	case 0:
    		mVideoType.setImageResource(R.drawable.new_ott_vod_sel);
    		break;
    	case 1:
    		mVideoType.setImageResource(R.drawable.new_ott_live_sel);
    		break;
    	case 2:
    		mVideoType.setImageResource(R.drawable.new_ott_history_sel);
    		break;
    	case 3:
    		mVideoType.setImageResource(R.drawable.new_ott_favorite_sel);
    		break;
    	case 4:
    		mVideoType.setImageResource(R.drawable.new_tittleicon_search);
    		break;
    	default:
    			break;
    	}
    	Log.d(TAG, "start Time = "+ System.currentTimeMillis());
    	
    }
		
	boolean haveInited = false;
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, "OnResume");
		CoreHandler.getInstace().setHandler(handler);
		Log.d(TAG, "start Time = " + System.currentTimeMillis());
	}
	
	public void setVideoInfoIntoVideoData(int CurrentSelectIndex){
		ArrayList<Integer> resoulteList = new ArrayList<Integer>(); 
		if(isSerialFragment == true){
			videoInfo.setCurrentVideoResoulteAndUrl(CurrentSelectIndex, mSerialInfo);
			resoulteList = videoInfo.getCurrentVideoResoulte();
		}else{
			videoInfo.setCurrentVideoResoulteAndUrl(mVideoInfo);
			resoulteList = videoInfo.getCurrentVideoResoulte();
		}
		mResoulte.setText(getResources().getString(resoulteList.get(videoInfo.getCurrentResoutleIndex())));
		mResoulteSupportList = new String[resoulteList.size()];
		for(int i = 0;i < resoulteList.size();i++){
			if(resoulteList.get(0) != 0){
				mResoulteSupportList[i] = getResources().getString(resoulteList.get(i));
				System.out.println(" mResoulteSupportList[i]"+"-------"+mResoulteSupportList[i]);
			}
		}
		mCurrentFilePath = mRelateVideoList.get(mCurSelectIndex).get("videoName");
		videoInfo.setResoulteList(mResoulteSupportList);
		videoInfo.setVideoName(mCurrentFilePath);
		
		if(favManager.isExist(mcontent)){
			bCollected = true;
			mCollect.setImageResource(R.drawable.new_ott_detail_btn_favorite_h);
		}else{
			bCollected = false;
			mCollect.setImageResource(R.drawable.new_ott_detail_btn_favorite_n);
		}
	}
	
	private void initUIContent(String descripe,String largePic){
		ImageLoader mGetImage;
		mGetImage = new ImageLoader();  
		mGetImage.setDownLoaderImageView(mVideoPrePic);
		mGetImage.setImageFromUrl(largePic);
		
		mFileName.setText(mRelateVideoList.get(mCurSelectIndex).get("videoName"));
		mvideoinfo_descripe.setText(descripe);
		mvideoinfo_descripe.setMovementMethod(ScrollingMovementMethod.getInstance());
		mvideoinfo_descripe.setVerticalScrollBarEnabled(false);
		
		if((mvideoinfo_descripe.getLineCount() / 10) < 1){
			mvideoinfo_descripe.setFocusable(false);
			mvideoinfo_descripe.setClickable(false);
		}
		
	}
	private void findViews(){
		mVideoType = (ImageView) findViewById(R.id.videoType);
		mPlay = (ImageButton) findViewById(R.id.play);
		mDownLoader = (ImageButton) findViewById(R.id.downLoader);
		mCollect = (ImageButton) findViewById(R.id.collect);
		mResoulte = (Button) findViewById(R.id.resolution);
		mVideoPrePic = (ImageView) findViewById(R.id.detail_videoPreView);
		mFileName = (TextView) findViewById(R.id.filmName);
		mvideoinfo_descripe = (TextView) findViewById(R.id.videoinfo_descripe);
		descripe_frame = findViewById(R.id.videoinfo_descripe_frame);
		mPlay.requestFocus();
		pop_dialog = new PopDialog(DetaiInfoView.this, null);
	}
	
	private int curTextLineCount = 0;
	
	private View descripe_frame;
	private void setViewListener(){
		
			System.out.println("-------- unFocus -----");
			mvideoinfo_descripe.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View arg0, boolean arg1) {
					// TODO Auto-generated method stub
					if(arg1){
						
						curTextLineCount = 0;
						mvideoinfo_descripe.scrollTo(0, 0);
						mvideoinfo_descripe.setTextColor(Color.BLUE);
						descripe_frame.setVisibility(View.VISIBLE);
						
					}else{
						descripe_frame.setVisibility(View.GONE);
						mvideoinfo_descripe.scrollTo(0, 0);
						mvideoinfo_descripe.setTextColor(Color.BLACK);
						mvideoinfo_descripe.setMovementMethod(null);
						mvideoinfo_descripe.setFocusable(true);
						mvideoinfo_descripe.setClickable(true);
					}
				}
			});
			
		mvideoinfo_descripe.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
					if (arg1 == KeyEvent.KEYCODE_DPAD_UP) {
						if (curTextLineCount > 0) {
							curTextLineCount--;
							mvideoinfo_descripe.scrollTo(0,300 * (curTextLineCount));
							return true;
						}
					} else if (arg1 == KeyEvent.KEYCODE_DPAD_DOWN) {
						if (curTextLineCount < mvideoinfo_descripe.getLineCount() / 10) {
							curTextLineCount++;
							mvideoinfo_descripe.scrollTo(0,300 * (curTextLineCount));
							return true;
						}
					}
				}
				return false;
			}
		});
		
		mPlay.setOnFocusChangeListener(buttonOnFocus);
		mDownLoader.setOnFocusChangeListener(buttonOnFocus);
		mCollect.setOnFocusChangeListener(buttonOnFocus);
		
		mPlay.setOnClickListener(buttonClick);
		mDownLoader.setOnClickListener(buttonClick);
		mCollect.setOnClickListener(buttonClick);
		mResoulte.setOnClickListener(buttonClick);
		
//		pop_dialog.setListener(null, null);
	}
	
	OnFocusChangeListener buttonOnFocus = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			// TODO Auto-generated method stub
			if((mPlay == arg0)||(mDownLoader == arg0)||(mCollect == arg0)||(mResoulte == arg0)){
				if(arg1){
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
			if(arg0 == mPlay){
				if (CoreHandler.getInstace().isNetworkConnected(DetaiInfoView.this)) {
					playVideo();
				}else{
					pop_dialog.showWarning(getResources().getString(R.string.network_fail), null);					
				}	
			}else if(arg0 == mDownLoader){
				downLoaderVideo();
			}else if(arg0 == mCollect){
				collectVideo();
			}else if(arg0 == mResoulte){
				int weight = arg0.getWidth()-22;
				int height = arg0.getHeight()-18;
				spinnerPopWindow myPopList = new spinnerPopWindow(DetaiInfoView.this, 1, mResoulte,mResoulteSupportList,handler);
				myPopList.showPopWindow(weight,height);
			}
		}
	};
	
	private void setFragment(boolean isSerialFragment){
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Bundle bundler = new Bundle();
		bundler.putBoolean("isPlayer", false);
		if(isSerialFragment){
			AlbumFragment fg = AlbumFragment.newInstance();
			bundler.putInt("totalCount", 80);
			bundler.putInt("currentSelectItem", mCurrentSelectVideoIndex);
			fg.setArguments(bundler);
			ft.replace(R.id.fragment_layout, fg);
		}else{
			RelateFragment fg = RelateFragment.newInstance();
			fg.setArguments(bundler);
			ft.replace(R.id.fragment_layout, fg);
		}
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.d(TAG,""+keyCode);
		if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
			if(isSerialFragment){
				if(mPlay.hasFocus()){
					mPlay.setSelected(true);
					setCurrentOperatMode(0);
				}else if(mDownLoader.hasFocus()){
					mDownLoader.setSelected(true);
					setCurrentOperatMode(1);
				}else if(mCollect.hasFocus() || mResoulte.hasFocus()){
					return true;
				}
			}
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			if(mPlay.hasFocus()){
				return true;
			}
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			if(mResoulte.hasFocus()){
				return true;
			}
		}else if(keyCode == KeyEvent.KEYCODE_BACK){
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
		Log.d(TAG,"onStop");
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		Log.d(TAG,"onDestroy");
	}
	public void setCurrentOperatMode(int mode){
		mOperateMode = mode;
	}
	
	private int mCurrentSelectVideoIndex = 1;
	
	public void setCurrentSelectItem(int currentPosition){
		mCurrentSelectVideoIndex = currentPosition;
	}
	public void doForFragmentSelectedItem(){
		
		if(mOperateMode == 0){
			playVideo();
		}else if(mOperateMode == 1){
			downLoaderVideo();
		}else if(mOperateMode == 2){
			collectVideo();
		}
	}
	
	public void updateVideoInfo(int videoIndex){
		if(mCurSelectIndex == videoIndex) 
			return;
		if(getIntent().getIntExtra("sourceType", 0) == 2){
			mcontent = hisManager.get(videoIndex);
		}else if(getIntent().getIntExtra("sourceType", 0) == 3){
			mcontent = favManager.get(videoIndex);
		}else{
			if(ContentManager.getCurrent() != null){
				mcontent = ContentManager.getCurrent().getNode(videoIndex);
			}else{
				System.out.println("---ContentManager.getCurrent() = null -----");
			}
		}

//		handler.sendEmptyMessage(UPDATE_COLLECT_ICON);// used for update the collect icon
		CoreHandler.getInstace().setHandler(handler);
		CoreHandler.getInstace().loadExtraInfo(mcontent);
				
//		mCurrentFilePath = getIntent().getStringExtra("filePath")+"/"+mRelateVideoList.get(videoIndex).get("videoName");
		mCurrentFilePath = mRelateVideoList.get(videoIndex).get("videoName");
		mCurSelectIndex = videoIndex;
	}
	
	public void playVideo(){
		
		Intent intent = getIntent();
		intent.putExtra("videoType", isSerialFragment); // serial or video 
		
		if(isSerialFragment == true){
			intent.putExtra("videoContent", mSerialInfo); // the content of the select
			intent.putExtra("currentPlayIndex", mCurrentSelectVideoIndex);  // the index of the serial
		}else{
			intent.putExtra("videoContent", mVideoInfo); // the content of the select
			intent.putExtra("currentPlayIndex", mCurSelectIndex); // the index of the list in the videobrowser 
		}
		intent.setClass(DetaiInfoView.this, VodPlayer.class);
		startActivityForResult(intent, 0);
	}
	
	public void downLoaderVideo(){
		pop_dialog.showToast(R.string.not_support_down);

	}
	
	public void collectVideo(){
		int str = R.string.collect_success;
		int img = R.drawable.new_ott_detail_btn_favorite_h;
		if(bCollected){
			favManager.remove(mcontent);
			str = R.string.cancel_collect;
			img = R.drawable.new_ott_detail_btn_favorite_n;
		}else{
			favManager.add(mcontent);
		}
		bCollected = !bCollected;
		mCollect.setImageResource(img);
		pop_dialog.showToast(str);
	}
	
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	
	int callbackIndex = mCurSelectIndex;
	if(data != null){
		callbackIndex = data.getIntExtra("lastPlayIndex", mCurSelectIndex);
	}
	if(isSerialFragment){
		// the play index is not equal the index of into play.java,update the serialAdapter when back from play.java,
		if(mCurrentSelectVideoIndex != callbackIndex){
			mCurrentSelectVideoIndex = callbackIndex;
			setFragment(true);
		}
	}else{
		if(callbackIndex != mCurSelectIndex){
			// update the info to the video ,selected in the play activity
			pop_dialog.showAnimation();
			updateVideoInfo(callbackIndex);
		}
	}
};
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(msg.what == UPDATE_RESOULTE){
				videoInfo.setCurrentResoutleIndex(msg.arg1);
				
			}else if(msg.what == CoreHandler.Callback.CBK_GET_EXTRA_DONE.ordinal()){
				Log.d(TAG, "in handler the msg");
				String mDetailDescripe="";
				pop_dialog.closeAnimation();
				mPlay.requestFocus();
				if(isSerialFragment == true){
					 mSerialInfo = (Serial)mcontent;
					 mDetailDescripe = mSerialInfo.getDescription();
				}else if(isSerialFragment == false){
					 mVideoInfo = (Video)mcontent;
					 mDetailDescripe = mVideoInfo.getDescription();
				}
				initUIContent(mDetailDescripe,mcontent.getLargePicURL());
				setVideoInfoIntoVideoData(mCurrentSelectVideoIndex);
				
			}else if(msg.what == CoreHandler.Callback.CBK_GET_EXTRA_FAIL.ordinal()){
				pop_dialog.closeAnimation();
				
			}
		};
	};
}

class spinnerPopWindow extends PopupWindow implements OnItemClickListener{
	
	private Context mContext;  
	private spinnerAdapter mAdapter;
	private ListView mListView ;
	private String text[];
	private int preSelection = 0;
	private Button mView;
	private int mViewWidth,mViewHight;
	Handler handler;
	public spinnerPopWindow(Context context,int preSelection,View ClickButton,String text[],Handler handler){
		this.mContext = context;
		this.preSelection = preSelection;
		this.mView = (Button)ClickButton;
		this.text = text;
		this.handler = handler;
		mViewWidth = mView.getWidth();
		mAdapter = new spinnerAdapter();
	}
	public void showPopWindow(int weight,int height){
		if(text.length > 2){
			setViewWidth(weight,height*3);
			showAsDropDown(mView, 11, (-height* 4-5));
		}else{
			setViewWidth(weight,height*text.length);
			showAsDropDown(mView, 11, (-height*text.length-height-5));
		}
	}
	public void setViewWidth(int width,int hight){
		mViewWidth = width;
		mViewHight = hight;
		createPopupList();
	}
	private void createPopupList() {
		System.out.println("------ in createpoplist -------");
		System.out.println("--????--- the height = "+mViewWidth+" ----- weight = "+mViewHight);
		View spinner = LayoutInflater.from(mContext).inflate(
				R.layout.spiner_listview, null, false);
		
		setContentView(spinner);
		setWidth(mViewWidth);
		setHeight(mViewHight);
		setFocusable(true);
		setBackgroundDrawable(new ColorDrawable(0x00));
		mListView = (ListView) spinner.findViewById(R.id.spinner_listview);
		mListView.setAdapter(mAdapter);	
		mListView.setOnItemClickListener(this);
		mListView.setSelection(preSelection);
		}
	

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		mView.setText(text[arg2]);
		Message msg = handler.obtainMessage();
		msg.what = DetaiInfoView.UPDATE_RESOULTE;
		msg.arg1 = arg2;
		msg.obj = mView;
		handler.sendMessage(msg);
		dismiss();
		mView.setSelected(false);
		
	}
		
	private class spinnerAdapter extends BaseAdapter{
		
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
			if(arg1 == null){
				hold = new mHold();
				System.out.println("------ in getview -------");
				arg1 = LinearLayout.inflate(mContext, R.layout.spinnertext, null);
				hold.tv = (TextView) arg1.findViewById(R.id.spinner_textview);
//				hold.cb = (CheckBox) arg1.findViewById(R.id.spinner_checkbox);
				arg1.setTag(hold);
			}else{
				hold = (mHold) arg1.getTag();
			}
			if(arg0 == preSelection){
				hold.tv.setTextColor(Color.RED);
				//hold.cb.setVisibility(View.VISIBLE);
			}
			hold.tv.setText(text[arg0]);
			return arg1;
		}
		
		class mHold{
			TextView tv;
//			CheckBox cb;
		}
		
	}
}
