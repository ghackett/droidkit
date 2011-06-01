package org.droidkit.net.ezhttp;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.droidkit.DroidKit;

import android.net.Uri;

public class EzHttpUriPostUploadEntity extends AbstractEzHttpPostUploadEntity {
	
	private Uri mUri;

	public EzHttpUriPostUploadEntity(Uri uri, String paramName, String postFileName, String contentType) {
		super(paramName, postFileName, contentType);
		mUri = uri;
	}

	@Override
	public InputStream getInputStream() {
		try {
			return DroidKit.getContentResolver().openInputStream(mUri);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public long getSize() {
		try {
			InputStream in = getInputStream();
			long avail = in.available();
			in.close();
			return avail;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

}
