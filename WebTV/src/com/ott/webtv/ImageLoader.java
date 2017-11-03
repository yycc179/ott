package com.ott.webtv;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

public class ImageLoader {

	public static HashMap<String, Bitmap> mImageCache = new HashMap<String, Bitmap>();
	private static ArrayList<String> picMapKey = new ArrayList<String>();
	private final int MAX_FAIL_TIME = 6;
	private ImageView view;
	private String mUrl;
	private int mRetryTimes = 0;
	int reqWidth = 0;
	int reqHeight = 0;
	private boolean isUrlError = false;
	private boolean isCancelNetRequest;
	
	public void setImageSize(int width, int height) {
		// TODO Auto-generated method stub
		this.reqWidth = width;
		this.reqHeight = height;
	}
	
	public void setDownLoaderImageView(View view) {
		this.view = (ImageView) view;
	}

	public void setImageFromUrl(String Url) {
		if(Url == null){
			return;
		}
		isCancelNetRequest = false;
		if (mImageCache.get(Url) != null && !mImageCache.get(Url).isRecycled()) {
			Bitmap bmInCache = mImageCache.get(Url);
			view.setImageBitmap(bmInCache);
		} else {
			StartDownLoaderImage(Url);
		}
	}

	private void StartDownLoaderImage(String url) {
		// TODO Auto-generated method stub
			loadImage3(url);
	}

	private void reDownLoaderImage(String imageUrl) {
		// TODO Auto-generated method stub
		if(isUrlError){
			return;
		}
		if ((imageUrl != null) && (imageUrl.equals(mUrl))) {
			mRetryTimes++;
			mUrl = imageUrl;
		} else {
			mRetryTimes = 0;
		}

		if (mRetryTimes == MAX_FAIL_TIME) {
			return;
		}
		mUrl = imageUrl;
		
		Bitmap bitmap = getBitMapFromCache(imageUrl);
		
		if (bitmap != null && !bitmap.isRecycled()) {
			view.setImageBitmap(bitmap);
			return;
		}
		StartDownLoaderImage(imageUrl);
	}
		
	
	private Bitmap getBitMapFromCache(String url){

		return mImageCache.get(url);
	}

	/********************** start *********************************/
	 private Handler handler = new Handler(){
		 public void handleMessage(Message msg) {
			 Bitmap bitmap = (Bitmap) msg.obj;
			 if(bitmap != null){
				 view.setImageBitmap(bitmap);
			 }
		 }
	 };
	 
	 public static int calculateInSampleSize(BitmapFactory.Options options,
				int reqWidth, int reqHeight) {
		    final int height = options.outHeight;
			final int width = options.outWidth;
			
			int inSampleSize = 1;
			if (height > reqHeight && width > reqWidth) {
				
				final int heightRatio = Math.round((float) height / (float) reqHeight);
				final int widthRatio = Math.round((float) width / (float) reqWidth);
				inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
				final float totalPixels = width * height;

	            final float totalReqPixelsCap = reqWidth * reqHeight /** 2*/;

	            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
	                inSampleSize++;
	            }
			}
			System.out.println("---------ImageSample size = "+inSampleSize);
			if(inSampleSize == 0){
				inSampleSize = 1;
			}
			return inSampleSize;
		}
	 
	 public Bitmap decodeBitmapFromResource(InputStream inputStream, 
			        int reqWidth, int reqHeight) {
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeStream(inputStream, null, options);
		    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		    options.inJustDecodeBounds = false;
		    try {
		    	inputStream.reset();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		    return BitmapFactory.decodeStream(inputStream, null, options);
	}
	 
	 
	 private static ExecutorService executorService = Executors.newFixedThreadPool(10);
     public void loadImage3(final String url) {
		if(executorService == null){
			executorService = Executors.newFixedThreadPool(10);
		}
		executorService.submit(new Runnable() { 
			public void run() {
					try {
//						 Drawable drawable = zoomDrawable(Drawable.createFromStream(new URL(url1).openStream(), "image.png"));
						Bitmap bitmap = downloadBitmap(url);
						if (bitmap != null) {
							if(mImageCache.size() > 50){
								Bitmap bm = mImageCache.remove(picMapKey.get(0));
								if(bm != null && !bm.isRecycled()){
									bm = null;
								}
								picMapKey.remove(0);
							}
							picMapKey.add(url);
							mImageCache.put(url, bitmap);
						} else {
							if(isCancelNetRequest){
								System.out.println("-----cancel requset again-----");
								return;
							}
							reDownLoaderImage(url);
						}
						Message msg = new Message();
						msg.obj = bitmap;
						handler.sendMessage(msg);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
			}
		});
	}
	/*********************  end   **********************************/
	 
     private Bitmap downloadBitmap(String imageUrl) {  
         Bitmap bitmap = null;  
         HttpURLConnection con = null;  
         try {  
             HttpGet httpRequest = new HttpGet(imageUrl);
             HttpClient httpclient = new DefaultHttpClient();
             HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
             HttpEntity entity = response.getEntity();
             BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
             if (response.getStatusLine().getStatusCode() == 200) {
	             InputStream is = bufferedHttpEntity.getContent();
	             if(!isCancelNetRequest){ 
		             bitmap = decodeBitmapFromResource(is,reqWidth,reqHeight);
		             is.close();
	             }else{
	            	 System.out.println("-----cancel  net request-----");
	             }
	          
             }else{
             		System.out.println("-------- http request error --------");
             }
         } catch (Exception e) {  
        	 e.printStackTrace();
             if(e.getMessage().indexOf("No address associated with hostname") != -1){
            	 isUrlError = true;
             }
             
         } finally {  
             if (con != null) {  
                 con.disconnect();  
             }  
         }  
         return bitmap;  
     }
     
	public void clearBmpCache() {

		if (executorService != null){
			executorService.shutdown(); // Disable new tasks from being submitted
//		try { // Wait a while for existing tasks to terminate
//			if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
//				executorService.shutdownNow(); // Cancel currently executing
//												// tasks
//				// Wait a while for tasks to respond to being cancelled
//				if (!executorService.awaitTermination(2, TimeUnit.SECONDS))
//					System.err.println("Pool did not terminate");
//			}
//		} catch (InterruptedException ie) { // (Re-)Cancel if current thread
//											// also interrupted
//			executorService.shutdownNow(); // Preserve interrupt status
//			Thread.currentThread().interrupt();
//		}
			executorService = null;
			}
		
		picMapKey.clear();

		if (mImageCache.isEmpty()) {
			return;
		}
		for (Bitmap bitmap : mImageCache.values()) {
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}
		mImageCache.clear();
	}
	
	public void CancleTask(){
		isCancelNetRequest = true;
		if (executorService != null){
			executorService.shutdownNow(); // Disable new tasks from being submitted
			executorService = null;
		}
	}

}