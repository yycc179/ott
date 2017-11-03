package com.ott.webtv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ott.webtv.R;
import com.ott.webtv.WebContent.TYPE_E;
import com.ott.webtv.core.ALParser;
import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.HttpUtils;
import com.ott.webtv.core.StoreManager;
import com.ott.webtv.core.DataNode.BaseNode;
import com.ott.webtv.core.url_parser.URLParser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class WebManager extends Activity implements OnItemClickListener,
		View.OnHoverListener {

	private static final int GRID_NUMCOL = 4;
	private static final int GRID_PAGESIZE = 12;

	private static final int DEVICE_LOGIN_DONE = 0;
	private static final int DEVICE_LOGIN_FAIL = -1;
	private static final int GET_SOURCE_DONE = 1;
	private static final int ACCESS_SITE_DONE = 2;

	private static final String sContentTypeJson = "application/json";
	private static final String sLoginUrl = "/OTTS/odata/Device/OTTS.Models.Login";
	private static final String sCommonParserUrl = "/OTTS/Content/Sites/Common/Parser.jar";
	private static final String sBugReportUrl = "/OTTS/odata/Error/OTTS.Models.Report";

	private static String serverAddr;
	private static String currentSiteId;

	private WebGridView mGirdView;
	private WebGridAdapter gdAdapter;
	private ImageView mImagePageLeft;
	private ImageView mImagePageRight;
	private TextView mPageCount;
	private int mTotalPage;
	private int mCurrentPage = 1;
	private static PopDialog pop_dialog;

	private List<WebContent> webList;
	private Map<TYPE_E, HttpUtils> reqList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.website_manager);
		findViews();
		initData();
	}

	private void initData() {
		webList = new ArrayList<WebContent>();
		reqList = new HashMap<TYPE_E, HttpUtils>();

		pop_dialog = new PopDialog(this, null);
		pop_dialog.showAnimation();

		if (!CoreHandler.isNetworkConnected(this)) {
			pop_dialog.showWarning(R.string.network_fail, exitListener);
			return;
		}

		doDeviceLogin();
	}

	private void findViews() {
		mGirdView = (WebGridView) findViewById(R.id.IDC_GridView_OTT_mainApp_Grid);
		mGirdView.setNumColumns(GRID_NUMCOL);
		mImagePageLeft = (ImageView) findViewById(R.id.IDC_GridView_OTT_pageback);
		mImagePageRight = (ImageView) findViewById(R.id.IDC_GridView_OTT_pagedown);
		mPageCount = (TextView) findViewById(R.id.IDC_GridView_OTT_PageNum);

		mGirdView.setOnItemClickListener(this);
		mGirdView.setOnHoverListener(this);
	}

	private void doDeviceLogin() {
		String device = null;
		try {
			byte[] buf = StoreManager.readFile(getAssets().open("config.json"));

			JSONObject jo = new JSONObject(new String(buf));
			serverAddr = jo.getString("server");
			device = jo.getJSONObject("device").toString();

			WebContent.init(serverAddr, getApplicationInfo().dataDir);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		new HttpUtils(serverAddr + sLoginUrl, new HttpUtils.CBKHandler() {

			@Override
			public void handle(byte[] src, Object... attr) {
				// TODO Auto-generated method stub
				if (src == null) {
					handler.sendEmptyMessage(DEVICE_LOGIN_FAIL);
				} else {
					handler.obtainMessage(DEVICE_LOGIN_DONE, new String(src))
							.sendToTarget();
				}
			}

		}).post(device).setParam(HttpUtils.sContentType, sContentTypeJson)
				.execute();

	}

	private void getCommonParser() {
		final File file = new File(getCacheDir() + File.separator
				+ URLParser.scCacheName);

		String date = StoreManager.getLastModified(file);

		new HttpUtils(serverAddr + sCommonParserUrl,
				new HttpUtils.CBKHandler() {

					@Override
					public void handle(byte[] src, Object... attr) {
						// TODO Auto-generated method stub
						if (src != null) {
							StoreManager.writeFile(file, src);
						}
					}

				}).setParam(HttpUtils.sModifySince, date).execute();
	}

	private void updatePageInfo(int pos) {
		if (mTotalPage <= 1) {
			mImagePageLeft.setVisibility(View.GONE);
			mImagePageRight.setVisibility(View.GONE);
		}

		if (mCurrentPage > 1) {
			mImagePageLeft.setVisibility(View.VISIBLE);
		}

		if (mCurrentPage < mTotalPage) {
			mImagePageRight.setVisibility(View.VISIBLE);
		}

		gdAdapter.setCurrentPage(mCurrentPage);
		mGirdView.setAdapter(gdAdapter);
		mGirdView.setSelection(pos);
		mPageCount.setText(mCurrentPage + "/" + mTotalPage);

	}

	private void parseWebList(String buf) {
		try {
			JSONArray ja = new JSONObject(buf).getJSONArray("value");

			int len = ja.length();
			mTotalPage = (len / GRID_PAGESIZE);
			if (len % GRID_PAGESIZE > 0) {
				mTotalPage += 1;
			}

			int i = 0;
			for (; i < len; i++) {
				JSONObject jo = ja.getJSONObject(i);

				WebContent content = new WebContent(jo.getString("Name"),
						jo.getString("Id"));

				webList.add(content);
			}
			gdAdapter = new WebGridAdapter(this, webList, GRID_PAGESIZE,
					mGirdView);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			pop_dialog.closeAnimation();

			switch (msg.what) {
			case DEVICE_LOGIN_DONE:
				parseWebList((String) msg.obj);
				updatePageInfo(0);
				mGirdView.setVisibility(View.VISIBLE);

				getCommonParser();
				break;

			case DEVICE_LOGIN_FAIL:
				pop_dialog.showWarning(R.string.login_fail, exitListener);
				break;

			case GET_SOURCE_DONE:
				WebContent web = (WebContent) msg.obj;

				String co = web.getConfig();
				String ca = web.getCategory();

				if (co == null || ca == null) {
					pop_dialog.showWarning(R.string.load_webs_fail, null);
					break;
				}
				CoreHandler.setUp(WebManager.this, VideoBrowser.class, co, ca,
						null, false);
				currentSiteId = web.getId();
				break;

			case ACCESS_SITE_DONE:
				if (msg.arg1 == HttpStatus.SC_OK) {
					loadWebSite((WebContent) msg.obj);

				} else if (msg.arg1 == HttpStatus.SC_NOT_ACCEPTABLE) {
					pop_dialog.showWarning(R.string.website_maintenance, null);

				} else if (msg.arg1 == HttpStatus.SC_MOVED_TEMPORARILY) {
					System.out.println("Login timeout");

				} else {
					pop_dialog.showWarning(R.string.load_webs_fail, null);
				}
				break;
			}

			super.handleMessage(msg);
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		HttpUtils.release();

		super.onDestroy();
	}

	private void makeCahceRequest(final WebContent content, final TYPE_E type) {
		String date = content.getTag(WebContent.sCacheDate, type);
		String etag = content.getTag(WebContent.sCacheEtag, type);

		reqList.put(type, new HttpUtils(content.getRequestUrl(type))
				.setCacheEnable().setParam(HttpUtils.sModifySince, date)
				.setParam(HttpUtils.sIfNoneMatch, etag).execute());
	}

	private void loadWebSite(final WebContent content) {
		pop_dialog.showAnimation();

		makeCahceRequest(content, TYPE_E.CONFIG);
		makeCahceRequest(content, TYPE_E.CATEGORY);
		makeCahceRequest(content, TYPE_E.PARSER);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (Entry<TYPE_E, HttpUtils> entry : reqList.entrySet()) {
					TYPE_E key = entry.getKey();
					HttpUtils req = entry.getValue();
					byte[] buf = req.getByteArray();

					if (req.isNotModified()) {
						content.readCache(key);

					} else if (buf != null) {
						content.writeCache(buf, req.getLastModified(),
								req.getEtag(), key);
					}
				}

				handler.obtainMessage(GET_SOURCE_DONE, content).sendToTarget();
			}
		}).start();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (!CoreHandler.isNetworkConnected(this)) {
			pop_dialog.showWarning(R.string.network_fail, exitListener);
			return;
		}

		final WebContent content = webList.get(gdAdapter.getPageStart() + arg2);

		new HttpUtils(content.getAccessUrl(), new HttpUtils.CBKHandler() {

			@Override
			public void handle(byte[] src, Object... attr) {
				// TODO Auto-generated method stub
				Message msg = handler.obtainMessage(ACCESS_SITE_DONE, content);
				msg.arg1 = (Integer) attr[0];
				msg.sendToTarget();
			}
		}).post(null).execute();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			if (mGirdView.hasFocus()) {
				if ((mGirdView.getSelectedItemPosition() % GRID_NUMCOL == GRID_NUMCOL - 1)
						&& (mCurrentPage < mTotalPage)) {
					mCurrentPage++;
					updatePageInfo(0);
					return true;
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			if (mGirdView.hasFocus()) {
				if ((mGirdView.getSelectedItemPosition() % GRID_NUMCOL == 0)
						&& (mCurrentPage > 1)) {
					mCurrentPage--;
					updatePageInfo(GRID_NUMCOL - 1);
					return true;
				}
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	public static Boolean reportBug(int ret, final int what,
			final BaseNode node, final DialogInterface.OnClickListener cbk) {
		if (ret != ALParser.PARSE_ERROR || CoreHandler.isLocalEdition()) {
			return false;
		}

		pop_dialog.showConfirm(R.string.report_bug,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						cbk.onClick(arg0, arg1);
						JSONObject jo = new JSONObject();
						try {
							jo.put("Title", CoreHandler.Callback.values()[what]);

							String desp = "";
							if (node != null) {
								desp = node + ", " + node.getURL();
							}
							jo.put("Description", desp);

							jo.put("SiteId", currentSiteId);

							new HttpUtils(serverAddr + sBugReportUrl,
									new HttpUtils.CBKHandler() {

										@Override
										public void handle(byte[] src,
												Object... attr) {
											// TODO Auto-generated method stub
											int code = (Integer) attr[0];
											if (code == HttpStatus.SC_OK) {
												CoreHandler
														.getInstace()
														.getCbkHandler()
														.sendEmptyMessage(
																CoreHandler.Callback.CBK_REPORT_BUG_DONE
																		.ordinal());
											}
										}

									})
									.post(jo.toString())
									.setParam(HttpUtils.sContentType,
											sContentTypeJson).execute();
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, cbk);

		return true;
	}

	DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			finish();
		}

	};

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

class WebContent {
	private static final String sAccessUrl = "/OTTS/odata/Site('%s')/OTTS.Models.Access";
	private static final String sContentUrl = "/OTTS/Content/Sites/";
	public static final String sCacheDate = "Date";
	public static final String sCacheEtag = "Etag";
	private static String sHost;
	private static String sRootDir;
	private String key;
	private String name;
	private String description;
	private String cachePath;
	private Drawable logo;
	private String config;
	private String category;
	private Map<String, Map<TYPE_E, String>> tag;

	private static final String[] sDataName = { "Cover.png", "Config.json",
			"Category.json", "Parser.jar", "tag.obj" };

	public static enum TYPE_E {
		LOGO, CONFIG, CATEGORY, PARSER, CACHE_TAG
	}

	public WebContent(String name, String key) {
		this.name = name;
		this.key = key;

		cachePath = sRootDir + File.separator + name;
		File file = new File(cachePath);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	protected static void init(String host, String path) {
		sHost = host;
		sRootDir = path;
	}

	private File getCacheFile(TYPE_E type) {
		return new File(cachePath + File.separator + sDataName[type.ordinal()]);
	}

	public String getDescription() {
		return description;
	}

	@SuppressWarnings("unchecked")
	public String getTag(String name, TYPE_E type) {
		if (tag == null
				&& (tag = (Map<String, Map<TYPE_E, String>>) StoreManager
						.readObject(getCacheFile(TYPE_E.CACHE_TAG))) == null) {
			tag = new HashMap<String, Map<TYPE_E, String>>();
			tag.put(sCacheDate, new HashMap<TYPE_E, String>());
			tag.put(sCacheEtag, new HashMap<TYPE_E, String>());
			return null;
		}
		return tag.get(name).get(type);
	}

	protected void setLogo(Drawable dw) {
		logo = dw;
	}

	public Drawable getLogo() {
		return logo;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return key;
	}

	public String getAccessUrl() {
		return String.format(sHost + sAccessUrl, key);
	}

	public String getRequestUrl(TYPE_E type) {
		return sHost + sContentUrl + key + "/" + sDataName[type.ordinal()];
	}

	public String getConfig() {
		return config;
	}

	public String getCategory() {
		return category;
	}

	public Boolean isCacheExit(TYPE_E type) {
		return getCacheFile(type).exists();
	}

	public Object readCache(TYPE_E type) {
		if (type == TYPE_E.PARSER || (type == TYPE_E.CONFIG && config != null)
				|| (type == TYPE_E.CATEGORY && category != null)) {
			return null;
		}

		File file = getCacheFile(type);
		byte[] buf = StoreManager.readFile(file);

		if (buf == null) {
			return null;
		}

		if (type == TYPE_E.LOGO) {
			return buf;
		}

		String str = new String(buf);

		if (type == TYPE_E.CONFIG) {
			config = str;

		} else if (type == TYPE_E.CATEGORY) {
			category = str;
		}

		return str;
	}

	public void writeCache(byte[] buf, String date, String etag, TYPE_E type) {
		if (type == TYPE_E.CONFIG) {
			config = new String(buf);

		} else if (type == TYPE_E.CATEGORY) {
			category = new String(buf);
		}

		tag.get(sCacheDate).put(type, date);
		tag.get(sCacheEtag).put(type, etag);

		StoreManager.writeObject(getCacheFile(TYPE_E.CACHE_TAG), tag);
		StoreManager.writeFile(getCacheFile(type), buf);
	}

}
