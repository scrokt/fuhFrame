package cn.ymcd.ods.db.base.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import cn.ymcd.ods.db.base.annotation.BeforeSql;
import cn.ymcd.ods.db.base.annotation.GroupBy;
import cn.ymcd.ods.db.base.annotation.SubQuery;
import cn.ymcd.ods.util.PojoUtils;
import cn.ymcd.ods.util.StringUtil;

@BeforeSql("select * from sm_user a,sm_role b where a.roleid=b.roleid")
public class JoinSearchForm extends BaseSearchForm {

    private String beforeSql;
    private String groupBy;
    private String subQuery;

    protected JoinSearchForm() {
        BeforeSql sql = this.getClass().getAnnotation(BeforeSql.class);
        if (sql != null) {
            setBeforeSql(sql.value());
        }
        GroupBy group = this.getClass().getAnnotation(GroupBy.class);
        if (group != null) {
            setGroupBy(group.value());
        }
        SubQuery subQuery = this.getClass().getAnnotation(SubQuery.class);
        if (subQuery != null) {
            this.subQuery = subQuery.value();
        }
    }

    @Override
    public String getDirectSearchSql() {
        String sql = getBeforeSql();
        // 替换子查询
        if (StringUtils.isNotBlank(subQuery)) {
            String[] split = subQuery.split(",");
            for (String fieldName : split) {
                Object fieldVal = PojoUtils.getPropertyValue(this, fieldName);
                if (fieldVal instanceof BaseSearchForm) {
                    BaseSearchForm searchForm = (BaseSearchForm)fieldVal;
                    String subQuerySql = searchForm.getDirectSearchSql();
                    sql = sql.replace("${" + fieldName + "}", subQuerySql);
                }
            }
        }

        // 加上查询条件
        String where = getDirectWhere();
        if (!StringUtil.isEmpty(where)) {
            if (getBeforeSql().toUpperCase().contains("WHERE")) {
                sql += " AND ( " + where + " )";
            } else {
                sql += " WHERE " + where;
            }
        }
        // 加上group by
        if (StringUtils.isNotBlank(getGroupBy())) {
            sql += " GROUP BY " + getGroupBy();
        }
        return sql;
    }

    @Override
    public String getSearchSql() {
        String sql = getBeforeSql();
        // 替换子查询
        if (StringUtils.isNotBlank(subQuery)) {
            String[] split = subQuery.split(",");
            for (String fieldName : split) {
                Object fieldVal = PojoUtils.getPropertyValue(this, fieldName);
                if (fieldVal instanceof BaseSearchForm) {
                    BaseSearchForm searchForm = (BaseSearchForm)fieldVal;
                    String subQuerySql = searchForm.getSearchSql();
                    sql = sql.replace("${" + fieldName + "}", subQuerySql);
                }
            }
        }

        // 加上查询条件
        String where = getWhere();
        if (!StringUtil.isEmpty(where)) {
            if (getBeforeSql().toUpperCase().contains("WHERE")) {
                sql += " AND ( " + where + " )";
            } else {
                sql += " WHERE " + where;
            }
        }
        // 加上group by
        if (StringUtils.isNotBlank(getGroupBy())) {
            sql += " GROUP BY " + getGroupBy();
        }
        if(escape){
            sql += " escape '\\'";
        }
        return sql;
    }

    @Override
    public Object[] getSearchParams() {
        // 有子查询
        List<Object[]> subParamsList = new ArrayList<>();
        int subParamsSize = 0;

        if (StringUtils.isNotBlank(subQuery)) {
            String[] split = subQuery.split(",");
            for (String fieldName : split) {
                Object fieldVal = PojoUtils.getPropertyValue(this, fieldName);
                if (fieldVal instanceof BaseSearchForm) {
                    BaseSearchForm searchForm = (BaseSearchForm)fieldVal;
                    Object[] searchParams = searchForm.getSearchParams();
                    subParamsList.add(searchParams);
                    subParamsSize += searchParams.length;
                }
            }
        }

        Object[] params = null;
        if (subParamsSize == 0) {// 没有子查询
            params = new Object[paramList.size()];
            for (int i = 0; i < paramList.size(); i++) {
                params[i] = paramList.get(i);
            }
        } else {// 需要加上子查询的param
            params = new Object[paramList.size() + subParamsSize];
            int i = 0;
            for (Object[] subParams : subParamsList) {
                for (int j = 0; j < subParams.length; j++) {
                    params[i] = subParams[j];
                    i++;
                }
            }
            for (Object pa : paramList) {
                params[i] = pa;
                i++;
            }
        }
        
        return params;
    }

    public String getBeforeSql() {
        return beforeSql;
    }

    public void setBeforeSql(String beforeSql) {
        this.beforeSql = beforeSql;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }
}
