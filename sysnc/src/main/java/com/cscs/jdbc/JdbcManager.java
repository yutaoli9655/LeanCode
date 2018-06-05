package com.cscs.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import oracle.sql.CLOB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * jdbc连接池类
 */
public class JdbcManager {

    private String dburl;
    private String user;
    private String password;
    public ThreadLocal tl = new ThreadLocal();
    public HikariDataSource dataSource;

    /**
     * 加载连接
     *
     * @param dburl
     * @param user
     * @param password
     * @param driver
     * @throws ClassNotFoundException
     */
    public JdbcManager(String dburl, String user, String password, String driver)
            throws ClassNotFoundException {
        Class.forName(driver);
        this.dburl = dburl;
        this.user = user;
        this.password = password;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dburl);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(30);
        config.setAutoCommit(false);
        this.dataSource = new HikariDataSource(config);
    }

    /**
     * 获取连接
     *
     * @return
     */
    public Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int executeUpdate(String sql, Object[] params, boolean releaseConn, boolean autoCommit)
            throws SQLException {
        int rtn = 0;
        PreparedStatement stmt = null;
        Connection conn = null;
        try {
            if (this.tl.get() == null) {
                conn = getConnection();
                this.tl.set(conn);
            } else {
                conn = (Connection) this.tl.get();
            }
            stmt = conn.prepareStatement(sql);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            rtn = stmt.executeUpdate();
            if (autoCommit) {
                conn.commit();
            }
            return rtn;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    throw new RuntimeException(e1);
                }
            }
            throw new SQLException(e);
        } finally {

            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ((conn != null) && (releaseConn)) {
                close();
                conn = null;
            }
        }
    }

    public Object executeQueryObject(String sql, ResultHandler resultHandler, Object[] params, boolean releaseConn)
            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            if (this.tl.get() == null) {
                conn = getConnection();
                this.tl.set(conn);
            } else {
                conn = (Connection) this.tl.get();
            }
            stmt = conn.prepareStatement(sql);
            int i;

            if (params != null && params.length != 0) {
                for (i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            rs = stmt.executeQuery();
            return resultHandler.handleObject(rs);
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((conn != null) && (releaseConn)) {
                close();
                conn = null;
            }
        }
    }

    public Object executeQueryList(String sql, ResultHandler resultHandler, Object[] params, boolean releaseConn)
            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            if (this.tl.get() == null) {
                conn = getConnection();
                this.tl.set(conn);
            } else {
                conn = (Connection) this.tl.get();
            }
            stmt = conn.prepareStatement(sql);
            int i;
            if (params != null) {
                for (i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            rs = stmt.executeQuery();
            return resultHandler.handleList(rs);
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((conn != null) && (releaseConn)) {
                close();
                conn = null;
            }
        }
    }


    public Object executeQueryMap(String sql, ResultHandler resultHandler, Object[] params, boolean releaseConn)
            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            if (this.tl.get() == null) {
                conn = getConnection();
                this.tl.set(conn);
            } else {
                conn = (Connection) this.tl.get();
            }
            stmt = conn.prepareStatement(sql);
            int i;
            if (params != null) {
                for (i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            rs = stmt.executeQuery();
            return resultHandler.handleMap(rs);
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((conn != null) && (releaseConn)) {
                close();
                conn = null;
            }
        }
    }


    public int executeQueryInt(String sql, ResultHandler resultHandler, Object[] params, boolean releaseConn)
            throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            if (this.tl.get() == null) {
                conn = getConnection();
                this.tl.set(conn);
            } else {
                conn = (Connection) this.tl.get();
            }
            stmt = conn.prepareStatement(sql);
            int i;
            if (params != null) {
                for (i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
            }
            rs = stmt.executeQuery();
            return resultHandler.handleCount(rs);
        } catch (SQLException e) {
            throw new SQLException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                    rs = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((conn != null) && (releaseConn)) {
                close();
                conn = null;
            }
        }
    }


    public int[] executeBatch(String sql, List<Object[]> paramList, boolean releaseConn, boolean autoCommit)
            throws SQLException, IOException {
        int[] re;
        int rtn2 = 0;
        PreparedStatement stmt = null;
        Connection conn = null;
        BufferedReader br = null;
        Reader is = null;
        try {
            if (this.tl.get() == null) {
                conn = getConnection();
                this.tl.set(conn);
            } else {
                conn = (Connection) this.tl.get();
            }
            stmt = conn.prepareStatement(sql);
            if (paramList != null) {
                int j = 0;
                for (Object[] params : paramList) {
                    for (int i = 0; i < params.length; i++) {
                        if (params[i] instanceof CLOB) {
                            //解决CLOB类型的数据在批处理时候报table not exist
                            CLOB clob = (CLOB) params[i];
                            String reString = "";
                            is = clob.getCharacterStream();
                            br = new BufferedReader(is);
                            String s = br.readLine();
                            StringBuffer sb = new StringBuffer();
                            while (s != null) {
                                sb.append(s);
                                s = br.readLine();
                            }
                            reString = sb.toString();
                            stmt.setString(i + 1, reString);
                        } else {
                            stmt.setObject(i + 1, params[i]);
                        }
                    }
                    //处理最后一个up_date 字段 源端表
                    Timestamp updt = new Timestamp(System.currentTimeMillis());
                    stmt.setObject(params.length + 1, updt);
                    stmt.addBatch();
                    j++;
                    if (j % 20000 == 0) {
                        int[] r = stmt.executeBatch();
                        rtn2 += r.length;
                        stmt.clearBatch();
                    }
                }
            }
            re = stmt.executeBatch();
            if (autoCommit) {
                conn.commit();
            }
            re = new int[re.length + rtn2];
            return re;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    throw new RuntimeException(e1);
                }
            }
            throw new SQLException(e);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((conn != null) && (releaseConn)) {
                close();
                conn = null;
            }
        }
    }


    public int[] executeBatchMap(String sql, List<Map<String, Object>> paramList, boolean releaseConn, boolean autoCommit)
            throws SQLException, IOException {
        int[] rtn;
        PreparedStatement stmt = null;
        Connection conn = null;
        BufferedReader br = null;
        Reader is = null;
        try {
            if (this.tl.get() == null) {
                conn = getConnection();
                this.tl.set(conn);
            } else {
                conn = (Connection) this.tl.get();
            }
            stmt = conn.prepareStatement(sql);
            if (paramList != null && paramList.size() > 0) {
                for (Map params : paramList) {
                    int i = 1;
                    for (Object value : params.values()) {
                        if (value instanceof CLOB) {
                            //解决CLOB类型的数据在批处理时候报table not exist
                            CLOB clob = (CLOB) value;
                            String reString = "";
                            is = clob.getCharacterStream();
                            br = new BufferedReader(is);
                            String s = br.readLine();
                            StringBuffer sb = new StringBuffer();
                            while (s != null) {
                                sb.append(s);
                                s = br.readLine();
                            }
                            reString = sb.toString();
                            stmt.setString(i, reString);
                        } else {
                            stmt.setObject(i, value);
                        }
                        i++;
                    }
                    stmt.addBatch();
                }
            }
            rtn = stmt.executeBatch();
            if (autoCommit) {
                conn.commit();
            }
            return rtn;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                    throw new RuntimeException(e1);
                }
            }
            throw new SQLException(e);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((conn != null) && (releaseConn)) {
                close();
                conn = null;
            }
        }
    }


    public void close()
            throws SQLException {
        Connection conn = (Connection) this.tl.get();
        if (conn != null) {
            conn.close();
        }
        this.tl.set(null);
    }

}
