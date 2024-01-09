package com.kwezal.bearinmind.core.user.model;

import java.time.OffsetDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "user_credentials_id", referencedColumnName = "id")
    UserCredentials userCredentials;

    @Column(nullable = false)
    String firstName;

    @Column
    String middleName;

    @Column(nullable = false)
    String lastName;

    @Column
    String title;

    @Column(nullable = false)
    String email;

    @Column
    String phoneNumber;

    @Column(nullable = false)
    String locale;

    @Column
    String image;

    @Column(nullable = false)
    OffsetDateTime registrationDateTime;
}
