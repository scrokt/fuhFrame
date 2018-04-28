package cn.ymcd.ods.db.base.search;

/**
 * 分页器
 * @author fuh
 * @since 1.0
 */
public class Page {
    // 第几页
    private int pageIndex = 1;
    // 每页大小
    private int pageSize = 10;
    // 总数
    private long rowTotal;
    // 总共多少页
    private long pageTotal;
    //默认需要查询总条数和总页数
    private boolean selectTotalRow = true;
    
    public static final Page NULL = new Page();

    public Page() {
    }

    public Page(int pageIndex,int pageSize,int rowTotal) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.rowTotal = rowTotal;
    }

    public long getRowTotal() {
        return rowTotal;
    }

    public void setRowTotal(long rowTotal) {
        this.rowTotal = rowTotal;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getPageTotal() {
        if (rowTotal % pageSize == 0) {
            pageTotal = this.rowTotal / this.pageSize;

        } else {
            pageTotal = rowTotal / pageSize + 1;
        }
        return pageTotal;
    }

    public void setPageTotal(int pageTotal) {
        this.pageTotal = pageTotal;
    }

    public boolean getSelectTotalRow() {
        return selectTotalRow;
    }

    public void setSelectTotalRow(boolean selectTotalRow) {
        this.selectTotalRow = selectTotalRow;
    }
    
}
