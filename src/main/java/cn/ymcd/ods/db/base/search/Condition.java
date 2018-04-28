package cn.ymcd.ods.db.base.search;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;

import cn.ymcd.ods.util.DateUtil;
import cn.ymcd.ods.util.PojoUtils;

/**
 * 条件类，负责生成一个条件的表达式
 * 
 * @author fuh
 * @since 1.0
 */
public class Condition {

    private String column;
    private String expr;
    private Object value;
    private String orGroup;
    private BaseSearchForm searchForm;

    public Condition(String column, String expr, Object value, BaseSearchForm searchForm) {
        this.column = column;
        this.expr = expr;
        this.value = value;
        this.searchForm = searchForm;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (expr.equals("like")) {
            sb.append(column).append(" like '%").append(value.toString()).append("%'");
        } else if (expr.equals("rlike")) {
            sb.append(column).append(" like '%").append(value.toString()).append("'");
        } else if (expr.equals("llike")) {
            sb.append(column).append(" like '").append(value.toString()).append("%'");
        } else if (expr.equals("not like")) {
            sb.append(column).append(" not like '%").append(value.toString()).append("%'");
        } else if (expr.equals("not rlike")) {
            sb.append(column).append(" not like '%").append(value.toString()).append("'");
        } else if (expr.equals("not llike")) {
            sb.append(column).append(" not like '").append(value.toString()).append("%'");
        } else if (expr.equals("isNull")) {
            if (value.equals("yes")) {
                sb.append(column).append(" is null ");
            } else if (value.equals("no")) {
                sb.append(column).append(" is not null");
            }
        } else if (expr.equals("in") || expr.equals("not in")) {
            if (value instanceof Integer[]) {
                Integer[] in = (Integer[])value;
                if (ArrayUtils.isNotEmpty(in)) {
                    sb.append(column).append(" ").append(expr).append(" (");
                    for (int i = 0; i < in.length; i++) {
                        sb.append(in[i]);
                        if (i < in.length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")");
                } else if (in != null && in.length == 0 && expr.equals("in")) {// in一个空数组，说明没有任何数据
                    sb.append(" 1<>1 ");
                }
            } else if (value instanceof Long[]) {
                Long[] in = (Long[])value;
                if (ArrayUtils.isNotEmpty(in)) {
                    sb.append(column).append(" ").append(expr).append(" (");
                    for (int i = 0; i < in.length; i++) {
                        sb.append(in[i]);
                        if (i < in.length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")");
                } else if (in != null && in.length == 0 && expr.equals("in")) {// in一个空数组，说明没有任何数据
                    sb.append(" 1<>1 ");
                }
            } else if (value instanceof String[]) {
                String[] in = (String[])value;
                if (ArrayUtils.isNotEmpty(in)) {
                    sb.append(column).append(" ").append(expr).append(" (");
                    for (int i = 0; i < in.length; i++) {
                        sb.append("'").append(in[i]).append("'");
                        if (i < in.length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")");
                } else if (in != null && in.length == 0 && expr.equals("in")) {// in一个空数组，说明没有任何数据
                    sb.append(" 1<>1 ");
                }
            }
        } else if (expr.startsWith("define#")) {
            String method = expr.substring(7);
            Object retVal = PojoUtils.excuteMethod(searchForm, method);
            if (retVal != null && retVal instanceof String) {
                sb.append(retVal);
            }
        } else {
            sb.append(column).append(" ").append(expr).append(" ");
            if (value instanceof Number) {
                sb.append(value.toString());
            } else if (value instanceof String) {
                sb.append("'").append(value.toString()).append("'");
            } else if (value instanceof Date || value instanceof Timestamp) {
                // oracle
                if (expr.contains("maxTime")) {
                    sb.delete(0, sb.length() - 1);
                    sb.append(column).append(" ").append(expr.replace("maxTime", "")).append(" ");
                    Calendar instance = Calendar.getInstance();
                    instance.setTime((Date)value);
                    instance.set(Calendar.HOUR_OF_DAY, 23);
                    instance.set(Calendar.MINUTE, 59);
                    instance.set(Calendar.SECOND, 59);
                    instance.set(Calendar.MILLISECOND, 999);
                    sb.append("to_date('").append(DateUtil.format(instance.getTime(), DateUtil.DATE_TIME_PATTERN))
                            .append("','yyyy-mm-dd hh24:mi:ss')");
                } else {
                    sb.append("to_date('").append(DateUtil.format((Date)value, DateUtil.DATE_TIME_PATTERN))
                            .append("','yyyy-mm-dd hh24:mi:ss')");
                }
            }
        }
        return sb.toString();
    }

    public String buildString() {
        StringBuilder sb = new StringBuilder();
        if (expr.equals("like")) {
            if (needConvert(value)) {
                sb.append(column).append(" like ? escape '\\'");
                searchForm.paramList.add("%" + convert(value) + "%");
            } else {
                sb.append(column).append(" like ?");
                searchForm.paramList.add("%" + value + "%");
            }
        } else if (expr.equals("rlike")) {
            if (needConvert(value)) {
                sb.append(column).append(" like ? escape '\\'");
                searchForm.paramList.add("%" + convert(value));
            } else {
                sb.append(column).append(" like ?");
                searchForm.paramList.add("%" + value);
            }
        } else if (expr.equals("llike")) {
            if (needConvert(value)) {
                sb.append(column).append(" like ? escape '\\'");
                searchForm.paramList.add(convert(value) + "%");
            } else {
                sb.append(column).append(" like ?");
                searchForm.paramList.add(value + "%");
            }
        } else if (expr.equals("not like")) {
            if (needConvert(value)) {
                sb.append(column).append(" not like ? escape '\\'");
                searchForm.paramList.add("%" + convert(value) + "%");
            } else {
                sb.append(column).append(" not like ?");
                searchForm.paramList.add("%" + value + "%");
            }
        } else if (expr.equals("not rlike")) {
            if (needConvert(value)) {
                sb.append(column).append(" not like ? escape '\\'");
                searchForm.paramList.add("%" + convert(value));
            } else {
                sb.append(column).append(" not like ?");
                searchForm.paramList.add("%" + value);
            }
        } else if (expr.equals("not llike")) {
            if (needConvert(value)) {
                sb.append(column).append(" not like ? escape '\\'");
                searchForm.paramList.add(convert(value) + "%");
            } else {
                sb.append(column).append(" not like ?");
                searchForm.paramList.add(value + "%");
            }
        } else if (expr.equals("isNull")) {
            if (value.equals("yes")) {
                sb.append(column).append(" is null ");
            } else if (value.equals("no")) {
                sb.append(column).append(" is not null");
            }
        } else if (expr.equals("in") || expr.equals("not in")) {
            if (value instanceof Integer[]) {
                Integer[] in = (Integer[])value;
                if (ArrayUtils.isNotEmpty(in)) {
                    sb.append(column).append(" ").append(expr).append(" (");
                    for (int i = 0; i < in.length; i++) {
                        sb.append(in[i]);
                        if (i < in.length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")");
                } else if (in != null && in.length == 0 && expr.equals("in")) {// in一个空数组，说明没有任何数据
                    sb.append(" 1<>1 ");
                }
            } else if (value instanceof Long[]) {
                Long[] in = (Long[])value;
                if (ArrayUtils.isNotEmpty(in)) {
                    sb.append(column).append(" ").append(expr).append(" (");
                    for (int i = 0; i < in.length; i++) {
                        sb.append(in[i]);
                        if (i < in.length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")");
                } else if (in != null && in.length == 0 && expr.equals("in")) {// in一个空数组，说明没有任何数据
                    sb.append(" 1<>1 ");
                }
            } else if (value instanceof String[]) {
                String[] in = (String[])value;
                if (ArrayUtils.isNotEmpty(in)) {
                    sb.append(column).append(" ").append(expr).append(" (");
                    for (int i = 0; i < in.length; i++) {
                        sb.append("'").append(in[i]).append("'");
                        if (i < in.length - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")");
                } else if (in != null && in.length == 0 && expr.equals("in")) {// in一个空数组，说明没有任何数据
                    sb.append(" 1<>1 ");
                }
            }
        } else if (expr.startsWith("define#")) {
            String method = expr.substring(7);
            Object retVal = PojoUtils.excuteMethod(searchForm, method);
            if (retVal != null && retVal instanceof String) {
                sb.append(retVal);
            }
        } else {

            if (expr.contains("maxTime")) {
                sb.append(column).append(" ").append(expr.replace("maxTime", "")).append(" ?");

                Calendar instance = Calendar.getInstance();
                instance.setTime((Date)value);
                instance.set(Calendar.HOUR_OF_DAY, 23);
                instance.set(Calendar.MINUTE, 59);
                instance.set(Calendar.SECOND, 59);
                instance.set(Calendar.MILLISECOND, 999);

                searchForm.paramList.add(instance.getTime());
            } else {
                sb.append(column).append(" ").append(expr).append(" ?");
                searchForm.paramList.add(value);
            }
        }
        return sb.toString();
    }

    public boolean needConvert(Object param) {
        if (param instanceof String) {
            String ps = (String)param;
            if (ps.matches(".*[_%]+.*")) {
                return true;
            }
            return false;
        }
        return false;
    }

    public String convert(Object param) {
        if (param instanceof String) {
            String ps = (String)param;
            if (ps.matches(".*[_%]+.*")) {
                ps = ps.replace("_", "\\_");
                ps = ps.replace("%", "\\%");
                // searchForm.escape = true;
            }
            return ps;
        }
        return param.toString();
    }

    public String getOrGroup() {
        return orGroup;
    }

    public void setOrGroup(String orGroup) {
        this.orGroup = orGroup;
    }

}
