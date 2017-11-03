package com.ott.webtv;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.ott.webtv.core.CategoryManager;
import com.ott.webtv.core.DataNode.Category;

class category_popWindow implements OnKeyListener {

	private final int STARTX = 0;
	private final int STARTY = 194;
	private final int POPWINDOW_HEIGHT = 468;
	private final int POPWINDOW_WIGHT = 1280;
	private CategoryAdapter category_adpter1 = null;
	private CategoryAdapter category_adpter2 = null;
	private CategoryAdapter category_adpter3 = null;
	private ListView listview_Level1 = null;
	private ListView listview_Level2 = null;
	private ListView listview_Level3 = null;

	private View layout_view2 = null;
	private View layout_view3 = null;

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
		mPopWindow_Category.showAtLocation(popView, Gravity.NO_GRAVITY, STARTX,
				STARTY);
		setCategoryData();
		listview_Level1.requestFocusFromTouch();
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
		mPopWindow_Category.setOutsideTouchable(true);
		popView.setFocusable(true);
		popView.setFocusableInTouchMode(true);
		listview_Level1 = (ListView) popView
				.findViewById(R.id.OTT_Video_Category_Level1);
		listview_Level2 = (ListView) popView
				.findViewById(R.id.OTT_Video_Category_Level2);
		listview_Level3 = (ListView) popView
				.findViewById(R.id.OTT_Video_Category_Level3);

		layout_view2 = popView.findViewById(R.id.relativeLayout_category2);
		layout_view3 = popView.findViewById(R.id.relativeLayout_category3);
	}

	private void init_CategoryPopWindow() {
		// TODO Auto-generated method stub
		category_adpter1 = new CategoryAdapter(mcontext, listview1_Hover);
		category_adpter2 = new CategoryAdapter(mcontext, listview2_Hover);
		category_adpter3 = new CategoryAdapter(mcontext, listview3_Hover);

		listview_Level1.setOnKeyListener(this);
		listview_Level2.setOnKeyListener(this);
		listview_Level3.setOnKeyListener(this);
		popView.setOnKeyListener(this);

		listview_Level1.setOnItemClickListener(L1_OnItemClick);
		listview_Level2.setOnItemClickListener(L2_OnItemClick);
		listview_Level3.setOnItemClickListener(L3_OnItemClick);

		popView.findViewById(R.id.category_background).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (mPopWindow_Category != null) {
							mPopWindow_Category.dismiss();
							mPopWindow_Category = null;
							((VideoBrowser) mcontext).doForPopWindowBack();
						}
					}
				});
	}

	OnItemClickListener L1_OnItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Category node_lv1 = CategoryManager.getCurrent().getNode(arg2);
			if (!node_lv1.hasSubCategory()) {
				((VideoBrowser) mcontext).getDataFromCategory(arg2);
				lv1LastSelectIndex = arg2;
				lv2LastSelectIndex = lv3LastSelectIndex = 0;
				if (mPopWindow_Category != null) {
					mPopWindow_Category.dismiss();
				}
			} else {
				listview1CurCuosr = arg2;
				// changeL2Focus_FromL1(arg2);
			}
		}
	};

	OnItemClickListener L2_OnItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Category node_lv2 = CategoryManager.getCurrent()
					.getNode(listview1CurCuosr).getSubNode(arg2);
			if (!node_lv2.hasSubCategory()) {
				lv1LastSelectIndex = listview1CurCuosr;
				lv2LastSelectIndex = arg2;
				lv3LastSelectIndex = 0;
				((VideoBrowser) mcontext).getDataFromCategory(
						lv1LastSelectIndex, arg2);
				if (mPopWindow_Category != null) {
					mPopWindow_Category.dismiss();
				}
			} else {
				listview2CurCuosr = arg2;
				// if (listview_Level3.getVisibility() == View.VISIBLE) {
				// changeL3Focus_FromL2(arg2);
				// }
				return;
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

	private void changeL2Focus_FromL1(int L1CurrentPosition) {

		Category node_lv1 = CategoryManager.getCurrent().getNode(
				listview1CurCuosr);
		category_adpter2.setDataSource(node_lv1, null);

		category_adpter1.setCurrentFocus(L1CurrentPosition);
		listview_Level1.setAdapter(category_adpter1);

		category_adpter2.setCurrentFocus(-1);
		listview_Level2.setAdapter(category_adpter2);

		if (L1CurrentPosition == lv1LastSelectIndex) {
			listview_Level2.setSelection(lv2LastSelectIndex);
		} else {
			listview_Level2.setSelection(0);
		}
		listview_Level2.requestFocusFromTouch();

		Category node_lv2 = CategoryManager.getCurrent()
				.getNode(listview1CurCuosr).getSubNode(listview2CurCuosr);
		if (node_lv2.hasSubCategory()) {
			layout_view3.setVisibility(View.VISIBLE);
			category_adpter3.setDataSource(node_lv2, null);
			changeL2_UpdateL3();
		} else {
			layout_view3.setVisibility(View.GONE);
		}

	}

	private void changeL3Focus_FromL2(int L2CurrentPosition) {
		category_adpter2.setCurrentFocus(L2CurrentPosition);
		category_adpter2.notifyDataSetChanged();

		category_adpter3.setCurrentFocus(-1);
		listview_Level3.setAdapter(category_adpter3);
		if (listview1CurCuosr == lv1LastSelectIndex
				&& L2CurrentPosition == lv2LastSelectIndex) {
			listview_Level3.setSelection(lv3LastSelectIndex);
			listview3CurCuosr = lv3LastSelectIndex;
		} else {
			listview_Level3.setSelection(0);
			listview3CurCuosr = 0;
		}
		listview_Level3.requestFocus();
	}

	private void changeL1_UpdateL2() {
		if (listview1CurCuosr == lv1LastSelectIndex) {
			category_adpter2.setCurrentFocus(lv2LastSelectIndex);
			listview_Level2.setAdapter(category_adpter2);
			listview_Level2.setSelection(lv2LastSelectIndex);
		} else {
			category_adpter2.setCurrentFocus(-1);
			listview_Level2.setAdapter(category_adpter2);
		}

	}

	private void changeL2_UpdateL3() {
		if ((listview1CurCuosr == lv1LastSelectIndex)
				&& (listview2CurCuosr == lv2LastSelectIndex)) {
			category_adpter3.setCurrentFocus(lv3LastSelectIndex);
			listview_Level3.setAdapter(category_adpter3);
			listview_Level3.setSelection(lv3LastSelectIndex);

		} else {
			category_adpter3.setCurrentFocus(-1);
			listview_Level3.setAdapter(category_adpter3);
		}
	}

	static int listview1CurCuosr = 0, listview2CurCuosr = 0,
			listview3CurCuosr = 0;

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
					if (layout_view2.getVisibility() == View.VISIBLE) {
						changeL2Focus_FromL1(listview_Level1
								.getSelectedItemPosition());
					}
				} else if (listview_Level2.hasFocus()) {
					if (layout_view3.getVisibility() == View.VISIBLE) {
						changeL3Focus_FromL2(listview_Level2
								.getSelectedItemPosition());
					}
				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				if (listview_Level3.hasFocus()) {
					changeL2_UpdateL3();

					category_adpter2.setCurrentFocus(-1);
					listview_Level2.setAdapter(category_adpter2);
					listview_Level2.setSelection(listview2CurCuosr);

					listview_Level2.requestFocus();
				} else if (listview_Level2.hasFocus()) {
					changeL1_UpdateL2();

					category_adpter1.setCurrentFocus(-1);
					listview_Level1.setAdapter(category_adpter1);
					listview_Level1.setSelection(listview1CurCuosr);
					listview_Level1.requestFocus();

					layout_view3.setVisibility(View.GONE);

				}
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP
					|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {

				if (listview_Level1.getChildAt(listview1CurCuosr
						- listview_Level1.getFirstVisiblePosition()) != null
						&& listview_Level1.getChildAt(
								listview1CurCuosr
										- listview_Level1
												.getFirstVisiblePosition())
								.isHovered()) {
					listview_Level1
							.getChildAt(
									listview1CurCuosr
											- listview_Level1
													.getFirstVisiblePosition())
							.setHovered(false);
				}
				if (listview_Level2.getChildAt(listview2CurCuosr
						- listview_Level2.getFirstVisiblePosition()) != null
						&& listview_Level2.getChildAt(
								listview2CurCuosr
										- listview_Level2
												.getFirstVisiblePosition())
								.isHovered()) {
					listview_Level2
							.getChildAt(
									listview2CurCuosr
											- listview_Level2
													.getFirstVisiblePosition())
							.setHovered(false);
				}
				if (listview_Level3.getChildAt(listview3CurCuosr
						- listview_Level3.getFirstVisiblePosition()) != null
						&& listview_Level3.getChildAt(
								listview3CurCuosr
										- listview_Level3
												.getFirstVisiblePosition())
								.isHovered()) {
					listview_Level3
							.getChildAt(
									listview3CurCuosr
											- listview_Level3
													.getFirstVisiblePosition())
							.setHovered(false);
				}

				if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
					if (listview_Level1.hasFocus()) {
						if (listview1CurCuosr == 0)
							listview1CurCuosr = listview_Level1.getCount() - 1;
						else
							listview1CurCuosr--;
					} else if (listview_Level2.hasFocus()) {
						if (listview2CurCuosr == 0)
							listview2CurCuosr = listview_Level2.getCount() - 1;
						else
							listview2CurCuosr--;
					} else if (listview_Level3.hasFocus()) {
						if (listview3CurCuosr == 0)
							listview3CurCuosr = listview_Level3.getCount() - 1;
						else
							listview3CurCuosr--;
					}
				} else {
					if (listview_Level1.hasFocus()) {
						if (listview1CurCuosr != listview_Level1.getCount() - 1) {
							listview1CurCuosr++;
						} else {
							listview1CurCuosr = 0;
						}
					} else if (listview_Level2.hasFocus()) {
						if (listview2CurCuosr != listview_Level2.getCount() - 1) {
							listview2CurCuosr++;
						} else {
							listview2CurCuosr = 0;
						}
					} else if (listview_Level3.hasFocus()) {
						if (listview3CurCuosr != listview_Level3.getCount() - 1) {
							listview3CurCuosr++;
						} else {
							listview3CurCuosr = 0;
						}
					}
				}
				if (listview_Level1.hasFocus()) {
					if (lv1LastSelectIndex != listview1CurCuosr) {
						listview2CurCuosr = 0;
					} else {
						listview2CurCuosr = lv2LastSelectIndex;
					}

					if (layout_view3.getVisibility() == View.VISIBLE) {
						layout_view3.setVisibility(View.GONE);
					}

					Category node_lv1 = CategoryManager.getCurrent().getNode(
							listview1CurCuosr);

					if (node_lv1.hasSubCategory()) {

						layout_view2.setVisibility(View.VISIBLE);
						category_adpter2.setDataSource(node_lv1, null);

						changeL1_UpdateL2();
					} else {
						layout_view2.setVisibility(View.GONE);
					}
					if (listview1CurCuosr == 0) {
						listview_Level1.setSelection(0);
						return true;
					} else if (listview1CurCuosr == listview_Level1.getCount() - 1) {
						listview_Level1
								.setSelection(listview_Level1.getCount() - 1);
						return true;
					}
				} else if (listview_Level2.hasFocus()) {

					Category node_lv2 = CategoryManager.getCurrent()
							.getNode(listview1CurCuosr)
							.getSubNode(listview2CurCuosr);
					if (node_lv2.hasSubCategory()) {
						layout_view3.setVisibility(View.VISIBLE);
						category_adpter3.setDataSource(node_lv2, null);
						changeL2_UpdateL3();
					} else {
						layout_view3.setVisibility(View.GONE);
					}
					if (listview2CurCuosr == 0) {
						listview_Level2.setSelection(0);
						return true;
					} else if (listview2CurCuosr == listview_Level2.getCount() - 1) {
						listview_Level2
								.setSelection(listview_Level2.getCount() - 1);
						return true;
					}
				} else if (listview_Level3.hasFocus()) {
					if (listview3CurCuosr == 0) {
						listview_Level3.setSelection(0);
						return true;
					} else if (listview3CurCuosr == listview_Level3.getCount() - 1) {
						listview_Level3
								.setSelection(listview_Level3.getCount() - 1);
						return true;
					}
				}
			}
		} else if (event.getAction() == KeyEvent.ACTION_UP) {
			if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
					|| (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT))
				return true;
		}
		return false;
	}

	public void setCategoryData() {
		// TODO Auto-generated method stub
		String[] data = CategoryManager.getCurrent().getTitles();

		listview1CurCuosr = lv1LastSelectIndex;
		listview2CurCuosr = lv2LastSelectIndex;
		listview3CurCuosr = lv3LastSelectIndex;

		layout_view2.setVisibility(View.GONE);
		layout_view3.setVisibility(View.GONE);

		category_adpter1.setCurrentFocus(-1);
		category_adpter2.setCurrentFocus(lv2LastSelectIndex);
		category_adpter3.setCurrentFocus(lv3LastSelectIndex);

		category_adpter1.setDataSource(null, data);
		listview_Level1.setAdapter(category_adpter1);
		listview_Level1.setSelection(lv1LastSelectIndex);

		Category node_lv1 = CategoryManager.getCurrent().getNode(
				lv1LastSelectIndex);
		if (node_lv1.hasSubCategory()) {
			layout_view2.setVisibility(View.VISIBLE);

			category_adpter2.setDataSource(node_lv1, null);

			Category node_lv2 = node_lv1.getSubNode(listview2CurCuosr);
			if (node_lv2.hasSubCategory()) {
				layout_view3.setVisibility(View.VISIBLE);
				category_adpter3.setDataSource(node_lv2, null);
				changeL2_UpdateL3();
			} else {
				listview_Level2.setAdapter(category_adpter2);
				listview_Level2.setSelection(lv2LastSelectIndex);
			}

		} else {
			layout_view2.setVisibility(View.GONE);
		}
	}

	Handler handler = new Handler();

	Runnable runable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			listview_Level2.setSelection(listview2CurCuosr);
		}
	};

	OnHoverListener listview1_Hover = new OnHoverListener() {
		public boolean onHover(View v, MotionEvent event) {
			// TODO Auto-generated method stub

			switch (event.getAction()) {
			case MotionEvent.ACTION_HOVER_ENTER:
				// v.setHovered(true);
				listview_Level1.requestFocusFromTouch();

				layout_view3.setVisibility(View.GONE);
				category_adpter1.setCurrentFocus(-1);
				category_adpter1.notifyDataSetChanged();

				listview1CurCuosr = (Integer) v
						.getTag(R.layout.video_category_format);
				if (lv1LastSelectIndex != listview1CurCuosr) {
					listview2CurCuosr = 0;
				} else {
					listview2CurCuosr = lv2LastSelectIndex;
				}

				hoverPosition = listview1CurCuosr
						- listview_Level1.getFirstVisiblePosition();
				if (listview_Level1.getFirstVisiblePosition() > 1) {
					hoverPosition = hoverPosition - 1;
				}
				listview_Level1.setSelection(listview1CurCuosr);
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						listview_Level1.setSelectionFromTop(listview1CurCuosr,
								hoverPosition * 48);
					}
				}, 100);

				Category node_lv1 = CategoryManager.getCurrent().getNode(
						listview1CurCuosr);

				if (node_lv1.hasSubCategory()) {
					layout_view2.setVisibility(View.VISIBLE);
					category_adpter2.setDataSource(node_lv1, null);
					changeL1_UpdateL2();
				} else {
					layout_view2.setVisibility(View.GONE);
				}

				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				// v.setHovered(false);
				break;
			default:
				break;
			}
			return false;
		}
	};

	int hoverPosition = 0;
	OnHoverListener listview2_Hover = new OnHoverListener() {
		public boolean onHover(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch (event.getAction()) {
			case MotionEvent.ACTION_HOVER_ENTER:
				// v.setHovered(true);

				category_adpter1.setCurrentFocus(listview1CurCuosr);
				category_adpter1.notifyDataSetChanged();

				if (lv2LastSelectIndex != listview2CurCuosr) {
					listview3CurCuosr = 0;
				} else {
					listview3CurCuosr = lv3LastSelectIndex;
				}

				listview2CurCuosr = (Integer) v
						.getTag(R.layout.video_category_format);
				hoverPosition = listview2CurCuosr
						- listview_Level2.getFirstVisiblePosition();
				if (listview_Level2.getFirstVisiblePosition() > 1) {
					hoverPosition = hoverPosition - 1;
				}
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						listview_Level2.setSelectionFromTop(listview2CurCuosr,
								hoverPosition * 48);
					}
				}, 100);
				listview_Level2.requestFocusFromTouch();

				category_adpter2.setCurrentFocus(-1);
				category_adpter2.notifyDataSetChanged();

				Category node_lv2 = CategoryManager.getCurrent()
						.getNode(listview1CurCuosr)
						.getSubNode(listview2CurCuosr);
				if (node_lv2.hasSubCategory()) {
					layout_view3.setVisibility(View.VISIBLE);
					category_adpter3.setDataSource(node_lv2, null);
					changeL2_UpdateL3();
				} else {
					layout_view3.setVisibility(View.GONE);
				}
				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				// v.setHovered(false);
				break;
			default:
				break;
			}
			return false;
		}
	};
	OnHoverListener listview3_Hover = new OnHoverListener() {
		public boolean onHover(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch (event.getAction()) {
			case MotionEvent.ACTION_HOVER_ENTER:
				// v.setHovered(true);

				category_adpter2.setCurrentFocus(listview2CurCuosr);
				category_adpter2.notifyDataSetChanged();

				listview_Level3.requestFocusFromTouch();
				category_adpter3.setCurrentFocus(-1);
				category_adpter3.notifyDataSetChanged();
				listview3CurCuosr = (Integer) v
						.getTag(R.layout.video_category_format);
				hoverPosition = listview3CurCuosr
						- listview_Level3.getFirstVisiblePosition();
				if (listview_Level3.getFirstVisiblePosition() > 1) {
					hoverPosition = hoverPosition - 1;
				}
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub

						listview_Level3.setSelectionFromTop(listview3CurCuosr,
								hoverPosition * 48);
					}
				}, 100);
				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				// v.setHovered(false);
				break;
			default:
				break;
			}
			return false;
		}
	};

}
