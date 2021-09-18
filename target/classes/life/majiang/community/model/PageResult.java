package life.majiang.community.model;

import java.io.Serializable;
import java.util.List;

public class PageResult<T> implements Serializable {


    private List<T> rows;	//每页的数据
    private Long total;		//总共有多少记录数
    private Integer currentPage;	//当前页数(从1开始)
    private Integer pages;		//总共页数

    public Integer getPages() {
        return pages;
    }
    public Integer getPrePage() {
        return currentPage-1;
    }
    public Integer getNextPage() {
        return currentPage+1;
    }
    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public List<T> getRows() {
        return rows;
    }
    public void setRows(List<T> rows) {
        this.rows = rows;
    }
    public Long getTotal() {
        return total;
    }
    public void setTotal(Long total) {
        this.total = total;
    }




}

