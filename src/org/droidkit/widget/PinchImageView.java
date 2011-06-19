package org.droidkit.widget;


import org.droidkit.util.tricks.Log;
import org.droidkit.widget.ScaleGestureDetector.OnScaleGestureListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class PinchImageView extends ImageView implements OnScaleGestureListener, OnGestureListener, OnDoubleTapListener, OnTouchListener  {
	private static final String TAG = "PinchImageView";
	
	private ScaleGestureDetector mScaleGestureDetector;
	private GestureDetector mNormalGestureDetector;
	private Scroller mScroller;
	
	private int mScrollX = 0;
	private int mScrollY = 0;
	private float mCurrentScale = 1.0f;
	private float mPreviousScale = 1.0f;
	
	private int mMinX = 0;
	private int mMaxX = 0;
	private int mMinY = 0;
	private int mMaxY = 0;
	
//	private Bitmap mBitmap = null;

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
		setScaleType(ScaleType.FIT_CENTER);
	}
	
	
//	
//
//	@Override
//	public void setImageBitmap(Bitmap bm) {
//		mBitmapWidth = bm.getWidth();
//		mBitmapHeight = bm.getHeight();
//		super.setImageBitmap(bm);
//	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if ((!mScaleGestureDetector.isInProgress()) && event.getAction() == event.ACTION_UP) {
			getHandler().post(mCheckEdgesRunnable);
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

		if (mCurrentScale < 2.5f)
			scaleTo(2.5f, true);
		else
			scaleTo(1.0f, true);
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
		
		
		float scrollX = -mScrollX/mCurrentScale;
		float scrollY = -mScrollY/mCurrentScale;
		
		
		float px = ((getWidth()/2)); //-mScrollX);
		float py = ((getHeight()/2)); //-mScrollY);
		
		
		
//		float px = (-mScrollX + (getWidth()/2))/mPreviousScale;
//		float py = (-mScrollY + (getHeight()/2))/mPreviousScale;
//		canvas.translate(px, py);
		canvas.scale(mCurrentScale, mCurrentScale, px, py);
		canvas.translate(scrollX, scrollY);
		
		mPreviousScale = mCurrentScale;
		
		Log.e(TAG, "scrollX: " + mScrollX + " mScrollY: " + mScrollY);
		
//		if (mCurrentScale != mPreviousScale) {
//			//try getting previous scaled size and current scaled size, finding the diff,
//			//then adjusting min and max vars proportionaly on either side? 
//			Log.e(TAG, "prevScale: " + mPreviousScale + " curScale:" + mCurrentScale);
//			int oldWidth = (int) (getWidth()*mPreviousScale);
//			int newWidth = (int) (getWidth()*mCurrentScale);
//			int wdiff = newWidth - oldWidth;
//			Log.e(TAG, "oldWidth: " + oldWidth + " newWidth: " + newWidth + " wdiff: " + wdiff);
//			float leftPercent = px / oldWidth;
//			int leftNewPixels = (int) (leftPercent*wdiff);
//			int rightNewPixels = wdiff-leftNewPixels;
//			Log.e(TAG, "leftPercent = " + leftPercent + " leftNewPixels: " + leftNewPixels + " rightNewPixels: " + rightNewPixels) ;
//			
//			mMinX-=(leftNewPixels*mCurrentScale);
//			mMaxX+=(rightNewPixels*mCurrentScale);
//			
//			mPreviousScale = mCurrentScale;
//		}
		
		super.onDraw(canvas);
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
	public void checkEdges() {
		
		if (mCurrentScale < 1.0f)
			scaleTo(1.0f, false);
		
		int targetX = mScrollX;
		int targetY = mScrollY;
		
		//TODO: need better edge checking, not only is this wrong but it also only does one side
		
		int viewWidth = getWidth();
		int viewHeight = getHeight();
		int trueWidth = getTrueWidth();
		int trueHeight = getTrueHeight();
		int dw = trueWidth - viewWidth;
		int dh = trueHeight - viewHeight;
		int hdw = dw/2;
		int hdh = dh/2;
		mMinX = -hdw;
		mMinY = -hdh;
		mMaxX = hdw;
		mMaxY = hdh;
		
		
		if (mScrollX < mMinX)
			targetX = mMinX;
		if (mScrollY < mMinY)
			targetY = mMinY;
		if (mScrollX > mMaxX)
			targetX = mMaxX;
		if (mScrollY > mMaxY)
			targetY = mMaxY;
		
		if (targetX != mScrollX || targetY != mScrollY)
			scrollTo(targetX, targetY, false);
		
		
		
		postInvalidate();
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