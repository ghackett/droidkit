package org.droidkit.widget;

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
	
	public static final float MIN_SCALE = 0.75f;
	
	public interface OnFlipListener {
		public void onFlippableViewVisibilityChanged(FlippableView view, boolean frontIsShowing);
		public void onViewFlipped(FlippableView view, boolean frontIsShowing);
		public void onViewBeingScrolled(FlippableView view);
	}
	
	protected Scroller mScroller;
    private int mTouchSlop;
    private int mMinVelocity;
    private int mMaxVelocity;
    private boolean mIsBeingDragged;;
    private int mLastMotionX = 0;
    private int mLastMotionY = 0;
    private VelocityTracker mVelocityTracker;
    
    private int mXOnDown = -1;
    private int mScrollX = 0;
    private boolean mScrollingDisabled = false;
    private boolean mFlippingDisabled = false;
    
    private boolean mFlipNeededOnLayout = false;
    
    protected Camera mCamera;
    protected Matrix mCameraMatrix;
	
	protected View mFrontView;
	protected View mBackView;
	
	protected int mFlipPadding = 0;
	
	protected OnFlipListener mListener = null;

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
	
	/**
	 * normally the moving your finger 100% across the view will result in one full flip
	 * however, if your visible front and back views have padding on them, that won't feel right
	 * so you can assign a flip padding that adujust how far you have to scroll to get one full flip.
	 * The flip padding is multiplied by two, the idea being if you're setting right and left padding
	 * on the child views, you could use that same value as the flip padding. 
	 * @param flipPadding
	 */
	public void setFlipPadding(int flipPadding) {
		mFlipPadding = flipPadding;
		updateLayout();
	}
	
	private int getFullFlipWidth() {
		return getWidth() - (mFlipPadding*2);
	}
	
	public void setOnFlipListener(OnFlipListener listener) {
		mListener = listener;
	}
	

	@Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
        	updateLayout();
        }
    }
	
	private void updateLayout() {
		if (getWidth() > 0) {
	        if (mFrontView.getVisibility() == VISIBLE) {
	        	if (mFlipNeededOnLayout) {
	        		mFlipNeededOnLayout = false;
	        		scrollTo(getFullFlipWidth(), true);
	        	} else {
	        		scrollTo(0, true);
	        	}
	        } else {
	        	if (mFlipNeededOnLayout) {
	        		mFlipNeededOnLayout = false;
	        		scrollTo(0, true);
	        	} else {
	        		scrollTo(getFullFlipWidth(), true);
	        	}
	        }
		}
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
				mBackView.setVisibility(View.VISIBLE);
				super.addView(child, 0, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				break;
			default:
				throw new RuntimeException("Can't add more than 2 views to FlippableView");
		}
	}
	
	public void setFlippingDisabled(boolean disabled) {
	    if (mFlippingDisabled != disabled) {
	        mFlippingDisabled = disabled;
	        int width = getFullFlipWidth();
	        if (mFlippingDisabled && width > 0 && mScrollX%width != 0)
	            finishScroll(true);
	    }
	}
	
	public boolean isFlippingDisabled() {
	    return mFlippingDisabled;
	}
	
	public void flip(boolean animated) {
	    if (mFlippingDisabled)
	        return;
	    if (getWidth() == 0) {
	    	mFlipNeededOnLayout = !mFlipNeededOnLayout;
	    	return;
	    }
	    if (animated) {
	        if (mScrollX % getFullFlipWidth() == 0) {
	            int div = mScrollX / getFullFlipWidth();
	            int scrollBy = div % 2 == 0 ? getFullFlipWidth() : -getFullFlipWidth();
	            mScroller.startScroll(mScrollX, 0, scrollBy, 0, 750);
	            invalidate();
	        }
	    } else {
	        if (mFrontView.getVisibility() == VISIBLE) {
	            scrollTo(getFullFlipWidth(), true);
	        } else {
	            scrollTo(0, true);
	        }
	    }
	}
	
	public void flipBackwardAnimated() {
		if (mFlippingDisabled)
	        return;
		if (getWidth() == 0) {
	    	mFlipNeededOnLayout = !mFlipNeededOnLayout;
	    	return;
	    }
        if (mScrollX % getFullFlipWidth() == 0) {
            mScroller.startScroll(mScrollX, 0, getFullFlipWidth(), 0, 750);
            invalidate();
        }
        
	}
	
	public void flipForwardAnimated() {
		if (mFlippingDisabled)
	        return;
		if (getWidth() == 0) {
	    	mFlipNeededOnLayout = !mFlipNeededOnLayout;
	    	return;
	    }
        if (mScrollX % getFullFlipWidth() == 0) {
            mScroller.startScroll(mScrollX, 0, -getFullFlipWidth(), 0, 750);
            invalidate();
        }
	}
	
	protected void setScrollingDisabled(boolean scrollingDisabled) {
		mScrollingDisabled = scrollingDisabled;
		if (mScrollingDisabled) {
			mXOnDown = -1;
			resetAllowScrollingTimer();
		} else {
			cancelAllowScrollingTimer();
		}
	}
	
	protected boolean isOkToScroll(int action, int x, int y) {
	    if (mFlippingDisabled)
	        return false;
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
				if (mXOnDown < 0)
					mXOnDown = x;
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
				mXOnDown = x;
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
				if (mXOnDown < 0)
					mXOnDown = x;
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
				
				if (Math.abs(x - mXOnDown) <= getFullFlipWidth()) {
					int dx = mLastMotionX - x;
					scrollBy(dx, true);
				} else {
			    	final int width = getFullFlipWidth();
			    	final int remainder = Math.abs(mScrollX) % width;
			    	int div = mScrollX / width;
			    	if (remainder != 0) {
			    		int scrollTo = div * width;
			    		if (mScrollX < 0) {
			    			if (remainder >= (width/2))
				    			scrollTo -= width;
			    		} else {
				    		if (remainder >= (width/2))
				    			scrollTo += width;
			    		}
			    		scrollTo(scrollTo, true);
			    	}
				}
				mLastMotionX = x;
				break;
			}
			case MotionEvent.ACTION_DOWN: {
				mXOnDown = x;
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
                
                
                if (Math.abs(x - mXOnDown) <= getFullFlipWidth() && Math.abs(initialVelocity) > mMinVelocity) {
                	fling(-initialVelocity);
                } else {
                	finishScroll(true);
                }
                
                mXOnDown = -1;
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
    	final int width = getFullFlipWidth();
		int div = mScrollX / width;
		int scrollTo = div * width;
		if (mScrollX < 0) {
			if (initVelocity < 0)
				scrollTo -= width;
		} else {
			if (initVelocity > 0)
				scrollTo += width;
		}
		mScroller.startScroll(mScrollX, 0, scrollTo - mScrollX, 0);
		invalidate();
    }
    
    private void finishScroll(boolean invalidate) {
    	if (!mScroller.isFinished())
    		mScroller.abortAnimation();
    	final int width = getFullFlipWidth();
    	final int remainder = Math.abs(mScrollX) % width;
    	int div = mScrollX / width;
    	if (remainder != 0) {
    		int scrollTo = div * width;
    		if (mScrollX < 0) {
    			if (remainder >= (width/2))
	    			scrollTo -= width;
    		} else {
	    		if (remainder >= (width/2))
	    			scrollTo += width;
    		}
    		mScroller.startScroll(mScrollX, 0, scrollTo - mScrollX, 0);
    		if (invalidate)
    			invalidate();
    	} else {
    		notifyFlip(div % 2 == 0);
    	}
    }
    
    private void updateViewVisiblilty() {
        final int width = getFullFlipWidth();
        final int remainder = Math.abs(mScrollX) % width;
        int div = mScrollX / width;
        if (mScrollX < 0) {
            if (remainder >= (width/2))
                div -= 1;
        } else {
            if (remainder >= (width/2))
                div += 1;
        }
        if (div % 2 == 0) {
            if (mFrontView.getVisibility() != View.VISIBLE) {
            	notifyVisibilityChange(true);
                mFrontView.setVisibility(View.VISIBLE);
            }
            //since both views start off as visibile, hide the back view if scrolling
            if (remainder != 0 && mBackView.getVisibility() != View.GONE) {
            	mBackView.setVisibility(View.GONE);
            }
        } else {
            if (mFrontView.getVisibility() != View.GONE) {
            	notifyVisibilityChange(false);
                mBackView.setVisibility(View.VISIBLE);
                mFrontView.setVisibility(View.GONE);
            }
        }
    }
    
    private void notifyVisibilityChange(boolean frontShowing) {
    	if (mListener != null) {
    		mListener.onFlippableViewVisibilityChanged(this, frontShowing);
    	}
    }
    
    private void notifyFlip(boolean frontShowing) {
    	if (mListener != null) {
    		mListener.onViewFlipped(this, frontShowing);
    	}
    }
    
    private void notifyScrolling() {
    	if (mListener != null) {
    		mListener.onViewBeingScrolled(this);
    	}
    }
    
    private void scrollTo(int x, boolean invalidate) {
    	notifyScrolling();
//    	CLog.e("Scrolling: mScrollX = " + mScrollX + ", new = " + x);
    	mScrollX = x;
    	updateViewVisiblilty();
    	if (invalidate)
    		invalidate();
    }
    
    private void scrollBy(int dx, boolean invalidate) {
    	scrollTo(mScrollX + dx, invalidate);
    }

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			final int x = mScroller.getCurrX();
			scrollTo(x, false);
			if (x == mScroller.getFinalX()) {
				finishScroll(false);
			}
			postInvalidate();
		}
		super.computeScroll();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		final Camera camera = mCamera;
		
    	final int width = getFullFlipWidth();
    	final int adjustedScroll = mScrollX - (width/2);
//    	final int height = getHeight();
    	final int remainder = adjustedScroll % width;
    	
    	float percent = (float)remainder / (float)width;
    	float degrees = percent * 180f;
    	degrees = 180f - degrees;
    	if (adjustedScroll < 0)
    		degrees += 90;
    	else
    		degrees-= 90;
    	
//    	CLog.e("DEGREES = " + degrees);
    	
    	float scaleRange = 1f - MIN_SCALE;
    	int scaleRemainder = mScrollX % (width);
    	float scalePercent = Math.abs((float)scaleRemainder / (width));
    	scalePercent = Math.abs(scalePercent - 0.5f)*2f;
    	float scale = MIN_SCALE + ((scalePercent) * scaleRange);
    	
//    	CLog.e("SCALE PERCENT = " + scalePercent);
//    	CLog.e("SCALE = " + scale);
    	
    	
    	
    	mCameraMatrix.reset();
    	final int centerX = getWidth()/2;
    	final int centerY = getHeight()/2;
    	camera.save();
    	camera.rotateY(degrees);
    	camera.getMatrix(mCameraMatrix);
    	mCameraMatrix.preTranslate(-centerX, -centerY);
    	mCameraMatrix.postTranslate(centerX, centerY);
    	mCameraMatrix.preScale(scale, scale, (float)centerX, (float)centerY);
    	camera.restore();
    	canvas.concat(mCameraMatrix);
    	
	}
	
    
}
