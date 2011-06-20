package org.droidkit.widget;


import org.droidkit.util.tricks.Log;
import org.droidkit.widget.ScaleGestureDetector.OnScaleGestureListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class PinchImageView extends View implements OnScaleGestureListener, OnGestureListener, OnDoubleTapListener, OnTouchListener  {
	private static final String TAG = "PinchImageView";
	
	private ScaleGestureDetector mScaleGestureDetector;
	private GestureDetector mNormalGestureDetector;
	private Scroller mScroller;
	private Scroller mScaler;
	
	private int mScrollX = 0;
	private int mScrollY = 0;
	private float mCurrentScale = -1.0f;
	private float mPreviousScale = -1.0f;
	
	private float mMinScale = 1.0f;
	
	private float mTargetScale = 0.0f;
	private boolean mIsScaling = false;
	private boolean mIsScrolling = false;
	
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
		mScaler = new Scroller(getContext());
	}
	
	
	
	
	

	public void setImageBitmap(Bitmap bm) {
		if (bm == null) {
			mBitmapHeight = 0;
			mBitmapWidth = 0;
		} else {
			mBitmapWidth = bm.getWidth();
			mBitmapHeight = bm.getHeight();
		}
		mBitmap = bm;
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
		if ( (!mScaleGestureDetector.isInProgress()) && 
				(mScroller.isFinished()) && 
				(mScaler.isFinished()) && 
				(event.getAction() == MotionEvent.ACTION_UP) ) {
			
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
	public boolean onDown(MotionEvent e) {
		if (!mScroller.isFinished())
			mScroller.abortAnimation();
		return true;
	}
	
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		scaleBy(detector.getScaleFactor(), true);
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (mScaleGestureDetector.isInProgress())
			return false;
		
		int dx = (int)(distanceX);
		int dy = (int)(distanceY);
		
		scrollBy(dx, dy);
		
		return true;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (mScaleGestureDetector.isInProgress())
			return false;
		
		getHandler().removeCallbacks(mCheckEdgesRunnable);
		fling(-velocityX, -velocityY);

		return true;
	}
	
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (mScaleGestureDetector.isInProgress())
			return false;
		getHandler().removeCallbacks(mCheckEdgesRunnable);
		final int tapX = (int) e.getX();
		final int tapY = (int) e.getY();
		
		if (mCurrentScale != 1.5f)
			mTargetScale = 1.5f;
		else
			mTargetScale = mMinScale;
		
		getHandler().post(new Runnable() {
			
			@Override
			public void run() {
				smoothScrollBy(tapX-(getWidth()/2), tapY-(getHeight()/2));
			}
		});
		
		
		return true;
	}

	
	

	
	
	
	

	

	public void smoothScrollTo(int x, int y) {
		smoothScrollBy(x-mScrollX, y-mScrollY);
	}
	
	public void smoothScrollBy(int dx, int dy) {
		if (!mScroller.isFinished())
			mScroller.abortAnimation();
		
		mScroller.startScroll(mScrollX, mScrollY, dx, dy);
		postInvalidate();
	}
	
	public void fling(float velocityX, float velocityY) {
		if (!mScroller.isFinished())
			mScroller.abortAnimation();
		
		float scaledWidth = mBitmapWidth*mCurrentScale;
		float scaledHeight = mBitmapHeight*mCurrentScale;
		
		

		if (scaledWidth <= getWidth() && scaledHeight <= getHeight()) {
			//if whole bitmap is smaller than or equal to the viewport, just do an edge check
			postCheckEdges();
			return;
		} else if (scaledWidth <= getWidth()) {
			//else if bitmap width is shorter that viewport width, only fling the y axis
			scrollTo((int) -((getWidth()-scaledWidth)/2), mScrollY, false);
			mScroller.fling(mScrollX, mScrollY, 0, (int)velocityY, mScrollX, mScrollX, 0, getMaxScrollY());
			postInvalidate();
		} else if (scaledHeight <= getHeight()) {
			//else if bitmap height is shorter than viewport height, only fling the x axis
			scrollTo(mScrollX, (int)-((getHeight()-scaledHeight)/2), false);
			mScroller.fling(mScrollX, mScrollY, (int)velocityX, 0, 0, getMaxScrollX(), mScrollY, mScrollY);
			postInvalidate();
		} else {
			
			int targetX = mScrollX;
			int targetY = mScrollY;
			
			int maxX = getMaxScrollX();
			int maxY = getMaxScrollY();
			
			if (mScrollX < 0) {
				targetX = 0;
			}
			if (mScrollX > maxX) {
				targetX = maxX;
			}
			if (mScrollY < 0) {
				targetY = 0;
			}
			if (mScrollY > maxY) {
				targetY = maxY;
			}
			
			if (targetX != mScrollX && targetY != mScrollY) {
				//corner pull, smooth scroll
				smoothScrollTo(targetX, targetY);
			} else {
				//else normal fling
				mScroller.fling(mScrollX, mScrollY, (int)velocityX, (int)velocityY, 0, getMaxScrollX(), 0, getMaxScrollY());
				postInvalidate();
			}
		}
	}
	
	public void smoothScaleTo(float scale) {
		if (!mScaler.isFinished())
			mScaler.abortAnimation();
		int start = (int)(mCurrentScale*1000f);
		int end = (int)(scale*1000f);
		int diff = end-start;
		mScaler.startScroll(start, 0, diff, 0);
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
	
	public void postCheckEdges() {
		if (getHandler() != null) {
			getHandler().removeCallbacks(mCheckEdgesRunnable);
			getHandler().post(mCheckEdgesRunnable);
		} else {
			checkEdges();
		}
	}
	
	public int getMaxScrollX() {
		return (int)((mBitmapWidth*mCurrentScale) - getWidth());
	}
	
	public int getMaxScrollY() {
		return (int)((mBitmapHeight*mCurrentScale) - getHeight());
	}
	
	public void checkEdges() {
		
		if (mCurrentScale < mMinScale) {
			if (Math.abs(mCurrentScale - mMinScale) <= 0.003f) {
				scaleTo(mMinScale, false);
				postInvalidate();
				postCheckEdges();
			} else {
				smoothScaleTo(mMinScale);
			}
			return;
		}
		
		
		int targetX = mScrollX;
		int targetY = mScrollY;
		
		
		float scaledWidth = mBitmapWidth*mCurrentScale;
		float scaledHeight = mBitmapHeight*mCurrentScale;
		int minX = 0; 
		int maxX = getMaxScrollX();
		int minY = 0;
		int maxY = getMaxScrollY();
		
		
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
		
		if (targetX != mScrollX || targetY != mScrollY) {
			if (Math.abs(targetX-mScrollX) <= 3 && Math.abs(targetY-mScrollY) <= 3) {
				scrollTo(targetX, targetY, false);
				postInvalidate();
			} else {
				smoothScrollTo(targetX, targetY);
			}
		}
		
	}
	
	
	private Runnable mCheckEdgesRunnable = new Runnable() {
		
		@Override
		public void run() {
			checkEdges();
		}
	};
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void computeScroll() {
		if (mScaler.computeScrollOffset()) {
			mIsScaling = true;
			int newScale = mScaler.getCurrX();
			
			scaleTo(((float)newScale/1000f), false);
			postInvalidate();
		} else if (mIsScaling) {
			mIsScaling = false;
			postCheckEdges();
			
		}
		if (mScroller.computeScrollOffset()) {
			mIsScrolling = true;
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();
			
			scrollTo(x, y, false);
			postInvalidate();
		} else if (mIsScrolling) {
			mIsScrolling = false;
			
			if (mTargetScale == 0.0f)
				postCheckEdges();
			else {
				smoothScaleTo(mTargetScale);
				mTargetScale = 0.0f;
			}
		}
		super.computeScroll();
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

	public RectF getImageRect() {
		
		RectF viewPort = new RectF(0, 0, getWidth(), getHeight());
		RectF prevRect = getPreviousScaleImageRect();
		
		if (mPreviousScale == mCurrentScale)
			return prevRect;
		
		float scaleFactor = mCurrentScale / mPreviousScale;
		translateRect(prevRect, -viewPort.centerX(), -viewPort.centerY());
		
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
	
	private void translateRect(RectF r, float dx, float dy) {
		r.left+=dx;
		r.right+=dx;
		r.top+=dy;
		r.bottom+=dy;
	}
	
	
	
	
	
	
	
	
	
	
	@Override
	public void onShowPress(MotionEvent e) {}
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {return true;}
	@Override
	public void onLongPress(MotionEvent e) {}
	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {return true;}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {return true;}
	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {return true;}
	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {}

}