package com.kwezal.bearinmind.core.auth.dto;

import java.util.Set;

public record LoginResponseDto(Long userId, String userFullName, String userImage, Set<String> authorities) {}
