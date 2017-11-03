package com.ott.webtv;

import java.util.ArrayList;
import java.util.HashMap;

import com.ott.webtv.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class VideoGridAdapter extends BaseAdapter{

	private Context mcontext;
	private ArrayList<HashMap<String,String>> mUrlList;
	private int mGridViewSize;
	private ImageLoader mGetImage;
	String name[];
	String url[];
	private TextView[] mVideoName;
	
	public void setGridParam(Context context,ArrayList<HashMap<String,String>>  list,
							int pagesize,int pagenum){
		this.mcontext = context;
		int pageStartIndex = (pagenum-1) * pagesize;
		int pageEndIndex = pageStartIndex + pagesize;
		mUrlList = new ArrayList<HashMap<String,String>>();
		while((pageStartIndex < pageEndIndex)&&(pageStartIndex < list.size())){
			mUrlList.add(list.get(pageStartIndex));
			pageStartIndex++;
		}
		mGridViewSize = mUrlList.size();
		mVideoName = new TextView[mGridViewSize];
		System.out.println("==== GridViwe size = " +mGridViewSize);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
			return mGridViewSize;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mVideoName[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	public void setUrl(String[] url){
		this.url = url;
	}
	
	public void setname(String[] name){
		this.name = name;
	}
	public ArrayList<HashMap<String,String>> getCurPageContent(){
		return mUrlList;
	}
	
	
	HoldAppContent mHold = null;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		if(convertView == null){
			mHold = new HoldAppContent();
			convertView = LayoutInflater.from(mcontext).inflate(R.layout.video_gridview_detail, parent, false);
			mHold.image_video = (ImageView)convertView.findViewById(R.id.iv);
			mHold.tv = (TextView)convertView.findViewById(R.id.tv);
			convertView.setTag(mHold);
		}else{
			mHold = (HoldAppContent) convertView.getTag();
		}
			mHold.image_video.setImageResource(R.drawable.default_image);
			mGetImage = new ImageLoader();   
			mGetImage.setDownLoaderImageView(mHold.image_video);
			mGetImage.setImageFromUrl(mUrlList.get(position).get("picUrl"));
			mHold.tv.setText(mUrlList.get(position).get("videoName"));
			mVideoName[position] = mHold.tv;
			
		return convertView;
	}
	
	private class HoldAppContent{
		ImageView image_video;
		TextView tv;
	}
	
	public void CancleTask(){
		if(mGetImage != null)
			mGetImage.CancleTask();
	}
	
	public void ClearCachePic(){
		if(mGetImage != null)
			mGetImage.clearBmpCache();
	}
	
	
}