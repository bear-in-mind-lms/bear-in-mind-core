package com.kwezal.bearinmind.core.user.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupDto {

    @NotNull
    private String name;

    private String image;

    @NotNull
    private List<UserListItemDto> members;
}
