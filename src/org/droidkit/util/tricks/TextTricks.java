package org.droidkit.util.tricks;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

public class TextTricks {
    public static String joinWithPrefix(String prefix, String[] list, String separator) {
        StringBuffer join = new StringBuffer();
        int count = list.length;
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                // nothing
            }
            else {
                join.append(separator);
            }
            join.append(prefix);
            join.append(list[i]);
        }
        return join.toString();
    }

    // ["a", "b", "c"] => "a, b and c"
    public static String listToParagraph(List<String> list) {
        return listToParagraph(list, ", ", " and ");
    }

    public static String listToParagraph(List<String> list, String comma, String and) {
        StringBuffer para = new StringBuffer();
        int count = list.size();
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                // nothing
            }
            else if (i == count - 1) {
                para.append(and);
            }
            else {
                para.append(comma);
            }
            para.append(list.get(i));
        }
        return para.toString();
    }
    
    public static MessageDigest md5MessageDigest = null;
    public static String md5Hash(String value) {
        if (md5MessageDigest == null) {
            try {
                md5MessageDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                return null;
            }
        }
        
        md5MessageDigest.reset();
        md5MessageDigest.update(value.getBytes(), 0, value.length());

        String hash = new BigInteger(1, md5MessageDigest.digest()).toString(16);
        while (hash.length() < 32)
            hash = "0" + hash;
        return hash;
    }
    
    public static final Pattern REGEXP_PHONE_NUMBER_USA = Pattern.compile("^\\+?1?[- ]?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}$");
    public static final Pattern REGEXP_SIMPLE_EMAIL = Pattern.compile("^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,8}$");

    public static boolean isValidUSAPhoneNumber(String phoneNumber) {
        return REGEXP_PHONE_NUMBER_USA.matcher(phoneNumber).matches();
    }
    
    public static boolean isValidEmail(String email) {
        return REGEXP_SIMPLE_EMAIL.matcher(email).matches();
    }
}
