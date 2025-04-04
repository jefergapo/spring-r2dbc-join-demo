package com.example.demo.repository;

import com.example.demo.repository.util.SqlUtils;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Table;

public class MenuItemImageSqlRepository {
    static class MenuItemImageTable {
        public static final String TABLE_NAME = "menu_item_image";
        public static final Table table = Table.create(TABLE_NAME);
        public static final Column id = SqlUtils.createColumn("id", table);
        public static final Column menu_item_id = SqlUtils.createColumn("menu_item_id", table);
        public static final Column url = SqlUtils.createColumn("url", table);
        public static final Column alt_text = SqlUtils.createColumn("alt_text", table);
        public static final Column created_at = SqlUtils.createColumn("created_at", table);
        public static final Column updated_at = SqlUtils.createColumn("updated_at", table);
        public static final Column[] allColumns = {
                id,
                menu_item_id,
                url,
                alt_text,
                created_at,
                updated_at
        };
    }
}