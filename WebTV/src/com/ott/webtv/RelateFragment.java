package com.ott.webtv;

import java.util.List;

import com.ott.webtv.R;
import com.ott.webtv.core.DataNode.Content;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
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
	private List<Content> mRelateList;
	private boolean isPlayerFragment;

	private final int GRID_NUMCOL = 6;
	private int mCurSelectIndex = 0;

	private Activity mActivity;

	public RelateFragment() {
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
		if (args != null) {
			isPlayerFragment = args.getBoolean("isPlayer");

			mCurSelectIndex = args.getInt("currentSelectItem");
			mRelateList = VideoBrowser.getInstance().getCurrentList();
			System.out.println("--------on create fragment ------"
					+ isPlayerFragment);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView;
		rootView = inflater.inflate(R.layout.detail_relatelistfragment, null,
				false);
		rootView.setVisibility(View.VISIBLE);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		if (!isPlayerFragment)
			mActivity = (DetaiInfoView) getActivity();
		else {
			mActivity = (Player) getActivity();
		}
		findViews();
		if (mRelateList != null) {
			initFragment();
		}
	}

	private void initFragment() {

		relateGdAdpater = new RelateGridAdapter();
		update_GridViewInfo(0);
		gd_relate_list.setAdapter(relateGdAdpater);
		gd_relate_list.setOnHoverListener(hoverListener);
		gd_relate_list.setOnFocusChangeListener(focusChangeListener);
		mPageback_pic.setOnHoverListener(hoverListener);
		mPagedown_pic.setOnHoverListener(hoverListener);
		relateGdAdpater.setHoverListener(hoverListener);
	}

	OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			// TODO Auto-generated method stub
			if (arg1) {
				update_GridViewInfo(mCurSelectIndex / GRID_NUMCOL);
				System.out.println("-----mCurSelectIndex = "+mCurSelectIndex);
				gd_relate_list.setAdapter(relateGdAdpater);
				gd_relate_list.setSelection(mCurSelectIndex % GRID_NUMCOL);
			}
		}
	};

	OnHoverListener hoverListener = new OnHoverListener() {

		@Override
		public boolean onHover(View v, MotionEvent event) {
			// TODO Auto-generated method stub

			switch (event.getAction()) {
			case MotionEvent.ACTION_HOVER_ENTER:
				if (v != gd_relate_list && v != mPageback_pic
						&& v != mPagedown_pic) {
					int position = (Integer) v
							.getTag(R.layout.fragment_relate_format);
					gd_relate_list.setSelection(position);
					// v.setHovered(true);
				} else {
					v.requestFocusFromTouch();
				}
				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				if (v != gd_relate_list && v != mPageback_pic
						&& v != mPagedown_pic) {
					v.setSelected(false);
				}
				break;
			}

			return false;
		}

	};

	private void findViews() {
		gd_relate_list = (GridView) mActivity
				.findViewById(R.id.detail_relate_gridview);
		mPageback_pic = (ImageView) mActivity.findViewById(R.id.pageback_line1);
		mPagedown_pic = (ImageView) mActivity.findViewById(R.id.pagedown_line1);

		mPagedown_pic.setOnClickListener(pageViewOnClick);
		mPageback_pic.setOnClickListener(pageViewOnClick);
		gd_relate_list.setOnItemClickListener(gridViewOnItemClick);
		gd_relate_list.setOnKeyListener(UserKeyListener);
	}

	OnClickListener pageViewOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v == mPagedown_pic) {
				if(mCurrentPageNum == 0){
					mCurrentPageNum = 1;
				}else{
					mCurrentPageNum = 0;
				}
				update_GridViewInfo(mCurrentPageNum);
				gd_relate_list.setSelection(0);
			} else if (v == mPageback_pic) {
				if(mCurrentPageNum == 0){
					mCurrentPageNum = 1;
				}else{
					mCurrentPageNum = 0;
				}
				update_GridViewInfo(mCurrentPageNum);
				gd_relate_list.setSelection(GRID_NUMCOL - 1);
			}
		}
	};
	OnItemClickListener gridViewOnItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			int videoIndex = arg2 + mCurrentPageNum * GRID_NUMCOL;
			if (gd_relate_list.hasFocus()) {
				if(mRelateList.get(videoIndex).getPlayFlag()){
					mCurSelectIndex = videoIndex;
					if (!isPlayerFragment) {
						((DetaiInfoView) mActivity).updateVideoInfo(videoIndex);
					} else {
						((VodPlayer) mActivity).playSelectedVideo(videoIndex);
					}
				}else{
					PopDialog pop = new PopDialog(mActivity, null);
					pop.showWarning(R.string.not_support_play, null);//Jie.jia 20140910 modify the hint msg.
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
						int pos = gd_relate_list.getSelectedItemPosition();
						if ((pos == GRID_NUMCOL - 1) && ((mCurrentPageNum + 1) * GRID_NUMCOL) < mRelateList
										.size()) {
							mCurrentPageNum = 1;
							update_GridViewInfo(mCurrentPageNum);
							gd_relate_list.setSelection(0);
							return true;
						}else if((mCurrentPageNum * GRID_NUMCOL)+pos == mRelateList.size()-1){
							mCurrentPageNum = 0;
							update_GridViewInfo(mCurrentPageNum);
							gd_relate_list.setSelection(0);
							return true;
						}

					} else if (arg1 == KeyEvent.KEYCODE_DPAD_LEFT) {
						int pos = gd_relate_list.getSelectedItemPosition();
						if (pos == 0) {
							if(mCurrentPageNum == 1){
								mCurrentPageNum = 0;
							}else if(mRelateList.size() > GRID_NUMCOL){
								mCurrentPageNum = 1;
							}
							update_GridViewInfo(mCurrentPageNum);
							gd_relate_list.setSelection(GRID_NUMCOL - 1);
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

	private void update_GridViewInfo(int mCurrentPageNum) {
		mPageback_pic.setVisibility(View.INVISIBLE);
		mPagedown_pic.setVisibility(View.INVISIBLE);
		this.mCurrentPageNum = mCurrentPageNum;
		
		if(mRelateList.size() > GRID_NUMCOL){
			mPageback_pic.setVisibility(View.VISIBLE);
			mPagedown_pic.setVisibility(View.VISIBLE);
		}
//		if (mCurrentPageNum > 0) {
//			mPageback_pic.setVisibility(View.VISIBLE);
//		}
//		System.out.println("----- the currentpage = " + mCurrentPageNum);
//		if ((mCurrentPageNum + 1) * GRID_NUMCOL < mRelateList.size()) {
//			mPagedown_pic.setVisibility(View.VISIBLE);
//		}
		relateGdAdpater.setGridParam(mActivity, mRelateList, GRID_NUMCOL,
				mCurrentPageNum);
		relateGdAdpater.notifyDataSetChanged();
	}

	public void updateData() {
		mRelateList = VideoBrowser.getInstance().getCurrentList();
		update_GridViewInfo(mCurSelectIndex / GRID_NUMCOL);
	}
	
	public void setFocusIndex(int curSelIndex){
		this.mCurSelectIndex = curSelIndex;
	}
}
