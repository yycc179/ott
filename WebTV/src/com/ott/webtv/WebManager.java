package com.ott.webtv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ott.webtv.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class WebManager extends Activity implements OnItemClickListener {
	private GridView gd = null;
	private final int GRID_NUMCOL = 4;
	private final int GRID_PAGESIZE = 12;
	private WebGridAdapter gdAdapter;
	private ImageView mImagePageLeft,mImagePageRight;
	private int mTotalPage,mCurrentPage = 1;
	private TextView mPageCount;
	private List<Map<String, Object>> Glist = new ArrayList<Map<String, Object>>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ott_apk_manager);
		initData();
		findViews();
		updatePageInfo();
	} 

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	private void findViews(){
		gd = (GridView)findViewById(R.id.IDC_GridView_OTT_mainApp_Grid);
		gd.setNumColumns(GRID_NUMCOL);
		mImagePageLeft = (ImageView)findViewById(R.id.IDC_GridView_OTT_pageback);
		mImagePageRight = (ImageView)findViewById(R.id.IDC_GridView_OTT_pagedown);
		mPageCount = (TextView) findViewById(R.id.IDC_GridView_OTT_PageNum);
		
		gd.setOnItemClickListener(this);
		GuestSupport gs = new GuestSupport();
		gd.setOnTouchListener(gs.onTouchListener);
	}
	
	private List<Map<String, Object>> getInstalledApps() {
		List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
		int packageSize = packages.size();
		List<Map<String, Object>> listMap = new ArrayList<Map<String,Object>>(packageSize);
		for (int j = 0; j < packageSize; j++) {
			Map<String, Object> map = new HashMap<String, Object>();
			PackageInfo packageInfo = packages.get(j);
			//load the installed application
			if((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)==0){
				map.put("desc", packageInfo.packageName);
				System.out.println("----------- the packageName = "+packageInfo.packageName);
				if(packageInfo.packageName.startsWith("com.webtv.")){
					map.put("img", packageInfo.applicationInfo.loadIcon(getPackageManager()).getCurrent());
					map.put("name", packageInfo.applicationInfo.loadLabel(getPackageManager()).toString());
					listMap.add(map);
				}
			}
		}
		return listMap;
	}
	
	private void updatePageInfo(){
		
		mImagePageLeft.setVisibility(View.GONE);
		mImagePageRight.setVisibility(View.GONE);
		
		if(mCurrentPage > 1){
			mImagePageLeft.setVisibility(View.VISIBLE);
		}
		
		if(mCurrentPage < mTotalPage){
			mImagePageRight.setVisibility(View.VISIBLE);
		}
		gdAdapter.setGridParam(this, Glist, GRID_PAGESIZE, mCurrentPage);
		mPageCount.setText(mCurrentPage + "/" + mTotalPage);
		gd.setAdapter(gdAdapter);
		gd.requestFocus();
	}
	
	private void initData() {
		// TODO Auto-generated method stub
		Glist = getInstalledApps();
		gdAdapter = new WebGridAdapter();
		int a = Glist.size() % GRID_PAGESIZE;
		if (a > 0)
			mTotalPage = Glist.size() / GRID_PAGESIZE + 1;
		else
			mTotalPage = Glist.size() / GRID_PAGESIZE;

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			if(gd.hasFocus()){
				System.out.println("--------- mCurrentPage111111 = "+mCurrentPage);
				if((gd.getSelectedItemPosition()%GRID_NUMCOL == GRID_NUMCOL-1)&&(mCurrentPage < mTotalPage)){
					mCurrentPage++;
					updatePageInfo();
					gd.setSelection(0);
					return true;
				}
			}
		}else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
			if(gd.hasFocus()){
				if((gd.getSelectedItemPosition()%GRID_NUMCOL == 0)&&(mCurrentPage > 1)){
					mCurrentPage--;
					updatePageInfo();
					gd.setSelection(GRID_NUMCOL-1);
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
	    PackageManager mPackageManager = getPackageManager();
		Intent intent = mPackageManager.getLaunchIntentForPackage((String)Glist.get(arg2).get("desc"));
		
		intent.putExtra("needLogin", true);
		startActivity(intent);
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
					System.out.println("-----  page change ----????---");
					dealGesture(endX - startX, endY - startY);
					return true;
				} else {
					System.out.println("-----  do for move -------");
					return false;
				}
			}
		};

		private void dealGesture(int offsetX, int offsetY) {
			System.out.println("--------- the offsetX = " + offsetX);
			System.out.println("--------- the offsetY = " + offsetY);
			if (Math.abs(offsetX) > 200 && Math.abs(offsetY) < 50) {
				if (offsetX < 0) {
					if(mCurrentPage < mTotalPage){
						mCurrentPage++;
						updatePageInfo();
						return;
					}
				} else if(mCurrentPage > 1){
						mCurrentPage--;
						updatePageInfo();
						return;
					}
				}
			}
		}

}
