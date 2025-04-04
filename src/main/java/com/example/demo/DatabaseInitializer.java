package com.example.demo;


import com.example.demo.repository.MenuItemSqlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final MenuItemSqlRepository menuItemRepository;

    public DatabaseInitializer(MenuItemSqlRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Example: Save initial menu items
        UUID orgId = UUID.fromString("b8d34b26-7a37-43e3-9256-b741ca4129c8");

        menuItemRepository.findAllByOrganizationWithImages(orgId)
                .subscribe(item -> {
                    log.info("[FOUND ITEM] {}", item.toString());
                    item.getImages().forEach(image -> log.info("[ITEM IMAGE] {}", image.toString()));
                }, error -> log.error(error.getMessage()));

        Thread.sleep(2000);

    }
}