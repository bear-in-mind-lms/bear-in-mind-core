package com.kwezal.bearinmind.core.user.dto;

import javax.validation.constraints.NotNull;

public record UserListItemDto(@NotNull Long id, @NotNull String name, String image) {}
