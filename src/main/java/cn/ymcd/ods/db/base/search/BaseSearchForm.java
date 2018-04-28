package cn.ymcd.ods.db.base.search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import cn.ymcd.ods.db.base.annotation.OrderBy;
import cn.ymcd.ods.db.base.annotation.SearchExpr;
import cn.ymcd.ods.util.PojoUtils;
import cn.ymcd.ods.util.StringUtil;

/**
 * 查询表单的基础类，负责生成where语句和orderby，另外集成分页器
 * 
 * @author fuh
 * @since 1.0
 */
public abstract class BaseSearchForm implements SearchForm {

    private Map<String, List<Condition>> conditions = new HashMap<>();
    private List<String> orderBys = new ArrayList<>();
    List<Object> paramList = new ArrayList<>();
    private boolean init = false;
    private Page page;
    boolean escape;
    private boolean useOrder = true;

    public void addCondition(Condition condition) {
        addCondition("or1", condition);
    }

    public void addCondition(String column, String expr, Object value) {
        addCondition("or1", column, expr, value);
    }

    public void addCondition(String orGroup, Condition condition) {
        List<Condition> list = conditions.get(orGroup);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(condition);
        conditions.put(orGroup, list);
    }

    public void addCondition(String orGroup, String column, String expr, Object value) {
        if (value == null || value.toString().equals("")) {
            return;
        }
        Condition condition = new Condition(column, expr, value, this);
        addCondition(orGroup, condition);
    }

    /**
     * 无条件返回""；一组条件返回 X and Y and Z;多组条件返回 A and B ( (X and X1) or (Y and Y1) )，其中A/B为or1分组，通用分组，X-X1/Y-Y1为or2/or3分组
     * 
     * @return
     * @author:fuh
     * @createTime:2017年9月2日 下午3:10:52
     */
    public String getDirectWhere() {
        buildCondition();

        if (conditions.size() == 0) {// 无条件
            return "";
        } else if (conditions.size() == 1) {// 一组条件
            List<Condition> list = conditions.values().iterator().next();// 取得唯一一组条件，全部and连接
            return buildAndConditions(list);
        } else if (conditions.size() > 1) {// 多组条件
            List<Condition> or1List = conditions.get("or1");
            StringBuilder sb = new StringBuilder();
            if (!CollectionUtils.isEmpty(or1List)) {// or1条件不为空
                sb.append(buildAndConditions(or1List)).append(" and (");
            }
            int i = 0;
            for (String key : conditions.keySet()) {// 必然有其它组条件
                if (key.equals("or1")) {
                    continue;
                }
                if (i > 0) {
                    sb.append(" or ");
                }
                List<Condition> list = conditions.get(key);
                sb.append(" (").append(buildAndConditions(list)).append(") ");
                i++;
            }
            if (!CollectionUtils.isEmpty(or1List)) {
                sb.append(" )");
            }
            return sb.toString();
        }
        return "";
    }

    public String getWhere() {
        buildCondition();

        if (conditions.size() == 0) {// 无条件
            return "";
        } else if (conditions.size() == 1) {// 一组条件
            List<Condition> list = conditions.values().iterator().next();// 取得唯一一组条件，全部and连接
            return buildAndConditions(list, false);
        } else if (conditions.size() > 1) {// 多组条件
            List<Condition> or1List = conditions.get("or1");
            StringBuilder sb = new StringBuilder();
            if (!CollectionUtils.isEmpty(or1List)) {// or1条件不为空
                sb.append(buildAndConditions(or1List, false)).append(" and (");
            }
            int i = 0;
            for (String key : conditions.keySet()) {// 必然有其它组条件
                if (key.equals("or1")) {
                    continue;
                }
                if (i > 0) {
                    sb.append(" or ");
                }
                List<Condition> list = conditions.get(key);
                sb.append(" (").append(buildAndConditions(list, false)).append(") ");
                i++;
            }
            if (!CollectionUtils.isEmpty(or1List)) {
                sb.append(" )");
            }
            return sb.toString();
        }
        return "";
    }

    private String buildAndConditions(List<Condition> list) {
        return buildAndConditions(list, true);
    }

    private String buildAndConditions(List<Condition> list, boolean isDirect) {
        StringBuilder sb = new StringBuilder();
        int j = 0;
        for (Condition condition : list) {

            String con = null;
            if (isDirect) {
                con = condition.toString();
            } else {
                con = condition.buildString();
            }
            if (j > 0 && StringUtils.isNotEmpty(con)) {
                sb.append(" and ");
            }
            sb.append(con);
            j++;
        }
        return sb.toString();
    }

    public String getOrderBy() {
        StringBuilder sb = new StringBuilder();
        if (orderBys.size() > 0) {
            sb.append(" ORDER BY ");
        }
        int k = 0;
        for (String orderBy : orderBys) {
            if (k > 0) {
                sb.append(",");
            }
            sb.append(orderBy);
            k++;
        }
        return sb.toString();
    }

    public void clearOrderBy() {
        orderBys.clear();
    }

    private void buildCondition() {
        if (!init) {
            Class<?> clz = this.getClass();
            while (!clz.equals(BaseSearchForm.class)) {
                Field[] declaredFields = clz.getDeclaredFields();
                for (Field field : declaredFields) {
                    SearchExpr searchExpr = field.getAnnotation(SearchExpr.class);
                    if (searchExpr != null) {
                        String column = searchExpr.column();
                        if (StringUtil.isEmpty(column)) {
                            column = field.getName();
                        }
                        Object value = PojoUtils.getPropertyValue(this, field.getName());
                        String expr = searchExpr.expr();
                        String orGroup = searchExpr.orGroup();
                        addCondition(orGroup, column, expr, value);
                        String orderBy = searchExpr.orderBy();
                        if (useOrder) {
                            if (!StringUtil.isEmpty(orderBy)) {
                                orderBys.add(column + " " + orderBy);
                            }
                        }
                    }
                }
                clz = clz.getSuperclass();
            }
            if (useOrder) {
            OrderBy orderByAnno = this.getClass().getAnnotation(OrderBy.class);
                if (orderByAnno != null && StringUtils.isNotEmpty(orderByAnno.value())) {
                    String[] split = orderByAnno.value().split(",");
                    for (String orBy : split) {
                        orderBys.add(orBy);
                    }
                }
            }
            init = true;
        } else {
            conditions = new HashMap<>();
            orderBys = new ArrayList<>();
            paramList = new ArrayList<>();
            init = false;
            buildCondition();
        }
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public boolean isUseOrder() {
        return useOrder;
    }

    public void setUseOrder(boolean useOrder) {
        this.useOrder = useOrder;
    }

}
