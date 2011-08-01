package org.droidkit.util.tricks;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class StorageTricks {
	
	private static final String EMMC_DIR_1 = "/emmc";
	private static final String EMMC_DIR_2 = "/mnt/emmc";
	private static final String NO_MEDIA_FILE_NAME = ".nomedia";
	
	
	public static String getEmmcDirectory() {
		File f = new File(EMMC_DIR_1);
		if (f.exists() && f.isDirectory() && f.canWrite())
			return EMMC_DIR_1;
		
		f = new File(EMMC_DIR_2);
		
		if (f.exists() && f.isDirectory() && f.canWrite())
			return EMMC_DIR_2;
		
		return null;
		
	}
	
	public static boolean doesDeviceHaveEmmcDirectory() {
		return getEmmcDirectory() != null;
	}
	
	public static File findWritableDirectoryWithMostFreeSpace(Context c, boolean includeInternalMemory) {
		long mostUsableSpace = 0;
		String rtr = null;
		
		StatFs stats = null;
		
		File f = Environment.getExternalStorageDirectory();
		if (f.exists() && f.canWrite()) {
			stats = new StatFs(f.getAbsolutePath());
			long space = (long)stats.getBlockSize() * (long)stats.getBlockCount();
			if (space > mostUsableSpace) {
				mostUsableSpace = space;
				rtr = f.getAbsolutePath();
			}
		}
		
		f = new File(EMMC_DIR_1);
		if (f.exists() && f.canWrite()) {
			stats = new StatFs(f.getAbsolutePath());
			long space = (long)stats.getBlockSize() * (long)stats.getBlockCount();
			if (space > mostUsableSpace) {
				mostUsableSpace = space;
				rtr = f.getAbsolutePath();
			}
		}

		f = new File(EMMC_DIR_2);
		if (f.exists() && f.canWrite()) {
			stats = new StatFs(f.getAbsolutePath());
			long space = (long)stats.getBlockSize() * (long)stats.getBlockCount();
			if (space > mostUsableSpace) {
				mostUsableSpace = space;
				rtr = f.getAbsolutePath();
			}
		}
		
		if (includeInternalMemory) {
			f = c.getFilesDir();
			if (f.exists() && f.canWrite()) {
				stats = new StatFs(f.getAbsolutePath());
				long space = (long)stats.getBlockSize() * (long)stats.getBlockCount();
				if (space > mostUsableSpace) {
					mostUsableSpace = space;
					rtr = f.getAbsolutePath();
				}
			}
		}

		if (rtr != null)
		    return new File(rtr);
		return null;
		
	}
	
	public static void addNoMediaFileToDirectory(File dir) {
	    try {
    	    if (dir.exists() && dir.canWrite()) {
    	        File noMedia = new File(dir, NO_MEDIA_FILE_NAME);
    	        if (!noMedia.exists())
    	            noMedia.createNewFile();
    	    }
	    } catch (Throwable t) {
	        t.printStackTrace();
	    }
	}
	
}
