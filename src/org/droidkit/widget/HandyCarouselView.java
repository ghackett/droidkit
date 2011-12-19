package org.droidkit.widget;

//com.episode6.android.common.ui.widget.HandyCarouselView

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.droidkit.DroidKit;
import org.droidkit.R;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;


/*
 * IMPORTANT: if you turn autoscrolling on, remember to turn it off in your activity's onPause
 */
public class HandyCarouselView extends FrameLayout {
	
	
	public interface OnPageChangedListener {
		public void OnPageChanged(HandyCarouselView pagedView, int lastPage, int newPage, int pageCount);
	}
	
	private final Handler mHandler = new Handler();
	
	private WeakReference<StoppableScrollView> mParentScrollview;
	private LinearLayout mInnerView;
	private ArrayList<ScaleableFrameLayout> mContainerViews;
	private ArrayList<ArrayList<View>> mRecycledViews;
	private AdapterViewInfo[] mVisibleViews;
	private ListAdapter mAdapter;
	private int mCurrentPage;
	private Scroller mScroller;
	private int mTouchSlop;
	private int mMinVelocity;
	private int mMaxVelocity;
	private boolean mIsBeingDragged;
	private boolean mIsBeingScrolled;
	private float mLastMotionX;
	private VelocityTracker mVelocityTracker;
	private boolean mAutoScroll;
	private long mAutoScrollInterval;
	private int mAutoScrollDuration;
	private boolean mInfiniteLoop;
	private boolean mStopAutoScrollingOnTouch;
	private boolean mPreventInvalidate;
	private OnPageChangedListener mPageChangedListener;
	private LinearLayout mPageIndicatorView;
	private int mScrollPadding;
//	private int mSpacing;
	private int mSideShowable;
	private int mSideShowablePort;
	private int mSideShowableLand;
	private float mSideScale;
	private boolean mStrictScrolling = false;
	private int mMargin;
	private int mMarginLandscape;
	private int mMarginPortrait;
	

	public HandyCarouselView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPagedView();
	}

	public HandyCarouselView(Context context) {
		super(context);
		initPagedView();
	}

	private void initPagedView() {
		setWillNotCacheDrawing(true);
		setWillNotDraw(false);
		
		setSpacing(60, 100);
		mSideScale = 0.75f;
		mMargin = 0;
		mPageIndicatorView = null;
		mPreventInvalidate = false;
		mParentScrollview = null;
		mContainerViews = new ArrayList<ScaleableFrameLayout>(5);
		mVisibleViews = new AdapterViewInfo[5];
		mRecycledViews = null;
		mAdapter = null;
		mCurrentPage = 0;
		mIsBeingDragged = false;
		mIsBeingScrolled = false;
		mVelocityTracker = null;
		mAutoScroll = false;
		mAutoScrollInterval = 400;
		mInfiniteLoop = true;
		mStopAutoScrollingOnTouch = false;
		mPageChangedListener = null;
		
		mInnerView = new LinearLayout(getContext());
		mInnerView.setOrientation(LinearLayout.HORIZONTAL);
		mInnerView.setGravity(Gravity.CENTER_HORIZONTAL);
		
		for (int i =0; i<5; i++) {

			mContainerViews.add(new ScaleableFrameLayout(getContext()));
			mInnerView.addView(mContainerViews.get(i));
		}
		
		addView(mInnerView);
		
		mScroller = new Scroller(getContext());
		
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMinVelocity = configuration.getScaledMinimumFlingVelocity();
		mMaxVelocity = configuration.getScaledMaximumFlingVelocity();
		
	}
	
	public void setMarginPx(int marginPort, int marginLand) {
        mMarginPortrait = marginPort;
        mMarginLandscape = marginLand;
        
        updateSideShowable();
        
        if (mAdapter != null && getWidth() > 0) {
            mHandler.post(new Runnable() {
    
                @Override
                public void run() {
                    updatePageLayout();
                }
            });
        }
	}
	
	public void setMarginDp(int marginPortDip, int marginLandscapeDp) {
	    setMarginPx(DroidKit.getPixels(marginPortDip), DroidKit.getPixels(marginLandscapeDp));
	}
	
	/**
	 * 
	 * @param spacingDp The spacing inbetween views
	 * @param sideShowableDp The amount of each view on either side to show
	 */
	public void setSpacing(int sideShowablePortDp, int sideShowableLandDp) {
	    mSideShowablePort = DroidKit.getPixels(sideShowablePortDp);
	    mSideShowableLand = DroidKit.getPixels(sideShowableLandDp);
	    updateSideShowable();
	    
	    if (mAdapter != null && getWidth() > 0) {
    	    mHandler.post(new Runnable() {
    
    	        @Override
    	        public void run() {
    	            updatePageLayout();
    	        }
    	    });
	    }
	    
	}
	
	private void updateSideShowable() {
	    if (DroidKit.isDeviceInLandscapeMode()) {
	        mSideShowable = mSideShowableLand;
	        mMargin = mMarginLandscape;
	    } else {
	        mSideShowable = mSideShowablePort;
	        mMargin = mMarginPortrait;
	    }
	    mScrollPadding = mSideShowable*2;
	}
	
	public int getCurrentPage() {
	    return mCurrentPage;
	}
	
	public void setCurrentPage(int currentPage) {
	    mCurrentPage = currentPage;
	    updatePageLayout();
	}
	
	
	private void updatePageLayout() {
		if (mAdapter != null && getWidth() > 0) {
		    removeAllViews();
			int pageWidth = getWidth();
			int pageHeight = getHeight();
			mInnerView.setLayoutParams(new FrameLayout.LayoutParams(pageWidth*5, pageHeight));
			for (int i = 0; i<5; i++) {
			    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(pageWidth-((mSideShowable*2)), pageHeight);
			    lp.leftMargin = mMargin;
			    lp.rightMargin = mMargin;
//			    lp.leftMargin = mSpacing/2;
//			    lp.rightMargin = mSpacing/2;
				mContainerViews.get(i).setLayoutParams(lp);
			}
			
			int startIndex = 0;
			int startPosition = 0;
			
			startPosition = mCurrentPage - 2;
			
//			if (startPosition == -1) {
//				startPosition = mAdapter.getCount()-1;
//			} else if (startPosition == -2) {
//			    startPosition = mAdapter.getCount()-2;
//			}
			    
			int position = startPosition;
			for (int i = startIndex; i<5; i++) {
				
//				if (position >= mAdapter.getCount())
//					position = 0;
				
				AdapterViewInfo info = getAdapterView(position);
				
				if (info != null) {
					info.view.setLayoutParams(new ScaleableFrameLayout.LayoutParams(pageWidth-((mSideShowable*2)), pageHeight));
					info.view.setLayoutParams(new ScaleableFrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					mContainerViews.get(i).addView(info.view);
					mVisibleViews[i] = info;
				}
				
				position++;
			}
			scrollTo(pageWidth*2, true);
			resetScales();
			updatePageIndicator();
		}
	}
	
	private void resetScales() {
	    for (int i = 0; i<mContainerViews.size(); i++) {
	        setChildScale(i, i == 2 ? 1.0f : mSideScale);
	    }
	}
	
	private void postPageChanged() {
	    mHandler.post(new Runnable() {

            @Override
            public void run() {
                pageChanged();
            }
	        
	    });
	}
	
	private void pageChanged() {
		if (getScrollX() == getCenterScrollTarget()) {
			//stayed on the same page, no need to continue
			return;
		}
		
		for (int i = 0; i<5; i++) {
			mContainerViews.get(i).removeAllViews();
		}
		
		int lastPage = mCurrentPage;
		
		if (getScrollX() == getLeftScrollTarget()) {
			//go one back
			mCurrentPage--;
			
			if (mCurrentPage == -1) {
				mCurrentPage = mAdapter.getCount()-1;
			}
			
			recycleView(mVisibleViews[4]);
			mVisibleViews[4] = mVisibleViews[3];
			mVisibleViews[3] = mVisibleViews[2];
			mVisibleViews[2] = mVisibleViews[1];
			mVisibleViews[1] = mVisibleViews[0];
			mVisibleViews[0] = getAdapterView(mCurrentPage-2);
			
		} else if (getScrollX() == getRightScrollTarget()) {
			//next page
			mCurrentPage++;
			
			if (mCurrentPage == mAdapter.getCount()) {
				mCurrentPage = 0;
			}
			
			recycleView(mVisibleViews[0]);
			mVisibleViews[0] = mVisibleViews[1];
			mVisibleViews[1] = mVisibleViews[2];
			mVisibleViews[2] = mVisibleViews[3];
			mVisibleViews[3] = mVisibleViews[4];
			mVisibleViews[4] = getAdapterView(mCurrentPage+2);
		}
		
		
		for (int i = 0; i<5; i++) {
			if (mVisibleViews[i] != null)
				mContainerViews.get(i).addView(mVisibleViews[i].view);
		}
		scrollTo(getCenterScrollTarget(), true);
		
		if (mPageChangedListener != null) {
			mPageChangedListener.OnPageChanged(this, lastPage, mCurrentPage, mAdapter.getCount());
		}
		resetScales();
		updatePageIndicator();
	}
	
	private void updatePageIndicator() {
		if (mPageIndicatorView != null && mAdapter != null) {
			if (!resetPageIndicatorViewIfNecessary()) {
				if (mAdapter.getCount() > 9) {
					TextView tv = (TextView)mPageIndicatorView.getChildAt(0);
					int curPageDisplay = mCurrentPage+1;
					tv.setText(curPageDisplay + "/" + mAdapter.getCount());
				} else if (mAdapter.getCount() > 0) {
					for (int i = 0; i<mAdapter.getCount(); i++) {
						ImageView iv = (ImageView)mPageIndicatorView.getChildAt(i);
						iv.setImageResource((i == mCurrentPage ? R.drawable.circle_black : R.drawable.circle_grey));
					}
				} else {
					mPageIndicatorView.removeAllViews();
				}
			}
		} else if (mPageIndicatorView != null) {
			mPageIndicatorView.removeAllViews();
		}
	}
	
	/**
	 * 
	 * @return true if the pageIndicatorView was reset
	 */
	private boolean resetPageIndicatorViewIfNecessary() {
		if (mPageIndicatorView != null && mAdapter != null) {
			if (mAdapter.getCount() > 9) {
				if (mPageIndicatorView.getChildCount() != 1) {
					buildPageIndicator();
					return true;
				}
				if (!(mPageIndicatorView.getChildAt(0) instanceof TextView)) {
					buildPageIndicator();
					return true;
				}
			} else if (mAdapter.getCount() > 0) {
				if (mPageIndicatorView.getChildCount() != mAdapter.getCount()) {
					buildPageIndicator();
					return true;
				}
				if (!(mPageIndicatorView.getChildAt(0) instanceof ImageView)) {
					buildPageIndicator();
					return true;
				}
			}
		}
		return false;
	}
	
	private void buildPageIndicator() {
		if (mPageIndicatorView != null) {
			mPageIndicatorView.removeAllViews();
			if (mAdapter != null) {
				if (mAdapter.getCount() > 9) {
					//use a text view
					TextView tv = new TextView(getContext());
					int curPageDisplay = mCurrentPage+1;
					tv.setText(curPageDisplay + "/" + mAdapter.getCount());
					mPageIndicatorView.addView(tv);
				} else if (mAdapter.getCount() > 0) {
					//use dots
					int padding = DroidKit.getPixels(4);
					int size = DroidKit.getPixels(20);
					for (int i =0; i<mAdapter.getCount(); i++) {
						ImageView iv = new ImageView(getContext());
						iv.setImageResource((i == mCurrentPage ? R.drawable.circle_black : R.drawable.circle_grey));
						iv.setPadding(padding, padding, padding, padding);
						mPageIndicatorView.addView(iv, new LinearLayout.LayoutParams(size, size));
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @return a page indicator view that gets updated automatically when the page is changed
	 * This view is NOT attached to anything by default and it is up to the developer to choose where to 
	 * put it. The view is made up of gray/black dots if adapter has 9 or fewer items OR a textview if the
	 * adapter has 10 or more objects
	 */
	public LinearLayout getPageIndicatorView() {
		if (mPageIndicatorView == null) {
			mPageIndicatorView = new LinearLayout(getContext());
			mPageIndicatorView.setGravity(Gravity.CENTER);
			mPageIndicatorView.setOrientation(LinearLayout.HORIZONTAL);
		}
		return mPageIndicatorView;
	}
	
	public void setOnPageChangedListener(OnPageChangedListener listener) {
		mPageChangedListener = listener;
	}
	
	
	@Override
	public Handler getHandler() {
		return mHandler;
	}

	/**
	 * 
	 * @param autoscrollInterval - amount of time to wait before scrolling to next page
	 * @param autoscrollDuration - duration for scroller (amount of time it takes to scroll from one view to the next)
	 * @param stopOnTouch - should autoscrolling be stopped when the user touches the view
	 */
	public void turnOnAutoScroll(long autoscrollInterval, int autoscrollDuration, boolean stopOnTouch) {
		mAutoScroll = true;
		mAutoScrollInterval = autoscrollInterval;
		mAutoScrollDuration = autoscrollDuration;
		mStopAutoScrollingOnTouch = stopOnTouch;
		getHandler().postDelayed(mAutoScrollRunnable, mAutoScrollInterval);
	}
	
	public void turnOffAutoScroll() {
		mAutoScroll = false;
		getHandler().removeCallbacks(mAutoScrollRunnable);
	}
	
	public void setInfiniteLoopMode(boolean infLoop) {
		mInfiniteLoop = infLoop;
	}
	
	public void setParentScrollView(StoppableScrollView parentScrollView, boolean strictScrolling) {
		mParentScrollview = new WeakReference<StoppableScrollView>(parentScrollView);
		mStrictScrolling = strictScrolling;
	}
	
	public void scrollTo(int scrollX, boolean invalidate) {
		if (scrollX < getLeftScrollTarget()) {
			scrollTo(getLeftScrollTarget(), invalidate);
			return;
		}
		if (scrollX > getRightScrollTarget()) {
			scrollTo(getRightScrollTarget(), invalidate);
			return;
		}
		if (scrollX < getCenterScrollTarget() && !canScrollBack()) {
			scrollTo(getCenterScrollTarget(), invalidate);
			return;
		}
		if (scrollX > getCenterScrollTarget() && !canScrollForward()) {
			scrollTo(getCenterScrollTarget(), invalidate);
			return;
		}
		
		if (!invalidate) {
			preventInvalidate();
		}
		super.scrollTo(scrollX, 0);
		allowInvalidate();
	}
	
	public void scrollBy(int dx, boolean invalidate) {
		scrollTo(getScrollX()+dx, invalidate);
	}
	
	public void smoothScrollTo(int x) {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		
		mScroller.startScroll(getScrollX(), 0, x - getScrollX(), 0);
		invalidate();
	}
	
	public void smoothScrollTo(int x, int duration) {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		mScroller.startScroll(getScrollX(), 0, x - getScrollX(), 0, duration);
		invalidate();
	}
	
	public void fling(int initVelocity) {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		mScroller.fling(getScrollX(), 0, initVelocity, 0, getLeftScrollTarget()-1, getRightScrollTarget()+1, 0, 0);
		invalidate();
	}
	
	public void setAdapter(ListAdapter adapter) {
		mAdapter = null;
		removeAllViews();
		mAdapter = adapter;
		if (mAdapter != null) {
			mRecycledViews = new ArrayList<ArrayList<View>>();
			for (int i = 0; i<mAdapter.getViewTypeCount(); i++) {
				mRecycledViews.add(new ArrayList<View>());
			}
		}
		mCurrentPage = 0;
		updatePageLayout();
	}
	
	public ListAdapter getAdapter() {
		return mAdapter;
	}
	
	
	

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
	    updateSideShowable();
		super.onLayout(changed, left, top, right, bottom);
		
		if (changed) {
			getHandler().post(new Runnable() {
				@Override
				public void run() {
					updatePageLayout();
				}
			});
		}
	}

	public boolean canScrollBack() {
		return mAdapter != null && (mCurrentPage > 0 || mInfiniteLoop);
	}
	
	public boolean canScrollForward() {
		return mAdapter != null && (mCurrentPage < mAdapter.getCount()-1 || mInfiniteLoop);
	}
	
	private void setParentScrollingAllowed(boolean allowed) {
		if (mParentScrollview != null) {
		    StoppableScrollView ss = mParentScrollview.get();
		    if (ss != null) {
    			if (allowed)
    				ss.allowScrolling();
    			else 
    				ss.stopScrolling();
		    }
		}
	}
	

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
			return true;
		}
		
		switch(action & MotionEvent.ACTION_MASK) {
		
		case MotionEvent.ACTION_MOVE: {
		    if (mStrictScrolling) setParentScrollingAllowed(false);
			final float x = ev.getX();
			final int dx = (int)Math.abs(x - mLastMotionX);
			if (dx >= mTouchSlop) {
				mIsBeingDragged = true;
				mLastMotionX = x;
				setParentScrollingAllowed(false);
			}
			break;
		}
		
		case MotionEvent.ACTION_DOWN: {
		    if (mStrictScrolling) setParentScrollingAllowed(false);
			final float x = ev.getX();
			mLastMotionX = x;
			mIsBeingDragged = !mScroller.isFinished();
			break;
		}
		
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mIsBeingDragged = false;
			setParentScrollingAllowed(true);
			break;
		}
		
		return mIsBeingDragged;

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		
		if (mAutoScroll && mStopAutoScrollingOnTouch) {
			turnOffAutoScroll();
		}
		
		final int action = event.getAction();
		
		switch (action & MotionEvent.ACTION_MASK) {
	        case MotionEvent.ACTION_DOWN: {
	            final float x = event.getX();
	            mIsBeingDragged = true;
	            if (!mScroller.isFinished()) {
	                mScroller.abortAnimation();
	            }
	            mLastMotionX = x;
	            break;
	        }
	        
	        case MotionEvent.ACTION_MOVE: {
	        	final float x = event.getX();
	        	final int deltaX = (int) (mLastMotionX - x);
	        	mLastMotionX = x;
	        	scrollBy(deltaX, true);
	        	break;
	        }
	        
	        case MotionEvent.ACTION_CANCEL:
	        case MotionEvent.ACTION_UP: {
	        	final VelocityTracker velocityTracker = mVelocityTracker;
	            velocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
	            
	            int initialVelocity = (int) velocityTracker.getXVelocity();
	            
	            mIsBeingDragged = false;
	            if (Math.abs(initialVelocity) > mMinVelocity) {
	            	fling(-initialVelocity);
	            } else {
	            	finishScroll();
	            }
	            
	            
	            if (mVelocityTracker != null) {
	            	mVelocityTracker.recycle();
	            	mVelocityTracker = null;
	            }
	        	
	            setParentScrollingAllowed(true);
	            
	        	break;
	        }
		}
		return true;

	}
	
	private void finishScroll() {
		if (!mIsBeingDragged) {
			mIsBeingScrolled = false;
			
			
			
			if (getScrollX() == getLeftScrollTarget() || getScrollX() == getCenterScrollTarget() || getScrollX() == getRightScrollTarget()) {
				postPageChanged();
				return;
			}
			int leftThreshold = getLeftScrollTarget() + ((getCenterScrollTarget() - getLeftScrollTarget())/2);
			int rightThreshold = getCenterScrollTarget() + ((getRightScrollTarget() - getCenterScrollTarget())/2);
			if (getScrollX() < leftThreshold) {
				smoothScrollTo(getWidth());
			} else if (getScrollX() < rightThreshold) {
				smoothScrollTo(getCenterScrollTarget());
			} else {
				smoothScrollTo(getRightScrollTarget());
			}
		}
	}

	
	@Override
	public void computeScroll() {
	    updateScales();
		if (mScroller.computeScrollOffset()) {
			mIsBeingScrolled = true;
			boolean finishScroll = false;
			int curX = mScroller.getCurrX();
			if (curX > getRightScrollTarget()) {
				curX = getRightScrollTarget();
				finishScroll = true;
			}
			if (curX < getLeftScrollTarget()) {
				curX = getLeftScrollTarget();
				finishScroll = true;
			}
			
			scrollTo(curX, false);
			postInvalidate();
			
			if (finishScroll) {
				mScroller.abortAnimation();
				finishScroll();
			}
		} else if (mIsBeingScrolled) {
			finishScroll();
		}
	}
	
	private void updateScales() {
	    int scrollX = getScrollX();
        int spaceDiff = getCenterScrollTarget() - getLeftScrollTarget();
        float scaleDiff = 1.0f - mSideScale;
        
        if (scrollX < getCenterScrollTarget()) {
            int leftDiff = scrollX - getLeftScrollTarget();
            int rightDiff = spaceDiff - leftDiff;
            float leftPercent = (float)rightDiff / (float)spaceDiff;
            float rightPercent = 1f - leftPercent;
            setChildScale(1, mSideScale + (scaleDiff * leftPercent));
            setChildScale(2, mSideScale + (scaleDiff * rightPercent));
        } else if (scrollX > getCenterScrollTarget()) {
            int leftDiff = scrollX - getCenterScrollTarget();
            int rightDiff = spaceDiff - leftDiff;
            float leftPercent = (float)rightDiff / (float)spaceDiff;
            float rightPercent = 1f - leftPercent;
            setChildScale(2, mSideScale + (scaleDiff * leftPercent));
            setChildScale(3, mSideScale + (scaleDiff * rightPercent));
        } else {
            resetScales();
        }
	}
	
	private void setChildScale(int index, float scale) {
	    mContainerViews.get(index).setScale(scale);
	}
	
	@Override
	public void removeAllViews() {
		for (int i = 0; i<5; i++) {
			ScaleableFrameLayout container = mContainerViews.get(i);
			AdapterViewInfo info = mVisibleViews[i];
			if (mAdapter == null) {
				container.removeAllViews();
			} else {
				if (info != null && container.getChildAt(0) == info.view) {
					container.removeAllViews();
					recycleView(info);
				} else {
					container.removeAllViews();
				}
			}
			mVisibleViews[i] = null;
		}
//		super.removeAllViews();
	}
	
	private void recycleView(AdapterViewInfo viewInfo) {
		if (viewInfo != null) {
			mRecycledViews.get(viewInfo.type).add(viewInfo.view);
		}
	}

	
	private AdapterViewInfo getAdapterView(int position) {
		if (mAdapter == null || mAdapter.getCount() <= 0)
			return null;
		if (position < 0) {
			if (mInfiniteLoop) {
				return getAdapterView(mAdapter.getCount() + position);
			} else {
				return null;
			}
		} else if (position >= mAdapter.getCount()) {
			if (mInfiniteLoop) {
				return getAdapterView((-mAdapter.getCount()) + position);
			} else {
				return null;
			}
		}
		int viewType = mAdapter.getItemViewType(position);
		ArrayList<View> recycleArray = mRecycledViews.get(viewType);
		View v = null;
		if (recycleArray.size() > 0) {
			v = recycleArray.get(0);
			recycleArray.remove(0);
		}
		v = mAdapter.getView(position, v, this);
		AdapterViewInfo info = new AdapterViewInfo(viewType, v);
		return info;
	}

	private Runnable mAutoScrollRunnable = new Runnable() {
		
		@Override
		public void run() {
			if (mAdapter != null && mAutoScroll) {
				if ((!mIsBeingDragged) && (!mIsBeingScrolled) && (mCurrentPage < mAdapter.getCount()-1 || mInfiniteLoop)) {
					smoothScrollTo(getRightScrollTarget(), mAutoScrollDuration);
				}
				getHandler().postDelayed(mAutoScrollRunnable, mAutoScrollInterval);
				
			}
		}
	};

	private class AdapterViewInfo {
		public int type;
		public View view;
		public AdapterViewInfo(int type, View v) {
			this.type = type;
			this.view = v;
		}
	}

	@Override
	public void invalidate() {
		if (!mPreventInvalidate)
			super.invalidate();
	}

	@Override
	public void invalidate(int l, int t, int r, int b) {
		if (!mPreventInvalidate)
			super.invalidate(l, t, r, b);
	}

	@Override
	public void invalidate(Rect dirty) {
		if (!mPreventInvalidate)
			super.invalidate(dirty);
	}

	@Override
	public void invalidateDrawable(Drawable drawable) {
		if (!mPreventInvalidate)
			super.invalidateDrawable(drawable);
	}
	
	public void preventInvalidate() {
		mPreventInvalidate = true;
	}
	
	public void allowInvalidate() {
		mPreventInvalidate = false;
	}
	
	public int getLeftScrollTarget() {
	    return getWidth()+mScrollPadding-(2*mMargin);
	}
	
	public int getRightScrollTarget() {
	    return getWidth()*3-mScrollPadding+(2*mMargin);
	}
	
	public int getCenterScrollTarget() {
	    return getWidth()*2;
	}
	
}
