package org.droidkit.widget;


import org.droidkit.util.tricks.Log;
import org.droidkit.widget.ScaleGestureDetector.OnScaleGestureListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;

public class PinchImageView extends View implements OnScaleGestureListener, OnGestureListener, OnDoubleTapListener, OnTouchListener  {
	private static final String TAG = "PinchImageView";
	
	private ScaleGestureDetector mScaleGestureDetector;
	private GestureDetector mNormalGestureDetector;
	private Scroller mScroller;
	
	private int mScrollX = 0;
	private int mScrollY = 0;
	private float mCurrentScale = -1.0f;
	private float mPreviousScale = -1.0f;
	
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	
	private float mMinScale = 1.0f;
	
	
	private Bitmap mBitmap = null;
	private int mBitmapWidth = 0;
	private int mBitmapHeight = 0;

	public PinchImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPinchImageView();
	}

	public PinchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPinchImageView();
	}

	public PinchImageView(Context context) {
		super(context);
		initPinchImageView();
	}
        
	private void initPinchImageView() {
		setWillNotDraw(false);
		setWillNotCacheDrawing(true);
		mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);
		mNormalGestureDetector = new GestureDetector(getContext(), this);
		mNormalGestureDetector.setOnDoubleTapListener(this);
		setOnTouchListener(this);
		mScroller = new Scroller(getContext());
		final ViewConfiguration viewConfig = ViewConfiguration.get(getContext());
		viewConfig.getScaledMaximumFlingVelocity();
		viewConfig.getScaledMinimumFlingVelocity();
//		setScaleType(ScaleType.FIT_CENTER);
	}
	
	
	

//	@Override
	public void setImageBitmap(Bitmap bm) {
		if (bm == null) {
			mBitmapHeight = 0;
			mBitmapWidth = 0;
		} else {
			mBitmapWidth = bm.getWidth();
			mBitmapHeight = bm.getHeight();
		}
		mBitmap = bm;
//		super.setImageBitmap(bm);
		resetMinScale();
	}
	
	public void resetMinScale() {
		if (mBitmapWidth > 0 && getWidth() > 0) {
			float wScale = (float)getWidth()/(float)mBitmapWidth;
			float hScale = (float)getHeight()/(float)mBitmapHeight;
			mMinScale = Math.min(wScale, hScale);
			postCheckEdges();
		}
	}
	
	

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		resetMinScale();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if ((!mScaleGestureDetector.isInProgress()) && event.getAction() == event.ACTION_UP) {
			postCheckEdges();
		} else {
			getHandler().removeCallbacks(mCheckEdgesRunnable);
		}
		
		mScaleGestureDetector.onTouchEvent(event);
		mNormalGestureDetector.onTouchEvent(event);
		return true;
	}
	
	public int getTrueWidth() {
		return (int)(getWidth()*mCurrentScale);
	}
	
	public int getTrueHeight() {
		return (int)(getHeight()*mCurrentScale);
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		
		return true;
	}
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		scaleBy(detector.getScaleFactor(), true);
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (mScaleGestureDetector.isInProgress())
			return false;
		
		int dx = (int)(distanceX); ///mCurrentScale);
		int dy = (int)(distanceY); ///mCurrentScale);
		
		scrollBy(dx, dy);
		
		return true;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (mScaleGestureDetector.isInProgress())
			return false;
		return true;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (mScaleGestureDetector.isInProgress())
			return false;

		if (mCurrentScale != 2.0f)
			scaleTo(2.0f, true);
		else
			scaleTo(mMinScale, true);
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (mScaleGestureDetector.isInProgress())
			return false;

		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap == null || getWidth() == 0) {
			super.onDraw(canvas);
			return;
		}
		
		canvas.drawBitmap(mBitmap, null, getImageRect(), null);
		super.onDraw(canvas);
		
	}
	
	private void logRect(String msg, RectF r) {
		Log.e(TAG, msg + " - RECT: l:" + r.left + " r:" + r.right + " t:" + r.top + " b:" + r.bottom + " center:" + r.centerX() + ", " + r.centerY());
	}
	
	private void translateRect(RectF r, float dx, float dy) {
		r.left+=dx;
		r.right+=dx;
		r.top+=dy;
		r.bottom+=dy;
	}

	
	public RectF getImageRect() {
		
		RectF viewPort = new RectF(0, 0, getWidth(), getHeight());
		RectF prevRect = getPreviousScaleImageRect();
		
		if (mPreviousScale == mCurrentScale)
			return prevRect;
		
		
		float scaleFactor = mCurrentScale / mPreviousScale;
		logRect("before trans", prevRect);
		translateRect(prevRect, -viewPort.centerX(), -viewPort.centerY());
		logRect("after trans", prevRect);
		
		float newWidth = prevRect.width()*scaleFactor;
		float newHeight = prevRect.height()*scaleFactor;
		float oldViewportCenterX = prevRect.left;
		float oldViewPortCenterY = prevRect.top;
		
		RectF newRect = new RectF(0, 0, newWidth, newHeight);
		translateRect(newRect, oldViewportCenterX*scaleFactor, oldViewPortCenterY*scaleFactor);
		translateRect(newRect, viewPort.centerX(), viewPort.centerY());
				
		mScrollX = (int) -newRect.left;
		mScrollY = (int) -newRect.top;

		mPreviousScale = mCurrentScale;
		return newRect;

	}
	
	public RectF getPreviousScaleImageRect() {
		float scaledWidth = mBitmapWidth*mPreviousScale;
		float scaledHeight = mBitmapHeight*mPreviousScale;
		
		RectF r = new RectF();
		r.left = -mScrollX;
		r.top = -mScrollY;
		r.bottom = (int) (r.top+scaledHeight);
		r.right = (int) (r.left+scaledWidth);
		return r;
	}
	
	
	
	
	
	@Override
	public void scrollBy(int x, int y) {
		scrollBy(x, y, true);
	}

	@Override
	public void scrollTo(int x, int y) {
		scrollTo(x, y, true);
	}

	public void scrollBy(int dx, int dy, boolean invalidate) {
		scrollTo(mScrollX+dx, mScrollY+dy, invalidate);
	}
	
	public void scrollTo(int x, int y, boolean invalidate) {
		mScrollX = x;
		mScrollY = y;
		
		if (invalidate)
			invalidate();
	}
	
	
	
	public void scaleTo(float scale, boolean invalidate) {
		mPreviousScale = mCurrentScale;
		mCurrentScale = scale;
		if (invalidate)
			invalidate();
	}
	public void scaleBy(float ds, boolean invalidate) {
		scaleTo(mCurrentScale*ds, invalidate);
	}
	
	public void postCheckEdges() {
		if (getHandler() != null) {
			getHandler().removeCallbacks(mCheckEdgesRunnable);
			getHandler().post(mCheckEdgesRunnable);
		} else {
			checkEdges();
		}
	}
	public void checkEdges() {
		
		if (mCurrentScale < mMinScale) {
			scaleTo(mMinScale, true);
			postCheckEdges();
			return;
		}
		
		
		int targetX = mScrollX;
		int targetY = mScrollY;
		
		//TODO: need better edge checking, not only is this wrong but it also only does one side
		
		float scaledWidth = mBitmapWidth*mCurrentScale;
		float scaledHeight = mBitmapHeight*mCurrentScale;
		int minX = 0; //(int) (getWidth() - scaledWidth);
		int maxX = (int) (scaledWidth - getWidth());
		int minY = 0;
		int maxY = (int) (scaledHeight - getHeight());
		
		if (scaledWidth <= getWidth()) {
			targetX = (int) -((getWidth()-scaledWidth)/2);
		} else {
			if (mScrollX < minX)
				targetX = minX;
			if (mScrollX > maxX)
				targetX = maxX;
		}
		
		if (scaledHeight <= getHeight()) {
			targetY = (int) -((getHeight()-scaledHeight)/2);
		} else {
			if (mScrollY < minY)
				targetY = minY;
			if (mScrollY > maxY)
				targetY = maxY;
		}
		
		
		Log.e(TAG, "minX = " + minX + " maxX = " + maxX + " mScrollX = " + mScrollX);
		
		if (targetX != mScrollX || targetY != mScrollY) {
			scrollTo(targetX, targetY, false);
			postInvalidate();
		}
		
		
		
		
	}
	
	
	private Runnable mCheckEdgesRunnable = new Runnable() {
		
		@Override
		public void run() {
			checkEdges();
		}
	};
	
	
	
	
	@Override
	public void onShowPress(MotionEvent e) {}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
	}
	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}


}