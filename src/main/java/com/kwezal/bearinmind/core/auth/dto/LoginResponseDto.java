package com.kwezal.bearinmind.core.auth.dto;

import java.util.Set;

public record LoginResponseDto(Long userId, Set<String> authorities) {}
