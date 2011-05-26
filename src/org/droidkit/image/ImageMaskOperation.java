package org.droidkit.image;

import org.droidkit.DroidKit;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

public class ImageMaskOperation extends ImageRequest.Operation {
    protected static final String TAG = "DroidKit";

    int resourceId = 0;
    int top = 0;
    int left = 0;
    
    static Paint xferModePaint = null;

    public ImageMaskOperation(ImageRequest request, int resourceId, int top, int left) {
        super(request);
        this.resourceId = resourceId;
        this.top = top;
        this.left = left;

        if (xferModePaint == null) {
            xferModePaint = new Paint();
            xferModePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }
    }

    @Override
    public Bitmap perform(Bitmap previousBitmap) {
        if (previousBitmap == null)
            return null;
        
        Bitmap maskBitmap = DroidKit.getBitmap(resourceId);

        synchronized (maskBitmap) {
            if (maskBitmap != null) {
                int w = previousBitmap.getWidth();
                int h = previousBitmap.getHeight();
    
                Bitmap combinedBitmap = null;
                try {
                    combinedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                } 
                catch (OutOfMemoryError e) {
                    Log.e(TAG, "Error overlying image: ", e);
                }
    
                if (combinedBitmap != null) {
                    Canvas canvas = new Canvas(combinedBitmap);
    
                    canvas.drawBitmap(previousBitmap, 0, 0, null);
                    
                    canvas.drawBitmap(maskBitmap, left, top, xferModePaint);
    
                    return combinedBitmap;
                }
            }
        }
        
        return previousBitmap;
    }

    @Override
    public String name(String previousName) {
        return "overlay_" + resourceId + "-" + previousName;
    }
}