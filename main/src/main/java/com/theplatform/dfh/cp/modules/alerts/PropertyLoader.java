package com.theplatform.dfh.cp.modules.alerts;

import java.io.*;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyLoader
{
    private static final Logger logger = LoggerFactory.getLogger(PropertyLoader.class);

    public static Properties load(String filePath)
    {
        Properties r = null;
        InputStream is = null;

        try
        {
            r = new Properties();
            is = new FileInputStream(new File(filePath));
            r.load(is);
        }
        catch (FileNotFoundException e)
        {
            String errorMessage = "Properties File:" + " " + filePath + " was not found. \n TIP: If this is a k8s " +
                "service then check your ConfigMap.yaml";
            logger.error(errorMessage, e);
        }
        catch (IOException e)
        {
            String errorMessage = "Properties File:" + " " + filePath + " Unable to load \n TIP: If this is a k8s " +
                "service then check your ConfigMap.yaml";
            logger.error(errorMessage, e);

        }
        return r;
    }
    public static Properties loadResource(String resourcePath)
    {
        try
        {
            Properties properties = new Properties();
            InputStream is = PropertyLoader.class.getResourceAsStream(resourcePath);
            if(is == null)
                throw new FileNotFoundException();
            properties.load(is);
            return properties;
        }
        catch (FileNotFoundException e)
        {
            String errorMessage = "Properties File:" + " " + resourcePath + " was not found. \n TIP: If this is a k8s " +
                "service then check your ConfigMap.yaml";
            logger.error(errorMessage, e);
        }
        catch (IOException e)
        {
            String errorMessage = "Properties File:" + " " + resourcePath + " Unable to load \n TIP: If this is a k8s " +
                "service then check your ConfigMap.yaml";
            logger.error(errorMessage, e);

        }
        return null;
    }
}
