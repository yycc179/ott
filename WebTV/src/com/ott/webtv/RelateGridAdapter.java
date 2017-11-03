package com.ott.webtv;

import java.util.List;

import com.ott.webtv.R;
import com.ott.webtv.core.DataNode.Content;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnHoverListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RelateGridAdapter extends BaseAdapter {

	private Context mcontext;
	private List<Content> mList;
	private int mGridViewSize;
	private ImageLoader mGetImage;

	public void setGridParam(Context context, List<Content> list, int pageSize,
            int pageNum) {
        this.mcontext = context;
        
        if(list == null){
            mGridViewSize = 0;
            return;
        }
        
        //jie.jia 20140912 modify for mantis 0244217{
        int validListSize = -1;
        for (int i = 0; i < list.size(); i++)
        {
            if(list.get(i) == null)
            {
                validListSize = i;
                break;
            }
        }
        //jie.jia 20140912 modify for mantis 0244217{
        int pageStartIndex = pageNum * pageSize;
        if(validListSize != -1)//jie.jia 20140912 modify for mantis 0244217
            mGridViewSize = validListSize - pageStartIndex;
        else
            mGridViewSize = list.size() - pageStartIndex;
        
        if(mGridViewSize > pageSize){
            mGridViewSize = pageSize;
        }
        
        mList = list.subList(pageStartIndex, pageStartIndex + mGridViewSize);
    }

	private OnHoverListener hoverListener = null;

	public void setHoverListener(OnHoverListener hoverListener) {
		this.hoverListener = hoverListener;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		if (convertView == null) {
			mHold = new HoldAppContent();
			convertView = LayoutInflater.from(mcontext).inflate(
					R.layout.fragment_relate_format, parent, false);
			mHold.image_video = (ImageView) convertView.findViewById(R.id.iv);
			mHold.playFlag = (ImageView) convertView
					.findViewById(R.id.unSupportPlay);
			mHold.tv = (TextView) convertView.findViewById(R.id.tv);
			convertView.setOnHoverListener(hoverListener);
			convertView.setTag(mHold);
		} else {
			mHold = (HoldAppContent) convertView.getTag();
		}

		Content content = mList.get(position);
		if (content.getPlayFlag()) {
			mHold.playFlag.setVisibility(View.GONE);
		} else {
			mHold.playFlag.setVisibility(View.VISIBLE);
		}
		convertView.setTag(R.layout.fragment_relate_format, position);
		mHold.image_video.setImageResource(R.drawable.default_image);
		mGetImage = new ImageLoader(); // new OTT_Video_GetImage();
		mGetImage.setImageSize(110, 128);
		mGetImage.setDownLoaderImageView(mHold.image_video);
		mGetImage.setImageFromUrl(mList.get(position).getPicURL());
		mHold.tv.setText(mList.get(position).getTitle());

		return convertView;
	}

	private class HoldAppContent {
		ImageView image_video;
		ImageView playFlag;
		TextView tv;
	}

}