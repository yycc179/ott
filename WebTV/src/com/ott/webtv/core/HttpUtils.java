package com.ott.webtv.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

public class HttpUtils implements Callable<byte[]> {
	public static final int WAIT_TIMEOUT = 10000;
	public static final int CONNECT_TIMEOUT = 5000;
	public static final int READ_TIMEOUT = 5000;
	public static final int MAX_TOTAL_CONNECTIONS = 100;
	public static final int MAX_HOST_CONNECTIONS = 50;
	public static final int MAX_RETRY_TIMES = 3;

	public static final String sCookie = "Cookie";
	public static final String sReferer = "Referer";
	public static final String sUserAgent = "User-Agent";
	public static final String sContentType = "Content-Type";
	public static final String sModifySince = "If-Modified-Since";
	public static final String sIfNoneMatch = "If-None-Match";

	private static ExecutorService pool;
	private static DefaultHttpClient httpClient;
	private static Boolean bCancel = false;

	private URI uri;
	private HttpUriRequest method;

	private Future<byte[]> future;
	private CBKHandler callBack;

	private Boolean bNotModified = false;
	private String location;
	
	private String etag;
	private String lastModified;
	private Boolean bCacheEnable = false;
	
	public HttpUtils(String url) {
		try {
			URL u = new URL(url);
			uri = new URI(u.getProtocol(), u.getHost(), u.getPath(),
					u.getQuery(), null);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Request URI = " + uri);

		method = new HttpGet(uri);
	}

	public HttpUtils(String url, CBKHandler cbk) {
		this(url);
		this.callBack = cbk;
	}

	public HttpUtils post(String data) {
		HttpPost post = new HttpPost(uri);

		if (data != null) {
			try {
				HttpEntity entity = new StringEntity(data);
				post.setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		method = post;

		setParam(sContentType, "application/x-www-form-urlencoded");
		return this;
	}

	public HttpUtils setParam(String key, String val) {
		if (val != null) {
			method.setHeader(key, val);
		}
		return this;
	}
	
	public HttpUtils setCacheEnable(){
		bCacheEnable = true;
		return this;
	}
	
	public HttpUtils setRedirectDisable() {
		HttpParams params = new BasicHttpParams();
		params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		method.setParams(params);
		return this;
	}

	public HttpUtils execute() {
		bCancel = false;
		future = getThreadPool().submit(this);
		return this;
	}

	@Override
	public byte[] call() {
		// TODO Auto-generated method stub
		byte[] buffer = null;
		int statusCode = -1;
		try {
			HttpResponse response = getHttpClient().execute(method);

			StatusLine line = response.getStatusLine();
			statusCode = line.getStatusCode();
			
			HttpEntity entity = response.getEntity();

			switch (statusCode) {
			case HttpStatus.SC_OK:
				buffer = EntityUtils.toByteArray(entity);
				
				if(bCacheEnable){
					Header header = response.getFirstHeader("ETag");
					if (header != null) {
						etag = header.getValue();
					}
					
					if((header = response.getFirstHeader("Last-Modified")) != null){
						lastModified = header.getValue();
					}
				}

				break;

			case HttpStatus.SC_NOT_MODIFIED:
				bNotModified = true;
				break;

			case HttpStatus.SC_MOVED_PERMANENTLY:
			case HttpStatus.SC_MOVED_TEMPORARILY:
			case HttpStatus.SC_SEE_OTHER:
			case HttpStatus.SC_TEMPORARY_REDIRECT:
				location = response.getFirstHeader("Location").getValue();
				break;

			default:
				break;
			}

			if (bCancel) {
				return null;
			}

			System.out.println(line);

		} catch (IOException e) {
			method.abort();
			e.printStackTrace();
		}

		if (callBack != null) {
			if(bCacheEnable){
				callBack.handle(buffer, statusCode, lastModified, etag);
			}else{
				callBack.handle(buffer, statusCode);
			}
		}

		return buffer;
	}

	public byte[] getByteArray() {
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

	public String getResult() {
		byte[] b = getByteArray();
		return b != null ? new String(b) : null;
	}

	public String getResult(String charset) {
		String ret = null;
		byte[] b = getByteArray();

		if (charset != null && b != null) {
			try {
				ret = new String(b, charset);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return ret;
	}

	// call after setRedirectDisable
	public String getLocation() {
		getResult();
		return location;
	}

	public String getLastModified(){
		return lastModified;
	}
	
	public String getEtag() {
		return etag;
	}
	
	public Boolean isNotModified() {
		return bNotModified;
	}

	public static void setCancel(Boolean flag) {
		bCancel = flag;

		if (flag && pool != null) {
			pool.shutdownNow();
			pool = null;
		}
	}

	public static Boolean getCanceled() {
		return bCancel;
	}

	public static void release() {
		if (pool != null) {
			pool.shutdownNow();
			pool = null;
		}

		if (httpClient != null) {
			httpClient.getConnectionManager().shutdown();
			httpClient = null;
		}
	}

	private synchronized static ExecutorService getThreadPool() {
		if (pool == null) {
			pool = Executors.newCachedThreadPool();
		}

		return pool;
	}

	private synchronized static HttpClient getHttpClient() {
		if (null == httpClient) {
			try {
				KeyStore trustStore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				trustStore.load(null, null);

				SSLSocketFactory ssFac = new MySSLSocketFactory(trustStore);
				ssFac.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

				HttpParams httpParams = new BasicHttpParams();

				ConnManagerParams.setMaxTotalConnections(httpParams,
						MAX_TOTAL_CONNECTIONS);
				ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
						new ConnPerRouteBean(MAX_HOST_CONNECTIONS));
				ConnManagerParams.setTimeout(httpParams, WAIT_TIMEOUT);

				HttpConnectionParams.setConnectionTimeout(httpParams,
						CONNECT_TIMEOUT);
				HttpConnectionParams.setSoTimeout(httpParams, READ_TIMEOUT);
				HttpConnectionParams.setTcpNoDelay(httpParams, true);
				HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);

				HttpProtocolParams.setUseExpectContinue(httpParams, false);

				HttpProtocolParams
						.setUserAgent(
								httpParams,
								"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22");

				SchemeRegistry schReg = new SchemeRegistry();
				schReg.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				schReg.register(new Scheme("https", ssFac, 443));

				ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
						httpParams, schReg);

				httpClient = new DefaultHttpClient(conMgr, httpParams);

				DefaultHttpRequestRetryHandler dhr = new DefaultHttpRequestRetryHandler(
						MAX_RETRY_TIMES, true);
				httpClient.setHttpRequestRetryHandler(dhr);

			} catch (Exception e) {
				e.printStackTrace();
				return new DefaultHttpClient();
			}
		}
		return httpClient;
	}

	public static interface CBKHandler {
		public void handle(byte[] src, Object...attr);
	}
}

class MySSLSocketFactory extends SSLSocketFactory {
	SSLContext sslContext = SSLContext.getInstance("TLS");

	public MySSLSocketFactory(KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super(truststore);

		TrustManager tm = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sslContext.init(null, new TrustManager[] { tm }, null);
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(socket, host, port,
				autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}
}