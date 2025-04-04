package com.example.demo.repository.util;

import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Table;

public class SqlUtils {

    public static Column createColumn(String columnName, Table table) {
        return table.column(columnName).as(table.getName().getReference() + "_" + columnName );
    }
}
