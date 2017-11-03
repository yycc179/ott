package com.ott.webtv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageView;


@SuppressLint({ "NewApi", "ValidFragment" })
public class AlbumFragment extends Fragment {
	
	private GridView gd_serial_list;
	private GridView gd_gather_list;
	private ImageView mPageback_Serial,mPagedown_Serial;
	private ImageView mPageback_Gather,mPagedown_Gather;
	private Context mcontext;
	private AlbumGridAdapter serialGdAdpater;
	private AlbumGridAdapter gatherGdAdpater;
	private int mGatherTotalNum,mserialTotalNum; // 1------N   10:10   
	private int mGatherTotalPageNum;// 0 -----N   50:0   51:1
	private int lastPlayIndex;
	private int mCurSelectSerialNum,mCurSelectGatherNum; // 0 ---- 9   // 0 -- 4
	
	private final String GATHER = "gather";
	private final String SINGLE = "single";
	private final int SINGLECOLUMNUM = 10;
	private final int GATHERCOLUMNUM = 5;
	private final int PAGETOTALNUM = (SINGLECOLUMNUM*GATHERCOLUMNUM);
	
	private int mSelectIndex = 0; // the video album,from 1 to the end.
	private Activity mActivity;
	private boolean isPlayerFragment;
	
	public AlbumFragment(){
		System.out.println(">>>>>>>  in here  AlbumFragment <<<<<<");
	}
//	public AlbumFragment(boolean isPlayer,Context context,int TotalNum,int lastPlayIndex){
//		this.mcontext = context;
//		this.mserialTotalNum = TotalNum;
//		this.lastPlayIndex = lastPlayIndex;
//		this.isPlayerFragment = isPlayer;
//		
//
//	}
	public static AlbumFragment newInstance(){
		final AlbumFragment fg = new AlbumFragment();
		return fg;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView;
		rootView = inflater.inflate(R.layout.albumlistfragment, null, false);
		return rootView;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if(args != null){
			isPlayerFragment = args.getBoolean("isPlayer");
			this.mserialTotalNum = args.getInt("totalCount");
			this.lastPlayIndex = args.getInt("currentSelectItem");
			System.out.println("---totalnum = "+mserialTotalNum+"---lastPlayIndex = "+lastPlayIndex+"-----");
		}
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		if(!isPlayerFragment)
			mActivity = (DetaiInfoView) getActivity();
		else{
			mActivity = (Player) getActivity();
		}
		 
		findViews();
		initFragment();
		setLastPlayIndex();

		setGridViewListener();
	}

	private void findViews(){
		gd_serial_list = (GridView) mActivity.findViewById(R.id.detail_serial_1);
		gd_gather_list = (GridView) mActivity.findViewById(R.id.detail_serial_2);
		
		mPageback_Serial = (ImageView) mActivity.findViewById(R.id.pageback_line1);
		mPagedown_Serial = (ImageView) mActivity.findViewById(R.id.pagedown_line1);
		mPageback_Gather = (ImageView) mActivity.findViewById(R.id.pageback_line2);
		mPagedown_Gather = (ImageView) mActivity.findViewById(R.id.pagedown_line2);
	}

	private void initFragment(){
		
		mCurSelectSerialNum = 0;
		mCurSelectGatherNum = 0;
		
		int a = mserialTotalNum % SINGLECOLUMNUM;
		if (a > 0)
			mGatherTotalNum = mserialTotalNum/SINGLECOLUMNUM + 1;
		else
			mGatherTotalNum = mserialTotalNum/SINGLECOLUMNUM;
		
		mGatherTotalPageNum = mserialTotalNum/PAGETOTALNUM;
		if(mserialTotalNum % PAGETOTALNUM == 0){
			mGatherTotalPageNum = mGatherTotalPageNum-1;
		}
		
		serialGdAdpater = new AlbumGridAdapter();
		serialGdAdpater.setAdapterTotalNum(mcontext,mserialTotalNum);
		gatherGdAdpater = new AlbumGridAdapter();
		gatherGdAdpater.setAdapterTotalNum(mcontext,mserialTotalNum);
		
		mPageback_Serial.setVisibility(View.INVISIBLE);
		mPagedown_Serial.setVisibility(View.INVISIBLE);
		mPageback_Gather.setVisibility(View.INVISIBLE);
		mPagedown_Gather.setVisibility(View.INVISIBLE);
		
		if(mserialTotalNum > SINGLECOLUMNUM){
			mPagedown_Serial.setVisibility(View.VISIBLE);
		}
		if(mGatherTotalNum > GATHERCOLUMNUM){
			mPagedown_Gather.setVisibility(View.VISIBLE);
		}
	}
	private void setLastPlayIndex(){
		
		int SelectSerialNum = lastPlayIndex%SINGLECOLUMNUM-1; // the selected index in current page  from 0 ---- 9
		int CurSelectGatherNum = lastPlayIndex/SINGLECOLUMNUM; // the selected index in current page  from 0(0--10) ---- 4
		
		gatherCurrentPageNum = lastPlayIndex/PAGETOTALNUM; // the selected gather page  from 0(0 -- 50) --- N
		if(lastPlayIndex%PAGETOTALNUM == 0){
			gatherCurrentPageNum = gatherCurrentPageNum-1;
		}
		if(SelectSerialNum == -1){ // 
			SelectSerialNum = SINGLECOLUMNUM-1;
			CurSelectGatherNum = CurSelectGatherNum -1;
		}
		if(CurSelectGatherNum > 0){
			mPageback_Serial.setVisibility(View.VISIBLE);
			if(CurSelectGatherNum >= GATHERCOLUMNUM){
				mPageback_Gather.setVisibility(View.VISIBLE);
				mPagedown_Gather.setVisibility(View.VISIBLE);
			}
			if(CurSelectGatherNum == mGatherTotalNum-1){
				mPagedown_Serial.setVisibility(View.INVISIBLE);
			}
			if(mGatherTotalPageNum == gatherCurrentPageNum){
				mPagedown_Gather.setVisibility(View.INVISIBLE);
			}
		}
		serialCurrentPageNum = CurSelectGatherNum;
		mCurSelectGatherNum = CurSelectGatherNum%5;
		
		mCurSelectSerialNum = SelectSerialNum;
		
		
		serialGdAdpater.setSerialContent(SINGLE, serialCurrentPageNum*SINGLECOLUMNUM);
		serialGdAdpater.setLastFocusItem(SelectSerialNum,true);
		gd_serial_list.setAdapter(serialGdAdpater);
		
		gatherGdAdpater.setGatherContent(GATHER,gatherCurrentPageNum);
		gatherGdAdpater.setLastFocusItem(mCurSelectGatherNum);
		gd_gather_list.setAdapter(gatherGdAdpater);
		
		if(!isPlayerFragment){
			((DetaiInfoView) mActivity).setCurrentSelectItem(lastPlayIndex);
		}
	}
	
	private void setGridViewListener(){
		gd_serial_list.setOnItemClickListener(gd_SerialOnItemClick);
		
		gd_serial_list.setOnKeyListener(UserKeyListener);

		gd_serial_list.setOnItemSelectedListener(serialListSelectListener);
		
		gd_gather_list.setOnKeyListener(UserKeyListener);
		gd_serial_list.setOnFocusChangeListener(gridViewOnFocus);
		gd_gather_list.setOnFocusChangeListener(gridViewOnFocus);
		gd_gather_list.setOnItemSelectedListener(gatherItemSelect);
		
		GusetSupport gs = new GusetSupport();
		gd_serial_list.setOnTouchListener(gs.onTouchListener);
		gd_gather_list.setOnTouchListener(gs.onTouchListener);
		gd_gather_list.setOnItemClickListener(gd_GatherOnItemClick);
	}

	int isSerialOnFouces = 0;
	OnFocusChangeListener gridViewOnFocus = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			// TODO Auto-generated method stub
			if (arg0 == gd_serial_list) {
				if (arg1) {
					isSerialOnFouces = 1;
					int focusIndex = mCurSelectSerialNum;
					serialGdAdpater.setLastFocusItem(-1,true);
					gd_serial_list.setAdapter(serialGdAdpater);
					gd_serial_list.setSelection(focusIndex);
					
				} else {
					
					serialGdAdpater.setLastFocusItem(mCurSelectSerialNum,true);
					serialGdAdpater.notifyDataSetChanged();
				}
			} else if (arg0 == gd_gather_list) {
				if (arg1) {
					int focusIndex = mCurSelectGatherNum;
					gatherGdAdpater.setLastFocusItem(-1);
					gd_gather_list.setAdapter(gatherGdAdpater);
					gd_gather_list.setSelection(focusIndex);
				} else {
					gatherGdAdpater.setLastFocusItem(mCurSelectGatherNum);
					gatherGdAdpater.notifyDataSetChanged();
				}
			}
		}
	};

	/**
	 * 
	 * @param arg2  the gather current num 0-----N
	 */
	private void updateSerialInfo(int arg2,boolean bIsIR){
		System.out.println("------- the arg2 = "+arg2);
		mCurSelectGatherNum = arg2;
		mCurSelectSerialNum = 0;
		mPageback_Serial.setVisibility(View.VISIBLE);
		mPagedown_Serial.setVisibility(View.VISIBLE);
		System.out.println("-----111--- the serial info serialCurrentPageNum = "+serialCurrentPageNum);
		serialCurrentPageNum = gatherCurrentPageNum*GATHERCOLUMNUM + arg2;
		System.out.println("------22222-- the serial info serialCurrentPageNum = "+serialCurrentPageNum);
		if((gatherCurrentPageNum*GATHERCOLUMNUM + arg2+1) == mGatherTotalNum){
			mPagedown_Serial.setVisibility(View.INVISIBLE);
		}
		if(gatherCurrentPageNum ==0 && arg2 == 0){
			mPageback_Serial.setVisibility(View.INVISIBLE);
		}
		serialGdAdpater.setSerialContent(SINGLE, (gatherCurrentPageNum*GATHERCOLUMNUM + arg2)*SINGLECOLUMNUM);
		serialGdAdpater.setLastFocusItem(0,bIsIR);
		gd_serial_list.setAdapter(serialGdAdpater);
		gd_serial_list.setSelection(0);
		
	}
	/**
	 * 
	 * @param toRight  shift to right??
	 */
	private void updateGatherInfo(boolean bTurnToRight,boolean isGatherNeedSelected){
		
		if(bTurnToRight){
			if(mCurSelectGatherNum == GATHERCOLUMNUM-1){
				gatherCurrentPageNum = gatherCurrentPageNum+1;
				gatherGdAdpater.setGatherContent(GATHER,gatherCurrentPageNum);
				gatherGdAdpater.setLastFocusItem(0,isGatherNeedSelected);
				gd_gather_list.setAdapter(gatherGdAdpater);
				mPageback_Gather.setVisibility(View.VISIBLE);
				if(gatherCurrentPageNum == mGatherTotalPageNum)
					mPagedown_Gather.setVisibility(View.INVISIBLE);
				mCurSelectGatherNum = 0;
			}else{
				mCurSelectGatherNum = mCurSelectGatherNum+1;
				gatherGdAdpater.setGatherContent(GATHER, gatherCurrentPageNum);
				gatherGdAdpater.setLastFocusItem(mCurSelectGatherNum);
				gd_gather_list.setAdapter(gatherGdAdpater);
			}
		}else{
			if(mCurSelectGatherNum == 0){
				gatherCurrentPageNum = gatherCurrentPageNum-1;
				gatherGdAdpater.setGatherContent(GATHER,gatherCurrentPageNum);
				mCurSelectGatherNum = GATHERCOLUMNUM-1;
				gatherGdAdpater.setLastFocusItem(mCurSelectGatherNum,isGatherNeedSelected);
				gd_gather_list.setAdapter(gatherGdAdpater);
				gd_gather_list.setSelection(mCurSelectGatherNum);
				
				mPagedown_Gather.setVisibility(View.VISIBLE);
				if(gatherCurrentPageNum < 1)
					mPageback_Gather.setVisibility(View.INVISIBLE);
				
			}else{
					mCurSelectGatherNum = mCurSelectGatherNum-1;
					gatherGdAdpater.setGatherContent(GATHER,gatherCurrentPageNum);
					gatherGdAdpater.setLastFocusItem(mCurSelectGatherNum);
					gd_gather_list.setAdapter(gatherGdAdpater);
			}
		}
	}
	private int serialCurrentPageNum = 0;
	private int gatherCurrentPageNum = 0;
	
	OnKeyListener UserKeyListener = new OnKeyListener(){
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			if(arg2.getAction() == KeyEvent.ACTION_DOWN){
				
				if(gd_serial_list.hasFocus()){
					if(arg1 == KeyEvent.KEYCODE_DPAD_RIGHT){
						System.out.println("---------serialCurrentPageNum = "+serialCurrentPageNum);
						System.out.println("---------mserialTotalNum = "+mserialTotalNum);
					if((gd_serial_list.getSelectedItemPosition() == SINGLECOLUMNUM-1)&&((serialCurrentPageNum*SINGLECOLUMNUM)+
							gd_serial_list.getSelectedItemPosition()+1)<mserialTotalNum)
							{
								serialCurrentPageNum++;
								updateGatherInfo(true,true);
								updateSerialInfo(serialCurrentPageNum%5,false);
								
								return true;
							}
						
					}else if(arg1 == KeyEvent.KEYCODE_DPAD_LEFT){
						if((gd_serial_list.getSelectedItemPosition() == 0) && (serialCurrentPageNum > 0)){
							serialCurrentPageNum--;
							updateGatherInfo(false,true);
							updateSerialInfo(serialCurrentPageNum%5,false);
							gd_serial_list.setSelection(SINGLECOLUMNUM-1);
							mSelectIndex = (gatherCurrentPageNum*GATHERCOLUMNUM+mCurSelectGatherNum)*SINGLECOLUMNUM+1;
							return true;
						}
					}
				}else if(gd_gather_list.hasFocus()){
					if(arg1 == KeyEvent.KEYCODE_DPAD_RIGHT){
						isNeedUpdateSerialInfo = true;
						if((gd_gather_list.getSelectedItemPosition() == GATHERCOLUMNUM-1)&&(((gatherCurrentPageNum+1)*GATHERCOLUMNUM) < mGatherTotalNum))
						{
							mCurSelectGatherNum = GATHERCOLUMNUM-1;
							updateGatherInfo(true,false);
							return true;
						}
						
					}else if(arg1 == KeyEvent.KEYCODE_DPAD_LEFT){
						isNeedUpdateSerialInfo = true;
						if((gd_gather_list.getSelectedItemPosition() == 0) && (gatherCurrentPageNum > 0)){
							mCurSelectGatherNum = 0;
							updateGatherInfo(false,false);
							return true;
						}
					}else if(arg1 == KeyEvent.KEYCODE_DPAD_UP) {
						if(gd_gather_list.hasFocus()){
							 gd_serial_list.requestFocus();
							return true;
						}
					}
				}
			}else if(arg2.getAction() == KeyEvent.ACTION_UP){
				if(gd_serial_list.hasFocus() || gd_gather_list.hasFocus()){
					if((arg1 == KeyEvent.KEYCODE_DPAD_LEFT)||(arg1 == KeyEvent.KEYCODE_DPAD_RIGHT))
						return true;
				}
			}
			return false;
		}
	};
	
	private boolean isNeedUpdateSerialInfo = false;
	OnItemSelectedListener gatherItemSelect = new OnItemSelectedListener(){
		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
				if(gd_gather_list.hasFocus()){
					if(isNeedUpdateSerialInfo){
						updateSerialInfo(arg2,true);
						isNeedUpdateSerialInfo = false;
					}
				}
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
	};
	
	OnItemClickListener gd_GatherOnItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
				System.out.println("-----gather on click arg2 = "+arg2);
				updateSerialInfo(arg2,false);
				gatherGdAdpater.setGatherContent(GATHER,gatherCurrentPageNum);
				gatherGdAdpater.setLastFocusItem(arg2);
				gd_gather_list.setAdapter(gatherGdAdpater);
		}
	};
	
	OnItemClickListener gd_SerialOnItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			if(!isPlayerFragment){
				System.out.println("-----serial on click arg2 = "+arg2);
				mSelectIndex = (gatherCurrentPageNum*GATHERCOLUMNUM+mCurSelectGatherNum)*SINGLECOLUMNUM+arg2+1;
				((DetaiInfoView) mActivity).setCurrentSelectItem(mSelectIndex);
				((DetaiInfoView) mActivity).doForFragmentSelectedItem();
				if(((DetaiInfoView) mActivity).mOperateMode == 2){
					//mVideoData.notifyAdapterCollectDataChange();
				}
			}else{
				((VodPlayer)mActivity).playSelectedVideo(mSelectIndex);
			}
		}
	};

	OnItemSelectedListener serialListSelectListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			
			mCurSelectSerialNum = arg2;
			if(isSerialOnFouces == 1){
				isSerialOnFouces = 0;
				return;
			}
			mSelectIndex = (gatherCurrentPageNum*GATHERCOLUMNUM+mCurSelectGatherNum)*SINGLECOLUMNUM+arg2+1;
			if(!isPlayerFragment){
				((DetaiInfoView) mActivity).setCurrentSelectItem(mSelectIndex);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	};
	
	class GusetSupport {
		// use to support mouse
		int startX = 0, startY = 0, endX = 0, endY = 0;
		OnTouchListener onTouchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				boolean mouseEnd = false;
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startX = (int) arg1.getX();
					startY = (int) arg1.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_UP:
					mouseEnd = true;
					endX = (int) arg1.getX();
					endY = (int) arg1.getY();
					break;
				default:
					break;
				}
				if (Math.abs(endX - startX) < 20
						&& Math.abs(endY - startY) < 20 && mouseEnd) {
					System.out.println("-----  Item OnClick -------");
					return false;
				} else if (mouseEnd) {
					System.out.println("-----  page change -------");
					dealGesture(arg0, endX - startX, endY - startY);
					return true;
				} else {
					System.out.println("-----  do nothing -------");
					return false;
				}
			}
		};

		private void dealGesture(View view, int offX, int offY) {
			System.out.println("--------offX = " + offX);
			System.out.println("--------offY = " + offY);
			if (view == gd_serial_list) {
				System.out.println("--------- gd_serial_list ---------");
				if (Math.abs(offX) > 200) {
					if (offX > 0) {
						if (serialCurrentPageNum > 0) {
							serialCurrentPageNum--;
							updateGatherInfo(false, true);
							updateSerialInfo(serialCurrentPageNum % 5, false);
						}
					} else {
						if (((serialCurrentPageNum + 1) * SINGLECOLUMNUM) < mserialTotalNum) {
							serialCurrentPageNum++;
							updateGatherInfo(true, true);
							updateSerialInfo(serialCurrentPageNum % 5, false);
						}
					}
				}
			} else if (view == gd_gather_list) {

				if (Math.abs(offX) > 200) {
					if (offX > 0) {
						if ((gatherCurrentPageNum > 0)) {
							mCurSelectGatherNum = 0;
							updateGatherInfo(false, true);
							updateSerialInfo(GATHERCOLUMNUM - 1, false);
						}
					} else {
						if (((gatherCurrentPageNum + 1) * GATHERCOLUMNUM) < mGatherTotalNum) {
							mCurSelectGatherNum = GATHERCOLUMNUM - 1;
							updateGatherInfo(true, true);
							updateSerialInfo(0, false);
						}
					}
				}
			}
		}
	}
}
