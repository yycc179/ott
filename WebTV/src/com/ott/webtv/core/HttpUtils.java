package com.ott.webtv.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HttpUtils implements Callable<String> {
	private static final int MAX_POOL_SIZE = 3;
	private static final int MAX_TIMEOUT = 10000;
	
	private static ExecutorService pool = null;

	private HttpURLConnection con;
	private String postData;
	private Future<String> future;
	private IParser callBack;

	private static Boolean bCancel = false;

	public HttpUtils(String url) {
		URL ul = null;
		
		if(pool == null){
			pool = Executors.newFixedThreadPool(MAX_POOL_SIZE);
		}

		try {
			ul = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			con = (HttpURLConnection) ul.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.setConnectTimeout(MAX_TIMEOUT);
		con.setReadTimeout(MAX_TIMEOUT);
		con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");
	}

	static void initialize() {
		pool = Executors.newFixedThreadPool(MAX_POOL_SIZE);
	}
	
	public HttpUtils(String url, IParser cbk) {
		this(url);
		this.callBack = cbk;
	}

	public HttpUtils post(String data) {
		try {
			con.setRequestMethod("POST");
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		con.setDoOutput(true);
		postData = data;
		return this;
	}

	public HttpUtils setParam(String key, String val) {
		con.setRequestProperty(key, val);
		return this;
	}

	public HttpUtils execute() {
		bCancel = false;
		future = pool.submit(this);
		return this;
	}

	@Override
	public String call() {
		// TODO Auto-generated method stub
		String ret = null;
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = null;
		do {
			try {
				
			
				if (postData != null) {
					con.getOutputStream().write(postData.getBytes());
				}

				int code = con.getResponseCode();
				
				if (code != HttpURLConnection.HTTP_OK) {
					System.out.println(code + " " + con.getResponseMessage());
					break;
				}

				bis = new BufferedInputStream(con.getInputStream());
				baos = new ByteArrayOutputStream();
				int b = -1;
				while ((b = bis.read()) != -1 && !bCancel) {
					baos.write(b);
				}

				if (bCancel) {
					break;
				}

				ret = baos.toString();

				if (callBack != null) {
					callBack.parse(ret);
				}

			} catch (IOException e) {
				System.err.println(e);
			}
			
		} while (false);

		try {
			if (bis != null) {
				bis.close();
			}
			if (baos != null) {
				baos.close();
			}
		} catch (IOException e) {
			System.err.println(e);
		}

		con.disconnect();
		return ret;
	}

	public String getResult() {
		try {
			return future.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	static void setCancel(Boolean flag) {
		bCancel = flag;
	}

	static Boolean getCanceled() {
		return bCancel;
	}

	static void release() {
		if(pool != null){
			pool.shutdownNow();
			pool = null;
		}
	}
}
