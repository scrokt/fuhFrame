package cn.ymcd.ods.db.base.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import cn.ymcd.ods.db.base.search.Page;
import cn.ymcd.ods.db.base.search.SearchForm;
import cn.ymcd.ods.db.base.search.SimpleSearchForm;
import cn.ymcd.ods.util.PojoUtils;
import cn.ymcd.ods.util.SqlUtil;

/**
 * 直接使用的dao，不需要和dto绑定
 * 
 * @author
 * 
 */
public class SimpleDAO extends JdbcDaoSupport {

    private final Logger LOGGER = Logger.getLogger("sql");

    public int countBySql(String sql, Object...args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SQL=[" + this.getClass().getSimpleName() + "] " + PojoUtils.fillSQL(sql, args));
        }
        return getJdbcTemplate().queryForObject(sql.toString(), args, Integer.class);
    }

    public <T> List<T> findBySql(Class<T> entityClass, String sql, Object...args) {
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SQL=[" + this.getClass().getSimpleName() + "] " + PojoUtils.fillSQL(sql, args));
        }
        return getJdbcTemplate().query(sql, args, rowMapper);
    }

    public List<Map<String, Object>> findBySql(String sql, Object...args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SQL=[" + this.getClass().getSimpleName() + "] " + PojoUtils.fillSQL(sql, args));
        }
        return getJdbcTemplate().queryForList(sql, args);
    }

    public int update(String sql, Object...args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SQL=[" + this.getClass().getSimpleName() + "] " + PojoUtils.fillSQL(sql, args));
        }
        return getJdbcTemplate().update(sql, args);
    }

    /**
     * 执行一条sql
     * 
     * @param sql
     * @author:fuh
     * @createTime:2017年7月28日 上午10:10:04
     */
    public void excute(String sql) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SQL=[" + this.getClass().getSimpleName() + "] " + sql);
        }
        getJdbcTemplate().execute(sql);
    }

    /**
     * 根据查询表单查询数据
     * 
     * @see SimpleSearchForm#SimpleSearchForm()
     * @param searchForm
     * @return
     */
    public <T> List<T> findBySearchForm(Class<T> entityClass, SearchForm searchForm) {
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(searchForm.getDirectSearchSql());
        if (searchForm.getPage() != null) {
            Integer pageIndex = searchForm.getPage().getPageIndex();
            Integer pageSize = searchForm.getPage().getPageSize();
            SqlUtil.getOraclePageSql(pageIndex, pageSize, sql);
        }
        sql.append(searchForm.getOrderBy());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SQL=[" + this.getClass().getSimpleName() + "] " + sql);
        }
        return getJdbcTemplate().query(sql.toString(), rowMapper);
    }

    public List<Map<String, Object>> findMapBySearchForm(SearchForm searchForm) {
        StringBuilder sql = new StringBuilder();
        sql.append(searchForm.getDirectSearchSql());
        if (searchForm.getPage() != null) {
            Page page = searchForm.getPage();
            Integer pageIndex = page.getPageIndex();
            Integer pageSize = page.getPageSize();
            String countSql = SqlUtil.getCountSql(sql.toString());
            int totalSize = this.countBySql(countSql);
            page.setRowTotal(totalSize);
            sql.append(searchForm.getOrderBy());
            SqlUtil.getOraclePageSql(pageIndex, pageSize, sql);
        } else {
            sql.append(searchForm.getOrderBy());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SQL=[" + this.getClass().getSimpleName() + "] " + sql);
        }
        return getJdbcTemplate().queryForList(sql.toString());
    }

    public <T> int[] batchInsert(final List<T> dtoList, Class<T> entityClass) {

        return getJdbcTemplate().batchUpdate(PojoUtils.getInsertSql(entityClass), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                T dto = dtoList.get(i);
                PojoUtils.setSqlArg(ps, dto);
            }

            @Override
            public int getBatchSize() {
                return dtoList.size();
            }
        });
    }

    public <T> int[] batchInsert(final List<T> dtoList, Class<T> entityClass, final String tableName) {

        return getJdbcTemplate().batchUpdate(PojoUtils.getInsertSql(entityClass, tableName),
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        T dto = dtoList.get(i);
                        PojoUtils.setSqlArg(ps, dto);
                    }

                    @Override
                    public int getBatchSize() {
                        return dtoList.size();
                    }
                });
    }
}
