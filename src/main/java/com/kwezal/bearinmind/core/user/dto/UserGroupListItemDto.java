package com.kwezal.bearinmind.core.user.dto;

import jakarta.validation.constraints.NotNull;

public record UserGroupListItemDto(@NotNull Long id, @NotNull String name, String image) {}
