package com.kwezal.bearinmind.core.auth.dto;

import java.util.Set;

public record LoginResponseDto(Set<String> authorities) {}
