package com.ott.webtv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ott.webtv.WebContent.TYPE_E;
import com.ott.webtv.core.HttpUtils;
import com.ott.webtv.core.HttpUtils.CBKHandler;

import java.util.List;

public class WebGridAdapter extends BaseAdapter implements View.OnHoverListener {

	private int mGridViewSize;
	private Context mContext;
	private Boolean mFirst;
	private HoldAppContent mHold;
	private List<WebContent> mList;
	private int mPageSize;
	private int pageStartIndex;
	private GridView mGrid;

	public WebGridAdapter(Context contenxt, List<WebContent> list,
			int pageSize, GridView gd) {
		mContext = contenxt;
		mPageSize = pageSize;
		pageStartIndex = 0;
		mList = list;
		mGrid = gd;
	}

	public void setCurrentPage(int page) {
		mFirst = true;
		pageStartIndex = ((page - 1) * mPageSize);

		int left = mList.size() - pageStartIndex;
		mGridViewSize = left > mPageSize ? mPageSize : left;
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

	public int getPageStart() {
		return pageStartIndex;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		if (convertView == null) {
			mHold = new HoldAppContent();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.website_gridview_item, parent, false);
			mHold.image = (ImageView) convertView.findViewById(R.id.iv);
			mHold.tv = (TextView) convertView.findViewById(R.id.tv);
			convertView.setTag(mHold);
			convertView.setOnHoverListener(this);
		} else {
			mHold = (HoldAppContent) convertView.getTag();
		}

		if (position == 0 && mFirst == false) {
			return convertView;
		}
		mFirst = false;

		convertView.setTag(R.layout.website_gridview_item, position);

		WebContent content = (WebContent) mList.get(position + pageStartIndex);
		mHold.tv.setText(content.getName());

		Drawable drawable = content.getLogo();
		if (drawable != null) {
			mHold.image.setImageDrawable(drawable);

		} else {
			new Logoloader(mHold.image, content).set();

		}

		return convertView;
	}

	private class HoldAppContent {
		ImageView image;
		TextView tv;
	}

	private class Logoloader {
		WebContent content;
		ImageView view;

		public Logoloader(ImageView view, WebContent content) {
			this.view = view;
			this.content = content;
		}

		public void set() {

			String date = content.getTag(WebContent.sCacheDate, TYPE_E.LOGO);
			String etag = content.getTag(WebContent.sCacheEtag, TYPE_E.LOGO);

			new HttpUtils(content.getRequestUrl(TYPE_E.LOGO), new CBKHandler() {

				@Override
				public void handle(byte[] buf, Object... attr) {
					if (buf == null) {
						buf = (byte[]) content.readCache(TYPE_E.LOGO);
					} else {
						content.writeCache(buf, (String) attr[1],
								(String) attr[2], TYPE_E.LOGO);
					}

					if (buf != null) {
						Drawable drawable = new BitmapDrawable(
								mContext.getResources(),
								BitmapFactory.decodeByteArray(buf, 0,
										buf.length));
						Message msg = handler.obtainMessage();
						msg.obj = drawable;
						msg.sendToTarget();
						content.setLogo(drawable);
					}

				}
			}).setCacheEnable().setParam(HttpUtils.sModifySince, date)
					.setParam(HttpUtils.sIfNoneMatch, etag).execute();
		}

		@SuppressLint("HandlerLeak")
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				Drawable drawable = (Drawable) msg.obj;
				view.setImageDrawable(drawable);
			}
		};
	}

	@Override
	public boolean onHover(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_HOVER_ENTER:
			int pos = (Integer) v.getTag(R.layout.website_gridview_item);
			mGrid.setSelection(pos);
			break;

		case MotionEvent.ACTION_HOVER_EXIT:
			mGrid.setSelected(false);
			break;
		}
		return false;
	}

}
