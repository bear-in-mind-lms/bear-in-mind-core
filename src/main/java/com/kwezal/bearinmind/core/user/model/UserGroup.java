package com.kwezal.bearinmind.core.user.model;

import java.time.OffsetDateTime;
import java.util.List;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    Integer nameIdentifier;

    @Column
    String image;

    @Column(nullable = false)
    OffsetDateTime creationDateTime;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    List<UserGroupMember> members;
}
