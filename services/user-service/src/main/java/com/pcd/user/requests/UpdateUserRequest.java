package com.pcd.user.requests;

import com.pcd.user.enums.Role;
import com.pcd.user.model.Address;
import com.pcd.user.records.AddressDTO;
import lombok.Data;

@Data
public  class UpdateUserRequest {
    private String firstname;
    private String lastname;
    private String email;
    private Role role;
    private AddressDTO address;
    private Boolean active;
}