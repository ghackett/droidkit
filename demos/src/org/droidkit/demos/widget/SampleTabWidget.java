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
package org.droidkit.demos.widget;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;

import org.droidkit.widget.TabItem;
import org.droidkit.widget.TabWidget;

import org.droidkit.demos.R;

public class SampleTabWidget extends ActivityGroup {
    TabWidget mTabWidget;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabwidget);
        
        mTabWidget = (TabWidget) findViewById(R.id.tab_widget);
        mTabWidget.setup(getLocalActivityManager());
        
        TabItem item = new TabItem(this);
        item.setText("Activity One");
        item.setContent(new Intent(this, TabActivityOne.class));
        
        TabItem tabTwo = new TabItem(this);
        tabTwo.setText("Activity Two");
        tabTwo.setContent(new Intent(this, TabActivityTwo.class));
        
        mTabWidget.addTab(item);
        mTabWidget.addTab(tabTwo);
        
        mTabWidget.setSelectedTab(0);
    }
}
