package com.example.demo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @Data
public class MenuItemImage {
    @Id
    private UUID id;
    private UUID menuItemId;
    private UUID organizationId;
    private String altText;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}