package xyz.hcworld.sqlake.analysis;

import xyz.hcworld.sqlake.emums.StatementType;
import xyz.hcworld.sqlake.structure.table.Row;

/**
 *  sql解析
 * @ClassName: Statement
 * @Author: 张冠诚
 * @Date: 2022/7/14 10:16
 * @Version： 1.0
 */
public class Statement {
    /**
     * sql的类型。增删改查
     */
    StatementType type;

    Row rowToInsert;

    public StatementType getType() {
        return type;
    }

    public void setType(StatementType type) {
        this.type = type;
    }

    public Row getRowToInsert() {
        return rowToInsert;
    }

    public void setRowToInsert(Row rowToInsert) {
        this.rowToInsert = rowToInsert;
    }
}
