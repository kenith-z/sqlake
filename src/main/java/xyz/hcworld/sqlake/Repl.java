package xyz.hcworld.sqlake;

import xyz.hcworld.sqlake.analysis.Statement;
import xyz.hcworld.sqlake.emums.ExecuteResult;
import xyz.hcworld.sqlake.emums.MetaCommandResult;
import xyz.hcworld.sqlake.emums.PrepareResult;
import xyz.hcworld.sqlake.structure.table.Row;
import xyz.hcworld.sqlake.structure.table.Table;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import static xyz.hcworld.sqlake.emums.ExecuteResult.*;
import static xyz.hcworld.sqlake.emums.MetaCommandResult.*;
import static xyz.hcworld.sqlake.emums.PrepareResult.*;
import static xyz.hcworld.sqlake.emums.StatementType.*;

/**
 * 交互式解释器
 *
 * @ClassName: REPL
 * @Author: 张冠诚
 * @Date: 2022/7/14 9:28
 * @Version： 1.0
 */
public class Repl {
    static int TABLE_MAX_PAGES = 100;
    static int PAGE_SIZE = 4096;
    static int ROW_SIZE = 8 + 32 << 1 + 255 << 2;
    static int ROWS_PER_PAGE = PAGE_SIZE / ROW_SIZE;

    static long TABLE_MAX_ROWS = ROWS_PER_PAGE * TABLE_MAX_PAGES;


    public static void main(String[] args) {
        printDataBaseInfoPrompt();
        Table table = new Table();
        Scanner sc = new Scanner(System.in);
        sc.useDelimiter("\n");
        long beginDate;
        while (true) {
            printPrompt();
            String inputStr = sc.next().trim();
            beginDate = System.currentTimeMillis();
            if (inputStr.startsWith(".")) {
                switch (doMetaCommand(inputStr)) {
                    case META_COMMAND_SUCCESS:
                        continue;
                    case META_COMMAND_UNRECOGNIZED_COMMAND:
                        System.out.printf("无法识别的命令 '%s'\n", inputStr);
                        continue;
                    default:
                }
            }
            Statement statement = new Statement();

            switch (prepareStatement(inputStr, statement)) {
                case PREPARE_SUCCESS:
                    break;
                case PREPARE_NEGATIVE_ID:
                    System.out.println("Error: ID必须为正整数");
                    continue;
                case PREPARE_SYNTAX_ERROR:
                    System.out.println("Error: 语法错误，无法分析该SQL语句");
                    continue;
                case PREPARE_UNRECOGNIZED_STATEMENT:
                    System.out.printf("Error: 无法识别的开始关键字 '%s'\n", inputStr);
                    continue;
                default:
            }
            switch (executeStatement(statement, table)) {
                case EXECUTE_SUCCESS:
                    System.out.printf("执行完成时间: %d ms\n", System.currentTimeMillis() - beginDate);
                    break;
                case EXECUTE_TABLE_FULL:
                    System.out.printf("Error: 表格已满。执行完成时间: %d ms\n", System.currentTimeMillis() - beginDate);
                    break;
                default:
            }
        }


    }

    /**
     * 命令执行前缀
     */
    public static void printPrompt() {
        System.out.print("db > ");
    }

    public static void printDataBaseInfoPrompt() {
        System.out.println("Welcome to the SQLake Commands end with ;.");
        System.out.println("Your SQLake version: 0.0.1");
    }

    /**
     * 元命令执行方法
     *
     * @param inputStr
     * @return
     */
    private static MetaCommandResult doMetaCommand(String inputStr) {
        if (".exit".equals(inputStr)) {
            System.exit(0);
        }
        return META_COMMAND_UNRECOGNIZED_COMMAND;
    }

    /**
     * 解析sql语法
     *
     * @return
     */
    private static PrepareResult prepareStatement(String inputStr, Statement statement) {
        if (inputStr.length() >= 6 && "insert".equalsIgnoreCase(inputStr.substring(0, 6))) {
            return prepareInsert(inputStr, statement);
        }
        if (inputStr.length() >=6 && "select".equalsIgnoreCase(inputStr.substring(0, 6))) {
            statement.setType(STATEMENT_SELECT) ;
            return PREPARE_SUCCESS;
        }
        return PREPARE_UNRECOGNIZED_STATEMENT;
    }

    /**
     * 解析插入语句
     *
     * @param inputStr
     * @param statement
     * @return
     */
    static PrepareResult prepareInsert(String inputStr, Statement statement) {
        statement.setType(STATEMENT_INSERT)  ;
        //
        String textParameter = inputStr.split("insert")[1].trim();
        String[] s = textParameter.split(" ");
        if (s.length < 3) {
            return PREPARE_SYNTAX_ERROR;
        }
        statement.setRowToInsert(new Row());
        Scanner sc = new Scanner(textParameter);

        String idString = sc.next();
        String username = sc.next();
        String email = sc.next();
        if (idString == null || username == null || email == null) {
            return PREPARE_SYNTAX_ERROR;
        }
        long id = Long.parseLong(idString);
        if (id < 0) {
            return PREPARE_NEGATIVE_ID;
        }
        statement.getRowToInsert().setId(id);
        statement.getRowToInsert().setUsername(username);
        statement.getRowToInsert().setEmail(email);
        return PREPARE_SUCCESS;
    }


    /**
     * 执行插入语句
     *
     * @param statement sql解析后的sql语句类型与参数
     * @param table
     * @return
     */
    private static ExecuteResult executeInsert(Statement statement, Table table) {
        if (table.getNumRows() >= TABLE_MAX_ROWS) {
            return EXECUTE_TABLE_FULL;
        }
        Row rowToInsert = statement.getRowToInsert();
        serializeRow(rowToInsert, rowSlot(table, table.getNumRows()), table.getPages()[table.getNumRows() / ROWS_PER_PAGE]);
        table.setNumRows(table.getNumRows()+ 1) ;
        return EXECUTE_SUCCESS;
    }

    /**
     * 查询数据
     *
     * @param statement
     * @param table
     * @return
     */
    private static ExecuteResult executeSelect(Statement statement, Table table) {
//        Row row = new Row();
        for (int i = 0; i < table.getNumRows(); i++) {
//            deserializeRow(rowSlot(table, i), row,table.getPages()[ i/ ROWS_PER_PAGE]);
            printRow((Row) table.getPages()[i / ROWS_PER_PAGE].get(rowSlot(table, i)));
        }
//        System.out.println(Arrays.asList(table.getPages()).toString());
        return EXECUTE_SUCCESS;
    }

    /**
     * 每页的元素计算偏移量（求膜hash）
     *
     * @param table  表
     * @param rowNum 表行数
     * @return
     */
    static Integer rowSlot(Table table, int rowNum) {
        int pageNum = rowNum / ROWS_PER_PAGE;
        Map<Integer, Object> page = table.getPages()[pageNum];
        if (page == null) {
            page = table.getPages()[pageNum] = new LinkedHashMap<>(ROWS_PER_PAGE);
        }
        return rowNum % ROWS_PER_PAGE;
    }

    /**
     * 执行sql
     *
     * @param statement
     */
    private static ExecuteResult executeStatement(Statement statement, Table table) {
        switch (statement.getType()) {
            case STATEMENT_INSERT:
                return executeInsert(statement, table);
            case STATEMENT_SELECT:
                return executeSelect(statement, table);
            default:
                return null;
        }
    }


    /**
     * 插入数据
     *
     * @param source
     * @param destination
     */
    static void serializeRow(Row source, Integer destination, Map<Integer, Object> page) {
        page.put(destination, source);
    }

    /**
     * 获取数据
     *
     * @param source
     * @param destination
     */
    static void deserializeRow(Integer source, Row destination, Map<Integer, Object> page) {
        destination = (Row) page.get(source);
    }

    /**
     * 控制台打印
     *
     * @param row 行数据
     */
    static void printRow(Row row) {
        System.out.printf("(%d, %s, %s)\n", row.getId(), row.getUsername(), row.getEmail());
    }


}
