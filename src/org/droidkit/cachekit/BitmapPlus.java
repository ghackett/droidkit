package org.droidkit.cachekit;

import android.graphics.Bitmap;


public class BitmapPlus {
	
	private Bitmap mBitmap = null;
	private Object mTag = null;
	
	public BitmapPlus(Bitmap b, Object tag) {
		mBitmap = b;
		mTag = tag;
	}
	
	public BitmapPlus(Bitmap b) {
		this(b, null);
	}
	
	public BitmapPlus() {
		this(null, null);
	}
	
	public Bitmap getBitmap() {
		return mBitmap;
	}
	
	public void setBitmap(Bitmap b) {
		mBitmap = b;
	}
	
	public void recycleBitmap() {
		if (mBitmap != null && !mBitmap.isRecycled())
			mBitmap.recycle();
		mBitmap = null;
	}
	
	public boolean isBitmapRecycled() {
		if (mBitmap == null)
			return true;
		return mBitmap.isRecycled();
	}
	
	public Object getTag() {
		return mTag;
	}
	
	public void setTag(Object tag) {
		mTag = tag;
	}

}
