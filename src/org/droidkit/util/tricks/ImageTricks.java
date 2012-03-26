package org.droidkit.util.tricks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.droidkit.DroidKit;
import org.droidkit.image.ImageRequest;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.widget.ImageView;


//@SuppressWarnings("unused")
public class ImageTricks {
    public static final String CAMERA_TEMP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.tmp";
    public static final String CAMERA_TEMP_FILE_NAME = "camera.jpg";
    
    protected static Handler uiThreadHandler = null;
    
    public static class AsyncImageRequest extends AsyncTricks.AsyncRequest {
        protected ImageRequest imageRequest;
        protected ImageView imageView;
        protected int defaultImageResource;
        protected Bitmap bitmap = null;
        
        protected static HashMap<String, String> assignedRequests = new HashMap<String, String>();
        
        public AsyncImageRequest(ImageRequest request, ImageView view, int defaultResource) {
            super(AsyncTricks.INTERACTIVE);
            
            imageRequest = request;
            imageView = view;
            defaultImageResource = defaultResource;
            
            if (uiThreadHandler == null)
                uiThreadHandler = new Handler();
            
            synchronized (assignedRequests) {
                String viewName = Integer.toString(view.hashCode());
                String requestName = imageRequest.name();
                assignedRequests.put(viewName, requestName);
            }
        }
        
        public AsyncImageRequest(ImageRequest request, ImageView view) {
            this(request, view, 0);
        }
        
        public AsyncImageRequest(String url, ImageView view, int defaultResource) {
            this(new ImageRequest().load(url).cache(), view, defaultResource);
        }

        public AsyncImageRequest(String url, ImageView view) {
            this(new ImageRequest().load(url).cache(), view, 0);
        }
        
        public String label() {
            return "loading image " + imageRequest.name() + " for view " + imageView.hashCode();
        }
        
        Handler handler() {
            return uiThreadHandler;
        }
        
        public void displayImage(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
        
        public void displayPlaceholder() {
            if (defaultImageResource != 0)
                imageView.setImageResource(defaultImageResource);
            else
                imageView.setImageBitmap(null);
        }
        
        public void displayFallback() {
            displayPlaceholder();
        }

        @Override
        public boolean before() {
            if (imageRequest.isInMemory()) {
                bitmap = imageRequest.getBitmap();
                if (bitmap != null)
                    displayImage(bitmap);
                else 
                    displayFallback();
                return false;
            }
            else {
                displayPlaceholder();
    
                return true;
            }
        }

        @Override
        public void request() {
            bitmap = imageRequest.getBitmap();
        }

        public boolean markAsCompletedAndSeeIfStillMatches() {
            boolean stillMatchesRequest = false;
            synchronized (assignedRequests) {
                String viewName = Integer.toString(imageView.hashCode());
                String requestName = imageRequest.name();
                String assignedName = assignedRequests.remove(viewName);
                stillMatchesRequest = (assignedName != null && assignedName.equals(requestName));
            }
            return stillMatchesRequest;
        }
        
        @Override
        public void interrupted() {
            if (markAsCompletedAndSeeIfStillMatches()) {
                displayFallback();
            }
        }

        @Override
        public void after() {
            if (markAsCompletedAndSeeIfStillMatches()) {
                if (bitmap != null)
                    displayImage(bitmap);
                else
                    displayFallback();
            }
        }

        public void queue() {
            AsyncTricks.queueRequest(AsyncTricks.INTERACTIVE, this);
        }
    }
    
    
    public static Bitmap scaleDownBitmap(Bitmap original, int maxDimension, boolean recycleOriginal) {
    	int origWidth = original.getWidth();
    	int origHeight = original.getHeight();
    	
    	if (origWidth <= maxDimension && origHeight <= maxDimension) {
    		Bitmap b = Bitmap.createBitmap(original);
    		if (recycleOriginal && (original != b))
    			original.recycle();
    		return b;
    	}
    	
    	int newWidth = 0;
    	int newHeight = 0;
    	
    	float ratio = (float)origHeight / (float)origWidth;
    	
    	if (origWidth > origHeight) {
    		newWidth = maxDimension;
    		newHeight = (int)((float)newWidth * ratio);
    	} else {
    		newHeight = maxDimension;
    		newWidth = (int)((float)newHeight / ratio);
    	}
    	
    	Bitmap rtr = Bitmap.createScaledBitmap(original, newWidth, newHeight, false);
    	if (recycleOriginal && original != rtr)
    		original.recycle();
    	return rtr;
    }
    
    public static void scaleDownImageFile(File originalImageFile, int maxDimension, CompressFormat format, int quality) {
    	Bitmap b = BitmapFactory.decodeFile(originalImageFile.getAbsolutePath());
    	if (b == null)
    		throw new RuntimeException("Original image could not be decoded.");
    	
    	try {
	    	b = scaleDownBitmap(b, maxDimension, true);
	    	originalImageFile.delete();
	    	originalImageFile.createNewFile();
	    	BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(originalImageFile));
	    	b.compress(format, quality, outputStream);
	    	outputStream.close();
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    public static Bitmap scaleDownContactPhoto(Uri contactUri, int maxDimension) {
    	try {
    		InputStream mediaStream = Contacts.openContactPhotoInputStream(DroidKit.getContentResolver(), contactUri);
    		if (mediaStream == null)
    			return null;
    		BitmapFactory.Options opts = new BitmapFactory.Options();
    		opts.inJustDecodeBounds = true;
        	BitmapFactory.decodeStream(mediaStream, null, opts);
        	mediaStream.close();
            int outWidth = opts.outWidth;
        	
            mediaStream = Contacts.openContactPhotoInputStream(DroidKit.getContentResolver(), contactUri);

        	opts = new BitmapFactory.Options();
        	opts.inSampleSize = outWidth / maxDimension;
        	
            Bitmap bitmap = BitmapFactory.decodeStream(mediaStream, null, opts);
            mediaStream.close();
            return bitmap;
    	} catch (Throwable t) {
    		t.printStackTrace();
    	}
        	
    	return null;
    }
    
    public static Bitmap scaleDownImageUriToBitmap(Uri imageUri, int maxDimension, boolean deleteOriginal) {
    	try {
    		InputStream mediaStream = DroidKit.getContentResolver().openInputStream(imageUri);
    		
    		BitmapFactory.Options opts = new BitmapFactory.Options();
    		opts.inJustDecodeBounds = true;
        	Bitmap b = BitmapFactory.decodeStream(mediaStream, null, opts);
        	if (b != null)
        	    b.recycle();
        	mediaStream.close();
        	mediaStream = null;
        	int maxSideSize = Math.max(opts.outWidth, opts.outHeight);
        	
        	mediaStream = DroidKit.getContentResolver().openInputStream(imageUri);

        	opts = new BitmapFactory.Options();
        	opts.inSampleSize = maxSideSize / maxDimension;
        	
            Bitmap bitmap = BitmapFactory.decodeStream(mediaStream, null, opts);
        	
        	mediaStream.close();
        	mediaStream = null;
        	
        	if (deleteOriginal)
        		DroidKit.getContentResolver().delete(imageUri, null, null);
        	return bitmap;
        	
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public static File scaleDownImageUriToFile(Uri imageUri, int maxDimension, CompressFormat format, int quality, boolean deleteOriginal) {
    	if (!ImageTricks.checkTempCameraDir())
    		return null;

    	Bitmap b = scaleDownImageUriToBitmap(imageUri, maxDimension, deleteOriginal);
    	if (b == null) 
    		return null;
    	
    	try {
        	
        	File tmpFile = new File(ImageTricks.CAMERA_TEMP_DIR, "scaledImage." + (format == CompressFormat.JPEG ? "jpg" : "png"));
        	tmpFile.createNewFile();
        	BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tmpFile));
        	b.compress(format, quality, outputStream);
        	
        	outputStream.close();
        	b.recycle();
        	
        	
        	return tmpFile;
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    	return null;
    	
    }
    
    public static Uri scaleDownImageUri(Uri imageUri, int maxDimension, CompressFormat format, int quality, boolean deleteOriginal) {
    	try {
	    	File tmpFile = scaleDownImageUriToFile(imageUri, maxDimension, format, quality, deleteOriginal);
	    	
	    	if (tmpFile == null)
	    		return null;
	    	
	    	Uri rtr = Uri.parse(MediaStore.Images.Media.insertImage(DroidKit.getContentResolver(), tmpFile.getAbsolutePath(), null, null));
	    	tmpFile.delete();
	    	return rtr;
    	} catch (Throwable e) {
    		throw new RuntimeException(e);
    	}
    }

    public static boolean checkTempCameraDir() {
        File dir = new File(CAMERA_TEMP_DIR);
        if (!dir.exists()) {
            try {
                if (!dir.mkdirs())
                    return false;
            } 
            catch (Throwable e) {
                return false;
            }
        }
        
        if (!dir.canWrite())
            return false;
        
        File noMedia = new File(dir, ".nomedia");
        try {
            noMedia.createNewFile();
        } 
        catch (Throwable e) {
            return false;
        }
        return true;
    }

//    public static File _tempCameraFile = null;
    
//    public static File tempCameraFile() {
//        if (_tempCameraFile == null)
//            _tempCameraFile = new File(ImageTricks.CAMERA_TEMP_DIR, ImageTricks.CAMERA_TEMP_FILE_NAME);
//        return _tempCameraFile;
//    }

    public static Uri putImageFileIntoGalleryAndGetUri(Context c, File imageFile, boolean deleteImageFileAfter) {
        if (imageFile.exists() && imageFile.isFile()) {
            try {
                Uri dataUri = Uri.parse(MediaStore.Images.Media.insertImage(c.getContentResolver(), imageFile.getAbsolutePath(), null, null));
                if (dataUri != null) {
                	ContentValues cv = new ContentValues();
                	cv.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
                	cv.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
                	c.getContentResolver().update(dataUri, cv, null, null);
                }
                if (deleteImageFileAfter)
                    imageFile.delete();
                return dataUri;
            } 
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            
        } else {
            Log.e("ImageTricks", "imageFile did not exists or was not a file: " + imageFile.getAbsolutePath());
        }
        return null;
    }
    
    public static Uri putBitmapIntoGalleryAndGetUri(Context c, Bitmap image, boolean recycleOriginal) {
        if (image != null) {
            Uri dataUri = Uri.parse(MediaStore.Images.Media.insertImage(c.getContentResolver(), image, null, null));
            if (dataUri != null) {
            	ContentValues cv = new ContentValues();
            	cv.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            	cv.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            	c.getContentResolver().update(dataUri, cv, null, null);
            }
            if (recycleOriginal)
                image.recycle();
            return dataUri; 
        }
        return null;
    }
    
    public static Bitmap roundCorners(int imgResId, int widthDp, int heightDp, int cornerDp, int strokeColor) {
        try {
            return roundCorners(DroidKit.getBitmap(imgResId, widthDp, heightDp), cornerDp, true, strokeColor);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Bitmap roundCorners(int imgResId, int widthDp, int heightDp, int cornerDp) {
        try {
            return roundCorners(DroidKit.getBitmap(imgResId, widthDp, heightDp), cornerDp, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Bitmap roundCorners(int imgResId, int dp) {
        try {
            return roundCorners(DroidKit.getBitmap(imgResId), dp, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Bitmap roundCorners(String file, int widthDp, int heightDp, int cornerDp, int strokeColor) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.outWidth = DroidKit.getPixels(widthDp);
            opts.outHeight = DroidKit.getPixels(heightDp);
            Bitmap b = BitmapFactory.decodeFile(file, opts);
            return roundCorners(b, cornerDp, true, strokeColor);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Bitmap roundCorners(String file, int widthDp, int heightDp, int cornerDp) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.outWidth = DroidKit.getPixels(widthDp);
            opts.outHeight = DroidKit.getPixels(heightDp);
            Bitmap b = BitmapFactory.decodeFile(file, opts);
            return roundCorners(b, cornerDp, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Bitmap roundCorners(String file, int dp, int strokeColor) {
        try {
            Bitmap b = BitmapFactory.decodeFile(file);
            return roundCorners(b, dp, true, strokeColor);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Bitmap roundCorners(String file, int dp) {
        try {
            Bitmap b = BitmapFactory.decodeFile(file);
            return roundCorners(b, dp, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Bitmap roundCorners(Bitmap bitmap, int widthDp, int heightDp, int cornerDp, boolean recycleOriginal, int strokeColor) {
        try {
            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, DroidKit.getPixels(widthDp), DroidKit.getPixels(heightDp), false);
            if (recycleOriginal)
                bitmap.recycle();
            return roundCorners(newBitmap, cornerDp, true, strokeColor);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Bitmap roundCorners(Bitmap bitmap, int widthDp, int heightDp, int cornerDp, boolean recycleOriginal) {
        try {
            Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, DroidKit.getPixels(widthDp), DroidKit.getPixels(heightDp), false);
            if (recycleOriginal)
                bitmap.recycle();
            return roundCorners(newBitmap, cornerDp, true);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
        
    public static Bitmap roundCorners(Bitmap bitmap, int dp, boolean recycleOriginal) {
        return roundCorners(bitmap, dp, recycleOriginal, false, 0);
    }
    
    public static Bitmap roundCorners(Bitmap bitmap, int dp, boolean recycleOriginal, int strokeColor) {
        return roundCorners(bitmap, dp, recycleOriginal, true, strokeColor);
    }
    
    private static Bitmap roundCorners(Bitmap bitmap, int dp, boolean recycleOriginal, boolean drawStroke, int strokeColor) {
        Bitmap output = null;
        try {
            Bitmap image = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Bitmap border = null;
            int borderWidth = 2; // DroidKit.getPixels(2);
            Rect imageSrcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//            Rect imageDestRect = new Rect(borderWidth, borderWidth, bitmap.getWidth()-borderWidth, bitmap.getHeight()-borderWidth);
            
            
            final float roundPx = DroidKit.getPixels(dp);
            final int color = 0xff424242;
            final Paint paint = new Paint();
            Canvas imageCanvas = new Canvas(image);
            paint.setAntiAlias(true);
            imageCanvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            imageCanvas.drawRoundRect(new RectF(imageSrcRect), roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            imageCanvas.drawBitmap(bitmap, imageSrcRect, imageSrcRect, paint);
            
            if (drawStroke) {
                RectF borderRect = new RectF(0, 0, bitmap.getWidth()+(borderWidth*2), bitmap.getHeight()+(borderWidth*2));
                border = Bitmap.createBitmap(bitmap.getWidth()+(borderWidth*2), bitmap.getHeight()+(borderWidth*2), Config.ARGB_8888);
                Canvas finalCanvas = new Canvas(border);
                final Paint strokePaint = new Paint();
                strokePaint.setAntiAlias(true);
                strokePaint.setColor(strokeColor);
                strokePaint.setStrokeWidth(borderWidth);
                finalCanvas.drawRoundRect(borderRect, roundPx, roundPx, strokePaint);
                
                Rect innerBitmapRect = new Rect(borderWidth, borderWidth, bitmap.getWidth()+borderWidth, bitmap.getHeight()+borderWidth);
                finalCanvas.drawBitmap(image, imageSrcRect, innerBitmapRect, null);
                
                image.recycle();
                image = null;
                
                output = border;
            } else {
                output = image;
            }

            
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        
        if (output != null && recycleOriginal) {
            bitmap.recycle();
        }
        
        return output;
    }
    
    public static Bitmap rotateBitmapRight(Bitmap src, boolean recycleOriginal) {
        if (src == null || src.isRecycled())
            return null;
        
        
        System.gc();
        Matrix m = new Matrix();
        
        
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        
        m.setRotate(90, srcWidth/2, srcHeight/2);
        Bitmap result = Bitmap.createBitmap(src, 0, 0, srcWidth, srcHeight, m, false);

        
        if (recycleOriginal) {
            src.recycle();
            System.gc();
        }
        return result;
    }
}
