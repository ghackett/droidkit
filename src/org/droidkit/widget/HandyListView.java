package org.droidkit.widget;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class HandyListView extends ListView {
	
	public interface OnSizeChangedListener {
		public void onSizeChanged(HandyListView listView, int w, int h, int oldw, int oldh);
	}
	
//	private View mEmptyListView = null;
//	private View mLoadingListView = null;
	private WeakReference<OnSizeChangedListener> mSizeListener = null;
	private int mFadingEdgeColor = -1;
	private boolean mIsBeingTouched = false;
//	private int mListViewHiddenValue = View.GONE;

	public HandyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initHandyListView();
	}

	public HandyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initHandyListView();
	}

	public HandyListView(Context context) {
		super(context);
		initHandyListView();
	}
	
	private void initHandyListView() {
		
	}
	
	
	
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch(ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mIsBeingTouched = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsBeingTouched = false;
                break;
        }
        return super.onTouchEvent(ev);
    }

	public boolean isBeingTouched() {
	    return mIsBeingTouched;
	}
    /**
	 * accepts View.INVISIBLE or View.GONE, default is View.GONE
	 * might not work with empty view
	 * @param hiddenValue
	 */
//	public void setListViewHiddenValue(int hiddenValue) {
//		mListViewHiddenValue = hiddenValue;
//	}
//
//	@Override
//	public void setAdapter(ListAdapter adapter) {
//		if (mLoadingListView != null)
//			mLoadingListView.setVisibility(View.GONE);
//		setVisibility(View.VISIBLE);
//		super.setEmptyView(mEmptyListView);
//		mEmptyListView = null;
//		super.setAdapter(adapter);
//		
//	}
//
//	@Override
//	public View getEmptyView() {
//		if (mEmptyListView != null)
//			return mEmptyListView;
//		return super.getEmptyView();
//	}
//
//	@Override
//	public void setEmptyView(View emptyView) {
//		mEmptyListView = emptyView;
//		if (mEmptyListView != null)
//			mEmptyListView.setVisibility(View.GONE);
//	}
//	
//	public void setLoadingListView(View loadingView) {
//		mLoadingListView = loadingView;
//		if (mLoadingListView != null) {
//			setVisibility(mListViewHiddenValue);
//			mLoadingListView.setVisibility(View.VISIBLE);
//		}
//	}
	
	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		mSizeListener = new WeakReference<HandyListView.OnSizeChangedListener>(listener);
	}
	
	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
        if (mSizeListener != null) {
            OnSizeChangedListener listener = mSizeListener.get();
            if (listener != null)
                listener.onSizeChanged(this, w, h, oldw, oldh);
        }
	}
	
	public ListAdapter getBaseAdapter() {
		ListAdapter adapter = super.getAdapter();
		if (adapter instanceof HeaderViewListAdapter) 
			return ((HeaderViewListAdapter)adapter).getWrappedAdapter();
		return adapter;
	}

	public void setFadingEdgeColor(int color) {
		mFadingEdgeColor = color;
	}
	
	@Override
	public int getSolidColor() {
		if (mFadingEdgeColor == -1)
			return super.getSolidColor();
		return mFadingEdgeColor;
	}
	
}
