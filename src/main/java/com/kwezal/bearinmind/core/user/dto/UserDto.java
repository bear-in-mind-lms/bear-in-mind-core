package com.kwezal.bearinmind.core.user.dto;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    // User
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String title;
    private String email;
    private String phoneNumber;
    private String locale;
    private String image;
    private OffsetDateTime registrationDateTime;

    // User credentials
    private String username;
    private String password;
    private UserRole role;
    private Boolean active;
}
