package com.pcd.user.records;

import com.pcd.user.enums.Role;

public record UserResponse(
        String id,
        String firstname,
        String lastname,
        String email,
        AddressDTO address, // A String representation
        Role role
) {}