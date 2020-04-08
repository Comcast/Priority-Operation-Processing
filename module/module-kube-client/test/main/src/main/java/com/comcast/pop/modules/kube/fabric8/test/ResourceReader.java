package com.comcast.pop.modules.kube.fabric8.test;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceReader {
    static final String PROPFILE = "propFile";
    private static final Logger logger = LoggerFactory.getLogger(ResourceReader.class);
    private String bundleName = System.getProperty("propFile");

    public ResourceReader(String defaultBundleName) {
        if (this.bundleName == null || this.bundleName.length() == 0) {
            this.bundleName = defaultBundleName;
        }

        if (this.bundleName.endsWith(".properties")) {
            this.bundleName = this.bundleName.substring(0, this.bundleName.indexOf(".properties"));
        }

    }

    public String getBundleName() {
        return this.bundleName;
    }

    public String getValue(String key) throws MissingResourceException {
        String value = "";
        ResourceBundle rb = ResourceBundle.getBundle(this.bundleName);
        if (rb != null) {
            value = rb.getString(key);
        }

        return value;
    }
}

