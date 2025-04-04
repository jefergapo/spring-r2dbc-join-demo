package com.example.demo.repository;

import com.example.demo.model.MenuItem;
import com.example.demo.model.MenuItemImage;
import com.example.demo.repository.util.RowObjectMapper;
import com.example.demo.repository.util.SqlUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.sql.*;
import org.springframework.data.relational.core.sql.render.SqlRenderer;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class MenuItemSqlRepository {
    private final R2dbcEntityTemplate template;

    public MenuItemSqlRepository(R2dbcEntityTemplate template) {
        this.template = template;
    }


    public Mono<MenuItem> save(MenuItem menuItem) {
        return template.insert(MenuItem.class).using(menuItem);
    }

    /**
     * Retrieves all MenuItems associated with a given organization ID, along with their associated images.
     * This implementation demonstrates how to perform a JOIN operation using Spring R2DBC's programmatic SQL API.
     *
     * @param organizationId The UUID of the organization whose menu items are to be retrieved.
     * @return A Flux of MenuItem objects, where each MenuItem contains a list of its associated MenuItemImages.
     */
    public Flux<MenuItem> findAllByOrganizationWithImages(UUID organizationId) {
        // 1. Define the columns to select from both tables.
        // We combine all columns from MenuItemTable and MenuItemImageTable.
        Column[] columns = ArrayUtils.addAll(MenuItemTable.allColumns, MenuItemImageSqlRepository.MenuItemImageTable.allColumns);

        // 2. Build the SELECT statement programmatically using Spring Data Relational's SQL DSL.
        Select select = Select.builder()
                .select(columns) // Select all defined columns.
                .from(MenuItemTable.table) // Start with the MenuItemTable.
                .join(MenuItemImageSqlRepository.MenuItemImageTable.table) // Perform an inner join with MenuItemImageTable.
                .on(MenuItemImageSqlRepository.MenuItemImageTable.menu_item_id) // Specify the join condition: menuItemImage.menu_item_id
                .equals(MenuItemTable.id) // is equal to menuItem.id.
                .where(MenuItemTable.organization_id.isEqualTo(SQL.literalOf(organizationId.toString()))) // Filter by the given organization ID.
                .build();

        // 3. Render the programmatic SQL statement into a plain SQL string.
        SqlRenderer renderer = SqlRenderer.create();
        String renderedSelect = renderer.render(select);

        // 4. Execute the rendered SQL query using R2dbcEntityTemplate's DatabaseClient.
        return template.getDatabaseClient()
                .sql(renderedSelect) // Provide the rendered SQL string.
                .fetch().all() // Fetch all rows returned by the query as a Flux of Map<String, Object>.
                // 5. Buffer the results until the 'id' of the MenuItem changes.
                // This is crucial because a single MenuItem can have multiple images, resulting in multiple rows
                // with the same MenuItem ID but different image details.
                .bufferUntilChanged(row -> row.get(MenuItemTable.id.toString()))
                // 6. Map each buffered list of rows (representing a single MenuItem and its images) to a MenuItem model.
                .map(this::mapToModel);
    }

    /**
     * Maps a list of rows (representing a MenuItem and its associated images) to a MenuItem domain model.
     *
     * @param rows A list of Maps, where each map represents a row returned from the JOIN query. The first row
     * in the list contains the main MenuItem attributes, and subsequent rows (if any) contain
     * the attributes of associated MenuItemImages.
     * @return A MenuItem object populated with data from the rows, including a list of its images.
     */
    private MenuItem mapToModel(List<Map<String, Object>> rows) {
        // The first row in the list contains the MenuItem's base attributes.
        Map<String, Object> row = rows.getFirst();
        // Use RowObjectMapper to map the columns from the MenuItem table to a MenuItem object.
        MenuItem menuItem = RowObjectMapper.apply(row, MenuItem.class, MenuItemTable.TABLE_NAME);

        // Create a list to hold the MenuItemImages.
        List<MenuItemImage> images = rows.stream()
                // For each row in the list (including the first one), attempt to map it to a MenuItemImage.
                .map(rowImage ->
                        // Use RowObjectMapper to map the columns from the MenuItemImage table to a MenuItemImage object.
                        RowObjectMapper.apply(rowImage,
                                MenuItemImage.class,
                                MenuItemImageSqlRepository.MenuItemImageTable.TABLE_NAME))
                .toList();

        // Set the list of images to the MenuItem.
        menuItem.setImages(images);

        return menuItem;
    }

    public Mono<Void> deleteById(String id) {
        return template.delete(MenuItem.class)
                .matching(Query.query(Criteria.where("id").is(id)))
                .all()
                .then();
    }

    /**
     * Defines the schema and column names for the 'menu_item' table.
     * This static inner class provides a structured way to refer to table and column names,
     * reducing the risk of typos and improving code readability.
     */
    static class MenuItemTable {
        private static final String TABLE_NAME = "menu_item";

        public static Table table = Table.create(TABLE_NAME);
        public static Column id = SqlUtils.createColumn("id", table);
        public static Column organization_id = SqlUtils.createColumn("organization_id", table);
        public static Column name = SqlUtils.createColumn("name", table);
        public static Column description = SqlUtils.createColumn("description", table);
        public static Column status = SqlUtils.createColumn("status", table);
        public static Column seasonal = SqlUtils.createColumn("seasonal", table);
        public static Column type = SqlUtils.createColumn("type", table);
        public static Column house_special = SqlUtils.createColumn("house_special", table);
        public static Column price = SqlUtils.createColumn("price", table);
        public static Column created_at = SqlUtils.createColumn("created_at", table);
        public static Column updated_at = SqlUtils.createColumn("updated_at", table);
        public static Column[] allColumns = {
                id
                , organization_id
                , name
                , description
                , status
                , seasonal
                , type
                , house_special
                , price
                , created_at
                , updated_at
        };

    }
}
