package com.ott.webtv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class WebGridView extends GridView {
	private int itemWidth;
	private int itemHeight;
	private int itemXSpacing;
	private int itemYSpacing;

	private Bitmap mBitmap;

	private Scroller mScroller;

	private Matrix m;

	private float sy;
	private float sx;

	private int sDuration = 1000;

	private boolean isScroll;
	private boolean isSetSelection;

	public WebGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new Scroller(context);
		m = new Matrix();

		mBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.web_logo_bg_h);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (getChildCount() > 0) {
			itemWidth = getChildAt(0).getWidth();
			itemHeight = getChildAt(0).findViewById(R.id.iv).getHeight();

			if (getChildCount() > 1) {

				itemXSpacing = getChildAt(1).getLeft()
						- getChildAt(0).getLeft();
				itemYSpacing = getChildAt(0).getHeight() + getVerticalSpacing();
			}

			sx = (float) itemWidth / mBitmap.getWidth();
			sy = (float) itemHeight / mBitmap.getHeight();
			m.setScale(sx, sy);

			mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(),
					mBitmap.getHeight(), m, true);
		}
	}

	@Override
	public void setSelection(int position) {
		super.setSelection(position);
		isSetSelection = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isSetSelection) {
			View view = getSelectedView();
			if (null != view) {
				mScroller.setFinalX(view.getLeft());
				mScroller.setFinalY(view.getTop());
				isSetSelection = false;
				return;
			}
		}

		if (mScroller.computeScrollOffset()) {
			invalidate();
		} else if (isScroll) {
			isScroll = false;
		}
		canvas.drawBitmap(mBitmap, mScroller.getCurrX(), mScroller.getCurrY(),
				null);
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View view = getSelectedView();

		if (null != view) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (getSelectedItemPosition() < getAdapter().getCount()
						- getNumColumns()) {
					mScroller.startScroll(view.getLeft(), view.getTop(), 0,
							itemYSpacing, sDuration);
					isScroll = true;
				}
				break;

			case KeyEvent.KEYCODE_DPAD_UP:
				if (getSelectedItemPosition() >= getNumColumns()) {
					mScroller.startScroll(view.getLeft(), view.getTop(), 0,
							-itemYSpacing, sDuration);
					isScroll = true;
				}

				break;

			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (getSelectedItemPosition() % getNumColumns() > 0) {
					mScroller.startScroll(view.getLeft(), view.getTop(),
							-itemXSpacing, 0, sDuration);
					isScroll = true;
				}
				break;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (getSelectedItemPosition() % getNumColumns() < getNumColumns() - 1) {
					mScroller.startScroll(view.getLeft(), view.getTop(),
							itemXSpacing, 0, sDuration);
					isScroll = true;
				}
				break;

			}
		}

		return super.onKeyDown(keyCode, event);
	}

	public void setFocusBitmap(int resourceId) {
		mBitmap = BitmapFactory.decodeResource(getResources(), resourceId);
	}
}
