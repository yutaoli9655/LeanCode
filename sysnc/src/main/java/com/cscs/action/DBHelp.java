package com.cscs.action;


import java.util.List;

/**
 * 拼装sql
 */
public class DBHelp {


    /**
     * 拼装插入sql
     * @param columnCount
     * @param tableName
     * @return
     */
    public static String getSql(int columnCount, String tableName) {


        StringBuffer sb = new StringBuffer();
        String z = "?";
        //获取表名
        sb.append("INSERT INTO ");
        sb.append(tableName);
        sb.append(" VALUES(");

        for (int c = 0; c < columnCount; c++) {
            sb.append(z);
            if (c < columnCount - 1) {
                sb.append(",");
            }
        }
        //处理最后一个up_date字段
        sb.append(","+z);
        sb.append(")");


        return sb.toString();

    }

    /**
     * 拼装Uplog表sql
     * @param keyValues
     * @param tableName
     * @return
     */
    public static String getUplogSql(List<Object> keyValues, String tableName) {

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < keyValues.size(); i++) {

            if ((i % 900) == 0 && i > 0) {
                sb.deleteCharAt(sb.length() - 1);
                sb.append(") OR id IN (?,");
            } else {
                sb.append( "?,");
            }
        }
        sb.deleteCharAt(sb.length() - 1);

        String selectSQL = "SELECT /*+PARALLEL(A,10)*/* FROM "+tableName+" A WHERE id IN ( " + sb.toString() + " )";

        return selectSQL;
    }


    /**
     * 只获取id
     * @param keyValues
     * @param tableName
     * @return
     */
    public static String getTaSql(List<Object> keyValues, String tableName) {
        StringBuffer ssql = new StringBuffer();
        if (keyValues.size() > 0 && keyValues != null) {
            ssql.append("select /*+PARALLEL(A,10)*/id from ");
            ssql.append(tableName+" A");
            ssql.append(" where id in(");
            int count = 0;
            for (Object id : keyValues) {
                ssql.append("?");
                if (count < keyValues.size() - 1) {
                    ssql.append(",");
                }
                count++;
            }
            ssql.append(")");
            ssql.append(" and ISVALID = 0");
            //ssql.append(" and TMSTAMP > ?");

        }

        return ssql.toString();
    }


   
}
