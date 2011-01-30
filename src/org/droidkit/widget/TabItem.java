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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.droidkit.R;

public class TabItem extends RelativeLayout {
    public static final int TYPE_INTENT = 0x1;
    public static final int TYPE_VIEW = 0x2;
    
    ImageView mIcon;
    TextView mLabel;
    Intent mContentIntent;
    View mContentView;
    
    String mTag;
    
    int mSelectedTextColor = 0x242424;
    int mUnselectedTextColor = 0xFFFFFF;
    int mBackgroundId = R.drawable.default_tab_bg;
    
    public TabItem(Context context) {
        super(context);
        init();
    }
    
    public TabItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        mIcon = new ImageView(getContext());
        mLabel = new TextView(getContext());
        mLabel.setTextColor(mUnselectedTextColor);
        
        LayoutParams iconParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        iconParams.addRule(CENTER_HORIZONTAL, TRUE);
        
        mIcon.setPadding(0, 10, 0, 0);
        addView(mIcon, iconParams);
        
        LayoutParams labelParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelParams.addRule(CENTER_HORIZONTAL, TRUE);
        labelParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        
        setBackgroundResource(mBackgroundId);
        
        mLabel.setPadding(0, 0, 0, 6);
        addView(mLabel, labelParams);
    }
    
    public void setTag(String tag) {
        mTag = tag;
    }
    
    public String getTag() {
        return mTag;
    }
    
    public void setTypeface(Typeface tf) {
        mLabel.setTypeface(tf);
    }
    
    public void setText(String text) {
        if (mTag == null) mTag = text;
        mLabel.setText(text);
    }
    
    public void setSelectedTextColor(String color) {
        mSelectedTextColor = Color.parseColor(color);
    }
    
    public void setSelectedTextColor(int color) {
        mSelectedTextColor = color;
    }
    
    public void setUnselectedTextColor(String color) {
        mUnselectedTextColor = Color.parseColor(color);
    }
    
    public void setUnselectedTextColor(int color) {
        mUnselectedTextColor = color;
    }
    
    public void setIcon(int id) {
        mIcon.setImageResource(id);
    }
    
    public void setIcon(Drawable img) {
        mIcon.setImageDrawable(img);
    }
    
    public void setBackground(int id) {
        mBackgroundId = id;
        setBackgroundResource(mBackgroundId);
    }
    
    public void setTabSelected(boolean selected) {
        if (selected) {
            setSelected(true);
            mLabel.setTextColor(mSelectedTextColor);
        } else {
            setSelected(false);
            mLabel.setTextColor(mUnselectedTextColor);
        }
    }
    
    public void setContent(Intent intent) {
        mContentIntent = intent;
    }
    
    public void setContent(View view) {
        mContentView = view;
    }
    
    public void setContent(int resId) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContent(inflater.inflate(resId, null, false));
    }
    
    public View getContentView() {
        return mContentView;
    }
    
    public Intent getContentIntent() {
        return mContentIntent;
    }
    
    public int getContentType() {
        if (mContentIntent == null && mContentView == null) {
            return -1;
        }
        
        if (mContentIntent == null && mContentView != null) {
            return TYPE_VIEW;
        } else {
            return TYPE_INTENT;
        }
    }
}
