package org.droidkit.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;


public class FullBleedImageView extends HandyImageView {
	
	private Rect mSrcRect = null, mDestRect = null;

	public FullBleedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initFullBleedImageView();
	}

	public FullBleedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initFullBleedImageView();
	}

	public FullBleedImageView(Context context) {
		super(context);
		initFullBleedImageView();
	}
	
	private void initFullBleedImageView() {
		setScaleType(ScaleType.CENTER_CROP);
	}
	
	   @Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			super.onLayout(changed, left, top, right, bottom);
			if (changed) {
				mSrcRect = null;
				mDestRect = null;
				postInvalidate();
			}
		}

		@Override
		public void setImageDrawable(Drawable drawable) {
			super.setImageDrawable(drawable);
			mSrcRect = null;
			postInvalidate();
		}

		private Rect getSourceRect(Bitmap b) {
	    	if (mSrcRect != null)
	    		return mSrcRect;
	    	
	    	final int vWidth = getWidth();
	    	if (vWidth > 0) {
				final int vHeight = getHeight();
				final int bWidth = b.getWidth();
				final int bHeight = b.getHeight();
				final float vWidthOverHeight = (float)vWidth/(float)vHeight;
				int srcWidth = (int)((float)bHeight * vWidthOverHeight);
				int srcHeight = 0;
				if (srcWidth <= bWidth) {
					srcHeight = bHeight;
				} else {
					final float vHeightOverWidth = (float)vHeight/(float)vWidth;
					srcHeight = (int)((float)bWidth * vHeightOverWidth);
					srcWidth = bWidth;
				}
				final int srcLeft = (bWidth - srcWidth)/2;
				final int srcRight = srcLeft + srcWidth;
				final int srcTop = (bHeight - srcHeight)/2;
				final int srcBottom = srcTop + srcHeight;
				mSrcRect = new Rect(srcLeft, srcTop, srcRight, srcBottom);
	    	}
	    	return mSrcRect;
	    }
		
		private Rect getDestRect() {
			if (mDestRect != null)
				return mDestRect;
			final int width = getWidth();
			if (width > 0) {
				final int height = getHeight();
				mDestRect = new Rect(0, 0, width, height);
			}
			return mDestRect;
		}

		@Override
		protected void onDraw(Canvas canvas) {
	    	final Drawable d = getDrawable();
	    	if (d instanceof BitmapDrawable) {
	    		Bitmap b = ((BitmapDrawable) d).getBitmap();
	    		if (b != null && !b.isRecycled()) {
		    		final Rect srcRect = getSourceRect(b);
		    		if (srcRect != null) {
		    			final Rect destRect = getDestRect();
		    			if (destRect != null) {
		    				canvas.drawBitmap(b, srcRect, destRect, null);
		    				return;
		    			}
		    		}
	    		}
	    	}
	    	
			super.onDraw(canvas);
		}

		

}
