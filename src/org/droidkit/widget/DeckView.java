/*
 * THIS CLASS IS STILL IN DEVELOPMENT, DO NOT USE
 */

package org.droidkit.widget;

//org.droidkit.widget.HandyPagedView

import org.droidkit.DroidKit;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


/*
* IMPORTANT: if you turn autoscrolling on, remember to turn it off in your activity's onPause
*/
public class DeckView extends RelativeLayout {    
    
    private int mVisibleSideMarginPx;
    
    private RelativeLayout mTopContainer;
    
    private View mLeftView;
    private View mRightView;
    private View mTopView;

    public DeckView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initDeckView();
    }

    public DeckView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDeckView();
    }

    public DeckView(Context context) {
        super(context);
        initDeckView();
    }
   
    private void initDeckView() {
        setWillNotCacheDrawing(true);
        setWillNotDraw(false);
        
        mVisibleSideMarginPx = DroidKit.getPixels(50);
        
        mTopContainer = new RelativeLayout(getContext());
    }
    
    public void setViews(View leftView, View rightView, View topView) {
        mLeftView = leftView;
        mRightView = rightView;
        mTopView = topView;
        updateLayout();
    }
    
    public void setVisibleSideMarginPx(int visibleSideMarginPx)
    {
        mVisibleSideMarginPx = visibleSideMarginPx;
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    updateLayout();
                }
            });
        }
    }
    
    private void updateLayout() {
        if (getWidth() <= 0 || mLeftView == null || mRightView == null || mTopView == null) {
            return;
        }
        removeAllViews();
        
        RelativeLayout.LayoutParams leftViewLayoutParams = new LayoutParams(getWidth() - mVisibleSideMarginPx, getHeight());
        mLeftView.setLayoutParams(leftViewLayoutParams);
        
        RelativeLayout.LayoutParams rightViewLayoutParams = new LayoutParams(getWidth() - mVisibleSideMarginPx, getHeight());
        rightViewLayoutParams.addRule(ALIGN_PARENT_RIGHT);
        mRightView.setLayoutParams(rightViewLayoutParams);
        
        int topContainerWidth = (getWidth() * 3) - (mVisibleSideMarginPx * 2);
        RelativeLayout.LayoutParams topContainerLayoutParams = new LayoutParams(topContainerWidth, getHeight());
        topContainerLayoutParams.leftMargin = -((topContainerWidth - getWidth())/2);
        mTopContainer.setLayoutParams(topContainerLayoutParams);
        mTopContainer.setBackgroundColor(Color.TRANSPARENT);
        mTopContainer.scrollTo(0, 0);
        
        RelativeLayout.LayoutParams topViewLayoutParams = new RelativeLayout.LayoutParams(getWidth(), getHeight());
//        topViewLayoutParams.addRule(CENTER_HORIZONTAL);
        topViewLayoutParams.leftMargin = getWidth() - mVisibleSideMarginPx;
        mTopView.setLayoutParams(topViewLayoutParams);
        
        addView(mLeftView);
        addView(mRightView);
        mTopContainer.addView(mTopView);
        addView(mTopContainer);
    }
    
    public void showLeft() {
        mRightView.setVisibility(View.INVISIBLE);
        mLeftView.setVisibility(View.VISIBLE);
        mTopContainer.scrollTo(-(getWidth() - mVisibleSideMarginPx), 0);
    }
    public void showRight() {
        mRightView.setVisibility(View.VISIBLE);
        mLeftView.setVisibility(View.INVISIBLE);
        mTopContainer.scrollTo((getWidth() - mVisibleSideMarginPx), 0);

    }
    public void showTop() {
        mTopContainer.scrollTo(0, 0);
}
    
    
}

