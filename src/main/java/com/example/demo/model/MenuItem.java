package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @Data
public class MenuItem {
    @Id
    private UUID id;
    private UUID organizationId;
    private String name;
    private String description;
    private MenuItemStatusType status;
    private boolean seasonal;
    private MenuItemType type;
    private boolean houseSpecial;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Transient
    private List<MenuItemImage> images;

    public enum MenuItemCategoryEnum {
    }
}