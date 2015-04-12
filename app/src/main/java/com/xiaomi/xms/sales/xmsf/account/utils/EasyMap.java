package com.xiaomi.xms.sales.xmsf.account.utils;

import java.util.HashMap;

public class EasyMap<K, V> extends HashMap<K, V> {

    public EasyMap() {
        super();
    }

    public EasyMap(K k, V v) {
        super();
        put(k, v);
    }

    public EasyMap<K, V> easyPut(K k, V v) {
        put(k, v);
        return this;
    }
}
