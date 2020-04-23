package com.comast.pop.handler.base.field.retriever.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

// for reference
// https://www.mkyong.com/java/java-properties-file-examples/

public class PropertyProvider
{
    private static Logger logger = LoggerFactory.getLogger(PropertyProvider.class);

    private Properties properties;

    public PropertyProvider(String filePath)
    {
        properties = new Properties();

        // no file, no properties (no error)
        if(filePath == null) return;

        try (FileInputStream input = new FileInputStream(filePath))
        {
            properties.load(input);
            logger.info("Loaded properties file: {}", filePath);
        }
        catch(FileNotFoundException e)
        {
            logger.warn("Properties file not found: {}", filePath);
            //throw new RuntimeException(String.format("File not found: %1$s", filePath), e);
        }
        catch(IOException e)
        {
            throw new RuntimeException(String.format("File read error: %1$s", filePath), e);
        }
    }

    public String getProperty(String propertyName, String defaultValue)
    {
        return properties.getProperty(propertyName, defaultValue);
    }

    public Properties getProperties()
    {
        return this.properties;
    }
}
