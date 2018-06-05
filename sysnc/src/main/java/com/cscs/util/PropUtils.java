package com.cscs.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * 解析配置文件类
 */
public class PropUtils {

    static final String PROPERTY_FILE_NAME = "application.properties";
    private static Properties props = new Properties();

    static
    {
        try
        {
            ClassLoader cl = PropUtils.class.getClassLoader();
            InputStream in = cl.getResourceAsStream(PROPERTY_FILE_NAME);
            props.load(in);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String getString(String key)
    {
        return props.getProperty(key);
    }

    public static Integer getInteger(String key)
    {
        String value = props.getProperty(key);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return Integer.valueOf(value);
    }

}
