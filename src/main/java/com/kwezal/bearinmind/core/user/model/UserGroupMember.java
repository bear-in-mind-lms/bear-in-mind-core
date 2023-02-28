package com.kwezal.bearinmind.core.user.model;

import com.kwezal.bearinmind.core.user.enumeration.UserGroupRole;
import java.time.OffsetDateTime;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_group_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    UserGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    UserGroupRole role;

    @Column
    OffsetDateTime registrationDateTime;
}
