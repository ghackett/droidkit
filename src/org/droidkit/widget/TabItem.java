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
import android.util.Log;
import android.util.TypedValue;
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
    
    static int sSelectedTextColor = Color.parseColor("#242424");
    static int sUnselectedTextColor = Color.parseColor("#FFFFFF");
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
                
        LayoutParams iconParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        iconParams.addRule(CENTER_HORIZONTAL, TRUE);
        
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.f, getContext().getResources().getDisplayMetrics());
        
        mIcon.setPadding(0, new Float(padding).intValue(), 0, 0);
        addView(mIcon, iconParams);
        
        LayoutParams labelParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelParams.addRule(CENTER_HORIZONTAL, TRUE);
        labelParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        
        setBackgroundResource(mBackgroundId);
        
        padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6.0f, getContext().getResources().getDisplayMetrics());
        
        mLabel.setPadding(0, 0, 0, new Float(padding).intValue());
        mLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f);
        mLabel.setTextColor(sUnselectedTextColor);
        
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
        sSelectedTextColor = Color.parseColor(color);
    }
    
    public void setSelectedTextColor(int color) {
        sSelectedTextColor = color;
    }
    
    public void setUnselectedTextColor(String color) {
        sUnselectedTextColor = Color.parseColor(color);
    }
    
    public void setUnselectedTextColor(int color) {
        sUnselectedTextColor = color;
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
            mLabel.setTextColor(sSelectedTextColor);
            setSelected(true);
        } else {
            mLabel.setTextColor(sUnselectedTextColor);
            setSelected(false);
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
