import com.cscs.action.SourceToTarget;
import com.cscs.mail.SendMail;
import com.cscs.shell.RunShell;
import com.cscs.util.LogUtils;
import com.cscs.util.PropUtils;
import com.cscs.util.StringUtils;

import com.cscs.warn.WarningServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 拉取数据
 *
 * @author yutao.li
 */
public class MasterDataSyncApplication {


    private static Logger logger = LoggerFactory.getLogger(MasterDataSyncApplication.class);

    /**
     * 程序入口
     *
     * @param args
     */
    public static void main(String[] args) throws ClassNotFoundException {

        ExecutorService exec = Executors.newSingleThreadExecutor();
        Callable<String> call = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return new SourceToTarget().dataPull();
            }
        };
        Future<String> future = null;
        try {
            future = exec.submit(call);
            String obj = future.get(PropUtils.getInteger("timeout"), TimeUnit.MINUTES);
            logger.info(obj);
        } catch (TimeoutException ex) {
            logger.error("本次任务超时,将进入下次轮询....");
            LogUtils.logException(logger, ex);
            WarningServer.commonUseWarning("This task is over time", LogUtils.logMessage(logger, ex));
            future.cancel(true);
        } catch (Exception e) {
            logger.error("设置任务失败!");
            LogUtils.logException(logger, e);
            WarningServer.commonUseWarning("Setting up a task failure", LogUtils.logMessage(logger, e));
        } finally {
            if (exec != null) {
                exec.shutdown();
            }
        }

        /**
         * 串行执行shell脚本 脚本是否执行取决于当前批次有没有文件成功入库
         * FLAG 1:成功 0:失败
         */
        if (SourceToTarget.Flag == 1) {
            List<String> lines = RunShell.executeShell();
            if (!(lines.isEmpty())) {
                String result = lines.get(lines.size() - 1);
                //0 表示脚本运行成功
                if (!(result.equals("0"))) {
                    Map<String, Object> m = SendMail.getToken();
                    String token = (String) m.get("accessToken");
                    if (StringUtils.isEmpty(token)) {
                        logger.error(m.toString());
                    } else {
                        String subject = "etl_scripts.sh";
                        StringBuffer sb = new StringBuffer();
                        sb.append("etl_scripts.sh脚本运行失败:\n");
                        for (String line : lines) {
                            sb.append(line + "\n");
                        }
                        Map<String, Object> map2 = SendMail.sendMail(subject, sb.toString(), token);
                        String emailId = (String) map2.get("emailId");
                        if (StringUtils.isEmpty(emailId)) {
                            logger.error(map2.toString());
                        } else {
                            SendMail.queryMail(emailId, token);
                        }
                    }
                } else {
                    logger.info("etl_scripts脚本运行成功!");
                }
            }
        } else {
            logger.info("本次没有任何表数据迁移,直接跳过执行数据库脚本!");
        }
    }

}

