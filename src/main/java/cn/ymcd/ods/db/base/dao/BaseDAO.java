package cn.ymcd.ods.db.base.dao;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.Assert;

import cn.ymcd.ods.OdsComponent;
import cn.ymcd.ods.db.base.annotation.Table;
import cn.ymcd.ods.db.base.annotation.UpdateKey;
import cn.ymcd.ods.db.base.annotation.UseGp;
import cn.ymcd.ods.db.base.annotation.UseLimit;
import cn.ymcd.ods.db.base.annotation.UseMpp;
import cn.ymcd.ods.db.base.search.Page;
import cn.ymcd.ods.db.base.search.SearchForm;
import cn.ymcd.ods.db.base.search.SimpleSearchForm;
import cn.ymcd.ods.service.id.IdService;
import cn.ymcd.ods.util.PojoUtils;
import cn.ymcd.ods.util.SqlUtil;

/**
 * 
 */
public class BaseDAO<T> extends JdbcDaoSupport {

    public static final String DEFAULT_DS = "dataSource";
    public static final String ELEFENCE_DS = "elefenceDataSource";
    public static final String PICKDATA_DS = "pickdataDataSource";
    public static final String TM_DS = "tmDataSource";
    public static final String BR_DS = "brDataSource";
    public static final String IDENTITY_DS = "identityDataSource";
    public static final String MPP_DS = "mppDataSource";

    /**
     * 泛型
     */
    private Class<T> entityClass;
    private String tableName;
    private final Logger LOGGER = Logger.getLogger("sql");

    @SuppressWarnings("unchecked")
    public BaseDAO() {
        ParameterizedType type = (ParameterizedType)getClass().getGenericSuperclass();
        entityClass = (Class<T>)type.getActualTypeArguments()[0];
        Table table = entityClass.getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("dto对象没有Table注解");
        }
        tableName = table.name();
        // LOGGER.debug(getClass() + "实现类是：" + entityClass.getName());
    }

    /**
     * 插入记录，默认检查时间；
     * 
     * @param dto
     * @return
     */
    public int insert(T dto) {
        return save(dto);
    }

    /**
     * 插入记录，默认
     * 
     * @param dto
     * @param needCheck
     * @return
     * @author:fuh
     * @createTime:2017年9月15日 上午9:08:35
     */
    public int insert(T dto, boolean needCheck) {
        return save(dto, needCheck);
    }

    public int insert(T dto, String tableName, boolean needCheck) {
        return save(dto, tableName, needCheck);
    }

    /**
     * 插入记录，默认检查时间；
     * 
     * @param entity
     * @return
     */
    public int save(T entity) {
        return save(entity, true);
    }

    /**
     * 插入记录；
     * 
     * @param entity
     * @param needCheckTime
     *            是否检查时间
     * @return
     * @author:fuh
     * @createTime:2017年9月15日 上午9:07:14
     */
    public int save(T entity, boolean needCheckTime) {
        return save(entity, null, needCheckTime);
    }

    public int save(T entity, String tableName, boolean needCheckTime) {
        Class<? extends Object> clz = entity.getClass();
        UpdateKey updateKey = clz.getAnnotation(UpdateKey.class);
        if (updateKey != null) {
            String keys = updateKey.keys();
            String[] keyArray = keys.split(",");
            if (keyArray.length == 1 && keyArray[0].equals("id")) {
                autoSetId(entity, clz);
            }
        }
        if (needCheckTime) {
            PojoUtils.checkAndSetTime(entity, PojoUtils.ONLY_CREATE);
        }
        String sql = null;
        if (tableName == null) {
            sql = PojoUtils.getInsertSql(entity);
        } else {
            sql = PojoUtils.getInsertSql(entity, tableName);
        }
        Object[] args = PojoUtils.getInsertArgs(entity);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql, args)));
        }
        return getJdbcTemplate().update(sql.toString(), args);
    }

    private void autoSetId(T entity, Class<? extends Object> clz) {
        try {
            Field idField = clz.getDeclaredField("id");
            if (idField.getType() == Long.class) {
                try {
                    idField.setAccessible(true);
                    Object object = idField.get(entity);
                    if (object == null) {
                        IdService idService = OdsComponent.getBean(IdService.class);
                        long longId = idService.getLongId(clz);

                        try {
                            idField.set(entity, longId);
                        } catch (IllegalArgumentException e) {
                            LOGGER.error("->id设置失败", e);
                        } catch (IllegalAccessException e) {
                            LOGGER.error("->id设置失败", e);
                        }
                    }
                } catch (IllegalArgumentException e1) {
                    LOGGER.error("->id取值失败", e1);
                } catch (IllegalAccessException e1) {
                    LOGGER.error("->id取值失败", e1);
                }
            } else if (idField.getType() == String.class) {
                try {
                    idField.setAccessible(true);
                    Object object = idField.get(entity);
                    if (object == null) {
                        IdService idService = OdsComponent.getBean(IdService.class);
                        String rowKey = idService.getRowKey();

                        try {
                            idField.set(entity, rowKey);
                        } catch (IllegalArgumentException e) {
                            LOGGER.error("->id设置失败", e);
                        } catch (IllegalAccessException e) {
                            LOGGER.error("->id设置失败", e);
                        }
                    }
                } catch (IllegalArgumentException e1) {
                    LOGGER.error("->id取值失败", e1);
                } catch (IllegalAccessException e1) {
                    LOGGER.error("->id取值失败", e1);
                }
            }
        } catch (NoSuchFieldException e) {
            LOGGER.error("->id字段获取失败", e);
        } catch (SecurityException e) {
            LOGGER.error("->id字段获取失败", e);
        }
    }

    /**
     * 批量插入，null字段也插入
     * 
     * @param dtoList
     * @return
     */
    public int[] batchInsert(final List<T> dtoList) {

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

    public int[] batchInsert(final List<T> dtoList, final String tableName) {

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

    /**
     * 更新记录，null字段不会更新，使用updateKey字段作为更新的key
     * 
     * @param entity
     * @return
     */
    public int update(T entity) {
        return update(entity, false);
    }

    /**
     * 更新记录;setNull为true，null会更新到数据库，false不会更新；
     * 
     * @param entity
     * @param setNull
     * @return
     */
    public int update(T entity, boolean setNull) {
        return update(entity, setNull, true);
    }

    /**
     * 更新记录；
     * 
     * @param entity
     * @param setNull
     *            是否设置Null值；
     * @param checkTime
     *            是否检查时间值；
     * @return
     * @author:fuh
     * @createTime:2017年9月15日 上午9:06:47
     */
    public int update(T entity, boolean setNull, boolean checkTime) {
        return update(entity, null, setNull, checkTime);
    }

    public int update(T entity, String tableName, boolean setNull, boolean checkTime) {
        if (checkTime) {
            PojoUtils.checkAndSetTime(entity, PojoUtils.ONLY_UPDATE);
        }
        String sql = null;
        if (tableName == null) {
            sql = PojoUtils.getUpdateSql(entity, setNull);
        } else {
            sql = PojoUtils.getUpdateSql(entity, tableName, setNull);
        }
        Object[] args = PojoUtils.getUpdateArgs(entity);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql, args)));
        }
        return getJdbcTemplate().update(sql, args);
    }

    /**
     * 更新，自由组织value字段和key字段
     * 
     * @param valueMap
     * @param keyMap
     * @return
     */
    public int update(Map<String, Object> valueMap, Map<String, Object> keyMap) {
        Assert.notEmpty(valueMap);
        Assert.notEmpty(keyMap);
        StringBuilder sql = new StringBuilder();
        sql.append(" UPDATE " + tableName + " SET ");
        for (String key : valueMap.keySet()) {
            sql.append(key).append("=").append("?,");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE ");
        int i = 0;
        for (String key : keyMap.keySet()) {
            if (i == 0) {
                sql.append(key).append("=").append("?");
            } else {
                sql.append(" AND ").append(key).append("=").append("?");
            }
            i++;
        }
        Object[] args = new Object[valueMap.size() + keyMap.size()];
        int j = 0;
        for (Object val : valueMap.values()) {
            args[j++] = val;
        }
        for (Object val : keyMap.values()) {
            args[j++] = val;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        return getJdbcTemplate().update(sql.toString(), args);
    }

    /**
     * 根据dto中的updateKey删除记录
     * 
     * @param entity
     * @return
     */
    public int delete(T entity) {
        String sql = PojoUtils.getDeleteSql(entity);
        Object[] args = PojoUtils.getDeleteArgs(entity);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        return getJdbcTemplate().update(sql, args);
    }

    /**
     * 根据where语句删除
     * 
     * @param where
     * @param args
     * @return
     */
    public int deleteByWhere(String where, Object...args) {
        String sql = "DELETE FROM " + tableName + " " + where;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql, args)));
        }
        return getJdbcTemplate().update(sql, args);
    }

    /**
     * 删除全表
     */
    public void deleteAll() {
        String sql = " TRUNCATE TABLE " + tableName;
        LOGGER.debug(getDebugSql(sql));
        getJdbcTemplate().execute(sql);
    }

    /**
     * 根据dto中的updateKey查找记录
     * 
     * @param dto
     * @return
     */
    public T findById(T dto) {
        UpdateKey updateKey = dto.getClass().getAnnotation(UpdateKey.class);
        if (updateKey == null) {
            throw new RuntimeException("wrong findById,no updateKey");
        }
        String[] keys = updateKey.keys().split(",");
        String where = "";
        Object[] objs = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            where += keys[i] + "=?";
            objs[i] = PojoUtils.getPropertyValue(dto, keys[i]);
            if (i < keys.length - 1) {
                where += " and ";
            }
        }
        return findOneByWhere(" where " + where, objs);
    }

    public T findById(T dto, String tableName) {
        UpdateKey updateKey = dto.getClass().getAnnotation(UpdateKey.class);
        if (updateKey == null) {
            throw new RuntimeException("wrong findById,no updateKey");
        }
        String[] keys = updateKey.keys().split(",");
        String where = "";
        Object[] objs = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            where += keys[i] + "=?";
            objs[i] = PojoUtils.getPropertyValue(dto, keys[i]);
            if (i < keys.length - 1) {
                where += " and ";
            }
        }
        return findOneByWhereWithTable(" where " + where, tableName, objs);
    }

    /**
     * 根据dto中的updateKey查找记录是否存在
     * 
     * @param dto
     * @return
     */
    public boolean isExist(T dto) {
        UpdateKey updateKey = dto.getClass().getAnnotation(UpdateKey.class);
        if (updateKey == null) {
            throw new RuntimeException("wrong isExist,no updateKey");
        }
        String[] keys = updateKey.keys().split(",");
        String where = "";
        Object[] objs = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            where += keys[i] + "=?";
            objs[i] = PojoUtils.getPropertyValue(dto, keys[i]);
            if (i < keys.length - 1) {
                where += " and ";
            }
        }
        return countByWhere(" where " + where, objs) > 0;
    }

    /**
     * 根据where语句查找单条记录，查找不到返回空，查找到多条会抛出异常
     * 
     * @param where
     * @param args
     * @return
     */
    public T findOneByWhere(String where, Object...args) {
        return findOneByWhereWithTable(where, null, args);
    }

    public T findOneByWhereWithTable(String where, String tableName, Object...args) {
        if (tableName == null) {
            tableName = this.tableName;
        }
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT * FROM ").append(tableName).append(" ").append(where);
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        try {
            return getJdbcTemplate().queryForObject(sql.toString(), args, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 使用findOneByWhere代替
     * 
     * @see BaseDAO#findOneByWhere(String, Object...)
     * @param sql
     * @param args
     * @return
     */
    public T findOneBySql(String sql, Object...args) {
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        try {
            return getJdbcTemplate().queryForObject(sql, args, rowMapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 使用findOneBySql(String)替代
     * 
     * @see BaseDAO#findOneBySql(String)
     * @return
     */
    public T findOneBySql(Class<T> cls, String sql, Object...args) {
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(cls);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        return getJdbcTemplate().query(sql, args, rowMapper).get(0);
    }

    /**
     * 
     * dto支持不建议带表名使用
     * 
     * @param sql
     * @param args
     * @return
     */
    public List<T> findBySql(String sql, Object...args) {
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        return getJdbcTemplate().query(sql, args, rowMapper);
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
            LOGGER.debug(getDebugSql(sql));
        }
        getJdbcTemplate().execute(sql);
    }

    /**
     * 根据where语句查询
     * 
     * @param where
     * @param args
     * @return
     */
    public List<T> findByWhere(String where, Object...args) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT * FROM ").append(tableName).append(" ").append(where);
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        return getJdbcTemplate().query(sql.toString(), args, rowMapper);
    }

    /**
     * 尽量使用findBySql(String)替代
     * 
     * @see BaseDAO#findBySql(String)
     * @return
     */
    public List<T> findBySql(Class<T> cls, String sql, Object...args) {
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(cls);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        return getJdbcTemplate().query(sql, args, rowMapper);
    }

    /**
     * 查询全表记录
     * 
     * @return
     */
    public List<T> findAll() {
        String sql = "SELECT * FROM " + tableName;
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(sql));
        }
        return getJdbcTemplate().query(sql, rowMapper);
    }

    /**
     * 不建议使用 使用searchForm代替
     * 
     * @param pageNo
     * @param pageSize
     * @param where
     * @param orderby
     * @return
     */
    public List<T> find(int pageNo, int pageSize, Map<String, String> where, LinkedHashMap<String, String> orderby) {

        StringBuilder sql = new StringBuilder(" SELECT * FROM (SELECT t.*,ROWNUM rn FROM (SELECT * FROM " + tableName);
        if (where != null && where.size() > 0) {
            sql.append(" WHERE "); // 注意不是where
            for (Map.Entry<String, String> me : where.entrySet()) {
                String columnName = me.getKey();
                String columnValue = me.getValue();
                sql.append(columnName).append(" ").append(columnValue).append(" AND "); // 没有考虑or的情况
            }
            int endIndex = sql.lastIndexOf("AND");
            if (endIndex > 0) {
                sql = new StringBuilder(sql.substring(0, endIndex));
            }
        }
        if (orderby != null && orderby.size() > 0) {
            sql.append(" ORDER BY ");
            for (Map.Entry<String, String> me : orderby.entrySet()) {
                String columnName = me.getKey();
                String columnValue = me.getValue();
                sql.append(columnName).append(" ").append(columnValue).append(",");
            }
            sql = sql.deleteCharAt(sql.length() - 1);
        }
        sql.append(" ) t WHERE ROWNUM<=? ) WHERE rn>=? ");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(sql));
        }
        Object[] args = {pageNo * pageSize, (pageNo - 1) * pageSize + 1};
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        return getJdbcTemplate().query(sql.toString(), args, rowMapper);
    }

    /**
     * 不建议使用 自由组织where条件
     * 
     * @param where
     * @return
     */
    public int count(Map<String, String> where) {
        StringBuilder sql = new StringBuilder(" SELECT COUNT(*) FROM " + tableName);
        if (where != null && where.size() > 0) {
            sql.append(" WHERE ");
            for (Map.Entry<String, String> me : where.entrySet()) {
                String columnName = me.getKey();
                String columnValue = me.getValue();
                sql.append(columnName).append(" ").append(columnValue).append(" AND "); // 没有考虑or的情况
            }
            int endIndex = sql.lastIndexOf("AND");
            if (endIndex > 0) {
                sql = new StringBuilder(sql.substring(0, endIndex));
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(sql));
        }
        return getJdbcTemplate().queryForObject(sql.toString(), Integer.class);
    }

    /**
     * 尽量不使用 使用countByWhere代替
     * 
     * @see BaseDAO#countByWhere(String, Object...)
     * @param sql
     * @param args
     * @return
     */
    public int countBySql(String sql, Object...args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        return getJdbcTemplate().queryForObject(sql.toString(), args, Integer.class);
    }
    
    public long bigCountBySql(String sql, Object...args) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        return getJdbcTemplate().queryForObject(sql.toString(), args, Long.class);
    }
    
    public long bigCountByWhere(String where, Object...args) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT COUNT(1) FROM ").append(tableName).append(" ").append(where);
        return bigCountBySql(sql.toString(), args);
    }

    /**
     * 根据where语句统计记录数
     * 
     * @param where
     * @param args
     * @return
     */
    public int countByWhere(String where, Object...args) {
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT COUNT(1) FROM ").append(tableName).append(" ").append(where);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), args)));
        }
        return getJdbcTemplate().queryForObject(sql.toString(), args, Integer.class);
    }

    /**
     * 根据查询表单查询数据
     * 
     * @see SimpleSearchForm#SimpleSearchForm()
     * @param searchForm
     * @return
     */
    public List<T> findBySearchForm(SimpleSearchForm<T> searchForm) {
        RowMapper<T> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(searchForm.getDirectSearchSql());
        if (searchForm.getPage() != null) {
            Page page = searchForm.getPage();
            Integer pageIndex = page.getPageIndex();
            Integer pageSize = page.getPageSize();
            String countSql = SqlUtil.getCountSql(sql.toString());
            long totalSize = this.bigCountBySql(countSql);
            page.setRowTotal(totalSize);
            sql.append(searchForm.getOrderBy());
            SqlUtil.getOraclePageSql(pageIndex, pageSize, sql);
        } else {
            sql.append(searchForm.getOrderBy());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(sql));
        }
        return getJdbcTemplate().query(sql.toString(), rowMapper);
    }

    public <M> List<M> findBySearchForm(SearchForm searchForm, Class<M> entityClass) {
        UseLimit useLimit = searchForm.getClass().getAnnotation(UseLimit.class);
        if (useLimit != null) {
            return findLimitBySearchForm(searchForm, entityClass, false);
        }
        if (searchForm.getPage() != null) {
            return findBySearchForm(searchForm, entityClass, searchForm.getPage().getSelectTotalRow());
        }
        return findBySearchForm(searchForm, entityClass, true);
    }

    public <M> List<M> findBySearchFormEx(SearchForm searchForm, Class<M> entityClass, boolean isDirect) {
        UseLimit useLimit = searchForm.getClass().getAnnotation(UseLimit.class);
        if (useLimit != null) {
            return findLimitBySearchForm(searchForm, entityClass, isDirect);
        }
        if (searchForm.getPage() != null) {
            return findBySearchForm(searchForm, entityClass, searchForm.getPage().getSelectTotalRow());
        }
        return findBySearchForm(searchForm, entityClass, true, isDirect);
    }

    public <M> List<M> findBySearchForm(SearchForm searchForm, Class<M> entityClass, boolean needCount) {
        UseMpp usempp = searchForm.getClass().getAnnotation(UseMpp.class);
        UseGp usegp = searchForm.getClass().getAnnotation(UseGp.class);
        if (usempp != null) {
            return findMppBySearchForm(searchForm, entityClass, needCount, false);
        } else if (usegp != null) {
            return findGpBySearchForm(searchForm, entityClass, needCount, false);
        } else {
            return findBySearchForm(searchForm, entityClass, needCount, false);
        }
    }

    public <M> List<M> findBySearchForm(SearchForm searchForm, Class<M> entityClass, boolean needCount, boolean isDirect) {
        RowMapper<M> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        StringBuilder sql = new StringBuilder();
        if (isDirect) {
            sql.append(searchForm.getDirectSearchSql());
        } else {
            sql.append(searchForm.getSearchSql());
        }
        if (searchForm.getPage() != null) {
            Page page = searchForm.getPage();
            Integer pageIndex = page.getPageIndex();
            Integer pageSize = page.getPageSize();
            if (needCount) {
                String countSql = SqlUtil.getCountSql(sql.toString());
                long totalSize = this.bigCountBySql(countSql, searchForm.getSearchParams());
                page.setRowTotal(totalSize);
            }
            sql.append(searchForm.getOrderBy());
            SqlUtil.getOraclePageSql(pageIndex, pageSize, sql);
        } else {
            sql.append(searchForm.getOrderBy());
        }
        if (LOGGER.isDebugEnabled()) {
            if (isDirect) {
                LOGGER.debug(getDebugSql(sql));
            } else {
                LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), searchForm.getSearchParams())));
            }
        }
        if (isDirect) {
            return getJdbcTemplate().query(sql.toString(), rowMapper);
        } else {
            return getJdbcTemplate().query(sql.toString(), searchForm.getSearchParams(), rowMapper);
        }
    }

    /**
     * 使用MPP
     * 
     * @param searchForm
     * @param entityClass
     * @param needCount
     * @param isDirect
     * @return
     * @author:wangjf
     * @createTime:2017年10月13日 上午11:02:37
     */
    public <M> List<M> findMppBySearchForm(SearchForm searchForm, Class<M> entityClass, boolean needCount,
            boolean isDirect) {
        RowMapper<M> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        StringBuilder sql = new StringBuilder();
        if (isDirect) {
            sql.append(searchForm.getDirectSearchSql());
        } else {
            sql.append(searchForm.getSearchSql());
        }
        if (searchForm.getPage() != null) {
            Page page = searchForm.getPage();
            Integer pageIndex = page.getPageIndex();
            Integer pageSize = page.getPageSize();
            if (needCount) {
                String countSql = SqlUtil.getMppCountSql(sql.toString());
                long totalSize = this.bigCountBySql(countSql, searchForm.getSearchParams());
                page.setRowTotal(totalSize);
            }
            SqlUtil.getMppPageSql(pageIndex, pageSize, sql, searchForm.getOrderBy());
        } else {
            sql.append(searchForm.getOrderBy());
        }
        if (LOGGER.isDebugEnabled()) {
            if (isDirect) {
                LOGGER.debug(getDebugSql(sql));
            } else {
                LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), searchForm.getSearchParams())));
            }
        }
        if (isDirect) {
            return getJdbcTemplate().query(sql.toString(), rowMapper);
        } else {
            return getJdbcTemplate().query(sql.toString(), searchForm.getSearchParams(), rowMapper);
        }
    }

    /**
     * 1000+分页实现
     * 
     * @param searchForm
     * @param entityClass
     * @param isDirect
     * @return
     * @author:wangjf
     * @createTime:2017年10月30日 上午11:40:21
     */
    public <M> List<M> findLimitBySearchForm(SearchForm searchForm, Class<M> entityClass, boolean isDirect) {
        RowMapper<M> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        StringBuilder sql = new StringBuilder();
        if (isDirect) {
            sql.append(searchForm.getDirectSearchSql());
        } else {
            sql.append(searchForm.getSearchSql());
        }
        if (searchForm.getPage() != null) {
            Page page = searchForm.getPage();
            Integer pageIndex = page.getPageIndex();
            Integer pageSize = page.getPageSize();
            sql.append(searchForm.getOrderBy());
            SqlUtil.getLimitPageSql(pageIndex, pageSize, sql);
        } else {
            sql.append(searchForm.getOrderBy());
        }
        if (LOGGER.isDebugEnabled()) {
            if (isDirect) {
                LOGGER.debug(getDebugSql(sql));
            } else {
                LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), searchForm.getSearchParams())));
            }
        }
        if (isDirect) {
            return getJdbcTemplate().query(sql.toString(), rowMapper);
        } else {
            return getJdbcTemplate().query(sql.toString(), searchForm.getSearchParams(), rowMapper);
        }
    }

    /**
     * GP数据查询
     * 
     * @param searchForm
     * @param entityClass
     * @param needCount
     * @param isDirect
     * @return
     * @author:wangjf
     * @createTime:2017年12月4日 下午4:18:11
     */
    public <M> List<M> findGpBySearchForm(SearchForm searchForm, Class<M> entityClass, boolean needCount,
            boolean isDirect) {
        RowMapper<M> rowMapper = BeanPropertyRowMapper.newInstance(entityClass);
        StringBuilder sql = new StringBuilder();
        if (isDirect) {
            sql.append(searchForm.getDirectSearchSql());
        } else {
            sql.append(searchForm.getSearchSql());
        }
        if (searchForm.getPage() != null) {
            Page page = searchForm.getPage();
            Integer pageIndex = page.getPageIndex();
            Integer pageSize = page.getPageSize();
            if (needCount) {
                String countSql = SqlUtil.getGpCountSql(sql.toString());
                long totalSize = this.bigCountBySql(countSql, searchForm.getSearchParams());
                page.setRowTotal(totalSize);
            }
            SqlUtil.getGpPageSql(pageIndex, pageSize, sql, searchForm.getOrderBy());
        } else {
            sql.append(searchForm.getOrderBy());
        }
        if (LOGGER.isDebugEnabled()) {
            if (isDirect) {
                LOGGER.debug(getDebugSql(sql));
            } else {
                LOGGER.debug(getDebugSql(PojoUtils.fillSQL(sql.toString(), searchForm.getSearchParams())));
            }
        }
        if (isDirect) {
            return getJdbcTemplate().query(sql.toString(), rowMapper);
        } else {
            return getJdbcTemplate().query(sql.toString(), searchForm.getSearchParams(), rowMapper);
        }
    }

    public List<Map<String, Object>> findMapBySearchForm(SearchForm searchForm) {
        StringBuilder sql = new StringBuilder();
        sql.append(searchForm.getDirectSearchSql());
        if (searchForm.getPage() != null) {
            Page page = searchForm.getPage();
            Integer pageIndex = page.getPageIndex();
            Integer pageSize = page.getPageSize();
            String countSql = SqlUtil.getCountSql(sql.toString());
            long totalSize = this.bigCountBySql(countSql);
            page.setRowTotal(totalSize);
            sql.append(searchForm.getOrderBy());
            SqlUtil.getOraclePageSql(pageIndex, pageSize, sql);
        } else {
            sql.append(searchForm.getOrderBy());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(sql));
        }
        return getJdbcTemplate().queryForList(sql.toString());
    }

    public int countBySearchForm(SearchForm searchForm) {
        StringBuilder sql = new StringBuilder();
        sql.append(searchForm.getDirectSearchSql());
        String countSql = SqlUtil.getCountSql(sql.toString());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getDebugSql(sql));
        }
        return getJdbcTemplate().queryForObject(countSql, Integer.class);
    }

    private String getDebugSql(StringBuilder sql) {
        return getDebugSql(sql.toString());
    }

    private String getDebugSql(String sql) {
        return "SQL=[" + this.getClass().getSimpleName() + "] " + sql;
    }

}
