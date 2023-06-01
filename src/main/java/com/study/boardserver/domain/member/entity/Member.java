package com.study.boardserver.domain.member.entity;

import com.study.boardserver.domain.member.type.MemberRole;
import com.study.boardserver.domain.member.type.MemberStatus;
import com.study.boardserver.domain.security.oauth2.type.ProviderType;
import com.study.boardserver.global.entity.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "member")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    private String name;

    @Column(unique = true)
    private String nickname;

    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    private String imgUrl;
}
