package com.cscs.jdbc;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 参数封装接口
 */
public abstract interface ResultHandler {

    /**
     * 返回单个Object
     * @param rs
     * @return
     * @throws SQLException
     */
    public abstract Object handleObject(ResultSet rs) throws SQLException;

    public abstract String handleString(ResultSet rs) throws SQLException, IOException;


    /**
     * 查询单条记录封装成list返回
     * @param paramResultSet
     * @return
     * @throws SQLException
     */
    public abstract Object handle(ResultSet paramResultSet)
            throws SQLException;

    /**
     *
     * @param rs
     * @return
     * @throws SQLException
     */
    public abstract Object handleList(ResultSet rs)throws SQLException;

    /**
     * 将数据封装成map 然后统一返回list
     * @param rs
     * @return
     * @throws SQLException
     */
    public abstract Object handleMap(ResultSet rs)throws SQLException;

    /**
     * 根据key查询数据
     * @param rs
     * @param key
     * @return
     * @throws SQLException
     */
    public abstract Object handleKey(ResultSet rs, String key)throws SQLException;


    /**
     * 查询数量
     * @param paramResultSet
     * @return
     * @throws SQLException
     */
    public abstract int handleCount(ResultSet paramResultSet)
            throws SQLException;

}
