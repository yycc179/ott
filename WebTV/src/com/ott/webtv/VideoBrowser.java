package com.ott.webtv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ott.webtv.core.ALParser;
import com.ott.webtv.core.CategoryManager;
import com.ott.webtv.core.ContentManager;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.DATA_TYPE;
import com.ott.webtv.core.StoreManager;

@SuppressLint("NewApi")
public class VideoBrowser extends Activity implements OnItemClickListener,
		OnClickListener, OnHoverListener {
	private GridView gd = null;
	private static final int GRID_NUMCOL = 5;
	public static final int GRID_PAGECONTENTSIZE = GRID_NUMCOL * 2;
	public VideoGridAdapter gdAdapter;
	public ImageView mImagePageLeft, mImagePageRight;
	private ImageView mSearchButton;
	// private ImageView mCategroyButton;
	private ImageButton mVodButton, mLiveButton, mFavButton, mHistoryButton;
	private TextView mPageCount, mCurSelectPath, mCategory;
	// private AlertDialog bufferDialog;
	// private View dialoglayout;

	private int mTotalPage, mCurrentPageNum;

	private category_popWindow category_window = null;
	private SearchView search_window = null;
	private PopDialog pop_dialog = null;

	private String webName = "";

	private String[] mDataSource = { "VOD", "Live", "History", "Favorite",
			"Search/" };

	private Map<Integer, Integer> mDataErrMsg;

	private CoreHandler core;
	private int mSourceType = SOURCE_TYPE.VOD.ordinal();

	private List<String> sSearchType;
	private List<DATA_TYPE> dSearchType;
	private Boolean bSearchEnable = false;
	private Boolean bLiveEnable;

	private boolean turnToLeft = false;

	private int currentIndex;
	private int lastFavNum;

	private static VideoBrowser videobrowsr;

	private boolean bDataFromCategory = false;

	public static enum SOURCE_TYPE {
		VOD, LIVE, HISTORY, FAV, SEARCH
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.videobrowser);

		System.out.println("---- VideoBrowser -----oncreate-----");
		findViewById();
		initData();
		setViewListeners();
		setOnHokeyClickListeners();

		if (!CoreHandler.isNetworkConnected(this)) {
			pop_dialog.showWarning(R.string.network_fail, eixtLister);

		} else if (initMoudle() != 0) {
			pop_dialog.showWarning(R.string.init_fail, eixtLister);
		}
	}

	private int initMoudle() {
		Intent intent = getIntent();

		String config = intent.getStringExtra("config");
		String cate = intent.getStringExtra("category");
		byte[] parser = intent.getByteArrayExtra("parser");

		String p_name = null;
		Boolean bsearch = false;
		Boolean needLogin = false;

		do {
			try {
				JSONObject jo = new JSONObject(config);

				webName = jo.getString("name");
				p_name = jo.getString("parser");

				bLiveEnable = jo.getBoolean("live");
				needLogin = jo.getBoolean("login");
				bsearch = jo.getBoolean("search");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}

			if (bsearch) {
				sSearchType = new ArrayList<String>();
				dSearchType = new ArrayList<DATA_TYPE>();
				sSearchType.add("Search");
				dSearchType.add(DATA_TYPE.VIDEO);
			} else {
				mSearchButton.setVisibility(View.GONE);
			}

			if (core.initializeAll(parser, p_name, handlerNetCallBackData,
					GRID_PAGECONTENTSIZE, this, webName) != 0) {
				break;
			}

			if (needLogin) {
				Intent i = new Intent(this, LoginView.class);
				startActivityForResult(i, 0);
			} else {
				core.loadCategory(cate);
				bDataFromCategory = true;
				pop_dialog.showAnimation();
			}

			return 0;

		} while (false);

		return -1;
	}

	protected void addSearchType(String type, DATA_TYPE dataType) {
		if (!bSearchEnable) {
			return;
		}
		sSearchType.add(type);
		dSearchType.add(dataType);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		System.out.println("---- VideoBrowser -----onResume-----");
		core.setHandler(handlerNetCallBackData);
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		System.out.println("---- VideoBrowser -----onDestroy-----");
		if (gdAdapter != null) {
			gdAdapter.ClearCachePic();
		}

		if (core != null) {
			core.finalizeAll();
		}
		super.onDestroy();
	}

	private void deleteSelectVideo(boolean isHistory) {
		int pos = gd.getSelectedItemPosition();
		int currentPosition = getPageStartIndex() + pos;

		pop_dialog.closeEditVideoMenu();

		StoreManager<Content> mgr = isHistory ? StoreManager.getHisManager()
				: StoreManager.getFavManager();

		mgr.remove(currentPosition);

		int page = mgr.getTotalPage();

		if (page <= 0) {
			gdAdapter.clear();
			updatePageInfo(0, 0);
			curSelectButton.requestFocus();
			return;
		}

		if (page < mCurrentPageNum) {
			mCurrentPageNum = page;
			if (pos == 0) {
				pos = GRID_PAGECONTENTSIZE - 1;
			}
		}

		gdAdapter.setGridParam(getCurrentList());
		gd.setAdapter(gdAdapter);
		gd.setSelection(pos);
		updatePageInfo(mCurrentPageNum, page);

	}

	private void deleteAllVideo(final boolean isHistory) {
		final StoreManager<Content> mgr = isHistory ? StoreManager
				.getHisManager() : StoreManager.getFavManager();

		pop_dialog.closeEditVideoMenu();

		pop_dialog.showConfirm(R.string.editVideoWarning,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mgr.clear();
						gdAdapter.clear();
						updatePageInfo(0, 0);
						curSelectButton.requestFocus();
					}
				}, null);

	}

	private void updateListFormDetail(boolean isHistory, int mIndexInDetail) {
		int pos = mIndexInDetail;
		int page = 0;
		if (!isHistory) {
			StoreManager<Content> FavManager = StoreManager.getFavManager();

			do {
				page = FavManager.getTotalPage();
				if (page <= 0) {
					updatePageInfo(page, page);
					mFavButton.setSelected(false);
					mFavButton.requestFocus();
					break;
				}

				int index = getPageStartIndex() + mIndexInDetail;
				int size = FavManager.getList().size();
				int del = lastFavNum - size;

				if (del == 0) {
					break;
				}

				if (page < mCurrentPageNum) {
					// updatePageInfo(page, page);
					mCurrentPageNum = page;
					if (pos == 0) {
						pos = GRID_PAGECONTENTSIZE - 1;
					}
				} else if (index > size) {
					pos -= del;
				}

			} while (false);
		}

		gdAdapter.setGridParam(getCurrentList());
		gd.setAdapter(gdAdapter);
		// gdAdapter.notifyDataSetChanged();
		gd.setSelection(pos);
		if (!isHistory) {
			updatePageInfo(page, page);
		}
	}

	/**
	 * the data from the LoginPage.java
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			if (requestCode == 100) {
				if (gd.getCount() > 0) {
					int mIndexInDetail = data.getIntExtra("indexInDetail", 0);
					if (gd.getCount() != 0 && gdAdapter != null) {
						if (mSourceType == SOURCE_TYPE.FAV.ordinal()) {
							// for not update the videolist after delete fav in
							// detail page
							updateListFormDetail(false, mIndexInDetail);
						} else if (mSourceType == SOURCE_TYPE.HISTORY.ordinal()) {
							updateListFormDetail(true, mIndexInDetail); // for
																		// manstis
																		// 0239743
						} else {
							gd.setSelection(mIndexInDetail);
							gdAdapter.notifyDataSetChanged();
						}
					}
				}
			} else {
				core.setHandler(handlerNetCallBackData);
				if (data.getBooleanExtra("LoginCancel", false)) {
					// System.out.printf(" >>>> Exit APK <<");
					finish();
				} else if (data.getBooleanExtra("LoginSuccess", false)) {
					pop_dialog.showAnimation();
					String cate = getIntent().getStringExtra("category");
					core.loadCategory(cate);
					bDataFromCategory = true;
					pop_dialog.showAnimation();
					// core.loadContentPage(CategoryManager.SOURCE_TYPE.VOD,
					// null);
				}
			}
		} else {
			System.out.println("----onActivityResult-----data = null -------");
		}
	}

	private void findViewById() {
		mImagePageLeft = (ImageView) findViewById(R.id.IDC_GridView_video_pageback);
		mImagePageRight = (ImageView) findViewById(R.id.IDC_GridView_video_pagedown);
		mSearchButton = (ImageView) findViewById(R.id.OTT_MainPage_search);
		mVodButton = (ImageButton) findViewById(R.id.OTT_MainPage_VOD);
		mVodButton.setNextFocusUpId(R.id.OTT_MainPage_VOD);
		mLiveButton = (ImageButton) findViewById(R.id.OTT_MainPage_LIVE);
		mLiveButton.setNextFocusUpId(R.id.OTT_MainPage_LIVE);
		mFavButton = (ImageButton) findViewById(R.id.OTT_MainPage_FAV);
		mFavButton.setNextFocusUpId(R.id.OTT_MainPage_FAV);
		mHistoryButton = (ImageButton) findViewById(R.id.OTT_MainPage_HISTORY);
		mHistoryButton.setNextFocusUpId(R.id.OTT_MainPage_HISTORY);
		mPageCount = (TextView) findViewById(R.id.OTT_VIDEO_PageNum);
		mCurSelectPath = (TextView) findViewById(R.id.OTT_Cur_select_path);
		mCategory = (TextView) findViewById(R.id.home_hotkey_category);
		gd = (GridView) findViewById(R.id.IDC_GridView_video_mainpage_Grid);
		gd.setNumColumns(GRID_NUMCOL);
	}

	private void setViewListeners() {
		gd.setOnHoverListener(this);
		mSearchButton.setOnClickListener(this);
		mSearchButton.setOnHoverListener(this);
		// mCategroyButton.setOnClickListener(this);
		mVodButton.setOnClickListener(this);
		mVodButton.setOnHoverListener(this);
		mVodButton.setOnClickListener(this);
		mLiveButton.setOnClickListener(this);
		mLiveButton.setOnHoverListener(this);
		mHistoryButton.setOnClickListener(this);
		mHistoryButton.setOnHoverListener(this);
		mFavButton.setOnClickListener(this);
		mFavButton.setOnHoverListener(this);
		gd.setOnItemClickListener(this);
		// GuestSupport gs = new GuestSupport();
		// gd.setOnTouchListener(gs.onTouchListener);
		mImagePageLeft.setOnClickListener(pageClickListener);
		mImagePageRight.setOnClickListener(pageClickListener);

		mFavButton.setOnFocusChangeListener(buttonFocusChange);
		mVodButton.setOnFocusChangeListener(buttonFocusChange);
		mLiveButton.setOnFocusChangeListener(buttonFocusChange);
		mHistoryButton.setOnFocusChangeListener(buttonFocusChange);
		mSearchButton.setOnFocusChangeListener(buttonFocusChange);
		// mCategroyButton.setOnFocusChangeListener(buttonFocusChange);
		// curSelectButton.setOnFocusChangeListener(buttonFocusChange);

	}

	private void setOnHokeyClickListeners() {
		HoverEventManager hoverEvent = new HoverEventManager(VideoBrowser.this);
		SparseArray<KeyEvent> hotKeyMap = new SparseArray<KeyEvent>();

		hotKeyMap.put(R.id.home_hotkey_category, new KeyEvent(
				KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
		hotKeyMap.put(R.id.home_hotkey_exit, new KeyEvent(KeyEvent.ACTION_DOWN,
				KeyEvent.KEYCODE_BACK));
		hoverEvent.setHotkeyClickListener(hotKeyMap);
	}

	private void setButtonStatesUnSelect() {
		mVodButton.setSelected(false);
		mLiveButton.setSelected(false);
		mHistoryButton.setSelected(false);
		mFavButton.setSelected(false);
		mSearchButton.setSelected(false);
		// mCategroyButton.setSelected(false);
	}

	public void doForPopWindowBack() {

		// back from category,search,popmsg display the last status.
		setButtonStatesUnSelect();
		curSelectButton.setSelected(true);
		if (gd.getCount() > 0) {
			gd.requestFocus();
			gd.setSelection(0);
		} else {
			curSelectButton.setSelected(false);
			curSelectButton.requestFocus();
		}
	}

	@SuppressLint("UseSparseArrays")
	private void initData() {
		// TODO Auto-generated method stub
		mCurrentPageNum = 0;

		videobrowsr = this;
		gdAdapter = new VideoGridAdapter(gd, this);

		category_window = new category_popWindow(VideoBrowser.this);
		search_window = new SearchView(VideoBrowser.this);

		pop_dialog = new PopDialog(VideoBrowser.this, cancelTaskListener);

		core = CoreHandler.getInstace();

		mDataErrMsg = new HashMap<Integer, Integer>();
		mDataErrMsg.put(ALParser.PARSE_IOERROR, R.string.err_io);
		mDataErrMsg.put(ALParser.PARSE_ERROR, R.string.err_parse);
		mDataErrMsg.put(ALParser.PARSE_NORESULT, R.string.err_no_reslut);

	}

	private void updateGridViewInfo(List<Content> list) {
		List<Content> tmp = list;

		if (tmp == null && (tmp = getCurrentList()) == null) {
			return;
		}

		gdAdapter.setGridParam(tmp);
		gd.setAdapter(gdAdapter);

		if (tmp.size() > 0) {
			gd.requestFocus();
		}

		int pos = 0;
		if (turnToLeft == true) {
			pos = GRID_NUMCOL - 1;
			if (gdAdapter.getCount() < GRID_NUMCOL) {
				pos = gdAdapter.getCount() - 1;
			}
		}

		gd.setSelection(pos);
	}

	private void updatePathInfo(String path) {
		String s = webName + "/" + mDataSource[mSourceType];
		if (path != null) {
			s += path;
		}
		mCurSelectPath.setText(s);
	}

	private void updatePageInfo(int currentPage, int totalPage) {

		if (totalPage == 0 && gdAdapter.getCount() > 0) {
			mPageCount.setText(currentPage + "/" + "--");
		} else {
			mPageCount.setText(currentPage + "/" + totalPage);
		}

		mCurrentPageNum = currentPage;
		mTotalPage = totalPage;

		mImagePageLeft.setVisibility(View.VISIBLE);
		mImagePageRight.setVisibility(View.VISIBLE);

		if (gdAdapter.getCount() == 0 || mTotalPage == 1) {
			mImagePageLeft.setVisibility(View.GONE);
			mImagePageRight.setVisibility(View.GONE);

		} else if (mTotalPage == 0 && currentPage == 1) {
			mImagePageLeft.setVisibility(View.GONE);
		}

	}

	@SuppressLint("HandlerLeak")
	Handler handlerNetCallBackData = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == CoreHandler.Callback.CBK_GET_CATEGORY_DONE
					.ordinal()) {
				if (CategoryManager.containVod()) {
					if (!CategoryManager.containLive()) {
						mLiveButton.setBackground(getResources().getDrawable(
								R.drawable.homepage_live_unsupport));
						((TextView) findViewById(R.id.live_text))
								.setTextColor(Color.GRAY);
						mLiveButton.setFocusable(false);
						mLiveButton.setClickable(false);
					}
					mVodButton.setSelected(true);
					lastSelectButton = curSelectButton = mVodButton;
					mSourceType = SOURCE_TYPE.VOD.ordinal();
					core.loadContentPage(CategoryManager.SOURCE_TYPE.VOD, null);
					updatePathInfo(null);
				} else if (CategoryManager.containLive()) {
					mVodButton.setFocusable(false);
					mVodButton.setClickable(false);
					mVodButton.setBackground(getResources().getDrawable(
							R.drawable.homepage_vod_unsupport));
					((TextView) findViewById(R.id.vod_text))
							.setTextColor(Color.GRAY);

					mLiveButton.setSelected(true);
					lastSelectButton = curSelectButton = mLiveButton;
					mSourceType = SOURCE_TYPE.LIVE.ordinal();
					core.loadContentPage(CategoryManager.SOURCE_TYPE.LIVE, null);
				}
				// gd.requestFocus();
			} else if (msg.what == CoreHandler.Callback.CBK_GET_CONTENT_FAIL
					.ordinal()) {
				bDataFromCategory = false;
				pop_dialog.closeAnimation();
				
				if(curSelectButton != lastSelectButton){
					gdAdapter.clear();
					updatePageInfo(0, 0);
					updatePathInfo(null);
					mCategory.setVisibility(View.VISIBLE);
					mCategory.setText(R.string.hint_category);	
					lastSelectButton = curSelectButton;
				}
				
				if (!WebManager.reportBug(msg.arg2, msg.what,
						(Content) msg.obj, null)) {
					pop_dialog.showWarning(mDataErrMsg.get(msg.arg2),
							null);
				}

			} else if (msg.what == CoreHandler.Callback.CBK_GET_CATEGORY_FAIL
					.ordinal()) {
				pop_dialog.closeAnimation();

			} else if (msg.what == CoreHandler.Callback.CBK_CANCEL_CMOMAND
					.ordinal()) {


			} else if (msg.what == CoreHandler.Callback.CBK_GET_CONTENT_DONE
					.ordinal()) {
				bDataFromCategory = false;
				pop_dialog.closeAnimation();

				if (curSelectButton != lastSelectButton) {
					mCategory.setVisibility(View.VISIBLE);
					mCategory.setText(R.string.hint_category);
					lastSelectButton = curSelectButton;
				}

				ContentManager mgr = ContentManager.getCurrent();
				updateGridViewInfo(mgr.getCurrentList());
				updatePageInfo(mgr.getCurrentPage(), mgr.getTotalPage());
				updatePathInfo(mgr.getCurrentPath());

			} else if (msg.what == CoreHandler.Callback.CBK_NETWORK_CONNECT_FAIL
					.ordinal()) {
				pop_dialog.closeAnimation();
				pop_dialog.showWarning(R.string.network_fail, null);
			} else if (msg.what == CoreHandler.Callback.CBK_REPORT_BUG_DONE
					.ordinal()) {
				pop_dialog.showToast(R.string.report_ok);
			}
		}
	};

	private void stopLoadPic() {
		gdAdapter.CancleTask();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (curSelectButton == mVodButton
					|| (bLiveEnable && curSelectButton == mLiveButton)) {
				category_window.showCategoryPopWindow();

			} else if (curSelectButton == mFavButton && gd.hasFocus()) {
				if (StoreManager.getFavManager().getList().size() > 0) {

					boolean isMouse = gd.getSelectedItemPosition() < 0;
					pop_dialog.showEditVideoMenu(R.string.editFavoriteTitle,
							isMouse, new android.view.View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									deleteSelectVideo(false);
								}
							}, new android.view.View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									deleteAllVideo(false);
								}
							});
				}

			} else if (curSelectButton == mHistoryButton && gd.hasFocus()) {
				if (StoreManager.getHisManager().getList().size() > 0) {

					boolean isMouse = gd.getSelectedItemPosition() < 0;
					pop_dialog.showEditVideoMenu(R.string.editHistoryTitle,
							isMouse, new android.view.View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									deleteSelectVideo(true);
								}
							}, new android.view.View.OnClickListener() {
								@Override
								public void onClick(View arg0) {
									deleteAllVideo(true);
								}
							});
				}

			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (gd.hasFocus()) {
				int pos = gd.getSelectedItemPosition();
				if ((pos == GRID_NUMCOL - 1 || pos == gdAdapter.getCount() - 1)) {
					updateNextPage(false);
				}
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (gd.hasFocus()) {
				if ((gd.getSelectedItemPosition() % GRID_NUMCOL == 0)) {
					updateNextPage(true);
				}
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			curSelectButton.setSelected(true);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			setButtonStatesUnSelect();
			curSelectButton.requestFocus();
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (ContentManager.getCurrentLevel() > 1
					&& mSourceType != SOURCE_TYPE.HISTORY.ordinal()
					&& mSourceType != SOURCE_TYPE.FAV.ordinal()) {
				core.cancleTask();

				ContentManager mgr = ContentManager.getLast();

				updateGridViewInfo(mgr.getCurrentList());
				updatePageInfo(mgr.getCurrentPage(), mgr.getTotalPage());
				updatePathInfo(mgr.getCurrentPath());

				gd.setSelection(currentIndex);
				gd.requestFocus();
				return true;
			} else {
				pop_dialog.showConfirm(R.string.exitapp,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								finish();
							}
						}, null);

				return true;
			}

		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

		pop_dialog.closeAnimation();
		stopLoadPic();
		Intent intent = new Intent();
		intent.setClass(VideoBrowser.this, DetaiInfoView.class);

		if (ContentManager.getCurrent() != null
				&& (mSourceType != SOURCE_TYPE.HISTORY.ordinal() && mSourceType != SOURCE_TYPE.FAV
						.ordinal())) {
			Content currentIndexContent = ContentManager.getCurrent().getNode(
					arg2);
			if (currentIndexContent.getContentType() == DATA_TYPE.CONTAINER
					&& currentIndexContent.getPlayFlag()) {
				pop_dialog.showAnimation();
				currentIndex = arg2;
				core.loadContentPage(null, currentIndexContent);
				return;

			} else {
				if (currentIndexContent.getPlayFlag() == false) {
					pop_dialog.showWarning(R.string.not_support_play,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
								}

							});
					return;
				}
			}
		}

		if (mSourceType == SOURCE_TYPE.FAV.ordinal()) {
			lastFavNum = StoreManager.getFavManager().getList().size();
		}

		intent.putExtra("sourceType", mSourceType); // vod live hsitory fav
		// intent.putExtra("VideoInfo", currentIndexContent);
		// intent.putExtra("curpagenum", mCurrentPageNum - 1);
		intent.putExtra("pageStart", getPageStartIndex());
		intent.putExtra("selectIndex", arg2); // the current index of selected
		// intent.putExtra("filePath", mCurSelectPath.getText().toString());

		startActivityForResult(intent, 100);
	}

	public int getPageStartIndex() {
		return (mCurrentPageNum - 1) * GRID_PAGECONTENTSIZE;
	}

	public static VideoBrowser getInstance() {
		return videobrowsr;
	}

	protected List<Content> getCurrentList() {
		List<Content> out = null;

		do {
			if (mSourceType == SOURCE_TYPE.FAV.ordinal()) {
				out = StoreManager.getFavManager().getList();

			} else if (mSourceType == SOURCE_TYPE.HISTORY.ordinal()) {
				out = StoreManager.getHisManager().getList();

			} else {
				out = ContentManager.getCurrent().getCurrentList();
				break;
			}

			if (out.size() == 0) {
				break;
			}

			int pageStartIndex = (mCurrentPageNum - 1) * GRID_PAGECONTENTSIZE;
			int endIndex = pageStartIndex + GRID_PAGECONTENTSIZE;

			if (endIndex > out.size()) {
				endIndex = out.size();

				if (endIndex == pageStartIndex) {
					pageStartIndex -= GRID_PAGECONTENTSIZE;
				}
			}

			out = new ArrayList<Content>(out.subList(pageStartIndex, endIndex));

		} while (false);

		return out;
	}

	private View curSelectButton;
	private View lastSelectButton;

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		// setButtonStatesUnSelect();
		lastSelectButton = curSelectButton;
		curSelectButton = arg0;

		lastSelectButton.setSelected(false);
		lastSelectButton.setHovered(false);
		curSelectButton.setSelected(true);

		if (arg0.getId() == R.id.OTT_MainPage_search) {
			System.out.println("------ on click  search ------");
			mCategory.setVisibility(View.GONE);
			search_window.showSearchPopWindow(sSearchType, dSearchType);
			return;

		} else if (arg0.getId() == R.id.OTT_MainPage_VOD) {
			updateVodInfo();

		} else if (arg0.getId() == R.id.OTT_MainPage_LIVE) {
			updateLiveInfo();

		} else if (arg0.getId() == R.id.OTT_MainPage_HISTORY) {
			// mCategory.setVisibility(View.GONE);
			mCategory.setVisibility(View.VISIBLE);
			mCategory.setText(R.string.hint_edit);
			updateHistoryInfo(arg0);

		} else if (arg0.getId() == R.id.OTT_MainPage_FAV) {
			// mCategory.setVisibility(View.GONE);
			mCategory.setVisibility(View.VISIBLE);
			mCategory.setText(R.string.hint_edit);
			updateFavInfo(arg0);

		}
	}

	private void updateNextPage(Boolean toLeft) {
		int page = toLeft == true ? mCurrentPageNum - 1 : mCurrentPageNum + 1;

		if ((mTotalPage == 0 && page == 0) || mTotalPage == 1) {
			return;

		} else if (mTotalPage > 1) {
			if (page < 1) {
				page = mTotalPage;
			} else if (page > mTotalPage) {
				page = 1;
			}
		}

		turnToLeft = toLeft;
		stopLoadPic();

		if (mSourceType == SOURCE_TYPE.HISTORY.ordinal()
				|| mSourceType == SOURCE_TYPE.FAV.ordinal()) {
			mCurrentPageNum = page;
			gdAdapter.setGridParam(getCurrentList());
			gdAdapter.notifyDataSetChanged();
			updatePageInfo(mCurrentPageNum, mTotalPage);
			// gd.setAdapter(gdAdapter);
			gd.requestFocus();
			if (turnToLeft == true) {
				gd.setSelection(GRID_NUMCOL - 1);
				turnToLeft = false;
			} else {
				gd.setSelection(0);
			}

		} else {
			core.loadPageList(page);
			pop_dialog.showAnimation();
		}

	}

	private void updateVodInfo() {
		mSourceType = SOURCE_TYPE.VOD.ordinal();

		category_window.clearSelecPosition();

		core.loadContentPage(CategoryManager.SOURCE_TYPE.VOD, null);
		pop_dialog.showAnimation();
	}

	private void updateLiveInfo() {
		category_window.clearSelecPosition();
		mSourceType = SOURCE_TYPE.LIVE.ordinal();

		core.loadContentPage(CategoryManager.SOURCE_TYPE.LIVE, null);
		pop_dialog.showAnimation();
	}

	private void updateHistoryInfo(View view) {
		category_window.clearSelecPosition();
		mSourceType = SOURCE_TYPE.HISTORY.ordinal();

		StoreManager<Content> historyManager = StoreManager.getHisManager();

		int page = historyManager.getTotalPage();
		if (page <= 0) {
			view.setSelected(false);
			view.requestFocus();
		}
		mCurrentPageNum = page > 0 ? 1 : 0;
		updateGridViewInfo(null);
		updatePageInfo(mCurrentPageNum, page);
		updatePathInfo(null);

	}

	private void updateFavInfo(View view) {
		mSourceType = SOURCE_TYPE.FAV.ordinal();
		StoreManager<Content> favManager = StoreManager.getFavManager();

		int page = favManager.getTotalPage();

		if (page <= 0) {
			view.setSelected(false);
			view.requestFocus();
		}
		mCurrentPageNum = page > 0 ? 1 : 0;
		updateGridViewInfo(null);
		updatePageInfo(mCurrentPageNum, page);
		updatePathInfo(null);

	}

	OnFocusChangeListener buttonFocusChange = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if (curSelectButton == v) {
				if (hasFocus) {
					curSelectButton.setSelected(false);
				} else {
					curSelectButton.setSelected(true);
				}
			}
		}
	};

	DialogInterface.OnKeyListener cancelTaskListener = new OnKeyListener() {
		@Override
		public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
			// TODO Auto-generated method stub
			if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
				if (arg1 == KeyEvent.KEYCODE_BACK) {
					if (bDataFromCategory) {
						pop_dialog.showConfirm(R.string.exitapp,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										bDataFromCategory = false;
										core.cancleTask();
										finish();
									}
								}, null);
					} else {
						core.cancleTask();
						pop_dialog.closeAnimation();
						if (curSelectButton != lastSelectButton) {
							lastSelectButton.requestFocus();
							lastSelectButton.setSelected(true);
							curSelectButton.setSelected(false);
							curSelectButton = lastSelectButton;
						}						
					}

				}
			}
			return false;
		}
	};

	DialogInterface.OnClickListener eixtLister = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			finish();
		}
	};

	View.OnClickListener pageClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			updateNextPage(v.getId() == R.id.IDC_GridView_video_pageback);

		}
	};

	/**
	 * get the search data by searchContent
	 * 
	 * @param searchType
	 *            VIDEO or CONTAINER
	 * @param searchContent
	 *            the content of the search
	 */
	public void getSearchVideoData(DATA_TYPE searchType, String searchContent) {
		setButtonStatesUnSelect();
		mSearchButton.setSelected(true);
		curSelectButton = mSearchButton;
		mSourceType = SOURCE_TYPE.SEARCH.ordinal();
		core.loadSearchPage(searchType, searchContent);
		pop_dialog.showAnimation();

	}

	public void getDataFromCategory(int... index) {
		bDataFromCategory = true;
		pop_dialog.showAnimation();

		core.loadContentPage(CategoryManager.SOURCE_TYPE.values()[mSourceType],
				CategoryManager.getCurrent().getNodeByIndex(index));
	}

	@Override
	public boolean onHover(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_HOVER_ENTER:
			v.requestFocusFromTouch();
			break;
		}
		return false;
	}
}