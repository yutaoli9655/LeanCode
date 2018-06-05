package com.cscs.shell;

import com.cscs.util.PropUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * shell脚本类
 */
public class RunShell {


    private static final Logger logger = LoggerFactory.getLogger(RunShell.class);

    /**
     * 执行shell脚本
     * @return
     */
    public static List<String> executeShell() {


        InputStreamReader stdISR = null;
        Process process = null;
        String command = PropUtils.getString("command");
        logger.info("command:"+command);
        List<String> list = null;
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            String line = null;
            stdISR = new InputStreamReader(process.getInputStream());
            BufferedReader stdBR = new BufferedReader(stdISR);
            list = new ArrayList<>();
            while ((line = stdBR.readLine()) != null) {
                logger.info("STD line:"+line);
                list.add(line);
            }

        } catch (IOException | InterruptedException e) {
            logger.error(e.toString());
        } finally {
            try {
                if (stdISR != null) {
                    stdISR.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                logger.error("正式执行命令：" + command + "有IO异常");
            }
        }

        return list;
    }
}



