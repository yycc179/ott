package com.ott.webtv;

import java.util.ArrayList;
import java.util.HashMap;

import com.ott.webtv.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

@SuppressLint({ "NewApi", "ValidFragment" })
public class RelateFragment extends Fragment {

	private GridView gd_relate_list;
	private ImageView mPageback_pic, mPagedown_pic;
	private RelateGridAdapter relateGdAdpater;
	private ArrayList<HashMap<String, String>> urlList;
	private boolean isPlayerFragment;
	
	private final int GRID_NUMCOL = 6;

	private Activity mActivity;

	public RelateFragment(){
		System.out.println(">>>>>>>  in here  RelateFragment <<<<<<");
	}

	public static RelateFragment newInstance() {
		final RelateFragment fg = new RelateFragment();
		return fg;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Bundle args = getArguments(); 
		if(args != null){
			isPlayerFragment = args.getBoolean("isPlayer");
			System.out.println("--------on create fragment ------"+isPlayerFragment);
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView;
		rootView = inflater.inflate(R.layout.relatelistfragment, null, false);
		rootView.setVisibility(View.VISIBLE);
		return rootView;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		if(!isPlayerFragment)
			mActivity = (DetaiInfoView) getActivity();
		else{
			mActivity = (Player) getActivity();
		}
		urlList = (ArrayList<HashMap<String,String>>)mActivity.getIntent().getSerializableExtra("curPageList");
		findViews();
		if(urlList != null){
			initFragment();
		}
	}

	private void initFragment() {

		relateGdAdpater = new RelateGridAdapter();
		update_GridViewInfo(0);
		gd_relate_list.setAdapter(relateGdAdpater);
	}

	private void findViews() {
		gd_relate_list = (GridView) mActivity.findViewById(R.id.detail_relate_gridview);
		mPageback_pic = (ImageView) mActivity.findViewById(R.id.pageback_line1);
		mPagedown_pic = (ImageView) mActivity.findViewById(R.id.pagedown_line1);
		gd_relate_list.setOnItemClickListener(gridViewOnItemClick);
		gd_relate_list.setOnKeyListener(UserKeyListener);
		GuestSupport gs = new GuestSupport();
		gd_relate_list.setOnTouchListener(gs.OnTouchListener);
	}

	OnItemClickListener gridViewOnItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			int videoIndex = arg2+mCurrentPageNum*GRID_NUMCOL;
			if (gd_relate_list.hasFocus()) {
				if(!isPlayerFragment){
					((DetaiInfoView) mActivity).updateVideoInfo(videoIndex);
				}else{
					((VodPlayer) mActivity).playSelectedVideo(videoIndex);
				}
			}
		}
	};
	
	private int mCurrentPageNum = 0;

	OnKeyListener UserKeyListener = new OnKeyListener() {
		@Override
		public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
				if (gd_relate_list.hasFocus()) {
					if (arg1 == KeyEvent.KEYCODE_DPAD_RIGHT) {
						if ((gd_relate_list.getSelectedItemPosition() == GRID_NUMCOL - 1)
								&& ((mCurrentPageNum + 1) * GRID_NUMCOL) < urlList.size()) {
							mCurrentPageNum++;
							update_GridViewInfo(mCurrentPageNum);
							gd_relate_list.setSelection(0);
							return true;
						}

					} else if (arg1 == KeyEvent.KEYCODE_DPAD_LEFT) {
						if ((gd_relate_list.getSelectedItemPosition() == 0)&& (mCurrentPageNum > 0)) {
							mCurrentPageNum--;
							update_GridViewInfo(mCurrentPageNum);
							gd_relate_list.setSelection(GRID_NUMCOL-1);
							return true;
						}
					}
				} else if (arg2.getAction() == KeyEvent.ACTION_UP) {
					if (gd_relate_list.hasFocus()) {
						if ((arg1 == KeyEvent.KEYCODE_DPAD_LEFT)
								|| (arg1 == KeyEvent.KEYCODE_DPAD_RIGHT))
							return true;
					}
				}
			}
			return false;
		}
	};
	private void update_GridViewInfo(int mCurrentPageNum){
		mPageback_pic.setVisibility(View.INVISIBLE);
		mPagedown_pic.setVisibility(View.INVISIBLE);
		if(mCurrentPageNum > 0){
			mPageback_pic.setVisibility(View.VISIBLE);
		}
		System.out.println("----- the currentpage = "+mCurrentPageNum);
		System.out.println("----- the urlList.size() = "+urlList.size());
		if ((mCurrentPageNum+1) * GRID_NUMCOL < urlList.size()) {
			mPagedown_pic.setVisibility(View.VISIBLE);
		}
		relateGdAdpater.setGridParam(mActivity, urlList, GRID_NUMCOL, mCurrentPageNum);
		relateGdAdpater.notifyDataSetChanged();
	}
	
	class GuestSupport {
		// use to support mouse
		int startX = 0, startY = 0, endX = 0, endY = 0;
		OnTouchListener OnTouchListener = new OnTouchListener() {
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
				System.out.println("---------offX = " + (endX - startX));
				System.out.println("---------ofY = " + (endY - startY));
				if (Math.abs(endX - startX) < 20
						&& Math.abs(endY - startY) < 20 && mouseEnd) {
					System.out.println("-----  Item OnClick -------");
					return false;
				} else if (mouseEnd) {
					System.out.println("-----  page change -------");
					dealGesture(endX - startX, endY - startY);
					return true;
				} else {
					System.out.println("-----  do nothing -------");
					return false;
				}
			}
		};

		private void dealGesture(int offsetX, int offsetY) {
			System.out.println("--------- the offsetX = " + offsetX);
			System.out.println("--------- the offsetY = " + offsetY);
			if (Math.abs(offsetX) > 200 && Math.abs(offsetY) < 30) {
				if (offsetX > 0) {
					if (mCurrentPageNum > 0) {
						mCurrentPageNum--;
						update_GridViewInfo(mCurrentPageNum);
					}
				} else {
					if (((mCurrentPageNum + 1) * GRID_NUMCOL) < urlList.size()) {
						mCurrentPageNum++;
						update_GridViewInfo(mCurrentPageNum);
					}
				}
			}
		}
	}

}
