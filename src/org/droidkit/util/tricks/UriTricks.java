package org.droidkit.util.tricks;

import android.net.Uri;

public class UriTricks {
    
    public static String stripQueryAndFragments(Uri baseUri) {
        String uriStr = baseUri.toString();
        int index = uriStr.indexOf("?");
        if (index >= 0)
            uriStr = uriStr.substring(0, index);
        else {
            index = uriStr.indexOf("#");
            if (index >= 0)
                uriStr = uriStr.substring(0, index);
        }
        return uriStr;
    }
    
    public static boolean doStrippedUrisMatch(Uri a, Uri b) {
    	if (a == null || b == null)
    		return false;
    	return stripQueryAndFragments(a).equals(stripQueryAndFragments(b));
    }

}
