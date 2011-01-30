/*
 * Copyright (C) 2010-2011 Mike Novak <michael.novakjr@gmail.com>
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
package org.droidkit.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.droidkit.R;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
    private SeekBar mSeekbar;
    private TextView mSeekbarLabel;
    private TextView mSeekbarValue;
    
    private int mDefault;
    private int mMaxValue;
    
    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.droidkit);
        mDefault = arr.getInteger(R.styleable.droidkit_defaultValue, 0);
        mMaxValue = arr.getInteger(R.styleable.droidkit_maxValue, 100);
        
        arr.recycle();
        
        setDialogLayoutResource(R.layout.seekbar_pref);
    }
    
    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }
    
    public SeekBarPreference(Context context) {
        this(context, null);
    }
    
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mSeekbar = (SeekBar) view.findViewById(R.id.pref_seekbar);
        mSeekbar.setMax(mMaxValue);
        mSeekbar.setProgress(getValue());
        mSeekbar.setOnSeekBarChangeListener(this);
        
        mSeekbarLabel = (TextView) view.findViewById(R.id.seekvalue_label);
        mSeekbarLabel.setText(getTitle() + ": ");
        
        mSeekbarValue = (TextView) view.findViewById(R.id.pref_seekvalue);
        mSeekbarValue.setText(String.valueOf(getValue()));
    }
    
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
            saveValue(mSeekbar.getProgress());
            break;
        default:
            break;
        }
    }
    
    private void saveValue(int val) {
        getEditor().putInt(getKey(), val).commit();
        notifyChanged();
    }
    
    private int getValue() {
        return getSharedPreferences().getInt(getKey(), mDefault);
    }
    
    /* SeekBar listener methods. */
    
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mSeekbarValue.setText(String.valueOf(progress));
    }
    
    public void onStartTrackingTouch(SeekBar seekBar) {
        /* thanks Java. */
    }
    
    public void onStopTrackingTouch(SeekBar seekBar) {
        /* thanks Java. */
    }
}
