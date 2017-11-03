package com.ott.webtv;

import android.app.Activity;
import android.content.Context;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class HoverEventManager{
	
	private Activity mActivity = null;
	private View.OnHoverListener mOnHoverListener = new XOnHoverListener();
	
	public HoverEventManager(Activity activity)
	{
		mActivity = activity;
	}
	
	public HoverEventManager(Context context)
	{
		mActivity = (Activity) context;
	}
	
	private class XOnHoverListener implements View.OnHoverListener{

		@Override
		public boolean onHover(View view, MotionEvent event) {
			switch(event.getAction())
			{
			case MotionEvent.ACTION_HOVER_ENTER:
				view.setHovered(true);
				break;
			case MotionEvent.ACTION_HOVER_EXIT:
				view.setHovered(false);
				break;
			}
			return false;
		}
	}
	
	/**
	 * for dispatchKeyEvent purpose 
	 */
	private class XOnHotKeyClickListener implements View.OnClickListener{
		@Override
		public void onClick(View view) {
			KeyEvent event = (KeyEvent)view.getTag();
			mActivity.onKeyDown(event.getKeyCode(), event);
//			mActivity.dispatchKeyEvent(event); //it's same as onKeyDown(), but...
		}
	}

	public void setHotkeyClickListener(SparseArray<KeyEvent> hotKeyMap)
	{
		View View = null;
		int size = hotKeyMap.size();
		View.OnClickListener onClickListener = new XOnHotKeyClickListener();
		for(int id=0; id<size; id++)
		{
			int resID = hotKeyMap.keyAt(id);
			View = (View) mActivity.findViewById(resID);
			View.setOnClickListener(onClickListener);
			View.setTag(hotKeyMap.get(resID));
		}
	}
	
	/**
	 * get hovet listener
	 * 
	 * @param need hover view
	 */
	public View.OnHoverListener getHoverListener()
	{
		return mOnHoverListener;
	}
	
}
