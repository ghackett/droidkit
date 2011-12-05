package org.droidkit.util.tricks;

import android.net.Uri;

public class ArrayTricks {
	
    public static int findValueInArray(int[] array, int value) {
        for (int i = 0; i<array.length; i++) {
            if (array[i] == value)
                return i;
        }
        return -1;
    }
    
	public static int findValueInArray(Object[] array, Object value) {
		for (int i = 0; i<array.length; i++) {
			if (array[i].equals(value))
				return i;
		}
		return -1;
	}
	
	public static boolean arrayContainsValue(int[] array, int value) {
        return findValueInArray(array, value) != -1;
    }
	
	public static boolean arrayContainsValue(Object[] array, Object value) {
		return findValueInArray(array, value) != -1;
	}
	
	public static String[] copyOf(String[] original, int newLength) {
		if (original == null) {
			return new String[0];
		}
		
		String[] rtr = new String[Math.min(newLength, original.length)];
		for (int i = 0; i<rtr.length; i++) {
			rtr[i] = original[i];
		}
		return rtr;
	}
	
	public static String join(String[] array, String delimiter) {
		if (array == null)
			return null;
		if (delimiter == null)
			delimiter = "";
		StringBuilder b = new StringBuilder();
		for (int i = 0; i< array.length; i++) {
			if (i!=0)
				b.append(delimiter);
			b.append(array[i]);
		}
		return b.toString();
	}
	
	public static String joinAndEncodeEachEntry(String[] array, String delimiter) {
        if (array == null)
            return null;
        if (delimiter == null)
            delimiter = "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i< array.length; i++) {
            if (i!=0)
                b.append(delimiter);
            b.append(Uri.encode(array[i]));
        }
        return b.toString();
	}

}
