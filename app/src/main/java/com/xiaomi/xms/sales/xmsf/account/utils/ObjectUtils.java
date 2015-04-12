package com.xiaomi.xms.sales.xmsf.account.utils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectUtils {

    /**
     * Convert map to NameValuePair list for performing http request.
     *
     * @param map map object
     * @return NameValuePair list, or null if map is null
     */
    public static List<NameValuePair> mapToPairs(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        Set<Map.Entry<String, String>> entries = map.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            BasicNameValuePair pair = new BasicNameValuePair(key,
                    value != null ? value : "");
            pairs.add(pair);
        }
        return pairs;
    }

    /**
     * <p>Flatten json object into Map. the data type in the json object will be
     * retained. The parameter type of the returned map is Object, so it can
     * contain arbitrary data types. The four basic json types corresponds to
     * primitive type or String or null in Java, eg.: <ul><li>json number => int
     * (or long)</li><li>json bool (true, false) => boolean</li><li>json string
     * => String </li><li>json null => null</li></ul> JSON array will be
     * converted to List, and JSON Object will recursively be converted to
     * Object, eg.: <ul><li>json array => List</li><li>json object =>
     * Object</li></ul>Here are some examples:</p> <ul><li>{"name":"Lin"} =>
     * map.put("name", "Lin")</li> <li> {"name", {"first":"Jun", "last":"Lin"}}
     * => nameMap.put("first", "Jun"); nameMap.put("last", "Lin");
     * map.put("name", nameMap); </li> </ul>
     *
     * @param jsonObj json object to be converted
     * @return map object or null if jsonObj is null
     */
    public static Map<String, Object> jsonToMap(JSONObject jsonObj) {
        if (jsonObj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator iter = jsonObj.keys();
        while (iter.hasNext()) {
            final String key = (String) iter.next();
            final Object value = jsonObj.opt(key);
            map.put(key, convertObj(value));
        }
        return map;
    }

    public static Map<String, String> listToMap(
            Map<String, List<String>> listMap) {
        Map<String, String> map = new HashMap<String, String>();
        if (listMap != null) {
            Set<Map.Entry<String, List<String>>> entries = listMap.entrySet();
            for (Map.Entry<String, List<String>> entry : entries) {
                final String key = entry.getKey();
                final List<String> valueList = entry.getValue();
                if (key != null && valueList != null && valueList.size() > 0) {
                    map.put(key, valueList.get(0));
                }
            }
        }
        return map;
    }

    /**
     * Helper method to convert a map to string in the form {"key1", "value1",
     * "key2", "value2", ...}
     *
     * @param map object
     * @return a string represents the map content, or a literal string "null"
     *         if the map is null
     */
    public static String flattenMap(Map<?, ?> map) {
        if (map == null) {
            return "null";
        }
        Set<? extends Map.Entry<?, ?>> entries = map.entrySet();
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Map.Entry<?, ?> entry : entries) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            sb.append("(");
            sb.append(key);
            sb.append(",");
            sb.append(value);
            sb.append("),");
        }

        sb.append("}");
        return sb.toString();
    }

    private static Object convertObj(Object obj) {
        if (obj instanceof JSONObject) {
            return jsonToMap((JSONObject) obj);
        } else if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray) obj;
            final int size = array.length();
            List<Object> list = new ArrayList<Object>();
            for (int i = 0; i < size; i++) {
                list.add(convertObj(array.opt(i)));
            }
            return list;
        } else if (obj == JSONObject.NULL) {
            return null;
        }
        return obj;
    }
}
