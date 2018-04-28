package cn.ymcd.ods.db.base.search;

import org.apache.commons.lang.StringUtils;

public class OtherSearchForm implements SearchForm {

    private String table;
    private String columns;
    private String[] and;
    private String[] or;
    private Page page;
    private String orderBy;

    @Override
    public String getDirectSearchSql() {
        StringBuilder sb = new StringBuilder();
        sb.append("select ").append(columns).append(" from ").append(table).append(" where 1=1");
        if(and != null){
            sb.append(" and ");
            int i=0;
            for(String con : or){
                if(i>0){
                    sb.append(" and ");
                }
                sb.append(con.replace("_", "="));
                i++;
            }
        }
        if(or !=null){
            sb.append(" and (");
            int i=0;
            for(String con : or){
                if(i>0){
                    sb.append(" or ");
                }
                sb.append(con.replace("_", "="));
                i++;
            }
            sb.append(")");
        }
        if(!StringUtils.isEmpty(orderBy)){
            sb.append(" order by ").append(orderBy);
        }
        return sb.toString();
    }

    @Override
    public String getSearchSql() {
        return null;
    }

    @Override
    public Object[] getSearchParams() {
        return null;
    }

    @Override
    public Page getPage() {
        return page;
    }

    @Override
    public String getOrderBy() {
        StringBuilder sb = new StringBuilder();
        if(!StringUtils.isEmpty(orderBy)){
            sb.append(" order by ").append(orderBy);
        }
        return sb.toString();
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String[] getAnd() {
        return and;
    }

    public void setAnd(String[] and) {
        this.and = and;
    }

    public String[] getOr() {
        return or;
    }

    public void setOr(String[] or) {
        this.or = or;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

}
