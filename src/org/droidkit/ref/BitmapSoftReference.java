package org.droidkit.ref;

import java.lang.ref.SoftReference;

import android.graphics.Bitmap;

public class BitmapSoftReference extends SoftReference<Bitmap> {
	@SuppressWarnings("unused")
	private static final String TAG = "BitmapReference";

	public BitmapSoftReference(Bitmap referent) {
		super(referent);
	}

	@Override
	public void clear() {
		
		Bitmap b = get();
		if (b != null && !b.isRecycled()) {
			b.recycle();
		}
		super.clear();
	}

    @Override
    protected void finalize() throws Throwable {
        Bitmap b = get();
        if (b != null && !b.isRecycled())
            b.recycle();
        super.finalize();
    }
	

}