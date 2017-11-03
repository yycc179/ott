package com.ott.webtv;

import java.util.List;

import com.ott.webtv.R;
import com.ott.webtv.core.ContentManager;
import com.ott.webtv.core.DataNode.Content;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class VideoGridAdapter extends BaseAdapter implements
		View.OnHoverListener {
	private Context mcontext;
	private int mGridViewSize;
	private ImageLoader mGetImage;
	private GridView gd;
	private boolean mFirst;
	private List<Content> mList;

	public VideoGridAdapter(GridView gd, Context context) {
		this.gd = gd;
		this.mcontext = context;
	}

	public void setGridParam(List<Content> list) {
		mFirst = true;
		mGridViewSize = 0;
		
		if(list != null){
			mList = list;
			for (Content c : list) {
				if (c != null) {
					mGridViewSize++;
				}
			}
		}
	}

	public void clear() {
		mGridViewSize = 0;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mGridViewSize;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	HoldAppContent mHold = null;
	ContentManager curContentMgr;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			mHold = new HoldAppContent();
			convertView = LayoutInflater.from(mcontext).inflate(
					R.layout.video_gridview_detail, parent, false);
			mHold.image_video = (ImageView) convertView.findViewById(R.id.iv);
			mHold.playFlag = (ImageView) convertView
					.findViewById(R.id.unSupportPlay);
			mHold.tv = (TextView) convertView.findViewById(R.id.tv);
			convertView.setOnHoverListener(this);
			convertView.setTag(mHold);
		} else {
			mHold = (HoldAppContent) convertView.getTag();
		}

		if (position == 0 && mFirst == false) {
			return convertView;
		}
		mFirst = false;

		Content content = mList.get(position);
		if(content == null){
			return convertView;
		}
		mHold.image_video.setImageResource(R.drawable.default_image);

		mGetImage = new ImageLoader();
		mGetImage.setImageSize(180, 135);
		mGetImage.setDownLoaderImageView(mHold.image_video);		
		
		mGetImage.setImageFromUrl(content.getPicURL());
		mHold.tv.setText(content.getTitle());

		if (content.getPlayFlag()) {
			mHold.playFlag.setVisibility(View.GONE);
		} else {
			mHold.playFlag.setVisibility(View.VISIBLE);
		}

		convertView.setTag(R.layout.video_gridview_detail, position);

		return convertView;
	}

	private class HoldAppContent {
		ImageView image_video;
		ImageView playFlag;
		TextView tv;
	}

	public void CancleTask() {
		if (mGetImage != null)
			mGetImage.CancleTask();
	}

	public void ClearCachePic() {
		if (mGetImage != null)
			mGetImage.clearBmpCache();
	}

	@Override
	public boolean onHover(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_HOVER_ENTER:
			int pos = (Integer) v.getTag(R.layout.video_gridview_detail);
			gd.setSelection(pos);
			break;
		case MotionEvent.ACTION_HOVER_EXIT:
			v.setSelected(false);
			break;
		}
		return false;
	}

}