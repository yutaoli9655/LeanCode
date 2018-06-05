package com.cscs.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringUtils {

    public static String getStringFromResource(String fileName)
    {
        try
        {
            ClassLoader cl = StringUtils.class.getClassLoader();
            InputStream in = cl.getResourceAsStream(fileName);
            BufferedReader buf = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            while ((line = buf.readLine()) != null) {
                str.append(line).append("\n");
            }
            buf.close();
            return str.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static boolean isEmpty(String str)
    {
        if ((str == null) || ("".equals(str))) {
            return true;
        }
        return false;
    }
}
