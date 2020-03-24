package com.theplatform.dfh.cp.modules.kube.client.config;

public class KeyPathPair {
    private String key;
    private String path;

    public KeyPathPair() {
    }

    public KeyPathPair(String key, String path) {
        this.key = key;
        this.path = path;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
