package org.droidkit.ref;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;

public class BitmapWeakReference extends WeakReference<Bitmap> {
	@SuppressWarnings("unused")
	private static final String TAG = "BitmapReference";

	public BitmapWeakReference(Bitmap referent) {
		super(referent);
	}

	@Override
	public void clear() {
		
		Bitmap b = get();
		if (b != null) {
			b.recycle();
		}
		super.clear();
	}
}
