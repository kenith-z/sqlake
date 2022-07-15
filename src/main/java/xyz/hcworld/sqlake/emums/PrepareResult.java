package xyz.hcworld.sqlake.emums;

/**
 * 准备结果
 * @ClassName: PrepareResult
 * @Author: 张冠诚
 * @Date: 2022/7/14 10:04
 * @Version： 1.0
 */
public enum PrepareResult {
    /**
     *解析sql语法成功
     */
    PREPARE_SUCCESS,
    /**
     * id小于0
     */
    PREPARE_NEGATIVE_ID,
    /**
     * sql语法错误
     */
    PREPARE_SYNTAX_ERROR,
    /**
     * 解析sql语法失败
     */
    PREPARE_UNRECOGNIZED_STATEMENT
}
