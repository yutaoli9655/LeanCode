package com.cscs.action;

import com.alibaba.fastjson.JSON;
import com.cscs.jdbc.JdbcManager;
import com.cscs.jdbc.ListResultHandler;
import com.cscs.util.LogUtils;
import com.cscs.util.PropUtils;
import com.cscs.util.StringUtils;
import com.cscs.warn.WarningServer;
import com.zaxxer.hikari.pool.HikariPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据拉取
 */
public class SourceToTarget {

    private static Logger logger = LoggerFactory.getLogger(SourceToTarget.class);

    /**
     * 表名
     */
    private static String tableName;
    /**
     * Source端参数
     */
    private static String sourceUrl;
    private static String sourceUser;
    private static String sourcePwd;
    private static String sourceDriver;
    /**
     * Target端参数
     */
    private static String targetUrl;
    private static String targetUser;
    private static String targetPwd;
    private static String targetDriver;

    JdbcManager sourceJdbc = null;
    JdbcManager targetJdbc = null;

    /**
     * 判断是否执行数据库脚本
     * 默认不执行
     * 1:执行 0:不执行
     */
    public static int Flag = 0;

    /**
     * 加载参数
     */
    static {
        sourceUrl = PropUtils.getString("oracle1.url");
        sourceUser = PropUtils.getString("oracle1.username");
        sourcePwd = PropUtils.getString("oracle1.password");
        sourceDriver = PropUtils.getString("oracle1.driver");

        targetUrl = PropUtils.getString("oracle2.url");
        targetUser = PropUtils.getString("oracle2.username");
        targetPwd = PropUtils.getString("oracle2.password");
        targetDriver = PropUtils.getString("oracle2.driver");
    }

    /**
     * 数据同步
     */
    public String dataPull() {

        try {
            try {
                this.sourceJdbc = new JdbcManager(sourceUrl, sourceUser, sourcePwd, sourceDriver);
            } catch (HikariPool.PoolInitializationException e) {
                logger.error("source端数据库连接建立失败！");
                LogUtils.logException(logger, e);
                WarningServer.commonUseWarning("Source database connection establishment failure", LogUtils.logMessage(logger, e));
                throw new RuntimeException();
            }
            try {
                this.targetJdbc = new JdbcManager(targetUrl, targetUser, targetPwd, targetDriver);
            } catch (HikariPool.PoolInitializationException e) {
                logger.error("target端数据库连接建立失败！");
                LogUtils.logException(logger, e);
                WarningServer.commonUseWarning("Target database connection establishment failure", LogUtils.logMessage(logger, e));
                throw new RuntimeException();
            }
        } catch (ClassNotFoundException e) {
            logger.error("找不到JDBC驱动！");
            LogUtils.logException(logger, e);
            WarningServer.commonUseWarning("No JDBC driver can be found", LogUtils.logMessage(logger, e));
            throw new RuntimeException();
        }
        tableName = PropUtils.getString("tableName");
        String[] t_names = tableName.split(",");
        StringBuffer sql = new StringBuffer();
        //存储邮件正文
        StringBuffer body = new StringBuffer();
        if (t_names.length != 0 && t_names != null) {
            //记录本批次开始时间
            Timestamp sdt = new Timestamp(System.currentTimeMillis());
            //循环遍历每张表 依次迁移数据
            try {
                for (int i = 0; i < t_names.length; i++) {
                    String caihuiName = null;
                    try {
                        String[] t_name = t_names[i].split(":");
                        caihuiName = t_name[0];
                        String stgName = t_name[1];

                        //拼接表名去查询TARGET数据库中TMSTAMP字段
                        sql.append("SELECT MAX(TMSTAMP) as TMSTAMP FROM ");
                        sql.append(stgName);
                        sql.append(" WHERE ISVALID = 1");
                        logger.info("开始查询" + targetUser + "." + stgName + "表TMSTAMP字段值");
                        logger.info("sql:" + sql.toString());
                        Object timestamp = targetJdbc.executeQueryObject(sql.toString(), new ListResultHandler(), null, true);
                        logger.info("查询成功 TMSTAMP:" + JSON.toJSONString(timestamp));
                        sql.setLength(0);
                        String source_sql = "SELECT * FROM " + caihuiName + " WHERE ISVALID = 1";
                        Object[] obj = null;
                        if (timestamp != null) {
                            source_sql = source_sql + " AND TMSTAMP > ?";
                            obj = new Object[]{timestamp};
                        }
                        logger.info("开始根据timestamp时间增量查询" + sourceUser + "." + caihuiName + "表数据");
                        logger.info("source_sql:" + source_sql);
                        List<Object[]> lists = (List<Object[]>) sourceJdbc.executeQueryList(source_sql, new ListResultHandler(), obj, true);
                        int counts = 0;
                        if (lists != null && lists.size()>0){
                            counts = lists.size();
                        }
                        logger.info("查询成功,总条数:" + counts);
                        int[] re = {0};
                        //源端有数据才开始拉取
                        if ( lists != null&&lists.size() > 0 ) {
                            String insertSql = DBHelp.getSql(lists.get(0).length,stgName);
                            logger.info("开始将" + sourceUser + "." + caihuiName + "增量导入至" + targetUser + "." + stgName);
                            logger.info("insertSql:" + insertSql);
                            logger.info("总条数:" + lists.size() + ",每条数量:" + lists.get(0).length);
                            re = targetJdbc.executeBatch(insertSql, lists, true, true);
                            logger.info("增量导入成功,成功数量:" + re.length);
                        } else {
                            logger.info("本次查询数量为0,直接跳过数据同步...");
                        }
                        /**
                         * 查询isvalid=0的UPDT_DT
                         * 从上次删除之后开始删除
                         */
                        logger.info("开始执行更新操作");
                        StringBuffer deleteSql = new StringBuffer();
                        deleteSql.append("SELECT MAX(UPDT_DT) as UPDT_DT FROM ");
                        deleteSql.append(stgName);
                        deleteSql.append(" WHERE ISVALID = 0");
                        logger.info("开始查询" + targetUser + "." + stgName + "表上一次做更新操作最新UPDT_DT字段值做增量更新");
                        logger.info("deleteSql:" + deleteSql);
                        Object deleteTmstamp = targetJdbc.executeQueryObject(deleteSql.toString(), new ListResultHandler(), null, true);
                        logger.info("查询成功,UPDT_DT:" + JSON.toJSONString(deleteTmstamp));
                        //上次已经删除了
                        String upSql = "SELECT KEYVALUE,ENTRYDATE FROM UPLOG WHERE TABLENAME='" + caihuiName + "' AND OPERTYPE='D'";

                        Object[] param = null;
                        if (deleteTmstamp != null) {
                            upSql = upSql + "  AND ENTRYDATE >?";
                            param = new Object[]{deleteTmstamp};
                        }
                        //封装了keyvalue 和 entrydate 查询的是UPLOG表
                        logger.info("开始根据UPDT_DT条件查询" + sourceUser + ".UPLOG表,即本次需要更新的数据");
                        logger.info("upSql:" + upSql);
                        List<Map<String, Object>> maps = (List<Map<String, Object>>) sourceJdbc.executeQueryMap(upSql, new ListResultHandler(), param, true);
                        logger.info("查询成功,总条数:" + maps.size());
                        if (  maps != null&& maps.size() > 0) {
                            //存放的只是数值
                            List<Object> keyValues = new ArrayList<>();
                            for (Map map : maps) {
                                keyValues.add(((String) map.get("KEYVALUE")).split("=")[1].trim());
                            }

                            logger.info("根据id去" + sourceUser + "." + caihuiName + "表copy指定数据");
                            String sourceSql = DBHelp.getUplogSql(keyValues, caihuiName);
                            logger.info("sourceSql:" + sourceSql);
                            //查询的是源表
                            List<Map<String, Object>> datas = (List<Map<String, Object>>) sourceJdbc.executeQueryMap(sourceSql, new ListResultHandler(), keyValues.toArray(), true);
                            int counts2 = 0;
                            if (datas != null && datas.size()>0){
                                counts2 = datas.size();
                            }
                            logger.info("copy指定数据成功,总条数:" + counts2);
                            List<String> ids = new ArrayList<>();
                            if ( datas != null&&datas.size() > 0) {
                                for (Map data : datas) {
                                    String id = data.get("ID").toString();
                                    ids.add(id);
                                    for (Map map : maps) {
                                        String uid = ((String) map.get("KEYVALUE")).split("=")[1].trim();
                                        if (id.equals(uid)) {
                                            //修改原data里面UPDT_DT的数据
                                            data.put("UPDT_DT", map.get("ENTRYDATE"));
                                            data.put("ISVALID", 0);
                                        }
                                    }
                                }
                                String insertSql2 = DBHelp.getSql(datas.get(0).size() - 1, targetUser + "." + stgName);
                                logger.info("开始更新" + targetUser + "." + stgName + ",总条数:" + datas.size() + ",每条数量:" + datas.get(0).size());
                                logger.info("insertSql2:" + insertSql2);
                                targetJdbc.executeBatchMap(insertSql2, datas, true, true);
                                logger.info("更新" + targetUser + "." + stgName + "成功");
                            }
                        } else {
                            logger.info("查询更新的数据为0,直接跳过....");
                        }
                        logger.info("开始生成本次表数据加载情况,插入SEQ_ETL_STG_LOADLOG日志表");
                        Timestamp edt = new Timestamp(System.currentTimeMillis());
                        String etlSql = "insert into ETL_STG_LOADLOG values(SEQ_ETL_STG_LOADLOG.nextval,?,?,?,?,?,?,?)";
                        Object[] condition = {"CMB_CAIHUI", sourceUser + "." + caihuiName + "->" + targetUser + "." + stgName, Date.valueOf(LocalDate.now()), lists.size(), re.length, sdt, edt};
                        targetJdbc.executeUpdate(etlSql, condition, true, true);
                        logger.info("日志表记录完成\n");

                        //修改标识 执行脚本
                        Flag = 1;
                    } catch (Exception e) {
                        logger.warn("请检查程序异常...");
                        LogUtils.logException(logger, e);
                        body.append(caihuiName + "\n");
                        body.append(LogUtils.logMessage(logger, e) + "\n");
                    }
                } //循环结束
            } catch (Exception e) {
                LogUtils.logException(logger, e);
            } finally {
                logger.info("关闭连接!");
                try {
                    targetJdbc.close();
                    sourceJdbc.close();
                } catch (SQLException e) {
                    logger.error("关闭连接出现异常");
                    LogUtils.logException(logger, e);
                }
            }
        }
        if (body.length() > 0) {
            logger.info("开始一次性发送本次同步失败的表和错误信息!");
            WarningServer.dataSyncFailWarning(body);
        }

        return "即将判断是否有数据成功入库执行数据库脚本etl_scripts.";
    }
}
