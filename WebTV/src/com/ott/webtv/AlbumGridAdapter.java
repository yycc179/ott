package com.ott.webtv;


import com.ott.webtv.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AlbumGridAdapter extends BaseAdapter{
	private  Context mcontext;
	private  int mserialNum;
	private int adapterCount;
	private String mClass;
	

	private final String GATHER = "gather";
	private final String SINGLE = "single";
	private final int SINGLECOLUMNUM = 10;
	private final int GATHERCOLUMNUM = 5;
	private String[] singleContent  = new String[SINGLECOLUMNUM];
	private String[] gatherContent  = new String[GATHERCOLUMNUM];
	private int gatherTotalNum;
	
	private OnHoverListener listHoverListener = null;
	
	public void setAdapterTotalNum(Context context,int serialNum){
		mcontext = context;
		mserialNum = serialNum;
		
		int a = mserialNum % SINGLECOLUMNUM;
		if (a > 0)
			gatherTotalNum = mserialNum/SINGLECOLUMNUM + 1;
		else
			gatherTotalNum = mserialNum/SINGLECOLUMNUM;
	}
	
	public void setHoverListener(OnHoverListener listener){
		this.listHoverListener = listener;
	}
	public void setSerialContent(String Class,int startIndex){
		mClass = Class;
		int i=0;
		System.out.println("----  the total num = "+mserialNum);
		while(startIndex < mserialNum && i<SINGLECOLUMNUM){
			singleContent[i] = String.valueOf(startIndex+1);
			
			System.out.println("----  the single num =     --  "+singleContent[i]);
			startIndex++;
			i++;
		}
		adapterCount = i;
//		tv = new TextView[adapterCount];
	}
	
	public void setGatherContent(String Class,int pagenum){
		mClass = Class;
		int i=0;
		int gatherCurStartNum = pagenum*GATHERCOLUMNUM; 
		int pageTotalNum = SINGLECOLUMNUM*GATHERCOLUMNUM;
		while((gatherCurStartNum+i) < gatherTotalNum && i<GATHERCOLUMNUM){
			if((gatherCurStartNum+i) == (gatherTotalNum-1)){
				if(mserialNum%SINGLECOLUMNUM == 0)
					gatherContent[i] = ((pagenum*pageTotalNum)+(i*SINGLECOLUMNUM+1))+"-"+(((pagenum*pageTotalNum)+(i+1)*SINGLECOLUMNUM));
				else
					gatherContent[i] = ((pagenum*pageTotalNum)+(i*SINGLECOLUMNUM+1))+"-"+(((pagenum*pageTotalNum)+i*SINGLECOLUMNUM)+mserialNum%SINGLECOLUMNUM);
			}
			else
				gatherContent[i] = ((pagenum*pageTotalNum)+(i*SINGLECOLUMNUM+1))+"-"+(((pagenum*pageTotalNum)+(i+1)*SINGLECOLUMNUM));
			System.out.println("----  the gather num =     --  "+gatherContent[i]);
			i++;
		}
		adapterCount = i;
//		tv = new TextView[adapterCount];
	}
	
	
	private int mLastItem;
	public void setLastFocusItem(int lastFocusItem,boolean bIsIR){
		if(bIsIR){
			mLastItem = lastFocusItem;
		}else{
			mLastItem = -1;
		}
	}
	
	public void setLastFocusItem(int lastFocusItem){
			mLastItem = lastFocusItem;
	}
	
	private int[] collectedVideo=new int[100];
	public void setCollectedVideo(int[] CollectedVideo){
		collectedVideo = CollectedVideo;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return adapterCount;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
//		return null;
		return tv;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	private View tv ;
	@SuppressLint("NewApi")
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		mHoldGridView mHold;
		if(arg1 == null){
			mHold = new mHoldGridView();
			
			arg1 = LayoutInflater.from(mcontext).inflate(R.layout.fragment_albuml_format, null, false);
			mHold.signleView = (TextView)arg1.findViewById(R.id.Video_detail_single_Content);
			mHold.gatherView  = (TextView)arg1.findViewById(R.id.Video_detail_gather_Content);
			mHold.serialLayout = (RelativeLayout)arg1.findViewById(R.id.video_detail_single);
			arg1.setOnHoverListener(listHoverListener);
			arg1.setTag(mHold);
		}else{
			mHold = (mHoldGridView)arg1.getTag();
		}
		arg1.setTag(R.layout.fragment_albuml_format,arg0);
		if(mClass.equals(GATHER)){
			mHold.serialLayout.setVisibility(View.GONE);
			mHold.gatherView.setVisibility(View.VISIBLE);
		}
		else if(mClass.equals(SINGLE)){
			mHold.serialLayout.setVisibility(View.VISIBLE);
			mHold.gatherView.setVisibility(View.GONE);
		}
		
		if(mClass.equals(GATHER)){
			mHold.gatherView.setText(gatherContent[arg0]);
			if(mLastItem == arg0){
				tv = arg1;
				arg1.setActivated(true);
//				mHold.gatherView.setTextColor(Color.BLUE);
//				mHold.gatherView.setBackground(mcontext.getResources().getDrawable(R.drawable.detail_album_serial_btn_list));
			}
		}else if(mClass.equals(SINGLE)){
			mHold.signleView.setText(singleContent[arg0]);
			if(mLastItem == arg0){
				tv = arg1;
				arg1.setActivated(true);
//				mHold.signleView.setTextColor(Color.BLUE);
//				mHold.signleView.setBackground(mcontext.getResources().getDrawable(R.drawable.detail_album_serial_btn_channel));
			}
		}
		
		if(mClass.equals(SINGLE)){
			for(int i=0;i < 100;i++){
				if(collectedVideo[i] != 0){
					if(singleContent[arg0].equals((collectedVideo[i]+""))){
					}
				}
			}
		}

		return arg1;
	}
	
	class mHoldGridView{
		TextView signleView;
		RelativeLayout serialLayout;
		TextView gatherView;
	}
	
}