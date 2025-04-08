package com.pcd.user.model;


import com.pcd.Token.Token;
import com.pcd.user.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import jakarta.persistence.Id;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Setter
@Getter
@Table(name = "app_users")
public class User implements UserDetails, Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String firstname;
    private String lastname;
    @Column(unique = true)
    @Email
    private String email;

    private Address address;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Token> tokens;
    private Boolean active;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    @Override
    public String getName() {
        return email;
    }


}
