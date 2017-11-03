package com.ott.webtv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ott.webtv.core.CategoryManager;
import com.ott.webtv.core.ContentManager;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.StoreManager;
import com.ott.webtv.core.DataNode.Category;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.DATA_TYPE;
import com.ott.webtv.R;

public class VideoBrowser extends Activity implements OnItemClickListener,
		OnClickListener {
	private GridView gd = null;
	private final int GRID_NUMCOL = 5;
	private final int GRID_PAGECONTENTSIZE = GRID_NUMCOL * 2;
	public VideoGridAdapter gdAdapter;
	public ImageView mImagePageLeft, mImagePageRight;
	private ImageView mSearchButton;
	private ImageView mCategroyButton;
	private ImageButton mVodButton, mLiveButton, mFavButton, mHistoryButton;
	private TextView mPageCount, mCurSelectPath;
	// private AlertDialog bufferDialog;
	// private View dialoglayout;

	private int mTotalPage, mCurrentPageNum;

	private ArrayList<HashMap<String, String>> urlList = new ArrayList<HashMap<String, String>>();

	private category_popWindow category_window = null;
	private SearchView search_window = null;
	private PopDialog pop_dialog = null;

	private String webName = "";

	private String[] mDataSource = { "VOD", "Live", "History", "Favorite",
			"Search" };

	private CoreHandler core;
	private int mSourceType = SOURCE_TYPE.VOD.ordinal();

	private CategoryManager mCategorymg;

	private List<String> sSearchType;
	private List<DATA_TYPE> dSearchType;
	private Boolean bSearchEnable;
	private Boolean bLiveEnable;

	private Boolean bInContainer = false;
	private int currentIndex;

	private String currentPath;
	private String lastPath;

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

		if (!core.isNetworkConnected(this)) {
			pop_dialog.showWarning(
					getResources().getString(R.string.network_fail),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							finish();
						}

					});

		} else {
			initMoudle();
		}

	}

	private void initMoudle() {
		Intent intent = getIntent();

		String config = intent.getStringExtra("config");
		String cate = intent.getStringExtra("category");
		byte[] parser = intent.getByteArrayExtra("parser");

		String p_name = null;
		Boolean bsearch = false;
		Boolean needLogin = false;
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
		}

		if (bsearch) {
			sSearchType = new ArrayList<String>();
			dSearchType = new ArrayList<DATA_TYPE>();
			sSearchType.add("Search");
			dSearchType.add(DATA_TYPE.VIDEO);
		}

		final String rootPath = getApplicationInfo().dataDir + "/" + webName;

		core.initializeAll(parser, p_name, handlerNetCallBackData, rootPath,
				GRID_PAGECONTENTSIZE);

		core.loadCategory(cate);

		if (needLogin) {
			Intent i = new Intent(this, LoginView.class);
			startActivityForResult(i, 0);
			return;
		}

		pop_dialog.showAnimation();
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
		super.onResume();
		System.out.println("---- VideoBrowser -----onResume-----");
		core.setHandler(handlerNetCallBackData);
		if (gdAdapter != null && gd.getVisibility() == View.VISIBLE) {
			gdAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("---- VideoBrowser -----onDestroy-----");
		if (gdAdapter != null) {
			gdAdapter.ClearCachePic();
		}

		if (core != null) {
			System.out.println("------on destory -------");
			core.finalizeAll();
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
					gd.setSelection(data.getIntExtra("indexInDetail", 0));
					gdAdapter.notifyDataSetChanged();
				}
			} else {
				core.setHandler(handlerNetCallBackData);
				if (data.getBooleanExtra("LoginCancel", false)) {
					// System.out.printf(" >>>> Exit APK <<");
					finish();
				} else if (data.getBooleanExtra("LoginSuccess", false)) {
					pop_dialog.showAnimation();
					core.loadContentPage(CategoryManager.SOURCE_TYPE.VOD, null);
				}
			}
		} else {
			System.out.println("------data = null -------");
		}
	}

	private void findViewById() {
		mImagePageLeft = (ImageView) findViewById(R.id.IDC_GridView_video_pageback);
		mImagePageRight = (ImageView) findViewById(R.id.IDC_GridView_video_pagedown);
		mSearchButton = (ImageView) findViewById(R.id.OTT_MainPage_search);
		mVodButton = (ImageButton) findViewById(R.id.OTT_MainPage_VOD);
		// mVodButton.setNextFocusUpId(R.id.OTT_MainPage_VOD);
		mLiveButton = (ImageButton) findViewById(R.id.OTT_MainPage_LIVE);
		mLiveButton.setNextFocusUpId(R.id.OTT_MainPage_LIVE);
		mFavButton = (ImageButton) findViewById(R.id.OTT_MainPage_FAV);
		mFavButton.setNextFocusUpId(R.id.OTT_MainPage_FAV);
		mHistoryButton = (ImageButton) findViewById(R.id.OTT_MainPage_HISTORY);
		mHistoryButton.setNextFocusUpId(R.id.OTT_MainPage_HISTORY);
		mPageCount = (TextView) findViewById(R.id.OTT_VIDEO_PageNum);
		mCurSelectPath = (TextView) findViewById(R.id.OTT_Cur_select_path);
		mCategroyButton = (ImageView) findViewById(R.id.OTT_MainPage_Categroy);
		gd = (GridView) findViewById(R.id.IDC_GridView_video_mainpage_Grid);
		gd.setNumColumns(GRID_NUMCOL);
		lastFocusButton = mVodButton;
	}

	private void setViewListeners() {
		mSearchButton.setOnClickListener(this);
		mCategroyButton.setOnClickListener(this);
		mVodButton.setOnClickListener(this);
		mLiveButton.setOnClickListener(this);
		mHistoryButton.setOnClickListener(this);
		mFavButton.setOnClickListener(this);
		gd.setOnItemClickListener(this);
		GuestSupport gs = new GuestSupport();
		gd.setOnTouchListener(gs.onTouchListener);

		mFavButton.setOnFocusChangeListener(buttonFocusChange);
		mVodButton.setOnFocusChangeListener(buttonFocusChange);
		mLiveButton.setOnFocusChangeListener(buttonFocusChange);
		mHistoryButton.setOnFocusChangeListener(buttonFocusChange);
		mSearchButton.setOnFocusChangeListener(buttonFocusChange);
		mCategroyButton.setOnFocusChangeListener(buttonFocusChange);
		lastFocusButton.setOnFocusChangeListener(buttonFocusChange);

		// pop_dialog.setListener(dialogBackListener, cancelTaskListener);
	}

	private void setButtonStatesUnSelect() {
		mVodButton.setSelected(false);
		mLiveButton.setSelected(false);
		mHistoryButton.setSelected(false);
		mFavButton.setSelected(false);
		mSearchButton.setSelected(false);
		mCategroyButton.setSelected(false);
	}

	public void doForPopWindowBack() {

		// back from category,search,popmsg display the last status.
		setButtonStatesUnSelect();
		lastFocusButton.setSelected(true);
		if (gd.getCount() > 0) {
			gd.requestFocus();
			gd.setSelection(0);
		} else {
			lastFocusButton.setSelected(false);
			lastFocusButton.requestFocus();
		}
	}

	private void initData() {
		// TODO Auto-generated method stub
		mCurrentPageNum = 0;

		gdAdapter = new VideoGridAdapter();

		category_window = new category_popWindow(VideoBrowser.this);
		search_window = new SearchView(VideoBrowser.this);

		pop_dialog = new PopDialog(VideoBrowser.this, cancelTaskListener);

		mCategorymg = CategoryManager.getInstace();
		core = CoreHandler.getInstace();

	}

	private void updateGridViewInfo(String[] urlPic, String[] title) {
		urlList.clear();
		if (urlPic != null) {
			for (int i = 0; i < urlPic.length; i++) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("picUrl", urlPic[i]);
				map.put("videoName", title[i]);
				urlList.add(map);
			}
		}

		gdAdapter.setGridParam(VideoBrowser.this, urlList,
				GRID_PAGECONTENTSIZE, 1);
		gd.setAdapter(gdAdapter);

		if (urlList.size() > 0) {
			gd.requestFocus();
		}

		if (turnToLeft == true) {
			gd.setSelection(GRID_NUMCOL - 1);
			turnToLeft = false;
		} else {
			gd.setSelection(0);
		}
	}

	private void updatePathInfo(String path) {
		String s = webName + "/" + mDataSource[mSourceType];
		if (path != null) {
			s += path;
		}
		mCurSelectPath.setText(s);
		lastPath = currentPath = path;
	}

	private void updatePageInfo(int currentPage, int totalPage) {

		mPageCount.setText(currentPage + "/" + totalPage);
		mCurrentPageNum = currentPage;
		mTotalPage = totalPage;

		mImagePageLeft.setVisibility(View.VISIBLE);
		mImagePageRight.setVisibility(View.VISIBLE);

		if (mTotalPage <= 1) {
			mImagePageLeft.setVisibility(View.GONE);
			mImagePageRight.setVisibility(View.GONE);
		} else if (mCurrentPageNum <= 1) {
			mImagePageLeft.setVisibility(View.GONE);
		} else if (mCurrentPageNum >= mTotalPage) {
			mImagePageRight.setVisibility(View.GONE);
		}
	}

	@SuppressLint("HandlerLeak")
	Handler handlerNetCallBackData = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == CoreHandler.Callback.CBK_GET_CATEGORY_DONE
					.ordinal()) {
				core.loadContentPage(CategoryManager.SOURCE_TYPE.VOD, null);
				mVodButton.setSelected(true);
				gd.requestFocus();
			} else if (msg.what == CoreHandler.Callback.CBK_GET_CONTENT_FAIL
					.ordinal()) {
				pop_dialog.closeAnimation();
				updatePathInfo(currentPath);
				pop_dialog.showWarning("" + msg.obj, focusChangeLister);
			} else if (msg.what == CoreHandler.Callback.CBK_GET_CATEGORY_FAIL
					.ordinal()) {
				pop_dialog.closeAnimation();

			} else if (msg.what == CoreHandler.Callback.CBK_CANCEL_CMOMAND
					.ordinal()) {
				setButtonStatesUnSelect();
				if (msg.arg1 == 1) {
					lastFocusButton.requestFocus();
				} else {
					lastFocusButton.setSelected(true);
					gd.requestFocus();
				}

			} else if (msg.what == CoreHandler.Callback.CBK_GET_CONTENT_DONE
					.ordinal()) {
				pop_dialog.closeAnimation();
				ContentManager mgr = ContentManager.getCurrent();
				updateGridViewInfo(mgr.getPicURLs(), mgr.getTitles());
				updatePageInfo(mgr.getCurrentPage(), mgr.getTotalPage());
				updatePathInfo(currentPath);
			}
		}
	};

	private void stopLoadPic() {
		gdAdapter.CancleTask();
	}

	private void turnToNextPage(int mCurrentPageNum) {
		mImagePageRight.setVisibility(View.VISIBLE);
		mImagePageLeft.setVisibility(View.VISIBLE);
		mPageCount.setText(mCurrentPageNum + "/" + mTotalPage);

		if (mCurrentPageNum >= mTotalPage) {
			mImagePageRight.setVisibility(View.GONE);
		}
		if (mCurrentPageNum > 1) {
			mImagePageLeft.setVisibility(View.VISIBLE);
		}
		gdAdapter.setGridParam(VideoBrowser.this, urlList,
				GRID_PAGECONTENTSIZE, mCurrentPageNum);
		gd.setAdapter(gdAdapter);
		gd.requestFocus();
		if (turnToLeft == true) {
			gd.setSelection(GRID_NUMCOL - 1);
			turnToLeft = false;
		} else {
			gd.setSelection(0);
		}
	}

	private boolean turnToLeft = false;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (lastFocusButton == mVodButton || (bLiveEnable && lastFocusButton == mLiveButton) ) {
				category_window.showCategoryPopWindow();
			} else {
				pop_dialog.showToast(R.string.no_category);
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (gd.hasFocus()) {
				if ((gd.getSelectedItemPosition() % GRID_NUMCOL == GRID_NUMCOL - 1)
						&& (mCurrentPageNum < mTotalPage)) {
					turnToLeft = false;
					if (mSourceType == SOURCE_TYPE.VOD.ordinal()
							|| mSourceType == SOURCE_TYPE.LIVE.ordinal()
							|| mSourceType == SOURCE_TYPE.SEARCH.ordinal()) {
						stopLoadPic();
						core.loadPageList(mCurrentPageNum + 1);
						pop_dialog.showAnimation();
					} else {
						mCurrentPageNum++;
						turnToNextPage(mCurrentPageNum);
					}
				}
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {

			if (gd.hasFocus()) {
				if ((gd.getSelectedItemPosition() % GRID_NUMCOL == 0)
						&& (mCurrentPageNum > 1)) {
					turnToLeft = true;
					if (mSourceType == SOURCE_TYPE.VOD.ordinal()
							|| mSourceType == SOURCE_TYPE.LIVE.ordinal()
							|| mSourceType == SOURCE_TYPE.SEARCH.ordinal()) {
						stopLoadPic();
						core.loadPageList(mCurrentPageNum - 1);
						pop_dialog.showAnimation();
						return true;
					} else {
						mCurrentPageNum--;
						turnToNextPage(mCurrentPageNum);
					}
				}
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			lastFocusButton.setSelected(true);
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (bInContainer) {
				bInContainer = false;

				ContentManager mgr = ContentManager.getLast();

				updateGridViewInfo(mgr.getPicURLs(), mgr.getTitles());
				updatePageInfo(mgr.getCurrentPage(), mgr.getTotalPage());
				updatePathInfo(lastPath);

				gd.setSelection(currentIndex);
				gd.requestFocus();

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

		if (ContentManager.getCurrent() != null) {
			Content currentIndexContent = ContentManager.getCurrent().getNode(
					arg2);
			if (currentIndexContent.getContentType() == DATA_TYPE.CONTAINER) {
				pop_dialog.showAnimation();
				bInContainer = true;
				currentIndex = arg2;
				currentPath += currentIndexContent.toString();
				core.loadContentPage(CategoryManager.SOURCE_TYPE.VOD,
						currentIndexContent);
				return;
			}
		}
		intent.putExtra("sourceType", mSourceType); // vod live hsitory fav
		// intent.putExtra("VideoInfo", currentIndexContent);
		intent.putExtra("selectIndex", arg2); // the current index of selected
		intent.putExtra("filePath", mCurSelectPath.getText().toString());

		intent.putExtra("curPageList", gdAdapter.getCurPageContent());
		startActivityForResult(intent, 100);
	}

	private View lastFocusButton;

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		//setButtonStatesUnSelect();
		lastFocusButton.setSelected(false);
		arg0.setSelected(true);
		
		if (arg0.getId() == R.id.OTT_MainPage_search) {
			System.out.println("------ on click  search ------");
			search_window.showSearchPopWindow(sSearchType, dSearchType);
			return;
		} else if (arg0.getId() == R.id.OTT_MainPage_Categroy) {
			System.out.println("------ on click  Categroy ------");
			if (lastFocusButton == mVodButton || lastFocusButton == mLiveButton) {
				lastFocusButton.setSelected(true);
				category_window.showCategoryPopWindow();
			} else {
				pop_dialog.showToast(R.string.no_category);
			}
			return;
		} else if (arg0.getId() == R.id.OTT_MainPage_VOD) {
			updateVodInfo();
			
		} else if (arg0.getId() == R.id.OTT_MainPage_LIVE) {
			updateLiveInfo();

		} else if (arg0.getId() == R.id.OTT_MainPage_HISTORY) {
			updateHistoryInfo(arg0);
			
		} else if (arg0.getId() == R.id.OTT_MainPage_FAV) {
			updateFavInfo(arg0);
			
		}
		lastFocusButton = arg0;
	}

	private void updateVodInfo() {
		mSourceType = SOURCE_TYPE.VOD.ordinal();
		clearDisplay();
		pop_dialog.showAnimation();

		category_window.clearSelecPosition();

		core.loadContentPage(CategoryManager.SOURCE_TYPE.VOD, null);

	}

	private void updateLiveInfo() {
		category_window.clearSelecPosition();
		mSourceType = SOURCE_TYPE.LIVE.ordinal();
		clearDisplay();
		
		if (bLiveEnable) {
			pop_dialog.showAnimation();
			core.loadContentPage(CategoryManager.SOURCE_TYPE.LIVE, null);
		} else {
			pop_dialog.showWarning(
					getResources().getString(R.string.not_support_live),
					focusChangeLister);
		}

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

		updateGridViewInfo(historyManager.getPicURLs(),
				historyManager.getTitles());

		updatePageInfo(page > 0 ? 1 : 0, page);
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

		updateGridViewInfo(favManager.getPicURLs(), favManager.getTitles());

		updatePageInfo(page > 0 ? 1 : 0, page);
		updatePathInfo(null);

	}

	private void clearDisplay() {
		updateGridViewInfo(null, null);
		updatePageInfo(0, 0);
		updatePathInfo(null);
	}

	OnFocusChangeListener buttonFocusChange = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO Auto-generated method stub
			if (lastFocusButton == v) {
				if (hasFocus) {
					lastFocusButton.setSelected(false);
				} else {
					lastFocusButton.setSelected(true);
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
					pop_dialog.closeAnimation();
					core.cancleTask();
				}
			}
			return false;
		}
	};

	DialogInterface.OnClickListener focusChangeLister = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			lastFocusButton.setSelected(false);
			lastFocusButton.requestFocus();
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
		lastFocusButton = mSearchButton;
		mSourceType = SOURCE_TYPE.SEARCH.ordinal();
		pop_dialog.showAnimation();
		clearDisplay();
		updatePathInfo("/" + searchContent);
		core.loadSearchPage(searchType, searchContent);

	}

	public String[] getCategoryData() {
		return mCategorymg
				.getTitles(CategoryManager.SOURCE_TYPE.values()[mSourceType]);
	}

	public void getDataFromCategory(int lv1, int lv2, int lv3) {
		pop_dialog.showAnimation();
		mCategroyButton.setSelected(true);
		Category currentNode = mCategorymg.getNode(lv1);

		currentPath = currentNode.toString();

		if (lv2 != -1) {
			currentNode = currentNode.getSubNode(lv2);
			currentPath += currentNode.toString();

			if (lv3 != -1) {
				currentNode = currentNode.getSubNode(lv3);
				currentPath += currentNode.toString();
			}
		}
		core.loadContentPage(CategoryManager.SOURCE_TYPE.values()[mSourceType],
				currentNode);
		// clearDisplay();
	}

	class GuestSupport {
		// use to support mouse
		int startX = 0, startY = 0, endX = 0, endY = 0;
		public OnTouchListener onTouchListener = new OnTouchListener() {
			@Override
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
			if (Math.abs(offsetX) > 200 && Math.abs(offsetY) < 50) {
				if (offsetX < 0) {
					if (mSourceType == SOURCE_TYPE.VOD.ordinal()
							|| mSourceType == SOURCE_TYPE.LIVE.ordinal()
							|| mSourceType == SOURCE_TYPE.SEARCH.ordinal()) {
						gdAdapter.CancleTask();
						core.loadPageList(mCurrentPageNum + 1);
						pop_dialog.showAnimation();
					} else {
						mCurrentPageNum++;
						turnToNextPage(mCurrentPageNum);
					}
					return;
				} else {
					if ((mCurrentPageNum > 1)) {
						if (mSourceType == SOURCE_TYPE.VOD.ordinal()
								|| mSourceType == SOURCE_TYPE.LIVE.ordinal()
								|| mSourceType == SOURCE_TYPE.SEARCH.ordinal()) {
							gdAdapter.CancleTask();
							core.loadPageList(mCurrentPageNum - 1);
							pop_dialog.showAnimation();
							return;
						} else {
							mCurrentPageNum--;
							turnToNextPage(mCurrentPageNum);
						}
					}
					return;
				}
			}
		}
	}
}