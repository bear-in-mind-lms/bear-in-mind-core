package com.kwezal.bearinmind.core.user.model;

import com.kwezal.bearinmind.core.user.dto.UserRole;
import javax.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_credentials")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    private Boolean active = true;
}
