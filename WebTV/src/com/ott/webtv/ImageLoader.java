package com.ott.webtv;


import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImageLoader {

	public static HashMap<String, Drawable> mImageCache = new HashMap<String, Drawable>();
	private static ArrayList<String> picMapKey = new ArrayList<String>();
	private final int MAX_FAIL_TIME = 6;
	private ImageView view;
	private String mUrl;
	private int mFail = 0;
	
	public void setDownLoaderImageView(View view) {
		this.view = (ImageView) view;
	}

	public void setImageFromUrl(String Url) {
		if(Url == null){
			return;
		}
		if (mImageCache.containsKey(Url)) {
			Drawable bmInCache = mImageCache.get(Url);
			view.setImageDrawable(bmInCache);
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
		if ((imageUrl != null) && (imageUrl.equals(mUrl))) {
			mFail++;
			mUrl = imageUrl;
		} else {
			mFail = 0;
		}

		if (mFail == MAX_FAIL_TIME) {
			return;
		}
		mUrl = imageUrl;
		if (mImageCache.containsKey(imageUrl)) {
			view.setImageDrawable(mImageCache.get(imageUrl));
			return;
		}
		StartDownLoaderImage(imageUrl);
	}
		
	private Drawable zoomDrawable(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable);
		Matrix matrix = new Matrix();
//		float scaleWidth = ((float) w / width);
//		float scaleHeight = ((float) h / height);
//		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true);
		return new BitmapDrawable(null, newbmp);
	}

	private Bitmap drawableToBitmap(Drawable drawable) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(width, height, config);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}

	/********************** start *********************************/
	 private Handler handler = new Handler(){
		 public void handleMessage(Message msg) {
			 Drawable drawable = (Drawable) msg.obj;
			 view.setImageDrawable(drawable);
		 }
	 };
	 private static ExecutorService executorService = Executors.newFixedThreadPool(10);
	
     public void loadImage3(final String url) {
		if(executorService == null){
			executorService = Executors.newFixedThreadPool(10);
		}
		executorService.submit(new Runnable() { 
			public void run() {
					try {
						Log.d("<<<<>>>>>>","---------currentThread id = "+Thread.currentThread().getId());
					
						 Drawable drawable = zoomDrawable(Drawable.createFromStream(new URL(url).openStream(), "image.png"));
						if (drawable != null) {
							System.out.println("------- the cache = "+mImageCache.size());
							if(mImageCache.size() > 50){
								mImageCache.remove(picMapKey.get(0));
								picMapKey.remove(0);
							}
							picMapKey.add(url);
							mImageCache.put(url, drawable);
						} else {
							reDownLoaderImage(url);
						}
						Message msg = new Message();
						msg.obj = drawable;
						handler.sendMessage(msg);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
			}
		});
	}
	/*********************  end   **********************************/
	 
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
		for (Drawable bitmap : mImageCache.values()) {
			if (bitmap != null) {
				bitmap.setCallback(null);
			}
		}
		mImageCache.clear();
	}
	
	public void CancleTask(){
		if (executorService != null){
			executorService.shutdownNow(); // Disable new tasks from being submitted
			executorService = null;
		}
	}
}