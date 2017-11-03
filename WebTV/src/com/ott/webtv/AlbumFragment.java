package com.ott.webtv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


@SuppressLint({ "NewApi", "ValidFragment" })
public class AlbumFragment extends Fragment {
	
	private GridView gd_serial_list;
	private GridView gd_gather_list;
	private ImageView mPageback_Serial,mPagedown_Serial;
	private ImageView mPageback_Gather,mPagedown_Gather;
	private AlbumGridAdapter serialGdAdpater;
	private AlbumGridAdapter gatherGdAdpater;
	private int mGatherTotalNum,mserialTotalNum; // 1------N   10:10   
	private int lastPlayIndex;
	private int mCurSelectSerialNum; // 0 ---- 9   // 0 -- 4
	private int serialCurrentPageNum = 0;
	
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
	public static AlbumFragment newInstance(){
		final AlbumFragment fg = new AlbumFragment();
		return fg;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView;
		rootView = inflater.inflate(R.layout.detail_albumlistfragment, null, false);
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
			mActivity =  (DetaiInfoView) getActivity();
		else{
			mActivity = (Player) getActivity();
		}
		 
		gd_serial_list = (GridView) mActivity.findViewById(R.id.detail_serial_1);
		gd_gather_list = (GridView) mActivity.findViewById(R.id.detail_serial_2);
		
		mPageback_Serial = (ImageView) mActivity.findViewById(R.id.pageback_line1);
		mPagedown_Serial = (ImageView) mActivity.findViewById(R.id.pagedown_line1);
		mPageback_Gather = (ImageView) mActivity.findViewById(R.id.pageback_line2);
		mPagedown_Gather = (ImageView) mActivity.findViewById(R.id.pagedown_line2);
		
		initFragment();
		setLastPlayIndex();
		setGridViewListener();
	}

	private void initFragment(){
		
		mCurSelectSerialNum = 0;
		
		int a = mserialTotalNum % SINGLECOLUMNUM;
		if (a > 0)
			mGatherTotalNum = mserialTotalNum/SINGLECOLUMNUM + 1;
		else
			mGatherTotalNum = mserialTotalNum/SINGLECOLUMNUM;

		serialGdAdpater = new AlbumGridAdapter();
		serialGdAdpater.setAdapterTotalNum(mActivity,mserialTotalNum);
		serialGdAdpater.setHoverListener(serialHoverListener);
		
		gatherGdAdpater = new AlbumGridAdapter();
		gatherGdAdpater.setAdapterTotalNum(mActivity,mserialTotalNum);
		gatherGdAdpater.setHoverListener(gatherHoverListener);
		
		mPageback_Serial.setVisibility(View.INVISIBLE);
		mPagedown_Serial.setVisibility(View.INVISIBLE);
		mPageback_Gather.setVisibility(View.INVISIBLE);
		mPagedown_Gather.setVisibility(View.INVISIBLE);
		
		if(mserialTotalNum > SINGLECOLUMNUM){
			mPagedown_Serial.setVisibility(View.VISIBLE);
			mPageback_Serial.setVisibility(View.VISIBLE);
		}
		if(mserialTotalNum/PAGETOTALNUM > 0){
			mPagedown_Gather.setVisibility(View.VISIBLE);
			mPageback_Gather.setVisibility(View.VISIBLE);
		}
	}
	private void setLastPlayIndex(){
		
		int SelectSerialIndex = lastPlayIndex%SINGLECOLUMNUM-1; 
		serialCurrentPageNum= lastPlayIndex/SINGLECOLUMNUM; 
		
		if(SelectSerialIndex == -1){ // 
			SelectSerialIndex = SINGLECOLUMNUM-1;
			serialCurrentPageNum = serialCurrentPageNum -1;
		}
		
		mCurSelectSerialNum = SelectSerialIndex;
		updateTotalPage(serialCurrentPageNum, 2, true);
		
		serialGdAdpater.setLastFocusItem(mCurSelectSerialNum);
		gd_serial_list.setAdapter(serialGdAdpater);
		
		if(!isPlayerFragment){
			((DetaiInfoView) mActivity).setCurrentSelectItem(lastPlayIndex-1);
		}
	}
	
	private void setGridViewListener(){
		gd_serial_list.setOnItemClickListener(gd_SerialOnItemClick);
		
		gd_serial_list.setOnKeyListener(UserKeyListener);

		gd_serial_list.setOnItemSelectedListener(serialListSelectListener);
		
		gd_gather_list.setOnKeyListener(UserKeyListener);
		gd_serial_list.setOnFocusChangeListener(gridViewOnFocus);
		gd_gather_list.setOnFocusChangeListener(gridViewOnFocus);
		
		gd_serial_list.setOnHoverListener(commonHoverListner);
		gd_gather_list.setOnHoverListener(commonHoverListner);
		mPageback_Serial.setOnHoverListener(commonHoverListner);
		mPagedown_Serial.setOnHoverListener(commonHoverListner);
		mPageback_Gather.setOnHoverListener(commonHoverListner);
		mPagedown_Gather.setOnHoverListener(commonHoverListner);
		
		mPageback_Serial.setOnClickListener(onClickListner);
		mPagedown_Serial.setOnClickListener(onClickListner);
		mPageback_Gather.setOnClickListener(onClickListner);
		mPagedown_Gather.setOnClickListener(onClickListner);
	}

	
	//serialCurIndex  from 0---9
	//isSerialFocus =0    isSerialFocus=1 (gather)    isSerialFocus=2(hover)
	private void updateTotalPage(int serialCurIndex,int isSerialFocus,boolean goRight){
		System.out.println("===== serialCurrentPageNum = "+serialCurIndex);
		int SerialCurPage = serialCurIndex ;
		serialCurIndex = serialCurIndex*SINGLECOLUMNUM;
		int SerialCurSelectIndex = serialCurIndex % SINGLECOLUMNUM;
		
			gatherGdAdpater.setGatherContent(GATHER,SerialCurPage/GATHERCOLUMNUM);
			if(isSerialFocus == 0 || isSerialFocus == 2){
				gatherGdAdpater.setLastFocusItem(SerialCurPage%GATHERCOLUMNUM);
			}else if(isSerialFocus == 1){
				gatherGdAdpater.setLastFocusItem(-1);
			}
			gd_gather_list.setAdapter(gatherGdAdpater);
			gd_gather_list.setSelection(SerialCurPage%GATHERCOLUMNUM);

			serialGdAdpater.setSerialContent(SINGLE, serialCurIndex-SerialCurSelectIndex);
			if(isSerialFocus == 0){
				serialGdAdpater.setLastFocusItem(-1);
			}else{
				serialGdAdpater.setLastFocusItem(0);
			}
			gd_serial_list.setAdapter(serialGdAdpater);
			if(serialCurIndex == mserialTotalNum-1){
				gd_serial_list.setSelection(SerialCurSelectIndex);
			}else{
				if(!goRight)
					gd_serial_list.setSelection(SINGLECOLUMNUM-1);
				else
					gd_serial_list.setSelection(0);
			}
	}
	
	OnFocusChangeListener gridViewOnFocus = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			// TODO Auto-generated method stub
			if (arg0 == gd_serial_list) {
				if (arg1) {
					int focusIndex = mCurSelectSerialNum;
					serialGdAdpater.setLastFocusItem(-1);
					gd_serial_list.setAdapter(serialGdAdpater);
					gd_serial_list.setSelection(focusIndex);
					
				} else {
					serialGdAdpater.setLastFocusItem(mCurSelectSerialNum);
					serialGdAdpater.notifyDataSetChanged();
				}
			} else if (arg0 == gd_gather_list) {
				if (arg1) {
					gatherGdAdpater.setLastFocusItem(-1);
					gd_gather_list.setAdapter(gatherGdAdpater);
					gd_gather_list.setSelection(serialCurrentPageNum%GATHERCOLUMNUM);
				} else {
					gatherGdAdpater.setLastFocusItem(serialCurrentPageNum%GATHERCOLUMNUM);
					gatherGdAdpater.notifyDataSetChanged();
				}
			}
		}
	};
	
	OnKeyListener UserKeyListener = new OnKeyListener(){
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			if(arg2.getAction() == KeyEvent.ACTION_DOWN){
				
				if(gd_serial_list.hasFocus()){
					if(arg1 == KeyEvent.KEYCODE_DPAD_RIGHT){
					if((gd_serial_list.getSelectedItemPosition() == SINGLECOLUMNUM-1)&&((serialCurrentPageNum*SINGLECOLUMNUM)+
							gd_serial_list.getSelectedItemPosition()+1)<mserialTotalNum)
							{
								serialCurrentPageNum++;
								updateTotalPage(serialCurrentPageNum,0,true);
								return true;
							}else if(((serialCurrentPageNum*SINGLECOLUMNUM)+gd_serial_list.getSelectedItemPosition()+1) == mserialTotalNum){
								serialCurrentPageNum = 0;
								updateTotalPage(serialCurrentPageNum,0,true);
								return true;
							}
					}else if(arg1 == KeyEvent.KEYCODE_DPAD_LEFT){
						if(gd_serial_list.getSelectedItemPosition() == 0){
							if(serialCurrentPageNum > 0){
								serialCurrentPageNum--;
								updateTotalPage(serialCurrentPageNum,0,false);
								return true;
							}else{
								serialCurrentPageNum = mserialTotalNum/SINGLECOLUMNUM;
								if(mserialTotalNum % SINGLECOLUMNUM == 0){
									serialCurrentPageNum -= 1;
								}
								updateTotalPage(serialCurrentPageNum,0,false);
							return true;
							}
						}
					}
				}else if(gd_gather_list.hasFocus()){
					if(arg1 == KeyEvent.KEYCODE_DPAD_RIGHT){
						if(serialCurrentPageNum < mGatherTotalNum-1){
							serialCurrentPageNum++;
						}else if(serialCurrentPageNum == mGatherTotalNum-1){
							serialCurrentPageNum = 0;
						}
						updateTotalPage(serialCurrentPageNum,1,true);
						return true;
					}else if(arg1 == KeyEvent.KEYCODE_DPAD_LEFT){
						if(serialCurrentPageNum > 0){
							serialCurrentPageNum--;
						}else if(serialCurrentPageNum == 0){
							serialCurrentPageNum = mserialTotalNum/SINGLECOLUMNUM;
							if(mserialTotalNum % SINGLECOLUMNUM == 0){
								serialCurrentPageNum -= 1;
							}
						}
						updateTotalPage(serialCurrentPageNum,1,false);
						return true;
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
	
	
	OnItemClickListener gd_SerialOnItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			if(!isPlayerFragment){
				mSelectIndex = serialCurrentPageNum*SINGLECOLUMNUM+arg2;
				System.out.println("--OnItemClickListener-----SerialSelectIndex = "+mSelectIndex);
				((DetaiInfoView) mActivity).setCurrentSelectItem(mSelectIndex);
				((DetaiInfoView) mActivity).doForFragmentSelectedItem();
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
			mSelectIndex = serialCurrentPageNum*SINGLECOLUMNUM+arg2;
			System.out.println("----OnItemSelectedListener---SerialSelectIndex = "+mSelectIndex);
			if(!isPlayerFragment){
				((DetaiInfoView) mActivity).setCurrentSelectItem(mSelectIndex);
			}
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
		}
	};
	
	OnHoverListener serialHoverListener = new OnHoverListener() {
		
		@Override
		public boolean onHover(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch(event.getAction()){
			case MotionEvent.ACTION_HOVER_ENTER:
				((View) serialGdAdpater.getItem(0)).setActivated(false);
				int position = (Integer)(v.getTag(R.layout.fragment_albuml_format));
				mCurSelectSerialNum = position;
				mSelectIndex = serialCurrentPageNum*SINGLECOLUMNUM+position;
				System.out.println("---OnHoverListener----SerialSelectIndex = "+mSelectIndex);
				if(!isPlayerFragment){
					((DetaiInfoView) mActivity).setCurrentSelectItem(mSelectIndex);
				}
				gd_serial_list.setSelection(position);
				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				v.setSelected(false);
				break;
			}
			return false;
		}
	};
	OnHoverListener gatherHoverListener = new OnHoverListener() {
		@Override
		public boolean onHover(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch(event.getAction()){
			case MotionEvent.ACTION_HOVER_ENTER:
				((View) gatherGdAdpater.getItem(0)).setActivated(false);
				int position = (Integer)(v.getTag(R.layout.fragment_albuml_format));
				if((serialCurrentPageNum % GATHERCOLUMNUM) != position){
					serialCurrentPageNum = (serialCurrentPageNum-serialCurrentPageNum%GATHERCOLUMNUM+position);

					gd_gather_list.setSelection(position);
					serialGdAdpater.setSerialContent(SINGLE, serialCurrentPageNum*10);
					serialGdAdpater.setLastFocusItem(0);
					gd_serial_list.setAdapter(serialGdAdpater);
					gd_serial_list.setSelection(0);
				}
				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				v.setSelected(false);
				break;
			}
			return false;
		}
	};
	
	OnHoverListener commonHoverListner = new OnHoverListener() {
		
		@Override
		public boolean onHover(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch(event.getAction()){
			case MotionEvent.ACTION_HOVER_ENTER:
				v.requestFocusFromTouch();
			}
			return false;
		}
	};
	
	OnClickListener onClickListner = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v == mPageback_Serial){
				gd_serial_list.requestFocusFromTouch();
				
				if(serialCurrentPageNum > 0){
					serialCurrentPageNum--;
				}else{
					serialCurrentPageNum = mserialTotalNum/SINGLECOLUMNUM;
					if(mserialTotalNum % SINGLECOLUMNUM == 0){
						serialCurrentPageNum -= 1;
					}
				}
				updateTotalPage(serialCurrentPageNum,2,true);
				mSelectIndex = serialCurrentPageNum*SINGLECOLUMNUM;
			}else if(v == mPagedown_Serial){
				gd_serial_list.requestFocusFromTouch();
				
				if(serialCurrentPageNum < mGatherTotalNum-1){
					serialCurrentPageNum++;
				}else{
					serialCurrentPageNum = 0;
				}
				updateTotalPage(serialCurrentPageNum,2,true);
				mSelectIndex = serialCurrentPageNum*SINGLECOLUMNUM;
			}else if(v == mPageback_Gather){
				gd_gather_list.requestFocusFromTouch();
				
				if(serialCurrentPageNum < GATHERCOLUMNUM){
					serialCurrentPageNum = mserialTotalNum/SINGLECOLUMNUM;
					if(mserialTotalNum % SINGLECOLUMNUM == 0){
						serialCurrentPageNum -= 1;
						serialCurrentPageNum = (serialCurrentPageNum/GATHERCOLUMNUM)*GATHERCOLUMNUM;
					}
				}else{
					serialCurrentPageNum -= GATHERCOLUMNUM;
					serialCurrentPageNum = (serialCurrentPageNum/GATHERCOLUMNUM)*GATHERCOLUMNUM;
				}
				updateTotalPage(serialCurrentPageNum,2,true);
				
			}else if(v == mPagedown_Gather){
				gd_gather_list.requestFocusFromTouch();
				int PageNum = mserialTotalNum/PAGETOTALNUM;
				if(mserialTotalNum % PAGETOTALNUM == 0){
					PageNum -= 1;
				}
				if((serialCurrentPageNum/GATHERCOLUMNUM) >= PageNum){
					serialCurrentPageNum = 0;
				}else {
					serialCurrentPageNum += GATHERCOLUMNUM; 
					serialCurrentPageNum = (serialCurrentPageNum/GATHERCOLUMNUM)*GATHERCOLUMNUM;
				}
				updateTotalPage(serialCurrentPageNum,2,true);
			}
		}
	};
}
