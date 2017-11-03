package com.ott.webtv;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.ott.webtv.core.CategoryManager;
import com.ott.webtv.core.DataNode.Category;
import com.ott.webtv.R;

class category_popWindow implements OnKeyListener {

	private final int STARTX = 0;
	private final int STARTY = 0;
	private final int POPWINDOW_HEIGHT = 720;
	private final int POPWINDOW_WIGHT = 1280;
	private CategoryAdapter category_adpter1 = null;
	private CategoryAdapter category_adpter2 = null;
	private CategoryAdapter category_adpter3 = null;
	private ListView listview_Level1 = null;
	private ListView listview_Level2 = null;
	private ListView listview_Level3 = null;
	private PopupWindow mPopWindow_Category;
	private View popView = null;
	private Context mcontext;
	private int lv1LastSelectIndex = 0, lv2LastSelectIndex = 0,
			lv3LastSelectIndex = 0;


	public category_popWindow(Context context) {
		this.mcontext = context;
	}

	public void clearSelecPosition() {
		this.lv1LastSelectIndex = 0;
		this.lv2LastSelectIndex = 0;
		this.lv3LastSelectIndex = 0;
	}

	public void showCategoryPopWindow() {
		// TODO Auto-generated method stub
		if (mPopWindow_Category == null) {
			initCategoryView();
			init_CategoryPopWindow();
		}
		mPopWindow_Category.showAtLocation(popView, Gravity.NO_GRAVITY, STARTX,STARTY);
		setCategoryData();
	}


	@SuppressWarnings("deprecation")
	private void initCategoryView() {
		// TODO Auto-generated method stub
		popView = LayoutInflater.from(mcontext).inflate(
				R.layout.video_categroy, null, false);
		mPopWindow_Category = new PopupWindow(popView, POPWINDOW_WIGHT,
				POPWINDOW_HEIGHT, true);
		mPopWindow_Category.setFocusable(true);
		 mPopWindow_Category.setBackgroundDrawable(new BitmapDrawable());
		popView.setFocusable(true);
		popView.setFocusableInTouchMode(true);
		listview_Level1 = (ListView) popView
				.findViewById(R.id.OTT_Video_Category_Level1);
		listview_Level2 = (ListView) popView
				.findViewById(R.id.OTT_Video_Category_Level2);
		listview_Level3 = (ListView) popView
				.findViewById(R.id.OTT_Video_Category_Level3);
	}

	private void init_CategoryPopWindow() {
		// TODO Auto-generated method stub
		category_adpter1 = new CategoryAdapter(mcontext);
		category_adpter2 = new CategoryAdapter(mcontext);
		category_adpter3 = new CategoryAdapter(mcontext);

		listview_Level1.setOnKeyListener(this);
		listview_Level2.setOnKeyListener(this);
		listview_Level3.setOnKeyListener(this);
		popView.setOnKeyListener(this);

		listview_Level1.setOnItemClickListener(L1_OnItemClick);
		listview_Level2.setOnItemClickListener(L2_OnItemClick);
		listview_Level3.setOnItemClickListener(L3_OnItemClick);

	}

	OnItemClickListener L1_OnItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Category node_lv1 = CategoryManager.getInstace().getNode(arg2);
			if (!node_lv1.hasSubCategory()) {
				((VideoBrowser) mcontext).getDataFromCategory(arg2, -1, -1);
				lv1LastSelectIndex = arg2;
				lv2LastSelectIndex = lv3LastSelectIndex = 0;
			} else {
				changeL2Focus_FromL1(arg2);
				return;
			}
			if (mPopWindow_Category != null) {
				mPopWindow_Category.dismiss();
			}
		}
	};

	OnItemClickListener L2_OnItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Category node_lv2 = CategoryManager.getInstace()
					.getNode(listview1CurCuosr).getSubNode(arg2);
			if (!node_lv2.hasSubCategory()) {
				lv1LastSelectIndex = listview1CurCuosr;
				lv2LastSelectIndex = arg2;
				lv3LastSelectIndex = 0;
				((VideoBrowser) mcontext).getDataFromCategory(
						lv1LastSelectIndex, arg2, -1);
			} else {
				if (listview_Level3.getVisibility() == View.VISIBLE) {
					changeL3Focus_FromL2(arg2);
				}
				return;
			}
			if (mPopWindow_Category != null) {
				mPopWindow_Category.dismiss();
				// mPopWindow_Category = null;
			}
		}
	};

	OnItemClickListener L3_OnItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub

			((VideoBrowser) mcontext).getDataFromCategory(listview1CurCuosr,
					listview2CurCuosr, arg2);

			lv1LastSelectIndex = listview1CurCuosr;
			lv2LastSelectIndex = listview2CurCuosr;
			lv3LastSelectIndex = arg2;

			if (mPopWindow_Category != null) {
				mPopWindow_Category.dismiss();
				// mPopWindow_Category = null;
			}
		}
	};

	
	private void changeL2Focus_FromL1(int L1CurrentPosition){
		category_adpter1.setCurrentFocus(L1CurrentPosition);
		listview_Level1.setAdapter(category_adpter1);
		listview_Level1.setSelection(L1CurrentPosition);
		category_adpter2.setCurrentFocus(-1);
		listview_Level2.setSelector(R.drawable.listview_selector);
		listview_Level2.setAdapter(category_adpter2);

		if(L1CurrentPosition == lv1LastSelectIndex){
			listview_Level2.setSelection(lv2LastSelectIndex );
		}else{
			listview_Level2.setSelection(0);
		}
		listview_Level2.requestFocus();
	}
	
	private void changeL3Focus_FromL2(int L2CurrentPosition){
		category_adpter2.setCurrentFocus(L2CurrentPosition);
		category_adpter2.notifyDataSetChanged();

		category_adpter3.setCurrentFocus(-1);
		listview_Level3.setSelector(R.drawable.listview_selector);
		listview_Level3.setAdapter(category_adpter3);
		if(L2CurrentPosition == lv2LastSelectIndex){
			listview_Level3.setSelection(lv3LastSelectIndex );
		}else{
			listview_Level3.setSelection(0);
		}
		listview_Level3.requestFocus();
	}
	
	private void changeL1_UpdateL2(){
		if(listview1CurCuosr == lv1LastSelectIndex){ 
			System.out.println("---- set gone----listview2CurCuosr = "+lv2LastSelectIndex);
			category_adpter2.setCurrentFocus(lv2LastSelectIndex);
			listview_Level2.setSelector(R.drawable.listview_selector);
			listview_Level2.setAdapter(category_adpter2);
			listview_Level2.setSelection(lv2LastSelectIndex );
		}else{
			category_adpter2.setCurrentFocus(-1);
			listview_Level2.setSelector(R.color.transparent_background);
			listview_Level2.setAdapter(category_adpter2);
		}
	}
	
	private void changeL2_UpdateL3(){
		if(listview2CurCuosr == lv2LastSelectIndex){
			System.out.println("---- set gone----listview2CurCuosr = "+lv2LastSelectIndex);
			category_adpter3.setCurrentFocus(lv3LastSelectIndex);
			listview_Level3.setSelector(R.drawable.listview_selector);
			listview_Level3.setAdapter(category_adpter3);
			listview_Level3.setSelection(lv3LastSelectIndex );
		}else{
			category_adpter3.setCurrentFocus(-1);
			listview_Level3.setSelector(R.color.transparent_background);
			listview_Level3.setAdapter(category_adpter3);
		}
	}
	
	
	
	static int listview1CurCuosr = 0, listview2CurCuosr = 0;
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (mPopWindow_Category != null) {
					mPopWindow_Category.dismiss();
					mPopWindow_Category = null;
					((VideoBrowser) mcontext).doForPopWindowBack();
				}
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				if (listview_Level1.hasFocus()) {
					if (listview_Level2.getVisibility() == View.VISIBLE) {
						changeL2Focus_FromL1(listview_Level1.getSelectedItemPosition());
					}
				} else if (listview_Level2.hasFocus()) {
					if (listview_Level3.getVisibility() == View.VISIBLE) {
						changeL3Focus_FromL2(listview_Level2.getSelectedItemPosition());
					}
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				if (listview_Level3.hasFocus()) {
					changeL2_UpdateL3();;

					category_adpter2.setCurrentFocus(-1);
					
					listview_Level2.setAdapter(category_adpter2);
					listview_Level2.setSelection(listview2CurCuosr );
					listview_Level2.requestFocus();
				} else if (listview_Level2.hasFocus()) {
					changeL1_UpdateL2();

					category_adpter1.setCurrentFocus(-1);
					listview_Level1.setAdapter(category_adpter1);
					listview_Level1.setSelection(listview1CurCuosr);
					listview_Level1.requestFocus();

				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP
					|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
					if (listview_Level1.hasFocus()) {
						if (listview1CurCuosr == 0) listview1CurCuosr = 0;
							else listview1CurCuosr--;
					} else if (listview_Level2.hasFocus()) {
						if (listview2CurCuosr == 0) listview2CurCuosr = 0;
							else listview2CurCuosr--;
					}
				} else {
					if (listview_Level1.hasFocus()) {
						if (listview1CurCuosr != listview_Level1.getCount() - 1)
							listview1CurCuosr++;
					} else if (listview_Level2.hasFocus()) {
						if (listview2CurCuosr != listview_Level2.getCount() - 1)
							listview2CurCuosr++;
					}
				}
				if (listview_Level1.hasFocus()) {
					if (lv1LastSelectIndex != listview1CurCuosr) {
						listview2CurCuosr = 0;
					} else {
						listview2CurCuosr = lv2LastSelectIndex;
					}
					Category node_lv1 = CategoryManager.getInstace().getNode(listview1CurCuosr);
					if (node_lv1.hasSubCategory()) {
						listview_Level2.setVisibility(View.VISIBLE);
						category_adpter2.setDataSource(node_lv1, null);
						category_adpter2.setCurrentFocus(listview2CurCuosr);
			
						changeL1_UpdateL2();
					} else {
						listview_Level2.setVisibility(View.GONE);
					}
				} else if (listview_Level2.hasFocus()) {
					Category node_lv2 = CategoryManager.getInstace().getNode(listview1CurCuosr).getSubNode(listview2CurCuosr);
					if (node_lv2.hasSubCategory()) {
						listview_Level3.setVisibility(View.VISIBLE);
						category_adpter2.setDataSource(node_lv2, null);

						changeL2_UpdateL3();
					} else {
						listview_Level3.setVisibility(View.GONE);
					}
				}
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT)|| (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT))
				return true;
		}
		return false;
	}


	public void setCategoryData() {
		// TODO Auto-generated method stub
		String[] data = ((VideoBrowser) mcontext).getCategoryData();
		
		listview1CurCuosr = lv1LastSelectIndex;
		listview2CurCuosr = lv2LastSelectIndex;

		category_adpter1.setCurrentFocus(-1);
		category_adpter2.setCurrentFocus(lv2LastSelectIndex);
		category_adpter3.setCurrentFocus(lv3LastSelectIndex);
		
		category_adpter1.setDataSource(null, data);
		listview_Level1.setAdapter(category_adpter1);
		listview_Level1.setSelection(lv1LastSelectIndex);
		listview_Level1.requestFocus();

		Category node_lv1 = CategoryManager.getInstace().getNode(lv1LastSelectIndex);
		if (node_lv1.hasSubCategory()) {
			listview_Level2.setVisibility(View.VISIBLE);
			category_adpter2.setDataSource(node_lv1, null);
			listview_Level2.setAdapter(category_adpter2);
			listview_Level2.setSelection(lv2LastSelectIndex );

			Category node_lv2 = node_lv1.getSubNode(lv2LastSelectIndex);
			if (node_lv2.hasSubCategory()) {
				category_adpter3.setDataSource(node_lv2, null);
				listview_Level3.setAdapter(category_adpter3);
			} else {
				listview_Level3.setVisibility(View.GONE);
			}
		} else {
			listview_Level2.setVisibility(View.GONE);
		}

	}
}
