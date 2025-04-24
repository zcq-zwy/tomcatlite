package com.nocoder.minitomcat.cookie;

/**
 * Cookie
 * @author 29282
 */
public class Cookie {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Cookie() {
    }

    public Cookie(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
