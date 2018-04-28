package cn.ymcd.ods.db.base.search;

import java.lang.reflect.ParameterizedType;

import cn.ymcd.ods.db.base.annotation.Table;
import cn.ymcd.ods.util.StringUtil;

/**
 * 查询表单的实例类
 * 
 * @author fuh
 * @since 1.0
 */

public class SimpleSearchForm<T> extends BaseSearchForm {

    private String tableName;

    @SuppressWarnings("unchecked")
    public SimpleSearchForm() {
        ParameterizedType type = (ParameterizedType)getClass().getGenericSuperclass();
        Class<T> entityClass = (Class<T>)type.getActualTypeArguments()[0];
        Table table = entityClass.getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("dto对象没有Table注解");
        }
        tableName = table.name();
    }

    @Override
    public String getDirectSearchSql() {
        String sql = "SELECT * FROM " + tableName;
        String where = getDirectWhere();
        if (!StringUtil.isEmpty(getDirectWhere())) {
            sql += " WHERE " + where;
        }
        return sql;
    }

    @Override
    public String getSearchSql() {
        String sql = "SELECT * FROM " + tableName;

        // 加上查询条件
        String where = getWhere();
        if (!StringUtil.isEmpty(where)) {
            sql += " WHERE " + where;
        }
        return sql;
    }

    @Override
    public Object[] getSearchParams() {
        Object[] params = new Object[paramList.size()];
        for (int i = 0; i < paramList.size(); i++) {
            params[i] = paramList.get(i);
        }
        return params;
    }
}
