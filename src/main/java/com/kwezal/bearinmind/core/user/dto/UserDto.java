package com.kwezal.bearinmind.core.user.dto;

import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;
    private String username;
    private String password;
    private String firstName;
    private String middleName;
    private String lastName;
    private String title;
    private String email;
    private String phoneNumber;
    private Locale locale;
    private String image;
    private UserRole role;
    private Boolean active;
}
