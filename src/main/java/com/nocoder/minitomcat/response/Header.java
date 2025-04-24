package com.nocoder.minitomcat.response;

/**
 * @author 29282
 */
public class Header {
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

    public Header() {
    }

    public Header(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
