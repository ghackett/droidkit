/*
 * Copyright (C) 2010-2011 Michael Novak <michael.novakjr@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.droidkit.widget;

import android.app.LocalActivityManager;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.droidkit.R;

public class TabWidget extends RelativeLayout {
    private LocalActivityManager mLocalActivityManager;
    private TabBar mTabBar;
    private LinearLayout mContent;
    private ImageView mShadow;
    private View mChildView;
    private OnTabChangeListener mListener;
    
    private int mCurrentIndex = -1;
    
    private static final int TAB_BAR_ID = 0x0011;
    private static final int CONTENT_ID = 0x0012;
    
    public TabWidget(Context context) {
        super(context);
        init();
    }
    
    public TabWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        setFocusableInTouchMode(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        
        mTabBar = new TabBar(getContext(), this);
        mContent = new LinearLayout(getContext());
        mShadow = new ImageView(getContext());
        
        mTabBar.setId(TAB_BAR_ID);
        mContent.setId(CONTENT_ID);
        mShadow.setImageResource(R.drawable.shadow_gradient);
        
        float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64.0f, getContext().getResources().getDisplayMetrics());
        
        LayoutParams tabBarParams = new LayoutParams(LayoutParams.FILL_PARENT, new Float(height).intValue());
        addView(mTabBar, tabBarParams);
        
        LayoutParams contentParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        contentParams.addRule(BELOW, TAB_BAR_ID);
        addView(mContent, contentParams);
        
        float shadow = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.0f, getContext().getResources().getDisplayMetrics());
        LayoutParams shadowParams = new LayoutParams(LayoutParams.FILL_PARENT, new Float(shadow).intValue());
        shadowParams.addRule(ALIGN_TOP, CONTENT_ID);
        addView(mShadow, shadowParams);
    }
    
    public void setOnTabChangeListener(OnTabChangeListener listener) {
        mListener = listener;
    }
    
    public void setSelectedTab(int index) {
        if (mCurrentIndex == index) {
            return;
        }
        
        TabItem tab = mTabBar.getTabs().get(index);
        tab.setTabSelected(true);
        
        if (tab.getContentType() == TabItem.TYPE_INTENT) {
            loadIntentContent(tab);
            mCurrentIndex = index;
        } else {
            loadViewContent(tab);
            mCurrentIndex = index;
        }
        
        if (mListener != null) {
            mListener.onTabChanged(tab);
        }
    }
    
    private void loadIntentContent(TabItem tab) {
        final Window win = mLocalActivityManager.startActivity(tab.getTag(), tab.getContentIntent());
        final View wd = win != null ? win.getDecorView() : null;
        
        if (mChildView != null && mChildView != wd) {
            if (mChildView.getParent() != null) {
                mContent.removeView(mChildView);
            }
        }
        
        if (mChildView != wd) {
            mChildView = wd;
            mContent.addView(mChildView, new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        }
    }
    
    private void loadViewContent(TabItem tab) {
        View v = tab.getContentView();
        
        if (mChildView != null) {
            if (mChildView.getParent() != null) {
                mContent.removeView(mChildView);
            }
        }
        
        if (mChildView != v) {
            mChildView = v;
            mContent.addView(mChildView, new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        }
    }
    
    public void showShadow(boolean show) {
        if (show) {
            mShadow.setVisibility(VISIBLE);
        } else {
            mShadow.setVisibility(GONE);
        }
    }
    
    public void addTab(TabItem item) {
        mTabBar.addTab(item);
    }
    
    public void setup(LocalActivityManager activityGroup) {
        mLocalActivityManager = activityGroup;
    }
    
    public interface OnTabChangeListener {
        public void onTabChanged(TabItem tab);
    }
}
