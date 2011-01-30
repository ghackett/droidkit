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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Vector;

class TabBar extends LinearLayout {
    private Vector<TabItem> mTabItems;
    private TabWidget mWidget;
    private LayoutParams mItemParams;
    
    public TabBar(Context context, TabWidget widget) {
        super(context);
        mWidget = widget;
        init();
    }
    
    public TabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        mTabItems = new Vector<TabItem>();
        
        float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64.0f, getContext().getResources().getDisplayMetrics());
        float margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -3.0f, getContext().getResources().getDisplayMetrics());
        
        mItemParams = new LayoutParams(0, new Float(height).intValue(), 1.0f);
        mItemParams.leftMargin = new Float(margin).intValue();
        mItemParams.rightMargin = new Float(margin).intValue();
    }
    
    public Vector<TabItem> getTabs() {
        return mTabItems;
    }
    
    public void addTab(TabItem tab) {
        tab.setOnClickListener(mTabListener);
        mTabItems.add(tab);
        addView(tab, mItemParams);
    }
    
    protected View.OnClickListener mTabListener = new View.OnClickListener() {
        public void onClick(View v) {
            TabItem clickedTab = (TabItem) v;
            
            for (TabItem item : mTabItems) {
                if (item == clickedTab) {
                    mWidget.setSelectedTab(mTabItems.indexOf(item));
                } else {
                    item.setTabSelected(false);
                }
            }
        }
    };
}
