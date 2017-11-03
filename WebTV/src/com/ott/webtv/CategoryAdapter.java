package com.ott.webtv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ott.webtv.core.DataNode.Category;
import com.ott.webtv.R;

public class CategoryAdapter extends BaseAdapter {
	Context context;
	private String[] str;
	private Category parrent;


	CategoryAdapter(Context context) {
		this.context = context;
	}

	public void setDataSource(Category parrent, String[] str) {
		this.str = str;
		this.parrent = parrent;
	}

	private int mcurrentPosition = -1;

	public void setCurrentFocus(int currentPosition) {
		this.mcurrentPosition = currentPosition;
	}

	@Override
	public int getCount() {

		if (str != null) {
			return str.length;
		} else {
			return parrent.getSubTitles().length;
		}
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return str[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView view;
		LinearLayout ll = null; // for support the mouse
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.video_category_format, null, false);
			view = (TextView) convertView.findViewById(R.id.OTT_TextView_Catgory_Content);
			ll = (LinearLayout) convertView.findViewById(R.id.category_textview_format);
			view.setTag(convertView);
			convertView.setTag(view);
		} else {
			view = (TextView) convertView.getTag();
		}

		if (parrent == null) {
			view.setText(str[position]);
		} else {
			view.setText(parrent.getSubTitles()[position]);
		}
		System.out.println("-----mcurrentPosition = "+mcurrentPosition+"----position = "+position);
		if (mcurrentPosition == position) {
			view.setTextColor(Color.WHITE);
			if (ll != null) {
//				ll.setBackground(context.getResources().getDrawable(R.drawable.listview_but_sel));
			}
		}
		return convertView;
	}

}