package com.cscs.util;

import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {


    public static void logException(Logger logger, Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        String str = sw.toString();
        logger.error(str);
    }


    public static  String logMessage(Logger logger,Exception e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        String str = sw.toString();
        return  str;
    }
}
