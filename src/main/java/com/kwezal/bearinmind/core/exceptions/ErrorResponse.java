package com.kwezal.bearinmind.core.exceptions;

import java.util.List;
import org.springframework.lang.Nullable;

public record ErrorResponse(String code, @Nullable List<String> arguments) {}
