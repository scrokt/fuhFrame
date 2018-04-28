package cn.ymcd.ods.util;

public class SqlUtil {

    private static final int QUERY_COUNT = 1000;

    public static String getOraclePageSql(Integer pageIndex, Integer pageSize, StringBuilder sqlBuffer) {
        // 计算第一条记录的位置，Oracle分页是通过rownum进行的，而rownum是从1开始的
        if (pageSize < 0) {
            return sqlBuffer.toString();
        }
        final Integer offset = (pageIndex - 1) * pageSize + 1;
        sqlBuffer.insert(0, "select u.*, rownum r from (").append(") u where rownum < ").append(offset + pageSize);
        sqlBuffer.insert(0, "select * from (").append(") where r >= ").append(offset);
        // 上面的Sql语句拼接之后大概是这个样子：
        // select * from (select u.*, rownum r from (select * from t_user) u
        // where rownum < 31) where r >= 16
        return sqlBuffer.toString();
    }

    public static String getCountSql(String sql) {
        // int index = sql.toLowerCase().indexOf("from");
        return "select count(*) from ( " + sql + " )";
    }

    /**
     * MPP计算总数 如果使用MPP表进行查询则不支持表之间的关联
     * 
     * @param sql
     * @return
     * @author:wangjf
     * @createTime:2017年10月13日 上午11:26:19
     */
    public static String getMppCountSql(String sql) {
        int index = sql.toLowerCase().indexOf("from");
        String rsSql = "select count(*) " + sql.substring(index, sql.length());
        return rsSql;
    }

    /**
     * MPP分页
     * 
     * @param pageIndex
     * @param pageSize
     * @param sqlBuffer
     * @return
     * @author:wangjf
     * @createTime:2017年10月13日 上午11:26:37
     */
    public static String getMppPageSql(Integer pageIndex, Integer pageSize, StringBuilder sqlBuffer, String sqlOrderBy) {
        if (pageSize < 0) {
            return sqlBuffer.toString();
        }
        final Integer offset = (pageIndex - 1) * pageSize;
        sqlBuffer.insert(0, "select * from (").append(") as mpppagetemptable ").append(sqlOrderBy).append(" limit ").append(offset).append(",")
                .append(pageSize);
        return sqlBuffer.toString();
    }

    /**
     * 1000+分页SQL
     * 
     * @param pageIndex
     * @param pageSize
     * @param sqlBuffer
     * @return
     * @author:wangjf
     * @createTime:2017年10月30日 上午10:55:42
     */
    public static String getLimitPageSql(Integer pageIndex, Integer pageSize, StringBuilder sqlBuffer) {
        int query_count = QUERY_COUNT;
        if (query_count < pageIndex * pageSize) {
            query_count = pageIndex * pageSize + QUERY_COUNT;
        }
        String sqlTemp = sqlBuffer.toString().toLowerCase();
        if (sqlTemp.indexOf("order") > -1 && sqlTemp.indexOf("by") > -1) {
            if (sqlTemp.indexOf("where") > -1) {
                sqlBuffer.insert(sqlTemp.indexOf("order"), " and rownum<=" + query_count + " ");
            } else {
                sqlBuffer.insert(sqlTemp.indexOf("order"), " where rownum<=" + query_count + " ");
            }
        } else if (sqlTemp.indexOf("where") > -1) {
            sqlBuffer.insert(sqlTemp.indexOf("where") + 5, " rownum<=" + query_count + " and ");
        } else {
            sqlBuffer.append(" where rownum<=" + query_count + " ");
        }
        if (pageSize < 0) {
            return sqlBuffer.toString();
        }
        final Integer offset = (pageIndex - 1) * pageSize + 1;

        sqlBuffer.insert(0, "select * from (select pagetemp.*, rownum as pagetempid from ( ");
        sqlBuffer.append(" ) pagetemp) where pagetempid <").append(offset + pageSize).append(" and pagetempid >=")
                .append(offset);
        return sqlBuffer.toString();
    }

    /**
     * GP统计总条数
     * 
     * @param sql
     * @return
     * @author:wangjf
     * @createTime:2017年12月5日 上午8:38:43
     */
    public static String getGpCountSql(String sql) {
        return "select count(*) from ( " + sql + " ) as gpcounttemptable";
    }

    /**
     * GP分页
     * 
     * @param pageIndex
     * @param pageSize
     * @param sqlBuffer
     * @param sqlOrderBy
     * @return
     * @author:wangjf
     * @createTime:2017年12月5日 上午8:39:27
     */
    public static String getGpPageSql(Integer pageIndex, Integer pageSize, StringBuilder sqlBuffer, String sqlOrderBy) {
        if (pageSize < 0) {
            return sqlBuffer.toString();
        }
        final Integer offset = (pageIndex - 1) * pageSize;
        sqlBuffer.insert(0, "select * from (").append(") as gppagetemptable ").append(sqlOrderBy).append(" limit ").append(pageSize)
                .append(" offset ").append(offset);
        return sqlBuffer.toString();
    }
}
