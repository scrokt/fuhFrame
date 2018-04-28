package cn.ymcd.ods.db.base.search;

/**
 * 查询表单的接口
 * @author fuh
 * @since
 */
public interface SearchForm {

    String getDirectSearchSql();
    
    String getSearchSql();
    
    Object[] getSearchParams();
    
    Page getPage();
    
    String getOrderBy();
}
