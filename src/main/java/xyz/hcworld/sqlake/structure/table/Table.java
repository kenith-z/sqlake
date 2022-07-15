package xyz.hcworld.sqlake.structure.table;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName: Table
 * @Author: 张冠诚
 * @Date: 2022/7/14 14:15
 * @Version： 1.0
 */
public class Table {

    /**行数
     *
     */
   private int numRows;

    private Map[] pages = new LinkedHashMap[100];


    public int getNumRows() {
        return numRows;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public Map[] getPages() {
        return pages;
    }

    public void setPages(Map[] pages) {
        this.pages = pages;
    }
}
