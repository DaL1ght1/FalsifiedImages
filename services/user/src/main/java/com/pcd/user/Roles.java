package com.pcd.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pcd.user.Permission.*;
import static java.util.stream.Collectors.toList;


@RequiredArgsConstructor
public enum Roles {
    Admin(Set.of(ADMIN_READ, ADMIN_UPDATE, ADMIN_CREATE, ADMIN_DELETE)),
    Investigator(Set.of(INVESTIGATOR_READ, INVESTIGATOR_SUBMIT)),
    Lawyer(Set.of(LAWYER_READ, LAWYER_SUBMIT, LAWYER_EXPORT)),
    Judge(Set.of(JUDGE_READ, JUDGE_HISTORY)),
    Expert(Set.of(EXPERT_ANALYZE, EXPERT_UPLOAD, EXPERT_ANNOTATE, EXPERT_REPORT));


    @Getter
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_"+this.name()));
        return authorities;
    }
}
