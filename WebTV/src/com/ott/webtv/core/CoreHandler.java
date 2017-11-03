package com.ott.webtv.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
		CMD_GET_CATEGORY, CMD_GET_CONTENT, CMD_GET_SEARCH, CMD_GET_EXTRA, CMD_PAGE_UPDOWN, CMD_GET_LOGINSTATUS
	}

	public static enum Callback {
		CBK_GET_CATEGORY_DONE, CBK_GET_CONTENT_DONE, CBK_GET_EXTRA_DONE, CBK_GET_SEARCH_DONE, CBK_GET_CATEGORY_FAIL, CBK_GET_CONTENT_FAIL, CBK_GET_EXTRA_FAIL, CBK_CANCEL_CMOMAND,
	}

	private static CoreHandler cmdHandler = null;

	private ILoader parser;
	private Handler handler;
	private Handler cbkHandler;

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

	public void initializeAll(byte[] parser, String parserName, Handler cbk,
			String path, int uiPageSize) {
		// TODO Auto-generated method stub

		ContentManager.initialize(uiPageSize);
		StoreManager.initialize(path, uiPageSize);
		this.cbkHandler = cbk;
		
		if(this.parser == null){
			this.parser = loadClass(parser, parserName, path);	
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
	}

	public static Intent setUp(Context context, Class<?> target, ILoader parser) {
		String config = null;
		String category = null;
		byte[] p = null;
		AssetManager am = context.getAssets();
		try {
			
			config = new String(convert(am.open("config.json")));
			category = new String(convert(am.open("category.json")));
			
			if (parser != null) {
				cmdHandler = new CoreHandler(parser);
			}else{
				p = convert(am.open("parser.jar"));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return setUp(context, target, config, category, p);

	}

	public static Intent setUp(Context context, Class<?> target, String config, String category,
			byte[] parser) {

		Intent intent = new Intent();
		
		if(target != null) {
			intent.setClass(context, target);
		}else{
			intent.setClassName("com.ott.webtv", "com.ott.webtv.VideoBrowser");			
		}

		intent.putExtra("category", category);
		intent.putExtra("config", config);
		intent.putExtra("parser", parser);

		return intent;
	}

	private static byte[] convert(InputStream is){
		byte[] b = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			BufferedInputStream bis = new BufferedInputStream(is);

			int i = -1;
			while ((i = bis.read()) != -1) {
				baos.write(i);
			}
			b = baos.toByteArray();
			bis.close();
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return b;		
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
				case CMD_GET_SEARCH:
					if (msg.arg2 == 1) {
						parser.loadSearchPage(DATA_TYPE.values()[msg.arg1],
								(String) msg.obj);
					} else {
						parser.loadContentPage(SOURCE_TYPE.values()[msg.arg1],
								(BaseNode) msg.obj);
					}

					break;

				case CMD_PAGE_UPDOWN:
					parser.loadPageList(msg.arg1);
					break;

				case CMD_GET_EXTRA:
					parser.loadExtraInfo((Content) msg.obj);
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
			CategoryManager mgr = CategoryManager.getInstace();
			cate = mgr.getNode(type, 0);
			if (cate.getChildType() == DATA_TYPE.CATEGORY) {
				cate = mgr.getNode(type, 0).getSubNode(0);
			}
		}
		sendMessage(Command.CMD_GET_CONTENT, type.ordinal(), 0, cate);
	}

	@Override
	public void loadSearchPage(DATA_TYPE searchType, String value) {
		// TODO Auto-generated method stub
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

	public boolean isNetworkConnected(Context context) {
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
		HttpUtils.release();
		ContentManager.release();
		CategoryManager.release();
		StoreManager.release();
		
		if(handler != null){
			handler.getLooper().quit();
			handler = null;
		}		
		cmdHandler = null;		
	}

	private void sendMessage(Command type, int arg1, int arg2, Object obj) {
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
	private ILoader loadClass(byte[] dex, String name, String path) {
		ILoader p = null;

		File file = new File(path + "/p.jar");

		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(dex);
			fos.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		DexClassLoader cl = new DexClassLoader(path + "/p.jar", path, null,
				getClass().getClassLoader());
		Class<?> clazz = null;
		try {
			clazz = cl.loadClass(name);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			p = (ILoader) clazz.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return p;
	}

	public void cancleTask() {
		HttpUtils.setCancel(true);
	}

}