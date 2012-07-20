package org.droidkit.widget;

import org.droidkit.util.tricks.CLog;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;


public class FlippableView extends FrameLayout {
	
	protected Scroller mScroller;
    private int mTouchSlop;
    private int mMinVelocity;
    private int mMaxVelocity;
    private boolean mIsBeingDragged;;
    private int mLastMotionX = 0;
    private int mLastMotionY = 0;
    private VelocityTracker mVelocityTracker;
    
    private int mScrollX = 0;
    private boolean mScrollingDisabled = false;
    
    protected Camera mCamera;
    protected Matrix mCameraMatrix;
	
	protected View mFrontView;
	protected View mBackView;

	public FlippableView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initFlippableView();
	}

	public FlippableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initFlippableView();
	}

	public FlippableView(Context context) {
		super(context);
		initFlippableView();
	}

	private void initFlippableView() {
		mScroller = new Scroller(getContext());
        mIsBeingDragged = false;
        mVelocityTracker = null;
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
        
        setWillNotDraw(false);
        
        mCamera = new Camera();
        mCameraMatrix = new Matrix();
	}
	

	@Override
	public void addView(View child, int width, int height) {
		super.addView(child, width, height);
	}

	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		int childCount = getChildCount();
		switch(childCount) {
			case 0:
				mFrontView = child;
				mFrontView.setVisibility(View.VISIBLE);
				super.addView(child, index, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				break;
			case 1:
				mBackView = child;
				mBackView.setVisibility(View.GONE);
				super.addView(child, index, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				break;
			default:
				throw new RuntimeException("Can't add more than 2 views to FlippableView");
		}
	}

	@Override
	public void addView(View child, int index) {
		super.addView(child, index);
	}

	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		super.addView(child, params);
	}

	@Override
	public void addView(View child) {
		super.addView(child);
	}
	
	public void setScrollingDisabled(boolean scrollingDisabled) {
		mScrollingDisabled = scrollingDisabled;
		if (mScrollingDisabled)
			resetAllowScrollingTimer();
		else
			cancelAllowScrollingTimer();
	}
	
	protected boolean isOkToScroll(int action, int x, int y) {
		if (mScrollingDisabled) {
			if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
				setScrollingDisabled(false);
			}
			return false;
		}
		
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();
		
		if (!isOkToScroll(action, x, y))
			return false;
		
		switch(action) {
			case MotionEvent.ACTION_MOVE: {
				if (mIsBeingDragged)
					return true;
				int dx = mLastMotionX - x;
				int dy = mLastMotionY - y;
				
				if (Math.abs(dy) > mTouchSlop) {
					setScrollingDisabled(true);
					return false;
				}
				
				if (Math.abs(dx) > mTouchSlop) {
					mLastMotionX = x;
					mIsBeingDragged = true;
				}
				
				break;
			}
			case MotionEvent.ACTION_DOWN: {
				mLastMotionX = x;
				mLastMotionY = y;
				mIsBeingDragged = !mScroller.isFinished();
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				mIsBeingDragged = false;
				break;
			}
		}
		
		return mIsBeingDragged;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();
		
		if (!isOkToScroll(action, x, y))
			return false;
		
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
		
		switch(action) {
			case MotionEvent.ACTION_MOVE: {
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				
				int dx = mLastMotionX - x;
				scrollBy(dx, true);
				mLastMotionX = x;
				break;
			}
			case MotionEvent.ACTION_DOWN: {
				if (!mScroller.isFinished())
					mScroller.abortAnimation();
				mLastMotionX = x;
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				mIsBeingDragged = false;
                
				final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                int initialVelocity = (int) velocityTracker.getXVelocity();
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                
                
                if (Math.abs(initialVelocity) > mMinVelocity) {
                	fling(-initialVelocity);
                } else {
                	finishScroll();
                }
                
				break;
			}
		}
		return true;
	}
	
    
    private Runnable mAllowScrollingRunnable = new Runnable() {
        
        @Override
        public void run() {
            setScrollingDisabled(false);
        }
    };
    
    private void resetAllowScrollingTimer() {
        getHandler().removeCallbacks(mAllowScrollingRunnable);
        getHandler().postDelayed(mAllowScrollingRunnable, 100);
    }
    
    private void cancelAllowScrollingTimer() {
        getHandler().removeCallbacks(mAllowScrollingRunnable);
    }
	
    private void fling(int initVelocity) {
    	if (!mScroller.isFinished())
    		mScroller.abortAnimation();
    	mScroller.fling(mScrollX, 0, initVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
    }
    
    private void finishScroll() {
    	if (!mScroller.isFinished())
    		mScroller.abortAnimation();
    }
    
//    private void updateViewVisiblilty() {
//    	final int width = getWidth();
//    	final int div = mScrollX / width;
//    	final int remainder = mScrollX % width;
//    	
//    }
    
    private void scrollTo(int x, boolean invalidate) {
//    	CLog.e("Scrolling: mScrollX = " + mScrollX + ", new = " + x);
    	mScrollX = x;
    	if (invalidate)
    		invalidate();
    }
    
    private void scrollBy(int dx, boolean invalidate) {
    	scrollTo(mScrollX + dx, invalidate);
    }

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), false);
			postInvalidate();
		}
		super.computeScroll();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		final Camera camera = mCamera;
		
		
		
    	final int width = getWidth();
    	final int adjustedScroll = mScrollX - (width/2);
    	final int height = getHeight();
    	final int div = adjustedScroll / width;
    	final int remainder = adjustedScroll % width;
    	
    	float percent = (float)remainder / (float)width;
    	float degrees = percent * 180f;
    	degrees = 180f - degrees;
    	if (adjustedScroll < 0)
    		degrees += 90;
    	else
    		degrees-= 90;
    	
    	CLog.e("DEGREES = " + degrees);
    	
//    	final Matrix m = new Matrix();
    	mCameraMatrix.reset();
    	final int centerX = width/2;
    	final int centerY = height/2;
//    	canvas.translate(-centerX, -centerY);
    	camera.save();
    	camera.rotateY(degrees);
    	camera.getMatrix(mCameraMatrix);
    	mCameraMatrix.preTranslate(-centerX, -centerY);
    	mCameraMatrix.postTranslate(centerX, centerY);
    	mCameraMatrix.preScale(0.9f, 0.9f, (float)centerX, (float)centerY);
    	camera.restore();
    	canvas.concat(mCameraMatrix);
//    	canvas.translate(centerX, centerY);
    	
	}
	
    
}
