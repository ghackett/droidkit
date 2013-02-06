package org.droidkit.util.tricks;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class JsonTricks {
    public static JSONObject getJSONObject(JSONObject json, String... path) {
        return getJSONObject(json, path, 0, path.length - 1);
    }
    
    protected static JSONObject getJSONObject(JSONObject json, String[] path, int start, int length) {
        if (start >= length)
            return json;
       
        while (start < length) {
            if (json == null)
                break;
            else
                json = json.optJSONObject(path[start]);
            start++;
        }
        return json;
    }

    public static String getString(JSONObject json, String... path) {
        json = getJSONObject(json, path, 0, path.length - 1);
        if (json != null) {
            return json.optString(path[path.length - 1]);
        }
        return null;
    }

    public static JSONObject fromMap(Map<String, ?> map) {
        JSONObject o = new JSONObject();

        if (map == null) {
            return o;
        }

        try {
            for (String key : map.keySet()) {
                o.put(key, map.get(key));
            }
        } catch (JSONException e) {
            CLog.e("Failed to convert map, ignoring.");
        }

        return o;
    }
    
}
