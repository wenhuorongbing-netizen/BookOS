package com.bookos.backend.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForumCategoryRequest(
        @NotBlank(message = "Category name is required.")
        @Size(max = 120, message = "Category name must be at most 120 characters.")
        String name,

        @Size(max = 1000, message = "Category description must be at most 1000 characters.")
        String description,

        Integer sortOrder) {}
