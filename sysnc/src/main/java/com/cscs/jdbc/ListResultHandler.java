package com.cscs.jdbc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数封装类
 *
 * @author yutao.li
 */
public class ListResultHandler implements ResultHandler {

    /**
     * 查询单条记录封装成list返回
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    @Override
    public Object handle(ResultSet rs) throws SQLException {
        List<Object> rowList = new ArrayList();
        while (rs.next()) {
            Object columnValue = rs.getObject(1);
            rowList.add(columnValue);
        }
        return rowList;
    }


    @Override
    public Object handleObject(ResultSet rs) throws SQLException {
        Object columnValue = null;
        if (rs.next()) {
            columnValue = rs.getObject(1);
        }
        return columnValue;
    }

    @Override
    public String handleString(ResultSet rs) throws SQLException, IOException {
        String result = null;
        if (rs.next()) {
            InputStream inputStream = rs.getBinaryStream(1);

            ByteArrayOutputStream infoStream = new ByteArrayOutputStream();
            int len = 0;
            byte[] bytes = new byte[1024];
            try {
                if (inputStream != null) {
                    while ((len = inputStream.read(bytes)) != -1) {
                        infoStream.write(bytes, 0, len);
                    }
                }
            } catch (IOException e) {
                throw new IOException();
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            result = infoStream.toString();
        }
        return result;
    }

    /**
     * 根据key查询数据
     *
     * @param rs
     * @param key
     * @return
     * @throws SQLException
     */
    public Object handleKey(ResultSet rs, String key) throws SQLException {
        List<Object> rowList = new ArrayList();
        while (rs.next()) {
            Object columnValue = rs.getObject(key);

            rowList.add(columnValue);
        }
        return rowList;
    }

    /**
     * 查询数据封装成Object数组 统一返回list
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    @Override
    public Object handleList(ResultSet rs) throws SQLException {
        List<Object[]> rowList = new ArrayList();
        while (rs.next()) {
            List<Object> dataList = new ArrayList();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                Object columnValue = rs.getObject(i);
                dataList.add(columnValue);
            }
            Object[] objects = dataList.toArray();
            rowList.add(objects);
        }
        return rowList;
    }

    /**
     * 将数据封装成map 然后统一返回list
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    @Override
    public Object handleMap(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rowList = new ArrayList();
        ResultSetMetaData metaData = rs.getMetaData();
        int cols_len = metaData.getColumnCount();
        while (rs.next()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < cols_len; i++) {
                String cols_name = metaData.getColumnName(i + 1);
                Object cols_value = rs.getObject(cols_name);
                map.put(cols_name, cols_value);
            }

            rowList.add(map);
        }
        return rowList;
    }

    /**
     * 查询单个int类型的字段
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    @Override
    public int handleCount(ResultSet rs) throws SQLException {

        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }

        return count;
    }
}
