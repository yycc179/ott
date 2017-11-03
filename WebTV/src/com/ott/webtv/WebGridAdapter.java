package com.ott.webtv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ott.webtv.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WebGridAdapter extends BaseAdapter {

	private Context mcontext;
	private ArrayList<Map<String, Object>> mlist;
	private int mGridViewSize;

	@SuppressWarnings("unchecked")
	public void setGridParam(Context context, Object list, int pagesize,int pagenum) {
		this.mcontext = context;
		int pageStartIndex = (pagenum-1) * pagesize;
		int pageEndIndex = pageStartIndex + pagesize;
		System.out.println("----------- the page index"+pageStartIndex+"-------"+pageEndIndex);
		List<Map<String, Object>> Glist = (List<Map<String, Object>>) list;
		mlist = new ArrayList<Map<String, Object>>();
		while ((pageStartIndex < pageEndIndex) && (pageStartIndex < Glist.size())) {
			mlist.add(Glist.get(pageStartIndex));
			pageStartIndex++;
		}
		mGridViewSize = mlist.size();
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
			convertView = LayoutInflater.from(mcontext).inflate(R.layout.ott_manager_detail, parent, false);
			mHold.image = (ImageView) convertView.findViewById(R.id.iv);
			mHold.tv = (TextView) convertView.findViewById(R.id.tv);
			convertView.setTag(mHold);
		} else {
			mHold = (HoldAppContent) convertView.getTag();
		}
		mHold.image.setImageDrawable((Drawable) mlist.get(position).get("img"));

		mHold.tv.setText((String)mlist.get(position).get("name"));
		return convertView;
	}

	int setok = 0;

	public void setnetpic(int setok) {
		this.setok = setok;
	}

	private class HoldAppContent {
		ImageView image;
		TextView tv;
	}

}