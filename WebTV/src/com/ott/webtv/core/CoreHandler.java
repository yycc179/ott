package com.ott.webtv.core;

import java.io.File;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ott.webtv.core.CategoryManager.SOURCE_TYPE;
import com.ott.webtv.core.DataNode.BaseNode;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.DATA_TYPE;

import dalvik.system.DexClassLoader;

public class CoreHandler extends Thread implements ILoader {
	private enum Command {
		CMD_GET_CATEGORY, CMD_GET_CONTENT, CMD_GET_SEARCH, CMD_GET_EXTRA, CMD_PAGE_UPDOWN, CMD_GET_PLAYURL, CMD_GET_CUSTOMER
	}

	public static enum Customer_CMD {
		CUSTOMER_LOGIN_SET, LIVE_PLAYER_GET_CHAN_LIST, LIVE_PLAYER_GET_REAL_URL, LIVE_PLAYER_GET_CHAN_INFO, LIVE_CHNFAV_GET_FAV_LIST, LIVE_CHNFAV_ADD_FAV, LIVE_CHNFAV_DEL_FAV, LIVE_POP_CHANGE_PIN_CODE, LIVE_EPG_GET_DAILY_EPG
	}

	public static enum Callback {
		CBK_GET_CATEGORY_DONE, CBK_GET_CONTENT_DONE, CBK_GET_EXTRA_DONE, CBK_GET_SEARCH_DONE, CBK_GET_URL_DONE, CBK_GET_CUSTOMER_DONE, CBK_GET_CATEGORY_FAIL, CBK_GET_CONTENT_FAIL, CBK_GET_EXTRA_FAIL, CBK_GET_URL_FAIL, CBK_GET_CUSTOMER_FAIL, CBK_CANCEL_CMOMAND, CBK_NETWORK_CONNECT_FAIL, CBK_REPORT_BUG_DONE
	}

	private static CoreHandler cmdHandler = null;
	private static Boolean bLocalEdition = true;

	private ILoader parser;
	private Handler handler;
	private Handler cbkHandler;
	private Context context;

	private CoreHandler() {

	}

	private CoreHandler(ILoader parser) {
		this.parser = parser;
	}

	public static CoreHandler getInstace() {
		if (cmdHandler == null) {
			cmdHandler = new CoreHandler();
		}
		return cmdHandler;
	}

	public void setHandler(Handler cbk) {
		this.cbkHandler = cbk;
	}

	public Handler getCbkHandler() {
		return cbkHandler;
	}

	public int initializeAll(byte[] parser, String parserName, Handler cbk,
			int uiPageSize, Context context, String name) {
		// TODO Auto-generated method stub

		ContentManager.initialize(uiPageSize);
		StoreManager.initialize(context, name, uiPageSize);
		this.cbkHandler = cbk;
		this.context = context;

		if (this.parser == null
				&& (this.parser = loadParser(parser, parserName, name)) == null) {
			return -1;
		}

		this.start();

		while (handler == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return 0;
	}

	public Context getContext() {
		return context;
	}

	public static void setUp(Context context, Class<?> target, ILoader parser) {
		String config = null;
		String category = null;
		byte[] p = null;

		AssetManager am = context.getAssets();
		try {

			config = new String(StoreManager.readFile(am.open("config.json")));
			category = new String(StoreManager.readFile(am
					.open("category.json")));

			if (parser != null) {
				cmdHandler = new CoreHandler(parser);
			} else {
				p = StoreManager.readFile(am.open("parser.jar"));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setUp(context, target, config, category, p, true);
	}

	public static void setUp(Context context, Class<?> target, String config,
			String category, byte[] parser, Boolean bLocal) {

		Intent intent = new Intent();

		if (target != null) {
			intent.setClass(context, target);
		} else {
			intent.setClassName("com.ott.webtv", "com.ott.webtv.VideoBrowser");
		}

		intent.putExtra("category", category);
		intent.putExtra("config", config);
		intent.putExtra("parser", parser);

		bLocalEdition = bLocal;

		context.startActivity(intent);
	}

	@SuppressLint("HandlerLeak")
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out
				.println("------------------------core start!----------------------");
		Looper.prepare();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (Command.values()[msg.what]) {
				case CMD_GET_CATEGORY:
					parser.loadCategory((String) msg.obj);
					break;

				case CMD_GET_CONTENT:
					parser.loadContentPage(null, (BaseNode) msg.obj);
					break;

				case CMD_GET_SEARCH:
					parser.loadSearchPage(DATA_TYPE.values()[msg.arg1],
							(String) msg.obj);
					break;

				case CMD_PAGE_UPDOWN:
					parser.loadPageList(msg.arg1);
					break;

				case CMD_GET_EXTRA:
					parser.loadExtraInfo((Content) msg.obj);
					break;

				case CMD_GET_PLAYURL:
					parser.loadPlayUrl((Content) msg.obj, msg.arg1);
					break;

				case CMD_GET_CUSTOMER:
					parser.loadCustomerData(msg.arg1, msg.arg1, msg.obj);
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}
		};
		Looper.loop();
	}

	@Override
	public void loadCategory(String src) {
		// TODO Auto-generated method stub
		sendMessage(Command.CMD_GET_CATEGORY, 0, 0, src);
	}

	@Override
	public void loadContentPage(SOURCE_TYPE type, BaseNode node) {
		// TODO Auto-generated method stub
		BaseNode cate = node;

		if (cate == null) {
			CategoryManager mgr = CategoryManager.getCurrent(type);
			
			if (ContentManager.isDataReady()) {
				sendCallback(Callback.CBK_GET_CONTENT_DONE, 0,
						ALParser.PARSE_SUCCESS, null);
				return;
			}
			
			cate = mgr.getFirstNode();
			
		} else if (type != null) {
			cancleTask();
			ContentManager.clear();
		}
		sendMessage(Command.CMD_GET_CONTENT, 0, 0, cate);
	}

	@Override
	public void loadSearchPage(DATA_TYPE searchType, String value) {
		// TODO Auto-generated method stub
		ContentManager.setSearchMode();
		sendMessage(Command.CMD_GET_SEARCH, searchType.ordinal(), 1, value);
	}

	@Override
	public void loadPageList(int page) {
		// TODO Auto-generated method stub
		sendMessage(Command.CMD_PAGE_UPDOWN, page, 0, null);
	}

	@Override
	public void loadExtraInfo(Content node) {
		// TODO Auto-generated method stub
		sendMessage(Command.CMD_GET_EXTRA, 0, 0, node);
	}

	@Override
	public void loadPlayUrl(Content node, int index) {
		// TODO Auto-generated method stub
		sendMessage(Command.CMD_GET_PLAYURL, index, 0, node);
	}

	@Override
	public void loadCustomerData(int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		sendMessage(Command.CMD_GET_CUSTOMER, arg1, arg2, obj);
	}

	public static boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	public void finalizeAll() {
		// TODO Auto-generated method stub

		if (bLocalEdition) {
			HttpUtils.release();
		}

		ContentManager.clear();
		CategoryManager.release();
		StoreManager.release();

		if (handler != null) {
			handler.getLooper().quit();
			handler = null;
		}
		cmdHandler = null;
	}

	private void sendMessage(Command type, int arg1, int arg2, Object obj) {
		if (!isNetworkConnected(context)) {
			cbkHandler.sendEmptyMessage(Callback.CBK_NETWORK_CONNECT_FAIL
					.ordinal());
			return;
		}

		Message msg = handler.obtainMessage(type.ordinal(), arg1, arg2, obj);
		msg.sendToTarget();
		System.out.println("__commond Message: " + type.name());
	}

	void sendCallback(Callback type, int arg1, int arg2, Object obj) {
		Message msg = cbkHandler.obtainMessage(type.ordinal(), arg1, arg2, obj);
		msg.sendToTarget();
		System.out.println("__callback Message: " + type.name() + " arg1: "
				+ arg1 + " obj: " + obj);
	}

	@SuppressLint("NewApi")
	private ILoader loadParser(byte[] dex, String p_name, String webname) {
		ILoader p = null;
		File file = null;

		if (bLocalEdition) {
			file = new File(context.getCacheDir() + File.separator + "p.jar");
			StoreManager.writeFile(file, dex);

		} else {
			file = new File(context.getApplicationInfo().dataDir
					+ File.separator + webname + File.separator + "Parser.jar");
		}

		try {
			p = (ILoader) loadClass(file, p_name).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return p;
	}

	public static Class<?> loadClass(File file, String name)
			throws ClassNotFoundException {
		Class<?> cls = null;
		DexClassLoader cl = new DexClassLoader(file.getAbsolutePath(),
				file.getParent(), null, cmdHandler.getClass().getClassLoader());

		cls = cl.loadClass(name);

		return cls;
	}

	public static Boolean isLocalEdition() {
		return bLocalEdition;
	}

	public void cancleTask() {
		HttpUtils.setCancel(true);
	}

}